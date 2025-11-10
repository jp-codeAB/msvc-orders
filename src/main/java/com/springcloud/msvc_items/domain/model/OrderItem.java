package com.springcloud.msvc_items.domain.model;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class OrderItem {
    Long id;
    Long productId;
    int quantity;

    @Setter
    Double price;

    public Double getSubTotal() {
        if (price == null || quantity <= 0) {
            return 0.0;
        }
        return (double) quantity * price;
    }
}