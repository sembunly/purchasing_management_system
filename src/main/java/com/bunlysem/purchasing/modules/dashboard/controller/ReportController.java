package com.bunlysem.purchasing.modules.dashboard.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/reports")
public class ReportController {
    private final JdbcTemplate jdbc;
    public ReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Row(String reference,String detail,LocalDateTime date,String status,BigDecimal amount) { }

    private void set(Model model,String title,String active,String label,String sql,String from,String to,String status) {
        model.addAttribute("title",title); model.addAttribute("activeCode",active); model.addAttribute("amountLabel",label);
        model.addAttribute("from",from); model.addAttribute("to",to); model.addAttribute("statusFilter",status);
        LocalDateTime start=from==null||from.isBlank()?LocalDate.of(2000,1,1).atStartOfDay():LocalDate.parse(from).atStartOfDay();
        LocalDateTime end=to==null||to.isBlank()?LocalDate.of(2999,12,31).atTime(23,59,59):LocalDate.parse(to).atTime(23,59,59);
        var rows=jdbc.query(sql,(rs,n)->{var ts=rs.getTimestamp(3);return new Row(rs.getString(1),rs.getString(2),ts==null?null:ts.toLocalDateTime(),rs.getString(4),rs.getBigDecimal(5));},start,end,status==null||status.isBlank()?"%":status);
        model.addAttribute("rows",rows); model.addAttribute("total",rows.stream().map(Row::amount).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add));
    }
    @GetMapping("/purchasing") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','VIEW')")
    String purchasing(@RequestParam(required=false) String from,@RequestParam(required=false) String to,@RequestParam(defaultValue="") String status,Model m) {
        set(m,"Purchase Requests Report","PURCHASING_REPORT","Estimated value","select pr.request_no,d.name,pr.request_date,pr.status,coalesce(sum(rd.quantity*rd.estimated_unit_price),0) from purchase_requests pr join departments d on d.id=pr.department_id left join purchase_request_details rd on rd.purchase_request_id=pr.id and rd.is_active=1 where pr.is_active=1 and pr.request_date between ? and ? and pr.status like ? group by pr.id,pr.request_no,d.name,pr.request_date,pr.status order by pr.request_date desc",from,to,status); return "dashboard/report";
    }
    @GetMapping("/orders") @PreAuthorize("@permissionService.hasPermission(authentication,'ORDERS','VIEW')")
    String orders(@RequestParam(required=false) String from,@RequestParam(required=false) String to,@RequestParam(defaultValue="") String status,Model m) {
        set(m,"Purchase Orders Report","ORDERS_REPORT","Order value","select po.order_no,s.name,po.order_date,po.status,coalesce(sum(d.quantity*d.unit_price),0) from purchase_orders po join suppliers s on s.id=po.supplier_id left join purchase_order_details d on d.purchase_order_id=po.id and d.is_active=1 where po.is_active=1 and po.order_date between ? and ? and po.status like ? group by po.id,po.order_no,s.name,po.order_date,po.status order by po.order_date desc",from,to,status); return "dashboard/report";
    }
    @GetMapping("/receiving") @PreAuthorize("@permissionService.hasPermission(authentication,'RECEIPTS','VIEW')")
    String receiving(@RequestParam(required=false) String from,@RequestParam(required=false) String to,@RequestParam(defaultValue="") String status,Model m) {
        set(m,"Goods Receipts Report","RECEIVING_REPORT","Quantity received","select gr.receipt_no,po.order_no,gr.received_at,po.status,coalesce(sum(d.quantity_received),0) from goods_receipts gr join purchase_orders po on po.id=gr.purchase_order_id left join goods_receipt_details d on d.goods_receipt_id=gr.id and d.is_active=1 where gr.is_active=1 and gr.received_at between ? and ? and po.status like ? group by gr.id,gr.receipt_no,po.order_no,gr.received_at,po.status order by gr.received_at desc",from,to,status); return "dashboard/report";
    }
    @GetMapping("/payments") @PreAuthorize("@permissionService.hasPermission(authentication,'PAYMENTS','VIEW')")
    String payments(@RequestParam(required=false) String from,@RequestParam(required=false) String to,@RequestParam(defaultValue="") String status,Model m) {
        set(m,"Payments Report","PAYMENT_REPORT","Paid amount","select p.payment_no,po.order_no,p.payment_date,po.status,p.amount from payments p join purchase_orders po on po.id=p.purchase_order_id where p.is_active=1 and po.is_active=1 and p.payment_date between ? and ? and po.status like ? order by p.payment_date desc",from,to,status); return "dashboard/report";
    }
    @GetMapping("/suppliers") @PreAuthorize("@permissionService.hasPermission(authentication,'SUPPLIERS','VIEW')")
    String suppliers(@RequestParam(required=false) String from,@RequestParam(required=false) String to,@RequestParam(defaultValue="") String status,Model m) {
        set(m,"Suppliers Report","SUPPLIERS_REPORT","Order value","select s.name,s.contact_name,po.order_date,po.status,coalesce(sum(d.quantity*d.unit_price),0) from suppliers s left join purchase_orders po on po.supplier_id=s.id and po.is_active=1 left join purchase_order_details d on d.purchase_order_id=po.id and d.is_active=1 where s.is_active=1 and (po.order_date between ? and ? or po.order_date is null) and (po.status like ? or po.status is null) group by s.id,s.name,s.contact_name,po.id,po.order_date,po.status order by s.name",from,to,status); return "dashboard/report";
    }
}
