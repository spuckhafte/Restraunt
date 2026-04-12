# Restaurant Order & Inventory Management System

Spring Boot backend for a staff-only restaurant system covering:

- UC-01 Menu management
- UC-02 Sales entry and billing
- UC-03 Inventory + reorder thresholds + invoices
- UC-04 Reporting dashboard
- UC-05 Manager role override
- UC-06 Automated supplier check generation

## Progress

- Refactor completed: project moved from CLI/TUI flow to Spring Boot REST backend.
- Backend API completed for all 6 core modules from the use-case document.
- MySQL-backed persistence implemented with startup schema initialization.
- Authentication integrated through a common request middleware for all protected `/api/**` routes.
- Session token flow integrated with role-aware authorization and manager override support.
- Controller API tests implemented under `backend/src/tests/java`.

## API Modules (Backend)

- Menu
  - `GET /api/menu`
  - `POST /api/menu`
  - `PUT /api/menu/{code}/price`
  - `DELETE /api/menu/{code}`
- Inventory
  - `GET /api/inventory`
  - `POST /api/inventory`
  - `POST /api/inventory/{code}/receive`
  - `POST /api/inventory/{code}/issue`
  - `GET /api/inventory/invoices`
  - `POST /api/inventory/invoices`
- Sales
  - `POST /api/sales`
  - `POST /api/sales/{billId}/void`
- Reports
  - `GET /api/reports/dashboard?from=YYYY-MM-DD&to=YYYY-MM-DD`
- Managerial Override
  - `POST /api/manager/override/assume/{role}`
  - `POST /api/manager/override/restore`
- Automated Check Generation
  - `POST /api/payments/checks/generate`

## Authentication & Authorization

- Public route:
  - `POST /api/auth/login`
- Protected routes:
  - All other `/api/**` routes require `X-Session-Token` header.
- Auth routes:
  - `POST /api/auth/logout`
  - `GET /api/auth/me`
- Role model:
  - `MANAGER`, `SALES`, `INVENTORY`
- Middleware behavior:
  - Validates session token.
  - Resolves current effective role from the session.
  - Attaches authenticated user/session principal to request context.
  - Enforces route-level role policy for current and upcoming modules.

### Seeded Users (hardcoded for staff-only deployment)

- `manager / manager123` (`MANAGER`)
- `sales / sales123` (`SALES`)
- `inventory / inventory123` (`INVENTORY`)

## Database Structure (MySQL)

The backend auto-creates these tables on startup:

### 1) `menu_items`
- `code VARCHAR(50) PRIMARY KEY`
- `name VARCHAR(255) NOT NULL`
- `base_price DOUBLE NOT NULL`
- `active BOOLEAN NOT NULL DEFAULT TRUE`
- `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### 2) `inventory_items`
- `code VARCHAR(50) PRIMARY KEY`
- `name VARCHAR(255) NOT NULL`
- `unit VARCHAR(50) NOT NULL`
- `quantity_on_hand DOUBLE NOT NULL`
- `reorder_threshold DOUBLE NOT NULL DEFAULT 0`
- `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`

### 3) `inventory_usage`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `item_code VARCHAR(50) NOT NULL`
- `quantity DOUBLE NOT NULL`
- `used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `FOREIGN KEY (item_code) REFERENCES inventory_items(code)`

### 4) `bills`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `subtotal DOUBLE NOT NULL`
- `voided BOOLEAN NOT NULL DEFAULT FALSE`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 5) `bill_lines`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `bill_id BIGINT NOT NULL`
- `item_code VARCHAR(50) NOT NULL`
- `item_name VARCHAR(255) NOT NULL`
- `unit_price DOUBLE NOT NULL`
- `quantity INT NOT NULL`
- `line_total DOUBLE NOT NULL`
- `FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE`

### 6) `app_users`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `username VARCHAR(100) NOT NULL UNIQUE`
- `password VARCHAR(255) NOT NULL`
- `role VARCHAR(30) NOT NULL`
- `active BOOLEAN NOT NULL DEFAULT TRUE`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 7) `user_sessions`
- `token VARCHAR(128) PRIMARY KEY`
- `user_id BIGINT NOT NULL`
- `effective_role VARCHAR(30) NULL`
- `active BOOLEAN NOT NULL DEFAULT TRUE`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE`

### 8) `supplier_invoices`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `supplier_name VARCHAR(255) NOT NULL`
- `item_code VARCHAR(50) NOT NULL`
- `quantity DOUBLE NOT NULL`
- `unit_price DOUBLE NOT NULL`
- `total_amount DOUBLE NOT NULL`
- `invoice_date DATE NOT NULL`
- `approved BOOLEAN NOT NULL DEFAULT TRUE`
- `paid BOOLEAN NOT NULL DEFAULT FALSE`
- `flagged_for_review BOOLEAN NOT NULL DEFAULT FALSE`
- `created_by_user_id BIGINT NULL`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 9) `supplier_checks`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `invoice_id BIGINT NOT NULL UNIQUE`
- `check_number VARCHAR(50) NOT NULL UNIQUE`
- `amount DOUBLE NOT NULL`
- `pdf_data LONGBLOB NOT NULL`
- `generated_by_user_id BIGINT NULL`
- `printed BOOLEAN NOT NULL DEFAULT TRUE`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 10) `cash_ledger`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `entry_type VARCHAR(20) NOT NULL`
- `amount DOUBLE NOT NULL`
- `reference_type VARCHAR(50) NULL`
- `reference_id BIGINT NULL`
- `note VARCHAR(255) NULL`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 11) `manager_override_audit`
- `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- `manager_user_id BIGINT NOT NULL`
- `token VARCHAR(128) NOT NULL`
- `from_role VARCHAR(30) NOT NULL`
- `to_role VARCHAR(30) NOT NULL`
- `action_name VARCHAR(50) NOT NULL`
- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`

## How to Run

### Backend

1. Go to backend folder:
	- `cd backend`
2. Ensure MySQL is running and accessible with values from `backend/.env`:
	- `DB_PORT`
	- `DB_NAME`
	- `DB_USERNAME`
	- `DB_PASSWORD`
3. Build and test:
	- `mvn clean install`
4. Run backend:
	- `java -jar target/restaurant-backend-1.0.0.jar`
5. Base URL:
	- `http://localhost:8080`


## Notes

- Supplier check generation stores a PDF check copy (`pdf_data`) and debits the cash ledger.
- Startup automatically seeds opening cash ledger with `50000` if empty.
- Reports endpoint combines sales, item performance, expenses/invoices, inventory trend flags, and liquidity.

### Frontend
*tbd*