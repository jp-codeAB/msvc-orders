package com.springcloud.msvc_items.domain.ports.out;

import com.springcloud.msvc_items.domain.model.Order;

public interface NotificationServicePort {
    void notifyOrderStatusChange(Order order, String customerEmail);
}