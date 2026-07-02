package com.genc.smpps.controller;

import com.genc.smpps.model.MaintenanceWorkOrder;
import com.genc.smpps.service.MaintenanceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) {
        this.service = service;
    }

    @GetMapping("/work-orders")
    public List<MaintenanceWorkOrder> getWorkOrders() {
        return service.getAllWorkOrders();
    }

    @PostMapping("/work-orders")
    public MaintenanceWorkOrder create(@Valid @RequestBody MaintenanceWorkOrder w) {
        return service.createWorkOrder(w);
    }

    @PostMapping("/work-orders/{id}/assign")
    public MaintenanceWorkOrder assign(@PathVariable int id, @RequestBody Map<String, String> body) {
        return service.assignTechnician(id, body.get("technician"));
    }

    @PostMapping("/work-orders/{id}/spare")
    public MaintenanceWorkOrder spare(@PathVariable int id, @RequestBody Map<String, String> body) {
        return service.issueSpare(id, body.get("spareParts"));
    }

    @PostMapping("/work-orders/{id}/close")
    public MaintenanceWorkOrder close(@PathVariable int id) {
        return service.closeWorkOrder(id);
    }

    @PostMapping("/work-orders/{id}/cancel")
    public MaintenanceWorkOrder cancel(@PathVariable int id) {
        return service.cancelWorkOrder(id);
    }
}