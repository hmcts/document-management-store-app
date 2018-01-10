package uk.gov.hmcts.dm.service;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.InputStream;

public class UnsupportedThumbnailService implements ThumbnailCreator {
    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        return null;
    }

    @Override
    public boolean supports(String mimeType) {
        return false;
    }
}
