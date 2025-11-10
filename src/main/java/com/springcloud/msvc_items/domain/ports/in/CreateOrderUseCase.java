package com.springcloud.msvc_items.domain.ports.in;

import com.springcloud.msvc_items.domain.model.Order;

public interface CreateOrderUseCase {
    Order createOrder(Order order);
}