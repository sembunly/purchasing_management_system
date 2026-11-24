package com.bunlysem.purchasing.modules.settings.model;

import jakarta.persistence.*;

@Entity
@Table(name = "module_types")
public class ModuleType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "is_active") private Integer isActive;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
