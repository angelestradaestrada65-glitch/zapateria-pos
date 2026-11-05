package com.miapp.zapateria.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.Category;
import com.miapp.zapateria.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByCategory(Category category);
	
}
