package com.miapp.zapateria.controller;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miapp.zapateria.model.SaleState;
import com.miapp.zapateria.model.User;
import com.miapp.zapateria.repository.UserRepository;
import com.miapp.zapateria.service.SaleService;

@Controller
public class SaleHistoryController {

	private final SaleService saleService;
	private final UserRepository userRepository;

	public SaleHistoryController(SaleService saleService, UserRepository userRepository) {
		this.saleService = saleService;
		this.userRepository = userRepository;
	}

	@GetMapping("/sales/history")
	public String history(@RequestParam(required = false) SaleState state, Model model) {
		model.addAttribute("sales", state == null ? saleService.listAll() : saleService.listByState(state));
		model.addAttribute("state", state);
		return "sales/history";
	}

	@GetMapping("/sales/{id}")
	public String detail(@PathVariable Long id, Model model) {
		model.addAttribute("sale", saleService.findById(id));
		return "sales/detail";
	}

	@PostMapping("/sales/{id}/payment")
	public String registerPayment(@PathVariable Long id, @RequestParam BigDecimal amount, Principal principal,
			RedirectAttributes redirectAttributes) {
		try {
			User receiver = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
			saleService.registerPayment(id, amount, receiver);
			redirectAttributes.addFlashAttribute("mensaje", "Abono registrado");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/sales/" + id;
	}

	@PostMapping("/admin/sales/{id}/cancel")
	public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			saleService.cancelSale(id);
			redirectAttributes.addFlashAttribute("mensaje", "Venta cancelada y stock devuelto");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/sales/" + id;
	}
}
