package uk.gov.hmcts.dm.smoke.utilities;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public File getResourceFile(String fileName) {
        String file = getClass().getClassLoader().getResource(fileName).getFile();

        String result = URLDecoder.decode(file, StandardCharsets.UTF_8);
        return new File(result);

    }

}
