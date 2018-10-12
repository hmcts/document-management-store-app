package uk.gov.hmcts.dm.config.thumbnail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.thumbnail.ImageThumbnailCreator;
import uk.gov.hmcts.dm.service.thumbnail.PdfThumbnailCreator;
import uk.gov.hmcts.dm.service.thumbnail.ThumbnailCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ThumbnailConfig {

    private final BlobStorageReadService blobStorageReadService;

    @Autowired
    public ThumbnailConfig(BlobStorageReadService blobStorageReadService) {
        this.blobStorageReadService = blobStorageReadService;
    }

    @Bean("thumbnailCreatorsMimeMap")
    public Map<String,ThumbnailCreator> thumbnailCreatorsMimeMap(
        @Value("#{'${thumbnail.imageThumbnailCreator}'.split(',')}") List<String> imageThumbnailCreatorMimeTypes,
        @Value("#{'${thumbnail.pdfThumbnailCreator}'.split(',')}") List<String> pdfThumbnailCreatorMimeTypes
    ) {
        Map<String,ThumbnailCreator> map = new HashMap<>();

        imageThumbnailCreatorMimeTypes.forEach(s -> map.put(s,imageThumbnailCreator()));
        pdfThumbnailCreatorMimeTypes.forEach(s -> map.put(s,pdfThumbnailCreator()));

        return map;
    }

    @Bean
    ImageThumbnailCreator imageThumbnailCreator() {
        return new ImageThumbnailCreator(blobStorageReadService);
    }

    @Bean
    PdfThumbnailCreator pdfThumbnailCreator() {
        return new PdfThumbnailCreator(blobStorageReadService);
    }
}
