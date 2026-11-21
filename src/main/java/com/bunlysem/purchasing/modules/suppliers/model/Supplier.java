package com.bunlysem.purchasing.modules.suppliers.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "contact_name", length = 120)
    private String contactName;
    @Column(length = 40)
    private String phone;
    @Column(length = 150)
    private String email;
    @Column(length = 500)
    private String address;
    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContactName() { return contactName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public Integer getIsActive() { return isActive; }
    public void setName(String value) { name = value; }
    public void setContactName(String value) { contactName = value; }
    public void setPhone(String value) { phone = value; }
    public void setEmail(String value) { email = value; }
    public void setAddress(String value) { address = value; }
    public void setIsActive(Integer value) { isActive = value; }
}
