package com.genc.smpps.service;

import com.genc.smpps.client.ProductionOrderClient;
import com.genc.smpps.dto.ProductionOrderDto;
import com.genc.smpps.model.InspectionResult;
import com.genc.smpps.model.QualityInspection;
import com.genc.smpps.repo.QualityInspectionRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class QualityService {
    private static final double AQL_REWORK_LIMIT_PERCENT = 5.0;

    private final QualityInspectionRepository repo;
    private final ProductionOrderClient productionOrderClient;

    public QualityService(QualityInspectionRepository repo, ProductionOrderClient productionOrderClient) {
        this.repo = repo;
        this.productionOrderClient = productionOrderClient;
    }

    public QualityInspection recordInspection(QualityInspection q) {
        ProductionOrderDto order = validateOrder(q.getOrderId());
        validateInspectionQuantity(q, order);
        clearDefectDetails(q);
        applyAqlResult(q);
        return repo.save(q);
    }

    public QualityInspection logDefect(Integer id, Map<String, String> body) {
        QualityInspection q = findInspectionOrThrow(id);
        q.setDefectType(clean(body.get("defectType")));
        q.setDefectDescription(clean(body.get("defectDescription")));
        q.setSeverity(normalizeSeverity(body.get("severity")));
        applyAqlResult(q);
        return repo.save(q);
    }

    public QualityInspection approveBatch(Integer id) {
        QualityInspection q = findInspectionOrThrow(id);
        q.setInspectionResult(InspectionResult.PASS);
        return repo.save(q);
    }

    public QualityInspection rejectBatch(Integer id) {
        QualityInspection q = findInspectionOrThrow(id);
        q.setInspectionResult(InspectionResult.FAIL);
        return repo.save(q);
    }

    public List<QualityInspection> getAllInspections() {
        return repo.findAll();
    }

    private ProductionOrderDto validateOrder(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be greater than 0");
        }
        ProductionOrderDto order = productionOrderClient.getOrder(orderId);
        if (order == null || order.orderId() == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    private void validateInspectionQuantity(QualityInspection q, ProductionOrderDto order) {
        if (q.getSampleSize() == null || q.getSampleSize() <= 0) {
            throw new IllegalArgumentException("Sample size must be greater than 0");
        }
        if (q.getDefectCount() == null || q.getDefectCount() < 0) {
            throw new IllegalArgumentException("Defect count cannot be negative");
        }
        if (q.getDefectCount() > q.getSampleSize()) {
            throw new IllegalArgumentException("Defect count cannot be greater than sample size");
        }

        Integer producedQuantity = order.producedQuantity();
        if (producedQuantity == null || producedQuantity <= 0) {
            throw new IllegalStateException("Inspection cannot be recorded because no quantity has been produced for this order");
        }
        if (q.getSampleSize() > producedQuantity) {
            throw new IllegalArgumentException("Sample size cannot be greater than produced quantity (" + producedQuantity + ")");
        }
    }

    private void applyAqlResult(QualityInspection q) {
        if (q.getDefectCount() == null || q.getDefectCount() == 0) {
            q.setInspectionResult(InspectionResult.PASS);
            return;
        }
        if ("CRITICAL".equalsIgnoreCase(q.getSeverity())) {
            q.setInspectionResult(InspectionResult.FAIL);
            return;
        }

        double defectRate = (q.getDefectCount() * 100.0) / q.getSampleSize();
        q.setInspectionResult(defectRate <= AQL_REWORK_LIMIT_PERCENT ? InspectionResult.REWORK : InspectionResult.FAIL);
    }

    private void clearDefectDetails(QualityInspection q) {
        q.setDefectType(null);
        q.setDefectDescription(null);
        q.setSeverity(null);
    }

    private String normalizeSeverity(String severity) {
        String value = clean(severity);
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!List.of("MINOR", "MAJOR", "CRITICAL").contains(normalized)) {
            throw new IllegalArgumentException("Severity must be MINOR, MAJOR, or CRITICAL");
        }
        return normalized;
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private QualityInspection findInspectionOrThrow(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Inspection ID must be greater than 0");
        }
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + id));
    }
}
