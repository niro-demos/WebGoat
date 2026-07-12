/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.requests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

class RequestsTemplateTest {

  @Test
  void capturedRequestFieldsAreRenderedAsInertText() throws IOException {
    var template =
        Jsoup.parse(
            getClass()
                .getResourceAsStream("/webwolf/templates/requests.html"),
            "UTF-8",
            "http://localhost");

    var escapedExpressions =
        template.select("*").stream()
            .filter(element -> element.hasAttr("th:text"))
            .map(element -> element.attr("th:text"))
            .toList();

    assertThat(escapedExpressions)
        .contains("${trace.date}", "${trace.path}", "${trace.json}");
    assertThat(template.select("*")).noneMatch(element -> element.hasAttr("th:utext"));
  }
}
