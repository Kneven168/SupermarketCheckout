package com.haiilo.supermarket.checkout.repository;

import com.haiilo.supermarket.checkout.domain.Product;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, String> {
}
