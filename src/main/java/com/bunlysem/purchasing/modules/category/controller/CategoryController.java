package com.bunlysem.purchasing.modules.category.controller;

import com.bunlysem.purchasing.modules.category.model.Category;
import com.bunlysem.purchasing.modules.category.repository.CategoryRepository;
import com.bunlysem.purchasing.modules.category.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryRepository categories;
    private final CategoryService categoryService;

    public CategoryController(CategoryRepository categories, CategoryService categoryService) {
        this.categories = categories;
        this.categoryService = categoryService;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("activeCode", "CATEGORIES");
        model.addAttribute("categories", categories.findByIsActiveOrderByNameAsc(1));
        return "categories/index";
    }

    @GetMapping("/new")
    String createForm(Model model) {
        return form(model, null);
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        return form(model, categoryService.get(id));
    }

    @PostMapping
    String create(@RequestParam String name, @RequestParam(required = false) String description,
                  RedirectAttributes redirect, Model model) {
        return save(null, name, description, redirect, model);
    }

    @PostMapping("/{id}")
    String update(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) String description,
                  RedirectAttributes redirect, Model model) {
        return save(id, name, description, redirect, model);
    }

    @PostMapping("/{id}/deactivate")
    String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            categoryService.deactivate(id);
            redirect.addFlashAttribute("success", "Category deactivated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/categories";
    }

    private String save(Long id, String name, String description, RedirectAttributes redirect, Model model) {
        try {
            categoryService.save(id, name, description);
            redirect.addFlashAttribute("success", "Category saved.");
            return "redirect:/categories";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            Category category = id == null ? null : categoryService.get(id);
            if (category == null) {
                category = new Category();
                category.setName(name);
                category.setDescription(description);
            }
            return form(model, category);
        }
    }

    private String form(Model model, Category category) {
        model.addAttribute("activeCode", "CATEGORIES");
        model.addAttribute("category", category);
        return "categories/form";
    }
}
