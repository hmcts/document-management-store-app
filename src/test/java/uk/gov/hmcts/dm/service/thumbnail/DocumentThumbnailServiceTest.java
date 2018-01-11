package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;
import uk.gov.hmcts.dm.service.thumbnail.FileSpecificThumbnailCreator;
import uk.gov.hmcts.dm.service.thumbnail.UnsupportedThumbnailService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentThumbnailServiceTest {

    private DocumentThumbnailService documentThumbnailService;

    @Mock
    private FileSpecificThumbnailCreator mockFileSpecificThumbnailCreator;

    @Mock
    private UnsupportedThumbnailService mockUnsupportedThumbnailService;

    @Before
    public void setUp() throws Exception {
        List<FileSpecificThumbnailCreator> fileSpecificThumbnailCreators = Collections.singletonList(mockFileSpecificThumbnailCreator);
        documentThumbnailService = new DocumentThumbnailService(fileSpecificThumbnailCreators,mockUnsupportedThumbnailService);
    }

    @Test
    public void returnImageThumbnail() throws IOException {

        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);
        InputStream expectedInputStream = new ByteArrayInputStream(new byte[]{0});

        when(documentContentVersion.getMimeType())
            .thenReturn(MediaType.IMAGE_JPEG_VALUE);

        when(mockFileSpecificThumbnailCreator.supports(MediaType.IMAGE_JPEG_VALUE))
            .thenReturn(true);

        when(mockFileSpecificThumbnailCreator.getThumbnail(documentContentVersion))
            .thenReturn(expectedInputStream);

        Resource generateThumbnail = documentThumbnailService.generateThumbnail(documentContentVersion);

        Assert.assertThat(generateThumbnail.getInputStream(), equalTo(expectedInputStream));
    }

    @Test
    public void returnUnsupportedThumbnail() throws IOException {
        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);
        InputStream expectedInputStream = new ByteArrayInputStream(new byte[]{0});

        when(documentContentVersion.getMimeType())
            .thenReturn(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        when(mockFileSpecificThumbnailCreator.supports(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .thenReturn(false);

        when(mockUnsupportedThumbnailService.getThumbnail(documentContentVersion))
            .thenReturn(expectedInputStream);

        Resource generateThumbnail = documentThumbnailService.generateThumbnail(documentContentVersion);

        Assert.assertThat(generateThumbnail.getInputStream(), equalTo(expectedInputStream));
    }

}
