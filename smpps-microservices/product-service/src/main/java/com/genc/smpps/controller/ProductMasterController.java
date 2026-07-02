package com.genc.smpps.controller;

import com.genc.smpps.dto.BomComponentRequest;
import com.genc.smpps.dto.ProductBomRequest;
import com.genc.smpps.dto.ProductStructureResponse;
import com.genc.smpps.model.BomComponent;
import com.genc.smpps.model.FinishedProduct;
import com.genc.smpps.model.ProductBom;
import com.genc.smpps.service.ProductMasterService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductMasterController {

    private final ProductMasterService service;

    public ProductMasterController(ProductMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<FinishedProduct> getProducts() {
        return service.getAllProducts();
    }

    @GetMapping("/{id}")
    public FinishedProduct getProduct(@PathVariable int id) {
        return service.findProductByIdOrThrow(id);
    }

    @PostMapping
    public FinishedProduct createProduct(@Valid @RequestBody FinishedProduct product) {
        return service.createProduct(product);
    }

    @PutMapping("/{id}")
    public FinishedProduct updateProduct(@PathVariable int id, @Valid @RequestBody FinishedProduct product) {
        product.setProductId(id);
        return service.updateProduct(product);
    }

    @GetMapping("/{id}/boms")
    public List<ProductBom> getBomVersions(@PathVariable int id) {
        return service.getBomVersions(id);
    }

    @PostMapping("/{id}/boms")
    public ProductBom createBomVersion(@PathVariable int id, @Valid @RequestBody ProductBomRequest request) {
        return service.createBomVersion(id, request);
    }

    @PatchMapping("/{id}/boms/{bomId}/activate")
    public ProductBom activateBomVersion(@PathVariable int id, @PathVariable int bomId) {
        return service.activateBomVersion(id, bomId);
    }

    @PatchMapping("/{id}/bom")
    public FinishedProduct updateBomVersion(@PathVariable int id, @RequestBody Map<String, String> body) {
        return service.updateBomVersion(id, body.get("bomVersion"));
    }

    @GetMapping("/{id}/structure")
    public ProductStructureResponse getProductStructure(@PathVariable int id) {
        return service.getProductStructure(id);
    }

    @GetMapping("/{id}/boms/{bomId}/structure")
    public ProductStructureResponse getBomStructure(@PathVariable int id, @PathVariable int bomId) {
        return service.getBomStructure(id, bomId);
    }

    @PostMapping("/{id}/boms/{bomId}/components")
    public BomComponent addBomComponent(
            @PathVariable int id,
            @PathVariable int bomId,
            @Valid @RequestBody BomComponentRequest request
    ) {
        return service.addBomComponent(id, bomId, request);
    }

    @PostMapping("/{id}/components")
    public BomComponent addBomComponentToActiveBom(@PathVariable int id, @Valid @RequestBody BomComponentRequest request) {
        return service.addBomComponent(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable int id) {
        service.deleteProduct(id);
    }

}