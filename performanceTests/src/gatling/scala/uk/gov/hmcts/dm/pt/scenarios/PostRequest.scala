package uk.gov.hmcts.dm.pt.scenarios

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmcts.dm.pt.util.idamTokenGenerator

import scala.collection.mutable
import scala.util.Random

object PostRequest {

  val ids: mutable.MutableList[String] = mutable.MutableList[String]()
  val randomNum: Random.type = scala.util.Random
  val tempVal: String = ""
  private val times: Int = 6

  val fileProviderRand: RecordSeqFeederBuilder[String] = csv("listoffiles.csv").random
  val fileProviderSeq: RecordSeqFeederBuilder[String] = csv("listoffiles.csv").queue

  //val serviceToken: String = idamTokenGenerator.generateUserToken()

  val serviceToken: String = idamTokenGenerator.generateS2SToken()

  val postRequest: HttpRequestBuilder = http("Post Request Scenario ${filename}").post("/documents")
    //    .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
    .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
    .bodyPart(RawFileBodyPart("files", "${filename}")
      .contentType("application/pdf")
      .fileName("${filename}")
    ).asMultipartForm
    .formParam("classification", "PUBLIC")
    .check(status is 200, jsonPath("$").saveAs("fileId"))


  val postRequestScenario: ScenarioBuilder = scenario("Post Request Scenario")
    .feed(fileProviderRand)
    //.exec(scn)
    .exec(postRequest)
    .exec{session => println("Response ===========>" + session.get("fileId"))
      session}



  val scn: ScenarioBuilder = scenario("Test Post response") // A scenario is a chain of requests and pauses
    .repeat(times) {
    feed(fileProviderRand)
      .exec(http("Get IDs")
        .post("/documents")
        .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
        .bodyPart(
          RawFileBodyPart("files", "${filename}")
            .contentType("application/pdf")
            .fileName("${filename}")).asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(status is 200, jsonPath("$._embedded.documents[0]._links.self.href").saveAs("fileId")))
      .exec { session =>
        println("fileId --------> " + session.get("fileId"))
        ids += session.get("fileId").as[String]
        session.remove("fileId")
      }
  }
    .repeat(times, "i") {
      exec { session =>
        println("First Session ------>" + session)
        session.remove("fileId")
        val index: Int = session("i").as[Int]
        session.set("fileId", ids(index))
      }
        .exec(
          http("Use id ${fileId}")
            .get("${fileId}")
            .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
            .check(status is 200)
        )
    }

  val scnStoreFileExt: ScenarioBuilder = scenario("Test Post response") // A scenario is a chain of requests and pauses
    .repeat(times) {
    feed(fileProviderRand)
      .exec(http("Get IDs")
        .post("/documents")
        .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
        .bodyPart(
          RawFileBodyPart("files", "${filename}")
            .contentType("application/pdf")
            .fileName("${filename}")).asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(status is 200, jsonPath("$._embedded.documents[0]._links.binary.href").saveAs("fileId")))
      .exec { session =>
        println("fileId --------> " + session.get("fileId"))
        ids += session.get("fileId").as[String]
        session.remove("fileId")
      }
  }
    .repeat(times, "i") {
      exec { session =>
        println("First Session ------>" + session)
        session.remove("fileId")
        val index: Int = session("i").as[Int]
        session.set("fileId", ids(index))
      }
        .exec(
          http("Use id ${fileId}")
            .get("${fileId}")
            .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
            .check(status is 200)
        )
    }

  val storeScn: ChainBuilder =  // A scenario is a chain of requests and pauses
    repeat(times) {
      feed(fileProviderRand)
        .exec(http("Post Files ${filename}")
          .post("/documents")
          .header("ServiceAuthorization", idamTokenGenerator.generateS2SToken()).header("user-id", "gatling")
          .bodyPart(
            RawFileBodyPart("files", "${filename}")
              .contentType("application/pdf")
              .fileName("${filename}")).asMultipartForm
          .formParam("classification", "PUBLIC")
          .check(status is 200, jsonPath("$._embedded.documents[0]._links.binary.href").saveAs("fileId")))
        .exec { session =>
            if(session.contains("fileId")){
          val fname: String = session.get("filename").as[String]
          println("File name --------->" + fname.substring(0, fname.indexOf("MB") + 2))
          println("fileId --------> " + session.get("fileId"))
          ids += session.get("fileId").as[String]}
          session.remove("fileId")
        }
    }

  val fetchScn: ChainBuilder = repeat(times, "i") {
    exec { session =>
      println("First Session ------>" + session)
      if (session.contains("fileId")){
      session.remove("fileId")}
      val index = randomNum.nextInt(ids.length)
      println("Length ------>" + ids.length + " Random Index ----> " + index)
      session.set("fileId", ids(index))
    }
      .exec(
//        http("Use id ${fileId}")
        http("Get Request")
          .get("${fileId}")
          .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
          .check(status is 200)
      )
  }

  val deleteScn: ChainBuilder = repeat(ids.length, "i") {
      exec { session =>
          println("First Session ------>" + session)
          if (session.contains("fileId")){
              session.remove("fileId")}
          val index: Int = session("i").as[Int]
          //val index = randomNum.nextInt(ids.length)
          println("Length ------>" + ids.length + " Random Index ----> " + index)
          session.set("fileId", ids(index))
      }
      .exec (
          http("Delete the record")
            .delete("${fileId}")
            .check(status is 204)
      )
          .exec(
              //        http("Use id ${fileId}")
              http("Get Request")
                  .get("${fileId}")
                  .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
                  .check(status is 404)
          )
  }

  val scnRefactored1: ScenarioBuilder = scenario("Test Post response")
    .repeat(times) {
    feed(fileProviderRand)
      .exec(http("Get IDs")
        .post("/documents")
        .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
        .bodyPart(
          RawFileBodyPart("files", "${filename}")
            .contentType("application/pdf")
            .fileName("${filename}")
        ).asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(status is 200, jsonPath("$._embedded.documents[0]._links.binary.href").saveAs("fileId")))
      .exec { session =>
      println("fileId --------> " + session.get("fileId"))
      ids += session.get("fileId").as[String]
      session.remove("fileId")
    }
  }
    .repeat(times, "i") {
      exec { session =>
        println("First Session ------>" + session)
        session.remove("fileId")
        val index = session("i").as[Int]
        session.set("fileId", ids(index))
      }
        .exec(
          http("Use id ${fileId}")
            .get("${fileId}")
            .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
            .check(status is 200)
        )
    }

  val storeInSeq: ChainBuilder = feed(fileProviderSeq)
      .exec(http("Post Files ${filename}")
        .post("/documents")
        .header("ServiceAuthorization", idamTokenGenerator.generateS2SToken()).header("user-id", "gatling")
        .bodyPart(
          RawFileBodyPart("files", "${filename}")
            .contentType("application/pdf")
            .fileName("${filename}")
        ).asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(status is 200, jsonPath("$._embedded.documents[0]._links.binary.href").saveAs("fileId"))
        .check(status is 200, jsonPath("$._embedded.documents[0].originalDocumentName").saveAs("fileName")))
      .pause(5)

  val fetchInSeq: ChainBuilder = exec(
    http("Get Files ${fileName}")
      .get("${fileId}")
      .header("ServiceAuthorization", serviceToken).header("user-id", "gatling")
      .check(status is 200)
  )

}
