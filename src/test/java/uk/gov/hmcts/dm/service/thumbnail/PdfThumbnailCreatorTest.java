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

public class PdfThumbnailCreatorTest {

    private PdfThumbnailCreator pdfThumbnailService;

    @Before
    public void setUp() {
        pdfThumbnailService = new PdfThumbnailCreator();
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
    public void shouldSupportPdf() {
        assertTrue(pdfThumbnailService.supports(MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    public void getPdfThumbnail() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/1MB.pdf").getFile());
        InputStream pdf = Files.newInputStream(file.toPath());

        BufferedImage resizedImage = pdfThumbnailService.getImg(pdf);

        assertThat(resizedImage.getWidth(), equalTo(ImageThumbnailCreator.DEFAULT_WIDTH));
    }

    @Test
    public void getPdfThumbnailNoPages() {
        try {
            pdfThumbnailService.getImg(new ByteArrayInputStream(new byte[]{0}));
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

    @Test
    public void getPdfThumbnailNull() {
        try {
            pdfThumbnailService.getImg(null);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

    @Test
    public void getPdfThumbnailWrongFileType() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/document-png-example.png").getFile());
        InputStream pdf = Files.newInputStream(file.toPath());
        try {
            pdfThumbnailService.getImg(pdf);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

}
