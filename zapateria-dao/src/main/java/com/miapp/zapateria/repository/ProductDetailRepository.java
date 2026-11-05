package com.miapp.zapateria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miapp.zapateria.model.Product;
import com.miapp.zapateria.model.ProductDetail;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

	List<ProductDetail> findByProduct(Product product);
	List<ProductDetail> findByProductAndColorAndSize(Product product, String color, String size);
	List<ProductDetail> findByStockLessThanEqualOrderByStockAsc(Integer stock);
}
