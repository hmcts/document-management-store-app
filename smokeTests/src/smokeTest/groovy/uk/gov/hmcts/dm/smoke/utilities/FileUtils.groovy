package uk.gov.hmcts.dm.smoke.utilities

/**
 * Created by pawel on 17/10/2017.
 */
class FileUtils {

    File getResourceFile(String fileName){
//        new File(getClass().getClassLoader().getResource(fileName).path.replace("%20", " "))
        new File(URLDecoder.decode(getClass().getClassLoader().getResource(fileName).path, "UTF-8"))
    }


}
