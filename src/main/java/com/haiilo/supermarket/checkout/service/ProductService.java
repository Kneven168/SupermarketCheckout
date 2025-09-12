package com.haiilo.supermarket.checkout.service;


import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository repository;

  private final ReactiveRedisTemplate<String, Product> productRedisTemplate;

  private static final Duration PRODUCT_CACHE_TTL = Duration.ofHours(1);

  public Mono<Product> getProductBySku(String sku) {
    return productRedisTemplate.opsForValue().get(sku)
        .switchIfEmpty(
            repository.findById(sku)
                .flatMap(product ->
                    productRedisTemplate.opsForValue()
                        .set(sku, product, PRODUCT_CACHE_TTL)
                        .thenReturn(product)
                )
        );
  }

  public Mono<Product> saveProduct(Product product) {
    return repository.save(product)
        .flatMap(savedProduct ->
            productRedisTemplate.opsForValue()
                .set(savedProduct.sku(), savedProduct, PRODUCT_CACHE_TTL)
                .thenReturn(savedProduct)
        )
        .doOnSuccess(savedProduct ->
            log.info("Product with SKU '{}' saved successfully.", savedProduct.sku()));
  }

  public Mono<Product> updateProduct(Product product, String sku) {
    if (product.sku() != null && !product.sku().equals(sku)) {
      return Mono.error(new IllegalArgumentException("SKU in path does not match SKU in body"));
    }
    return this.saveProduct(product);

  }

  public Mono<Void> deleteProduct(String sku) {
    return repository.deleteById(sku)
        .then(productRedisTemplate.opsForValue().delete(sku))
        .doOnSuccess(v -> log.info("Product with SKU '{}' successfully deleted from DB and cache.", sku))
        .then();
  }
}
