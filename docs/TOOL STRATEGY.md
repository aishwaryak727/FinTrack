# FinTrack AI Tool Strategy & Governance

This document establishes the tool strategy, actual interaction audit trail, analysis of Copilot capabilities, solutions to assessment scenarios, and genuine limitations observed during the development of FinTrack.

---

## 1. Actual Usage-Log Entries

### Entry 1: Package Structure Setup & Base Package Correction
- **Timestamp**: `2026-10-30T12:59:28+05:30`
- **Action**: Created target package structure for `transaction`, `expense`, `exception`, and `config`.
- **Finding/Correction**: Spring Initializr generated package name `com.fintrack.fintrack_api` due to the artifact ID hyphen. Relocated `FintrackApiApplication` to `com.fintrack` to enable proper component scanning across all subpackages. Added `package-info.java` placeholders so empty packages remained visible in IDEs and tracked by Git.

### Entry 2: Copilot Instructions Specification
- **Timestamp**: `2026-11-01T18:25:38+05:30`
- **Action**: Created `.github/copilot-instructions.md` containing 14 project engineering rules.
- **Finding/Correction**: Grounded instructions on `BigDecimal` mandatory usage, zero `double`/`float` tolerance, constructor injection, DTO isolation, JUnit 5/Mockito testing, and ownership validation.

### Entry 3: Transaction Module Review & Unreviewed Output Preservation
- **Timestamp**: `2026-11-01T19:13:25+05:30`
- **Action**: Inspected generated transaction code; backed up original unreviewed files into `docs/unreviewed-ai-output/`.
- **Finding/Correction**: Detected 12 distinct issues across JPA mapping, IDOR vulnerabilities, missing database indexing, mutable timestamps, and lack of logging.

### Entry 4: Transaction Remediation & Comprehensive Review Report
- **Timestamp**: `2026-11-01T19:18:24+05:30`
- **Action**: Created `docs/REVIEW.md` and remediated working Transaction source files.
- **Finding/Correction**: Added `TransactionType` enum (`INCOME`, `EXPENSE`, `TRANSFER`), composite database indexes, domain exceptions (`ResourceNotFoundException`, `UnauthorizedException`), centralized `@RestControllerAdvice` (`GlobalExceptionHandler`), and caller ownership checks (`X-User-Id`).

### Entry 5: Expense Splitting Engine Implementation
- **Timestamp**: `2026-11-01T20:58:26+05:30`
- **Action**: Implemented group expense splitting feature (`SharedExpense`, `ExpenseParticipant`, `SplitType`, `SharedExpenseRepository`, `ExpenseService`, `ExpenseServiceImpl`, `ExpenseController`, and DTOs).
- **Finding/Correction**: Implemented penny remainder distribution algorithm for equal splits ($100 divided 3 ways yielding 33.34, 33.33, 33.33) and multi-expense cumulative balance settlement engine satisfying the zero-sum invariant.

### Entry 6: Verification & Test Suite Execution
- **Timestamp**: `2026-11-01T21:05:23+05:30`
- **Action**: Created exactly 26 executable JUnit 5 and Mockito test cases matching the required 6-category distribution and executed `mvn clean test`.
- **Finding/Correction**: Resolved missing static assertion import in `NetBalancesTest`; achieved 100% build success with 26 tests executed, 26 passed, 0 failures, 0 errors.

---

## 2. Copilot / AI Features Leveraged

1. **Repository-Level Instructions (`.github/copilot-instructions.md`)**:
   Acts as persistent system-level steering. Ensures generated code adheres to fintech conventions (e.g., using `BigDecimal` instead of `double`, constructor injection, layer separation).
2. **Multi-File Workspace Context Indexing**:
   Enables Copilot to recognize symbols across modules, cross-referencing DTOs, entities, and services across `transaction` and `expense` packages without requiring redundant copy-pasting of definitions.
3. **Targeted Code Refactoring & Remediation**:
   Assists in translating architectural findings from code reviews (e.g. replacing leaked `ResponseStatusException` with domain exceptions) into concrete source adjustments.
4. **Isolated Mockito Test Synthesis**:
   Generates unit tests utilizing pure Mockito mocks (`@Mock`, `@InjectMocks`, `ArgumentCaptor`) without initializing the entire Spring application context, ensuring fast, deterministic verification.

---

## 3. Answers to the Six Assessment Scenarios

### Scenario 1: Hallucinated or Deprecated API Usage
- **Challenge**: AI models trained on historical data often suggest deprecated APIs (e.g., Spring Security's `WebSecurityConfigurerAdapter`) or hallucinate non-existent starter packages.
- **Strategy Applied**: Enforce modern Java 21 and Spring Boot 3 standards in `.github/copilot-instructions.md`. Verify every dependency against `pom.xml`. When AI attempted to import non-existent `@WebMvcTest` slices, human engineering immediately pivoted to pure Mockito unit tests, ensuring build stability.

### Scenario 2: Insecure Direct Object References (IDOR) & Security Vulnerabilities
- **Challenge**: AI default scaffolding prioritizes happy-path CRUD and frequently trusts user-provided identifiers (`request.userId()` or `@PathVariable Long userId`) without validating caller identity.
- **Strategy Applied**: Mandate ownership verification in all service contracts. Reject mutations where `authenticatedUserId != targetUserId`. Enforce repository queries that bind both the resource ID and owner ID (`findByIdAndUserId`, `deleteByIdAndUserId`), completely mitigating broken object level authorization (OWASP API1:2023).

### Scenario 3: Financial Precision & Rounding Discrepancies
- **Challenge**: AI arithmetic typically divides numbers directly without allocating remainders, causing fractional penny loss (e.g. $100 / 3 = $33.33 * 3 = $99.99), breaking financial ledger invariants.
- **Strategy Applied**: Implement integer-cent based penny allocation (`baseCents` + `remainderCents`) in `ExpenseServiceImpl`. Explicitly enforce `BigDecimal.setScale(2, RoundingMode.HALF_UP)` on all amounts and validate that the sum of all participant shares matches `totalAmount` down to the exact cent, ensuring the sum of all net balances across the platform strictly equals zero.

### Scenario 4: Managing Context Window Limitations on Multi-Module Systems
- **Challenge**: Passing an entire enterprise application in a single prompt degrades prompt attention, leading to truncated files or ignored instructions.
- **Strategy Applied**: Decompose work into discrete, sequential modules: (1) Package scaffolding, (2) Instruction governance, (3) Transaction audit & remediation, (4) Expense splitting implementation, (5) Test suite generation. Preserve artifacts at each milestone (`docs/unreviewed-ai-output/`, `docs/REVIEW.md`).

### Scenario 5: Test Quality & Edge-Case Coverage Assurance
- **Challenge**: AI-generated tests skew heavily toward happy-path tests, neglecting negative validations, authorization boundaries, and edge cases.
- **Strategy Applied**: Impose strict category quotas (e.g., Equal split: 4, Custom split: 4, Validation: 5, Net balances: 5, Authorization: 4, Transaction: 4). Require explicit assertions for zero/negative amounts, duplicate participants, solo participants, unauthorized callers, and multi-expense settlement chains.

### Scenario 6: Human-in-the-Loop Governance & AI Pair Programming Boundaries
- **Challenge**: Over-reliance on AI output without human verification risks introducing silent bugs, unindexed database tables, and performance bottlenecks.
- **Strategy Applied**: Establish a clear division of responsibility: AI is utilized for rapid scaffolding, syntax typing, and boilerplate generation; Human engineers maintain absolute authority over security architecture, database indexing, financial invariants, and build verification.

---

## 4. Three Genuine Limitations of AI Code Generation

1. **Blind Trust in Client-Supplied Identifiers (Lack of Threat Modeling)**:
   AI tools inherently optimize for concise code that passes basic unit tests. They will generate controllers that take `userId` from request bodies and pass it directly to `repository.deleteByUserId(userId)` without questioning whether the caller has the right to delete those records. Threat modeling must be provided by human engineers.
2. **Naive Floating-Point & Fractional Division Arithmetic**:
   AI does not intuitively understand financial double-entry bookkeeping rules. When asked to divide $100 among 3 people, it defaults to standard division with rounding, dropping the 1-cent remainder and creating un-reconciled ledger books unless explicitly guided by domain algorithms.
3. **Build Environment & Classpath Unawareness**:
   AI suggests annotations and imports based on generic web patterns (such as importing `@WebMvcTest` or `ObjectMapper`) without knowing whether those specific libraries exist in the active `pom.xml`. This creates compilation failures that require manual dependency tree analysis and test refactoring.

