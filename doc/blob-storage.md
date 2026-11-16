# Product images and purchase order PDFs

The database still has 18 tables. New installations use `ORACLE/purchasing_schema.sql`.
For an existing database, run `ORACLE/blob_migration.sql` as `purchasing_user`
before starting the application (`ddl-auto: validate` checks the columns).

Products: `/products` lists active products. Create or edit a product to upload
a JPG/PNG image (maximum 5 MB). The saved image can be previewed and displayed;
deactivation sets `is_active = 0`. Category records must exist before creating
a product.

Purchase orders: `/orders` lists active orders. Generate creates a new PDF from
the order header and active lines, then stores it in `report_file`. View / Print
opens the saved PDF inline in a browser; Download sends it as an attachment.
Orders and their lines must already exist in the database.

Permissions use the existing `module_details` matrix: PRODUCTS VIEW/CREATE/EDIT/DELETE
and ORDERS VIEW/EDIT. PDFBox's built-in font covers Latin text; non-Latin order
names currently appear as `?` in generated PDFs.
