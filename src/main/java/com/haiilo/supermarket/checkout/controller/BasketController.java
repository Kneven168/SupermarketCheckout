package com.haiilo.supermarket.checkout.controller;

import static com.haiilo.supermarket.checkout.util.AppConstants.BASKET_ID;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.service.BasketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@RestController
@RequestMapping("/api/v1/baskets")
@RequiredArgsConstructor
public class BasketController {

  private final BasketService basketService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Basket> createBasket() {
    return basketService.createBasket();
  }

  @PostMapping("/{basketId}/items/{sku}")
  public Mono<Integer> addItem(@PathVariable String basketId, @PathVariable String sku) {
    return basketService.addItemToBasket(basketId, sku)
        .contextWrite(Context.of(BASKET_ID, basketId));
  }

  @PutMapping("/{basketId}/items/{sku}")
  public Mono<Integer> removeItem(@PathVariable String basketId, @PathVariable String sku) {
    return basketService.removeItemFromBasket(basketId, sku)
        .contextWrite(Context.of(BASKET_ID, basketId));
  }

  @DeleteMapping("/{basketId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> cancelBasket(@PathVariable String basketId) {
    return basketService.cancelBasket(basketId);
  }

}
