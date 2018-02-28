package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.InputStream;

public interface ThumbnailCreator {

    InputStream getThumbnail(DocumentContentVersion documentContentVersion);

}
