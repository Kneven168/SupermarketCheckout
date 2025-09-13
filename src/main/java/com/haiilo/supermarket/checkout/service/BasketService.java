package com.haiilo.supermarket.checkout.service;

import static com.haiilo.supermarket.checkout.utill.AppConstants.BASKET_TTL;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.utill.CheckoutHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketService {

  private final CheckoutHelper checkoutHelper;
  private final ReactiveRedisTemplate<String, Basket> basketRedisTemplate;

  public Mono<Basket> createBasket() {
    String basketId = UUID.randomUUID().toString();
    Basket newBasket = new Basket(basketId);
    log.info("Creating a new basket with ID '{}'", basketId);
    return basketRedisTemplate.opsForValue().set(basketId, newBasket, BASKET_TTL)
        .thenReturn(newBasket);
  }

  public Mono<Integer> addItemToBasket(String basketId, String sku) {
    return Mono.zip(
            checkoutHelper.getProductBySku(sku),
            checkoutHelper.getBasketById(basketId),
            (product, basket) -> {
              basket.addItem(product.sku());
              return basket;
            })
        .flatMap(checkoutHelper::getTotalPrice);
  }

  public Mono<Integer> removeItemFromBasket(String basketId, String sku) {
    log.info("Removing one item with SKU '{}' from basket '{}'", sku, basketId);
    return checkoutHelper.getBasketById(basketId)
        .doOnNext(basket -> {
          if (!basket.getItems().containsKey(sku)) {
            throw new ResponseStatusException(NOT_FOUND, "Item with SKU " + sku + " not found in basket");
          }
          basket.removeItem(sku);
        })
        .flatMap(checkoutHelper::getTotalPrice);
  }

  public Mono<Void> cancelBasket(String basketId) {
    return basketRedisTemplate.opsForValue().delete(basketId).then()
        .doOnSuccess(totalPrice ->
            log.info("Basket with Id '{}' have been successfully deleted.", basketId));
  }

}