package com.springcloud.msvc_items.infrastructure.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "Product ID cannot be null")
    @Min(value = 1, message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Minimum quantity is 1")
    private Integer quantity;
}

