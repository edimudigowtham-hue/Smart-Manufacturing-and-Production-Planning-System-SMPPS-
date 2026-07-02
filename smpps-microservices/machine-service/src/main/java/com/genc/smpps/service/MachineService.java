package com.genc.smpps.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.genc.smpps.model.FactoryMachine;
import com.genc.smpps.model.MachineAvailability;
import com.genc.smpps.model.MachineLog;
import com.genc.smpps.model.MachineStatus;
import com.genc.smpps.repo.FactoryMachineRepository;
import com.genc.smpps.repo.MachineLogRepository;

@Service
public class MachineService {

    private final MachineLogRepository repo;
    private final FactoryMachineRepository machineRepo;

    public MachineService(MachineLogRepository repo, FactoryMachineRepository machineRepo) {
        this.repo = repo;
        this.machineRepo = machineRepo;
    }

    public FactoryMachine createMachine(FactoryMachine machine) {
        if (machine.getMachineId() == null || machine.getMachineId() <= 0) {
            throw new IllegalArgumentException("Machine ID must be greater than 0");
        }
        if (machineRepo.existsById(machine.getMachineId())) {
            throw new IllegalArgumentException("Machine ID already exists: " + machine.getMachineId());
        }
        if (machine.getMachineName() == null || machine.getMachineName().trim().isEmpty()) {
            throw new IllegalArgumentException("Machine name is required");
        }
        machine.setMachineName(machine.getMachineName().trim());
        if (machine.getAvailability() == null) {
            machine.setAvailability(MachineAvailability.AVAILABLE);
        }
        if (machine.getAvailability() == MachineAvailability.UNDER_MAINTENANCE) {
            throw new IllegalArgumentException("New machines can be created only as AVAILABLE or UNAVAILABLE");
        }
        return machineRepo.save(machine);
    }

    public List<FactoryMachine> getAllMachines() {
        return machineRepo.findAll();
    }

    public List<FactoryMachine> getAvailableMachines() {
        return machineRepo.findByAvailability(MachineAvailability.AVAILABLE);
    }

    public FactoryMachine findMachineByIdOrThrow(Integer machineId) {
        if (machineId == null || machineId <= 0) {
            throw new IllegalArgumentException("Machine ID must be greater than 0");
        }
        return machineRepo.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found: " + machineId));
    }

    public FactoryMachine updateMachineAvailability(Integer machineId, MachineAvailability availability) {
        if (availability == null) {
            throw new IllegalArgumentException("Machine availability is required");
        }
        FactoryMachine machine = findMachineByIdOrThrow(machineId);
        machine.setAvailability(availability);
        return machineRepo.save(machine);
    }

    public MachineLog recordRuntime(MachineLog log) {
        validateBaseLog(log);
        findMachineByIdOrThrow(log.getMachineId());
        validateHours(log.getRuntimeHours(), "Runtime hours");
        if (repo.existsByMachineIdAndLogDateAndRuntimeHoursGreaterThan(log.getMachineId(), log.getLogDate(), 0.0)) {
            throw new IllegalStateException("Runtime is already recorded for this machine and date");
        }
        validateDailyTotal(log, log.getRuntimeHours());
        log.setMachineStatus(MachineStatus.RUNNING);
        log.setDowntimeHours(0.0);
        return repo.save(log);
    }


    public MachineLog logDowntime(MachineLog log) {
        validateBaseLog(log);
        findMachineByIdOrThrow(log.getMachineId());
        validateHours(log.getDowntimeHours(), "Downtime hours");
        if (repo.existsByMachineIdAndLogDateAndDowntimeHoursGreaterThan(log.getMachineId(), log.getLogDate(), 0.0)) {
            throw new IllegalStateException("Downtime is already recorded for this machine and date");
        }
        validateDailyTotal(log, log.getDowntimeHours());
        log.setMachineStatus(categorizeStatus(log.getDowntimeReason()));
        log.setRuntimeHours(0.0);
        return repo.save(log);
    }

    public String getMachineOee() {
        Map<String, Object> summary = getMachineOeeSummary();
        double overallOee = (double) summary.get("overallOee");
        if ((double) summary.get("totalRuntimeHours") + (double) summary.get("totalDowntimeHours") == 0) {
            return "No data available to calculate OEE";
        }
        return "OEE = " + String.format("%.2f", overallOee) + " %";
    }

    public Map<String, Object> getMachineOeeSummary() {
        List<FactoryMachine> machines = machineRepo.findAll();
        List<MachineLog> logs = repo.findAll();

        Map<Integer, Map<String, Object>> machineMetrics = new LinkedHashMap<>();
        for (FactoryMachine machine : machines) {
            machineMetrics.put(machine.getMachineId(), createMachineMetric(machine));
        }

        double totalRuntime = 0.0;
        double totalDowntime = 0.0;
        int downtimeEvents = 0;

        for (MachineLog log : logs) {
            Integer machineId = log.getMachineId();
            if (!machineMetrics.containsKey(machineId)) {
                continue;
            }

            Map<String, Object> metric = machineMetrics.get(machineId);
            double runtime = safeHours(log.getRuntimeHours());
            double downtime = safeHours(log.getDowntimeHours());

            metric.put("runtimeHours", (double) metric.get("runtimeHours") + runtime);
            metric.put("downtimeHours", (double) metric.get("downtimeHours") + downtime);
            metric.put("logCount", (int) metric.get("logCount") + 1);
            if (downtime > 0) {
                metric.put("downtimeEvents", (int) metric.get("downtimeEvents") + 1);
                downtimeEvents++;
            }
            if (isLatestLog(log, (MachineLog) metric.get("latestLog"))) {
                metric.put("latestLog", log);
                metric.put("latestStatus", log.getMachineStatus());
            }

            totalRuntime += runtime;
            totalDowntime += downtime;
        }

        List<Map<String, Object>> perMachine = new ArrayList<>();
        for (Map<String, Object> metric : machineMetrics.values()) {
            double runtime = (double) metric.get("runtimeHours");
            double downtime = (double) metric.get("downtimeHours");
            metric.put("oee", calculateOee(runtime, downtime));
            metric.remove("latestLog");
            perMachine.add(metric);
        }

        int machineCount = machineMetrics.size();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("overallOee", calculateOee(totalRuntime, totalDowntime));
        summary.put("totalRuntimeHours", round(totalRuntime));
        summary.put("totalDowntimeHours", round(totalDowntime));
        summary.put("averageRuntimeHours", machineCount == 0 ? 0.0 : round(totalRuntime / machineCount));
        summary.put("averageDowntimeHours", machineCount == 0 ? 0.0 : round(totalDowntime / machineCount));
        summary.put("machineCount", machineCount);
        summary.put("logCount", perMachine.stream().mapToInt(metric -> (int) metric.get("logCount")).sum());
        summary.put("downtimeEvents", downtimeEvents);
        summary.put("machines", perMachine);
        return summary;
    }


    public List<MachineLog> getAllLogs() {
        return repo.findAll();
    }

    public MachineLog getMachineStatus(Integer machineId) {
        if (machineId == null || machineId <= 0) {
            throw new IllegalArgumentException("Machine ID must be greater than 0");
        }
        return repo.findTopByMachineIdOrderByLogDateDescLogIdDesc(machineId)
                .orElseThrow(() -> new IllegalArgumentException("No logs found for machine: " + machineId));
    }

    private void validateBaseLog(MachineLog log) {
        if (log.getMachineId() == null || log.getMachineId() <= 0) {
            throw new IllegalArgumentException("Machine ID must be greater than 0");
        }
        if (log.getLogDate() == null) {
            throw new IllegalArgumentException("Log date is required");
        }
    }

    private void validateHours(Double hours, String label) {
        if (hours == null || hours <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        if (hours > 24) {
            throw new IllegalArgumentException(label + " cannot be greater than 24 for a single day");
        }
    }

    private void validateDailyTotal(MachineLog log, Double newHours) {
        double existingHours = Optional.ofNullable(repo.totalLoggedHoursForMachineDate(log.getMachineId(), log.getLogDate()))
                .orElse(0.0);
        if (existingHours + newHours > 24) {
            throw new IllegalStateException("Total runtime and downtime cannot be greater than 24 hours for the same machine and date");
        }
    }

    private MachineStatus categorizeStatus(String downtimeReason) {
        if (downtimeReason == null || downtimeReason.trim().isEmpty()) {
            return MachineStatus.BREAKDOWN;
        }

        String reason = downtimeReason.toLowerCase();
        if (reason.contains("maintenance") || reason.contains("service") || reason.contains("calibration")) {
            return MachineStatus.MAINTENANCE;
        }
        if (reason.contains("idle") || reason.contains("waiting") || reason.contains("no material")) {
            return MachineStatus.IDLE;
        }
        return MachineStatus.BREAKDOWN;
    }

    private Map<String, Object> createMachineMetric(FactoryMachine machine) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("machineId", machine.getMachineId());
        metric.put("machineName", machine.getMachineName());
        metric.put("availability", machine.getAvailability());
        metric.put("runtimeHours", 0.0);
        metric.put("downtimeHours", 0.0);
        metric.put("oee", 0.0);
        metric.put("logCount", 0);
        metric.put("downtimeEvents", 0);
        metric.put("latestStatus", null);
        metric.put("latestLog", null);
        return metric;
    }

    private double safeHours(Double hours) {
        return hours == null ? 0.0 : hours;
    }

    private double calculateOee(double runtime, double downtime) {
        return runtime + downtime == 0 ? 0.0 : round((runtime / (runtime + downtime)) * 100);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean isLatestLog(MachineLog candidate, MachineLog current) {
        if (current == null) {
            return true;
        }
        int dateComparison = candidate.getLogDate().compareTo(current.getLogDate());
        if (dateComparison != 0) {
            return dateComparison > 0;
        }
        return candidate.getLogId() != null && current.getLogId() != null && candidate.getLogId() > current.getLogId();
    }
}
