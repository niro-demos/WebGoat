/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.requests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.security.core.Authentication;

class RequestsTest {

  private static final String LEARNER = "learner-a";

  private WebWolfTraceRepository repository;
  private Requests requests;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    repository = new WebWolfTraceRepository();
    requests = new Requests(repository, new ObjectMapper().findAndRegisterModules());
    authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn(LEARNER);
  }

  @Test
  void onlyShowsLandingRequestsScopedToAuthenticatedLearner() {
    addTrace("/WebWolf/landing/callback?uniqueCode=a-renrael");
    addTrace("/WebWolf/landing/callback?uniqueCode=b-renrael");
    addTrace("/WebWolf/landing/unscoped?probe=private-value");

    assertThat(visibleTraces()).hasSize(1);
  }

  @Test
  void onlyShowsPasswordResetCallbacksScopedToAuthenticatedLearner() {
    addTrace(
        "/WebWolf/PasswordReset/reset/reset-password/11111111-1111-1111-1111-111111111111"
            + "?uniqueCode=a-renrael");
    addTrace(
        "/WebWolf/PasswordReset/reset/reset-password/22222222-2222-2222-2222-222222222222"
            + "?uniqueCode=b-renrael");
    addTrace(
        "/WebWolf/PasswordReset/reset/reset-password/33333333-3333-3333-3333-333333333333");

    assertThat(visibleTraces()).hasSize(1);
  }

  private Collection<?> visibleTraces() {
    return (Collection<?>) requests.get(authentication).getModel().get("traces");
  }

  private void addTrace(String path) {
    HttpExchange exchange = mock();
    HttpExchange.Request request = mock();
    when(exchange.getRequest()).thenReturn(request);
    when(exchange.getTimestamp()).thenReturn(Instant.EPOCH);
    when(request.getUri()).thenReturn(URI.create("http://localhost:9090" + path));
    repository.add(exchange);
  }
}
