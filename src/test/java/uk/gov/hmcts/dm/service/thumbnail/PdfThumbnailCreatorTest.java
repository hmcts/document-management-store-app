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
import java.sql.Blob;

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

public class PdfThumbnailCreatorTest {

    private static final String EXAMPLE_JPG_FILE = "files/document-jpg-example.jpg";
    private static final String EXAMPLE_PDF_FILE = "files/1MB.pdf";
    private static final String CONTENT_URI = "123";

    @Mock
    private DocumentContentVersion contentVersion;

    @Mock
    private Blob blob;

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @InjectMocks
    private PdfThumbnailCreator pdfThumbnailService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getPdfThumbnail() {
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE);

        BufferedImage resizedImage = pdfThumbnailService.getImg(file);

        assertThat(resizedImage.getWidth(), equalTo(ThumbnailWidths.WIDTH_256.getWidth()));
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

    @Test
    public void shouldBuildThumbnailFromAzure() throws IOException {
        when(contentVersion.getContentUri()).thenReturn(CONTENT_URI);
        InputStream file = getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE);
        assertNotNull(file);
        doAnswer(invocation -> {
            final OutputStream out = invocation.getArgument(1);
            IOUtils.copy(file, out);
            out.close();
            return null;
        })
               .when(blobStorageReadService)
               .loadFullBlob(same(contentVersion), Mockito.any(OutputStream.class));

        final InputStream thumbnail = pdfThumbnailService.getThumbnail(contentVersion);

        assertThat(thumbnail, is(notNullValue()));
        verify(blobStorageReadService).loadFullBlob(same(contentVersion), Mockito.any(OutputStream.class));
    }

}
