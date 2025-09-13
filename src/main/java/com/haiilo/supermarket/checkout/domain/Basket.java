package com.haiilo.supermarket.checkout.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Basket")
public class Basket implements Serializable {

  @Id
  private String id;
  private Map<String, Integer> items = new HashMap<>();
  private int totalPrice = 0;

  public Basket(String id) {
    this.id = id;
  }

  public void addItem(String sku) {
    this.items.compute(sku, (key, quantity) -> (quantity == null) ? 1 : quantity + 1);
  }

  public void removeItem(String sku) {
    this.items.computeIfPresent(sku, (key, quantity) -> {
      if (quantity > 1) {
        return quantity - 1;
      }
      return null;
    });
  }
}
