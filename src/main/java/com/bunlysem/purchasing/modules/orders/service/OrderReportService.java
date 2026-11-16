package com.bunlysem.purchasing.modules.orders.service;

import com.bunlysem.purchasing.modules.orders.model.PurchaseOrder;
import com.bunlysem.purchasing.modules.orders.repository.PurchaseOrderRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @Transactional
public class OrderReportService {
    private final PurchaseOrderRepository orders;
    private final JdbcTemplate jdbc;

    public OrderReportService(PurchaseOrderRepository orders, JdbcTemplate jdbc) {
        this.orders = orders; this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public PurchaseOrder get(Long id) {
        return orders.findByIdAndIsActive(id, 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));
    }

    public void generate(Long id) throws IOException {
        PurchaseOrder order = get(id);
        OrderHeader header = jdbc.queryForObject("""
            select po.order_no, po.order_date, po.status, s.name, po.notes
            from purchase_orders po join suppliers s on s.id=po.supplier_id
            where po.id=? and po.is_active=1
            """, (rs, row) -> new OrderHeader(rs.getString(1), rs.getTimestamp(2).toLocalDateTime(),
                rs.getString(3), rs.getString(4), rs.getString(5)), id);
        List<OrderLine> lines = jdbc.query("""
            select p.sku, p.name, d.quantity, d.unit_price
            from purchase_order_details d join products p on p.id=d.product_id
            where d.purchase_order_id=? and d.is_active=1 order by d.id
            """, (rs, row) -> new OrderLine(rs.getString(1), rs.getString(2),
                rs.getBigDecimal(3), rs.getBigDecimal(4)), id);
        byte[] pdf = createPdf(header, lines);
        if (pdf.length > 5L * 1024 * 1024) throw new IllegalArgumentException("Report exceeds 5 MB.");
        order.setReportFile(pdf);
        order.setReportFilename("purchase-order-" + order.getId() + ".pdf");
        order.setReportContentType("application/pdf");
    }

    private byte[] createPdf(OrderHeader header, List<OrderLine> lines) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPage page = null;
            PDPageContentStream stream = null;
            float y = 0;
            BigDecimal total = BigDecimal.ZERO;
            try {
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                stream = new PDPageContentStream(document, page);
                y = 790;
                write(stream, bold, 18, 48, y, "PURCHASE ORDER"); y -= 36;
                write(stream, regular, 11, 48, y, "Order: " + header.number()); y -= 20;
                write(stream, regular, 11, 48, y, "Supplier: " + header.supplier()); y -= 20;
                write(stream, regular, 11, 48, y, "Date: " + header.date().toLocalDate() + "    Status: " + header.status()); y -= 32;
                if (header.notes() != null && !header.notes().isBlank()) {
                    write(stream, regular, 10, 48, y, "Notes: " + cut(header.notes(), 92)); y -= 25;
                }
                write(stream, bold, 10, 48, y, "SKU / PRODUCT");
                write(stream, bold, 10, 330, y, "QTY");
                write(stream, bold, 10, 400, y, "PRICE");
                write(stream, bold, 10, 490, y, "AMOUNT"); y -= 22;
                for (OrderLine line : lines) {
                    if (y < 70) {
                        stream.close();
                        page = new PDPage(PDRectangle.A4); document.addPage(page);
                        stream = new PDPageContentStream(document, page); y = 790;
                    }
                    BigDecimal amount = line.quantity().multiply(line.price());
                    total = total.add(amount);
                    write(stream, regular, 9, 48, y, cut(line.sku() + " / " + line.name(), 51));
                    write(stream, regular, 9, 330, y, line.quantity().toPlainString());
                    write(stream, regular, 9, 400, y, line.price().toPlainString());
                    write(stream, regular, 9, 490, y, amount.toPlainString()); y -= 19;
                }
                if (y < 70) { stream.close(); page = new PDPage(PDRectangle.A4); document.addPage(page); stream = new PDPageContentStream(document, page); y = 790; }
                y -= 15;
                write(stream, bold, 11, 400, y, "TOTAL: " + total.toPlainString());
            } finally {
                if (stream != null) stream.close();
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private void write(PDPageContentStream stream, PDType1Font font, int size, float x, float y, String text) throws IOException {
        // Standard PDF fonts support Latin characters; replace unsupported characters safely.
        String safe = text == null ? "" : text.replaceAll("[^\\x20-\\x7E]", "?");
        stream.beginText(); stream.setFont(font, size); stream.newLineAtOffset(x, y);
        stream.showText(safe); stream.endText();
    }

    private String cut(String value, int length) { return value.length() <= length ? value : value.substring(0, length - 3) + "..."; }
    private record OrderHeader(String number, LocalDateTime date, String status, String supplier, String notes) { }
    private record OrderLine(String sku, String name, BigDecimal quantity, BigDecimal price) { }
}
