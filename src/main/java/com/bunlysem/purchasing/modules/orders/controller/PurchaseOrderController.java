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
import java.math.BigDecimal;
import java.util.*;
import org.springframework.security.core.Authentication;

@Controller @RequestMapping("/orders")
public class PurchaseOrderController {
    private final JdbcTemplate jdbc;
    private final OrderReportService reports;

    public PurchaseOrderController(JdbcTemplate jdbc, OrderReportService reports) {
        this.jdbc = jdbc; this.reports = reports;
    }

    public record RequestChoice(Long id,String number,String department) { }
    public record SupplierChoice(Long id,String name) { }
    public record LineView(String product,BigDecimal quantity,BigDecimal price,BigDecimal amount) { }

    @GetMapping
    String list(@RequestParam(defaultValue="") String search, Model model) {
        model.addAttribute("activeCode", "ORDERS");
        model.addAttribute("search",search);
        model.addAttribute("orders", jdbc.query("""
            select po.id,po.order_no,po.order_date,po.status,s.name,
                   case when po.report_file is null then 0 else 1 end has_report
            from purchase_orders po join suppliers s on s.id=po.supplier_id
            where po.is_active=1 and lower(po.order_no) like lower(?) order by po.order_date desc
            """, (rs, row) -> new OrderRow(rs.getLong(1), rs.getString(2),
                rs.getTimestamp(3).toLocalDateTime().toLocalDate(), rs.getString(4),
                rs.getString(5), rs.getInt(6) == 1), "%"+search.trim()+"%"));
        return "orders/index";
    }
    public record OrderRow(Long id, String number, LocalDate date, String status, String supplier, boolean hasReport) { }

    @GetMapping("/new") String createForm(Model model) { return form(model,null); }
    @PostMapping String create(@RequestParam Long requestId,@RequestParam Long supplierId,@RequestParam String status,
        @RequestParam(required=false) String notes,@RequestParam(required=false) List<Long> detailIds,
        @RequestParam(required=false) List<BigDecimal> quantities,@RequestParam(required=false) List<BigDecimal> prices,
        Authentication auth,Model model,RedirectAttributes redirect) {
        try {
            if (!List.of("DRAFT","ORDERED").contains(status)) throw new IllegalArgumentException("Choose DRAFT or ORDERED status.");
            if(jdbc.queryForObject("select count(*) from purchase_requests where id=? and status='APPROVED' and is_active=1",Integer.class,requestId)==0) throw new IllegalArgumentException("Select an active approved request.");
            if(jdbc.queryForObject("select count(*) from suppliers where id=? and is_active=1",Integer.class,supplierId)==0) throw new IllegalArgumentException("Choose an active supplier.");
            if(detailIds==null||detailIds.isEmpty()||detailIds.size()!=quantities.size()||detailIds.size()!=prices.size()) throw new IllegalArgumentException("Provide valid order line items.");
            Set<Long> unique=new HashSet<>(); for(int i=0;i<detailIds.size();i++) {
                if(!unique.add(detailIds.get(i))) throw new IllegalArgumentException("Duplicate request line.");
                BigDecimal q=quantities.get(i),p=prices.get(i); if(q==null||p==null||q.signum()<=0||p.signum()<0) throw new IllegalArgumentException("Quantities must be positive and prices cannot be negative.");
                Integer valid=jdbc.queryForObject("select count(*) from purchase_request_details where id=? and purchase_request_id=? and is_active=1 and quantity>=?",Integer.class,detailIds.get(i),requestId,q);
                if(valid==0) throw new IllegalArgumentException("Order quantity cannot exceed the approved request quantity.");
            }
            Long userId=jdbc.queryForObject("select id from users where username=? and is_active=1",Long.class,auth.getName());
            String no="PO-"+System.currentTimeMillis();
            jdbc.update("insert into purchase_orders(order_no,purchase_request_id,supplier_id,ordered_by,status,notes,is_active) values(?,?,?,?,?,?,1)",no,requestId,supplierId,userId,status,notes);
            Long poId=jdbc.queryForObject("select id from purchase_orders where order_no=?",Long.class,no);
            for(int i=0;i<detailIds.size();i++) { Long product=jdbc.queryForObject("select product_id from purchase_request_details where id=?",Long.class,detailIds.get(i)); jdbc.update("insert into purchase_order_details(purchase_order_id,product_id,quantity,unit_price,is_active) values(?,?,?,?,1)",poId,product,quantities.get(i),prices.get(i)); }
            redirect.addFlashAttribute("success","Purchase order created."); return "redirect:/orders";
        } catch(IllegalArgumentException ex) { model.addAttribute("error",ex.getMessage()); return form(model,Map.of("requestId",requestId,"supplierId",supplierId,"status",status,"notes",notes==null?"":notes)); }
    }
    @GetMapping("/{id}") String details(@PathVariable Long id,Model model) {
        model.addAttribute("activeCode","ORDERS"); model.addAttribute("order",jdbc.queryForMap("select po.id,po.order_no,po.status,po.notes,s.name supplier from purchase_orders po join suppliers s on s.id=po.supplier_id where po.id=? and po.is_active=1",id));
        model.addAttribute("lines",jdbc.query("select p.name,d.quantity,d.unit_price,d.quantity*d.unit_price from purchase_order_details d join products p on p.id=d.product_id where d.purchase_order_id=? and d.is_active=1",(rs,n)->new LineView(rs.getString(1),rs.getBigDecimal(2),rs.getBigDecimal(3),rs.getBigDecimal(4)),id));
        model.addAttribute("total",jdbc.queryForObject("select coalesce(sum(quantity*unit_price),0) from purchase_order_details where purchase_order_id=? and is_active=1",BigDecimal.class,id)); return "orders/details";
    }
    @PostMapping("/{id}/status") String status(@PathVariable Long id,@RequestParam String status,RedirectAttributes redirect) {
        if(!List.of("DRAFT","ORDERED","CANCELLED").contains(status)) { redirect.addFlashAttribute("error","Invalid order status."); return "redirect:/orders/"+id; }
        jdbc.update("update purchase_orders set status=? where id=? and is_active=1",status,id); redirect.addFlashAttribute("success","Order status updated."); return "redirect:/orders/"+id;
    }
    private String form(Model model,Map<String,Object> values) {
        model.addAttribute("activeCode","ORDERS"); model.addAttribute("order",values);
        model.addAttribute("requests",jdbc.query("select pr.id,pr.request_no,d.name from purchase_requests pr join departments d on d.id=pr.department_id where pr.status='APPROVED' and pr.is_active=1 and not exists(select 1 from purchase_orders po where po.purchase_request_id=pr.id and po.is_active=1)",(rs,n)->new RequestChoice(rs.getLong(1),rs.getString(2),rs.getString(3))));
        model.addAttribute("suppliers",jdbc.query("select id,name from suppliers where is_active=1 order by name",(rs,n)->new SupplierChoice(rs.getLong(1),rs.getString(2))));
        model.addAttribute("requestLines",List.of()); return "orders/form";
    }
    @GetMapping("/request/{requestId}/lines") @ResponseBody List<Map<String,Object>> requestLines(@PathVariable Long requestId) {
        return jdbc.queryForList("select id detail_id,product_id, (select name from products p where p.id=prd.product_id) product_name,quantity,estimated_unit_price from purchase_request_details prd where purchase_request_id=? and is_active=1",requestId);
    }

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
