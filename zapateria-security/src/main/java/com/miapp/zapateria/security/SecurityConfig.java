package com.miapp.zapateria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	public SecurityConfig(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		.authorizeHttpRequests(auth -> auth
				// Rutas publicas solo login y archivos estaticos
				.requestMatchers("/login", "/error", "/css/**", "/js/**", "/imagenes/**").permitAll()
				
				// Reportes y gestion de usuarios solo el dueño
				.requestMatchers("/sysadmin/**").hasRole("SYSADMIN")
				
				// Inventario y productos dueño y admin
				.requestMatchers("/admin/**").hasAnyRole("ADMIN", "SYSADMIN")
				
				// Pantalla de ventas cualquiera que este logueado puede vender
				.requestMatchers("/sales").hasAnyRole("USER", "ADMIN", "SYSADMIN")
				
				// Todo lo demas requiere estar logueado
				.anyRequest().authenticated()
				)
		.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.permitAll()
				)
		.logout(logout -> logout.permitAll());
		
		return http.build();
	}
}
