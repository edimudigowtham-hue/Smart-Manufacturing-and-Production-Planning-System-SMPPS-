package com.genc.smpps.dto;

import com.genc.smpps.model.BomComponent;
import com.genc.smpps.model.FinishedProduct;
import com.genc.smpps.model.ProductBom;
import com.genc.smpps.model.ProductStatus;
import java.util.List;

public class ProductStructureResponse {

    private final Integer productId;
    private final String productCode;
    private final String productName;
    private final Integer activeBomId;
    private final Integer bomId;
    private final String bomVersion;
    private final Double standardCost;
    private final ProductStatus productStatus;
    private final List<BomComponentResponse> components;

    public ProductStructureResponse(FinishedProduct product, ProductBom bom, List<BomComponent> components) {
        this.productId = product.getProductId();
        this.productCode = product.getProductCode();
        this.productName = product.getProductName();
        this.activeBomId = product.getActiveBomId();
        this.bomId = bom == null ? null : bom.getBomId();
        this.bomVersion = bom == null ? null : bom.getBomVersion();
        this.standardCost = product.getStandardCost();
        this.productStatus = product.getProductStatus();
        this.components = components.stream()
                .map(BomComponentResponse::new)
                .toList();
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getActiveBomId() {
        return activeBomId;
    }

    public Integer getBomId() {
        return bomId;
    }

    public String getBomVersion() {
        return bomVersion;
    }

    public Double getStandardCost() {
        return standardCost;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public List<BomComponentResponse> getComponents() {
        return components;
    }

    public static class BomComponentResponse {
        private final Integer componentId;
        private final String componentCode;
        private final String componentName;
        private final Double quantity;
        private final String unitOfMeasure;
        private final String bomVersion;

        public BomComponentResponse(BomComponent component) {
            this.componentId = component.getComponentId();
            this.componentCode = component.getComponentCode();
            this.componentName = component.getComponentName();
            this.quantity = component.getQuantity();
            this.unitOfMeasure = component.getUnitOfMeasure();
            this.bomVersion = component.getBomVersion();
        }

        public Integer getComponentId() {
            return componentId;
        }

        public String getComponentCode() {
            return componentCode;
        }

        public String getComponentName() {
            return componentName;
        }

        public Double getQuantity() {
            return quantity;
        }

        public String getUnitOfMeasure() {
            return unitOfMeasure;
        }

        public String getBomVersion() {
            return bomVersion;
        }

    }
}

