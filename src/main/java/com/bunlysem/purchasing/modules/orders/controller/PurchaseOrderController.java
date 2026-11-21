package com.bunlysem.purchasing.modules.orders.controller;

import com.bunlysem.purchasing.modules.orders.model.PurchaseOrder;
import com.bunlysem.purchasing.modules.orders.service.OrderReportService;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/orders")
public class PurchaseOrderController {
    private final JdbcTemplate jdbc;
    private final OrderReportService reports;

    public PurchaseOrderController(JdbcTemplate jdbc, OrderReportService reports) {
        this.jdbc = jdbc; this.reports = reports;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("activeCode", "ORDERS");
        model.addAttribute("orders", jdbc.query("""
            select po.id,po.order_no,po.order_date,po.status,s.name,
                   case when po.report_file is null then 0 else 1 end has_report
            from purchase_orders po join suppliers s on s.id=po.supplier_id
            where po.is_active=1 order by po.order_date desc
            """, (rs, row) -> new OrderRow(rs.getLong(1), rs.getString(2),
                rs.getTimestamp(3).toLocalDateTime().toLocalDate(), rs.getString(4),
                rs.getString(5), rs.getInt(6) == 1)));
        return "orders/index";
    }
    public record OrderRow(Long id, String number, LocalDate date, String status, String supplier, boolean hasReport) { }

    @PostMapping("/{id}/report")
    String generate(@PathVariable Long id, RedirectAttributes redirect) throws IOException {
        reports.generate(id);
        redirect.addFlashAttribute("success", "PDF report generated and saved.");
        return "redirect:/orders";
    }

    @GetMapping("/{id}/report")
    ResponseEntity<byte[]> report(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean download) {
        PurchaseOrder order = reports.get(id);
        if (order.getReportFile() == null) return ResponseEntity.notFound().build();
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
            .filename(order.getReportFilename()).build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .cacheControl(CacheControl.noStore()).body(order.getReportFile());
    }
}
