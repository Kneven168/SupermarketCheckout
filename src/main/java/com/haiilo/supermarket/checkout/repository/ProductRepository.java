package com.haiilo.supermarket.checkout.repository;

import com.haiilo.supermarket.checkout.domain.Product;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, Long> {

  Mono<Product> findBySku(String sku);
  Mono<Void> deleteBySku(String sku);

  @Modifying
  @Query("UPDATE products SET sku = :#{#product.name}, " +
      "unit_price = :#{#product.unitPrice}, offer_quantity = :#{#product.offerQuantity}, " +
      "offer_price = :#{#product.offerPrice} WHERE sku = :#{#product.sku}")
  Mono<Boolean> updateBySku(@Param("product") Product product);
}
