package com.genc.smpps.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.genc.smpps.model.validation.DowntimeLogValidation;
import com.genc.smpps.model.validation.RuntimeLogValidation;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MachineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @NotNull(message = "Machine ID is required")
    @Positive(message = "Machine ID must be greater than 0")
    private Integer machineId;

    @NotNull(message = "Log date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate logDate;

    @NotNull(message = "Runtime hours are required", groups = RuntimeLogValidation.class)
    @Positive(message = "Runtime hours must be greater than 0", groups = RuntimeLogValidation.class)
    @PositiveOrZero(message = "Runtime hours cannot be negative")
    private Double runtimeHours = 0.0;

    @NotNull(message = "Downtime hours are required", groups = DowntimeLogValidation.class)
    @Positive(message = "Downtime hours must be greater than 0", groups = DowntimeLogValidation.class)
    @PositiveOrZero(message = "Downtime hours cannot be negative")
    private Double downtimeHours = 0.0;

    private String downtimeReason;

    @Enumerated(EnumType.STRING)
    private MachineStatus machineStatus;


    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }

    public Integer getMachineId() { return machineId; }
    public void setMachineId(Integer machineId) { this.machineId = machineId; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public Double getRuntimeHours() { return runtimeHours; }
    public void setRuntimeHours(Double runtimeHours) { this.runtimeHours = runtimeHours; }

    public Double getDowntimeHours() { return downtimeHours; }
    public void setDowntimeHours(Double downtimeHours) { this.downtimeHours = downtimeHours; }

    public String getDowntimeReason() { return downtimeReason; }
    public void setDowntimeReason(String downtimeReason) { this.downtimeReason = downtimeReason; }

    public MachineStatus getMachineStatus() { return machineStatus; }
    public void setMachineStatus(MachineStatus machineStatus) { this.machineStatus = machineStatus; }
}