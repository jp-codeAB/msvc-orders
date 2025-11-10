package com.springcloud.msvc_items.infrastructure.integration.client;

import com.springcloud.msvc_items.domain.model.OrderItem;
import com.springcloud.msvc_items.infrastructure.integration.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "msvc-inventory", configuration = FeignClientConfiguration.class)
public interface InventoryFeignClient {

    @PostMapping("/products/reserve-stock")
    public Boolean reserveStock(@RequestBody List<OrderItem> items);

    @PostMapping("/products/reverse-stock")
    public void reverseStock(@RequestBody List<OrderItem> items);


    @GetMapping(value = "/products/prices")
    public Map<Long, Double> getPricesByIds(@RequestParam(name = "productIds") List<Long> productIds);
}