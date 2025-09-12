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
        .doOnSuccess(product -> {
          if (product != null) {
            log.info("Product with SKU '{}' found in cache.", sku);
          }
        })
        .switchIfEmpty(
            Mono.defer(() -> {
              log.info("Product with SKU '{}' not found in cache. Fetching from DB.", sku);
              return repository.findById(sku)
                  .flatMap(this::saveToCache);
            })
        );
  }

  public Mono<Product> createProduct(Product product) {
    return repository.save(product)
        .flatMap(this::saveToCache)
        .doOnSuccess(savedProduct ->
            log.info("Product with SKU '{}' saved successfully.", savedProduct.sku()));
  }

  public Mono<Product> updateProduct(String sku, Product product) {
    if (product.sku() != null && !product.sku().equals(sku)) {
      return Mono.error(new IllegalArgumentException("SKU in path does not match SKU in body"));
    }
    return this.createProduct(product);

  }

  public Mono<Void> deleteProduct(String sku) {
    return repository.deleteById(sku)
        .then(productRedisTemplate.opsForValue().delete(sku))
        .doOnSuccess(
            v -> log.info("Product with SKU '{}' successfully deleted from DB and cache.", sku))
        .then();
  }

  private Mono<Product> saveToCache(Product product) {
    return productRedisTemplate.opsForValue()
        .set(product.sku(), product, PRODUCT_CACHE_TTL)
        .doOnSuccess(v -> log.info("Product with SKU '{}' saved to cache.", product.sku()))
        .thenReturn(product);
  }
}
