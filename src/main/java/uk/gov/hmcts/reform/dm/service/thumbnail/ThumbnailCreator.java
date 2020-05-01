package uk.gov.hmcts.reform.dm.service.thumbnail;

import uk.gov.hmcts.reform.dm.domain.DocumentContentVersion;

import java.io.InputStream;

public interface ThumbnailCreator {

    InputStream getThumbnail(DocumentContentVersion documentContentVersion);

}
