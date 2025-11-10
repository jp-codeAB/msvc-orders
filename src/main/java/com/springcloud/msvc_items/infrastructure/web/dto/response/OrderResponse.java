package com.springcloud.msvc_items.infrastructure.web.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String status;
    private Double totalAmount;
    private List<OrderItemResponse> items;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}