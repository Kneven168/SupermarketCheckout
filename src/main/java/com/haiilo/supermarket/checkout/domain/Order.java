package com.haiilo.supermarket.checkout.domain;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@Table("orders")
public record Order(
    @Id
    Long id,

    @Column("final_price")
    int finalPrice,

    @Column("created_at")
    Instant createdAt
){
}
