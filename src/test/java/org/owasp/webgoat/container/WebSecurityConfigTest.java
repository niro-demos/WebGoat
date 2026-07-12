/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = WebGoat.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSecurityConfigTest {

  private static final String ATTACKER_HOST = "evil.example";

  @LocalServerPort private int port;

  static Stream<String> unauthenticatedRedirects() {
    return Stream.of("/WebGoat/", "/WebGoat/welcome.mvc", "/WebGoat/logout");
  }

  @ParameterizedTest
  @MethodSource("unauthenticatedRedirects")
  void authenticationRedirectsDoNotTrustTheHostHeader(String path) throws Exception {
    var legitimate = request(path, "127.0.0.1:" + port);
    assertThat(legitimate.status()).isBetween(300, 399);
    assertThat(URI.create(legitimate.location()).getHost()).isNotEqualToIgnoringCase(ATTACKER_HOST);

    var forged = request(path, ATTACKER_HOST);
    assertThat(forged.status()).isBetween(300, 399);
    assertThat(URI.create(forged.location()).getHost()).isNotEqualToIgnoringCase(ATTACKER_HOST);
  }

  private Response request(String path, String host) throws Exception {
    try (var socket = new Socket("127.0.0.1", port);
        var writer =
            new PrintWriter(socket.getOutputStream(), false, StandardCharsets.US_ASCII);
        var reader =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII))) {
      writer.printf("GET %s HTTP/1.1\r\nHost: %s\r\nConnection: close\r\n\r\n", path, host);
      writer.flush();

      var status = Integer.parseInt(reader.readLine().split(" ")[1]);
      String location = null;
      for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
        if (line.regionMatches(true, 0, "Location: ", 0, 10)) {
          location = line.substring(10);
        }
      }
      assertThat(location).as("Location response header").isNotNull();
      return new Response(status, location);
    }
  }

  private record Response(int status, String location) {}
}
