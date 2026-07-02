package com.genc.smpps.service;

import com.genc.smpps.client.ProductionOrderClient;
import com.genc.smpps.dto.BomComponentRequest;
import com.genc.smpps.dto.ProductBomRequest;
import com.genc.smpps.dto.ProductStructureResponse;
import com.genc.smpps.model.BomComponent;
import com.genc.smpps.model.FinishedProduct;
import com.genc.smpps.model.ProductBom;
import com.genc.smpps.repo.BomComponentRepository;
import com.genc.smpps.repo.ProductBomRepository;
import com.genc.smpps.repo.ProductRepository;
import feign.FeignException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductMasterService {
    private static final String UNASSIGNED_BOM_VERSION = "UNASSIGNED";


    private final ProductRepository repo;
    private final ProductBomRepository productBomRepo;
    private final BomComponentRepository bomComponentRepo;
    private final ProductionOrderClient productionOrderClient;

    public ProductMasterService(
            ProductRepository repo,
            ProductBomRepository productBomRepo,
            BomComponentRepository bomComponentRepo,
            ProductionOrderClient productionOrderClient
    ) {
        this.repo = repo;
        this.productBomRepo = productBomRepo;
        this.bomComponentRepo = bomComponentRepo;
        this.productionOrderClient = productionOrderClient;
    }

    public FinishedProduct createProduct(FinishedProduct product) {
        String productCode = cleanRequired(product.getProductCode(), "Product code is required");
        if (repo.existsByProductCodeIgnoreCase(productCode)) {
            throw new IllegalArgumentException("Product code already exists: " + productCode);
        }

        product.setProductId(null);
        product.setProductCode(productCode);
        product.setProductName(cleanRequired(product.getProductName(), "Product name is required"));
        product.setBomVersion(UNASSIGNED_BOM_VERSION);
        product.setActiveBomId(null);
        return repo.save(product);
    }

    public FinishedProduct updateProduct(FinishedProduct product) {
        FinishedProduct existing = findProductByIdOrThrow(product.getProductId());
        String productCode = cleanRequired(product.getProductCode(), "Product code is required");
        if (repo.existsByProductCodeIgnoreCaseAndProductIdNot(productCode, product.getProductId())) {
            throw new IllegalArgumentException("Product code already exists: " + productCode);
        }

        product.setProductCode(productCode);
        product.setProductName(cleanRequired(product.getProductName(), "Product name is required"));
        product.setActiveBomId(existing.getActiveBomId());
        product.setBomVersion(existing.getBomVersion());
        return repo.save(product);
    }

    @Transactional
    public ProductBom createBomVersion(Integer productId, ProductBomRequest request) {
        FinishedProduct product = findProductByIdOrThrow(productId);
        String version = cleanRequired(request.getBomVersion(), "BOM version is required");
        if (productBomRepo.existsByProductProductIdAndBomVersionIgnoreCase(productId, version)) {
            throw new IllegalArgumentException("BOM version already exists for this product");
        }
        ProductBom bom = new ProductBom();
        bom.setProduct(product);
        bom.setBomVersion(version);
        ProductBom saved = productBomRepo.save(bom);

        if (product.getActiveBomId() == null) {
            activateBomVersion(productId, saved.getBomId());
            return productBomRepo.findById(saved.getBomId()).orElse(saved);
        }
        return saved;
    }

    public List<ProductBom> getBomVersions(Integer productId) {
        findProductByIdOrThrow(productId);
        return productBomRepo.findByProductProductIdOrderByBomIdAsc(productId);
    }

    @Transactional
    public ProductBom activateBomVersion(Integer productId, Integer bomId) {
        FinishedProduct product = findProductByIdOrThrow(productId);
        ProductBom selected = findBomByProductOrThrow(productId, bomId);

        product.setActiveBomId(selected.getBomId());
        product.setBomVersion(selected.getBomVersion());
        repo.save(product);
        return productBomRepo.findById(selected.getBomId()).orElse(selected);
    }

    @Transactional
    public FinishedProduct updateBomVersion(Integer productId, String version) {
        String cleanVersion = cleanRequired(version, "BOM version is required");
        ProductBom bom = productBomRepo.findByProductProductIdAndBomVersionIgnoreCase(productId, cleanVersion)
                .orElseThrow(() -> new IllegalArgumentException("BOM version not found for this product: " + cleanVersion));
        activateBomVersion(productId, bom.getBomId());
        return findProductByIdOrThrow(productId);
    }

    public List<FinishedProduct> getAllProducts() {
        return repo.findAll();
    }

    @Transactional
    public BomComponent addBomComponent(Integer productId, Integer bomId, BomComponentRequest request) {
        FinishedProduct product = findProductByIdOrThrow(productId);
        ProductBom bom = findBomByProductOrThrow(productId, bomId);

        BomComponent component = new BomComponent();
        component.setProduct(product);
        component.setProductBom(bom);
        component.setComponentCode(cleanRequired(request.getComponentCode(), "Component code is required"));
        component.setComponentName(cleanRequired(request.getComponentName(), "Component name is required"));
        component.setQuantity(request.getQuantity());
        component.setUnitOfMeasure(cleanRequired(request.getUnitOfMeasure(), "Unit of measure is required"));
        component.setBomVersion(bom.getBomVersion());

        return bomComponentRepo.save(component);
    }

    @Transactional
    public BomComponent addBomComponent(Integer productId, BomComponentRequest request) {
        ProductBom bom = resolveActiveBom(productId)
                .orElseThrow(() -> new IllegalStateException("Assign an active BOM version before adding components"));
        return addBomComponent(productId, bom.getBomId(), request);
    }

    public ProductStructureResponse getProductStructure(Integer productId) {
        FinishedProduct product = findProductByIdOrThrow(productId);
        ProductBom activeBom = resolveActiveBom(productId).orElse(null);
        List<BomComponent> components = activeBom == null
                ? List.of()
                : bomComponentRepo.findByProductBomBomIdOrderByComponentIdAsc(activeBom.getBomId());
        return new ProductStructureResponse(product, activeBom, components);
    }

    public ProductStructureResponse getBomStructure(Integer productId, Integer bomId) {
        FinishedProduct product = findProductByIdOrThrow(productId);
        ProductBom bom = findBomByProductOrThrow(productId, bomId);
        List<BomComponent> components = bomComponentRepo.findByProductBomBomIdOrderByComponentIdAsc(bomId);
        return new ProductStructureResponse(product, bom, components);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }
        findProductByIdOrThrow(id);
        if (isProductUsedInProductionOrders(id)) {
            throw new IllegalStateException("Product cannot be deleted because it is used in production orders");
        }
        bomComponentRepo.deleteByProductProductId(id);
        productBomRepo.deleteByProductProductId(id);
        repo.deleteById(id);
    }

    public FinishedProduct getProductById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return repo.findById(id).orElse(null);
    }

    public FinishedProduct findProductByIdOrThrow(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    private boolean isProductUsedInProductionOrders(Integer productId) {
        try {
            return productionOrderClient.existsByProduct(productId);
        } catch (FeignException ex) {
            throw new IllegalStateException("Product cannot be deleted because production order usage could not be verified");
        }
    }

    private ProductBom findBomByProductOrThrow(Integer productId, Integer bomId) {
        if (bomId == null || bomId <= 0) {
            throw new IllegalArgumentException("BOM ID must be greater than 0");
        }
        return productBomRepo.findByProductProductIdAndBomId(productId, bomId)
                .orElseThrow(() -> new IllegalArgumentException("BOM version not found for this product: " + bomId));
    }

    private java.util.Optional<ProductBom> resolveActiveBom(Integer productId) {
        FinishedProduct product = findProductByIdOrThrow(productId);
        if (product.getActiveBomId() != null) {
            return productBomRepo.findByProductProductIdAndBomId(productId, product.getActiveBomId());
        }
        return java.util.Optional.empty();
    }

    private String cleanRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

}