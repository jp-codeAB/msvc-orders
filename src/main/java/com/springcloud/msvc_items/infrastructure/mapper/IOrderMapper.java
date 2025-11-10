package com.springcloud.msvc_items.infrastructure.mapper;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderItem;
import com.springcloud.msvc_items.domain.model.OrderStatus;
import com.springcloud.msvc_items.infrastructure.persistence.entity.OrderEntity;
import com.springcloud.msvc_items.infrastructure.persistence.entity.OrderItemEntity;
import com.springcloud.msvc_items.infrastructure.web.dto.request.OrderItemRequest;
import com.springcloud.msvc_items.infrastructure.web.dto.request.OrderRequest;
import com.springcloud.msvc_items.infrastructure.web.dto.response.OrderResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IOrderMapper {

    // Domain ↔ Persistence
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    OrderEntity toEntity(Order domain);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToEnum")
    Order toDomain(OrderEntity entity);

    OrderItemEntity toItemEntity(OrderItem domain);

    OrderItem toItemDomain(OrderItemEntity entity);

    // Web ↔ Domain
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    Order toDomain(OrderRequest request);

    OrderResponse toResponse(Order domain);

    default OrderItem toDomainItem(OrderItemRequest request) {
        if (request == null) return null;

        return OrderItem.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
    }

    // Estado ↔ String
    @Named("mapStatusToString")
    default String mapStatusToString(OrderStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapStatusToEnum")
    default OrderStatus mapStatusToEnum(String status) {
        return status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
    }
}