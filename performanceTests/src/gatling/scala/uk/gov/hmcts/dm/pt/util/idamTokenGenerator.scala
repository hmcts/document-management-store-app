package uk.gov.hmcts.dm.pt.util

import com.warrenstrange.googleauth.GoogleAuthenticator
import io.restassured.RestAssured

object idamTokenGenerator {

    def generateS2SToken(): String = {
        val request = RestAssured.given
            .relaxedHTTPSValidation
            .baseUri(Environments.idamS2S)
            //          .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header("Content-Type", "application/json")
            .body(
                Map(
                    ("microservice", "em_gw"),
                    ("oneTimePassword", new GoogleAuthenticator().getTotpPassword(Environments.s2sSecret))
                )
            )
            .post("/lease")
            .andReturn().toString

        print("generateS2SToken===============" + request)
        request
    }
}


