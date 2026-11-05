package com.miapp.zapateria.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.miapp.zapateria.model.ProductDetail;

public record DashboardDTO(

		BigDecimal todayTotal, 
		long todayCount, 
		BigDecimal monthTotal, 
		BigDecimal pendingTotal,
		List<DailySales> last7Days, 
		List<TopProduct> topProducts, 
		List<Debtor> debtors, 
		List<ProductDetail> lowStock) {

	public record DailySales(String label, BigDecimal total) {

	}

	public record TopProduct(String name, long quantity) {

	}

	public record Debtor(String name, String phone, BigDecimal pending) {

	}

	public String chartLabels() {
		return last7Days.stream().map(DailySales::label).collect(Collectors.joining(","));
	}

	public String chartValues() {
		return last7Days.stream().map(d -> d.total().toPlainString()).collect(Collectors.joining(","));
	}
}
