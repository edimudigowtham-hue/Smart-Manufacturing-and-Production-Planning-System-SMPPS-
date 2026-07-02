package com.genc.smpps.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BomComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private FinishedProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id")
    @JsonIgnore
    private ProductBom productBom;

    @NotBlank(message = "Component code is required")
    @Size(max = 50, message = "Component code must not exceed 50 characters")
    private String componentCode;

    @NotBlank(message = "Component name is required")
    @Size(max = 100, message = "Component name must not exceed 100 characters")
    private String componentName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotBlank(message = "Unit of measure is required")
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String unitOfMeasure;

    @NotBlank(message = "BOM version is required")
    @Size(max = 20, message = "BOM version must not exceed 20 characters")
    private String bomVersion;

    public Integer getComponentId() {
        return componentId;
    }

    public void setComponentId(Integer componentId) {
        this.componentId = componentId;
    }

    public FinishedProduct getProduct() {
        return product;
    }

    public void setProduct(FinishedProduct product) {
        this.product = product;
    }

    public ProductBom getProductBom() {
        return productBom;
    }

    public void setProductBom(ProductBom productBom) {
        this.productBom = productBom;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getBomVersion() {
        return bomVersion;
    }

    public void setBomVersion(String bomVersion) {
        this.bomVersion = bomVersion;
    }

}

