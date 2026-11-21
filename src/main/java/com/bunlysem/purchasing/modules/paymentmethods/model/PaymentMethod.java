package com.bunlysem.purchasing.modules.paymentmethods.model;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 40, unique = true)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Integer getIsActive() { return isActive; }
    public void setCode(String value) { code = value; }
    public void setName(String value) { name = value; }
    public void setIsActive(Integer value) { isActive = value; }
}
