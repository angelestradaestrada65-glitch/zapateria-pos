package com.miapp.zapateria.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductDetailFormDTO {

	@NotBlank(message = "La talla es obligatoria")
	@Size(max = 10, message = "La talla no puede pasar de 10 caracteres")
	private String size;

	@NotBlank(message = "El color es obligatorio")
	@Size(max = 30, message = "El color no puede pasar de 30 caracteres")
	private String color;

	@NotNull(message = "El precio es obligatorio")
	@DecimalMin(value = "10.01", message = "El precio debe ser mayor a $10, que es la rebaja que se descuenta en cada venta")
	@Digits(integer = 8, fraction = 2, message = "El precio no es válido")
	private BigDecimal price;

	@NotNull(message = "El stock es obligatorio")
	@Min(value = 0, message = "El stock no puede ser negativo")
	private Integer stock;

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	

}
