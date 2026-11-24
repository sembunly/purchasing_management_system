package com.bunlysem.purchasing.modules.requests.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
public class PurchaseRequestController {
    private final JdbcTemplate jdbc;
    public PurchaseRequestController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record RequestRow(Long id, String number, String department, String requestedBy, String status) { }
    public record ProductOption(Long id, String sku, String name) { }
    public record Line(Long productId, String product, BigDecimal quantity, BigDecimal price, String notes) { }
    public record DepartmentOption(Long id, String name) { }

    @GetMapping @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','VIEW')") String list(@RequestParam(defaultValue="") String search, Model model) {
        model.addAttribute("activeCode", "REQUESTS"); model.addAttribute("search", search);
        model.addAttribute("requests", jdbc.query("""
          select pr.id,pr.request_no,d.name,u.full_name,pr.status from purchase_requests pr
          join departments d on d.id=pr.department_id join users u on u.id=pr.requested_by
          where pr.is_active=1 and lower(pr.request_no) like lower(?) order by pr.request_date desc
          """, (rs,n)->new RequestRow(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)), "%"+search.trim()+"%"));
        return "requests/index";
    }
    @GetMapping("/new") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','CREATE')") String createForm(Model model) { return form(model,null); }
    @GetMapping("/{id}") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','VIEW')") String details(@PathVariable Long id, Model model) {
        Map<String,Object> request = header(id); model.addAttribute("activeCode","REQUESTS"); model.addAttribute("request",request);
        model.addAttribute("lines", lines(id)); return "requests/details";
    }
    @GetMapping("/{id}/edit") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','EDIT')") String editForm(@PathVariable Long id, Model model) { return form(model,header(id)); }

    @PostMapping @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','CREATE')") String create(@RequestParam Long departmentId, @RequestParam String status,
        @RequestParam(required=false) String notes, @RequestParam(required=false) List<Long> productIds,
        @RequestParam(required=false) List<BigDecimal> quantities, @RequestParam(required=false) List<BigDecimal> prices,
        Authentication auth, Model model, RedirectAttributes redirect) {
        return save(null,departmentId,status,notes,productIds,quantities,prices,auth,model,redirect);
    }
    @PostMapping("/{id}") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','EDIT')") String update(@PathVariable Long id,@RequestParam Long departmentId,@RequestParam String status,
        @RequestParam(required=false) String notes,@RequestParam(required=false) List<Long> productIds,
        @RequestParam(required=false) List<BigDecimal> quantities,@RequestParam(required=false) List<BigDecimal> prices,
        Authentication auth,Model model,RedirectAttributes redirect) {
        return save(id,departmentId,status,notes,productIds,quantities,prices,auth,model,redirect);
    }
    private String save(Long id,Long departmentId,String status,String notes,List<Long> pids,List<BigDecimal> qtys,List<BigDecimal> prices,Authentication auth,Model model,RedirectAttributes redirect) {
        try {
            if (!List.of("DRAFT","PENDING").contains(status)) throw new IllegalArgumentException("Status must be DRAFT or PENDING.");
            if (jdbc.queryForObject("select count(*) from departments where id=? and is_active=1",Integer.class,departmentId)==0) throw new IllegalArgumentException("Choose an active department.");
            if (pids==null||pids.isEmpty()||qtys==null||prices==null||pids.size()!=qtys.size()||pids.size()!=prices.size()) throw new IllegalArgumentException("Add at least one complete product line.");
            Set<Long> unique=new HashSet<>();
            for(int i=0;i<pids.size();i++) {
                if(!unique.add(pids.get(i))) throw new IllegalArgumentException("Each product can appear only once.");
                if(qtys.get(i)==null||qtys.get(i).compareTo(BigDecimal.ZERO)<=0||prices.get(i)==null||prices.get(i).compareTo(BigDecimal.ZERO)<0) throw new IllegalArgumentException("Quantities must be positive and prices cannot be negative.");
                if(jdbc.queryForObject("select count(*) from products where id=? and is_active=1",Integer.class,pids.get(i))==0) throw new IllegalArgumentException("Select active products only.");
            }
            String user=auth.getName(); Long userId=jdbc.queryForObject("select id from users where username=? and is_active=1",Long.class,user);
            if(userId==null) throw new IllegalArgumentException("Current user is unavailable.");
            Long requestId=id;
            if(id==null) {
                String number="PR-"+System.currentTimeMillis();
                jdbc.update("insert into purchase_requests(request_no,department_id,requested_by,status,notes,is_active) values(?,?,?,? ,?,1)",number,departmentId,userId,status,notes);
                requestId=jdbc.queryForObject("select id from purchase_requests where request_no=?",Long.class,number);
            } else {
                Map<String,Object> old=header(id);
                if(!List.of("DRAFT","PENDING").contains(old.get("status"))) throw new IllegalArgumentException("Only DRAFT or PENDING requests can be edited.");
                jdbc.update("update purchase_requests set department_id=?,status=?,notes=? where id=? and is_active=1",departmentId,status,notes,id);
                jdbc.update("update purchase_request_details set is_active=0 where purchase_request_id=?",id);
            }
            for(int i=0;i<pids.size();i++) jdbc.update("insert into purchase_request_details(purchase_request_id,product_id,quantity,estimated_unit_price,is_active) values(?,?,?,?,1)",requestId,pids.get(i),qtys.get(i),prices.get(i));
            redirect.addFlashAttribute("success","Purchase request saved."); return "redirect:/requests";
        } catch(IllegalArgumentException ex) {
            model.addAttribute("error",ex.getMessage()); Map<String,Object> values=new HashMap<>(); values.put("id",id);values.put("departmentId",departmentId);values.put("status",status);values.put("notes",notes);
            model.addAttribute("request",values); model.addAttribute("selectedProductIds",pids==null?List.of():pids);model.addAttribute("quantities",qtys==null?List.of():qtys);model.addAttribute("prices",prices==null?List.of():prices); return form(model,values);
        }
    }
    @PostMapping("/{id}/deactivate") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','DELETE')") String deactivate(@PathVariable Long id,RedirectAttributes redirect) {
        Map<String,Object> request=header(id); if(!List.of("DRAFT","PENDING").contains(request.get("status"))) { redirect.addFlashAttribute("error","Only DRAFT or PENDING requests can be deleted."); return "redirect:/requests"; }
        jdbc.update("update purchase_requests set is_active=0 where id=?",id); jdbc.update("update purchase_request_details set is_active=0 where purchase_request_id=?",id); redirect.addFlashAttribute("success","Purchase request deactivated."); return "redirect:/requests";
    }
    @PostMapping("/{id}/approve") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','APPROVE')") String approve(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        int changed=jdbc.update("update purchase_requests set status='APPROVED', approved_by=(select id from users where username=? and is_active=1), approved_at=current_timestamp where id=? and is_active=1 and status='PENDING'",auth.getName(),id);
        redirect.addFlashAttribute(changed==1?"success":"error",changed==1?"Request approved.":"Only PENDING requests can be approved.");
        return "redirect:/requests/"+id;
    }
    @PostMapping("/{id}/reject") @PreAuthorize("@permissionService.hasPermission(authentication,'REQUESTS','APPROVE')") String reject(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirect) {
        String clean=reason==null?"":reason.trim();
        if(clean.isBlank()||clean.length()>500) { redirect.addFlashAttribute("error","Enter a rejection reason of up to 500 characters."); return "redirect:/requests/"+id; }
        int changed=jdbc.update("update purchase_requests set status='REJECTED', notes=? where id=? and is_active=1 and status='PENDING'", "Rejection reason: "+clean,id);
        redirect.addFlashAttribute(changed==1?"success":"error",changed==1?"Request rejected.":"Only PENDING requests can be rejected.");
        return "redirect:/requests/"+id;
    }
    private Map<String,Object> header(Long id) {
        return jdbc.query("select pr.id,pr.request_no,pr.department_id,d.name department,pr.status,pr.notes,u.full_name requested_by,au.full_name approved_by,pr.approved_at from purchase_requests pr join departments d on d.id=pr.department_id join users u on u.id=pr.requested_by left join users au on au.id=pr.approved_by where pr.id=? and pr.is_active=1",rs->{if(!rs.next()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);Map<String,Object> m=new HashMap<>();m.put("id",rs.getLong(1));m.put("number",rs.getString(2));m.put("departmentId",rs.getLong(3));m.put("department",rs.getString(4));m.put("status",rs.getString(5));m.put("notes",rs.getString(6));m.put("requestedBy",rs.getString(7));m.put("approvedBy",rs.getString(8));m.put("approvedAt",rs.getTimestamp(9));return m;},id);
    }
    private List<Line> lines(Long id) { return jdbc.query("select prd.product_id,p.name,prd.quantity,prd.estimated_unit_price,prd.notes from purchase_request_details prd join products p on p.id=prd.product_id where prd.purchase_request_id=? and prd.is_active=1",(rs,n)->new Line(rs.getLong(1),rs.getString(2),rs.getBigDecimal(3),rs.getBigDecimal(4),rs.getString(5)),id); }
    private String form(Model model,Map<String,Object> request) {
        model.addAttribute("activeCode","REQUESTS");model.addAttribute("request",request);
        model.addAttribute("departments",jdbc.query("select id,name from departments where is_active=1 order by name",(rs,n)->new DepartmentOption(rs.getLong(1),rs.getString(2))));
        model.addAttribute("products",jdbc.query("select id,sku,name from products where is_active=1 order by name",(rs,n)->new ProductOption(rs.getLong(1),rs.getString(2),rs.getString(3))));
        model.addAttribute("lines",request!=null&&request.get("id") instanceof Long?lines((Long)request.get("id")):List.of());return "requests/form";
    }
}
