package uk.gov.hmcts.dm.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

    private StringUtils() {
    }

    public static final String sanitiseFileName(String originalDocumentName) {
        return originalDocumentName == null ? null :
                originalDocumentName.replaceAll("[^a-zA-Z0-9\\.\\-_+ ]","");
    }

    public static String convertValidLogString(String log) {
        List<String> list = Arrays.asList("%0d", "\r", "%0a", "\n");

        // normalize the log content
        String encode = Normalizer.normalize(log, Normalizer.Form.NFKC);
        for (String toReplaceStr : list) {
            encode = encode.replace(toReplaceStr, "");
        }
        return encode;
    }
}
