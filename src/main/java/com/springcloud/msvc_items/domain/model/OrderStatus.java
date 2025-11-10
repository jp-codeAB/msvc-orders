package com.springcloud.msvc_items.domain.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    IN_DISPATCH("IN_DISPATCH"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return switch (this) {
            case PENDING -> nextStatus == CONFIRMED || nextStatus == CANCELLED;
            case CONFIRMED -> nextStatus == IN_DISPATCH || nextStatus == CANCELLED;
            case IN_DISPATCH -> nextStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}