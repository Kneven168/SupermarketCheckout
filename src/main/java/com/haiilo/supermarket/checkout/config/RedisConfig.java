package com.haiilo.supermarket.checkout.config;


import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.domain.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public ReactiveRedisTemplate<String, Basket> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<Basket> valueSerializer = new Jackson2JsonRedisSerializer<>(Basket.class);
    RedisSerializationContext.RedisSerializationContextBuilder<String, Basket> builder =
        RedisSerializationContext.newSerializationContext(keySerializer);
    RedisSerializationContext<String, Basket> context = builder.value(valueSerializer).build();
    return new ReactiveRedisTemplate<>(factory, context);
  }

  @Bean
  public ReactiveRedisTemplate<String, Product> productRedisTemplate(ReactiveRedisConnectionFactory factory) {
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<Product> valueSerializer = new Jackson2JsonRedisSerializer<>(Product.class);
    RedisSerializationContext.RedisSerializationContextBuilder<String, Product> builder =
        RedisSerializationContext.newSerializationContext(keySerializer);
    RedisSerializationContext<String, Product> context = builder.value(valueSerializer).build();
    return new ReactiveRedisTemplate<>(factory, context);
  }
}
