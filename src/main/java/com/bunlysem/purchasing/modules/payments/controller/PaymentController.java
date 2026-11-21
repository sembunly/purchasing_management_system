package com.bunlysem.purchasing.modules.payments.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/payments")
public class PaymentController {
    private final JdbcTemplate jdbc;
    public PaymentController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public record OrderChoice(Long id,String number,String supplier,BigDecimal total,BigDecimal paid,BigDecimal remaining){}
    public record MethodChoice(Long id,String name){}
    @GetMapping String list(Model model){model.addAttribute("activeCode","PAYMENTS");model.addAttribute("payments",jdbc.queryForList("select p.id,p.payment_no,p.purchase_order_id,po.order_no,m.name method,p.amount,p.payment_date,p.reference_no from payments p join purchase_orders po on po.id=p.purchase_order_id join payment_methods m on m.id=p.payment_method_id where p.is_active=1 order by p.payment_date desc"));return "payments/index";}
    @GetMapping("/new") String form(Model model){model.addAttribute("activeCode","PAYMENTS");model.addAttribute("orders",jdbc.query("select po.id,po.order_no,s.name,coalesce((select sum(d.quantity*d.unit_price) from purchase_order_details d where d.purchase_order_id=po.id and d.is_active=1),0) total,coalesce((select sum(p.amount) from payments p where p.purchase_order_id=po.id and p.is_active=1),0) paid from purchase_orders po join suppliers s on s.id=po.supplier_id where po.is_active=1 and po.status<>'CANCELLED' order by po.order_date desc",(rs,n)->{BigDecimal t=rs.getBigDecimal(4),p=rs.getBigDecimal(5);return new OrderChoice(rs.getLong(1),rs.getString(2),rs.getString(3),t,p,t.subtract(p));}));model.addAttribute("methods",jdbc.query("select id,name from payment_methods where is_active=1 order by name",(rs,n)->new MethodChoice(rs.getLong(1),rs.getString(2))));return "payments/form";}
    @PostMapping String create(@RequestParam Long orderId,@RequestParam Long methodId,@RequestParam BigDecimal amount,@RequestParam(required=false) String referenceNo,@RequestParam(required=false) String notes,Authentication auth,RedirectAttributes redirect){
        if(amount==null||amount.signum()<=0){redirect.addFlashAttribute("error","Payment amount must be greater than zero.");return "redirect:/payments/new";}
        try {
            Integer validOrder=jdbc.queryForObject("select count(*) from purchase_orders where id=? and is_active=1 and status<>'CANCELLED'",Integer.class,orderId);
            Integer validMethod=jdbc.queryForObject("select count(*) from payment_methods where id=? and is_active=1",Integer.class,methodId);
            if(validOrder==0||validMethod==0) throw new IllegalArgumentException("Choose an active order and payment method.");
            BigDecimal total=jdbc.queryForObject("select coalesce(sum(quantity*unit_price),0) from purchase_order_details where purchase_order_id=? and is_active=1",BigDecimal.class,orderId);
            BigDecimal paid=jdbc.queryForObject("select coalesce(sum(amount),0) from payments where purchase_order_id=? and is_active=1",BigDecimal.class,orderId);
            if(amount.compareTo(total.subtract(paid))>0) throw new IllegalArgumentException("Payment exceeds remaining balance: "+total.subtract(paid));
            Long user=jdbc.queryForObject("select id from users where username=? and is_active=1",Long.class,auth.getName());
            String no="PAY-"+System.currentTimeMillis();jdbc.update("insert into payments(payment_no,purchase_order_id,payment_method_id,paid_by,amount,reference_no,notes,is_active) values(?,?,?,?,?,?,?,1)",no,orderId,methodId,user,amount,referenceNo,notes);
            redirect.addFlashAttribute("success","Payment recorded.");
        } catch(IllegalArgumentException ex){redirect.addFlashAttribute("error",ex.getMessage());return "redirect:/payments/new";}
        return "redirect:/payments";
    }
    @GetMapping("/order/{id}") String orderHistory(@PathVariable Long id,Model model){
        Map<String,Object> order=jdbc.queryForMap("select po.id,po.order_no,s.name supplier from purchase_orders po join suppliers s on s.id=po.supplier_id where po.id=? and po.is_active=1",id);
        BigDecimal total=jdbc.queryForObject("select coalesce(sum(quantity*unit_price),0) from purchase_order_details where purchase_order_id=? and is_active=1",BigDecimal.class,id);
        BigDecimal paid=jdbc.queryForObject("select coalesce(sum(amount),0) from payments where purchase_order_id=? and is_active=1",BigDecimal.class,id);
        model.addAttribute("activeCode","PAYMENTS");model.addAttribute("order",order);model.addAttribute("total",total);model.addAttribute("paid",paid);model.addAttribute("remaining",total.subtract(paid));model.addAttribute("payments",jdbc.queryForList("select p.payment_no,m.name method,p.amount,p.payment_date,p.reference_no,p.notes from payments p join payment_methods m on m.id=p.payment_method_id where p.purchase_order_id=? and p.is_active=1 order by p.payment_date desc",id));return "payments/order-history";
    }
}
