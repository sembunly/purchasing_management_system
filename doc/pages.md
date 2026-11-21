# Accessing the Application

## 1. Start the app

```bash
docker compose up -d oracle-db
./mvnw spring-boot:run
```

PowerShell: `.\mvnw.cmd spring-boot:run`

Open http://localhost:8080 — you are redirected to the login page.

## 2. Log in

The seeded demo account (`ORACLE/demo_data.sql`) is:

| Username | Password |
|---|---|
| `admin` | `1` |

`admin` has the `ADMIN` role and full module permissions, so every page below is reachable. Other accounts must have the matching `module_details` actions or the request fails with 403 (`/access-denied`).

## 3. Page URLs

| Page | URL | Required permission |
|---|---|---|
| Login | `/login` | public |
| Dashboard | `/` or `/dashboard` | `DASHBOARD` VIEW |
| Purchasing report | `/reports/purchasing` | `REQUESTS` VIEW |
| Receiving report | `/reports/receiving` | `RECEIPTS` VIEW |
| Payment report | `/reports/payments` | `PAYMENTS` VIEW |
| Product list | `/products` | `PRODUCTS` VIEW |
| New product | `/products/new` | `PRODUCTS` CREATE |
| Edit product | `/products/{id}/edit` | `PRODUCTS` EDIT |
| Purchase orders | `/orders` | `ORDERS` VIEW |
| Order PDF | `/orders/{id}/report` | `ORDERS` VIEW |
| Users | `/settings/users` | `USERS` VIEW |
| New user | `/settings/users/new` | `USERS` CREATE |
| Edit user | `/settings/users/{id}/edit` | `USERS` EDIT |
| Permissions | `/settings/permissions` | `PERMISSIONS` VIEW |
| Edit permission | `/settings/permissions/edit/{id}` | `PERMISSIONS` VIEW |

Reports, orders, products, and users are also reachable from the sidebar. The sidebar is database-driven, so it shows links for every module in the `modules` table, including modules with no implemented pages (requests, receipts, payments, suppliers, categories, departments) — those links return 404.

## 4. Writable actions

| Action | Request |
|---|---|
| Create product | `POST /products` |
| Update product | `POST /products/{id}` |
| Deactivate product | `POST /products/{id}/deactivate` |
| View product image | `GET /products/{id}/image` |
| Generate order PDF | `POST /orders/{id}/report` |
| Download order PDF | `GET /orders/{id}/report?download=true` |
| Create user | `POST /settings/users` |
| Update user | `POST /settings/users/{id}` |
| Deactivate user | `POST /settings/users/{id}/deactivate` |
| Activate user | `POST /settings/users/{id}/activate` |
| Save module permissions | `POST /settings/permissions/edit/{id}` |

## 5. Troubleshooting

- **403 / Access Denied:** the logged-in user lacks the required permission; grant it via `/settings/permissions` or insert a `module_details` row.
- **404 on a sidebar link:** the module has no controller yet (see list above).
- **Empty dashboard, reports, or orders:** the app only renders existing rows. Seed `purchase_orders`, suppliers, and order lines with SQL — `demo_data.sql` only creates the `admin` login and categories.
- **Login fails:** confirm `demo_data.sql` was run against the same schema the app connects to.
