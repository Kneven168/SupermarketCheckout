package com.haiilo.supermarket.checkout;

import com.haiilo.supermarket.checkout.util.DbUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext
public abstract class BaseTest {

  public static final String LOCALHOST = "http://localhost:";
  @LocalServerPort
  int port;

  @Autowired
  protected DatabaseClient databaseClient;

  protected static WebTestClient webTestClient;

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13")
      .withDatabaseName("checkout_db")
      .withUsername("postgres")
      .withPassword("test123")
      .withReuse(true);

  @Container
  @ServiceConnection
  static GenericContainer<?> redisContainer = new GenericContainer<>(
      DockerImageName.parse("redis:7-alpine"))
      .withExposedPorts(6379)
      .withReuse(true);

  @BeforeEach
  public void init() {
    webTestClient = WebTestClient.bindToServer().baseUrl(LOCALHOST + port + "").build();
  }

  @AfterEach
  public void cleanUp() {
    DbUtils.cleanUp(databaseClient);
  }
}
