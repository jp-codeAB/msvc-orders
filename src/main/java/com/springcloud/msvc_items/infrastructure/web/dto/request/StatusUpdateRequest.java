package com.springcloud.msvc_items.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotBlank(message = "New status cannot be empty")
    @Pattern(
            regexp = "PENDING|CONFIRMED|IN_DISPATCH|DELIVERED|CANCELLED|FAILED",
            message = "Invalid order status value"
    )
    private String newStatus;
}
