package uk.gov.hmcts.dm.controller;

import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HealthControllerTests extends ComponentTestBase {

    @Test
    public void testGetSuccess() throws Exception {
        restActions
            .get("/health")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", Matchers.is("UP")));
    }

}
