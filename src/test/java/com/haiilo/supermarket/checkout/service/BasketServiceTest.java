package com.haiilo.supermarket.checkout.service;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.util.BasketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.haiilo.supermarket.checkout.TestConstants.SKU_A;
import static com.haiilo.supermarket.checkout.TestConstants.SKU_B;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

  public static final String BASKET_ID = "test-basket-id";

  @Mock
  private BasketHelper basketHelper;

  @Mock
  private ReactiveRedisTemplate<String, Basket> basketRedisTemplate;

  @Mock
  private ReactiveValueOperations<String, Basket> reactiveValueOperations;

  @InjectMocks
  private BasketService basketService;

  private Basket testBasket;

  @BeforeEach
  void setUp() {
    testBasket = new Basket(BASKET_ID);
  }

  @Test
  @DisplayName("should create a new basket successfully")
  void createBasket_success() {
    when(basketRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(reactiveValueOperations.set(anyString(), any(Basket.class), any())).thenReturn(Mono.just(true));

    StepVerifier.create(basketService.createBasket())
        .expectNextMatches(basket -> basket.getId() != null && basket.getItems().isEmpty())
        .verifyComplete();
  }

  @Test
  @DisplayName("addItemToBasket should return total price from helper")
  void addItemToBasket_success() {
    when(basketHelper.getBasketById(BASKET_ID)).thenReturn(Mono.just(testBasket));
    when(basketHelper.getProductBySku(SKU_A)).thenReturn(Mono.just(new Product(1L, SKU_A, "Apple", 30, 5, 100)));
    when(basketHelper.getTotalPrice(any(Basket.class))).thenReturn(Mono.just(30));

    StepVerifier.create(basketService.addItemToBasket(BASKET_ID, SKU_A))
        .expectNext(30)
        .verifyComplete();

    verify(basketHelper, times(1)).getTotalPrice(testBasket);
  }

  @Test
  @DisplayName("addItemToBasket should fail when product not found")
  void addItemToBasket_productNotFound_fails() {
    when(basketHelper.getBasketById(BASKET_ID)).thenReturn(Mono.just(testBasket));
    when(basketHelper.getProductBySku(SKU_B)).thenReturn(Mono.error(new ResponseStatusException(NOT_FOUND)));

    StepVerifier.create(basketService.addItemToBasket(BASKET_ID, SKU_B))
        .expectError(ResponseStatusException.class)
        .verify();
  }

  @Test
  @DisplayName("removeItemFromBasket should return total price from helper")
  void removeItemFromBasket_success() {
    testBasket.addItem(SKU_A);
    when(basketHelper.getBasketById(BASKET_ID)).thenReturn(Mono.just(testBasket));
    when(basketHelper.getTotalPrice(any(Basket.class))).thenReturn(Mono.just(0));

    StepVerifier.create(basketService.removeItemFromBasket(BASKET_ID, SKU_A))
        .expectNext(0)
        .verifyComplete();

    verify(basketHelper, times(1)).getTotalPrice(testBasket);
  }

  @Test
  @DisplayName("removeItemFromBasket should fail if item not in basket")
  void removeItemFromBasket_itemNotFound_fails() {
    when(basketHelper.getBasketById(BASKET_ID)).thenReturn(Mono.just(testBasket));

    StepVerifier.create(basketService.removeItemFromBasket(BASKET_ID, SKU_A))
        .expectError(ResponseStatusException.class)
        .verify();
  }

  @Test
  @DisplayName("cancelBasket should delete basket from redis")
  void cancelBasket_success() {
    when(basketRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    when(reactiveValueOperations.delete(BASKET_ID)).thenReturn(Mono.just(true));

    StepVerifier.create(basketService.cancelBasket(BASKET_ID))
        .verifyComplete();

    verify(reactiveValueOperations, times(1)).delete(BASKET_ID);
  }
}

