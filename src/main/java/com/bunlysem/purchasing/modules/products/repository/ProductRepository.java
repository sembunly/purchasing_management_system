package com.bunlysem.purchasing.modules.products.repository;

import com.bunlysem.purchasing.modules.products.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndIsActive(Long id, Integer isActive);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
}
