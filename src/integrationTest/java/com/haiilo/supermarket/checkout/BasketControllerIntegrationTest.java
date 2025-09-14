package com.haiilo.supermarket.checkout;

import static com.haiilo.supermarket.checkout.util.TestConstants.BASE_PRODUCT_URI;
import static com.haiilo.supermarket.checkout.util.TestConstants.SKU_A;
import static com.haiilo.supermarket.checkout.util.TestConstants.SKU_B;
import static com.haiilo.supermarket.checkout.util.TestConstants.BASE_BASKET_URI;

import static org.assertj.core.api.Assertions.assertThat;

import com.haiilo.supermarket.checkout.domain.Basket;
import com.haiilo.supermarket.checkout.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

class BasketControllerIntegrationTest extends BaseTest {

  public static final Product PRODUCT_A = new Product(null, SKU_A, "Apple", 50, 3, 130);
  public static final Product PRODUCT_B = new Product(null, SKU_B, "Banana", 30, 2, 50);

  @Test
  @DisplayName("POST /api/v1/baskets should create a new empty basket")
  void createBasket_Endpoint() {
    webTestClient.post().uri(BASE_BASKET_URI)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.items").isEmpty()
        .jsonPath("$.totalPrice").isEqualTo(0);
  }

  @Test
  @DisplayName("POST /api/v1/baskets/{id}/items should add items and return correct total price with offers")
  void addItem_ToBasket_Endpoint() {
    // 1. Create a basket
    Basket basket = webTestClient.post().uri(BASE_BASKET_URI)
        .exchange()
        .expectBody(Basket.class).returnResult().getResponseBody();
    assertThat(basket).isNotNull();

    String baseBasketsItemsUri = BASE_BASKET_URI + "/" + basket.getId() + "/items/";

    // 2. Create a products
    webTestClient.post()
        .uri(BASE_PRODUCT_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(PRODUCT_A), Product.class)
        .exchange()
        .expectStatus().isCreated() // Ожидаем статус 201 Created
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo(SKU_A)
        .jsonPath("$.name").isEqualTo("Apple")
        .jsonPath("$.unitPrice").isEqualTo(50)
        .jsonPath("$.offerQuantity").isEqualTo(3)
        .jsonPath("$.offerPrice").isEqualTo(130);

    webTestClient.post()
        .uri(BASE_PRODUCT_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(PRODUCT_B), Product.class)
        .exchange()
        .expectStatus().isCreated() // Ожидаем статус 201 Created
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo(SKU_B)
        .jsonPath("$.name").isEqualTo("Banana")
        .jsonPath("$.unitPrice").isEqualTo(30)
        .jsonPath("$.offerQuantity").isEqualTo(2)
        .jsonPath("$.offerPrice").isEqualTo(50);

    // 3. Add items
    webTestClient.post().uri(baseBasketsItemsUri + SKU_A)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Integer.class).isEqualTo(50);

    webTestClient.post().uri(baseBasketsItemsUri + SKU_B)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Integer.class).isEqualTo(80);

    webTestClient.post().uri(baseBasketsItemsUri + SKU_B)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Integer.class).isEqualTo(100);
  }

  @Test
  @DisplayName("PUT /api/v1/baskets/{id}/items/{sku} should remove one item and return updated total")
  void removeItem_FromBasket_Endpoint() {
    // 1. Create a basket
    Basket basket = webTestClient.post().uri(BASE_BASKET_URI)
        .exchange()
        .expectBody(Basket.class).returnResult().getResponseBody();
    assertThat(basket).isNotNull();

    String baseBasketsItemsUri = BASE_BASKET_URI + "/" + basket.getId() + "/items/";

    // 2. Create a products
    webTestClient.post()
        .uri(BASE_PRODUCT_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(PRODUCT_A), Product.class)
        .exchange()
        .expectStatus().isCreated() // Ожидаем статус 201 Created
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo(SKU_A)
        .jsonPath("$.name").isEqualTo("Apple")
        .jsonPath("$.unitPrice").isEqualTo(50)
        .jsonPath("$.offerQuantity").isEqualTo(3)
        .jsonPath("$.offerPrice").isEqualTo(130);

    // 3. Add items
    webTestClient.post().uri(baseBasketsItemsUri + SKU_A).exchange().expectStatus().isOk();
    webTestClient.post().uri(baseBasketsItemsUri + SKU_A).exchange().expectStatus().isOk();

    // 4. Remove one 'A'. Total should go back to 50.
    webTestClient.put().uri(baseBasketsItemsUri + SKU_A)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Integer.class).isEqualTo(50);

    // 5. Remove the last 'A'. Total should be 0.
    webTestClient.put().uri(baseBasketsItemsUri + SKU_A)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Integer.class).isEqualTo(0);
  }

  @Test
  @DisplayName("DELETE /api/v1/baskets/{id} should delete the basket")
  void cancelBasket_Endpoint() {
    // 1. Create a basket
    Basket basket = webTestClient.post().uri(BASE_BASKET_URI)
        .exchange()
        .expectBody(Basket.class).returnResult().getResponseBody();
    assertThat(basket).isNotNull();

    // 2. Cancel it
    webTestClient.delete().uri(BASE_BASKET_URI + "/" + basket.getId())
        .exchange()
        .expectStatus().isNoContent();

    // 3. Verify it's gone
    webTestClient.get().uri(BASE_BASKET_URI + "/" + basket.getId())
        .exchange()
        .expectStatus().isNotFound();
  }


  @Test
  @DisplayName("POST /api/v1/baskets/{id}/checkout should create an order and delete the basket")
  void checkout_Endpoint() {
    // 1. Create a products
    webTestClient.post()
        .uri(BASE_PRODUCT_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(PRODUCT_A), Product.class)
        .exchange()
        .expectStatus().isCreated() // Ожидаем статус 201 Created
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.sku").isEqualTo(SKU_A)
        .jsonPath("$.name").isEqualTo("Apple")
        .jsonPath("$.unitPrice").isEqualTo(50)
        .jsonPath("$.offerQuantity").isEqualTo(3)
        .jsonPath("$.offerPrice").isEqualTo(130);

    // 2. Create basket and add items
    Basket basket = webTestClient.post().uri(BASE_BASKET_URI)
        .exchange()
        .expectBody(Basket.class).returnResult().getResponseBody();
    assertThat(basket).isNotNull();

    String addItemUri = BASE_BASKET_URI + "/" + basket.getId() + "/items/" + SKU_A;
    webTestClient.post().uri(addItemUri).exchange().expectStatus().isOk(); // Total should be 50

    // 3. Checkout the basket
    String checkoutUri = BASE_BASKET_URI + "/" + basket.getId() + "/checkout";
    webTestClient.post().uri(checkoutUri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isNotEmpty()
        .jsonPath("$.finalPrice").isEqualTo(50)
        .jsonPath("$.items[0].productSku").isEqualTo(SKU_A)
        .jsonPath("$.items[0].quantity").isEqualTo(1);

    // 4. Verify basket is deleted by trying to get it (should fail)
    webTestClient.get().uri(BASE_BASKET_URI + "/" + basket.getId())
        .exchange()
        .expectStatus().isNotFound();
  }
}


