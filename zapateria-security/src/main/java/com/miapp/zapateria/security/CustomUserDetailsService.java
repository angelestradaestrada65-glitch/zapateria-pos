package com.miapp.zapateria.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.miapp.zapateria.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		com.miapp.zapateria.model.User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("No existe usuario con ese email" + email));
		
		return User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.roles(user.getRole().getName())
				.disabled(!user.isActive())
				.build();
	}
}
