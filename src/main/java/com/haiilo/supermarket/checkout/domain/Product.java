package com.haiilo.supermarket.checkout.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
public record Product(
    @Id
    String sku,
    String name,

    @Column("unit_price")
    int unitPrice,

    @Column("offer_quantity")
    Integer offerQuantity,

    @Column("offer_price")
    Integer offerPrice
) {}