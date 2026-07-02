package com.genc.smpps.repo;

import com.genc.smpps.model.BomComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BomComponentRepository extends JpaRepository<BomComponent, Integer> {

    List<BomComponent> findByProductProductIdOrderByComponentIdAsc(Integer productId);

    List<BomComponent> findByProductBomBomIdOrderByComponentIdAsc(Integer bomId);

    List<BomComponent> findByProductProductIdAndBomVersionOrderByComponentIdAsc(
            Integer productId,
            String bomVersion
    );

    void deleteByProductBomBomId(Integer bomId);

    void deleteByProductProductId(Integer productId);
}

