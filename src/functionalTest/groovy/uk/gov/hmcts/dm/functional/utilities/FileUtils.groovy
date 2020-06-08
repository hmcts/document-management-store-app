package uk.gov.hmcts.dm.functional.utilities

class FileUtils {

    File getResourceFile(String fileName) {
        String file = getClass().getClassLoader().getResource(fileName).getFile();

        try {
            String result = URLDecoder.decode(file, "UTF-8");
            new File(result);
        } catch (UnsupportedEncodingException e) {
            new File(file);
        }
    }
}
