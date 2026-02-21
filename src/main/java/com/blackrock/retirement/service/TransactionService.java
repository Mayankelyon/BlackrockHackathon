package com.blackrock.retirement.service;

import com.blackrock.retirement.dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    private static final double NPS_RATE = 0.0711;
    private static final double INDEX_RATE = 0.1449;
    private static final double NPS_MAX_DEDUCTION = 200_000;
    private static final double MAX_AMOUNT = 500_000;

    // --- Parse: ceiling = next multiple of 100, remanent = ceiling - amount ---
    public List<TransactionDto> parse(List<ExpenseInput> expenses) {
        if (expenses == null) return List.of();
        List<TransactionDto> result = new ArrayList<>();
        for (ExpenseInput e : expenses) {
            TransactionDto t = new TransactionDto();
            String dateStr = e.getTimestamp() != null ? e.getTimestamp().trim() : null;
            if (dateStr != null && dateStr.length() == 16) dateStr = dateStr + ":00";
            LocalDateTime parsed = DateTimeSupport.parse(dateStr);
            t.setDate(parsed != null ? DateTimeSupport.format(parsed) : dateStr);
            t.setAmount(e.getAmount());
            double ceiling = Math.ceil(e.getAmount() / 100.0) * 100.0;
            double remanent = ceiling - e.getAmount();
            t.setCeiling(ceiling);
            t.setRemanent(remanent);
            result.add(t);
        }
        return result;
    }

    // --- Validator: valid, invalid, duplicate (by date) ---
    public ValidatorResponseDto validate(double wage, double maxAmountToInvest, List<TransactionDto> transactions) {
        ValidatorResponseDto out = new ValidatorResponseDto();
        out.setValid(new ArrayList<>());
        out.setInvalid(new ArrayList<>());
        out.setDuplicate(new ArrayList<>());
        if (transactions == null) return out;

        Set<String> seenDates = new HashSet<>();
        double annualIncome = wage * 12;
        double maxAllowed = Math.min(maxAmountToInvest, Math.min(NPS_MAX_DEDUCTION, annualIncome * 0.10));

        for (TransactionDto t : transactions) {
            String dateKey = t.getDate() != null ? t.getDate() : "";
            if (seenDates.contains(dateKey)) {
                out.getDuplicate().add(cloneTransaction(t));
                continue;
            }
            InvalidTransactionDto inv = validateOne(t, maxAllowed);
            if (inv != null) {
                out.getInvalid().add(inv);
            } else {
                seenDates.add(dateKey);
                out.getValid().add(cloneTransaction(t));
            }
        }
        return out;
    }

    private InvalidTransactionDto validateOne(TransactionDto t, double maxAllowed) {
        String msg = null;
        if (t.getAmount() == null || t.getAmount() < 0) msg = "Invalid or negative amount";
        else if (t.getCeiling() == null || t.getRemanent() == null)
            msg = "Missing ceiling or remanent";
        else {
            double expectedCeiling = Math.ceil(t.getAmount() / 100.0) * 100.0;
            double expectedRemanent = expectedCeiling - t.getAmount();
            if (Math.abs(t.getCeiling() - expectedCeiling) > 0.01 || Math.abs(t.getRemanent() - expectedRemanent) > 0.01)
                msg = "Ceiling/remanent inconsistent with amount";
        }
        if (msg == null && t.getCeiling() != null && Math.abs(t.getCeiling() % 100) > 0.01) msg = "Ceiling must be multiple of 100";
        if (msg == null && t.getRemanent() != null && t.getRemanent() < 0) msg = "Remanent cannot be negative";
        if (msg == null && t.getRemanent() != null && t.getRemanent() > maxAllowed) msg = "Remanent exceeds maximum allowed to invest";
        if (msg == null) return null;
        InvalidTransactionDto inv = new InvalidTransactionDto();
        copyTransaction(t, inv);
        inv.setMessage(msg);
        return inv;
    }

    // --- Filter: apply q then p, return valid (all with updated remanent) and invalid ---
    public FilterResponseDto filter(List<QPeriodDto> q, List<PPeriodDto> p, List<KPeriodDto> k,
                                    List<TransactionDto> transactions) {
        FilterResponseDto out = new FilterResponseDto();
        out.setValid(new ArrayList<>());
        out.setInvalid(new ArrayList<>());
        if (transactions == null) return out;

        for (TransactionDto t : transactions) {
            LocalDateTime txTime = DateTimeSupport.parse(t.getDate() != null ? t.getDate() : t.getTimestamp());
            if (txTime == null) {
                InvalidTransactionDto inv = new InvalidTransactionDto();
                copyTransaction(t, inv);
                inv.setMessage("Invalid date format");
                out.getInvalid().add(inv);
                continue;
            }
            TransactionDto copy = cloneTransaction(t);
            double rem = copy.getRemanent() != null ? copy.getRemanent() : 0;

            // Step 2: q - replace with fixed (matching q that starts latest wins; same start = first in list)
            if (q != null && !q.isEmpty()) {
                QPeriodDto chosen = null;
                for (QPeriodDto qp : q) {
                    if (DateTimeSupport.inRangeInclusive(txTime, qp.getStart(), qp.getEnd())) {
                        if (chosen == null || DateTimeSupport.startsLater(qp.getStart(), chosen.getStart()))
                            chosen = qp;
                    }
                }
                if (chosen != null) rem = chosen.getFixed();
            }

            // Step 3: p - add all matching extras
            if (p != null) {
                for (PPeriodDto pp : p) {
                    if (DateTimeSupport.inRangeInclusive(txTime, pp.getStart(), pp.getEnd()))
                        rem += pp.getExtra();
                }
            }

            copy.setRemanent(rem);
            out.getValid().add(copy);
        }
        return out;
    }

    // --- Returns: apply q/p, group by k, then compound + inflation (+ tax for NPS) ---
    public ReturnsResponseDto returnsNps(ReturnsRequestDto req) {
        return computeReturns(req, true);
    }

    public ReturnsResponseDto returnsIndex(ReturnsRequestDto req) {
        return computeReturns(req, false);
    }

    private ReturnsResponseDto computeReturns(ReturnsRequestDto req, boolean nps) {
        ReturnsResponseDto out = new ReturnsResponseDto();
        List<TransactionDto> withRemanent = filter(req.getQ(), req.getP(), req.getK(), req.getTransactions()).getValid();
        double totalAmount = withRemanent.stream().mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0).sum();
        double totalCeiling = withRemanent.stream().mapToDouble(t -> t.getCeiling() != null ? t.getCeiling() : 0).sum();
        out.setTransactionsTotalAmount(totalAmount);
        out.setTransactionsTotalCeiling(totalCeiling);

        List<KPeriodDto> kList = req.getK() != null ? req.getK() : List.of();
        List<SavingsByDatesDto> savings = new ArrayList<>();
        int years = yearsToRetirement(req.getAge());
        double inflation = req.getInflation() <= 0 ? 0.055 : req.getInflation();
        double annualIncome = req.getWage() * 12;

        for (KPeriodDto kp : kList) {
            SavingsByDatesDto sd = new SavingsByDatesDto();
            sd.setStart(kp.getStart());
            sd.setEnd(kp.getEnd());
            double sum = 0;
            for (TransactionDto t : withRemanent) {
                LocalDateTime txTime = DateTimeSupport.parse(t.getDate() != null ? t.getDate() : t.getTimestamp());
                if (txTime != null && DateTimeSupport.inRangeInclusive(txTime, kp.getStart(), kp.getEnd()))
                    sum += t.getRemanent() != null ? t.getRemanent() : 0;
            }
            sd.setAmount(sum);
            double rate = nps ? NPS_RATE : INDEX_RATE;
            double A = sum * Math.pow(1 + rate, years);
            double AReal = A / Math.pow(1 + inflation, years);
            double profits = AReal - sum; // real profit as in example: "profit" from return
            sd.setProfits(profits);
            if (nps) {
                double npsDeduction = Math.min(sum, Math.min(annualIncome * 0.10, NPS_MAX_DEDUCTION));
                double taxBenefit = taxBenefit(annualIncome, npsDeduction);
                sd.setTaxBenefit(taxBenefit);
            } else {
                sd.setTaxBenefit(0);
            }
            savings.add(sd);
        }
        out.setSavingsByDates(savings);
        return out;
    }

    private int yearsToRetirement(int age) {
        if (age >= 60) return 5;
        return 60 - age;
    }

    /** Tax(income) - Tax(income - NPS_Deduction) per simplified slabs. */
    private double taxBenefit(double income, double npsDeduction) {
        return taxAt(income) - taxAt(income - npsDeduction);
    }

    private double taxAt(double income) {
        if (income <= 700_000) return 0;
        if (income <= 1_000_000) return (income - 700_000) * 0.10;
        if (income <= 1_200_000) return 30_000 + (income - 1_000_000) * 0.15;
        if (income <= 1_500_000) return 60_000 + (income - 1_200_000) * 0.20;
        return 120_000 + (income - 1_500_000) * 0.30;
    }

    private static TransactionDto cloneTransaction(TransactionDto t) {
        TransactionDto c = new TransactionDto();
        copyTransaction(t, c);
        return c;
    }

    private static void copyTransaction(TransactionDto from, TransactionDto to) {
        to.setDate(from.getDate());
        to.setTimestamp(from.getTimestamp());
        to.setAmount(from.getAmount());
        to.setCeiling(from.getCeiling());
        to.setRemanent(from.getRemanent());
    }
}
