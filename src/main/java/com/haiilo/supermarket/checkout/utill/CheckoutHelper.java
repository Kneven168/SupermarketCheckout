package com.haiilo.supermarket.checkout.utill;


import static com.haiilo.supermarket.checkout.utill.AppConstants.BASKET_ID;
import static com.haiilo.supermarket.checkout.utill.AppConstants.BASKET_TTL;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class CheckoutHelper {

  private final ProductService productService;
  private final ReactiveRedisTemplate<String, Basket> basketRedisTemplate;

  public Mono<Basket> getBasketById(String basketId) {
    return basketRedisTemplate.opsForValue().get(basketId)
        .switchIfEmpty(Mono.error(
            new ResponseStatusException(NOT_FOUND, "Basket with ID " + basketId + " not found")));
  }

  public Mono<Product> getProductBySku(String sku) {
    return productService.getProductBySku(sku).switchIfEmpty(Mono.error(
        new ResponseStatusException(NOT_FOUND, "Product with SKU " + sku + " not found")));
  }

  public Mono<Integer> getTotalPrice(Basket basket) {
    return this.updateBasketAndRecalculate(basket).transformDeferredContextual(
        (originalMono, ctx) ->
            originalMono
                .map(Basket::getTotalPrice)
                .doOnSuccess(
                    totalPrice -> log.info("Basket with Id '{}' recalculated total price '{}'.",
                        ctx.get(BASKET_ID), totalPrice)));
  }


  private Mono<Basket> updateBasketAndRecalculate(Basket basket) {
    return calculateTotal(basket)
        .flatMap(totalPrice -> {
          basket.setTotalPrice(totalPrice);
          log.debug("Updating basket '{}' with new total price: {}", basket.getId(), totalPrice);
          return basketRedisTemplate.opsForValue()
              .set(basket.getId(), basket, BASKET_TTL)
              .thenReturn(basket);
        });
  }

  private Mono<Integer> calculateTotal(Basket basket) {
    if (basket.getItems().isEmpty()) {
      return Mono.just(0);
    }
    return Flux.fromIterable(basket.getItems().entrySet())
        .flatMap(entry -> {
          String sku = entry.getKey();
          int quantity = entry.getValue();
          return getProductBySku(sku)
              .map(product -> calculateItemTotal(product, quantity))
              .switchIfEmpty(Mono.just(0));
        })
        .reduce(0, Integer::sum);
  }

  private int calculateItemTotal(Product product, int quantity) {
    if (product.hasSpecialOffer() && quantity >= product.offerQuantity()) {
      int offerCount = quantity / product.offerQuantity();
      int remainder = quantity % product.offerQuantity();
      return (offerCount * product.offerPrice()) + (remainder * product.unitPrice());
    }
    return quantity * product.unitPrice();
  }
}
