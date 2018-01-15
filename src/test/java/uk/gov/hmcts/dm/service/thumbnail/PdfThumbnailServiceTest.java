package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class PdfThumbnailServiceTest {

    @Autowired
    ImageResizeService imageResizeService;

    @Test
    public void shouldNotSupportJpeg() {
        assertFalse(new PdfThumbnailService(imageResizeService).supports(MediaType.IMAGE_JPEG_VALUE));
    }
    @Test
    public void shouldNotSupportPng() {
        assertFalse(new PdfThumbnailService(imageResizeService).supports(MediaType.IMAGE_PNG_VALUE));
    }
    @Test
    public void shouldNotSupportGif() {
        assertFalse(new PdfThumbnailService(imageResizeService).supports(MediaType.IMAGE_GIF_VALUE));
    }
    @Test
    public void shouldSupportPDF() {
        assertTrue(new PdfThumbnailService(imageResizeService).supports(MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    public void getPdfThumbnail() throws IOException {
        PdfThumbnailService pdfThumbnailService = new PdfThumbnailService(imageResizeService);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("img/1MB.pdf").getFile());

        InputStream pdf = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = PdfThumbnailService.getPdfImage(pdf);
        File outputfile = new File("pdf-test-thumbnail.jpg");
        ImageIO.write(resizedImage, "jpg", outputfile);

        assertThat(resizedImage.getWidth(), equalTo(ImageResizeService.DEFAULT_WIDTH));
//        assertThat(resizedImage.getHeight(), equalTo(194));
    }


}
