# Purchasing Management System — Workflow

## 1. Login

Users log in using their username and password. Available menus and actions depend on their assigned permissions.

## 2. Admin Setup

Admin configures:

- Users and Roles
- Module Permissions (View, Create, Edit, Delete, Approve)
- Departments
- Suppliers, Categories and Products

## 3. Purchase Request

Staff creates a purchase request, selects products, enters quantities and submits it for approval.

**Status:** DRAFT → PENDING → APPROVED / REJECTED

## 4. Purchase Order

After approval, an authorized user creates a purchase order, selects a supplier and confirms prices and quantities.

## 5. Goods Receiving

Staff records received quantities against purchase order items.

The system supports partial receiving until all ordered quantities are received.

## 6. Payment

Authorized users record supplier payments using Cash, Bank Transfer or KHQR.

## 7. Reports

Users view purchasing summaries, order status, receiving progress and payment records.

## 8. Permissions & Soft Delete

- Admin controls sidebar visibility and action buttons.
- Backend validates user permissions.
- Deleted records use `is_active = 0`.
- Active records use `is_active = 1`.

## Workflow Summary

```text
Login
  ↓
Admin Setup
  ↓
Purchase Request
  ↓
Manager Approval
  ↓
Purchase Order
  ↓
Goods Receiving
  ↓
Payment
  ↓
Reports
```
