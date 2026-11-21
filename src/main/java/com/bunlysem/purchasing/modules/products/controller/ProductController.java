package com.bunlysem.purchasing.modules.products.controller;

import com.bunlysem.purchasing.modules.products.model.Product;
import com.bunlysem.purchasing.modules.products.service.ProductService;
import java.io.IOException;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/products")
public class ProductController {
    private final JdbcTemplate jdbc;
    private final ProductService products;

    public ProductController(JdbcTemplate jdbc, ProductService products) {
        this.jdbc = jdbc; this.products = products;
    }

    @GetMapping
    String list(Model model) {
        model.addAttribute("activeCode", "PRODUCTS");
        model.addAttribute("products", jdbc.query("""
            select p.id,p.sku,p.name,p.unit,c.name category,
                   case when p.image_data is null then 0 else 1 end has_image
            from products p join categories c on c.id=p.category_id
            where p.is_active=1 order by p.name
            """, (rs, row) -> new ProductRow(rs.getLong(1), rs.getString(2), rs.getString(3),
                rs.getString(4), rs.getString(5), rs.getInt(6) == 1)));
        return "products/index";
    }

    public record ProductRow(Long id, String sku, String name, String unit, String category, boolean hasImage) { }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    String tooLarge(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Image must be 5 MB or smaller.");
        return "redirect:/products/new";
    }

    @GetMapping("/new")
    String create(Model model) { return form(model, null); }

    @GetMapping("/{id}/edit")
    String edit(@PathVariable Long id, Model model) { return form(model, products.get(id)); }

    private String form(Model model, Product product) {
        model.addAttribute("activeCode", "PRODUCTS");
        model.addAttribute("product", product);
        model.addAttribute("categories", jdbc.query("select id,name from categories where is_active=1 order by name",
            (rs, row) -> new CategoryRow(rs.getLong(1), rs.getString(2))));
        return "products/form";
    }
    public record CategoryRow(Long id, String name) { }

    @PostMapping
    String create(@RequestParam Long categoryId, @RequestParam String sku, @RequestParam String name,
                  @RequestParam String unit, @RequestParam(required = false) String description,
                  @RequestParam(required = false) MultipartFile image, RedirectAttributes redirect, Model model) throws IOException {
        return save(null, categoryId, sku, name, unit, description, image, redirect, model);
    }

    @PostMapping("/{id}")
    String update(@PathVariable Long id, @RequestParam Long categoryId, @RequestParam String sku, @RequestParam String name,
                  @RequestParam String unit, @RequestParam(required = false) String description,
                  @RequestParam(required = false) MultipartFile image, RedirectAttributes redirect, Model model) throws IOException {
        return save(id, categoryId, sku, name, unit, description, image, redirect, model);
    }

    private String save(Long id, Long categoryId, String sku, String name, String unit, String description,
                        MultipartFile image, RedirectAttributes redirect, Model model) throws IOException {
        try {
            if (sku.isBlank() || name.isBlank() || unit.isBlank()) throw new IllegalArgumentException("SKU, name and unit are required.");
            products.save(id, categoryId, sku, name, unit, description, image);
            redirect.addFlashAttribute("success", "Product saved.");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return form(model, id == null ? null : products.get(id));
        }
    }

    @PostMapping("/{id}/deactivate")
    String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        products.deactivate(id); redirect.addFlashAttribute("success", "Product deactivated."); return "redirect:/products";
    }

    @GetMapping("/{id}/image")
    ResponseEntity<byte[]> image(@PathVariable Long id) {
        Product product = products.get(id);
        if (product.getImageData() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(product.getImageContentType()))
            .cacheControl(CacheControl.noStore()).body(product.getImageData());
    }
}
