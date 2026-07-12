/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.owasp.webgoat.lessons.cia.CIAQuiz;
import org.owasp.webgoat.lessons.httpbasics.HttpBasicsQuiz;
import org.owasp.webgoat.lessons.jwt.JWTQuiz;
import org.owasp.webgoat.lessons.openredirect.OpenRedirectQuiz;
import org.owasp.webgoat.lessons.xss.CrossSiteScriptingQuiz;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class QuizSessionIsolationTest {

  @ParameterizedTest(name = "{0} quiz progress is isolated per learner session")
  @MethodSource("quizzes")
  void quizProgressIsIsolatedPerLearnerSession(
      String name,
      Object controller,
      String path,
      MockHttpServletRequestBuilder learnerBSubmission,
      String learnerBState,
      MockHttpServletRequestBuilder learnerASubmission)
      throws Exception {
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    MockHttpSession learnerA = new MockHttpSession();
    MockHttpSession learnerB = new MockHttpSession();

    mockMvc.perform(learnerBSubmission.session(learnerB)).andExpect(status().isOk());
    mockMvc
        .perform(get(path).session(learnerB))
        .andExpect(status().isOk())
        .andExpect(content().json(learnerBState));

    mockMvc.perform(learnerASubmission.session(learnerA)).andExpect(status().isOk());

    mockMvc
        .perform(get(path).session(learnerB))
        .andExpect(status().isOk())
        .andExpect(content().json(learnerBState));
  }

  private static Stream<Arguments> quizzes() {
    return Stream.of(
        Arguments.of(
            "Open Redirect",
            new OpenRedirectQuiz(),
            "/OpenRedirect/quiz",
            submission("/OpenRedirect/quiz", "Solution 1", "Solution 1", "Solution 1", "Solution 1"),
            "[false,false,false,false]",
            submission("/OpenRedirect/quiz", "Solution 0", "Solution 2", "Solution 0", "Solution 0")),
        Arguments.of(
            "HTTP Basics",
            new HttpBasicsQuiz(),
            "/HttpBasics/quiz",
            submission("/HttpBasics/quiz", "wrong", "wrong", "wrong", "wrong", "wrong", "wrong", "wrong"),
            "[false,false,false,false,false,false,false]",
            submission("/HttpBasics/quiz", "Solution 2", "Solution 3", "Solution 1", "Solution 2", "Solution 3", "Solution 4", "Solution 4")),
        Arguments.of(
            "CIA",
            new CIAQuiz(),
            "/cia/quiz",
            submission("/cia/quiz", "wrong", "wrong", "wrong", "wrong"),
            "[false,false,false,false]",
            submission("/cia/quiz", "Solution 3", "Solution 1", "Solution 4", "Solution 2")),
        Arguments.of(
            "Cross-Site Scripting",
            new CrossSiteScriptingQuiz(),
            "/CrossSiteScripting/quiz",
            submission("/CrossSiteScripting/quiz", "wrong", "wrong", "wrong", "wrong", "wrong"),
            "[false,false,false,false,false]",
            submission("/CrossSiteScripting/quiz", "Solution 4", "Solution 3", "Solution 1", "Solution 2", "Solution 4")),
        Arguments.of(
            "JWT",
            new JWTQuiz(),
            "/JWT/quiz",
            submission("/JWT/quiz", "wrong", "wrong"),
            "[false,false]",
            submission("/JWT/quiz", "Solution 1", "Solution 2")));
  }

  private static MockHttpServletRequestBuilder submission(String path, String... answers) {
    MockHttpServletRequestBuilder request = post(path);
    for (int i = 0; i < answers.length; i++) {
      request.param("question_" + i + "_solution", answers[i]);
    }
    return request;
  }
}
