# FinTrack - Personal Finance & Group Expense Management API

FinTrack is an enterprise-grade financial tracking and multi-party expense splitting RESTful API built for high reliability, strict monetary precision, and robust authorization.

---

## Technology Stack

FinTrack is built with the following technologies and frameworks:

- **Java**: `Java 21` (LTS)
- **Framework**: `Spring Boot` (Web MVC, Validation, Security)
- **Build & Dependency Management**: `Maven` (via Maven Wrapper `mvnw`)
- **Database**: `H2 Database` (In-Memory with JPA indexing and H2 console enabled)
- **ORM / Persistence**: `Spring Data JPA` (Hibernate 7.x core)
- **Unit & Integration Testing**: `JUnit 5` (Jupiter) and `Mockito` (5.x)

---

## Key Features

### 1. Financial Transaction Management (`/api/transactions`)
- **Proper JPA Entity Mapping**: Indexed on `(user_id, created_at DESC)` and `(user_id)` for high-throughput customer statement queries.
- **Strict Monetary Precision**: Mandatory `BigDecimal` calculation and storage; all values normalized to 2 decimal places with `RoundingMode.HALF_UP`.
- **User Ownership & IDOR Protection**: Endpoints enforce caller identity verification via `X-User-Id` (or security context), preventing users from reading or deleting records belonging to other users.
- **Categorization**: Strongly-typed `TransactionType` enum (`INCOME`, `EXPENSE`, `TRANSFER`).
- **Resilience**: Immutable `createdAt` audit timestamps safeguarded with `@PrePersist` and `@Column(updatable = false)`.

### 2. Group Expense Splitting Engine (`/api/expenses`)
- **Flexible Split Strategies**: Full support for both `EQUAL` and `CUSTOM` split types.
- **Penny-Remainder Allocation**: Guarantees that uneven equal divisions (e.g. $100.00 split 3 ways $\rightarrow$ $33.34, $33.33, $33.33) allocate remainder pennies deterministically so the sum of participant shares strictly equals the total amount.
- **Validation**: Rejects solo expenses (< 2 participants), duplicate participants, negative or zero amounts, and mismatched custom shares.
- **Cumulative Net Balances (`GET /api/expenses/balances`)**:
  - Aggregates paid amounts and owed shares across all group expenses.
  - Positive balance ($> 0$) indicates money the user should receive.
  - Negative balance ($< 0$) indicates money the user owes.
  - **Zero-Sum Invariant**: The sum of all net balances across all platform users strictly equals `0.00`.

### 3. Architecture, Error Handling & Observability
- **Clean Layered Decoupling**: Strict separation between Controller $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Entity.
- **Standardized Error Envelopes**: `@RestControllerAdvice` (`GlobalExceptionHandler`) intercepts domain exceptions (`ResourceNotFoundException`, `UnauthorizedException`, `InvalidExpenseException`, `InvalidTransactionException`) and validation errors, returning structured `ErrorResponse` payloads.
- **Structured Logging**: Parameterized SLF4J audit logging across all critical financial actions.

---

## API Documentation

### Transactions

| Method | Endpoint | Description | Headers / Query |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/transactions` | Record a new financial transaction | `X-User-Id: <id>` (optional caller ID) |
| `GET` | `/api/transactions/{id}` | Retrieve a transaction by ID (requires ownership) | `X-User-Id: <id>` |
| `GET` | `/api/transactions/user/{userId}` | Retrieve all transactions for a user | `X-User-Id: <id>` |
| `DELETE` | `/api/transactions/{id}` | Delete a specific transaction (requires ownership) | `X-User-Id: <id>` |
| `DELETE` | `/api/transactions/user/{userId}` | Delete all transactions for a user | `X-User-Id: <id>` |

### Group Expenses & Net Balances

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/expenses` | Create and divide a shared group expense (`EQUAL` or `CUSTOM`) |
| `GET` | `/api/expenses/balances` | Calculate cumulative net balances across all recorded expenses |
| `GET` | `/api/expenses/{id}` | Retrieve details of a specific shared expense |
| `GET` | `/api/expenses` | Retrieve all recorded shared expenses |

---

## Getting Started

### Prerequisites
- JDK 21 or higher installed (`java -version`)
- Git

### Build and Test
Run the test suite using the embedded Maven wrapper:

```bash
# On Windows PowerShell
cd fintrack-api
.\mvnw.cmd clean test

# On Linux / macOS
cd fintrack-api
./mvnw clean test
```

### Running the Application Locally
```bash
# On Windows PowerShell
.\mvnw.cmd spring-boot:run

# On Linux / macOS
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.
The in-memory H2 database console is accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:fintrackdb`, User: `sa`, Password: *(empty)*).

---

## Test Suite Summary

The application includes exactly **26 executable JUnit 5 and Mockito test cases**:

- **Equal Split Tests (4)**: Equal division among 3 participants, penny remainder distribution, even split, 4-participant division with remainder.
- **Custom Split Tests (4)**: Exact sum match, under-allocated rejection, over-allocated rejection, arbitrary 2-decimal fractional shares.
- **Validation Tests (5)**: Single participant rejection, duplicate participant rejection, zero total rejection, negative total rejection, invalid custom shares rejection.
- **Net Balances Tests (5)**: Single expense balance (+80, -40, -40), multi-expense payback chain, creator not in participants, zero-sum platform invariant, empty ledger handling.
- **Authorization Tests (4)**: Unauthorized creation rejection, unauthorized retrieval rejection, unauthorized single deletion rejection, unauthorized bulk deletion rejection.
- **Transaction Module Tests (4)**: Creation with scale normalization, retrieval by ID, retrieval by user ID, deletion by ID.

**Test Results**: `26 executed, 26 passed, 0 failed, 0 errors`.

---

## Assessment Documentation Directory

- [`docs/ARCHITECTURE.md`](file:///D:/Fin/docs/ARCHITECTURE.md): Architectural overview and Mermaid component diagram.
- [`docs/REVIEW.md`](file:///D:/Fin/docs/REVIEW.md): Detailed code audit, fintech impact assessment, and remediation summary.
- [`docs/PROMPTS.md`](file:///D:/Fin/docs/PROMPTS.md): Exact prompt sequence, Copilot features, prompting techniques, and post-generation corrections.
- [`docs/PR_DESCRIPTION.md`](file:///D:/Fin/docs/PR_DESCRIPTION.md): Pull request documentation, AI tool disclosure, risk analysis, and simulated peer reviews.
- [`docs/TOOL STRATEGY.md`](file:///D:/Fin/docs/TOOL%20STRATEGY.md): AI governance log, assessment scenario solutions, and genuine limitations.
- [`.github/copilot-instructions.md`](file:///D:/Fin/.github/copilot-instructions.md): Repository instructions file steering AI code generation.

