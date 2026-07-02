package com.genc.smpps.repo;

import com.genc.smpps.model.FactoryMachine;
import com.genc.smpps.model.MachineAvailability;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryMachineRepository extends JpaRepository<FactoryMachine, Integer> {
    List<FactoryMachine> findByAvailability(MachineAvailability availability);
}

