package com.bunlysem.purchasing.modules.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Integer isActive;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getIsActive() { return isActive; }
}
