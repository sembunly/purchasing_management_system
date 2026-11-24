package com.bunlysem.purchasing.modules.settings.model;

import com.bunlysem.purchasing.modules.auth.model.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "module_details")
public class ModulePermission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id")
    private Role role;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "module_id")
    private AppModule module;
    @Column(name = "action_code") private String actionCode;
    @Column(name = "is_allowed") private Integer isAllowed;
    @Column(name = "is_active") private Integer isActive;

    public void setRole(Role role) { this.role = role; }
    public void setModule(AppModule module) { this.module = module; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }
    public void setIsAllowed(Integer isAllowed) { this.isAllowed = isAllowed; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }
    public Integer getIsAllowed() { return isAllowed; }
    public Integer getIsActive() { return isActive; }
    public AppModule getModule() { return module; }
    public String getActionCode() { return actionCode; }
}
