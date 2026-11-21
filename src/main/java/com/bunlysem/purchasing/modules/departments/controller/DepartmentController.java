package com.bunlysem.purchasing.modules.departments.controller;

import com.bunlysem.purchasing.modules.departments.model.Department;
import com.bunlysem.purchasing.modules.departments.repository.DepartmentRepository;
import com.bunlysem.purchasing.modules.departments.service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentRepository departments;
    private final DepartmentService service;
    public DepartmentController(DepartmentRepository departments, DepartmentService service) {
        this.departments = departments; this.service = service;
    }
    @GetMapping String list(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("activeCode", "DEPARTMENTS"); model.addAttribute("search", search);
        model.addAttribute("departments", departments.findByIsActiveAndNameContainingIgnoreCaseOrderByNameAsc(1, search.trim()));
        return "departments/index";
    }
    @GetMapping("/new") String createForm(Model model) { return form(model, null); }
    @GetMapping("/{id}/edit") String editForm(@PathVariable Long id, Model model) { return form(model, service.get(id)); }
    @PostMapping String create(@RequestParam String name, @RequestParam(required = false) String description,
            RedirectAttributes redirect, Model model) { return save(null, name, description, redirect, model); }
    @PostMapping("/{id}") String update(@PathVariable Long id, @RequestParam String name,
            @RequestParam(required = false) String description, RedirectAttributes redirect, Model model) {
        return save(id, name, description, redirect, model);
    }
    @PostMapping("/{id}/deactivate") String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        try { service.deactivate(id); redirect.addFlashAttribute("success", "Department deactivated."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/departments";
    }
    private String save(Long id, String name, String description, RedirectAttributes redirect, Model model) {
        try { service.save(id, name, description); redirect.addFlashAttribute("success", "Department saved."); return "redirect:/departments"; }
        catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            Department department = id == null ? new Department() : service.get(id);
            department.setName(name); department.setDescription(description);
            return form(model, department);
        }
    }
    private String form(Model model, Department department) {
        model.addAttribute("activeCode", "DEPARTMENTS"); model.addAttribute("department", department);
        return "departments/form";
    }
}
