-- Run against an existing purchasing_user schema. Safe to run again.
-- Fresh databases already get these columns from purchasing_schema.sql.
DECLARE
    column_count NUMBER;
    PROCEDURE add_if_missing(p_table_name VARCHAR2, p_column_name VARCHAR2, p_ddl VARCHAR2) IS
    BEGIN
        SELECT COUNT(*) INTO column_count FROM user_tab_columns
        WHERE user_tab_columns.table_name = p_table_name
          AND user_tab_columns.column_name = p_column_name;
        IF column_count = 0 THEN EXECUTE IMMEDIATE p_ddl; END IF;
    END;
BEGIN
    add_if_missing('PRODUCTS', 'IMAGE_DATA',
        'ALTER TABLE products ADD image_data BLOB');
    add_if_missing('PRODUCTS', 'IMAGE_FILENAME',
        'ALTER TABLE products ADD image_filename VARCHAR2(255)');
    add_if_missing('PRODUCTS', 'IMAGE_CONTENT_TYPE',
        'ALTER TABLE products ADD image_content_type VARCHAR2(100)');
    add_if_missing('PURCHASE_ORDERS', 'REPORT_FILE',
        'ALTER TABLE purchase_orders ADD report_file BLOB');
    add_if_missing('PURCHASE_ORDERS', 'REPORT_FILENAME',
        'ALTER TABLE purchase_orders ADD report_filename VARCHAR2(255)');
    add_if_missing('PURCHASE_ORDERS', 'REPORT_CONTENT_TYPE',
        'ALTER TABLE purchase_orders ADD report_content_type VARCHAR2(100)');
END;
/
