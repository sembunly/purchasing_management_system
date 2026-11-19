# Run Without Docker

This setup runs Spring Boot on your computer and connects to an Oracle database installed and running locally. The database is still required; Docker is not.

## Requirements

- Java 21
- Oracle Database running on this computer or reachable over the network
- Oracle service name and credentials for a schema with the project tables
- SQL Developer or SQL*Plus to initialize a fresh schema

The examples below use Oracle Free with service `FREEPDB1` on port `1521`. Change the service name, host, and port if your Oracle installation uses different values.

## 1. Create the application schema

For a fresh Oracle Free database, connect to the `FREEPDB1` pluggable database as an administrator (for example, with SQL Developer), then create the application user and grant the required development privileges:

```sql
CREATE USER purchasing_user IDENTIFIED BY "0000";
GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE,
      CREATE PROCEDURE, CREATE TRIGGER TO purchasing_user;
ALTER USER purchasing_user QUOTA UNLIMITED ON USERS;
```

Connect to the database as `purchasing_user`. Open `ORACLE/purchasing_schema.sql` and run the SQL beginning with `CREATE TABLE departments` through the end of the file. Skip the introductory user-creation statements at the top; those are for Docker setup. This creates the tables and reference data. Then run `ORACLE/demo_data.sql` to add the development login `admin` / `1`.

Run these scripts only for a new, empty schema. The schema script is not an upgrade script and its table-creation statements are not safe to rerun.

## 2. Start the application

From the project root, set the database connection and disable Spring Boot's Docker Compose integration. The defaults in `application.yaml` target Docker's mapped port `1522`, so override the URL for a local Oracle listener on port `1521`.

PowerShell:

```powershell
$env:SPRING_DOCKER_COMPOSE_ENABLED = "false"
$env:SPRING_DATASOURCE_URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1"
$env:SPRING_DATASOURCE_USERNAME = "purchasing_user"
$env:SPRING_DATASOURCE_PASSWORD = "0000"
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
export SPRING_DOCKER_COMPOSE_ENABLED=false
export SPRING_DATASOURCE_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export SPRING_DATASOURCE_USERNAME=purchasing_user
export SPRING_DATASOURCE_PASSWORD=0000
./mvnw spring-boot:run
```

Maven downloads project dependencies the first time it runs. When the application starts, open <http://localhost:8080> and sign in with the demo account `admin` / `1`.

## Troubleshooting

- **Connection refused:** Ensure Oracle is running, and that the host, listener port, and service name in `SPRING_DATASOURCE_URL` are correct.
- **Invalid username/password:** Confirm the schema user exists in the target PDB and the connection variables match its credentials.
- **Schema validation failed:** Run the schema script as `purchasing_user` on a fresh schema before starting the application; Hibernate is configured to validate, not create, tables.
- **Port already in use:** Change `server.port` in `src/main/resources/application.yaml` or set `SERVER_PORT` to an available port.

The example credentials are for local development only. Use a strong database password and avoid storing real credentials in source control.
