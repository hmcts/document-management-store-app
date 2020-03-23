package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service
public class UnsupportedThumbnailCreator implements ThumbnailCreator {

    public static final String DEFAULT_FILE_THUMBNAIL = "/files/default-file.jpg";

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion, HttpServletRequest request, HttpServletResponse response) {
        return getClass().getResourceAsStream(DEFAULT_FILE_THUMBNAIL);
    }

}
