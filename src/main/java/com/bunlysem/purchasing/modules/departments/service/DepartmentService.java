package com.bunlysem.purchasing.modules.departments.service;

import com.bunlysem.purchasing.modules.departments.model.Department;
import com.bunlysem.purchasing.modules.departments.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository departments;
    private final JdbcTemplate jdbc;
    public DepartmentService(DepartmentRepository departments, JdbcTemplate jdbc) {
        this.departments = departments; this.jdbc = jdbc;
    }
    @Transactional(readOnly = true)
    public Department get(Long id) {
        return departments.findByIdAndIsActive(id, 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found."));
    }
    public Department save(Long id, String name, String description) {
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isBlank()) throw new IllegalArgumentException("Department name is required.");
        boolean duplicate = id == null ? departments.existsByNameIgnoreCase(cleanName)
            : departments.existsByNameIgnoreCaseAndIdNot(cleanName, id);
        if (duplicate) throw new IllegalArgumentException("A department with this name already exists.");
        Department department = id == null ? new Department() : get(id);
        department.setName(cleanName);
        department.setDescription(description == null || description.isBlank() ? null : description.trim());
        return departments.save(department);
    }
    public void deactivate(Long id) {
        Department department = get(id);
        Integer referenced = jdbc.queryForObject("select (select count(*) from users where department_id = ?) + (select count(*) from purchase_requests where department_id = ? and is_active = 1) from dual", Integer.class, id, id);
        if (referenced != null && referenced > 0)
            throw new IllegalArgumentException("This department is assigned to users or active purchase requests and cannot be deactivated.");
        department.setIsActive(0);
    }
}
