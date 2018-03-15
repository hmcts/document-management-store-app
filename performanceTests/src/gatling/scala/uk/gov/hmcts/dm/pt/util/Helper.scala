package uk.gov.hmcts.dm.pt.util

import java.io.File
import java.nio.file.{Path, Paths}


object Helper {

  //  UNIX = '/' WINDOWS = '\'
  val dirSep : String = File.separator

  //  ./
  val projectRootDir: String = getClass.getResource("").getPath

  // ./src/test/scala
//  val sourcesDirectory: String = projectRootDir + dirSep + "src" + dirSep +  "test" + dirSep +  "scala"

  // ./src/test/resources
  val resDir: String = projectRootDir + dirSep + "src" + dirSep + "test" + dirSep +  "resources"

  // ./src/test/resources/data
  val dataDir: String = resDir + dirSep +   "data"

  // ./src/test/resources/bodies
  val bodiesDir: String = resDir + dirSep + "bodies"

  // ./src/test/resources/recorder.conf
  val recorderConfigFile: String = resDir + dirSep + "recorder.conf"

  // ./src/test/resources/recorder.conf : Path
  val recorderConfigPath: Path = Paths.get(recorderConfigFile)


  // ./target
  val targetDir: String = projectRootDir + dirSep +  "target"

  // ./target
  val recorderOutDir: String = targetDir

  // ./target/test-classes
  val binDir: String = targetDir + dirSep +   "test-classes"

  // ./src/test/resources/results
  val resultsDir: String = targetDir + dirSep + "results"

}
