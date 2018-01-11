package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Test;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ImageResizeServiceTest {

    @Test
    public void shouldResizeJpegImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("img/evidence-management.jpg").getFile());

        InputStream image = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = ImageResizeService.resizeImage(image);
        File outputfile = new File("evidence-management-resize.jpg");
        ImageIO.write(resizedImage, "jpg", outputfile);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test
    public void shouldResizePngImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("img/document-png-example.png").getFile());

        InputStream image = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = ImageResizeService.resizeImage(image);
        File outputfile = new File("document-png-example-resize.jpg");
        ImageIO.write(resizedImage, "jpg", outputfile);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
        assertThat(resizedImage.getHeight(), equalTo(256));
    }

    @Test
    public void shouldSupportJpeg() {
        assertTrue(new ImageResizeService().supports(MediaType.IMAGE_JPEG_VALUE));
    }
    @Test
    public void shouldSupportPng() {
        assertTrue(new ImageResizeService().supports(MediaType.IMAGE_PNG_VALUE));
    }
    @Test
    public void shouldSupportGif() {
        assertTrue(new ImageResizeService().supports(MediaType.IMAGE_GIF_VALUE));
    }
    @Test
    public void shouldNotSupportPDF() {
        assertFalse(new ImageResizeService().supports(MediaType.APPLICATION_PDF_VALUE));
    }
}
