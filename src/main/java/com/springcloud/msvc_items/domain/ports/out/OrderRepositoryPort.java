package com.springcloud.msvc_items.domain.ports.out;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    List<Order> findAllByCustomerId(Long customerId);
    void delete(Order order);
    Optional<Long> findCustomerIdById(Long orderId);
    List<Order> findAllByStatus(OrderStatus status);
}