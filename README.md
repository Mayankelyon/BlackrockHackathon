# BlackRock Challenge: Self-Saving for Retirement

APIs for automated retirement savings via expense-based micro-investments: round expenses to the next 100, apply time-based rules (q, p, k), and compute NPS and Index Fund returns with inflation and tax benefits.

---

## Table of Contents

- [Setup](#setup)
- [Run the Application](#run-the-application)
- [Testing](#testing)
- [API Reference](#api-reference)
- [Example Flow](#example-flow)
- [Repository and Submission](#repository-and-submission)

---

## Setup

### Prerequisites

Install the following on your machine:

| Tool    | Version   | Purpose                    |
|---------|-----------|----------------------------|
| **JDK** | 17 or higher | Run and build the application |
| **Maven** | 3.6+   | Build, test, and package   |
| **Docker** | (optional) | Run in a container        |

**Check installations:**

```bash
java -version    # Should show version 17 or higher
mvn -v           # Should show Maven 3.6+
docker --version # Optional, for containerized run
```

### Step 1: Get the code

Clone or download the repository and go into the project folder:

```bash
git clone <repository-url>
cd challange
```

*(If you received a ZIP, extract it and `cd` into the extracted folder.)*

### Step 2: Build the project

From the project root (where `pom.xml` is):

```bash
mvn clean package -DskipTests
```

To run tests as part of the build:

```bash
mvn clean package
```

On success, the JAR is created at: `target/retirement-savings-api-0.0.1-SNAPSHOT.jar`.

### Step 3: (Optional) Change configuration

Default settings:

- **Port:** `5477`
- **Config file:** `src/main/resources/application.properties`

To change the port, edit `application.properties`:

```properties
server.port=5477
```

No database or other external services are required.

---

## Run the Application

### Option A: Run with Maven (recommended for development)

```bash
mvn spring-boot:run
```

The API will be available at **http://localhost:5477**.

### Option B: Run the JAR

After building (see [Setup](#setup)):

```bash
java -jar target/retirement-savings-api-0.0.1-SNAPSHOT.jar
```

Again, the API is at **http://localhost:5477**.

### Option C: Run with Docker

1. Build the JAR (if not already done):

   ```bash
   mvn clean package -DskipTests
   ```

2. Build the Docker image (replace `name-lastname` with your details):

   ```bash
   docker build -t blk-hacking-ind-name-lastname .
   ```

3. Run the container:

   ```bash
   docker run -d -p 5477:5477 blk-hacking-ind-name-lastname
   ```

   Port mapping: host `5477` → container `5477`.

### Option D: Run with Docker Compose

From the project root:

```bash
docker compose up -d
```

This builds the image (if needed) and starts the app on port **5477**. Stop with:

```bash
docker compose down
```

---

## Testing

Tests live under **`src/test`**. The main API tests are in `BlackrockChallengeApiTest`.

**Run only the API integration tests:**

```bash
mvn test -Dtest=BlackrockChallengeApiTest
```

**Run all tests:**

```bash
mvn test
```

**Test type:** Integration / API tests (REST).  
**Validation:** Request/response shape, status codes, parse (ceiling/remanent), filter (q/p/k), returns (NPS/index), validator (valid/invalid/duplicate), performance endpoint.

**Quick health check (when the app is running):**

```bash
curl http://localhost:5477/blackrock/challenge/v1/performance
```

You should get JSON with `time`, `memory`, and `threads`.

---

## API Reference

**Base URL:** `http://localhost:5477/blackrock/challenge/v1`

All request/response bodies are **JSON**. Timestamps use format: `YYYY-MM-DD HH:mm:ss` (seconds optional in requests).

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/transactions:parse`     | List of expenses → list of transactions (ceiling, remanent) |
| POST   | `/transactions:validator` | Validate transactions; returns valid, invalid, duplicate |
| POST   | `/transactions:filter`    | Apply q/p periods; returns valid and invalid transactions |
| POST   | `/returns:nps`            | NPS returns by k periods (profits, tax benefit, inflation-adjusted) |
| POST   | `/returns:index`         | Index fund returns by k periods (taxBenefit = 0) |
| GET    | `/performance`            | System metrics: time, memory, threads |

---

### 1. Transaction Builder — `POST /transactions:parse`

**Request body:** A **list** of expenses (array).

```json
[
  { "timestamp": "2023-10-12 20:15:00", "amount": 250 },
  { "timestamp": "2023-02-28 15:49:00", "amount": 375 }
]
```

**Response:** List of transactions, each with:

- `date` — normalized datetime  
- `amount` — original amount  
- `ceiling` — next multiple of 100  
- `remanent` — ceiling − amount  

---

### 2. Transaction Validator — `POST /transactions:validator`

**Request body:**

```json
{
  "wage": 50000,
  "transactions": [
    { "date": "2023-01-01 10:00:00", "amount": 100, "ceiling": 200, "remanent": 100 }
  ]
}
```

Optional: `maxAmountToInvest` (default 500000).

**Response:** `valid`, `invalid` (each with `message`), `duplicate` (by transaction date).

---

### 3. Temporal Constraints — `POST /transactions:filter`

**Request body:**

```json
{
  "q": [{ "fixed": 0, "start": "2023-07-01 00:00", "end": "2023-07-31 23:59" }],
  "p": [{ "extra": 25, "start": "2023-10-01 08:00", "end": "2023-12-31 19:59" }],
  "k": [{ "start": "2023-01-01 00:00", "end": "2023-12-31 23:59" }],
  "transactions": [
    { "timestamp": "2023-10-12 20:15:00", "amount": 250, "ceiling": 300, "remanent": 50 }
  ]
}
```

**Response:** `valid` (remanent updated by q then p), `invalid` (e.g. bad date format).

---

### 4. Returns — `POST /returns:nps` and `POST /returns:index`

**Request body:**

```json
{
  "age": 29,
  "wage": 50000,
  "inflation": 0.055,
  "q": [],
  "p": [],
  "k": [{ "start": "2023-01-01 00:00", "end": "2023-12-31 23:59" }],
  "transactions": []
}
```

**Response:**  
`transactionsTotalAmount`, `transactionsTotalCeiling`, and `savingsByDates` (one entry per k period), each with:

- `start`, `end`, `amount`, `profits`, `taxBenefit` (0 for index)

NPS: 7.11% rate, tax benefit per Indian slabs. Index: 14.49%, no tax benefit. Years to retirement: 60 − age (or 5 if age ≥ 60).

---

### 5. Performance — `GET /performance`

**Request:** None.

**Response:** `time` (uptime HH:mm:ss.SSS), `memory` (e.g. "X.XX MB"), `threads` (integer).

---

## Example Flow

From the challenge spec:

1. **Parse** 4 expenses → e.g. 250→300/50, 375→400/25, 620→700/80, 480→500/20.  
2. **q period** (July, fixed 0) → July expense remanent = 0.  
3. **p period** (Oct–Dec, extra 25) → Oct remanent 75, Dec remanent 45.  
4. **k periods** → Mar–Nov sum = 75; full year sum = 145.  
5. **Returns** (amount 145, age 29) → NPS real value ≈ 231.9, index ≈ 1829.5; NPS tax benefit 0 for income 6,00,000.

---

## Repository and Submission

- Keep all source code, **Dockerfile**, **compose.yaml**, and this **README** in the default branch.  
- Repository must be **public** and accessible without extra permissions.  
- Do **not** change the repository after the challenge deadline.

---

