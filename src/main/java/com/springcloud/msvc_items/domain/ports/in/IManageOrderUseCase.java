package com.springcloud.msvc_items.domain.ports.in;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface IManageOrderUseCase {
    List<Order> findAllOrders();
    List<Order> findAllOrdersByCustomer(Long customerId);
    Optional<Order> findOrderById(Long id);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    void deleteOrder(Long orderId);
    boolean isOwner(Long orderId, Long userId);
    List<Order> findConfirmedOrders();
}
