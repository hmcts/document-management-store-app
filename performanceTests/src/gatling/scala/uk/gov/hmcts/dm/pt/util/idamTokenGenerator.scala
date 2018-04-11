package uk.gov.hmcts.dm.pt.util

import com.warrenstrange.googleauth.GoogleAuthenticator
import io.restassured.RestAssured

object idamTokenGenerator {

    val params = Map(
        "microservice", "em_gw",
        "oneTimePassword", new GoogleAuthenticator().getTotpPassword(Environments.s2sSecret)
    )

    def generateS2SToken(): String = {
        RestAssured.given
            .relaxedHTTPSValidation
            .baseUri(Environments.idamS2S)
            //          .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header("Content-Type", "application/json")
            .body(params)
            .post("/lease")
          .andReturn().toString
    }
}


