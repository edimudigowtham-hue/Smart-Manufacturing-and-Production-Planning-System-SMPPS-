package com.genc.smpps.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.genc.smpps.model.MaintenanceWorkOrder;

public interface MaintenanceWorkOrderRepository extends JpaRepository<MaintenanceWorkOrder, Integer> {
}