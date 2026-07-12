/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container;

import lombok.AllArgsConstructor;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

/** Security configuration for WebGoat. */
@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

  private final UserService userDetailsService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/favicon.ico",
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/fonts/**",
                        "/plugins/**",
                        "/registration",
                        "/register.mvc",
                        "/actuator/**")
                    .permitAll()
                    // Lessons deliver mail by POSTing to the mailbox over HTTP (RestTemplate),
                    // which carries no session, so the receiving endpoint must be public. Reading
                    // the mailbox (GET /mail) stays authenticated so users only see their own mail.
                    .requestMatchers(HttpMethod.POST, "/mail")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            login ->
                login
                    .loginPage("/login")
                    .successHandler(authenticationSuccessHandler())
                    .failureHandler(authenticationFailureHandler())
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll())
        .oauth2Login(
            oidc -> {
              oidc.defaultSuccessUrl("/login-oauth.mvc");
              oidc.loginPage("/login");
            })
        .logout(
            logout ->
                logout
                    .logoutSuccessHandler(logoutSuccessHandler())
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true))
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.disable())
        .exceptionHandling(
            handling ->
                handling.authenticationEntryPoint(new AjaxAuthenticationEntryPoint("/login")))
        .build();
  }

  private SavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler() {
    var handler = new SavedRequestAwareAuthenticationSuccessHandler();
    handler.setDefaultTargetUrl("/welcome.mvc");
    handler.setAlwaysUseDefaultTargetUrl(true);
    handler.setRedirectStrategy(relativeRedirectStrategy());
    return handler;
  }

  private SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
    var handler = new SimpleUrlAuthenticationFailureHandler("/login?error");
    handler.setRedirectStrategy(relativeRedirectStrategy());
    return handler;
  }

  private SimpleUrlLogoutSuccessHandler logoutSuccessHandler() {
    var handler = new SimpleUrlLogoutSuccessHandler();
    handler.setDefaultTargetUrl("/login?logout");
    handler.setRedirectStrategy(relativeRedirectStrategy());
    return handler;
  }

  private RedirectStrategy relativeRedirectStrategy() {
    return RelativeRedirectStrategy.INSTANCE;
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService);
  }

  @Bean
  @Primary
  public UserDetailsService userDetailsServiceBean() {
    return userDetailsService;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public NoOpPasswordEncoder passwordEncoder() {
    return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
  }
}
