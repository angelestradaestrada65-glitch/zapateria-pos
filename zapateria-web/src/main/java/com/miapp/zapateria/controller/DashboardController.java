package com.miapp.zapateria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.miapp.zapateria.service.DashboardService;

@Controller
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
	@GetMapping("/sysadmin/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("dashboard", dashboardService.build());
		return "sysadmin/dashboard";
	}
}
