/*
 *
 * Copyright (c) 2022. Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 *
 */

package org.onap.portalng.bff.preferences;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.restassured.http.Header;
import org.junit.jupiter.api.Test;
import org.onap.portalng.bff.openapi.client_preferences.model.PreferencesPreferencesDto;
import org.onap.portalng.bff.openapi.client_preferences.model.ProblemPreferencesDto;
import org.onap.portalng.bff.openapi.server.model.CreatePreferencesRequestApiDto;
import org.onap.portalng.bff.openapi.server.model.PreferencesResponseApiDto;
import org.onap.portalng.bff.openapi.server.model.ProblemApiDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class CreatePreferencesIntegrationTest extends PreferencesMocks {
  @Test
  void thatPreferencesCanBeCreated() throws Exception {
    PreferencesPreferencesDto preferencesPreferencesDto = new PreferencesPreferencesDto();
    preferencesPreferencesDto.setProperties(
        """
        {
        "properties": {
        "appStarter": "value1",
        "dashboard": {"key1:" : "value2"}
        }

        }\
        """);
    mockCreatePreferences(preferencesPreferencesDto);

    final CreatePreferencesRequestApiDto request =
        new CreatePreferencesRequestApiDto()
            .properties(
                """
                {
                "properties": {
                "appStarter": "value1",
                "dashboard": {"key1:" : "value2"}
                }

                }\
                """);
    final PreferencesResponseApiDto response = createPreferences(request);
    assertThat(response).isNotNull();
    assertThat(response.getProperties()).isEqualTo(preferencesPreferencesDto.getProperties());
  }

  @Test
  void thatPreferencesCanNotBeCreated() throws Exception {
    final var problemPreferencesDto = new ProblemPreferencesDto();
    problemPreferencesDto.setStatus(HttpStatus.BAD_REQUEST.value());
    problemPreferencesDto.setTitle(HttpStatus.BAD_REQUEST.toString());
    problemPreferencesDto.setDetail("Some details");

    final PreferencesPreferencesDto preferencesPreferencesDto =
        new PreferencesPreferencesDto()
            .properties(
                """
                {
                "properties": {
                "appStarter": "value1",
                "dashboard": {"key1:" : "value2"}
                }

                }\
                """);
    mockCreatePreferencesError(preferencesPreferencesDto, problemPreferencesDto);

    CreatePreferencesRequestApiDto responseBody =
        new CreatePreferencesRequestApiDto()
            .properties(
                """
                {
                "properties": {
                "appStarter": "value1",
                "dashboard": {"key1:" : "value2"}
                }

                }\
                """);
    final ProblemApiDto response =
        requestSpecification()
            .given()
            .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header(new Header("X-Request-Id", X_REQUEST_ID))
            .body(responseBody)
            .when()
            .post("/preferences")
            .then()
            .statusCode(HttpStatus.BAD_GATEWAY.value())
            .extract()
            .body()
            .as(ProblemApiDto.class);

    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
    assertThat(response.getDetail()).isEqualTo(problemPreferencesDto.getDetail());
    assertThat(response.getDownstreamSystem())
        .isEqualTo(ProblemApiDto.DownstreamSystemEnum.PREFERENCES);
    assertThat(response.getDownstreamStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
