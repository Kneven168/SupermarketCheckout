package com.haiilo.supermarket.checkout;

import static com.haiilo.supermarket.checkout.util.DbUtils.cleanUp;
import static com.haiilo.supermarket.checkout.util.WireMockServerUtils.createWiremockServerForPort;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.haiilo.supermarket.checkout.util.DbUtils;
import com.haiilo.supermarket.checkout.util.UpdateTestPropertyUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {BaseTest.Initializer.class})
@Testcontainers
public abstract class BaseTest {
  protected static final int TEST_SERVER_PORT = 9023;
  public static final String LOCALHOST = "http://localhost:";
  @LocalServerPort
  int port;

  protected DbUtils dbUtils;
  protected static WireMockServer mockServer;
  protected static WebTestClient webTestClient;
  protected static PostgreSQLContainer<?> postgreSQLContainer;


  @Container
  @ServiceConnection
  static GenericContainer<?> redisContainer = new GenericContainer<>(
      DockerImageName.parse("redis:7-alpine"))
      .withExposedPorts(6379);


  @BeforeAll
  public static void setUp() {
    mockServer = createWiremockServerForPort(TEST_SERVER_PORT);
    mockServer.start();
    postgreSQLContainer = DbUtils.getDbContainer("schema.sql");
    DbUtils.getR2BCDbContainer(postgreSQLContainer).start();
  }

  @BeforeEach
  public void init() {
    dbUtils = new DbUtils();
    mockServer.resetAll();
    webTestClient = WebTestClient.bindToServer().baseUrl(LOCALHOST + port + "").build();
  }

  @AfterEach
  public void close() {
    cleanUp(postgreSQLContainer);
    mockServer.resetAll();
    postgreSQLContainer.start();
  }

  @AfterAll
  public static void tearDown() {
    mockServer.shutdown();
    DbUtils.getR2BCDbContainer(postgreSQLContainer).stop();
  }



  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertyValues.of(
              UpdateTestPropertyUtils.instance().updateDbUrl(postgreSQLContainer).build())
          .applyTo(applicationContext.getEnvironment());
    }
  }
}
