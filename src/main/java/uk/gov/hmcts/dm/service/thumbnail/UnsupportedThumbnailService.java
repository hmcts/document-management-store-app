package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.InputStream;
import java.net.URL;

@Service
public class UnsupportedThumbnailService implements ThumbnailCreator {

    public static final String DEFAULT_FILE_THUMBNAIL = "/files/default-file.jpg";

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        URL url = getClass().getResource(DEFAULT_FILE_THUMBNAIL);
        System.out.printf(url.getFile());

        return getClass().getResourceAsStream(DEFAULT_FILE_THUMBNAIL);
    }

}
