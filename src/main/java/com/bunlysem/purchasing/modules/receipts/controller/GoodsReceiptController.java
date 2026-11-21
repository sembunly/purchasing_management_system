package com.bunlysem.purchasing.modules.receipts.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/receipts")
public class GoodsReceiptController {
    private final JdbcTemplate jdbc;
    public GoodsReceiptController(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public record Receipt(Long id,String number,String order,String receiver,java.time.LocalDateTime receivedAt,String notes) { }
    public record OrderChoice(Long id,String number,String supplier) { }
    public record Line(Long id,String product,BigDecimal ordered,BigDecimal received,BigDecimal remaining) { }

    @GetMapping String list(Model model) {
        model.addAttribute("activeCode","RECEIPTS");
        model.addAttribute("receipts",jdbc.query("select gr.id,gr.receipt_no,po.order_no,u.full_name,gr.received_at,gr.notes from goods_receipts gr join purchase_orders po on po.id=gr.purchase_order_id join users u on u.id=gr.received_by where gr.is_active=1 order by gr.received_at desc",(rs,n)->new Receipt(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getTimestamp(5).toLocalDateTime(),rs.getString(6))));
        return "receipts/index";
    }
    @GetMapping("/new") String form(Model model) {
        model.addAttribute("activeCode","RECEIPTS"); model.addAttribute("orders",jdbc.query("select po.id,po.order_no,s.name from purchase_orders po join suppliers s on s.id=po.supplier_id where po.is_active=1 and po.status in ('ORDERED','PARTIAL') and exists(select 1 from purchase_order_details pod where pod.purchase_order_id=po.id and pod.is_active=1 and pod.quantity > (select coalesce(sum(grd.quantity_received),0) from goods_receipt_details grd where grd.purchase_order_detail_id=pod.id and grd.is_active=1 and grd.goods_receipt_id in (select gr.id from goods_receipts gr where gr.is_active=1))) order by po.order_date desc",(rs,n)->new OrderChoice(rs.getLong(1),rs.getString(2),rs.getString(3))));
        model.addAttribute("lines",List.of()); return "receipts/form";
    }
    @GetMapping("/order/{id}/lines") @ResponseBody List<Map<String,Object>> orderLines(@PathVariable Long id) {
        return jdbc.queryForList("select pod.id line_id,p.name product,pod.quantity ordered,coalesce((select sum(grd.quantity_received) from goods_receipt_details grd join goods_receipts gr on gr.id=grd.goods_receipt_id where grd.purchase_order_detail_id=pod.id and grd.is_active=1 and gr.is_active=1),0) received,pod.quantity-coalesce((select sum(grd.quantity_received) from goods_receipt_details grd join goods_receipts gr on gr.id=grd.goods_receipt_id where grd.purchase_order_detail_id=pod.id and grd.is_active=1 and gr.is_active=1),0) remaining from purchase_order_details pod join products p on p.id=pod.product_id where pod.purchase_order_id=? and pod.is_active=1 and pod.quantity>coalesce((select sum(grd.quantity_received) from goods_receipt_details grd join goods_receipts gr on gr.id=grd.goods_receipt_id where grd.purchase_order_detail_id=pod.id and grd.is_active=1 and gr.is_active=1),0)",id);
    }
    @PostMapping String create(@RequestParam Long orderId,@RequestParam List<Long> orderLineIds,@RequestParam List<BigDecimal> quantities,@RequestParam(required=false) String notes, Authentication auth,RedirectAttributes redirect) {
        if(orderLineIds.isEmpty()||orderLineIds.size()!=quantities.size()) { redirect.addFlashAttribute("error","Add receipt quantities for the selected order.");return "redirect:/receipts/new"; }
        Long user=jdbc.queryForObject("select id from users where username=? and is_active=1",Long.class,auth.getName());
        String receipt="GR-"+System.currentTimeMillis();
        try {
            jdbc.update("insert into goods_receipts(receipt_no,purchase_order_id,received_by,notes,is_active) values(?,?,?,?,1)",receipt,orderId,user,notes);
            Long receiptId=jdbc.queryForObject("select id from goods_receipts where receipt_no=?",Long.class,receipt);
            Set<Long> seen=new HashSet<>();
            for(int i=0;i<orderLineIds.size();i++) {
                BigDecimal q=quantities.get(i); if(q==null||q.signum()<=0||!seen.add(orderLineIds.get(i))) throw new IllegalArgumentException("Each line needs a positive quantity and may appear only once.");
                List<Map<String,Object>> row=jdbc.queryForList("select pod.quantity-coalesce((select sum(grd.quantity_received) from goods_receipt_details grd join goods_receipts gr on gr.id=grd.goods_receipt_id where grd.purchase_order_detail_id=pod.id and grd.is_active=1 and gr.is_active=1),0) remaining from purchase_order_details pod join purchase_orders po on po.id=pod.purchase_order_id where pod.id=? and po.id=? and po.is_active=1 and po.status in ('ORDERED','PARTIAL') and pod.is_active=1",orderLineIds.get(i),orderId);
                if(row.isEmpty()||q.compareTo((BigDecimal)row.get(0).get("remaining"))>0) throw new IllegalArgumentException("Received quantity cannot exceed the remaining ordered quantity.");
                jdbc.update("insert into goods_receipt_details(goods_receipt_id,purchase_order_id,purchase_order_detail_id,quantity_received,is_active) values(?,?,?,?,1)",receiptId,orderId,orderLineIds.get(i),q);
            }
            Integer outstanding=jdbc.queryForObject("select count(*) from purchase_order_details pod where pod.purchase_order_id=? and pod.is_active=1 and pod.quantity>coalesce((select sum(grd.quantity_received) from goods_receipt_details grd join goods_receipts gr on gr.id=grd.goods_receipt_id where grd.purchase_order_detail_id=pod.id and gr.is_active=1 and grd.is_active=1),0)",Integer.class,orderId);
            jdbc.update("update purchase_orders set status=? where id=?",outstanding==0?"RECEIVED":"PARTIAL",orderId);
            redirect.addFlashAttribute("success","Goods receipt recorded.");
        } catch(IllegalArgumentException ex) { jdbc.update("delete from goods_receipt_details where goods_receipt_id=(select id from goods_receipts where receipt_no=?)",receipt);jdbc.update("delete from goods_receipts where receipt_no=?",receipt);redirect.addFlashAttribute("error",ex.getMessage());return "redirect:/receipts/new"; }
        return "redirect:/receipts";
    }
    @GetMapping("/{id}") String details(@PathVariable Long id,Model model) {
        model.addAttribute("activeCode","RECEIPTS");model.addAttribute("receipt",jdbc.queryForMap("select gr.id,gr.receipt_no,gr.notes,po.order_no,u.full_name receiver,gr.received_at from goods_receipts gr join purchase_orders po on po.id=gr.purchase_order_id join users u on u.id=gr.received_by where gr.id=? and gr.is_active=1",id));
        model.addAttribute("lines",jdbc.queryForList("select p.name product,grd.quantity_received quantity from goods_receipt_details grd join purchase_order_details pod on pod.id=grd.purchase_order_detail_id join products p on p.id=pod.product_id where grd.goods_receipt_id=? and grd.is_active=1",id));return "receipts/details";
    }
}
