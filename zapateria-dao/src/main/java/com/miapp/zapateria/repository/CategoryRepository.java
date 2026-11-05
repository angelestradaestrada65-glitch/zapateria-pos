package com.miapp.zapateria.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.Category;


public interface CategoryRepository extends JpaRepository<Category, Long> {

}
