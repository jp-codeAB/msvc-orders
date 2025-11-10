package com.springcloud.msvc_items.infrastructure.security;

import com.springcloud.msvc_items.domain.model.OrderStatus;
import com.springcloud.msvc_items.domain.ports.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("orderAccessGuard")
@RequiredArgsConstructor
public class OrderAccessGuard {

    private final OrderRepositoryPort orderRepositoryPort;

    public boolean isOwner(Long orderId, Long userId) {
        return orderRepositoryPort.findCustomerIdById(orderId)
                .map(ownerId -> ownerId.equals(userId))
                .orElse(false);
    }

    public boolean isPendingOwner(Long orderId, Long userId) {
        return orderRepositoryPort.findById(orderId)
                .filter(order -> order.getCustomerId().equals(userId))
                .map(order -> order.getStatus() == OrderStatus.PENDING)
                .orElse(false);
    }
}
