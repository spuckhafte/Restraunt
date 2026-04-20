# Restaurant OMS — Project README

## Functionalities

**Auth (UC-00)**
- Token-based login/logout via `X-Session-Token` header
- `/auth/me` returns current effective role
- Session stored in localStorage, synced across tabs via custom event

**Menu Management (UC-01)**
- Add, soft-delete, update price of menu items
- Only active items shown in POS
- MANAGER-only write; all roles can read

**Point of Sale (UC-02)**
- Cart-based billing with quantity controls
- Process sale → creates bill with line items
- Void bill by ID
- Last receipt shown inline with void option

**Inventory Control (UC-03)**
- Add items, receive stock (IN), issue stock (OUT)
- Issue logs usage to `inventory_usage` table
- Flags unusual consumption if quantity > rolling 3-issue average

**Supplier Invoices (UC-03)**
- Register invoice → auto-receives stock if approved
- Tracks paid/unpaid/flagged status

**Reports Dashboard (UC-04)**
- Date-range filtered: monthly sales, item performance, expenses
- Inventory trend flags (unusual usage)
- Liquidity: cash balance, checks issued, total check payments

**Manager Role Override (UC-05)**
- MANAGER can assume SALES or INVENTORY effective role
- All overrides audited in `manager_override_audit`
- Restore to MANAGER any time

**Supplier Check Generation (UC-06)**
- Generates PDF check for approved unpaid invoices
- Deducts from cash ledger (`cash_ledger`)
- Returns base64 PDF for download
- Guards against insufficient funds

---

## SWE Principles Used

**Layered Architecture**
- Controller → Service → Repository; no layer skips

**Separation of Concerns**
- `AuthPolicyService` owns role-permission mapping
- `SchemaInitializer` owns DB bootstrapping
- Frontend: `lib/api.ts` owns HTTP; `lib/session.ts` owns auth state

**Single Responsibility**
- Each service class handles one domain (Menu, Sales, Inventory, etc.)

**Repository Pattern**
- All SQL isolated in `*Repository` classes extending `DbSupport`
- Services never touch JDBC directly

**Interceptor Pattern**
- `AuthInterceptor` validates token and resolves `SessionPrincipal` before every `/api/**` request

**Soft Delete**
- Menu items deactivated (`active=FALSE`), never hard-deleted

**Optimistic Transaction Safety**
- `issue()`, `createInvoiceAndReceive()`, `persistCheckPayment()` use `setAutoCommit(false)` + rollback on failure

**Observer / Event Pattern**
- `onSessionChange` subscribes to `restaurant-session-changed` custom event + storage event for cross-tab sync

**DTO Pattern**
- All API payloads use Java records as DTOs (no entity exposure)

**Validation at Boundary**
- `@Valid`, `@NotBlank`, `@Positive`, `@DecimalMin` on all request records
- `MethodArgumentNotValidException` caught globally

**Global Exception Handling**
- `ApiExceptionHandler` (`@RestControllerAdvice`) maps all exceptions to structured JSON

**Guard Clauses**
- Services throw `ResponseStatusException` early on bad state instead of nesting conditionals

**Environment Config**
- All DB credentials via `.env` using `dotenv-java`; never hardcoded

**Conditional Bean**
- `@ConditionalOnProperty(app.auth.enabled)` disables auth interceptor in tests

**Audit Logging**
- Manager override actions logged to `manager_override_audit` with actor, token, from/to role

---

## Viva Questions

**Architecture**
- Why use interceptor over Spring Security for auth?
- Why Repository pattern with raw JDBC instead of JPA?
- What does `DbSupport` abstract and why?

**Auth & Sessions**
- How does manager role override work end-to-end?
- What happens if `effective_role` is NULL in DB?
- How does frontend detect session change across tabs?

**Transactions**
- Which operations use explicit transactions and why?
- What happens if PDF generation succeeds but DB insert fails?
- Why `setAutoCommit(true)` in `finally`?

**Business Logic**
- How is unusual inventory consumption detected?
- What prevents double-payment of an invoice?
- How does cash ledger track balance?

**Frontend**
- Why is `effectiveRole` stored in both DB and localStorage?
- How does `DashboardLayout` handle role mismatch between local session and `/auth/me`?
- Why are menu items filtered by `active` only in POS but not Manager panel?

**Testing**
- Why use `@WebMvcTest` instead of `@SpringBootTest`?
- How are DB dependencies bypassed in controller tests?
- What does `app.auth.enabled=false` do in test config?

**Edge Cases**
- What happens when a void is attempted on an already-voided bill?
- What if inventory stock goes negative due to concurrent issues?
- What if check generation races on same invoice?