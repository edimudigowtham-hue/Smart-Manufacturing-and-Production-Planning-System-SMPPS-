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
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QualityInspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inspectionId;

    @NotNull(message = "Production order is required")
    @Positive(message = "Order ID must be greater than 0")
    private Integer orderId;

    @NotNull(message = "Inspection date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate inspectionDate;

    @NotNull(message = "Sample size is required")
    @Positive(message = "Sample size must be greater than 0")
    private Integer sampleSize;

    @NotNull(message = "Defect count is required")
    @PositiveOrZero(message = "Defect count cannot be negative")
    private Integer defectCount;

    @Enumerated(EnumType.STRING)
    private InspectionResult inspectionResult;

    @Size(max = 100, message = "Defect type must not exceed 100 characters")
    private String defectType;
    @Size(max = 255, message = "Defect description must not exceed 255 characters")
    private String defectDescription;
    @Size(max = 50, message = "Severity must not exceed 50 characters")
    private String severity;

    @AssertTrue(message = "Defect count cannot be greater than sample size")
    public boolean isDefectCountValid() {
        return sampleSize == null || defectCount == null || defectCount <= sampleSize;
    }

    public Integer getInspectionId() { return inspectionId; }
    public void setInspectionId(Integer inspectionId) { this.inspectionId = inspectionId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public Integer getDefectCount() { return defectCount; }
    public void setDefectCount(Integer defectCount) { this.defectCount = defectCount; }
    public InspectionResult getInspectionResult() { return inspectionResult; }
    public void setInspectionResult(InspectionResult inspectionResult) { this.inspectionResult = inspectionResult; }
    public String getDefectType() { return defectType; }
    public void setDefectType(String defectType) { this.defectType = defectType; }
    public String getDefectDescription() { return defectDescription; }
    public void setDefectDescription(String defectDescription) { this.defectDescription = defectDescription; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
