package uk.gov.hmcts.dm.smoke.utilities

class FileUtils {

    File getResourceFile(String fileName){
//        new File(getClass().getClassLoader().getResource(fileName).path.replace("%20", " "))
        new File(URLDecoder.decode(getClass().getClassLoader().getResource(fileName).path, "UTF-8"))
    }


}
