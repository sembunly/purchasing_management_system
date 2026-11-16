package com.bunlysem.purchasing.modules.products.service;

import com.bunlysem.purchasing.modules.products.model.Product;
import com.bunlysem.purchasing.modules.products.repository.ProductRepository;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service @Transactional
public class ProductService {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private final ProductRepository products;
    private final JdbcTemplate jdbc;

    public ProductService(ProductRepository products, JdbcTemplate jdbc) {
        this.products = products; this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return products.findByIdAndIsActive(id, 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
    }

    public Product save(Long id, Long categoryId, String sku, String name, String unit,
                        String description, MultipartFile image) throws IOException {
        Product product = id == null ? new Product() : get(id);
        if (categoryId == null || jdbc.queryForObject(
            "select count(*) from categories where id=? and is_active=1", Integer.class, categoryId) == 0)
            throw new IllegalArgumentException("Choose an active category.");
        if ((id == null && products.existsBySku(sku.trim()))
            || (id != null && products.existsBySkuAndIdNot(sku.trim(), id)))
            throw new IllegalArgumentException("SKU already exists.");
        product.setCategoryId(categoryId);
        product.setSku(sku.trim());
        product.setName(name.trim());
        product.setUnit(unit.trim());
        product.setDescription(description == null ? null : description.trim());
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Image must be 5 MB or smaller.");
            byte[] bytes = image.getBytes();
            String contentType = detectImage(bytes);
            if (contentType == null || ImageIO.read(new ByteArrayInputStream(bytes)) == null)
                throw new IllegalArgumentException("Upload a valid JPG or PNG image.");
            product.setImageData(bytes);
            product.setImageContentType(contentType);
            product.setImageFilename(contentType.equals("image/png") ? "product.png" : "product.jpg");
        }
        return products.save(product);
    }

    public void deactivate(Long id) { get(id).setIsActive(0); }

    private String detectImage(byte[] bytes) {
        if (bytes.length >= 8 && Arrays.equals(Arrays.copyOf(bytes, 8),
            new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a})) return "image/png";
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
            && (bytes[2] & 0xff) == 0xff) return "image/jpeg";
        return null;
    }
}
