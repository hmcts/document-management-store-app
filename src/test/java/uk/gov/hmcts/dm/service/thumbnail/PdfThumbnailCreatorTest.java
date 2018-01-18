package uk.gov.hmcts.dm.service.thumbnail;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static uk.gov.hmcts.dm.service.thumbnail.TestResource.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class PdfThumbnailCreatorTest {

    private PdfThumbnailCreator pdfThumbnailService;

    @Before
    public void setUp() {
        pdfThumbnailService = new PdfThumbnailCreator();
    }

    @Test
    public void getPdfThumbnail() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE);

        BufferedImage resizedImage = pdfThumbnailService.getImg(file);

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
    public void getPdfThumbnailWrongFileType() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_JPG_FILE);
        try {
            pdfThumbnailService.getImg(file);
        } catch (CantCreateThumbnailException e) {
            assertTrue(e.getMessage(),true);
            return;
        }
        fail();
    }

}
