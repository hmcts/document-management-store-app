package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class PdfThumbnailServiceTest {

    private PdfThumbnailService pdfThumbnailService;

    @Before
    public void setUp() throws Exception {
        pdfThumbnailService = new PdfThumbnailService();
    }

    @Test
    public void shouldNotSupportJpeg() {
        assertFalse(pdfThumbnailService.supports(MediaType.IMAGE_JPEG_VALUE));
    }
    @Test
    public void shouldNotSupportPng() {
        assertFalse(pdfThumbnailService.supports(MediaType.IMAGE_PNG_VALUE));
    }
    @Test
    public void shouldNotSupportGif() {
        assertFalse(pdfThumbnailService.supports(MediaType.IMAGE_GIF_VALUE));
    }
    @Test
    public void shouldSupportPDF() {
        assertTrue(pdfThumbnailService.supports(MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    public void getPdfThumbnail() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/1MB.pdf").getFile());

        InputStream pdf = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = pdfThumbnailService.getImg(pdf);
        File outputFile = new File("pdf-test-thumbnail.jpg");
        ImageIO.write(resizedImage, "jpg", outputFile);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
//        assertThat(resizedImage.getHeight(), equalTo(194));
    }

    @Test(expected = RuntimeException.class)
    public void getPdfThumbnailNoPages() {
        InputStream pdf = new ByteArrayInputStream(new byte[]{0});
        pdfThumbnailService.getImg(pdf);
    }


    @Test(expected = RuntimeException.class)
    public void getPdfThumbnailWrongFileType() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/document-png-example.png").getFile());
        InputStream pdf = Files.newInputStream(null);
        pdfThumbnailService.getImg(pdf);
    }


}
