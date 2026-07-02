package com.genc.smpps.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FinishedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String productName;

    @Size(max = 20, message = "BOM version must not exceed 20 characters")
    private String bomVersion;

    private Integer activeBomId;

    @NotNull(message = "Standard cost is required")
    @PositiveOrZero(message = "Standard cost cannot be negative")
    private Double standardCost;

    @NotNull(message = "Product status is required")
    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBomVersion() { return bomVersion; }
    public void setBomVersion(String bomVersion) { this.bomVersion = bomVersion; }

    public Integer getActiveBomId() { return activeBomId; }
    public void setActiveBomId(Integer activeBomId) { this.activeBomId = activeBomId; }

    public Double getStandardCost() { return standardCost; }
    public void setStandardCost(Double standardCost) { this.standardCost = standardCost; }

    public ProductStatus getProductStatus() { return productStatus; }
    public void setProductStatus(ProductStatus productStatus) { this.productStatus = productStatus; }
}