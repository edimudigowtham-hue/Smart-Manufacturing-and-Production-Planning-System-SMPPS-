package com.genc.smpps.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.genc.smpps.model.FinishedProduct;

public interface ProductRepository extends JpaRepository<FinishedProduct, Integer> {
	boolean existsByProductCodeIgnoreCase(String productCode);

	boolean existsByProductCodeIgnoreCaseAndProductIdNot(String productCode, Integer productId);
}
