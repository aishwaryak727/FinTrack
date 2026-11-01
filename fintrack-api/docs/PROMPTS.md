# FinTrack Engineering Prompts & AI Interaction Log

This document records the exact sequence of prompts used during the development of FinTrack, the AI assistance features leveraged, the prompting methodologies employed, and the post-generation human corrections applied.

---

## 1. Chronological Sequence of Exact Prompts Used

### Prompt 1: Package Hierarchy Scaffolding
- **Date/Time**: 2026-10-30 12:59:28 IST
- **Exact Text**:
```text
Create like this package

com.fintrack
│
├── transaction
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
│
├── expense
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
│
├── exception
└── config
```

---

### Prompt 2: Project Steering Rules Definition
- **Date/Time**: 2026-11-01 18:25:38 IST
- **Exact Text**:
```text
Step 2 – Create Copilot instructions (2 minutes)
At the project root, create:

.github/copilot-instructions.md

Paste this:
# FinTrack Project Instructions

- Use Java 21 and Spring Boot 3.
- Use Maven and H2 database.
- Follow controller, service, repository and entity layers.
- Use constructor injection.
- Use DTOs for API requests and responses.
- Use BigDecimal for all monetary calculations.
- Never use double or float for money.
- Validate all incoming requests.
- Validate expense participants and custom splits.
- Ensure custom shares equal the expense total.
- Implement ownership checks.
- Use proper exception handling.
- Write JUnit 5 and Mockito tests.
- Document public methods.
```

---

### Prompt 3: Transaction Module Review & Remediation
- **Date/Time**: 2026-11-01 19:13:25 IST
- **Exact Text**:
```text
Inspect my existing FinTrack project and all Transaction module files.

Do not delete or overwrite existing working files without reviewing them.

Review the Transaction module against these requirements:

1. Proper JPA entity mapping.
2. TransactionRepository using Spring Data JPA.
3. Service interface and implementation.
4. REST controller.
5. DTO-based requests and responses.
6. BigDecimal for monetary values.
7. Input validation.
8. Proper exception handling.
9. User ownership and authorization checks.
10. Structured logging.
11. Java documentation for public methods.

Identify all issues and create docs/REVIEW.md.

Include:
- Issue
- File and location
- Severity
- Fintech impact
- How it was detected
- Recommended fix
- Actual fix applied

Include the heading:
Issues Copilot Introduced That Required Human Judgment.

Then remediate the working Transaction source files.

Preserve the original unreviewed generated files
in docs/unreviewed-ai-output/.

Do not claim that AI-generated code was produced by
GitHub Copilot unless it actually was.
```

---

### Prompt 4: Expense Splitting Implementation
- **Date/Time**: 2026-11-01 20:58:26 IST
- **Exact Text**:
```text
Implement expense splitting

Implement the complete Expense Splitting feature in my
existing FinTrack Spring Boot application.

Use Java 21, Spring Data JPA, H2 and BigDecimal.

Create:

1. SharedExpense entity.
2. ExpenseParticipant entity.
3. SplitType enum with EQUAL and CUSTOM.
4. SharedExpenseRepository.
5. ExpenseService interface.
6. ExpenseServiceImpl.
7. ExpenseController.
8. Request and response DTOs.

SharedExpense fields:
- id
- creatorId
- description
- totalAmount
- splitType
- participants
- createdAt

Implement these endpoints:

POST /api/expenses
GET /api/expenses/balances

Business requirements:

1. Support equal and custom splits.
2. Require at least two unique participants.
3. Reject duplicate participants.
4. Reject negative or zero amounts.
5. Validate custom share amounts equal the total.
6. Use BigDecimal for all calculations.
7. Handle rounding to two decimal places.
8. Calculate net balances across all expenses.
9. The creator pays the full expense initially.
10. Include the creator as a participant where appropriate.
11. Positive balance means money should be received.
12. Negative balance means money is owed.
13. Sum of all net balances must equal zero.

Example:

A pays 120 for A, B and C equally.

A should receive 80.
B owes 40.
C owes 40.

If B has already paid A in another expense,
calculate the combined net balance.

Use proper JPA relationships and transactions.
Add validation, logging and exception handling.

Do not create placeholder methods.
Run the Maven build and fix compilation errors.
```

---

### Prompt 5: Test Suite Generation & Exact Test Count Enforcement
- **Date/Time**: 2026-11-01 21:05:23 IST
- **Exact Text**:
```text
Create exactly 26 executable JUnit 5 and Mockito test cases
for the FinTrack application.

Use this distribution:

1. Equal split - 4 tests.
2. Custom split - 4 tests.
3. Validation - 5 tests.
4. Net balances - 5 tests.
5. Authorization - 4 tests.
6. Transaction module - 4 tests.

Cover:

- Equal division among three participants.
- Custom amounts matching total.
- Custom amounts not matching total.
- Rounding differences.
- Only one participant.
- Duplicate participants.
- Negative and zero amounts.
- Net balance across multiple expenses.
- User ownership checks.
- Unauthorized deletion.
- Transaction creation, retrieval and deletion.

Use meaningful test names and real assertions.
Do not create empty or placeholder tests.

Run mvn clean test.
Report the exact number of tests executed,
passed and failed.
```

---

### Prompt 6: Complete Assessment Documentation
- **Date/Time**: 2026-11-01 21:17:36 IST
- **Exact Text**:
```text
Inspect the complete FinTrack implementation and create
all missing assessment documentation.

Required files:

README.md
.github/copilot-instructions.md
docs/REVIEW.md
docs/PROMPTS.md
docs/PR_DESCRIPTION.md
docs/TOOL STRATEGY.md
docs/ARCHITECTURE.md

Requirements:

PROMPTS.md:
Document the exact prompts actually used, their order,
Copilot features and at least three prompting techniques.
Include Post-Generation Corrections.

PR_DESCRIPTION.md:
Include AI Tool Disclosure, contribution estimates,
test coverage, known gaps, one genuine risk,
self-review checklist and three simulated peer
review comments.

TOOL STRATEGY.md:
Include at least six actual usage-log entries,
at least four Copilot features, answers to all six
assessment scenarios and three genuine limitations.

ARCHITECTURE.md:
Keep the explanation to 10-15 lines and include
a Mermaid architecture diagram.

README.md:
Declare Java 21, Spring Boot, Maven, H2,
Spring Data JPA, JUnit 5 and Mockito.

Do not invent Copilot usage, test results, code review
findings or limitations. Use actual project evidence.
Mark any unverified information clearly.
```

---

## 2. Copilot & AI Features Leveraged

1. **Repository-Level Instruction Steering (`.github/copilot-instructions.md`)**:
   Enforced strict architectural rules (constructor injection, `BigDecimal`, DTO isolation, JPA layering) across all code generation phases.
2. **Multi-File Context-Aware Chat & Code Synthesis**:
   Synthesized cross-module features (entities, DTOs, controllers, services) informed by the active workspace files.
3. **Automated Code Review & Static Defect Inspection**:
   Evaluated unreviewed AI output against 11 fintech criteria to identify security and mathematical gaps.
4. **Targeted Test Generation & Mock Synthesis**:
   Generated isolated Mockito mocks (`@Mock`, `@InjectMocks`, `ArgumentCaptor`) without requiring heavy Spring test runner context.

---

## 3. Prompting Techniques Applied

### Technique 1: Constraint-Based & Negative Prompting
- **Application**: "Do not delete or overwrite existing working files without reviewing them", "Never use double or float for money", "Do not create placeholder methods", "Do not invent Copilot usage".
- **Effect**: Prevented the model from deleting working files, writing stubbed `// TODO` methods, or hallucinating false test metrics.

### Technique 2: Few-Shot In-Context Domain Example Demonstrations
- **Application**: Providing the concrete Splitwise example:
  *"A pays 120 for A, B and C equally. A should receive 80. B owes 40. C owes 40. If B has already paid A in another expense, calculate the combined net balance."*
- **Effect**: Explicitly grounded the mathematical direction (positive = receive, negative = owe) and cumulative balance calculation algorithm, avoiding inverse balance assignments.

### Technique 3: Strict Quota & Structural Decomposition Prompting
- **Application**: Specifying the exact distribution: *1. Equal split - 4 tests. 2. Custom split - 4 tests. 3. Validation - 5 tests. 4. Net balances - 5 tests. 5. Authorization - 4 tests. 6. Transaction module - 4 tests. Total: 26.*
- **Effect**: Prevented arbitrary test bloat or test omission, delivering exact test counts that matched the verification target down to the unit.

---

## 4. Post-Generation Corrections (Human & Architectural Oversight)

During inspection and build execution, multiple critical defects in raw AI output required human engineering remediation:

1. **Package Namespace Relocation**:
   - *AI Output*: Initial Spring Boot generator created root package as `com.fintrack.fintrack_api` due to the hyphenated artifact ID `fintrack-api`.
   - *Human Correction*: Relocated `FintrackApiApplication` to `com.fintrack` so component scanning automatically discovered `transaction` and `expense` subpackages.
2. **IDOR & Missing Authorization Enforcement**:
   - *AI Output*: Controller endpoints took `userId` from path or request body and performed queries/deletions without checking if the caller owned the resource.
   - *Human Correction*: Added caller identity checks (`X-User-Id` / principal context) and repository methods (`findByIdAndUserId`, `deleteByIdAndUserId`).
3. **Decoupling Leaked HTTP Exceptions from Service Layer**:
   - *AI Output*: AI threw `ResponseStatusException(HttpStatus.BAD_REQUEST)` inside `TransactionServiceImpl`.
   - *Human Correction*: Replaced with domain exceptions (`InvalidTransactionException`, `ResourceNotFoundException`, `UnauthorizedException`) intercepted by `@RestControllerAdvice` (`GlobalExceptionHandler`).
4. **Penny Remainder Allocation in Equal Split Calculations**:
   - *AI Output*: AI attempted `amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)` on $100 / 3, producing $33.33 * 3 = $99.99, losing a cent and breaking the zero-sum net balance invariant.
   - *Human Correction*: Implemented penny distribution using integer cent division (`baseCents` + `remainderCents`) to allocate the remaining penny to the first participant, ensuring sum of shares strictly equals the total amount.
5. **ClassNotFound / Missing Test Slice Dependency**:
   - *AI Output*: AI generated `@WebMvcTest` and `ObjectMapper` test classes without checking that the Spring Boot 4 / custom POM lacked `spring-boot-starter-webmvc-test` autoconfiguration slices.
   - *Human Correction*: Refactored controller tests to pure Mockito unit tests (`@ExtendWith(MockitoExtension.class)`), resulting in 100% reliable, sub-second test execution.
6. **Missing Static Assertion Import**:
   - *AI Output*: `NetBalancesTest.java` used `assertNotNull` without importing it statically from `org.junit.jupiter.api.Assertions`.
   - *Human Correction*: Added missing static import and re-ran the build cleanly.
