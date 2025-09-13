package com.haiilo.supermarket.checkout.util;

import static com.haiilo.supermarket.checkout.TestConstants.SKU_A;
import static com.haiilo.supermarket.checkout.TestConstants.SKU_B;
import static com.haiilo.supermarket.checkout.TestConstants.SKU_C;
import static com.haiilo.supermarket.checkout.util.AppConstants.BASKET_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.service.ProductService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
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
import reactor.util.context.Context;

@ExtendWith(MockitoExtension.class)
class BasketHelperTest {

  @Mock
  private ProductService productService;

  @Mock
  private ReactiveRedisTemplate<String, Basket> basketRedisTemplate;

  @Mock
  private ReactiveValueOperations<String, Basket> reactiveValueOperations;

  @InjectMocks
  private BasketHelper basketHelper;

  private Product productA;
  private Product productB;
  private Product productC;

  @BeforeEach
  void setUp() {
    productA = new Product(1L, SKU_A, "Apple", 20, 3, 130);
    productB = new Product(2L, SKU_B, "Banana", 25, 3, 45);
    productC = new Product(3L, SKU_C, "Cherry", 40, 2, 60);

    when(basketRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
  }

  @Test
  @DisplayName("should calculate total for items without special offer")
  void calculateTotal_noOffer() {
    when(productService.getProductBySku(SKU_A)).thenReturn(Mono.just(productA));
    when(productService.getProductBySku(SKU_B)).thenReturn(Mono.just(productB));
    when(productService.getProductBySku(SKU_C)).thenReturn(Mono.just(productC));

    Basket basket = new Basket("basket-1");
    basket.addItem(SKU_A); // 1 * 20 = 20
    basket.addItem(SKU_B); // 1 * 25 = 25
    basket.addItem(SKU_C); // 1 * 40 = 40
    // Total = 85

    when(reactiveValueOperations.set(anyString(), any(Basket.class),
        any(Duration.class))).thenReturn(Mono.just(true));

    StepVerifier.create(basketHelper.getTotalPrice(basket).contextWrite(
            Context.of(BASKET_ID, basket.getId())))
        .expectNextMatches(p -> p == 85)
        .verifyComplete();
  }

  @Test
  @DisplayName("should apply special offer correctly")
  void calculateTotal_withOffer() {
    when(productService.getProductBySku(SKU_B)).thenReturn(Mono.just(productB));
    Basket basket = new Basket("basket-2");
    basket.addItem(SKU_B);
    basket.addItem(SKU_B); // 2 * 25 = 50

    when(reactiveValueOperations.set(anyString(), any(Basket.class),
        any(Duration.class))).thenReturn(Mono.just(true));

    StepVerifier.create(
            basketHelper.getTotalPrice(basket).contextWrite(Context.of(BASKET_ID, basket.getId())))
        .expectNextMatches(p -> p == 50)
        .verifyComplete();
  }

  @Test
  @DisplayName("should apply special offer and add remaining items")
  void calculateTotal_withOfferAndRemainder() {
    when(productService.getProductBySku(SKU_A)).thenReturn(Mono.just(productA));

    Basket basket = new Basket("basket-3");
    basket.addItem(SKU_A);
    basket.addItem(SKU_A);
    basket.addItem(SKU_A); // 3 * A = 130
    basket.addItem(SKU_A);
    basket.addItem(SKU_A); // 2 * 20 = 40
    // Total = 170

    when(reactiveValueOperations.set(anyString(), any(Basket.class),
        any(Duration.class))).thenReturn(Mono.just(true));

    StepVerifier.create(
            basketHelper.getTotalPrice(basket).contextWrite(Context.of(BASKET_ID, basket.getId())))
        .expectNextMatches(p -> p == 170)
        .verifyComplete();
  }

  @Test
  @DisplayName("should calculate total for a mixed basket with multiple offers")
  void calculateTotal_mixedBasket() {
    when(productService.getProductBySku(SKU_A)).thenReturn(Mono.just(productA));
    when(productService.getProductBySku(SKU_B)).thenReturn(Mono.just(productB));

    Basket basket = new Basket("basket-4");
    basket.addItem(SKU_A);
    basket.addItem(SKU_B);
    basket.addItem(SKU_A);
    basket.addItem(SKU_B); // 2* B = 50
    basket.addItem(SKU_A); // 3 * A = 130,

    when(reactiveValueOperations.set(anyString(), any(Basket.class),
        any(Duration.class))).thenReturn(Mono.just(true));

    StepVerifier.create(
            basketHelper.getTotalPrice(basket).contextWrite(Context.of(BASKET_ID, basket.getId())))
        .expectNextMatches(p -> p == 180)
        .verifyComplete();
  }
}

