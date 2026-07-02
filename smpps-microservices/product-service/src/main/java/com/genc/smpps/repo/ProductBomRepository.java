package com.genc.smpps.repo;

import com.genc.smpps.model.ProductBom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductBomRepository extends JpaRepository<ProductBom, Integer> {

    List<ProductBom> findByProductProductIdOrderByBomIdAsc(Integer productId);

    Optional<ProductBom> findByProductProductIdAndBomId(Integer productId, Integer bomId);

    Optional<ProductBom> findByProductProductIdAndBomVersionIgnoreCase(Integer productId, String bomVersion);

    boolean existsByProductProductIdAndBomVersionIgnoreCase(Integer productId, String bomVersion);

    void deleteByProductProductId(Integer productId);
}


