package uk.gov.hmcts.dm.utils;

public class StringUtils {

    private StringUtils(){}

    public static final String sanitiseFileName(String originalDocumentName) {
        return originalDocumentName == null ? null :
                originalDocumentName.replaceAll("[^a-zA-Z0-9\\.\\-_+ ]","");
    }
}
