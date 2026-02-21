# Requirements Verification Checklist

## API Endpoints (all under `/blackrock/challenge/v1`)

| # | Requirement | Endpoint | Status |
|---|-------------|----------|--------|
| 1 | Transaction Builder | `POST /transactions:parse` | ✅ |
| 2 | Transaction Validator | `POST /transactions:validator` | ✅ |
| 3 | Temporal Constraints Validator | `POST /transactions:filter` | ✅ |
| 4 | Returns NPS | `POST /returns:nps` | ✅ |
| 5 | Returns Index | `POST /returns:index` | ✅ |
| 6 | Performance Report | `GET /performance` | ✅ |

## 1. Transaction Builder (`/transactions:parse`)

- **Input:** List of expenses; each with `timestamp` (datetime), `amount` (double).  
  Body: `{ "expenses": [ { "timestamp": "YYYY-MM-DD HH:mm:ss", "amount": number }, ... ] }`
- **Output:** List of transactions with `date`, `amount`, `ceiling`, `remanent`.  
  Ceiling = next multiple of 100; remanent = ceiling − amount.
- **Format:** Timestamp normalized to `YYYY-MM-DD HH:mm:ss`.

## 2. Transaction Validator (`/transactions:validator`)

- **Input:** `wage` (double), `transactions` (list with `date`, `amount`, `ceiling`, `remanent`).  
  Optional: `maxAmountToInvest` (default 500,000).
- **Output:** `valid`, `invalid` (each with `message`), `duplicate`.
- **Rules:** Ceiling/remanent consistency, ceiling multiple of 100, remanent ≤ max (and NPS cap: min(10% annual, ₹2,00,000)); duplicates by date.

## 3. Temporal Constraints (`/transactions:filter`)

- **Input:** `q` (fixed, start, end), `p` (extra, start, end), `k` (start, end), `transactions` (timestamp, amount, ceiling, remanent).
- **Output:** `valid` (remanent updated by q then p), `invalid` (e.g. bad date).
- **q:** Replace remanent with `fixed` when date in [start,end]; if multiple q match, use the one that **starts latest** (same start ⇒ first in list).
- **p:** Add `extra` to remanent when date in [start,end]; **sum all** matching p.

## 4. Returns (`/returns:nps`, `/returns:index`)

- **Input:** `age`, `wage`, `inflation`, `q`, `p`, `k`, `transactions`.
- **Output:** `transactionsTotalAmount`, `transactionsTotalCeiling`, `savingsByDates` (same length as `k`): each with `start`, `end`, `amount`, `profits`, `taxBenefit` (0 for index).
- **Processing:** Apply q/p to get effective remanent; for each k, sum remanent in [start,end]; compound interest (NPS 7.11%, Index 14.49%); years = 60 − age (or 5 if age ≥ 60); inflation adjustment; NPS tax benefit via simplified slabs (0%–30%).

## 5. Performance (`/performance`)

- **Input:** None.
- **Output:** `time` (HH:mm:ss.SSS uptime), `memory` (X.XX MB), `threads` (integer).

## Deployment

- **Port:** App listens on **5477**; `server.port=5477` in `application.properties`.
- **Docker:**  
  - First line of Dockerfile: build command comment.  
  - Image name: `blk-hacking-ind-{name-lastname}`.  
  - `EXPOSE 5477`; run with `-p 5477:5477`.  
  - Base: Linux (eclipse-temurin:17-jre-alpine).
- **Compose:** `compose.yaml` with service on port 5477.

## Testing

- Tests under **`src/test`** (Maven layout).
- In test class: **comment** with (1) Test type, (2) Validation, (3) Command:  
  `mvn test -Dtest=BlackrockChallengeApiTest`

## README

- Instructions to configure, build, run (local + Docker), and run tests; API summary and examples.

---

**Summary:** Implementation matches the stated API contract, processing order (ceiling/remanent → q → p → k → returns), formulas (compound interest, inflation, tax slabs), and deployment/test/README requirements. Replace `name-lastname` in the image name before submission.
