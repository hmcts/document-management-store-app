package uk.gov.hmcts.dm.smoke;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.dm.smoke.config.SmokeTestContextConfiguration;

@SpringBootTest(classes = {SmokeTestContextConfiguration.class})
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags(@WithTag("testType:Smoke"))
public class SmokeTest {

    private static final String MESSAGE = "{\"message\":\"Welcome to DM Store API!\"}";

    @Value("${base-urls.dm-store}")
    String testUrl;

    @Test
    public void testHealthEndpoint() {
        SerenityRest.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .given()
            .baseUri(testUrl)
            .get("/")
            .then()
            .statusCode(200).extract().body().asString();

        Assert.assertEquals(MESSAGE, response);
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
