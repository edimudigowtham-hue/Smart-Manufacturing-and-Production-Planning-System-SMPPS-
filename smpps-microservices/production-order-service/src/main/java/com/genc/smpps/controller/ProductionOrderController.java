package com.genc.smpps.controller;

import com.genc.smpps.model.OrderStatus;
import com.genc.smpps.model.ProductionOrder;
import com.genc.smpps.service.ProductionOrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class ProductionOrderController {

    private final ProductionOrderService service;

    public ProductionOrderController(ProductionOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductionOrder> getOrders() {
        return service.getAllOrders();
    }

    @GetMapping("/{id}")
    public ProductionOrder getOrder(@PathVariable int id) {
        return service.findOrderByIdOrThrow(id);
    }

    @PostMapping
    public ProductionOrder createOrder(@Valid @RequestBody ProductionOrder order) {
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(OrderStatus.PLANNED);
        }
        return service.createProductionOrder(order);
    }

    @PutMapping("/{id}")
    public ProductionOrder updateOrder(@PathVariable int id, @Valid @RequestBody ProductionOrder order) {
        order.setOrderId(id);
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(service.findOrderByIdOrThrow(id).getOrderStatus());
        }
        return service.updateOrder(order);
    }

    @GetMapping("/{id}/progress")
    public Map<String, String> progress(@PathVariable int id) {
        return Map.of("progress", service.getOrderProgress(id));
    }

    @GetMapping("/product/{productId}/exists")
    public boolean existsByProduct(@PathVariable int productId) {
        return service.existsByProductId(productId);
    }

    @PostMapping("/{id}/schedule")
    public ProductionOrder scheduleWorkCenter(@PathVariable int id, @RequestBody Map<String, String> body) {
        return service.scheduleWorkCenter(id, body.get("workCenterId"));
    }

    @PostMapping("/{id}/release")
    public ProductionOrder releaseOrder(@PathVariable int id) {
        return service.releaseOrder(id);
    }

    @PostMapping("/{id}/start")
    public ProductionOrder startOrder(@PathVariable int id, @RequestBody(required = false) Map<String, String> body) {
        return service.startOrder(id, body == null ? null : body.get("workCenterId"));
    }

    @PostMapping("/{id}/produced-quantity")
    public ProductionOrder updateProducedQuantity(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        return service.updateProducedQuantity(id, body.get("producedQuantity"));
    }

    @PostMapping("/{id}/complete")
    public ProductionOrder completeOrder(@PathVariable int id) {
        return service.completeOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public ProductionOrder cancelOrder(@PathVariable int id) {
        return service.cancelOrder(id);
    }

}