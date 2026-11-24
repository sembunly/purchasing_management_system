# Purchasing Management System

A purchasing workflow web application built with Spring Boot 4, Java 21, Thymeleaf, and Oracle Database.

## Features

- User roles and module permissions
- Department, supplier, category, and product management
- Purchase requests, approvals, orders, goods receipts, and payments
- Dashboard, reports, and soft deletion

## Run with Docker

```bash
docker compose up -d --build
```

Open <http://localhost:8080>. Demo login: `admin` / `1` (local demo only).

## Connect to Oracle from the terminal

Start the database container, then connect with SQL*Plus:

```bash
docker compose up -d oracle-db
docker exec -it purchasing_oracle_db sqlplus purchasing_user/0000@//localhost:1521/FREEPDB1
```

If SQL*Plus is installed on your computer, connect through the published port instead:

```bash
sqlplus purchasing_user/0000@//localhost:1522/FREEPDB1
```

For a fresh database, run `ORACLE/purchasing_schema.sql`, then `ORACLE/demo_data.sql`. See [database setup](doc/run-without-docker.md).

## Application call path

```text
Browser → Spring Security → MVC Controller → Service → JPA/JDBC → Oracle
                                              ↓
Browser ← Thymeleaf HTML ← Model ← Controller
```
