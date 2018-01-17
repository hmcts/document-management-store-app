package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class DocumentThumbnailService {

    private final List<FileSpecificThumbnailCreator> thumbnailCreators;
    private final ThumbnailCreator unsupportedThumbnailService;

    @Autowired
    public DocumentThumbnailService(List<FileSpecificThumbnailCreator> thumbnailCreators,
                                    UnsupportedThumbnailService unsupportedThumbnailService) {
        this.thumbnailCreators = new ArrayList<>(thumbnailCreators);
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
        Optional<? extends ThumbnailCreator> maybeThumbnailCreator = thumbnailCreators.stream()
            .filter(tc -> tc.supports(documentContentVersion.getMimeType()))
            .findFirst();
        if (maybeThumbnailCreator.isPresent()) {
            return maybeThumbnailCreator.get();
        }
        return unsupportedThumbnailService;
    }

}
