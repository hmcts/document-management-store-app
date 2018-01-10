package uk.gov.hmcts.dm.service;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.InputStream;

public interface ThumbnailCreator {
    String THUMBNAIL_FORMAT = "jpg";
    int DEFAULT_WIDTH = 256;

    InputStream getThumbnail(DocumentContentVersion documentContentVersion);

    boolean supports(String mimeType);

}
