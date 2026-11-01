# FinTrack System Architecture

FinTrack is engineered as an enterprise-grade financial management platform following a strict multi-tiered layered architecture.
Incoming HTTP client requests hit the Presentation Layer, where REST controllers (`TransactionController`, `ExpenseController`) enforce Jakarta Bean Validation constraints and extract caller context.
Valid requests are converted from immutable record DTOs and passed to the Business Logic Service Layer (`TransactionServiceImpl`, `ExpenseServiceImpl`).
The service layer encapsulates financial invariants, caller ownership authorization, `BigDecimal` scale normalization, and penny-remainder allocation algorithms.
Data persistence is delegated to the Data Access Layer using Spring Data JPA Repositories (`TransactionRepository`, `SharedExpenseRepository`).
Repositories execute type-safe queries and optimized batch fetches against an in-memory H2 relational database configured with composite index tuning.
Domain entities (`Transaction`, `SharedExpense`, `ExpenseParticipant`) maintain strict encapsulation, audit timestamps, and lifecycle pre-persist hooks.
Cross-cutting concerns are decoupled using a centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) that translates domain exceptions into standard HTTP error envelopes.
Structured audit logging via SLF4J is embedded across all mutation operations to ensure financial traceability and compliance.
The entire architecture guarantees that balance settlements across all multi-party expenses maintain a strict zero-sum invariant.

```mermaid
flowchart TD
    Client(["HTTP Client / API Consumer"])

    subgraph PresentationLayer["Presentation Layer (REST API)"]
        TC["TransactionController\n(/api/transactions)"]
        EC["ExpenseController\n(/api/expenses)"]
        GEH["GlobalExceptionHandler\n(@RestControllerAdvice)"]
    end

    subgraph DTOLayer["Data Transfer Objects (Java Records)"]
        ReqDTO["Request DTOs\n- CreateTransactionRequest\n- CreateExpenseRequest\n- ParticipantShareRequest"]
        ResDTO["Response DTOs\n- TransactionResponse\n- SharedExpenseResponse\n- ErrorResponse"]
    end

    subgraph ServiceLayer["Service Layer (Business Logic & Safeguards)"]
        TS["TransactionService / Impl\n- Ownership Verification\n- Scale Normalization\n- Audit Logging"]
        ES["ExpenseService / Impl\n- Split Calculations (EQUAL / CUSTOM)\n- Penny Allocation\n- Net Balances Engine"]
    end

    subgraph DataAccessLayer["Data Access Layer (Spring Data JPA)"]
        TR["TransactionRepository\n(findByUserId, deleteByIdAndUserId)"]
        SR["SharedExpenseRepository\n(findAllWithParticipants, findByCreatorId)"]
    end

    subgraph PersistenceLayer["Database & Persistence (H2 In-Memory)"]
        T_TABLE[("transactions Table\nidx_tx_user_created")]
        SE_TABLE[("shared_expenses Table\nidx_shared_exp_creator")]
        EP_TABLE[("expense_participants Table\nidx_exp_part_user")]
    end

    Client -->|JSON Request + X-User-Id| TC
    Client -->|JSON Request| EC
    TC -.->|Validation / Errors| GEH
    EC -.->|Validation / Errors| GEH
    GEH -.->|ErrorResponse 4xx/5xx| Client

    TC -->|Validated DTO| TS
    EC -->|Validated DTO| ES
    ReqDTO -.-> TC
    ReqDTO -.-> EC

    TS -->|Entity Queries & Mutates| TR
    ES -->|Eager Fetch & Cascade Save| SR

    TS -.-> ResDTO
    ES -.-> ResDTO
    ResDTO -.-> Client

    TR -->|JPA / SQL| T_TABLE
    SR -->|Cascade ALL| SE_TABLE
    SE_TABLE -->|1:N Join| EP_TABLE
```

