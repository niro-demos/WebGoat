/*
 * SPDX-FileCopyrightText: Copyright © 2026 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class FileServerTest {

  @TempDir Path fileLocation;

  @Test
  void uploadShouldNotEscapeAuthenticatedUsersDirectory() throws Exception {
    FileServer fileServer = new FileServer();
    ReflectionTestUtils.setField(fileServer, "fileLocation", fileLocation.toString());
    Authentication alice = authentication("alice");
    Authentication bob = authentication("bob");

    fileServer.importFile(file("bob-file.txt", "owned by bob"), bob);
    assertThat(fileLocation.resolve("bob/bob-file.txt"))
        .hasContent("owned by bob");

    Throwable rejection =
        catchThrowable(
            () ->
                fileServer.importFile(
                    file("../bob/written-by-alice.txt", "owned by alice"), alice));

    assertThat(fileLocation.resolve("bob/written-by-alice.txt")).doesNotExist();
    assertThat(rejection).isInstanceOf(ResponseStatusException.class);
  }

  private MockMultipartFile file(String filename, String contents) {
    return new MockMultipartFile("file", filename, "text/plain", contents.getBytes());
  }

  private Authentication authentication(String username) {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn(username);
    return authentication;
  }
}
