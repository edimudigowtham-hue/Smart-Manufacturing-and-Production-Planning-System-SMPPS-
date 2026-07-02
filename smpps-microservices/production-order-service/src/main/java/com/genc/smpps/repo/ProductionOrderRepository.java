package com.genc.smpps.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.genc.smpps.model.ProductionOrder;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Integer> {
	boolean existsByProductId(Integer productId);
}