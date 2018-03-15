package uk.gov.hmcts.dm.pt.util

object Environments {

  val dmStoreApp : String = scala.util.Properties.envOrElse("TEST_URL","http://localhost:4603")
  val idamS2S : String = scala.util.Properties.envOrElse("IDAM_S2S_BASE_URI","http://localhost:4502") + "/testing-support/lease"


  val users: String = scala.util.Properties.envOrElse("NUMBER_OF_USERS", "500")
  val maxResponseTime: String = scala.util.Properties.envOrElse("MAX_RES_TIME", "3000000") // in milliseconds
}
