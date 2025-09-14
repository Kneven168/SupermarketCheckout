package com.haiilo.supermarket.checkout.controller;

import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "API for managing products in the supermarket")
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
  @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(schema = @Schema(implementation = Product.class)))
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Product> createProduct(
      @RequestBody(description = "Product details to create", required = true,
          content = @Content(schema = @Schema(implementation = Product.class)))
      @org.springframework.web.bind.annotation.RequestBody Product product) {
    return productService.createProduct(product);
  }

  @Operation(summary = "Get product by SKU", description = "Retrieves a product by its SKU (Stock Keeping Unit)")
  @ApiResponse(responseCode = "200", description = "Product found", content = @Content(schema = @Schema(implementation = Product.class)))
  @ApiResponse(responseCode = "404", description = "Product not found")
  @GetMapping("/{sku}")
  public Mono<Product> getProductBySku(
      @Parameter(description = "SKU (Stock Keeping Unit) of the product", required = true)
      @PathVariable String sku) {
    return productService.getProductBySku(sku);
  }

  @Operation(summary = "Update product", description = "Updates an existing product with new details")
  @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(schema = @Schema(implementation = Product.class)))
  @ApiResponse(responseCode = "404", description = "Product not found")
  @ApiResponse(responseCode = "400", description = "SKU in path does not match SKU in body")
  @PutMapping("/{sku}")
  public Mono<Product> updateProduct(
      @Parameter(description = "SKU (Stock Keeping Unit) of the product to update", required = true)
      @PathVariable String sku,
      @RequestBody(description = "Updated product details", required = true,
          content = @Content(schema = @Schema(implementation = Product.class)))
      @org.springframework.web.bind.annotation.RequestBody Product product) {
    return productService.updateProduct(sku, product);
  }

  @Operation(summary = "Delete product", description = "Deletes a product by its SKU")
  @ApiResponse(responseCode = "204", description = "Product deleted successfully")
  @DeleteMapping("/{sku}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteProduct(
      @Parameter(description = "SKU (Stock Keeping Unit) of the product to delete", required = true)
      @PathVariable String sku) {
    return productService.deleteProduct(sku);
  }
}