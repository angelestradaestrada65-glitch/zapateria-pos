package com.miapp.zapateria.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.miapp.zapateria.dto.ProductDetailFormDTO;
import com.miapp.zapateria.dto.ProductFormDTO;
import com.miapp.zapateria.model.Category;
import com.miapp.zapateria.model.Product;
import com.miapp.zapateria.model.ProductDetail;
import com.miapp.zapateria.repository.CategoryRepository;
import com.miapp.zapateria.repository.ProductDetailRepository;
import com.miapp.zapateria.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductDetailRepository productDetailRepository;
	private final CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, ProductDetailRepository productDetailRepository,
			CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.productDetailRepository = productDetailRepository;
		this.categoryRepository = categoryRepository;
	}

	public List<Product> listAll() {
		return productRepository.findAll();
	}

	public Product findById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El producto no existe"));
	}

	public List<Category> listCategories() {
		return categoryRepository.findAll();
	}

	public Product createProduct(ProductFormDTO form) {
		Category category = categoryRepository.findById(form.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("La categoría no existe"));

		Product product = new Product();
		product.setName(form.getName());
		product.setBrand(form.getBrand());
		product.setCategory(category);

		return productRepository.save(product);
	}

	public Product updateProduct(Long id, ProductFormDTO form) {
		Product product = findById(id);

		Category category = categoryRepository.findById(form.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("La categoria no existe"));

		product.setName(form.getName());
		product.setBrand(form.getBrand());
		product.setCategory(category);

		return productRepository.save(product);
	}

	public void deleteProduct(Long id) {
		Product product = findById(id);
		if (!productDetailRepository.findByProduct(product).isEmpty()) {
			throw new IllegalArgumentException(
					"No se puede borrar: el producto todavía variantes. Borrá primero sus variantes.");
		}
		productRepository.deleteById(id);
	}

	public List<ProductDetail> listVariants(Long id) {
		Product product = findById(id);
		return productDetailRepository.findByProduct(product);
	}

	public ProductDetail addVariant(Long productId, ProductDetailFormDTO form) {
		Product product = findById(productId);

		List<ProductDetail> existing = productDetailRepository.findByProductAndColorAndSize(product, form.getColor(),
				form.getSize());

		if (!existing.isEmpty()) {
			throw new IllegalArgumentException("Ya existe esa combinacioón de talla y color");
		}

		ProductDetail detail = new ProductDetail();
		detail.setProduct(product);
		detail.setSize(form.getSize());
		detail.setColor(form.getColor());
		detail.setPrice(form.getPrice());
		detail.setStock(form.getStock());

		return productDetailRepository.save(detail);
	}

	public void deleteVariant(Long variantId) {
		try {
			productDetailRepository.deleteById(variantId);
		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("No se puede borrar, esta variante ya tiene ventas registradas.");
		}
	}
}
