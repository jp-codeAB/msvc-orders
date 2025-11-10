package com.springcloud.msvc_items.infrastructure.web.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long productId;
    private Integer quantity;
    private Double price;
    private Double subTotal;
}