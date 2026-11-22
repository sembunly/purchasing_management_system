package com.bunlysem.purchasing.modules.suppliers.controller;

import com.bunlysem.purchasing.modules.suppliers.model.Supplier;
import com.bunlysem.purchasing.modules.suppliers.repository.SupplierRepository;
import com.bunlysem.purchasing.modules.suppliers.service.SupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {
    private final SupplierRepository suppliers;
    private final SupplierService supplierService;

    public SupplierController(SupplierRepository suppliers, SupplierService supplierService) {
        this.suppliers = suppliers;
        this.supplierService = supplierService;
    }

    @GetMapping
    String list(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("activeCode", "SUPPLIERS");
        model.addAttribute("search", search);
        model.addAttribute("suppliers", suppliers.findByIsActiveAndNameContainingIgnoreCaseOrderByNameAsc(1, search.trim()));
        return "suppliers/index";
    }

    @GetMapping("/new") String createForm(Model model) { return form(model, new Supplier()); }
    @GetMapping("/{id}") String details(@PathVariable Long id, Model model) {
        model.addAttribute("activeCode", "SUPPLIERS");
        model.addAttribute("supplier", supplierService.get(id));
        return "suppliers/details";
    }
    @GetMapping("/{id}/edit") String editForm(@PathVariable Long id, Model model) {
        return form(model, supplierService.get(id));
    }

    @PostMapping String create(@RequestParam String name, @RequestParam(required=false) String contactName,
            @RequestParam(required=false) String phone, @RequestParam(required=false) String email,
            @RequestParam(required=false) String address, RedirectAttributes redirect, Model model) {
        return save(null, name, contactName, phone, email, address, redirect, model);
    }
    @PostMapping("/{id}") String update(@PathVariable Long id, @RequestParam String name,
            @RequestParam(required=false) String contactName, @RequestParam(required=false) String phone,
            @RequestParam(required=false) String email, @RequestParam(required=false) String address,
            RedirectAttributes redirect, Model model) {
        return save(id, name, contactName, phone, email, address, redirect, model);
    }
    @PostMapping("/{id}/deactivate") String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        supplierService.deactivate(id);
        redirect.addFlashAttribute("success", "Supplier deactivated.");
        return "redirect:/suppliers";
    }

    private String save(Long id, String name, String contactName, String phone, String email, String address,
                        RedirectAttributes redirect, Model model) {
        try {
            supplierService.save(id, name, contactName, phone, email, address);
            redirect.addFlashAttribute("success", "Supplier saved.");
            return "redirect:/suppliers";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            Supplier supplier = id == null ? new Supplier() : supplierService.get(id);
            supplier.setName(name); supplier.setContactName(contactName); supplier.setPhone(phone);
            supplier.setEmail(email); supplier.setAddress(address);
            return form(model, supplier);
        }
    }
    private String form(Model model, Supplier supplier) {
        model.addAttribute("activeCode", "SUPPLIERS");
        model.addAttribute("supplier", supplier);
        return "suppliers/form";
    }
}
