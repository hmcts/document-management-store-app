package uk.gov.hmcts.dm.pt.util


import com.warrenstrange.googleauth.GoogleAuthenticator
import io.restassured.RestAssured
import io.restassured.response.Response

object idamTokenGenerator {

    def generateS2SToken(): String = {
        val otp = new GoogleAuthenticator().getTotpPassword(Environments.s2sSecret)
        val body = s"""{ "microservice": "em_gw", "oneTimePassword": $otp }"""

        val response: Response = RestAssured
            .given
            .relaxedHTTPSValidation
            .baseUri(Environments.idamS2S)
            .header("Content-Type", "application/json")
            .body(body)
            .post("/lease")

       "Bearer " + response.getBody.asString()
    }

}


