package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImageThumbnailCreatorTest {

    private static final String EXAMPLE_JPG_FILE = "files/document-jpg-example.jpg";
    private static final String EXAMPLE_GIF_FILE = "files/document-gif-example.gif";
    // Taken from http://www.adamgrimshaw.com/gifs/turn_turn_turn.gif
    private static final String EXAMPLE_GIF_ANI_FILE = "files/document-gif-animated-example.gif";
    private static final String EXAMPLE_PNG_FILE = "files/document-png-example.png";
    private static final String EXAMPLE_PDF_FILE = "files/1MB.pdf";
    private static final String CONTENT_URI = "123";

    @Mock
    private DocumentContentVersion contentVersion;

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @InjectMocks
    private ImageThumbnailCreator imageResizeService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldResizeJpegImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_JPG_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ThumbnailWidths.WIDTH_256.getWidth()));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizeGifImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_GIF_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ThumbnailWidths.WIDTH_256.getWidth()));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizeAnimatedGifImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_GIF_ANI_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ThumbnailWidths.WIDTH_256.getWidth()));
        assertThat(resizedImage.getHeight(), equalTo(204));
    }

    @Test
    public void shouldResizePngImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PNG_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ThumbnailWidths.WIDTH_256.getWidth()));
        assertThat(resizedImage.getHeight(), equalTo(256));
    }

    @Test
    public void shouldThrowExceptionOnResizePdf() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE);

        try {
            imageResizeService.getImg(file);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        fail();
    }

    @Test
    public void shouldThrowExceptionOnNull() {
        try {
            imageResizeService.getImg(null);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        fail();
    }

    @Test
    public void shouldThrowExceptionOnEmptyStream() {
        try {
            imageResizeService.getImg(new ByteArrayInputStream(new byte[]{0}));
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        fail();
    }

    @Test
    public void shouldBuildThumbnailFromAzure() throws IOException {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_JPG_FILE);
        when(contentVersion.getContentUri()).thenReturn(CONTENT_URI);
        assertNotNull(file);
        doAnswer(invocation -> {
            final OutputStream out = invocation.getArgument(1);
            IOUtils.copy(file, out);
            out.close();
            return null;
        })
               .when(blobStorageReadService)
               .loadFullBlob(same(contentVersion), Mockito.any(OutputStream.class));

        final InputStream thumbnail = imageResizeService.getThumbnail(contentVersion);

        assertThat(thumbnail, is(notNullValue()));
        verify(blobStorageReadService).loadFullBlob(same(contentVersion), Mockito.any(OutputStream.class));
    }

}
