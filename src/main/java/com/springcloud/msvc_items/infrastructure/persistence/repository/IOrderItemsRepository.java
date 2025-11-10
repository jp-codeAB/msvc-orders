package com.springcloud.msvc_items.infrastructure.persistence.repository;

import com.springcloud.msvc_items.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderItemsRepository extends JpaRepository<OrderItemEntity, Long> {
}
