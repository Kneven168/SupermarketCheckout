package com.haiilo.supermarket.checkout.service;

import static com.haiilo.supermarket.checkout.TestConstants.SKU_A;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.repository.ProductRepository;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ReactiveRedisTemplate<String, Product> productRedisTemplate;

  @Mock
  private ReactiveValueOperations<String, Product> reactiveValueOperations;

  @InjectMocks
  private ProductService productService;

  private static Product testProduct;

  @BeforeAll
  static void setUp() {
    testProduct = new Product(1L, SKU_A, "Apple", 50, 3, 130);
  }

  @Test
  @DisplayName("getProductBySku should return product from cache if present")
  void getProductBySku_FoundInCache() {
    when(productRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(reactiveValueOperations.get(SKU_A)).thenReturn(Mono.just(testProduct));

    StepVerifier.create(productService.getProductBySku(SKU_A))
        .expectNext(testProduct)
        .verifyComplete();

    verify(productRepository, never()).findBySku(anyString());
  }

  @Test
  @DisplayName("getProductBySku should return product from DB and cache it if not in cache")
  void getProductBySku_NotFoundInCache_FoundInDb() {
    when(productRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(reactiveValueOperations.get(SKU_A)).thenReturn(Mono.empty());
    when(productRepository.findBySku(SKU_A)).thenReturn(Mono.just(testProduct));
    when(reactiveValueOperations.set(eq(SKU_A), eq(testProduct), any(Duration.class))).thenReturn(
        Mono.just(true));

    StepVerifier.create(productService.getProductBySku(SKU_A))
        .expectNext(testProduct)
        .verifyComplete();

    verify(productRepository, times(1)).findBySku(SKU_A);
    verify(reactiveValueOperations, times(1)).set(eq(SKU_A), eq(testProduct), any(Duration.class));
  }

  @Test
  @DisplayName("createProduct should save to repository and cache")
  void createProduct_Success() {
    when(productRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(productRepository.save(testProduct)).thenReturn(Mono.just(testProduct));
    when(reactiveValueOperations.set(eq(SKU_A), eq(testProduct), any(Duration.class))).thenReturn(
        Mono.just(true));

    StepVerifier.create(productService.createProduct(testProduct))
        .expectNext(testProduct)
        .verifyComplete();

    verify(productRepository, times(1)).save(testProduct);
    verify(reactiveValueOperations, times(1)).set(eq(SKU_A), eq(testProduct), any(Duration.class));
  }

  @Test
  @DisplayName("updateProduct should update product successfully")
  void updateProduct_Success() {
    when(productRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(productRepository.updateBySku(testProduct)).thenReturn(Mono.just(true));
    when(reactiveValueOperations.set(eq(SKU_A), eq(testProduct), any(Duration.class))).thenReturn(
        Mono.just(true));

    StepVerifier.create(productService.updateProduct(SKU_A, testProduct))
        .expectNext(testProduct)
        .verifyComplete();

    verify(productRepository, times(1)).updateBySku(testProduct);
    verify(reactiveValueOperations, times(1)).set(eq(SKU_A), eq(testProduct), any(Duration.class));
  }

  @Test
  @DisplayName("updateProduct should return error on SKU mismatch")
  void updateProduct_SkuMismatch_Error() {
    Product mismatchedProduct = new Product(2L, "B", "Banana", 30, null, null);

    StepVerifier.create(productService.updateProduct(SKU_A, mismatchedProduct))
        .expectError(IllegalArgumentException.class)
        .verify();

    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("deleteProduct should delete from repository and cache")
  void deleteProduct_Success() {
    when(productRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(productRepository.deleteBySku(SKU_A)).thenReturn(Mono.empty());
    when(reactiveValueOperations.delete(SKU_A)).thenReturn(Mono.just(true));

    StepVerifier.create(productService.deleteProduct(SKU_A))
        .verifyComplete();

    verify(productRepository, times(1)).deleteBySku(SKU_A);
    verify(reactiveValueOperations, times(1)).delete(SKU_A);
  }
}

