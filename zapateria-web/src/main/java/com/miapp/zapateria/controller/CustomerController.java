package com.miapp.zapateria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miapp.zapateria.dto.CustomerFormDTO;
import com.miapp.zapateria.model.Customer;
import com.miapp.zapateria.service.CustomerService;

import jakarta.validation.Valid;

@Controller
public class CustomerController {

	public final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping("/customers")
	public String list(Model model) {
		model.addAttribute("customers", customerService.listAll());
		return "customers/list";
	}

	@GetMapping("/customers/new")
	public String newForm(Model model) {
		model.addAttribute("customerForm", new CustomerFormDTO());
		return "customers/form";
	}

	@PostMapping("/customers")
	public String create(@Valid @ModelAttribute("customerForm") CustomerFormDTO customerForm, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "customers/form";
		}
		try {
			customerService.createCustomer(customerForm);
			redirectAttributes.addFlashAttribute("mensaje", "Cliente guardado");
			return "redirect:/customers";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/customers/new";
		}
	}

	@GetMapping("/customers/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("customerForm", toForm(customerService.findById(id)));
		model.addAttribute("customerId", id);
		return "customers/form";
	}

	@PostMapping("/customers/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("customerForm") CustomerFormDTO customerForm,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("customerId", id);
			return "customers/form";
		}
		try {
			customerService.updateCustomer(id, customerForm);
			redirectAttributes.addFlashAttribute("mensaje", "Cliente actualizado");
			return "redirect:/customers";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/customers/" + id + "/edit";
		}
	}

	@PostMapping("/customers/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			customerService.deleteCustomer(id);
			redirectAttributes.addFlashAttribute("mensaje", "Cliente borrado");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/customers";
	}

	private CustomerFormDTO toForm(Customer customer) {
		CustomerFormDTO form = new CustomerFormDTO();
		form.setName(form.getName());
		form.setPhone(form.getPhone());
		form.setCredit(form.isCredit());

		return form;
	}
}
