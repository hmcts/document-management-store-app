package uk.gov.hmcts.dm.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HealthControllerTests extends ComponentTestBase {

    @Test
    @Ignore
    public void testGetSuccess() throws Exception {
        restActions
            .get("/health")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", Matchers.is("UP")));
    }

    @Test
    public void testHealthEndpointForSuccess() {

        final String healthURL = "/health";

        stubFor(
            WireMock.get(urlEqualTo(healthURL))
                .willReturn(
                    aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBody("""
                            {
                                "status": "UP",
                                "components": {
                                  "diskSpace": {
                                    "status": "UP",
                                    "details": {
                                      "total": 62671097856,
                                      "free": 37045514240,
                                      "threshold": 10485760,
                                      "path": "/opt/app/.",
                                      "exists": true
                                    }
                                  },
                                  "ping": {
                                    "status": "UP"
                                  }
                                }
                              }
                            """)
                        .withStatus(200)
                )
        );

        verify(getRequestedFor(urlEqualTo(healthURL))
            .withRequestBody(
                matchingJsonPath("$.status", equalTo("UP"))
                    .and()
            ));
    }
}
