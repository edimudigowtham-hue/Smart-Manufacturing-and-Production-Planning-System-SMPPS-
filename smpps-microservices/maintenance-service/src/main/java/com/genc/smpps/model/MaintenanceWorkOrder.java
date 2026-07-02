package com.genc.smpps.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MaintenanceWorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workOrderId;

    @NotNull(message = "Machine ID is required")
    @Positive(message = "Machine ID must be greater than 0")
    private Integer machineId;

    @NotNull(message = "Maintenance type is required")
    @Enumerated(EnumType.STRING)
    private MaintenanceType maintenanceType;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate scheduledDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus workOrderStatus;

    @Size(max = 100, message = "Technician name must not exceed 100 characters")
    private String technician;

    @Size(max = 255, message = "Spare parts must not exceed 255 characters")
    private String spareParts;


    @AssertTrue(message = "Completion date cannot be before start date")
    public boolean isCompletionDateValid() {
        if (scheduledDate == null || completionDate == null) {
            return true;
        }
        return !completionDate.isBefore(scheduledDate);
    }


    public Integer getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Integer workOrderId) { this.workOrderId = workOrderId; }

    public Integer getMachineId() { return machineId; }
    public void setMachineId(Integer machineId) { this.machineId = machineId; }

    public MaintenanceType getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(MaintenanceType maintenanceType) { this.maintenanceType = maintenanceType; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public WorkOrderStatus getWorkOrderStatus() { return workOrderStatus; }
    public void setWorkOrderStatus(WorkOrderStatus workOrderStatus) { this.workOrderStatus = workOrderStatus; }

    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }

    public String getSpareParts() { return spareParts; }
    public void setSpareParts(String spareParts) { this.spareParts = spareParts; }
}