---

# Restaurant OMS — Full Presentation Script

---

## 1. Introduction

"This project is a Restaurant Operations Management System built as a full-stack application. The backend is Spring Boot with raw JDBC on MySQL. The frontend is Next.js with TypeScript. The system implements six use cases — menu management, point of sale, inventory control, reporting, manager role override, and supplier check generation — all protected by a custom token-based authentication system."

---

## 2. Architecture Overview

"The project follows a strict three-layer architecture on the backend.

Controllers receive HTTP requests and delegate immediately to services. They never touch the database directly. Services contain all business logic — validation, domain rules, calculations. Repositories contain all SQL and JDBC calls, extending a shared `DbSupport` base class that provides the database connection.

On the frontend, `lib/api.ts` owns all HTTP communication. `lib/session.ts` owns authentication state. Components only call these abstractions — they never fetch directly."

---

## 3. Database & Schema

"The schema is bootstrapped on startup by `SchemaInitializer`, which implements `CommandLineRunner`. It creates all tables if they don't exist and seeds three users — manager, sales, inventory — using `ON DUPLICATE KEY UPDATE` so re-runs are idempotent.

Key tables:
- `app_users` — stores users with plaintext passwords for this academic scope
- `user_sessions` — active tokens with `effective_role` column for override support
- `menu_items` — soft-delete via `active` boolean
- `inventory_items` + `inventory_usage` — stock levels and issue history
- `bills` + `bill_lines` — sales records
- `supplier_invoices` + `supplier_checks` — procurement flow
- `cash_ledger` — double-entry style CREDIT/DEBIT ledger seeded with $50,000 opening balance
- `manager_override_audit` — full audit trail for role switches"

---

## 4. Authentication Flow

"When a user hits `/api/auth/login` with username and password, `AuthService` queries `app_users`. On match, it generates a 128-character UUID token and inserts it into `user_sessions` with the user's base role as `effective_role`.

The token is returned to the frontend and stored in `localStorage` via `lib/session.ts`. Every subsequent request attaches it as the `X-Session-Token` header.

`AuthInterceptor` runs on every `/api/**` route except `/api/auth/login`. It calls `validateToken`, which queries `user_sessions` joined with `app_users`. If the session is active and the user is active, it builds a `SessionPrincipal` record containing `userId`, `username`, `role` (effective), `baseRole`, and `token`. This is set as a request attribute and available to all controllers downstream.

`AuthPolicyService` maps path prefixes to allowed role sets. Menu reads allow all three roles. Menu writes are MANAGER only. Inventory and sales routes allow their respective roles plus MANAGER. Reports, manager routes, and payments are MANAGER only.

The interceptor has a special bypass: if the path starts with `/api/manager`, and the principal's `baseRole` is MANAGER, access is granted regardless of effective role. This lets the manager always reach the override endpoint even while impersonating another role."

---

## 5. Session Management on Frontend

"`SessionState` is stored as JSON in `localStorage` under the key `restaurant.session`. It holds token, userId, username, baseRole, and effectiveRole.

`onSessionChange` registers listeners on both a custom `restaurant-session-changed` DOM event and the browser's `storage` event. The storage event catches changes from other tabs. Every `setSession` and `clearSession` call fires `emitSessionChange`, which dispatches the custom event. This gives real-time cross-tab synchronization.

`DashboardLayout` runs `validateSession` on mount. It first renders from local state, then calls `/auth/me` to verify the server still agrees. If roles diverge — for example after a manager override from another tab — it calls `setSession` to reconcile. If the server returns 401, it clears the session and redirects to login."

---

## 6. Menu Management (UC-01)

"Menu items have a code, name, base price, and active flag. The `MenuRepository` only queries `WHERE active = TRUE`. Deletion is a soft delete — `UPDATE menu_items SET active = FALSE`. This preserves historical bill lines that reference the item code.

The manager can add items, update prices with a prompt dialog, and deactivate items. The `MenuPanel` component accepts a `readOnly` prop — when the manager is operating under an overridden role, the panel renders without action buttons.

Price updates use `PUT /{code}/price` with a `UpdatePriceRequest` body validated with `@DecimalMin(0.0)`."

---

## 7. Point of Sale (UC-02)

"The `POSPanel` loads only active menu items on mount. Staff click items to add them to an in-memory cart. Each cart entry tracks the `MenuItemDto` and quantity. Quantity controls use `Math.max(1, newQ)` to prevent zero quantities.

On checkout, the cart is serialized to `SaleEntryRequest[]` and posted to `/api/sales`. `SalesService.processSale` iterates entries, calls `menuService.findActive` for each code to get the current price, builds `SaleLineDto` objects, and accumulates the subtotal.

`SalesRepository.createBill` wraps the bill insert and all line inserts in a single transaction with `setAutoCommit(false)`. The generated bill ID retrieves the complete `BillDto` which is returned to the frontend.

The last bill is shown inline. Staff can void it via `POST /api/sales/{id}/void`. Void sets `voided = TRUE` only if currently false — idempotency check built in. Voided bills are excluded from reports."

---

## 8. Inventory Control (UC-03)

"Inventory items have a code, name, unit, quantity on hand, and reorder threshold.

Receiving stock — `POST /{code}/receive` — is a simple `UPDATE inventory_items SET quantity_on_hand = quantity_on_hand + ?`. Atomic in MySQL, no explicit transaction needed.

Issuing stock is more complex. `InventoryRepository.issue` runs in a transaction:
1. Loads current item, checks quantity is sufficient
2. Calculates historical average from last 3 entries in `inventory_usage` before this issue
3. Sets `flagged = true` if the requested quantity exceeds that average
4. Inserts the new usage record
5. Recalculates average including this new record
6. Updates `quantity_on_hand` and sets `reorder_threshold` to `2 × new average`
7. Commits

The `IssueResultDto` returns the updated item and the flag. If flagged, the frontend can surface a warning."

---

## 9. Supplier Invoices (UC-03 continued)

"Creating a supplier invoice calls `createInvoiceAndReceive`, which runs in a transaction:
1. Verifies the inventory item exists
2. Increments `quantity_on_hand` by invoice quantity
3. Inserts the invoice record with `approved`, `paid=FALSE`, `flagged_for_review=FALSE`

This means approved invoices automatically receive stock. The transaction ensures stock and invoice record are always consistent.

`InvoicesPanel` shows all invoices sorted by creation descending. It highlights approved-unpaid invoices — these are the ones eligible for check generation."

---

## 10. Reports Dashboard (UC-04)

"`ReportsService.dashboard` accepts optional `from` and `to` dates. All four queries are run independently and assembled into a single `DashboardReportDto`.

Sales summaries group non-voided bills by `DATE_FORMAT(created_at, '%Y-%m')`, counting bills and summing subtotals.

Item performance joins `bill_lines` with `bills`, filters voided bills, groups by item code and name.

Expense summary aggregates `supplier_invoices` in the date range — total, paid, unpaid counts.

Inventory trends pull `issued_today` (sum of today's usage) and `avg_last_three` (average of last 3 usage records) per item, then flag items where today exceeds the average.

Liquidity computes cash balance via the ledger's CREDIT minus DEBIT sum, plus check count and total check payments.

The service throws 404 if all queries return empty and a date range was specified — this distinguishes 'no data in range' from 'no data at all'."

---

## 11. Manager Role Override (UC-05)

"This is the most architecturally interesting feature. A manager can temporarily operate as SALES or INVENTORY to cover for absent staff.

`POST /api/manager/override/assume/{role}` calls `ManagerOverrideService.assumeRole`. It verifies `principal.baseRole() == MANAGER` — this check uses baseRole not effectiveRole, so it's immune to the override itself. It then calls `authRepository.updateSessionRole(token, targetRole)`, updating `effective_role` in the session row. It logs the action to `manager_override_audit`.

The frontend then calls `/auth/me`, which reads the session and returns the new effective role. `updateEffectiveRole` updates localStorage. React state re-renders, hiding panels that require full MANAGER access.

Restore works the same way — sets `effective_role` back to MANAGER's base role.

The audit table records manager user ID, token, from-role, to-role, and action name with a timestamp. This satisfies the accountability requirement."

---

## 12. Supplier Check Generation (UC-06)

"`PaymentsService.generateSupplierCheck` does the following:
1. Queries `supplier_invoices WHERE id=? AND approved=TRUE AND paid=FALSE` — enforces exactly one valid target
2. Reads current cash balance from `cash_ledger` using CREDIT minus DEBIT aggregation
3. Guards: if balance < invoice amount, throws 400 with 'Insufficient funds' message
4. Generates check number as `CHK-{System.currentTimeMillis()}`
5. Calls `createCheckPdf` using OpenPDF (LibrePDF) to produce a PDF byte array
6. Calls `persistCheckPayment` which in one transaction:
   - Inserts into `supplier_checks` with PDF blob
   - Sets `paid=TRUE` on the invoice
   - Inserts a DEBIT entry into `cash_ledger`
7. Returns `CheckPaymentDto` with the PDF as base64

The frontend renders a download anchor using `data:application/pdf;base64,{pdfBase64}` with a `download` attribute set to the check number filename."

---

## 13. Testing Strategy

"Tests use `@WebMvcTest` which loads only the web layer — controllers, exception handler, and message converters. No database, no real services. `@MockBean` provides Mockito mocks for service dependencies.

`app.auth.enabled=false` in `src/test/resources/application.properties` disables the `AuthWebConfig` via `@ConditionalOnProperty`. This means `AuthInterceptor` is not registered, so tests don't need to supply tokens.

For endpoints that need a principal, tests inject it directly via `requestAttr(AuthRequestContext.ATTR_PRINCIPAL, principal)`.

Each controller test covers: happy path returning correct status and body, validation failure returning 400, and service exceptions mapping to correct HTTP status via the global exception handler."

---

## 14. Key Design Decisions

**Why raw JDBC over JPA?**
"Full SQL control. The inventory trend query and the cash ledger aggregation would be difficult to express cleanly in JPQL. Raw JDBC also makes the transaction boundaries explicit — critical for correctness in issue and payment flows."

**Why soft delete for menu items?**
"Bill lines store `item_code` and `item_name` at time of sale. Hard deleting would break historical lookups. Soft delete preserves the audit trail."

**Why `DBConnect` singleton?**
"The project uses a single shared connection. Sufficient for academic scope, but in production this would be replaced with a connection pool like HikariCP."

**Why custom session tokens over JWT?**
"Server-side sessions allow instant invalidation — logout truly kills the session. JWTs can't be revoked without a blacklist. For a system where role overrides need to take effect immediately, server-side is cleaner."

**Why `onSessionChange` event system?**
"Multiple components — `DashboardLayout`, `DashboardHomePage` — independently subscribe to session state. The event bus avoids prop drilling and works across browser tabs via the storage event."

---

## 15. Closing

"To summarize — the system covers the full restaurant operations lifecycle from staff login through sales, inventory, procurement, reporting, and payment generation. It demonstrates layered architecture, transactional data integrity, role-based access control with runtime override, audit logging, and a reactive frontend session model. The test suite validates all controller boundaries in isolation without database dependency."

---