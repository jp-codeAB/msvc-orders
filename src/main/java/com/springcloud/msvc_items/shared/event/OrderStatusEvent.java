package com.springcloud.msvc_items.shared.event;

import java.time.LocalDateTime;

public record OrderStatusEvent(
        Long orderId,
        String customerEmail,
        String newStatus,
        LocalDateTime eventTimestamp
) {}
