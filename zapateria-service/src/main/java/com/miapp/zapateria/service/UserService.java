package com.miapp.zapateria.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miapp.zapateria.dto.UserFormDTO;
import com.miapp.zapateria.model.Role;
import com.miapp.zapateria.model.User;
import com.miapp.zapateria.repository.RoleRepository;
import com.miapp.zapateria.repository.UserRepository;

@Service
public class UserService {

	private static final int MIN_PASSWORD_LENGTH = 8;
	
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	public List<User> listAll() {
		return userRepository.findAll();
	}
	
	public List<Role> listRoles() {
		return roleRepository.findAll();
	}
	
	public User findById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
	}

	public User registerUser(String name, String email, String passwordPlain, Long roleId) {

		if (userRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("Ya existe un usuario con ese email");
		}

		validatePassword(passwordPlain);
		
		Role roleUser = roleRepository.findById(roleId)
				.orElseThrow(()-> new IllegalArgumentException("Este rol no existe"));

		User newUser = new User();
		newUser.setName(name);
		newUser.setEmail(email);
		newUser.setPassword(passwordEncoder.encode(passwordPlain));
		newUser.setRole(roleUser);
		newUser.setActive(true);

		return userRepository.save(newUser);
	}
	
	@Transactional
	public User updateUser(Long id, UserFormDTO form, String currentEmail) {
		User user = findById(id);
		
		Role role = roleRepository.findById(form.getRoleId())
				.orElseThrow(() -> new IllegalArgumentException("Este rol no existe"));
		
		userRepository.findByEmail(form.getEmail()).ifPresent(existing -> {
			if(!existing.getId().equals(id)) {
				throw new IllegalArgumentException("Ya existe un usuario con ese email");
			}
		});
		
		// Proteccion: nadie se quita el acceso a si mismo
		boolean isSelf = user.getEmail().equals(currentEmail);
		if(isSelf) {
			if(!form.isActive() || !role.getId().equals(user.getRole().getId())) {
				throw new IllegalArgumentException("No puedes desactivarte ni cambiarte el rol tu mismo");
			}
			if(!form.getEmail().equals(user.getEmail())) {
				throw new IllegalArgumentException("No puedes cambiar tu propio email");
			}
		}
		
		user.setName(form.getName());
		user.setEmail(form.getEmail());
		user.setRole(role);
		user.setActive(form.isActive());
		
		// Contraseña opcional al editar : vacia significa dejar la que tiene
		if(form.getPassword() != null && !form.getPassword().isBlank()) {
			validatePassword(form.getPassword());
			user.setPassword(passwordEncoder.encode(form.getPassword()));
		}
		
		return userRepository.save(user);
	}
	
	private void validatePassword(String passwordPlain) {
		if(passwordPlain == null || passwordPlain.length() < MIN_PASSWORD_LENGTH) {
			throw new IllegalArgumentException("La contraseña debe de tener al menos " + MIN_PASSWORD_LENGTH + "caracteres");
		}
	}

}


























