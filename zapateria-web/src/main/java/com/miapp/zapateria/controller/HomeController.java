package com.miapp.zapateria.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.miapp.zapateria.model.User;
import com.miapp.zapateria.repository.UserRepository;

@Controller
public class HomeController {

	private final UserRepository userRepository;

	public HomeController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@GetMapping("/")
	public String home(Principal principal, Model model) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
		model.addAttribute("user", user);
		return "home";
	}
}
