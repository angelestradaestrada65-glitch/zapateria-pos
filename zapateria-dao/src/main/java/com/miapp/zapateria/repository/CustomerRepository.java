package com.miapp.zapateria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.Customer;
import java.util.List;



public interface CustomerRepository extends JpaRepository<Customer, Long> {

	
	Optional<Customer> findByPhone(String phone);
	List<Customer> findByCredit(boolean credit);
}
