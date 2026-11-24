package com.miapp.zapateria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.miapp.zapateria.dto.SaleItemDTO;
import com.miapp.zapateria.model.Customer;
import com.miapp.zapateria.model.Product;
import com.miapp.zapateria.model.ProductDetail;
import com.miapp.zapateria.model.Sale;
import com.miapp.zapateria.model.SaleDetail;
import com.miapp.zapateria.model.SaleState;
import com.miapp.zapateria.model.User;
import com.miapp.zapateria.repository.CustomerRepository;
import com.miapp.zapateria.repository.ProductDetailRepository;
import com.miapp.zapateria.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

	@Mock
	private SaleRepository saleRepository;

	@Mock
	private ProductDetailRepository productDetailRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private SaleService saleService;

	private User seller;

	@BeforeEach
	void setUp() {
		seller = new User();
		seller.setName("Vendedor");

		// Al guardar, el repositorio falso devuelve la misma venta que recibe
		lenient().when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	// ---------- registrar venta ----------

	@Test
	@DisplayName("Venta de contado: aplica la rebaja de $10 por par, descuenta stock y queda pagada")
	void registerSale_cash_appliesDiscountAndDecreasesStock() {
		ProductDetail variant = variant(1L, "450.00", 5);
		when(productDetailRepository.findById(1L)).thenReturn(Optional.of(variant));

		Sale sale = saleService.registerSale(seller, null, List.of(item(1L, 2)), null, false);

		assertThat(sale.getTotal()).isEqualByComparingTo("880");
		assertThat(sale.getAmountPaid()).isEqualByComparingTo("880");
		assertThat(sale.getSaleState()).isEqualTo(SaleState.PAID);
		assertThat(sale.getDetails()).hasSize(1);
		assertThat(sale.getDetails().get(0).getUnitPrice()).isEqualByComparingTo("440");
		assertThat(variant.getStock()).isEqualTo(3);
		verify(productDetailRepository).save(variant);
	}

	@Test
	@DisplayName("Una venta sin productos se rechaza")
	void registerSale_emptyItems_throws() {
		assertThatThrownBy(() -> saleService.registerSale(seller, null, List.of(), null, false))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("al menos un producto");
	}

	@Test
	@DisplayName("No se puede vender más de lo que hay en stock, y no se guarda nada")
	void registerSale_notEnoughStock_throws() {
		ProductDetail variant = variant(1L, "450.00", 1);
		when(productDetailRepository.findById(1L)).thenReturn(Optional.of(variant));

		assertThatThrownBy(() -> saleService.registerSale(seller, null, List.of(item(1L, 2)), null, false))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("stock suficiente");

		assertThat(variant.getStock()).isEqualTo(1);
		verify(saleRepository, never()).save(any(Sale.class));
	}

	@Test
	@DisplayName("Un fiado sin cliente se rechaza")
	void registerSale_credit_withoutCustomer_throws() {
		assertThatThrownBy(() -> saleService.registerSale(seller, null, List.of(item(1L, 1)), null, true))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("necesita un cliente");
	}

	@Test
	@DisplayName("Un fiado a un cliente sin fiado permitido se rechaza")
	void registerSale_credit_customerNotAllowed_throws() {
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer(10L, false)));

		assertThatThrownBy(() -> saleService.registerSale(seller, 10L, List.of(item(1L, 1)), null, true))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no tiene permitido el fiado");
	}

	@Test
	@DisplayName("Fiado con anticipo: queda a crédito y debe la diferencia")
	void registerSale_credit_withAdvance_isOnCredit() {
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer(10L, true)));
		when(productDetailRepository.findById(1L)).thenReturn(Optional.of(variant(1L, "450.00", 5)));

		Sale sale = saleService.registerSale(seller, 10L, List.of(item(1L, 1)), new BigDecimal("200"), true);

		assertThat(sale.getSaleState()).isEqualTo(SaleState.ON_CREDIT);
		assertThat(sale.getAmountPaid()).isEqualByComparingTo("200");
		assertThat(sale.getPending()).isEqualByComparingTo("240");
	}

	@Test
	@DisplayName("Fiado con anticipo vacío: cuenta como cero, no falla")
	void registerSale_credit_blankAdvance_isZero() {
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer(10L, true)));
		when(productDetailRepository.findById(1L)).thenReturn(Optional.of(variant(1L, "450.00", 5)));

		Sale sale = saleService.registerSale(seller, 10L, List.of(item(1L, 1)), null, true);

		assertThat(sale.getSaleState()).isEqualTo(SaleState.ON_CREDIT);
		assertThat(sale.getAmountPaid()).isEqualByComparingTo("0");
		assertThat(sale.getPending()).isEqualByComparingTo("440");
	}

	@Test
	@DisplayName("Fiado con anticipo mayor al total: se cobra solo el total y queda pagada")
	void registerSale_credit_advanceCoversTotal_isPaid() {
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer(10L, true)));
		when(productDetailRepository.findById(1L)).thenReturn(Optional.of(variant(1L, "450.00", 5)));

		Sale sale = saleService.registerSale(seller, 10L, List.of(item(1L, 1)), new BigDecimal("1000"), true);

		assertThat(sale.getSaleState()).isEqualTo(SaleState.PAID);
		assertThat(sale.getAmountPaid()).isEqualByComparingTo("440");
	}

	// ---------- abonos ----------

	@Test
	@DisplayName("Un abono parcial reduce la deuda y la venta sigue fiada")
	void registerPayment_partial_keepsOnCredit() {
		Sale sale = sale(SaleState.ON_CREDIT, "440", "200");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		saleService.registerPayment(1L, new BigDecimal("100"), seller);

		assertThat(sale.getAmountPaid()).isEqualByComparingTo("300");
		assertThat(sale.getPending()).isEqualByComparingTo("140");
		assertThat(sale.getSaleState()).isEqualTo(SaleState.ON_CREDIT);
	}

	@Test
	@DisplayName("Un abono que cubre todo lo pendiente salda la venta")
	void registerPayment_full_settles() {
		Sale sale = sale(SaleState.ON_CREDIT, "440", "200");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		saleService.registerPayment(1L, new BigDecimal("240"), seller);

		assertThat(sale.getSaleState()).isEqualTo(SaleState.SETTLED);
		assertThat(sale.getPending()).isEqualByComparingTo("0");
	}

	@Test
	@DisplayName("No se puede abonar más de lo que se debe")
	void registerPayment_exceedsPending_throws() {
		Sale sale = sale(SaleState.ON_CREDIT, "440", "200");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		assertThatThrownBy(() -> saleService.registerPayment(1L, new BigDecimal("500"), seller))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("supera");

		assertThat(sale.getAmountPaid()).isEqualByComparingTo("200");
	}

	@Test
	@DisplayName("El abono debe ser mayor a cero")
	void registerPayment_zero_throws() {
		Sale sale = sale(SaleState.ON_CREDIT, "440", "200");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		assertThatThrownBy(() -> saleService.registerPayment(1L, BigDecimal.ZERO, seller))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("mayor a cero");
	}

	@Test
	@DisplayName("No se puede abonar a una venta que no es fiada")
	void registerPayment_notOnCredit_throws() {
		Sale sale = sale(SaleState.PAID, "440", "440");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		assertThatThrownBy(() -> saleService.registerPayment(1L, new BigDecimal("10"), seller))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Solo se puede abonar");
	}

	// ---------- cancelaciones ----------

	@Test
	@DisplayName("Cancelar una venta devuelve el stock al inventario")
	void cancelSale_restoresStock() {
		ProductDetail variant = variant(1L, "450.00", 3);
		Sale sale = sale(SaleState.PAID, "880", "880");

		SaleDetail line = new SaleDetail();
		line.setSale(sale);
		line.setProductDetail(variant);
		line.setQuantity(2);
		sale.getDetails().add(line);

		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		saleService.cancelSale(1L);

		assertThat(variant.getStock()).isEqualTo(5);
		assertThat(sale.getSaleState()).isEqualTo(SaleState.CANCELLED);
		verify(productDetailRepository).save(variant);
	}

	@Test
	@DisplayName("No se puede cancelar dos veces la misma venta")
	void cancelSale_alreadyCancelled_throws() {
		Sale sale = sale(SaleState.CANCELLED, "880", "880");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		assertThatThrownBy(() -> saleService.cancelSale(1L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ya está cancelada");
	}

	@Test
	@DisplayName("Una venta de contado registra un pago por el total")
	void registerSale_cash_recordsPayment() {
		when(productDetailRepository.findById(1L)).thenReturn(Optional.of(variant(1L, "450.00", 5)));

		Sale sale = saleService.registerSale(seller, null, List.of(item(1L, 1)), null, false);

		assertThat(sale.getPayments()).hasSize(1);
		assertThat(sale.getPayments().get(0).getAmount()).isEqualByComparingTo("440");
		assertThat(sale.getPayments().get(0).getUser()).isSameAs(seller);
	}

	@Test
	@DisplayName("Cada abono queda registrado como un pago con quien lo recibió")
	void registerPayment_recordsPayment() {
		Sale sale = sale(SaleState.ON_CREDIT, "440", "200");
		when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

		saleService.registerPayment(1L, new BigDecimal("100"), seller);

		assertThat(sale.getPayments()).hasSize(1);
		assertThat(sale.getPayments().get(0).getAmount()).isEqualByComparingTo("100");
		assertThat(sale.getPayments().get(0).getUser()).isSameAs(seller);
	}

	// ---------- ayudas para armar los datos de prueba ----------

	private ProductDetail variant(Long id, String price, int stock) {
		Product product = new Product();
		product.setName("Zapatilla Running X1");

		ProductDetail detail = new ProductDetail();
		detail.setId(id);
		detail.setProduct(product);
		detail.setPrice(new BigDecimal(price));
		detail.setStock(stock);
		return detail;
	}

	private SaleItemDTO item(Long variantId, int quantity) {
		SaleItemDTO item = new SaleItemDTO();
		item.setProductDetailId(variantId);
		item.setQuantity(quantity);
		return item;
	}

	private Customer customer(Long id, boolean creditAllowed) {
		Customer customer = new Customer();
		customer.setId(id);
		customer.setName("Juan Pérez");
		customer.setCredit(creditAllowed);
		return customer;
	}

	private Sale sale(SaleState state, String total, String paid) {
		Sale sale = new Sale();
		sale.setId(1L);
		sale.setSaleState(state);
		sale.setTotal(new BigDecimal(total));
		sale.setAmountPaid(new BigDecimal(paid));
		return sale;
	}

}