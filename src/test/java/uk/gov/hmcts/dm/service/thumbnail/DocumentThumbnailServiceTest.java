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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentThumbnailServiceTest {

    private DocumentThumbnailService singleDocumentThumbnailService;

    private DocumentThumbnailService multiDocumentThumbnailService;

    @Mock
    private FileSpecificThumbnailCreator mockFileSpecificThumbnailCreator1;

    @Mock
    private FileSpecificThumbnailCreator mockFileSpecificThumbnailCreator2;

    @Mock
    private UnsupportedThumbnailService mockUnsupportedThumbnailService;

    @Before
    public void setUp() {
        List<FileSpecificThumbnailCreator> singleFileSpecificThumbnailCreators = Collections
            .singletonList(mockFileSpecificThumbnailCreator1);
        singleDocumentThumbnailService = new DocumentThumbnailService(singleFileSpecificThumbnailCreators,mockUnsupportedThumbnailService);

        List<FileSpecificThumbnailCreator> mutipleFileSpecificThumbnailCreators = Arrays
            .asList(mockFileSpecificThumbnailCreator1, mockFileSpecificThumbnailCreator2);
        multiDocumentThumbnailService = new DocumentThumbnailService(mutipleFileSpecificThumbnailCreators,mockUnsupportedThumbnailService);
    }

    @Test
    public void returnImageThumbnail() throws IOException {

        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);
        InputStream expectedInputStream = new ByteArrayInputStream(new byte[]{0});

        when(documentContentVersion.getMimeType())
            .thenReturn(MediaType.IMAGE_JPEG_VALUE);

        when(mockFileSpecificThumbnailCreator1.supports(MediaType.IMAGE_JPEG_VALUE))
            .thenReturn(true);

        when(mockFileSpecificThumbnailCreator1.getThumbnail(documentContentVersion))
            .thenReturn(expectedInputStream);

        Resource generateThumbnail = singleDocumentThumbnailService.generateThumbnail(documentContentVersion);

        Assert.assertThat(generateThumbnail.getInputStream(), equalTo(expectedInputStream));
    }

    @Test
    public void returnPdfThumbnail() throws IOException {

        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);
        InputStream expectedInputStream = new ByteArrayInputStream(new byte[]{0});

        when(documentContentVersion.getMimeType())
            .thenReturn(MediaType.APPLICATION_PDF_VALUE);

        when(mockFileSpecificThumbnailCreator1.supports(MediaType.APPLICATION_PDF_VALUE))
            .thenReturn(true);

        when(mockFileSpecificThumbnailCreator1.getThumbnail(documentContentVersion))
            .thenReturn(expectedInputStream);


        Resource generateThumbnail = singleDocumentThumbnailService.generateThumbnail(documentContentVersion);

        Assert.assertThat(generateThumbnail.getInputStream(), equalTo(expectedInputStream));
    }

    @Test
    public void returnUnsupportedThumbnail() throws IOException {
        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);
        InputStream expectedInputStream = new ByteArrayInputStream(new byte[]{0});

        when(documentContentVersion.getMimeType())
            .thenReturn(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        when(mockFileSpecificThumbnailCreator1.supports(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .thenReturn(false);

        when(mockUnsupportedThumbnailService.getThumbnail(documentContentVersion))
            .thenReturn(expectedInputStream);

        Resource generateThumbnail = singleDocumentThumbnailService.generateThumbnail(documentContentVersion);

        Assert.assertThat(generateThumbnail.getInputStream(), equalTo(expectedInputStream));
    }



    @Test
    public void returnMultipleThumbnail() throws IOException {

        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);
        InputStream expectedInputStream = new ByteArrayInputStream(new byte[]{0});
        InputStream notexpectedInputStream = new ByteArrayInputStream(new byte[]{1});

        when(documentContentVersion.getMimeType())
            .thenReturn(MediaType.APPLICATION_PDF_VALUE);

        when(mockFileSpecificThumbnailCreator1.supports(MediaType.APPLICATION_PDF_VALUE))
            .thenReturn(true);

        when(mockFileSpecificThumbnailCreator2.supports(MediaType.APPLICATION_PDF_VALUE))
            .thenReturn(false);

        when(mockFileSpecificThumbnailCreator1.getThumbnail(documentContentVersion))
            .thenReturn(expectedInputStream);

        when(mockFileSpecificThumbnailCreator2.getThumbnail(documentContentVersion))
            .thenReturn(notexpectedInputStream);

        Resource generateThumbnail = multiDocumentThumbnailService.generateThumbnail(documentContentVersion);

        Assert.assertThat(generateThumbnail.getInputStream(), equalTo(expectedInputStream));
    }

}
