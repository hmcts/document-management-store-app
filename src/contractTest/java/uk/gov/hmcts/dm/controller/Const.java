package uk.gov.hmcts.dm.controller;

public final class Const {

    public static final String EXAMPLE_USER = "user@example.com";
    public static final String EXAMPLE_SERVICE = "some-service";
    public static final String TEST_USER = "test-user";


    public static final String DOCUMENTS_IN_URI = "/documents/";
    public static final String BINARY = "/binary";
    public static final String VERSIONS_IN_URI = "/versions/";
    public static final String PUBLIC_CLASSIFICATION = "PUBLIC";

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";


    public static final String ACCEPT_HEADER = "Accept";
    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE = "application/pdf";
    public static final String LOCATION_HEADER = "Location";

    public static final String DOCUMENT_NAME = "sample.pdf";
    public static final String BODY_FIELD_ORIGINAL_DOCUMENT_NAME = "originalDocumentName";
    public static final String BODY_FIELD_MIME_TYPE = "mimeType";
    public static final String BODY_FIELD_LINKS_DOCUMENT_HREF = "_links.document.href";
    public static final String BODY_FIELD_LINKS_BINARY_HREF = "_links.binary.href";
    public static final String BODY_FIELD_LINKS_SELF_HREF = "_links.self.href";

    public static final String BODY_FIELD_CLASSIFICATION = "classification";


    public static final String DUMMY_SERVICE_AUTHORIZATION_VALUE = "Bearer some-s2s-token";

    public static final String HTTP_LOCALHOST_DOCUMENTS_URL = "http://localhost/documents/";
    public static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+0000";

    private Const() {
    }
}
