package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.io.InputStream;
import java.util.Map;


@Service
public class DocumentThumbnailService {

    private final Map<String,ThumbnailCreator> thumbnailCreatorsMimeMap;
    private final ThumbnailCreator unsupportedThumbnailService;

    @Autowired
    public DocumentThumbnailService(
        @Qualifier("thumbnailCreatorsMimeMap") Map<String,ThumbnailCreator> thumbnailCreatorsMimeMap,
                                    UnsupportedThumbnailCreator unsupportedThumbnailService) {
        this.thumbnailCreatorsMimeMap = thumbnailCreatorsMimeMap;
        this.unsupportedThumbnailService = unsupportedThumbnailService;
    }

    public Resource generateThumbnail(DocumentContentVersion documentContentVersion) {
        ThumbnailCreator thumbnailCreator = selectThumbnailCreator(documentContentVersion);
        InputStream inputStream = thumbnailCreator.getThumbnail(documentContentVersion);
        if (inputStream != null) {
            return new InputStreamResource(inputStream);
        } else {
            throw new CantCreateThumbnailException("Input Stream is null");
        }
    }

    private ThumbnailCreator selectThumbnailCreator(DocumentContentVersion documentContentVersion) {
        return thumbnailCreatorsMimeMap.getOrDefault(documentContentVersion.getMimeType(), unsupportedThumbnailService);
    }

}
