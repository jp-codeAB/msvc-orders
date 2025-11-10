package com.springcloud.msvc_items.infrastructure.integration.adapter;

import com.springcloud.msvc_items.domain.model.OrderItem;
import com.springcloud.msvc_items.domain.ports.out.InventoryServicePort;
import com.springcloud.msvc_items.infrastructure.integration.client.InventoryFeignClient;
import com.springcloud.msvc_items.shared.exception.custom.InsufficientStockException;
import com.springcloud.msvc_items.shared.exception.custom.InventoryServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRestAdapter implements InventoryServicePort {

    private final InventoryFeignClient inventoryClient;

    @Override
    public boolean checkAndReserveStock(List<OrderItem> items) {
        try {
            Boolean success = inventoryClient.reserveStock(items);
            return success != null && success;

        } catch (FeignException e) {
            log.error("Error de comunicación o stock insuficiente con msvc-inventory: {}", e.getMessage());

            if (e.status() == 409) {

                String errorMessage = e.contentUTF8().isEmpty() ? "Insufficient stock." : e.contentUTF8();
                throw new InsufficientStockException("Stock check failed: " + errorMessage, e);

            } else if (e.status() == 403) {
                throw new InventoryServiceException("Authorization failed with Inventory Service (403).", e);

            } else if (e.status() >= 500) {
                throw new InventoryServiceException("Inventory Service returned an internal error (5xx).", e);
            }
            throw new InventoryServiceException("Unexpected error communicating with Inventory Service.", e);
        }
    }

    @Override
    public void reverseStock(List<OrderItem> items) {
        try {
            inventoryClient.reverseStock(items);
        } catch (FeignException ex) {
            if (ex.status() == 409) {
                log.warn("Stock conflict detected in inventory: {}", ex.contentUTF8());
                return;
            }
            throw new InventoryServiceException("Inventory service unavailable.", ex);
        }
        catch (Exception e) {
            log.error("Unexpected error during stock reversal: {}", e.getMessage());
            throw new InventoryServiceException("Unexpected error during stock reversal.", e);
        }
    }

    @Override
    public Map<Long, Double> getProductsPrice(List<Long> productIds) {
        try {
            return inventoryClient.getPricesByIds(productIds);
        } catch (FeignException e) {
            log.error("Communication error while fetching prices from msvc-products: {}", e.getMessage());
            return Map.of();
        }
    }
}