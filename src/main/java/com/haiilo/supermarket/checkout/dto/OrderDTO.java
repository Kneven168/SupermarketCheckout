package com.haiilo.supermarket.checkout.dto;

import com.haiilo.supermarket.checkout.domain.OrderItem;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderDTO(
    Long id,
    int finalPrice,
    List<OrderItem> items,
    Instant createdAt
){
}
