package com.bunlysem.purchasing.modules.dashboard.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller @RequestMapping("/reports")
public class ReportController {
    private final JdbcTemplate jdbc;
    public ReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Row(String reference, String detail, LocalDateTime date, String status, BigDecimal amount) { }

    @GetMapping("/purchasing")
    String purchasing(Model model) {
        model.addAttribute("activeCode", "PURCHASING_REPORT");
        model.addAttribute("title", "Purchasing Report");
        model.addAttribute("amountLabel", "Estimated total");
        model.addAttribute("rows", jdbc.query("""
            select pr.request_no,d.name,pr.request_date,pr.status,
                   coalesce(sum(prd.quantity*prd.estimated_unit_price),0)
            from purchase_requests pr join departments d on d.id=pr.department_id
            left join purchase_request_details prd on prd.purchase_request_id=pr.id and prd.is_active=1
            where pr.is_active=1 group by pr.id,pr.request_no,d.name,pr.request_date,pr.status
            order by pr.request_date desc fetch first 50 rows only
            """, this::row));
        return "dashboard/report";
    }

    @GetMapping("/receiving")
    String receiving(Model model) {
        model.addAttribute("activeCode", "RECEIVING_REPORT");
        model.addAttribute("title", "Receiving Report");
        model.addAttribute("amountLabel", "Quantity received");
        model.addAttribute("rows", jdbc.query("""
            select gr.receipt_no,po.order_no,gr.received_at,'RECEIVED',
                   coalesce(sum(grd.quantity_received),0)
            from goods_receipts gr join purchase_orders po on po.id=gr.purchase_order_id
            left join goods_receipt_details grd on grd.goods_receipt_id=gr.id and grd.is_active=1
            where gr.is_active=1 and po.is_active=1
            group by gr.id,gr.receipt_no,po.order_no,gr.received_at
            order by gr.received_at desc fetch first 50 rows only
            """, this::row));
        return "dashboard/report";
    }

    @GetMapping("/payments")
    String payments(Model model) {
        model.addAttribute("activeCode", "PAYMENT_REPORT");
        model.addAttribute("title", "Payment Report");
        model.addAttribute("amountLabel", "Paid amount");
        model.addAttribute("rows", jdbc.query("""
            select p.payment_no,po.order_no,p.payment_date,'PAID',p.amount
            from payments p join purchase_orders po on po.id=p.purchase_order_id
            where p.is_active=1 and po.is_active=1
            order by p.payment_date desc fetch first 50 rows only
            """, this::row));
        return "dashboard/report";
    }

    private Row row(java.sql.ResultSet rs, int index) throws java.sql.SQLException {
        return new Row(rs.getString(1), rs.getString(2), rs.getTimestamp(3).toLocalDateTime(),
            rs.getString(4), rs.getBigDecimal(5));
    }
}
