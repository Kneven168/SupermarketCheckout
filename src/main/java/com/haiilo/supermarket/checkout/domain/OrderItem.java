package com.haiilo.supermarket.checkout.domain;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("order_items")
public record OrderItem(
    @Id
    Long id,

    @Column("order_id")
    Long orderId,

    @Column("product_sku")
    String productSku,

    int quantity
) {
}
