# Restaurant Order & Inventory Management System

## Progress

- Refactor completed: project moved from CLI/TUI flow to Spring Boot REST backend.
- Backend API implemented for menu, inventory, and sales modules.
- MySQL-backed persistence implemented with startup schema initialization.
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
- Sales
  - `POST /api/sales`
  - `POST /api/sales/{billId}/void`

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

### Frontend

_TBD_