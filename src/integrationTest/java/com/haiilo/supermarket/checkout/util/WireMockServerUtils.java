package com.haiilo.supermarket.checkout.util;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockServerUtils {

  private WireMockServerUtils() {}

  public static WireMockServer createWiremockServerForPort(int port) {
    return new WireMockServer(
        options().port(port).usingFilesUnderDirectory("src/integrationTest/resources/"));
  }
}
