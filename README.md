# Purchasing Management System

A web-based Purchasing Management System built with Spring Boot 4 and Oracle Database, covering the complete purchasing process from request to payment.

## Tech Stack

- Java 21, Spring Boot 4
- Spring MVC, Spring Data JPA, JDBC
- Thymeleaf, Bootstrap 5
- Oracle Database Free
- Docker Compose

## Requirements

- Docker Desktop
- Java 21 (for local development)

## Getting Started

Start the application and Oracle Database:

```bash
docker compose up -d --build
```

Check running containers:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f spring-app
```

For a fresh database, initialize the schema and demo data using:

- `ORACLE/purchasing_schema.sql`
- `ORACLE/demo_data.sql`

See [Database Setup](doc/run-without-docker.md) for instructions.

## Access

Open http://localhost:8080

**Demo Account**

| Username | Password |
|---|---|
| `admin` | `1` |

For local demonstration only. Change the default password before deployment.

## Modules

- Dashboard
- Departments and Suppliers
- Categories and Products
- Purchase Requests and Approvals
- Purchase Orders
- Goods Receipts
- Payment Methods and Payments
- Reports

## Purchasing Workflow

```text
Purchase Request
       |
       v
Approval / Rejection
       |
       v
Purchase Order
       |
       v
Goods Receipt
       |
       v
Payment
       |
       v
Reports
```

The system supports purchase request approvals, purchase orders, partial or full goods receipts, and payment tracking.

## Stop Application

```bash
docker compose down
```

Database data is persisted in a Docker volume.
