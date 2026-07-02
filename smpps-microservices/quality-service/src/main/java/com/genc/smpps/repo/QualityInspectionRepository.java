package com.genc.smpps.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.genc.smpps.model.QualityInspection;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Integer> {
}
