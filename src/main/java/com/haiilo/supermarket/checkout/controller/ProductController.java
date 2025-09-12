package com.haiilo.supermarket.checkout.controller;


import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Product> createProduct(@RequestBody Product product) {
    return productService.saveProduct(product);
  }

  @GetMapping("/{sku}")
  public Mono<Product> getProductBySku(@PathVariable String sku) {
    return productService.getProductBySku(sku);
  }

  @PutMapping("/{sku}")
  public Mono<Product> updateProduct(@PathVariable String sku, @RequestBody Product product) {
    return productService.updateProduct(product, sku);
  }

  @DeleteMapping("/{sku}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteProduct(@PathVariable String sku) {
    return productService.deleteProduct(sku);
  }
}