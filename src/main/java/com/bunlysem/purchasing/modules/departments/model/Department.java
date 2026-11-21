package com.bunlysem.purchasing.modules.departments.model;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120, unique = true)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getIsActive() { return isActive; }
    public void setName(String value) { name = value; }
    public void setDescription(String value) { description = value; }
    public void setIsActive(Integer value) { isActive = value; }
}
