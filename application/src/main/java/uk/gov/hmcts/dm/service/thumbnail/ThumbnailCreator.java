package uk.gov.hmcts.dm.service.thumbnail;

import java.io.InputStream;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

public interface ThumbnailCreator {

    InputStream getThumbnail(DocumentContentVersion documentContentVersion);

}
