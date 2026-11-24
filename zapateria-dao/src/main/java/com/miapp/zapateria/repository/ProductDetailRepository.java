package com.miapp.zapateria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.miapp.zapateria.model.Product;
import com.miapp.zapateria.model.ProductDetail;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

	List<ProductDetail> findByProduct(Product product);

	List<ProductDetail> findByProductAndColorAndSize(Product product, String color, String size);

	@Query("select d from ProductDetail d where d.stock <= :stock and d.product.active = true order by d.stock asc")
	List<ProductDetail> findLowStock(@Param("stock") Integer stock);
}
