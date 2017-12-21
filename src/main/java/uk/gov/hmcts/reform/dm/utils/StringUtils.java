package uk.gov.hmcts.reform.dm.utils;

/**
 * Created by pawel on 04/10/2017.
 */
public class StringUtils {

    private StringUtils(){}

    public static final String sanitiseFileName(String originalDocumentName) {
        return originalDocumentName == null ? null :
                originalDocumentName.replaceAll("[^a-zA-Z0-9\\.\\-_+ ]","");
    }
}
