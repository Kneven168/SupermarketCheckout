package com.haiilo.supermarket.checkout.utill;

import java.time.Duration;

public class AppConstants {

  private AppConstants() {
  }

  public static final Duration BASKET_TTL = Duration.ofDays(1);
  public static final String BASKET_ID = "basketId";
}
