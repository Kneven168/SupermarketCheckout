package com.haiilo.supermarket.checkout.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public class UpdateTestPropertyUtils {

  private Map<String, String> updatedProperties = new HashMap<>();

  private UpdateTestPropertyUtils() {}

  public static UpdateTestPropertyUtils instance() {
    return new UpdateTestPropertyUtils();
  }

  public List<String> build() {
    return new ArrayList<>(this.updatedProperties.values());
  }

  public UpdateTestPropertyUtils setPropertyForTest(String propertyName, String propertyValue) {
    updatedProperties.put(propertyName, StringUtils.joinWith("=", propertyName, propertyValue));
    return this;
  }

  public UpdateTestPropertyUtils updateDbUrl(PostgreSQLContainer container) {
    return setPropertyForTest(
            "spring.r2dbc.url",
            "r2dbc:postgresql://"
                + container.getHost()
                + ":"
                + container.getFirstMappedPort()
                + "/"
                + container.getDatabaseName())
        .setPropertyForTest("spring.r2dbc.username", container.getUsername())
        .setPropertyForTest("spring.r2dbc.password", container.getPassword());
  }
}
