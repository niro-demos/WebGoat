/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.UrlUtils;

final class RelativeRedirectStrategy implements RedirectStrategy {

  static final RelativeRedirectStrategy INSTANCE = new RelativeRedirectStrategy();

  private RelativeRedirectStrategy() {}

  @Override
  public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
      throws IOException {
    if (UrlUtils.isAbsoluteUrl(url)) {
      throw new IllegalArgumentException("Redirect target must be relative");
    }
    var target = url.startsWith(request.getContextPath()) ? url : request.getContextPath() + url;
    response.setStatus(HttpServletResponse.SC_FOUND);
    response.setHeader("Location", response.encodeRedirectURL(target));
    response.getWriter().flush();
  }
}
