package com.bunlysem.purchasing.modules.paymentmethods.repository;

import com.bunlysem.purchasing.modules.paymentmethods.model.PaymentMethod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByIsActiveOrderByNameAsc(Integer active);
    Optional<PaymentMethod> findByIdAndIsActive(Long id, Integer active);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
