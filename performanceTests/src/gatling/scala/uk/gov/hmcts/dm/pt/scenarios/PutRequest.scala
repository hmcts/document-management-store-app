package uk.gov.hmcts.dm.pt.scenarios

import GetRequest.feeder
import PostRequest.postRequest
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import uk.gov.hmcts.dm.pt.util.idamTokenGenerator

object PutRequest {
  //val jResponse = new JsonResponse()
  //  jResponse.postFileAsARequest()

  //    val putRequest = http(Environments.dmApiGw)
  //      .put("/documents/1")
  //      .check(status is 200)

  val serviceToken: String = idamTokenGenerator.generateS2SToken()
  //val usertoken: String = idamTokenGenerator.generateUserToken()

  //  ScalaJdbcConnectSelect.fill_storedFile()
  //  ScalaJdbcConnectSelect.fill_fileContent()
  //  ScalaJdbcConnectSelect.fill_fileContentVersion()

  //  val postRequest = http("Post/Put Request Scenario")
  //  .post("/api/v1/files")
  //  .formUpload("file", "Dummy.txt")
  //  .check(jsonPath("$._embedded.storedFileHalResources[0]._links.self.href").saveAs("fileId"))
  //  .check(status is 200)

  val putRequest: ScenarioBuilder = scenario("Post Request Scenario")
    .exec(postRequest)
    .exec(http("get")
      .get("${fileId}")
      .check(status is 200))


  //  val putRequest = http(Environments.dmApiGw)
  //    .put("/documents/10")
  //    .formUpload("file", "pdf-sample.pdf")
  //    .check(status is 200)

  val putRequest2: ScenarioBuilder = scenario("Put request scenario")
    .pause(1)
    .repeat(1){//feeder.records.length){
      feed(feeder)
        .exec(http("Put Test")
          .put("/api/files/86d4efab-fa41-4681-913f-d7931d94e2a2")
          //          .header("Accept", "application/json")
          .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
          .header("Accept","application/vnd.uk.gov.hmcts.dm.filecontentversion.v1+json")
          //          .put("/documents/${id}")
          .formUpload("file", "Dummy.txt")
          .check(status is 200))  }


  //  val putRequestScenario = scenario("Put Request Scenario")
  //      .exec(putRequest)
  //    .check(jsonPath("$._embedded.storedFileHalResources").saveAs("myresponseId"))
  //    .check(regex(".+\"href\".+v1/files/(\\d+)\".+").saveAs("fileId"))
  //    .check(status is 200)).exec(session => {
  //    println(session.get("myresponseId").asOption[String])
  //     val fileId = "/documents/" + session.get("fileId")
  //    //println("--------------------------------------" + session.get("fileId"))
  //    http(Environments.dmApiGw)
  //        .put(fileId)
  //          .formUpload("file", "Dummy.txt")
  //        .check(status is 200)
  //    session
  //  }
  //  )

  //  val putRequestScenario = scenario("Put Request Scenario")
  //    .exec(postRequest)
  //    .exec(session => { val fileId = session.get("id ").asOption[String]
  //    println(fileId.getOrElse(""))})
  //    .exec(putRequest)
  //
}
