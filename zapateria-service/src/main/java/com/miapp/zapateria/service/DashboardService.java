package com.miapp.zapateria.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miapp.zapateria.dto.DashboardDTO;
import com.miapp.zapateria.model.Payment;
import com.miapp.zapateria.model.ProductDetail;
import com.miapp.zapateria.model.Sale;
import com.miapp.zapateria.model.SaleState;
import com.miapp.zapateria.repository.PaymentRepository;
import com.miapp.zapateria.repository.ProductDetailRepository;
import com.miapp.zapateria.repository.SaleDetailRepository;
import com.miapp.zapateria.repository.SaleRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

	private static final int LOW_STOCK_LIMIT = 2;

	private final SaleRepository saleRepository;
	private final SaleDetailRepository saleDetailRepository;
	private final ProductDetailRepository productDetailRepository;
	private final PaymentRepository paymentRepository;

	public DashboardService(SaleRepository saleRepository, SaleDetailRepository saleDetailRepository,
			ProductDetailRepository productDetailRepository, PaymentRepository paymentRepository) {
		this.saleRepository = saleRepository;
		this.saleDetailRepository = saleDetailRepository;
		this.productDetailRepository = productDetailRepository;
		this.paymentRepository = paymentRepository;
	}

	public DashboardDTO build() {
		LocalDate today = LocalDate.now();
		LocalDateTime startMonth = today.withDayOfMonth(1).atStartOfDay();
		LocalDateTime startWeek = today.minusDays(6).atStartOfDay();
		LocalDateTime from = startMonth.isBefore(startWeek) ? startMonth : startWeek;

		List<Sale> sales = saleRepository.findBySaleStateNotAndDateGreaterThanEqual(SaleState.CANCELLED, from);
		List<Payment> payments = paymentRepository.findCollectedSince(SaleState.CANCELLED, from);

		List<Sale> todaySales = sales.stream().filter(s -> s.getDate().toLocalDate().equals(today)).toList();
		List<Sale> monthSales = sales.stream().filter(s -> !s.getDate().isBefore(startMonth)).toList();

		BigDecimal todayCollected = sumPayments(
				payments.stream().filter(p -> p.getDate().toLocalDate().equals(today)).toList());
		BigDecimal monthCollected = sumPayments(
				payments.stream().filter(p -> !p.getDate().isBefore(startMonth)).toList());

		List<DashboardDTO.DailySales> last7Days = new ArrayList<>();
		for (int i = 6; i >= 0; i--) {
			LocalDate day = today.minusDays(i);
			BigDecimal sold = sumTotal(sales.stream().filter(s -> s.getDate().toLocalDate().equals(day)).toList());
			BigDecimal collected = sumPayments(
					payments.stream().filter(p -> p.getDate().toLocalDate().equals(day)).toList());
			last7Days.add(
					new DashboardDTO.DailySales(day.format(DateTimeFormatter.ofPattern("dd/MM")), sold, collected));
		}

		List<Sale> creditSales = saleRepository.findBySaleStateOrderByDateDesc(SaleState.ON_CREDIT);

		BigDecimal pendingTotal = creditSales.stream().map(Sale::getPending).reduce(BigDecimal.ZERO, BigDecimal::add);

		List<DashboardDTO.Debtor> debtors = creditSales.stream().filter(s -> s.getCustomer() != null)
				.collect(Collectors.groupingBy(s -> s.getCustomer().getId())).values().stream()
				.map(list -> new DashboardDTO.Debtor(list.get(0).getCustomer().getName(),
						list.get(0).getCustomer().getPhone(),
						list.stream().map(Sale::getPending).reduce(BigDecimal.ZERO, BigDecimal::add)))
				.sorted(Comparator.comparing(DashboardDTO.Debtor::pending).reversed()).toList();

		List<DashboardDTO.TopProduct> topProducts = saleDetailRepository
				.findTopProducts(SaleState.CANCELLED, PageRequest.of(0, 5)).stream()
				.map(row -> new DashboardDTO.TopProduct((String) row[0], ((Number) row[1]).longValue())).toList();

		List<ProductDetail> lowStock = productDetailRepository.findLowStock(LOW_STOCK_LIMIT);

		return new DashboardDTO(sumTotal(todaySales), todaySales.size(), todayCollected, sumTotal(monthSales),
				monthCollected, pendingTotal, last7Days, topProducts, debtors, lowStock);
	}

	private BigDecimal sumTotal(List<Sale> sales) {
		return sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal sumPayments(List<Payment> payments) {
		return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}