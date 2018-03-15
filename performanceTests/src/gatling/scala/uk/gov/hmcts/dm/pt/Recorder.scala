//package uk.gov.hmcts.dm.pt
//
//import io.gatling.recorder.GatlingRecorder
//import io.gatling.recorder.config.RecorderPropertiesBuilder
//import uk.gov.hmcts.dm.pt.util.Helper
//
//object Recorder extends App {
//
//  val props: RecorderPropertiesBuilder = new RecorderPropertiesBuilder
//  props.simulationPackage("uk.gov.hmcts.dm.pt.scenarios")
//  props.simulationOutputFolder(Helper.recorderOutDir)
//  props.bodiesFolder(Helper.bodiesDir)
//
//  GatlingRecorder.fromMap(props.build, Some(Helper.recorderConfigPath))
//}
