package uk.gov.hmcts.dm.smoke;

import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.smoke.config.SmokeTestContextConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {SmokeTestContextConfiguration.class})
@ExtendWith(value = {SerenityJUnit5Extension.class, SpringExtension.class})
@WithTags(@WithTag("testType:Smoke"))
class SmokeTest {

    private static final String MESSAGE = "Welcome to DM Store API!";

    @Value("${base-urls.dm-store}")
    String testUrl;

    @Test
    void testHealthEndpoint() {
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

        assertEquals(1, responseMap.size());
        assertEquals(MESSAGE, responseMap.get("message"));

    }

    @Test
    void testHealthUrlEndpoint() {
        SerenityRest.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .given()
            .baseUri(testUrl)
            .get("/health")
            .then()
            .statusCode(200)
            .extract()
            .body().jsonPath().getString("status");

        assertEquals("UP", response);
    }
}
