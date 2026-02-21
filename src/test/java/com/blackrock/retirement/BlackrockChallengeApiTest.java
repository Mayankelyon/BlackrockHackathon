package com.blackrock.retirement;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/*
 * Test type: Integration / API tests (REST endpoints)
 * Validation: Request/response shapes, status codes, and business logic (parse ceiling/remanent, filter q/p/k, returns NPS/index)
 * Command: mvn test -Dtest=BlackrockChallengeApiTest
 */
import com.blackrock.retirement.dto.ExpenseInput;
import com.blackrock.retirement.dto.FilterRequestDto;
import com.blackrock.retirement.dto.KPeriodDto;
import com.blackrock.retirement.dto.PPeriodDto;
import com.blackrock.retirement.dto.QPeriodDto;
import com.blackrock.retirement.dto.ReturnsRequestDto;
import com.blackrock.retirement.dto.TransactionDto;
import com.blackrock.retirement.dto.ValidatorRequestDto;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class BlackrockChallengeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /transactions:parse - ceiling and remanent")
    void parseExpenses() throws Exception {
        List<ExpenseInput> body = List.of(
                expense("2023-10-12 20:15", 250),
                expense("2023-02-28 15:49", 375),
                expense("2023-07-01 21:59", 620),
                expense("2023-12-17 08:09", 480)
        );
        mockMvc.perform(post("/blackrock/challenge/v1/transactions:parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].amount").value(250))
                .andExpect(jsonPath("$[0].ceiling").value(300))
                .andExpect(jsonPath("$[0].remanent").value(50))
                .andExpect(jsonPath("$[1].ceiling").value(400))
                .andExpect(jsonPath("$[1].remanent").value(25))
                .andExpect(jsonPath("$[2].ceiling").value(700))
                .andExpect(jsonPath("$[2].remanent").value(80))
                .andExpect(jsonPath("$[3].ceiling").value(500))
                .andExpect(jsonPath("$[3].remanent").value(20));
    }

    @Test
    @DisplayName("POST /transactions:filter - q and p periods, k sums")
    void filterWithQPK() throws Exception {
        List<TransactionDto> transactions = List.of(
                tx("2023-10-12 20:15:00", 250, 300, 50),
                tx("2023-02-28 15:49:00", 375, 400, 25),
                tx("2023-07-01 21:59:00", 620, 700, 80),
                tx("2023-12-17 08:09:00", 480, 500, 20)
        );
        FilterRequestDto body = new FilterRequestDto();
        body.setQ(List.of(qPeriod(0, "2023-07-01 00:00", "2023-07-31 23:59")));
        body.setP(List.of(pPeriod(25, "2023-10-01 08:00", "2023-12-31 19:59")));
        body.setK(List.of(
                kPeriod("2023-03-01 00:00", "2023-11-30 23:59"),
                kPeriod("2023-01-01 00:00", "2023-12-31 23:59")
        ));
        body.setTransactions(transactions);

        ResultActions ra = mockMvc.perform(post("/blackrock/challenge/v1/transactions:filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
        ra.andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", hasSize(4)));
        // After q: July expense remanent = 0. After p: Oct+Dec +25 each -> 75 and 45
        ra.andExpect(jsonPath("$.valid[2].remanent").value(0.0))
                .andExpect(jsonPath("$.valid[0].remanent").value(75.0))
                .andExpect(jsonPath("$.valid[3].remanent").value(45.0));
    }

    @Test
    @DisplayName("POST /returns:nps - totals and savingsByDates with profits and taxBenefit")
    void returnsNps() throws Exception {
        List<TransactionDto> transactions = List.of(
                tx("2023-10-12 20:15:00", 250, 300, 50),
                tx("2023-02-28 15:49:00", 375, 400, 25),
                tx("2023-07-01 21:59:00", 620, 700, 80),
                tx("2023-12-17 08:09:00", 480, 500, 20)
        );
        ReturnsRequestDto body = new ReturnsRequestDto();
        body.setAge(29);
        body.setWage(50_000);
        body.setInflation(0.055);
        body.setQ(List.of(qPeriod(0, "2023-07-01 00:00", "2023-07-31 23:59")));
        body.setP(List.of(pPeriod(25, "2023-10-01 08:00", "2023-12-31 19:59")));
        body.setK(List.of(
                kPeriod("2023-03-01 00:00", "2023-11-30 23:59"),
                kPeriod("2023-01-01 00:00", "2023-12-31 23:59")
        ));
        body.setTransactions(transactions);

        mockMvc.perform(post("/blackrock/challenge/v1/returns:nps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionsTotalAmount").isNumber())
                .andExpect(jsonPath("$.transactionsTotalCeiling").isNumber())
                .andExpect(jsonPath("$.savingsByDates", hasSize(2)))
                .andExpect(jsonPath("$.savingsByDates[0].amount").value(75.0))
                .andExpect(jsonPath("$.savingsByDates[1].amount").value(145.0))
                .andExpect(jsonPath("$.savingsByDates[0].profits").isNumber())
                .andExpect(jsonPath("$.savingsByDates[1].taxBenefit").value(0.0));
    }

    @Test
    @DisplayName("POST /returns:index - taxBenefit is 0")
    void returnsIndex() throws Exception {
        ReturnsRequestDto body = new ReturnsRequestDto();
        body.setAge(29);
        body.setWage(50_000);
        body.setInflation(0.055);
        body.setTransactions(List.of(tx("2023-01-15 12:00:00", 100, 200, 100)));
        body.setK(List.of(kPeriod("2023-01-01 00:00", "2023-12-31 23:59")));

        mockMvc.perform(post("/blackrock/challenge/v1/returns:index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingsByDates[0].taxBenefit").value(0.0))
                .andExpect(jsonPath("$.savingsByDates[0].profits").isNumber());
    }

    @Test
    @DisplayName("POST /transactions:validator - valid, invalid, duplicate")
    void validator() throws Exception {
        ValidatorRequestDto body = new ValidatorRequestDto();
        body.setWage(50_000);
        body.setTransactions(List.of(
                tx("2023-01-01 10:00:00", 100, 200, 100),
                tx("2023-01-01 10:00:00", 150, 200, 50),  // duplicate date
                tx("2023-01-02 10:00:00", 50, 100, 50)   // valid
        ));
        mockMvc.perform(post("/blackrock/challenge/v1/transactions:validator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate", hasSize(1)))
                .andExpect(jsonPath("$.valid", hasSize(2)));
    }

    @Test
    @DisplayName("GET /performance - time, memory, threads")
    void performance() throws Exception {
        mockMvc.perform(get("/blackrock/challenge/v1/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").isString())
                .andExpect(jsonPath("$.memory").isString())
                .andExpect(jsonPath("$.threads").isNumber());
    }

    private static ExpenseInput expense(String timestamp, double amount) {
        ExpenseInput e = new ExpenseInput();
        e.setTimestamp(timestamp);
        e.setAmount(amount);
        return e;
    }

    private static TransactionDto tx(String date, double amount, double ceiling, double remanent) {
        TransactionDto t = new TransactionDto();
        t.setDate(date);
        t.setTimestamp(date);
        t.setAmount(amount);
        t.setCeiling(ceiling);
        t.setRemanent(remanent);
        return t;
    }

    private static QPeriodDto qPeriod(double fixed, String start, String end) {
        QPeriodDto q = new QPeriodDto();
        q.setFixed(fixed);
        q.setStart(start);
        q.setEnd(end);
        return q;
    }

    private static PPeriodDto pPeriod(double extra, String start, String end) {
        PPeriodDto p = new PPeriodDto();
        p.setExtra(extra);
        p.setStart(start);
        p.setEnd(end);
        return p;
    }

    private static KPeriodDto kPeriod(String start, String end) {
        KPeriodDto k = new KPeriodDto();
        k.setStart(start);
        k.setEnd(end);
        return k;
    }
}
