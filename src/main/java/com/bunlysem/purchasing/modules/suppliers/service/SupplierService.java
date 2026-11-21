package com.bunlysem.purchasing.modules.suppliers.service;

import com.bunlysem.purchasing.modules.suppliers.model.Supplier;
import com.bunlysem.purchasing.modules.suppliers.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SupplierService {
    private final SupplierRepository suppliers;

    public SupplierService(SupplierRepository suppliers) { this.suppliers = suppliers; }

    @Transactional(readOnly = true)
    public Supplier get(Long id) {
        return suppliers.findByIdAndIsActive(id, 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found."));
    }

    public Supplier save(Long id, String name, String contactName, String phone, String email, String address) {
        String cleanName = clean(name);
        if (cleanName == null) throw new IllegalArgumentException("Supplier name is required.");
        String cleanEmail = clean(email);
        if (cleanEmail != null && !cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Enter a valid email address.");

        Supplier supplier = id == null ? new Supplier() : get(id);
        supplier.setName(cleanName);
        supplier.setContactName(clean(contactName));
        supplier.setPhone(clean(phone));
        supplier.setEmail(cleanEmail);
        supplier.setAddress(clean(address));
        return suppliers.save(supplier);
    }

    public void deactivate(Long id) { get(id).setIsActive(0); }

    private String clean(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
