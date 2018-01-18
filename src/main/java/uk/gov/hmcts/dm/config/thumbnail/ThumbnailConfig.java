package uk.gov.hmcts.dm.config.thumbnail;

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.dm.service.thumbnail.ImageThumbnailCreator;
import uk.gov.hmcts.dm.service.thumbnail.PdfThumbnailCreator;
import uk.gov.hmcts.dm.service.thumbnail.ThumbnailCreator;

import java.util.List;

@Configuration
public class ThumbnailConfig {

    @Bean(name = "thumbnailCreators")
    public List<ThumbnailCreator> thumbnailCreatorsList() {
        return ImmutableList.of(
            new ImageThumbnailCreator(),
            new PdfThumbnailCreator()
        );
    }

}
