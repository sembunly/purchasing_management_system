# Purchasing Management System

A monolithic web application for managing purchasing operations, built for an Oracle Database university assignment.

## Tech Stack

- **Backend:** Spring Boot 4, Java 21, Spring Data JPA
- **Frontend:** HTML, Thymeleaf, Bootstrap 5, JavaScript
- **Security:** Spring Security, BCrypt
- **Database:** Oracle Database (26ai)
- **Infrastructure:** Docker Compose

## Features

- User and Role Management
- Supplier, Category, Product and Department Management
- Purchase Requests and Approvals
- Purchase Orders
- Partial Goods Receiving
- Payment Management
- Soft Delete (`is_active = 0`)

## Database Design

15 tables organized into five groups:

| Group | Tables |
|---|---|
| Security | users, roles, user_roles |
| Master Data | departments, suppliers, categories, products |
| Purchasing | purchase_requests, purchase_request_details, purchase_orders, purchase_order_details |
| Receiving | goods_receipts, goods_receipt_details |
| Payment | payment_methods, payments |

**Database connection:**

- Host: `localhost`
- Port: `1522`
- Service: `FREEPDB1`
- Schema: `PURCHASING_USER`

## Run Project

```bash
docker compose up -d
./mvnw spring-boot:run
```

Open: http://localhost:8080

## Architecture

```text
Browser → Thymeleaf → Controller → Service → Repository → Oracle DB
```

## Documentation

- `doc/connectorcreatedb.md` — Database setup
- `doc/workflow.md` — System workflow

## Project Purpose

Demonstrate Oracle Database relationships, SQL, PL/SQL, CRUD operations, access control and purchasing business processes.
