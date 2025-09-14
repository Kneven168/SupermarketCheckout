package com.haiilo.supermarket.checkout.controller;

import static com.haiilo.supermarket.checkout.util.AppConstants.BASKET_ID;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.dto.OrderDTO;
import com.haiilo.supermarket.checkout.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@RestController
@RequestMapping("/api/v1/baskets")
@RequiredArgsConstructor
@Tag(name = "Basket Management", description = "API for managing shopping process")
public class BasketController {

  private final BasketService basketService;

  @Operation(summary = "Create a new basket", description = "Creates a new empty shopping basket")
  @ApiResponse(responseCode = "201", description = "Basket created successfully")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Basket> createBasket() {
    return basketService.createBasket();
  }

  @Operation(summary = "Get basket by ID", description = "Retrieves a basket by its unique identifier")
  @ApiResponse(responseCode = "200", description = "Basket found", content = @Content(schema = @Schema(implementation = Basket.class)))
  @ApiResponse(responseCode = "404", description = "Basket not found")
  @GetMapping("/{basketId}")
  public Mono<Basket> getBasketById(
      @Parameter(description = "Unique identifier of the basket", required = true) @PathVariable String basketId) {
    return basketService.getBasketById(basketId);
  }

  @Operation(summary = "Add item to basket", description = "Adds one unit of a product to the basket")
  @ApiResponse(responseCode = "200", description = "Item added successfully", content = @Content(schema = @Schema(implementation = Integer.class)))
  @ApiResponse(responseCode = "404", description = "Basket or product not found")
  @PostMapping("/{basketId}/items/{sku}")
  public Mono<Integer> addItemToBasket(
      @Parameter(description = "Unique identifier of the basket", required = true) @PathVariable String basketId,
      @Parameter(description = "SKU (Stock Keeping Unit) of the product", required = true) @PathVariable String sku) {
    return basketService.addItemToBasket(basketId, sku)
        .contextWrite(Context.of(BASKET_ID, basketId));
  }

  @Operation(summary = "Remove item from basket", description = "Removes one unit of a product from the basket")
  @ApiResponse(responseCode = "200", description = "Item removed successfully", content = @Content(schema = @Schema(implementation = Integer.class)))
  @ApiResponse(responseCode = "404", description = "Basket or product not found")
  @DeleteMapping("/{basketId}/items/{sku}")
  public Mono<Integer> removeItemFromBasket(
      @Parameter(description = "Unique identifier of the basket", required = true) @PathVariable String basketId,
      @Parameter(description = "SKU (Stock Keeping Unit) of the product", required = true) @PathVariable String sku) {
    return basketService.removeItemFromBasket(basketId, sku)
        .contextWrite(Context.of(BASKET_ID, basketId));
  }

  @Operation(summary = "Cancel basket", description = "Cancels and deletes a basket with all its items")
  @ApiResponse(responseCode = "204", description = "Basket cancelled successfully")
  @ApiResponse(responseCode = "404", description = "Basket not found")
  @DeleteMapping("/{basketId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> cancelBasket(
      @Parameter(description = "Unique identifier of the basket", required = true) @PathVariable String basketId) {
    return basketService.cancelBasket(basketId);
  }

  @Operation(summary = "Checkout basket", description = "Processes the checkout for a basket and creates an order")
  @ApiResponse(responseCode = "200", description = "Checkout completed successfully", content = @Content(schema = @Schema(implementation = OrderDTO.class)))
  @ApiResponse(responseCode = "404", description = "Basket not found")
  @ApiResponse(responseCode = "400", description = "Basket is empty or invalid")
  @PostMapping("/{basketId}/checkout")
  public Mono<OrderDTO> checkout(
      @Parameter(description = "Unique identifier of the basket", required = true) @PathVariable String basketId) {
    return basketService.checkout(basketId);
  }

}
