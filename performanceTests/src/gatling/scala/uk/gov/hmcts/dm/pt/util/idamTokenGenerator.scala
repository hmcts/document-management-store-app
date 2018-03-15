package uk.gov.hmcts.dm.pt.util

import io.restassured.RestAssured._

object idamTokenGenerator {


  def generateS2SToken() : String = {

    val serviceToken = given().multiPart("microservice", "em_gw")
      .post(Environments.idamS2S)
      .asString()

    serviceToken
  }

}
