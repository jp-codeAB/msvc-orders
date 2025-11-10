package com.springcloud.msvc_items.infrastructure.persistence.adapter;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderStatus;
import com.springcloud.msvc_items.domain.ports.out.OrderRepositoryPort;
import com.springcloud.msvc_items.infrastructure.mapper.IOrderMapper;
import com.springcloud.msvc_items.infrastructure.persistence.entity.OrderEntity;
import com.springcloud.msvc_items.infrastructure.persistence.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderRepositoryPort {

    private final IOrderRepository jpaRepository;
    private final IOrderMapper mapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAllByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        jpaRepository.delete(entity);
    }

    @Override
    public Optional<Long> findCustomerIdById(Long orderId) {
        return jpaRepository.findById(orderId)
                .map(OrderEntity::getCustomerId);
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}