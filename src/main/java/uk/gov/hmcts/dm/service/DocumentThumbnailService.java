package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.util.List;


@Service
public class DocumentThumbnailService {

    private final List<ThumbnailCreator> thumbnailCreators;

    @Autowired
    public DocumentThumbnailService(List<ThumbnailCreator> thumbnailCreators) {
        this.thumbnailCreators = thumbnailCreators;
    }

    public Resource generateThumbnail(DocumentContentVersion documentContentVersion) {
        return new InputStreamResource(thumbnailCreators.stream()
            .filter(tc -> tc.supports(documentContentVersion.getMimeType()))
            .findFirst()
            .orElseGet(new UnsupportedThumbnailService())
            .getThumbnail(documentContentVersion));
    }

}
