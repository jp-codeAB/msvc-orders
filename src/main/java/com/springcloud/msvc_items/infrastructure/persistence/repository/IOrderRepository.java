package com.springcloud.msvc_items.infrastructure.persistence.repository;

import com.springcloud.msvc_items.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IOrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerId(Long customerId);
    List<OrderEntity> findByStatus(String status);
}