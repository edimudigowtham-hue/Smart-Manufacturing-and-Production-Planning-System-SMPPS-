package com.genc.smpps.controller;

import com.genc.smpps.model.QualityInspection;
import com.genc.smpps.service.QualityService;
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
@RequestMapping("/api/quality")
public class QualityController {

    private final QualityService service;

    public QualityController(QualityService service) {
        this.service = service;
    }

    @GetMapping("/inspections")
    public List<QualityInspection> getInspections() {
        return service.getAllInspections();
    }

    @PostMapping("/inspections")
    public QualityInspection recordInspection(@Valid @RequestBody QualityInspection q) {
        return service.recordInspection(q);
    }

    @PostMapping("/inspections/{id}/defect")
    public QualityInspection logDefect(@PathVariable int id, @RequestBody Map<String, String> body) {
        return service.logDefect(id, body);
    }

    @PostMapping("/inspections/{id}/approve")
    public QualityInspection approve(@PathVariable int id) {
        return service.approveBatch(id);
    }

    @PostMapping("/inspections/{id}/reject")
    public QualityInspection reject(@PathVariable int id) {
        return service.rejectBatch(id);
    }
}
