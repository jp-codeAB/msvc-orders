package com.springcloud.msvc_items.domain.model;

import com.springcloud.msvc_items.shared.exception.custom.InvalidOrderStatusTransitionException;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    private Long id;
    private Long customerId;
    @Setter(AccessLevel.PRIVATE)
    private OrderStatus status;
    private Double totalAmount;
    @Setter
    private List<OrderItem> items;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public Order(Long customerId, List<OrderItem> items) {
        this.customerId = customerId;
        this.items = items;
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0.0;
        this.createAt = LocalDateTime.now();
    }

    public void calculateTotalAmount() {
        this.totalAmount = this.items.stream().mapToDouble(OrderItem::getSubTotal).sum();
    }

    public void changeStatus(OrderStatus newStatus) {
        if (this.status.canTransitionTo(newStatus)) {
            this.status = newStatus;
            this.updateAt = LocalDateTime.now();
        } else {
            throw new InvalidOrderStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", this.status, newStatus)
            );
        }
    }
}