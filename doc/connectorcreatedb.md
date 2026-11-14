# Oracle Database Connection

## 1. Database Configuration

- **Database:** Oracle Database Free
- **Container:** `purchasing_oracle_db`
- **Host:** `localhost`
- **Port:** `1522`
- **Service:** `FREEPDB1`
- **Username:** `purchasing_user`
- **Password:** `0000` (Local Development Only)

## 2. Start Docker

```bash
docker compose up -d
docker compose ps
```

## 3. Create Database User

```bash
docker exec -it purchasing_oracle_db sqlplus / as sysdba
```

```sql
ALTER SESSION SET CONTAINER = FREEPDB1;

CREATE USER purchasing_user IDENTIFIED BY "0000";

GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW,
      CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER
TO purchasing_user;

ALTER USER purchasing_user QUOTA UNLIMITED ON USERS;
```

## 4. Spring Boot Connection

File: `src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//localhost:1522/FREEPDB1
    username: purchasing_user
    password: "0000"
    driver-class-name: oracle.jdbc.OracleDriver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

server:
  port: 8080
```

## 5. Test Connection

```bash
docker exec -it purchasing_oracle_db \
  sqlplus purchasing_user/0000@//localhost:1521/FREEPDB1
```

## 6. Run Spring Boot

```bash
./mvnw spring-boot:run
```

**Note:** Create database tables before running JPA schema validation. Never use the demo password in production.
