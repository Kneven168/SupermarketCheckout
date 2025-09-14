package com.haiilo.supermarket.checkout.util;


import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import com.haiilo.supermarket.checkout.domain.Product;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.junit.ClassRule;
import org.springframework.r2dbc.core.DatabaseClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

public class DbUtils {

  public static final String DATABASE_NAME = "checkout_db";
  public static final String IMAGE_NAME = "postgres:13";
  public static final String USERNAME = "postgres";
  public static final String PASSWORD = "test123";
  public static final String PRODUCT_TABLE_NAME = "products";

  public static final String DELETE_ALL = "DELETE from products; DELETE from order_items; DELETE from orders;";

  public static final String ID = "id";
  static PostgreSQLR2DBCDatabaseContainer postgreSQLRDBCContainer;
  @ClassRule
  public static PostgreSQLContainer<?> postgreSQLContainer;

  public static PostgreSQLR2DBCDatabaseContainer getR2BCDbContainer(
      PostgreSQLContainer postgreSQLContainer) {
    postgreSQLRDBCContainer = new PostgreSQLR2DBCDatabaseContainer(postgreSQLContainer);
    return postgreSQLRDBCContainer;
  }

  public static PostgreSQLContainer getDbContainer(String scriptFile) {
    postgreSQLContainer =
        new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_NAME))
            .withDatabaseName(DATABASE_NAME)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withReuse(true);

    return postgreSQLContainer;
  }

  private static ConnectionFactory connectionFactory(PostgreSQLContainer postgreSQLContainer) {
    final ConnectionFactoryOptions.Builder builder =
        ConnectionFactoryOptions.parse(
                "r2dbc:postgresql://"
                    + postgreSQLContainer.getHost()
                    + ":"
                    + postgreSQLContainer.getFirstMappedPort()
                    + "/"
                    + postgreSQLContainer.getDatabaseName())
            .mutate();
    builder.option(USER, postgreSQLContainer.getUsername());
    builder.option(ConnectionFactoryOptions.PASSWORD, postgreSQLContainer.getPassword());

    return ConnectionFactories.get(builder.build());
  }

  public static void cleanUp(PostgreSQLContainer postgreSQLContainer) {
    DatabaseClient dbClient = DatabaseClient.create(connectionFactory(postgreSQLContainer));
    dbClient.sql(DELETE_ALL).fetch().rowsUpdated().block();
  }

  public static void loadRecord(Product product, PostgreSQLContainer postgreSQLContainer) {
    DatabaseClient dbClient = DatabaseClient.create(connectionFactory(postgreSQLContainer));
    dbClient
        .sql(
            "INSERT INTO "
                + PRODUCT_TABLE_NAME
                + " (sku,\n"
                + "    name,\n"
                + "    unit_price,\n"
                + "    offer_quantity,\n"
                + "    offer_price) VALUES (:sku, :name, :unit_price, :offer_quantity, :offer_price)")
        .filter((statement, executeFunction) -> statement.returnGeneratedValues(ID).execute())
        .bind("sku", product.sku())
        .bind("name", product.name())
        .bind("unit_price", product.unitPrice())
        .bind("offer_quantity", product.offerQuantity())
        .bind("offer_price", product.offerPrice())
        .fetch()
        .first()
        .map(r -> r.get(ID))
        .block();
  }


}
