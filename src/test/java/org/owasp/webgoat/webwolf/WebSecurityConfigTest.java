/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.owasp.webgoat.webwolf.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = {WebSecurityConfigTest.TestApplication.class, WebSecurityConfig.class})
@AutoConfigureMockMvc
class WebSecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void rejectsLoginWithoutCsrfToken() throws Exception {
    mockMvc
        .perform(post("/login").param("username", "attacker").param("password", "attacker"))
        .andExpect(status().isForbidden());
  }

  @Test
  void acceptsLoginSubmissionWithCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/login")
                .with(csrf())
                .param("username", "legitimate-user")
                .param("password", "incorrect-password"))
        .andExpect(status().is3xxRedirection());
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {

    @Bean
    UserService userService() {
      return mock(UserService.class);
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
      return mock(ClientRegistrationRepository.class);
    }
  }
}
