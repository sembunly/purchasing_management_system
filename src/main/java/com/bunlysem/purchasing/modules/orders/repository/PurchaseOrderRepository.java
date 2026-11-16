package com.bunlysem.purchasing.modules.orders.repository;

import com.bunlysem.purchasing.modules.orders.model.PurchaseOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdAndIsActive(Long id, Integer isActive);
}
