package com.haiilo.supermarket.checkout.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basket{

  private String id;
  private Map<String, Integer> items = new HashMap<>();
}
