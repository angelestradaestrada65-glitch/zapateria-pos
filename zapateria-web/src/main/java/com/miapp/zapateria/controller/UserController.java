package com.miapp.zapateria.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miapp.zapateria.dto.UserFormDTO;
import com.miapp.zapateria.model.Role;
import com.miapp.zapateria.model.User;
import com.miapp.zapateria.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/sysadmin/users")
	public String list(Model model) {
		model.addAttribute("users", userService.listAll());
		return "users/list";
	}

	@GetMapping("/sysadmin/users/new")
	public String newForm(Model model) {
		UserFormDTO form = new UserFormDTO();
		// Por seguridad el rol que viene preseleccionado es el de vendedor
		form.setRoleId(userService.listRoles().stream().filter(r -> r.getName().equals("USER")).findFirst()
				.map(Role::getId).orElse(null));

		model.addAttribute("userForm", form);
		model.addAttribute("roles", userService.listRoles());
		return "users/form";

	}

	@PostMapping("/sysadmin/users")
	public String create(@Valid @ModelAttribute("userForm") UserFormDTO userForm, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("roles", userService.listRoles());
			return "users/form";
		}
		try {
			userService.registerUser(userForm.getName(), userForm.getEmail(), userForm.getPassword(),
					userForm.getRoleId());
			redirectAttributes.addFlashAttribute("mensaje", "Usuario creado");
			return "redirect:/sysadmin/users";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/sysadmin/users/new";
		}
	}

	@GetMapping("/sysadmin/users/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("userForm", toForm(userService.findById(id)));
		model.addAttribute("roles", userService.listRoles());
		model.addAttribute("userId", id);
		return "users/form";
	}

	@PostMapping("/sysadmin/users/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("userForm") UserFormDTO userForm,
			BindingResult result, Principal principal, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("roles", userService.listRoles());
			model.addAttribute("userId", id);
			return "users/form";
		}
		try {
			userService.updateUser(id, userForm, principal.getName());
			redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado");
			return "redirect:/sysadmin/users";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/sysadmin/users/" + id + "/edit";
		}
	}

	private UserFormDTO toForm(User user) {
		UserFormDTO form = new UserFormDTO();
		form.setName(user.getName());
		form.setEmail(user.getEmail());
		form.setRoleId(user.getRole().getId());
		form.setActive(user.isActive());
		return form;
	}
}
