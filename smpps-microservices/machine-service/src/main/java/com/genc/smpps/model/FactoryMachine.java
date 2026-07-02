package com.genc.smpps.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FactoryMachine {

    @Id
    @NotNull(message = "Machine ID is required")
    @Positive(message = "Machine ID must be greater than 0")
    private Integer machineId;

    @NotBlank(message = "Machine name is required")
    @Size(max = 100, message = "Machine name must not exceed 100 characters")
    private String machineName;

    @NotNull(message = "Machine availability is required")
    @Enumerated(EnumType.STRING)
    private MachineAvailability availability = MachineAvailability.AVAILABLE;

    public Integer getMachineId() { return machineId; }
    public void setMachineId(Integer machineId) { this.machineId = machineId; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    
}

