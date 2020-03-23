package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public interface ThumbnailCreator {

    InputStream getThumbnail(DocumentContentVersion documentContentVersion, HttpServletRequest request, HttpServletResponse response);

}
