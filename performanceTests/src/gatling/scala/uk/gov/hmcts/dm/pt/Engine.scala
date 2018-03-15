//package uk.gov.hmcts.dm.pt
//
//import io.gatling.app.Gatling
//import io.gatling.core.config.GatlingPropertiesBuilder
//import uk.gov.hmcts.dm.pt.util.Helper
//
//object Engine extends App {
//
//  val props: GatlingPropertiesBuilder = new GatlingPropertiesBuilder
//  props.dataDirectory(Helper.dataDir)
//  props.resultsDirectory(Helper.resultsDir)
//  props.bodiesDirectory(Helper.bodiesDir)
//  props.binariesDirectory(Helper.binDir)
//
//  Gatling.fromMap(props.build)
//}
