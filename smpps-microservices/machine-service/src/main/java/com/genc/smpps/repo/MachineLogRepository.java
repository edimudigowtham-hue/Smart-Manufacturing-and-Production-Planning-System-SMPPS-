package com.genc.smpps.repo;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.genc.smpps.model.MachineLog;

public interface MachineLogRepository extends JpaRepository<MachineLog, Integer> {
	boolean existsByMachineIdAndLogDateAndRuntimeHoursGreaterThan(Integer machineId, LocalDate logDate, Double runtimeHours);

	boolean existsByMachineIdAndLogDateAndDowntimeHoursGreaterThan(Integer machineId, LocalDate logDate, Double downtimeHours);

	Optional<MachineLog> findTopByMachineIdOrderByLogDateDescLogIdDesc(Integer machineId);

	@Query("""
			select coalesce(sum(coalesce(m.runtimeHours, 0) + coalesce(m.downtimeHours, 0)), 0)
			from MachineLog m
			where m.machineId = :machineId and m.logDate = :logDate
			""")
	Double totalLoggedHoursForMachineDate(@Param("machineId") Integer machineId, @Param("logDate") LocalDate logDate);
}