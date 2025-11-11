package com.springcloud.msvc_items.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "Customer ID cannot be null")
    @Min(value = 1, message = "Customer ID must be positive")
    private Long customerId;

    @Valid
    @NotNull(message = "Item list cannot be null")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemRequest> items;
}
