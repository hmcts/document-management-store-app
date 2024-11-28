package uk.gov.hmcts.dm.smoke;

import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.dm.smoke.config.SmokeTestContextConfiguration;

import java.util.Map;

@SpringBootTest(classes = {SmokeTestContextConfiguration.class})
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags(@WithTag("testType:Smoke"))
public class SmokeTest {

    private static final String MESSAGE = "Welcome to DM Store API!";

    @Value("${base-urls.dm-store}")
    String testUrl;

    @Test
    public void testHealthEndpoint() {
        SerenityRest.useRelaxedHTTPSValidation();

        Map responseMap =
            SerenityRest
                .given()
                .baseUri(testUrl)
                .get("/")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Map.class);

        Assert.assertEquals(1, responseMap.size());
        Assert.assertEquals(MESSAGE, responseMap.get("message"));

    }

    @Test
    public void testHealthUrlEndpoint() {
        SerenityRest.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .given()
            .baseUri(testUrl)
            .get("/health")
            .then()
            .statusCode(200)
            .extract()
            .body().jsonPath().getString("status");

        Assert.assertEquals("UP", response);
    }
}
