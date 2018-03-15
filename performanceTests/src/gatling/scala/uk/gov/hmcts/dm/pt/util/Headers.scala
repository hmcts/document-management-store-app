package uk.gov.hmcts.dm.pt.util

object Headers {

  val commonHeader: Map[String, String] = Map(
    "Accept" -> "application/vnd.uk.gov.hmcts.dm.document.v1+json"
    //    "Accept-Charset" -> "ISO-8859-1,utf-8;q=0.7,*;q=0.7"
    //    "Authorization" -> "user2",
    //    "ServiceAuthorization" ->"sscs"

    //    Uncomment the code below for testing with IDAM
    //    "Accept" -> "application/json"
    //    "Authorization" -> idamTokenGenerator.generateUserToken(),
    //    "ServiceAuthorization" -> token
  )

}
