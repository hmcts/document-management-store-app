package uk.gov.hmcts.dm.pt.util

object Environments {

    //  val idam : String = scala.util.Properties.envOrElse("IDAM_API_URL","http://localhost:4501")
    val idamS2S : String = scala.util.Properties.envOrElse("S2S_URL","http://localhost:4502")
    val s2sSecret : String = scala.util.Properties.envOrElse("S2S_TOKEN","AAAAAAAAAAAAAAAA")
    val dmStore : String = scala.util.Properties.envOrElse("TEST_URL","http://localhost:4603")

    val users: String = scala.util.Properties.envOrElse("NUMBER_OF_USERS", "500")
    val maxResponseTime: String = scala.util.Properties.envOrElse("MAX_RES_TIME", "3000000") // in milliseconds
}
