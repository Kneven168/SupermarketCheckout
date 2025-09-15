package com.haiilo.supermarket.checkout.util;

import com.haiilo.supermarket.checkout.domain.Product;
import org.springframework.r2dbc.core.DatabaseClient;

public class DbUtils {

  public static final String PRODUCT_TABLE_NAME = "products";
  public static final String DELETE_ALL = "DELETE from products; DELETE from order_items; DELETE from orders;";
  public static final String ID = "id";

  public static void cleanUp(DatabaseClient dbClient) {
    dbClient.sql(DELETE_ALL).fetch().rowsUpdated().block();
  }

  public static void loadRecord(Product product, DatabaseClient dbClient) {
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
