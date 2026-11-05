package com.miapp.zapateria.controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miapp.zapateria.dto.SaleFormDTO;
import com.miapp.zapateria.dto.SaleItemDTO;
import com.miapp.zapateria.model.ProductDetail;
import com.miapp.zapateria.model.Sale;
import com.miapp.zapateria.model.User;
import com.miapp.zapateria.repository.ProductDetailRepository;
import com.miapp.zapateria.repository.UserRepository;
import com.miapp.zapateria.service.CustomerService;
import com.miapp.zapateria.service.SaleService;

@Controller
public class SaleController {

	private final ProductDetailRepository productDetailRepository;
	private final UserRepository userRepository;
	private final SaleService saleService;
	private final CustomerService customerService;

	public SaleController(ProductDetailRepository productDetailRepository, UserRepository userRepository,
			SaleService saleService, CustomerService customerService) {
		this.productDetailRepository = productDetailRepository;
		this.userRepository = userRepository;
		this.saleService = saleService;
		this.customerService = customerService;
	}

	@GetMapping("/sales")
	public String showForm(Model model) {
		model.addAttribute("saleForm", new SaleFormDTO());
		loadScreenData(model);
		return "sales/new";
	}

	@PostMapping("/sales")
	public String registerSale(@ModelAttribute("saleForm") SaleFormDTO form, Principal principal, Model model,
			RedirectAttributes redirectAttributes) {

		User seller = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

		List<SaleItemDTO> itemsToSell = form.getItems() == null ? List.of()
				: form.getItems().stream().filter(i -> i.getQuantity() != null && i.getQuantity() > 0).toList();

		try {
			Sale sale = saleService.registerSale(seller, form.getCustomerId(), itemsToSell, form.getAmountPaid(),
					form.isCredit());
			redirectAttributes.addFlashAttribute("mensaje", "Venta registrada con éxito. Total: $" + sale.getTotal());
			return "redirect:/sales";
		} catch (IllegalArgumentException e) {
			// Se vuelve a mostrar la pantalla con lo que ya había cargado (cliente, fiado,
			// anticipo)
			model.addAttribute("error", e.getMessage());
			loadScreenData(model);
			return "sales/new";
		}
	}

	private void loadScreenData(Model model) {
		List<ProductDetail> availableDetails = productDetailRepository
				.findAll().stream().filter(d -> d.getStock() > 0).sorted(Comparator
						.comparing((ProductDetail d) -> d.getProduct().getName()).thenComparing(ProductDetail::getSize))
				.toList();

		model.addAttribute("availableDetails", availableDetails);
		model.addAttribute("customers", customerService.listAll());
		model.addAttribute("discount", saleService.getDiscountPerUnit());
	}

}
