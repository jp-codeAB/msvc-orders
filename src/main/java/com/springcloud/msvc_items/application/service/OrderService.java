package com.springcloud.msvc_items.application.service;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderItem;
import com.springcloud.msvc_items.domain.model.OrderStatus;
import com.springcloud.msvc_items.domain.ports.in.CreateOrderUseCase;
import com.springcloud.msvc_items.domain.ports.in.IManageOrderUseCase;
import com.springcloud.msvc_items.domain.ports.out.InventoryServicePort;
import com.springcloud.msvc_items.domain.ports.out.OrderRepositoryPort;
import com.springcloud.msvc_items.shared.exception.custom.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements CreateOrderUseCase, IManageOrderUseCase {

    private final OrderRepositoryPort repositoryPort;
    private final InventoryServicePort inventoryServicePort;

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders(){
        return repositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrdersByCustomer(Long customerId) {
        return repositoryPort.findAllByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Long id) {
        return repositoryPort.findById(id);
    }

    @Override
    public Order createOrder(Order order){
        // 1. Obtención de precios y cálculo de montos
        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProductId).collect(Collectors.toList());
        Map<Long, Double> prices = inventoryServicePort.getProductsPrice(productIds);

        for (OrderItem item : order.getItems()) {
            Double price = prices.get(item.getProductId());
            if (price == null || price <= 0) {
                throw new ProductNotFoundException(
                        String.format("Product with ID %d not found or price not available.", item.getProductId()));
            }
            item.setPrice(price);
        }
        order.calculateTotalAmount();

        try {
            inventoryServicePort.checkAndReserveStock(order.getItems());

        } catch (InsufficientStockException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Order creation failed due to Inventory service dependency error.", ex);
        }
        Order savedOrder = repositoryPort.save(order);
        return savedOrder;
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = repositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format("Order with ID %d not found", orderId)));
        order.changeStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) {
            inventoryServicePort.reverseStock(order.getItems());
        }
        Order updatedOrder = repositoryPort.save(order);
        return updatedOrder;
    }

    @Override
    public void deleteOrder(Long orderId) {
        Order order = repositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format("Order with ID %d not found", orderId)));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusTransitionException(
                    "Only PENDING orders can be deleted (current status: " + order.getStatus() + "). Use CANCELLED instead.");
        }
        inventoryServicePort.reverseStock(order.getItems());
        repositoryPort.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(Long orderId, Long userId) {
        return repositoryPort.findCustomerIdById(orderId)
                .map(ownerId -> ownerId.equals(userId))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findConfirmedOrders() {
        return repositoryPort.findAllByStatus(OrderStatus.CONFIRMED);
    }
}
