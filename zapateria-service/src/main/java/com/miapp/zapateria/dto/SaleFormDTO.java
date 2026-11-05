package com.miapp.zapateria.dto;

import java.math.BigDecimal;
import java.util.List;

public class SaleFormDTO {

	private List<SaleItemDTO> items;
	private Long customerId;
	private BigDecimal amountPaid;
	private boolean credit;

	public List<SaleItemDTO> getItems() {
		return items;
	}

	public void setItems(List<SaleItemDTO> items) {
		this.items = items;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	public boolean isCredit() {
		return credit;
	}

	public void setCredit(boolean credit) {
		this.credit = credit;
	}

}
