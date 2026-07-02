package com.genc.smpps.controller;

import com.genc.smpps.model.FactoryMachine;
import com.genc.smpps.model.MachineAvailability;
import com.genc.smpps.model.MachineLog;
import com.genc.smpps.model.validation.DowntimeLogValidation;
import com.genc.smpps.model.validation.RuntimeLogValidation;
import com.genc.smpps.service.MachineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService service;

    public MachineController(MachineService service) {
        this.service = service;
    }

    @GetMapping
    public List<FactoryMachine> getMachines() {
        return service.getAllMachines();
    }

    @GetMapping("/available")
    public List<FactoryMachine> getAvailableMachines() {
        return service.getAvailableMachines();
    }

    @GetMapping("/{machineId}")
    public FactoryMachine getMachine(@PathVariable int machineId) {
        return service.findMachineByIdOrThrow(machineId);
    }

    @PostMapping
    public FactoryMachine createMachine(@Valid @RequestBody FactoryMachine machine) {
        return service.createMachine(machine);
    }

    @PostMapping("/{machineId}/availability")
    public FactoryMachine updateAvailability(@PathVariable int machineId, @RequestBody Map<String, String> body) {
        String availability = body.get("availability");
        if (availability == null || availability.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine availability is required");
        }
        return service.updateMachineAvailability(machineId, MachineAvailability.valueOf(availability.trim()));
    }

    @GetMapping("/logs")
    public List<MachineLog> getLogs() {
        return service.getAllLogs();
    }

    @PostMapping("/runtime")
    public MachineLog runtime(@Validated(RuntimeLogValidation.class) @RequestBody MachineLog log) {
        return service.recordRuntime(log);
    }

    @PostMapping("/downtime")
    public MachineLog downtime(@Validated(DowntimeLogValidation.class) @RequestBody MachineLog log) {
        return service.logDowntime(log);
    }

    @GetMapping("/oee")
    public Map<String, String> oee() {
        return Map.of("oee", service.getMachineOee());
    }

    @GetMapping("/oee/summary")
    public Map<String, Object> oeeSummary() {
        return service.getMachineOeeSummary();
    }

    @GetMapping("/status")
    public List<MachineLog> status() {
        return service.getAllLogs();
    }

    @GetMapping("/{machineId}/status")
    public MachineLog machineStatus(@PathVariable int machineId) {
        return service.getMachineStatus(machineId);
    }
}