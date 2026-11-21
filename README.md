# Purchasing Management System

A Spring Boot web application for managing purchasing requests, orders, receipts, and payments with Oracle Database.

## Technology

- Java 21 and Spring Boot 4
- Spring MVC, Spring Data JPA, and JDBC
- Thymeleaf, Bootstrap 5, and JavaScript
- Oracle Database Free (23ai/26ai compatible)
- Docker Compose

## Modules

- Authentication and user management
- Departments, suppliers, categories, and products
- Purchase requests with approval and rejection
- Purchase orders created from approved requests
- Goods receipts with partial receiving
- Payment methods and partial payments
- Date- and status-filtered reports
- Dashboard with live totals, charts, and recent activity

The application uses soft deletion through `is_active`. Database tables are defined in `ORACLE/purchasing_schema.sql`; the application does not require additional tables.

## Setup

Configure Oracle connection properties in `src/main/resources/application.yaml`. The default local connection uses service `FREEPDB1`, port `1522`, and schema user `PURCHASING_USER`.

Start Oracle using Docker Compose and run the application:

```bash
docker compose up -d
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). Initialize a new database by running `ORACLE/purchasing_schema.sql` with the required Oracle privileges. To stop the database container, run:

```bash
docker compose down
```

## Workflow

Create a purchase request with product lines and save it as `DRAFT` or submit it as `PENDING`. A pending request may be approved or rejected; the rejection reason is stored in the existing notes field. Create an order from an approved request, select a supplier, and enter order quantities and prices. Record one or more goods receipts, up to the ordered quantity; order status becomes `PARTIAL` or `RECEIVED`. Record payments against an order; partial payments are supported and payments cannot exceed the remaining order balance. Use reports to filter purchasing activity by date and status and print results.

## Documentation

- `ORACLE/purchasing_schema.sql` — Oracle schema and reference data
- `doc/purchasing_workflow.xml` — Structured purchasing workflow
- `doc/run-without-docker.md` — Local run instructions
