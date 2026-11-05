package com.miapp.zapateria.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.miapp.zapateria.model.Sale;
import com.miapp.zapateria.model.SaleDetail;
import com.miapp.zapateria.model.SaleState;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

	List<SaleDetail> findBySale(Sale sale);

	@Query("""
			select p.name, sum(d.quantity)
			from SaleDetail d
			join d.productDetail pd
			join pd.product p
			where d.sale.saleState <> :excluded
			group by p.name
			order by sum(d.quantity) desc
			""")

	List<Object[]> findTopProducts(@Param("excluded") SaleState excluded, Pageable pageable);
}
