package com.haiilo.supermarket.checkout.util;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.domain.Order;
import com.haiilo.supermarket.checkout.dto.OrderDTO;
import com.haiilo.supermarket.checkout.domain.OrderItem;
import com.haiilo.supermarket.checkout.repository.OrderItemRepository;
import com.haiilo.supermarket.checkout.repository.OrderRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderHelper {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final BasketHelper basketHelper;

  public Mono<Order> saveOrderHeader(Basket basket) {
    Order orderHeader = new Order(null, basket.getTotalPrice(), Instant.now());
    return orderRepository.save(orderHeader)
        .doOnSuccess(order ->
            log.info("Order with Id '{}' have been successfully saved.", order.id()));
  }

  public Mono<OrderDTO> saveOrderItems(Order savedOrder, String basketId) {
    return basketHelper.getBasketById(basketId)
        .flatMap(basket -> Flux.fromIterable(basket.getItems().entrySet())
            .map(entry -> OrderItem.builder().orderId(null).orderId(savedOrder.id())
                .productSku(entry.getKey()).quantity(entry.getValue()).build())
            .collectList()
        )
        .flatMap(items -> orderItemRepository.saveAll(items).collectList())
        .doOnSuccess(order ->
            log.info("OrderItems have been successfully saved for Order Id '{}'", savedOrder.id()))
        .flatMap(items -> buildFullOrder(savedOrder, items));
  }

  private Mono<OrderDTO> buildFullOrder(Order order, List<OrderItem> orderItems) {
    return Mono.just(OrderDTO.builder().id(order.id()).items(orderItems)
        .finalPrice(order.finalPrice()).createdAt(order.createdAt())
        .build());
  }
}
