package com.miapp.zapateria.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.miapp.zapateria.model.Role;
import com.miapp.zapateria.repository.RoleRepository;
import com.miapp.zapateria.repository.UserRepository;
import com.miapp.zapateria.service.UserService;

@Configuration
public class DataSeeder {

	/*@Bean
	public CommandLineRunner seedData(RoleRepository roleRepository, UserRepository userRepository,
			UserService userService) {
		return args -> {
			// Crear los 3 roles si no existen
			for (String roleName : new String[] { "SYSADMIN", "ADMIN", "USER" }) {
				if (roleRepository.findByName(roleName).isEmpty()) {
					Role role = new Role();
					role.setName(roleName);
					roleRepository.save(role);
					System.out.println(">>> Rol creado: " + roleName);
				}
			}

			// Crear el usuario SYSADMIN si no existe
			if (userRepository.findByEmail("sysadmin").isEmpty()) {
				Role sysadminRole = roleRepository.findByName("SYSADMIN").orElseThrow();
				userService.registerUser("Dueño", "sysadmin@gmail.com", "luffy500*", sysadminRole.getId());
				System.out.println(">>> Usuario SYSADMIN creado: sysadmin@gmail.com");
			}
		};
	}
	*/
}
