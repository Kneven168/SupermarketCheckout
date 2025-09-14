package com.haiilo.supermarket.checkout.service;

import static com.haiilo.supermarket.checkout.util.AppConstants.BASKET_TTL;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.dto.OrderDTO;
import com.haiilo.supermarket.checkout.util.BasketHelper;
import com.haiilo.supermarket.checkout.util.OrderHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketService {

  private final BasketHelper basketHelper;
  private final OrderHelper orderHelper;
  private final ReactiveRedisTemplate<String, Basket> basketRedisTemplate;

  public Mono<Basket> createBasket() {
    String basketId = UUID.randomUUID().toString();
    Basket newBasket = new Basket(basketId);
    log.info("Creating a new basket with ID '{}'", basketId);
    return basketRedisTemplate.opsForValue().set(basketId, newBasket, BASKET_TTL)
        .thenReturn(newBasket);
  }

  public Mono<Basket> getBasketById(String basketId) {
    log.info("Getting a basket by ID '{}'", basketId);
    return basketHelper.getBasketById(basketId);
  }

  public Mono<Integer> addItemToBasket(String basketId, String sku) {
    return Mono.zip(
            basketHelper.getProductBySku(sku),
            basketHelper.getBasketById(basketId),
            (product, basket) -> {
              basket.addItem(product.sku());
              return basket;
            })
        .flatMap(basketHelper::getTotalPrice);
  }

  public Mono<Integer> removeItemFromBasket(String basketId, String sku) {
    log.info("Removing one item with SKU '{}' from basket '{}'", sku, basketId);
    return basketHelper.getBasketById(basketId)
        .doOnNext(basket -> {
          if (!basket.getItems().containsKey(sku)) {
            throw new ResponseStatusException(NOT_FOUND,
                "Item with SKU " + sku + " not found in basket");
          }
          basket.removeItem(sku);
        })
        .flatMap(basketHelper::getTotalPrice);
  }

  public Mono<Void> cancelBasket(String basketId) {
    return basketRedisTemplate.opsForValue().delete(basketId).then()
        .doOnSuccess(totalPrice ->
            log.info("Basket with Id '{}' have been successfully deleted.", basketId));
  }

  @Transactional
  public Mono<OrderDTO> checkout(String basketId) {
    log.info("Checking out basket with ID '{}'", basketId);
    return getBasketById(basketId)
        .filter(basket -> !basket.getItems().isEmpty())
        .switchIfEmpty(Mono.error(
            new ResponseStatusException(BAD_REQUEST, "Cannot checkout an empty basket.")))
        .flatMap(orderHelper::saveOrderHeader)
        .flatMap(savedOrder -> orderHelper.saveOrderItems(savedOrder, basketId))
        .flatMap(fullOrder -> cancelBasket(basketId).thenReturn(fullOrder))
        .doOnSuccess(totalPrice ->
            log.info("Basket with Id '{}' have been successfully checkout.", basketId));
  }
}