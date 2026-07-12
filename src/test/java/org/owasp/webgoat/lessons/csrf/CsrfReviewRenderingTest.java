/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CsrfReviewRenderingTest {

  @Test
  void learnerReviewTextIsRenderedAsTextInsteadOfHtml() throws IOException {
    var script =
        new String(
            getClass().getResourceAsStream("/lessons/csrf/js/csrf-review.js").readAllBytes(),
            StandardCharsets.UTF_8);

    assertThat(script)
        .as("the review renderer keeps its normal list and text content")
        .contains("$(\"#list\").append(comment)", ".text(result[i].text)");
    assertThat(script)
        .as("learner-controlled review text is never interpolated into an HTML string")
        .doesNotContain("replace('COMMENT', result[i].text)");
  }
}
