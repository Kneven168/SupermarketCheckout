package com.haiilo.supermarket.checkout.repository;

import com.haiilo.supermarket.checkout.domain.OrderItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
}