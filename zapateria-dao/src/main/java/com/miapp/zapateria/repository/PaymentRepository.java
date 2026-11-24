package com.miapp.zapateria.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.miapp.zapateria.model.Payment;
import com.miapp.zapateria.model.SaleState;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	// Dinero cobrado desde una fecha, sin cobrar ventas canceladas
	@Query("SELECT p from Payment p WHERE p.sale.saleState <> :excluded AND p.date >= :from")
	List<Payment> findCollectedSince(@Param("excluded") SaleState excluded, @Param("from") LocalDateTime from);
}
