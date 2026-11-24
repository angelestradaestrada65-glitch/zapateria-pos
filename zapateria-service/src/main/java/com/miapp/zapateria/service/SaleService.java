package com.miapp.zapateria.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miapp.zapateria.dto.SaleItemDTO;
import com.miapp.zapateria.model.Customer;
import com.miapp.zapateria.model.ProductDetail;
import com.miapp.zapateria.model.Sale;
import com.miapp.zapateria.model.SaleDetail;
import com.miapp.zapateria.model.SaleState;
import com.miapp.zapateria.model.User;
import com.miapp.zapateria.model.Payment;
import com.miapp.zapateria.repository.CustomerRepository;
import com.miapp.zapateria.repository.ProductDetailRepository;
import com.miapp.zapateria.repository.SaleRepository;

@Service
public class SaleService {

	private static final BigDecimal DISCOUNT = new BigDecimal("10");

	private final SaleRepository saleRepository;
	private final ProductDetailRepository productDetailRepository;
	private final CustomerRepository customerRepository;

	public SaleService(SaleRepository saleRepository, ProductDetailRepository productDetailRepository,
			CustomerRepository customerRepository) {
		this.saleRepository = saleRepository;
		this.productDetailRepository = productDetailRepository;
		this.customerRepository = customerRepository;
	}

	@Transactional
	public Sale registerSale(User seller, Long customerId, List<SaleItemDTO> items, BigDecimal amountPaid,
			boolean isCredit) {

		if (items == null || items.isEmpty()) {
			throw new IllegalArgumentException("La venta debe tener al menos un producto");
		}

		Sale sale = new Sale();
		sale.setUser(seller);

		Customer customer = null;
		if (customerId != null) {
			customer = customerRepository.findById(customerId)
					.orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
			sale.setCustomer(customer);
		}

		// Un fiado necesita saber quién debe, y que ese cliente tenga el fiado
		// permitido
		if (isCredit) {
			if (customer == null) {
				throw new IllegalArgumentException("Una venta fiada necesita un cliente");
			}
			if (!customer.isCredit()) {
				throw new IllegalArgumentException("Este cliente no tiene permitido el fiado");
			}
		}

		BigDecimal total = BigDecimal.ZERO;

		for (SaleItemDTO item : items) {

			ProductDetail detail = productDetailRepository.findById(item.getProductDetailId())
					.orElseThrow(() -> new IllegalArgumentException("El producto no existe"));

			if (detail.getStock() < item.getQuantity()) {
				throw new IllegalArgumentException("No hay stock suficiente de " + detail.getProduct().getName());
			}

			BigDecimal unitPrice = detail.getPrice().subtract(DISCOUNT);

			SaleDetail saleDetail = new SaleDetail();
			saleDetail.setSale(sale);
			saleDetail.setProductDetail(detail);
			saleDetail.setQuantity(item.getQuantity());
			saleDetail.setUnitPrice(unitPrice);

			sale.getDetails().add(saleDetail);

			detail.setStock(detail.getStock() - item.getQuantity());
			productDetailRepository.save(detail);

			total = total.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
		}

		sale.setTotal(total);

		// Si no es fiado se cobra todo. Si es fiado, lo pagado es el anticipo (vacío =
		// 0)
		BigDecimal paid = BigDecimal.ZERO;
		if (!isCredit) {
			paid = total;
		} else if (amountPaid != null) {
			paid = amountPaid.max(BigDecimal.ZERO).min(total);
		}

		sale.setAmountPaid(paid);
		if (paid.compareTo(BigDecimal.ZERO) > 0) {
			sale.getPayments().add(newPayment(sale, seller, paid));
		}
		sale.setSaleState(paid.compareTo(total) < 0 ? SaleState.ON_CREDIT : SaleState.PAID);

		return saleRepository.save(sale);
	}

	public List<Sale> listAll() {
		return saleRepository.findAllByOrderByDateDesc();
	}

	public List<Sale> listByState(SaleState state) {
		return saleRepository.findBySaleStateOrderByDateDesc(state);
	}

	public Sale findById(Long id) {
		return saleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("La venta no existe"));
	}

	@Transactional
	public Sale registerPayment(Long saleId, BigDecimal amount, User receiver) {
		Sale sale = findById(saleId);

		if (sale.getSaleState() != SaleState.ON_CREDIT) {
			throw new IllegalArgumentException("Solo se puede abonar a una venta fiada pendiente");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El abono debe ser mayor a cero");
		}
		if (amount.compareTo(sale.getPending()) > 0) {
			throw new IllegalArgumentException("El abono supera lo que falta por pagar ($" + sale.getPending() + ")");
		}

		sale.setAmountPaid(sale.getAmountPaid().add(amount));
		sale.getPayments().add(newPayment(sale, receiver, amount));

		if (sale.getPending().compareTo(BigDecimal.ZERO) == 0) {
			sale.setSaleState(SaleState.SETTLED);
		}

		return saleRepository.save(sale);
	}

	private Payment newPayment(Sale sale, User user, BigDecimal amount) {
		Payment payment = new Payment();
		payment.setSale(sale);
		payment.setUser(user);
		payment.setAmount(amount);
		return payment;
	}

	@Transactional
	public Sale cancelSale(Long saleId) {
		Sale sale = findById(saleId);

		if (sale.getSaleState() == SaleState.CANCELLED) {
			throw new IllegalArgumentException("La venta ya está cancelada");
		}

		// Devuelve al inventario lo que se había descontado
		for (SaleDetail line : sale.getDetails()) {
			ProductDetail detail = line.getProductDetail();
			detail.setStock(detail.getStock() + line.getQuantity());
			productDetailRepository.save(detail);
		}

		sale.setSaleState(SaleState.CANCELLED);
		return saleRepository.save(sale);
	}

	public BigDecimal getDiscountPerUnit() {
		return DISCOUNT;
	}

}