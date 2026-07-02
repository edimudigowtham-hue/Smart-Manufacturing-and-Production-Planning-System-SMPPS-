package com.genc.smpps.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.genc.smpps.client.MachineClient;
import com.genc.smpps.dto.FactoryMachineDto;
import com.genc.smpps.model.WorkOrderStatus;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.genc.smpps.model.MaintenanceWorkOrder;
import com.genc.smpps.repo.MaintenanceWorkOrderRepository;

@Service
public class MaintenanceService {

    private final MaintenanceWorkOrderRepository repo;
    private final MachineClient machineClient;

    public MaintenanceService(MaintenanceWorkOrderRepository repo, MachineClient machineClient) {
        this.repo = repo;
        this.machineClient = machineClient;
    }


    @Transactional
    public MaintenanceWorkOrder createWorkOrder(MaintenanceWorkOrder w) {
        validateStartDate(w.getScheduledDate());
        validateMachine(w.getMachineId());
        w.setWorkOrderId(null);
        w.setCompletionDate(null);
        w.setWorkOrderStatus(WorkOrderStatus.OPEN); // default status
        MaintenanceWorkOrder saved = repo.save(w);
        updateMachineAvailability(saved.getMachineId(), "UNDER_MAINTENANCE");
        return saved;
    }


    public MaintenanceWorkOrder assignTechnician(int id, String technician) {
        if (technician == null || technician.trim().isEmpty()) {
            throw new IllegalArgumentException("Technician is required");
        }
        MaintenanceWorkOrder w = findWorkOrderOrThrow(id);
        ensureCanModify(w, "assign technician");
        if (w.getWorkOrderStatus() != WorkOrderStatus.OPEN) {
            throw new IllegalStateException("Only OPEN work orders can be assigned");
        }
        w.setTechnician(technician.trim());
        w.setWorkOrderStatus(WorkOrderStatus.IN_PROGRESS);
        return repo.save(w);
    }


    public MaintenanceWorkOrder issueSpare(int id, String spareParts) {
        if (spareParts == null || spareParts.trim().isEmpty()) {
            throw new IllegalArgumentException("Spare parts are required");
        }
        MaintenanceWorkOrder w = findWorkOrderOrThrow(id);
        ensureCanModify(w, "issue spare parts");
        if (w.getWorkOrderStatus() != WorkOrderStatus.IN_PROGRESS || isBlank(w.getTechnician())) {
            throw new IllegalStateException("Assign a technician before issuing spare parts");
        }
        w.setSpareParts(spareParts.trim());
        return repo.save(w);
    }


    @Transactional
    public MaintenanceWorkOrder closeWorkOrder(int id) {
        MaintenanceWorkOrder w = findWorkOrderOrThrow(id);
        if (w.getWorkOrderStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException("Work order is already completed");
        }
        if (w.getWorkOrderStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled work orders cannot be closed");
        }
        if (w.getWorkOrderStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS work orders can be closed");
        }
        if (isBlank(w.getTechnician())) {
            throw new IllegalStateException("Assign a technician before closing the work order");
        }
        if (isBlank(w.getSpareParts())) {
            throw new IllegalStateException("Issue spare parts before closing the work order");
        }
        LocalDate completionDate = LocalDate.now();
        if (w.getScheduledDate() != null && completionDate.isBefore(w.getScheduledDate())) {
            throw new IllegalStateException("Cannot close work order before its start date");
        }

        w.setWorkOrderStatus(WorkOrderStatus.COMPLETED);
        w.setCompletionDate(completionDate);
        MaintenanceWorkOrder saved = repo.save(w);
        updateMachineAvailability(w.getMachineId(), "AVAILABLE");
        return saved;
    }

    @Transactional
    public MaintenanceWorkOrder cancelWorkOrder(int id) {
        MaintenanceWorkOrder w = findWorkOrderOrThrow(id);
        if (w.getWorkOrderStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed work orders cannot be cancelled");
        }
        if (w.getWorkOrderStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("Work order is already cancelled");
        }

        w.setWorkOrderStatus(WorkOrderStatus.CANCELLED);
        w.setCompletionDate(null);
        MaintenanceWorkOrder saved = repo.save(w);
        updateMachineAvailability(w.getMachineId(), "AVAILABLE");
        return saved;
    }


    public List<MaintenanceWorkOrder> getAllWorkOrders() {
        return repo.findAll();
    }

    private MaintenanceWorkOrder findWorkOrderOrThrow(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Work order ID must be greater than 0");
        }
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));
    }

    private void ensureCanModify(MaintenanceWorkOrder w, String action) {
        if (w.getWorkOrderStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed work orders cannot " + action);
        }
        if (w.getWorkOrderStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled work orders cannot " + action);
        }
    }

    private void validateMachine(Integer machineId) {
        if (machineId == null || machineId <= 0) {
            throw new IllegalArgumentException("Machine ID must be greater than 0");
        }
        FactoryMachineDto machine;
        try {
            machine = machineClient.getMachine(machineId);
        } catch (FeignException ex) {
            throw new IllegalArgumentException("Machine not found: " + machineId);
        }
        if (machine == null || machine.machineId() == null) {
            throw new IllegalArgumentException("Machine not found: " + machineId);
        }
        if (!"AVAILABLE".equalsIgnoreCase(machine.availability())) {
            throw new IllegalStateException("Maintenance can be scheduled only for AVAILABLE machines: " + machineId);
        }
    }

    private void validateStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
    }

    private void updateMachineAvailability(Integer machineId, String availability) {
        machineClient.updateAvailability(machineId, Map.of("availability", availability));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}