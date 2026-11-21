package com.bunlysem.purchasing.modules.dashboard.controller;

import com.bunlysem.purchasing.modules.dashboard.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping({"/", "/dashboard"})
    String dashboard(Model model) {
        model.addAttribute("activeCode", "DASHBOARD");
        model.addAttribute("totals", dashboardService.totals());
        model.addAttribute("requestStatuses", dashboardService.requestStatuses());
        model.addAttribute("departmentSpend", dashboardService.departmentSpend());
        model.addAttribute("recentActivity", dashboardService.recentActivity());

        return "dashboard/index";
    }
}
