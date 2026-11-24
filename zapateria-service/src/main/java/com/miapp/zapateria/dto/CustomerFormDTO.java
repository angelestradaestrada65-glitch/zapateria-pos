package com.miapp.zapateria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CustomerFormDTO {

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
	private String name;

	@NotBlank(message = "El teléfono es obligatorio")
	@Pattern(regexp = "^[0-9 +()-]{7,20}$", message = "Teléfono no válido: solo números, de 7 a 20 caracteres")
	private String phone;
	
	private boolean credit;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isCredit() {
		return credit;
	}

	public void setCredit(boolean credit) {
		this.credit = credit;
	}

}
