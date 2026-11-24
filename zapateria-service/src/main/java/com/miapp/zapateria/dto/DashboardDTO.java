package com.miapp.zapateria.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.miapp.zapateria.model.ProductDetail;

public record DashboardDTO(
		BigDecimal todayTotal,
		long todayCount,
		BigDecimal todayCollected,
		BigDecimal monthTotal,
		BigDecimal monthCollected,
		BigDecimal pendingTotal,
		List<DailySales> last7Days,
		List<TopProduct> topProducts,
		List<Debtor> debtors,
		List<ProductDetail> lowStock) {

	public record DailySales(String label, BigDecimal sold, BigDecimal collected) {
	}

	public record TopProduct(String name, long quantity) {
	}

	public record Debtor(String name, String phone, BigDecimal pending) {
	}

	public String chartLabels() {
		return last7Days.stream().map(DailySales::label).collect(Collectors.joining(","));
	}

	public String chartSold() {
		return last7Days.stream().map(d -> d.sold().toPlainString()).collect(Collectors.joining(","));
	}

	public String chartCollected() {
		return last7Days.stream().map(d -> d.collected().toPlainString()).collect(Collectors.joining(","));
	}

}