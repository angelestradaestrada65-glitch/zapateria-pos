package com.miapp.zapateria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miapp.zapateria.dto.ProductDetailFormDTO;
import com.miapp.zapateria.dto.ProductFormDTO;
import com.miapp.zapateria.service.ProductService;

import jakarta.validation.Valid;

@Controller
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/admin/products")
	public String list(Model model) {
		model.addAttribute("products", productService.listAll());
		return "products/list";
	}

	@GetMapping("/admin/products/new")
	public String newForm(Model model) {
		model.addAttribute("productForm", new ProductFormDTO());
		model.addAttribute("categories", productService.listCategories());
		return "products/form";
	}

	@PostMapping("/admin/products")
	public String create(@Valid @ModelAttribute("productForm") ProductFormDTO productForm, BindingResult result,
			Model model) {
		if (result.hasErrors()) {
			model.addAttribute("categories", productService.listCategories());
			return "products/form";
		}
		productService.createProduct(productForm);
		return "redirect:/admin/products";
	}

	@GetMapping("/admin/products/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("productForm", toForm(productService.findById(id)));
		model.addAttribute("categories", productService.listCategories());
		model.addAttribute("productId", id);

		return "products/form";
	}

	@PostMapping("/admin/products/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("productForm") ProductFormDTO productForm,
			BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("categories", productService.listCategories());
			model.addAttribute("productId", id);
			return "products/form";
		}
		productService.updateProduct(id, productForm);
		return "redirect:/admin/products";
	}

	@PostMapping("/admin/products/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			productService.deleteProduct(id);
			redirectAttributes.addFlashAttribute("mensaje", "Producto borrado");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/admin/products/{id}/variants")
	public String variants(@PathVariable Long id, Model model) {
		model.addAttribute("product", productService.findById(id));
		model.addAttribute("variants", productService.listVariants(id));
		model.addAttribute("variantForm", new ProductDetailFormDTO());
		return "products/variants";
	}

	@PostMapping("/admin/products/{id}/variants")
	public String addVariant(@PathVariable Long id,
			@Valid @ModelAttribute("variantForm") ProductDetailFormDTO variantForm, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("product", productService.findById(id));
			model.addAttribute("variants", productService.listVariants(id));
			return "products/variants";
		}
		try {
			productService.addVariant(id, variantForm);
			redirectAttributes.addFlashAttribute("mensaje", "Variante agregada");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/products/" + id + "/variants";
	}

	@PostMapping("/admin/products/variants/{variantId}/delete")
	public String deleteVariant(@PathVariable Long variantId, @RequestParam Long productId,
			RedirectAttributes redirectAttributes) {
		try {
			productService.deleteVariant(variantId);
			redirectAttributes.addFlashAttribute("mensaje", "Variante borrada");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/admin/products/" + productId + "/variants";
	}

	@PostMapping("/admin/products/{id}/active")
	public String setActive(@PathVariable Long id, @RequestParam boolean active,
			RedirectAttributes redirectAttributes) {
		productService.setActive(id, active);
		redirectAttributes.addFlashAttribute("mensaje", active ? "Producto activado" : "Producto desacticado");
		return "redirect:/admin/products";
	}

	private ProductFormDTO toForm(com.miapp.zapateria.model.Product product) {
		ProductFormDTO form = new ProductFormDTO();
		form.setName(product.getName());
		form.setBrand(product.getBrand());
		form.setCategoryId(product.getCategory().getId());
		return form;
	}
}
