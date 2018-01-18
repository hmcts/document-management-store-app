package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedThumbnailCreatorTest {

    private UnsupportedThumbnailCreator unsupportedThumbnailCreator;

    @Before
    public void setUp() {
        unsupportedThumbnailCreator = new UnsupportedThumbnailCreator();
    }

    @Test
    public void getThumbnail() throws IOException {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        UnsupportedThumbnailCreator unsupportedThumbnailService = new UnsupportedThumbnailCreator();
        InputStream thumbnail = unsupportedThumbnailService.getThumbnail(documentContentVersion);

        InputStream expectInputStream = getClass().getResourceAsStream(UnsupportedThumbnailCreator.DEFAULT_FILE_THUMBNAIL);
        assertTrue(IOUtils.contentEquals(thumbnail,expectInputStream));
    }

    @Test
    public void shouldSupportTxt() {
        assertTrue(unsupportedThumbnailCreator.supports(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void shouldSupportJpeg() {
        assertTrue(unsupportedThumbnailCreator.supports(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void shouldSupportPng() {
        assertTrue(unsupportedThumbnailCreator.supports(MediaType.IMAGE_PNG_VALUE));
    }

    @Test
    public void shouldSupportGif() {
        assertTrue(unsupportedThumbnailCreator.supports(MediaType.IMAGE_GIF_VALUE));
    }

    @Test
    public void shouldSupportPdf() {
        assertTrue(unsupportedThumbnailCreator.supports(MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    public void shouldSupportJpgDocumentContent() {
        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);

        when(documentContentVersion.getMimeType()).thenReturn(MediaType.IMAGE_JPEG_VALUE);

        assertTrue(unsupportedThumbnailCreator.supports(documentContentVersion));
    }
    
    @Test
    public void shouldSupportPngDocumentContent() {
        DocumentContentVersion documentContentVersion = Mockito.mock(DocumentContentVersion.class);

        when(documentContentVersion.getMimeType()).thenReturn(MediaType.IMAGE_PNG_VALUE);

        assertTrue(unsupportedThumbnailCreator.supports(documentContentVersion));
    }

}
