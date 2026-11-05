package com.miapp.zapateria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.User;


public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
}
