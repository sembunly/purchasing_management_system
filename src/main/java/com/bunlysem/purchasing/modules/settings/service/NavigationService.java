package com.bunlysem.purchasing.modules.settings.service;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("navigationService")
public class NavigationService {
    private final JdbcTemplate jdbc;

    public NavigationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record MenuItem(String code, String name, String path) { }
    public record MenuGroup(String name, List<MenuItem> items) { }

    public List<MenuGroup> groups(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return List.of();
        // The matrix is read on each page request so permission changes take effect immediately.
        List<MenuItem> allowed = jdbc.query("""
            SELECT DISTINCT mt.code, mt.name, mt.sort_order, m.code, m.name,
                   m.route_path, m.sort_order
              FROM module_types mt
              JOIN modules m ON m.module_type_id = mt.id
              JOIN module_details md ON md.module_id = m.id
              JOIN roles r ON r.id = md.role_id
              JOIN user_roles ur ON ur.role_id = r.id
              JOIN users u ON u.id = ur.user_id
             WHERE u.username = ? AND u.is_active = 1 AND ur.is_active = 1
               AND r.is_active = 1 AND mt.is_active = 1 AND m.is_active = 1
               AND md.is_active = 1 AND md.is_allowed = 1 AND md.action_code = 'VIEW'
               AND m.route_path IS NOT NULL
             ORDER BY mt.sort_order, mt.name, m.sort_order, m.name
            """, rs -> {
            List<MenuItem> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new MenuItem(rs.getString(4), rs.getString(5), rs.getString(6)));
            }
            return items;
        }, authentication.getName());
        Map<String, List<MenuItem>> groups = new LinkedHashMap<>();
        for (String name : List.of("Main", "Purchasing", "Master Data", "Finance", "Reports", "Settings"))
            groups.put(name, new ArrayList<>());
        for (MenuItem item : allowed) {
            String group = switch (item.code()) {
                case "DASHBOARD" -> "Main";
                case "REQUESTS", "ORDERS", "RECEIPTS" -> "Purchasing";
                case "DEPARTMENTS", "SUPPLIERS", "CATEGORIES", "PRODUCTS" -> "Master Data";
                case "PAYMENTS" -> "Finance";
                default -> "Settings";
            };
            groups.get(group).add(item);
        }
        if (allowed.stream().anyMatch(item -> item.code().equals("REQUESTS")))
            groups.get("Reports").add(new MenuItem("PURCHASING_REPORT", "Purchasing Report", "/reports/purchasing"));
        if (allowed.stream().anyMatch(item -> item.code().equals("RECEIPTS")))
            groups.get("Reports").add(new MenuItem("RECEIVING_REPORT", "Receiving Report", "/reports/receiving"));
        if (allowed.stream().anyMatch(item -> item.code().equals("PAYMENTS")))
            groups.get("Reports").add(new MenuItem("PAYMENT_REPORT", "Payment Report", "/reports/payments"));
        return groups.entrySet().stream().filter(entry -> !entry.getValue().isEmpty())
            .map(entry -> new MenuGroup(entry.getKey(), entry.getValue())).toList();
    }
}
