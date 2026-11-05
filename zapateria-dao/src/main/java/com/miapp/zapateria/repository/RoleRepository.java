package com.miapp.zapateria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(String name);
}
