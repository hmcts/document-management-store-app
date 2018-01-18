package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static uk.gov.hmcts.dm.service.thumbnail.TestResource.*;

public class ImageThumbnailCreatorTest {

    private ImageThumbnailCreator imageResizeService;


    @Before
    public void setUp() {
        imageResizeService = new ImageThumbnailCreator();
    }

    @Test
    public void shouldResizeJpegImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_JPG_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ImageThumbnailCreator.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizeGifImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_GIF_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ImageThumbnailCreator.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizeAnimatedGifImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_GIF_ANI_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ImageThumbnailCreator.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(204));
    }

    @Test
    public void shouldResizePngImage() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PNG_FILE);

        BufferedImage resizedImage = imageResizeService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ImageThumbnailCreator.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(256));
    }

    @Test
    public void shouldThrowExceptionOnResizePdf() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE);

        try {
            imageResizeService.getImg(file);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

    @Test
    public void shouldThrowExceptionOnNull() {
        try {
            imageResizeService.getImg(null);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

    @Test
    public void shouldThrowExceptionOnEmptyStream() {
        try {
            imageResizeService.getImg(new ByteArrayInputStream(new byte[]{0}));
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

}
