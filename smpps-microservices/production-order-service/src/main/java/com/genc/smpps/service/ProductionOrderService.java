package com.genc.smpps.service;

import com.genc.smpps.client.MachineClient;
import com.genc.smpps.client.ProductClient;
import com.genc.smpps.dto.FactoryMachineDto;
import com.genc.smpps.dto.ProductDto;
import com.genc.smpps.model.OrderStatus;
import com.genc.smpps.model.ProductionOrder;
import com.genc.smpps.repo.ProductionOrderRepository;
import feign.FeignException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductionOrderService {
    private final ProductionOrderRepository repo;
    private final ProductClient productClient;
    private final MachineClient machineClient;

    public ProductionOrderService(ProductionOrderRepository repo, ProductClient productClient, MachineClient machineClient) {
        this.repo = repo;
        this.productClient = productClient;
        this.machineClient = machineClient;
    }

    public ProductionOrder createProductionOrder(ProductionOrder order) {
        validateProduct(order.getProductId());
        if (order.getWorkCenterId() != null && !order.getWorkCenterId().trim().isEmpty()) {
            order.setWorkCenterId(validateAvailableMachine(order.getWorkCenterId()).toString());
        }
        order.setOrderStatus(OrderStatus.PLANNED);
        order.setProducedQuantity(0);
        return repo.save(order);
    }

    public ProductionOrder updateOrder(ProductionOrder updatedOrder) {
        ProductionOrder existing = findOrderOrThrow(updatedOrder.getOrderId());
        if (existing.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be updated");
        }
        if (existing.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled orders cannot be updated");
        }
        validateProduct(updatedOrder.getProductId());
        existing.setProductId(updatedOrder.getProductId());
        existing.setPlannedQuantity(updatedOrder.getPlannedQuantity());
        existing.setStartDate(updatedOrder.getStartDate());
        existing.setEndDate(updatedOrder.getEndDate());
        if (updatedOrder.getWorkCenterId() == null || updatedOrder.getWorkCenterId().trim().isEmpty()) {
            existing.setWorkCenterId(null);
        } else {
            existing.setWorkCenterId(validateAvailableMachine(updatedOrder.getWorkCenterId()).toString());
        }
        existing.setProducedQuantity(hasAssignedMachine(existing) ? updatedOrder.getProducedQuantity() : 0);
        return repo.save(existing);
    }

    public ProductionOrder updateProducedQuantity(Integer id, Integer producedQuantity) {
        ProductionOrder order = findOrderOrThrow(id);
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be updated");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled orders cannot be updated");
        }
        if (!hasAssignedMachine(order)) {
            throw new IllegalStateException("Assign a machine before updating produced quantity");
        }
        if (producedQuantity == null || producedQuantity < 0) {
            throw new IllegalArgumentException("Produced quantity cannot be negative");
        }
        if (producedQuantity > order.getPlannedQuantity()) {
            throw new IllegalArgumentException("Produced quantity cannot be greater than planned quantity");
        }
        order.setProducedQuantity(producedQuantity);
        return repo.save(order);
    }

    public ProductionOrder scheduleWorkCenter(Integer id, String workCenterId) {
        ProductionOrder order = findOrderOrThrow(id);
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be scheduled");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled orders cannot be scheduled");
        }
        if (workCenterId == null || workCenterId.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine is required");
        }

        Integer machineId = validateAvailableMachine(workCenterId);
        order.setWorkCenterId(machineId.toString());
        return repo.save(order);
    }

    private Integer validateAvailableMachine(String workCenterId) {
        String machineIdValue = workCenterId.trim();
        if (!machineIdValue.matches("\\d+")) {
            throw new IllegalArgumentException("Select a valid machine ID");
        }

        Integer machineId = Integer.valueOf(machineIdValue);
        FactoryMachineDto machine;
        try {
            machine = machineClient.getMachine(machineId);
        } catch (FeignException ex) {
            throw new IllegalArgumentException("Machine not found: " + machineId);
        }
        if (machine == null || machine.machineId() == null) {
            throw new IllegalArgumentException("Machine not found: " + machineId);
        }
        if (!"AVAILABLE".equalsIgnoreCase(machine.availability())) {
            throw new IllegalStateException("Machine is not available for scheduling: " + machineId);
        }
        return machineId;
    }

    public ProductionOrder releaseOrder(Integer id) {
        ProductionOrder order = findOrderOrThrow(id);
        if (order.getOrderStatus() != OrderStatus.PLANNED) {
            throw new IllegalStateException("Only PLANNED orders can be released");
        }
        order.setOrderStatus(OrderStatus.RELEASED);
        return repo.save(order);
    }

    public ProductionOrder startOrder(Integer id) {
        return startOrder(id, null);
    }

    public ProductionOrder startOrder(Integer id, String workCenterId) {
        ProductionOrder order = findOrderOrThrow(id);
        if (order.getOrderStatus() != OrderStatus.RELEASED) {
            throw new IllegalStateException("Only RELEASED orders can be started");
        }
        String selectedWorkCenter = workCenterId;
        if (selectedWorkCenter == null || selectedWorkCenter.trim().isEmpty()) {
            selectedWorkCenter = order.getWorkCenterId();
        }
        if (selectedWorkCenter == null || selectedWorkCenter.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine is required before starting production");
        }
        order.setWorkCenterId(validateAvailableMachine(selectedWorkCenter).toString());
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        return repo.save(order);
    }

    public ProductionOrder completeOrder(Integer id) {
        ProductionOrder order = findOrderOrThrow(id);
        if (order.getOrderStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS orders can be completed");
        }
        if (!order.getProducedQuantity().equals(order.getPlannedQuantity())) {
            throw new IllegalStateException("Produced quantity must match planned quantity before completion");
        }
        order.setOrderStatus(OrderStatus.COMPLETED);
        return repo.save(order);
    }

    public ProductionOrder cancelOrder(Integer id) {
        ProductionOrder order = findOrderOrThrow(id);
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be cancelled");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        return repo.save(order);
    }

    public List<ProductionOrder> getAllOrders() {
        return repo.findAll();
    }

    public String getOrderProgress(Integer id) {
        ProductionOrder order = findOrderOrThrow(id);
        return "Order ID: " + id + ", Status: " + order.getOrderStatus() + ", Produced: "
                + order.getProducedQuantity() + "/" + order.getPlannedQuantity();
    }

    public boolean existsByProductId(Integer productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }
        return repo.existsByProductId(productId);
    }

    public ProductionOrder getOrderById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public ProductionOrder findOrderByIdOrThrow(Integer id) {
        return findOrderOrThrow(id);
    }

    private ProductionOrder findOrderOrThrow(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be greater than 0");
        }
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    private void validateProduct(Integer productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0");
        }
        ProductDto product = productClient.getProduct(productId);
        if (product == null || product.productId() == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (!"ACTIVE".equalsIgnoreCase(product.productStatus())) {
            throw new IllegalStateException("Only ACTIVE products can be used for production orders");
        }
    }

    private boolean hasAssignedMachine(ProductionOrder order) {
        return order.getWorkCenterId() != null && !order.getWorkCenterId().trim().isEmpty();
    }

}