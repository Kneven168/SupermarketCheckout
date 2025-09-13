package com.haiilo.supermarket.checkout;

import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.util.DbUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

public class ProductControllerBaseTest extends BaseTest {

  @Test
  @DisplayName("POST /api/v1/products should create a new product")
  void createProduct_Endpoint() {
    // Given
    Product newProduct = new Product(null, "A", "Apple", 50, 3, 130);
    String baseUri = "/api/v1/products";

    // When & Then
    webTestClient.post()
        .uri(baseUri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(newProduct), Product.class)
        .exchange()
        .expectStatus().isCreated() // Ожидаем статус 201 Created
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo("A")
        .jsonPath("$.name").isEqualTo("Apple")
        .jsonPath("$.unitPrice").isEqualTo(50)
        .jsonPath("$.offerQuantity").isEqualTo(3)
        .jsonPath("$.offerPrice").isEqualTo(130);
  }

  @Test
  @DisplayName("GET /api/v1/products/{sku} should return a product")
  void getProduct_Endpoint() {
    Product existingProduct = new Product(null,"B", "Banana", 30, 5, 100);
    DbUtils.loadRecord(existingProduct, postgreSQLContainer);
    String uri = "/api/v1/products/B";

    // When & Then
    webTestClient.get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo("B")
        .jsonPath("$.name").isEqualTo("Banana")
        .jsonPath("$.unitPrice").isEqualTo(30)
        .jsonPath("$.offerQuantity").isEqualTo(5)
        .jsonPath("$.offerPrice").isEqualTo(100);
  }

  @Test
  @DisplayName("PUT /api/v1/products/{sku} should update a product")
  void updateProduct_Endpoint() {
    // Given: сохраняем оригинальный продукт
    Product originalProduct = new Product(null, "C", "Cherry", 60, 5, 400);
    DbUtils.loadRecord(originalProduct, postgreSQLContainer);

    Product updatedInfo = new Product(null, "C", "Sweet Cherry", 70, 4, 300);
    String uri = "/api/v1/products/C";

    // When & Then
    webTestClient.put()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(updatedInfo), Product.class)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.sku").isEqualTo("C")
        .jsonPath("$.name").isEqualTo("Sweet Cherry")
        .jsonPath("$.unitPrice").isEqualTo(70)
        .jsonPath("$.offerQuantity").isEqualTo(4)
        .jsonPath("$.offerPrice").isEqualTo(300);
  }

  @Test
  @DisplayName("DELETE /api/v1/products/{sku} should delete a product")
  void deleteProduct_Endpoint() {
    Product productToDelete = new Product(null, "D", "Date", 100, 4, 300);
    DbUtils.loadRecord(productToDelete, postgreSQLContainer);
    String uri = "/api/v1/products/D";

    webTestClient.delete()
        .uri(uri)
        .exchange()
        .expectStatus().isNoContent();
  }
}

