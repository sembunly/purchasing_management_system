package com.bunlysem.purchasing.modules.settings.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modules")
public class AppModule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    @Column(name = "route_path") private String routePath;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "is_active") private Integer isActive;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "module_type_id")
    private ModuleType moduleType;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public ModuleType getModuleType() { return moduleType; }
}
