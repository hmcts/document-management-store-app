package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ImageResizeServiceTest {

    private ImageResizeService imageResizeService;

    @Before
    public void setUp() {
        imageResizeService = new ImageResizeService();
    }

    @Test
    public void shouldResizeJpegImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/document-jpg-example.jpg").getFile());
        InputStream image = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = imageResizeService.getImg(image);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizeGifImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/document-gif-example.gif").getFile());
        InputStream image = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = imageResizeService.getImg(image);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizeAnimatedGifImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        // Taken from http://www.adamgrimshaw.com/gifs/turn_turn_turn.gif
        File file = new File(classLoader.getResource("files/document-gif-animated-example.gif").getFile());
        InputStream image = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = imageResizeService.getImg(image);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(204));
    }

    @Test
    public void shouldResizePngImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/document-png-example.png").getFile());
        InputStream image = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = imageResizeService.getImg(image);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(256));
    }

    @Test
    public void shouldThrowExceptionOnResizePdf() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/1MB.pdf").getFile());
        InputStream image = Files.newInputStream(file.toPath());

        try {
            imageResizeService.getImg(image);
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
        InputStream nullInputStream =  new ByteArrayInputStream(new byte[]{0});

        try {
            imageResizeService.getImg(nullInputStream);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

    @Test
    public void shouldSupportJpeg() {
        assertTrue(imageResizeService.supports(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    public void shouldSupportPng() {
        assertTrue(imageResizeService.supports(MediaType.IMAGE_PNG_VALUE));
    }

    @Test
    public void shouldSupportGif() {
        assertTrue(imageResizeService.supports(MediaType.IMAGE_GIF_VALUE));
    }

    @Test
    public void shouldNotSupportPdf() {
        assertFalse(imageResizeService.supports(MediaType.APPLICATION_PDF_VALUE));
    }
}
