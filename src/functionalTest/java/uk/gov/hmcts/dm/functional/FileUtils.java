package uk.gov.hmcts.dm.functional;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class FileUtils {
    public File getResourceFile(String fileName) {
        String file = getClass().getClassLoader().getResource(fileName).getFile();

        try {
            String result = URLDecoder.decode(file, "UTF-8");
            return new File(result);
        } catch (UnsupportedEncodingException e) {
            return new File(file);
        }

    }

}
