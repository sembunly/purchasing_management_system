-- Demo account for local development.
-- Login: admin / 1
-- The password below is a BCrypt hash, as required by Spring Security.

MERGE INTO users target
USING (
    SELECT 'admin' AS username,
           '$2y$10$SdkOi3.YWc.HfS0d2dOSx.kKtjFCsKNsVIzfDG3PM6MS0KORDmyv6' AS password_hash,
           'System Administrator' AS full_name,
           'bunlysem@pms.com' AS email,
           1 AS is_active
    FROM dual
) source
ON (target.username = source.username)
WHEN MATCHED THEN UPDATE SET
    target.password_hash = source.password_hash,
    target.full_name = source.full_name,
    target.email = source.email,
    target.is_active = source.is_active
WHEN NOT MATCHED THEN INSERT (username, password_hash, full_name, email, is_active)
VALUES (source.username, source.password_hash, source.full_name, source.email, source.is_active);

MERGE INTO user_roles target
USING (
    SELECT u.id AS user_id, r.id AS role_id, 1 AS is_active
    FROM users u
    JOIN roles r ON r.name = 'ADMIN'
    WHERE u.username = 'admin'
) source
ON (target.user_id = source.user_id AND target.role_id = source.role_id)
WHEN MATCHED THEN UPDATE SET target.is_active = source.is_active
WHEN NOT MATCHED THEN INSERT (user_id, role_id, is_active)
VALUES (source.user_id, source.role_id, source.is_active);

-- Product categories. Existing names are left unchanged, so this is safe to rerun.
MERGE INTO categories target
USING (
    SELECT 'Electronics' name, 'Electronic devices and equipment' description FROM dual UNION ALL
    SELECT 'Office Supplies', 'Office stationery and supplies' FROM dual UNION ALL
    SELECT 'Furniture', 'Office furniture and accessories' FROM dual UNION ALL
    SELECT 'Computer Equipment', 'Computers, laptops and accessories' FROM dual UNION ALL
    SELECT 'Networking', 'Routers, switches and network equipment' FROM dual UNION ALL
    SELECT 'Cleaning Supplies', 'Cleaning materials and equipment' FROM dual UNION ALL
    SELECT 'Medical Supplies', 'Medical equipment and consumables' FROM dual UNION ALL
    SELECT 'Printing Supplies', 'Printers, ink and toner' FROM dual
) source
ON (target.name = source.name)
WHEN NOT MATCHED THEN INSERT (name, description, is_active)
VALUES (source.name, source.description, 1);

COMMIT;
