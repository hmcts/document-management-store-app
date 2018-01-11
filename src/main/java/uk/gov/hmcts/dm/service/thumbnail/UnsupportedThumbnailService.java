package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.InputStream;

@Service
public class UnsupportedThumbnailService implements ThumbnailCreator {

    public static final String DEFAULT_FILE_THUMBNAIL = "img/default-file.jpg";

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        InputStream inputStream = getClass().getResourceAsStream(DEFAULT_FILE_THUMBNAIL);

        inputStream.available();
        return
    }

}
