package com.springcloud.msvc_items.domain.ports.out;

import com.springcloud.msvc_items.domain.model.OrderItem;
import java.util.List;
import java.util.Map;

public interface InventoryServicePort {
    boolean checkAndReserveStock(List<OrderItem> items);
    void reverseStock(List<OrderItem> items);
    Map<Long, Double> getProductsPrice(List<Long> productIds);
}