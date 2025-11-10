package com.springcloud.msvc_items.infrastructure.integration.adapter;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.ports.out.NotificationServicePort;
import com.springcloud.msvc_items.shared.event.OrderStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageAdapter implements NotificationServicePort {

    private final StreamBridge streamBridge;
    private static final String OUTPUT_BINDING_NAME = "orderStatusUpdate-out-0";

    @Override
    public void notifyOrderStatusChange(Order order, String customerEmail) {

        OrderStatusEvent event = new OrderStatusEvent(
                order.getId(),
                customerEmail,
                order.getStatus().name(),
                LocalDateTime.now()
        );
        try {
            boolean sent = streamBridge.send(OUTPUT_BINDING_NAME, event);

            if (sent) {
                log.info("Asynchronous Notification Sent. Order ID: {} to Status: {} for Email: {}",
                        event.orderId(), event.newStatus(), event.customerEmail());
            } else {
                log.error("Failed to send Order Status Event for Order ID: {} - StreamBridge returned false.", event.orderId());
            }
        } catch (Exception e) {
            log.error("Failed to publish Order Status Event for Order ID: {}. Error: {}", event.orderId(), e.getMessage(), e);
        }
    }
}