package uk.gov.hmcts.dm.service;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileContentVerifierTests {

    private final FileContentVerifier fileContentVerifier = new FileContentVerifier(
        Arrays.asList(
            "text/plain",
            "text/csv",
            "image/gif",
            "image/tiff",
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
        ),
        Arrays.asList(
            ".txt",
            ".csv",
            ".gif",
            ".tiff",
            ".jpeg",
            ".png",
            ".webp",
            ".pdf"
        )
    );

    private static final String EXAMPLE_PDF_FILE = "files/1MB.pdf";

    @Test
    public void testUploadDocumentsSuccess() {
        MultipartFile file = new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
        boolean b = fileContentVerifier.verifyContentType(file);
        assertTrue(b);
    }

    @Test
    public void testUploadDifferentDocumentsTypesSuccess() throws Exception {
        MultipartFile file = new MockMultipartFile("files", "filename.txt", "application/pdf", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));
        boolean b = fileContentVerifier.verifyContentType(file);
        assertTrue(b);
    }

    @Test
    public void testInputException() throws Exception {
        MultipartFile file = Mockito.mock(MockMultipartFile.class);
        Mockito.when(file.getContentType()).thenReturn("application/pdf");
        Mockito.when(file.getOriginalFilename()).thenReturn("test.pdf");
        Mockito.when(file.getInputStream()).thenThrow(new IOException("x"));
        assertFalse(fileContentVerifier.verifyContentType(file));
    }

    @Test
    public void testNull() throws Exception {
        assertFalse(fileContentVerifier.verifyContentType(null));
    }

    @Test
    public void testUploadMimeTypeNotAllowed() {
        MultipartFile file =  new MockMultipartFile("file", "filename.xml", "application/xml", "hello".getBytes(StandardCharsets.UTF_8));
        assertFalse(fileContentVerifier.verifyContentType(file));
    }

    @Test
    public void testUploadMimeTypeNotAllowedWithAllowedExtension() {
        MultipartFile file =  new MockMultipartFile("file", "filename.txt", "application/xml", "hello".getBytes(StandardCharsets.UTF_8));
        assertFalse(fileContentVerifier.verifyContentType(file));
    }

    @Test
    public void testIgnoreClientMimeType() throws Exception {
        MultipartFile file = new MockMultipartFile("files", "filename.txt", "tex", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));
        assertTrue(fileContentVerifier.verifyContentType(file));
    }

    @Test
    public void testFailureForDisallowedExt() throws Exception {
        MultipartFile file = new MockMultipartFile("files", "filename.dat", "application/pdf", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));
        assertFalse(fileContentVerifier.verifyContentType(file));
    }

    @Test
    public void testFailureForNoExt() throws Exception {
        MultipartFile file = new MockMultipartFile("files", "filename", "application/pdf", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));
        assertFalse(fileContentVerifier.verifyContentType(file));
    }

    @Test
    public void testEmptyFileNameException() throws Exception {
        MultipartFile file = Mockito.mock(MockMultipartFile.class);
        Mockito.when(file.getContentType()).thenReturn("application/pdf");
        Mockito.when(file.getOriginalFilename()).thenReturn(null);
        assertFalse(fileContentVerifier.verifyContentType(file));
    }

}
