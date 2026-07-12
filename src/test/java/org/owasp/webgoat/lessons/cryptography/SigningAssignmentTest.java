/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.mock.web.MockHttpServletRequest;

class SigningAssignmentTest {

  private final SigningAssignment assignment = new SigningAssignment();

  @Test
  void missingKeyPairFailsSafely() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    AttackResult result = assignment.completed(request, "abc", "def");

    assertThat(result.assignmentSolved()).isFalse();
    assertThat(result.getFeedback()).isEqualTo("crypto-signing.notok");
  }

  @Test
  void initializedKeyPairReturnsNormalAssignmentFeedback() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    KeyPair keyPair = CryptoUtil.generateKeyPair();
    request.getSession().setAttribute("keyPair", keyPair);

    AttackResult result = assignment.completed(request, "abc", "def");

    assertThat(result.assignmentSolved()).isFalse();
    assertThat(result.getFeedback()).isEqualTo("crypto-signing.modulusnotok");
  }
}
