/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import static org.owasp.webgoat.container.assignments.AttackResultBuilder.failed;
import static org.owasp.webgoat.container.assignments.AttackResultBuilder.success;

import jakarta.servlet.http.HttpSession;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Quiz assignment for the Open Redirect lesson. */
@RestController
public class OpenRedirectQuiz implements AssignmentEndpoint {

  // Correct solution indices mapped to their label prefix used by quiz.js ("Solution <index>")
  private final String[] solutions = {"Solution 0", "Solution 2", "Solution 0", "Solution 0"};
  private static final String SESSION_KEY = OpenRedirectQuiz.class.getName() + ".guesses";

  @PostMapping("/OpenRedirect/quiz")
  @ResponseBody
  public AttackResult submit(
      @RequestParam(name = "question_0_solution") String[] q0,
      @RequestParam(name = "question_1_solution") String[] q1,
      @RequestParam(name = "question_2_solution") String[] q2,
      @RequestParam(name = "question_3_solution") String[] q3,
      HttpSession session) {

    String[] given = {q0[0], q1[0], q2[0], q3[0]};
    boolean[] guesses = new boolean[solutions.length];
    int correct = 0;
    for (int i = 0; i < solutions.length; i++) {
      if (given[i].contains(solutions[i])) {
        guesses[i] = true;
        correct++;
      } else {
        guesses[i] = false;
      }
    }
    session.setAttribute(SESSION_KEY, guesses);
    if (correct == solutions.length) {
      return success(this).feedback("openredirect.quiz.success").build();
    }
    return failed(this).feedback("openredirect.quiz.failure").build();
  }

  @GetMapping("/OpenRedirect/quiz")
  @ResponseBody
  public boolean[] results(HttpSession session) {
    boolean[] guesses = (boolean[]) session.getAttribute(SESSION_KEY);
    return guesses == null ? new boolean[solutions.length] : guesses;
  }
}
