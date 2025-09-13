package com.haiilo.supermarket.checkout;

import static com.haiilo.supermarket.checkout.util.TestConstants.BASE_PRODUCT_URI;
import static com.haiilo.supermarket.checkout.util.TestConstants.SKU_A;
import static com.haiilo.supermarket.checkout.util.TestConstants.SKU_B;
import static com.haiilo.supermarket.checkout.util.TestConstants.SKU_C;

import com.haiilo.supermarket.checkout.domain.Product;
import com.haiilo.supermarket.checkout.util.DbUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

class ProductControllerIntegrationTest extends BaseTest {

  @Test
  @DisplayName("POST /api/v1/products should create a new product")
  void createProduct_Endpoint() {
    Product newProduct = new Product(null, SKU_A, "Apple", 50, 3, 130);

    webTestClient.post()
        .uri(BASE_PRODUCT_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(newProduct), Product.class)
        .exchange()
        .expectStatus().isCreated() // Ожидаем статус 201 Created
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo(SKU_A)
        .jsonPath("$.name").isEqualTo("Apple")
        .jsonPath("$.unitPrice").isEqualTo(50)
        .jsonPath("$.offerQuantity").isEqualTo(3)
        .jsonPath("$.offerPrice").isEqualTo(130);
  }

  @Test
  @DisplayName("GET /api/v1/products/{sku} should return a product")
  void getProduct_Endpoint() {
    Product existingProduct = new Product(null, SKU_B, "Banana", 30, 5, 100);
    DbUtils.loadRecord(existingProduct, postgreSQLContainer);

    webTestClient.get()
        .uri(BASE_PRODUCT_URI+ "/" + SKU_B)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo(SKU_B)
        .jsonPath("$.name").isEqualTo("Banana")
        .jsonPath("$.unitPrice").isEqualTo(30)
        .jsonPath("$.offerQuantity").isEqualTo(5)
        .jsonPath("$.offerPrice").isEqualTo(100);
  }

  @Test
  @DisplayName("PUT /api/v1/products/{sku} should update a product")
  void updateProduct_Endpoint() {
    Product originalProduct = new Product(null, SKU_C, "Cherry", 60, 5, 400);
    DbUtils.loadRecord(originalProduct, postgreSQLContainer);

    Product updatedInfo = new Product(null, SKU_C, "Sweet Cherry", 70, 4, 300);

    webTestClient.put()
        .uri(BASE_PRODUCT_URI+ "/" + SKU_C)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(updatedInfo), Product.class)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.sku").isEqualTo(SKU_C)
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

    webTestClient.delete()
        .uri(BASE_PRODUCT_URI+ "/D")
        .exchange()
        .expectStatus().isNoContent();
  }
}

