package com.miapp.zapateria.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.Customer;
import com.miapp.zapateria.model.Sale;
import com.miapp.zapateria.model.SaleState;


public interface SaleRepository extends JpaRepository<Sale, Long> {

	List<Sale> findByCustomer(Customer customer);
	List<Sale> findAllByOrderByDateDesc();
	List<Sale> findBySaleStateOrderByDateDesc(SaleState saleState);
	List<Sale> findBySaleStateNotAndDateGreaterThanEqual(SaleState saleState, LocalDateTime from);
}
