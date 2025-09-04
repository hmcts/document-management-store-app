package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FileContentVerifierTests {

    private final FileContentVerifier fileContentVerifier = new FileContentVerifier(
        List.of(
            "text/plain",
            "text/csv",
            "image/gif",
            "image/tiff",
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
        ),
        List.of(
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
    void testUploadDocumentsSuccess() {
        MultipartFile file = new MockMultipartFile("files", "filename.txt",
            "text/plain", "hello".getBytes(StandardCharsets.UTF_8));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertTrue(result.isValid());
        assertEquals("text/plain", result.getDetectedMimeType().orElse(null));
    }

    @Test
    void testUploadDifferentDocumentsTypesSuccess() throws IOException {
        MultipartFile file = new MockMultipartFile("files", "filename.pdf",
            "application/pdf", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertTrue(result.isValid());
        assertEquals("application/pdf", result.getDetectedMimeType().orElse(null));
    }

    @Test
    void testInputException() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("test.pdf");
        Mockito.when(file.getInputStream()).thenThrow(new IOException("x"));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertFalse(result.isValid());
        assertTrue(result.getDetectedMimeType().isEmpty(), "Mime type should be empty on IO exception");
    }

    @Test
    void testNull() {
        FileVerificationResult result = fileContentVerifier.verifyContentType(null);

        assertFalse(result.isValid());
    }

    @Test
    void testUploadMimeTypeNotAllowed() {
        MultipartFile file =  new MockMultipartFile("file", "filename.xml",
            "application/xml", "hello".getBytes(StandardCharsets.UTF_8));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertFalse(result.isValid());
    }

    @Test
    void testUploadMimeTypeNotAllowedWithAllowedExtension() {
        MultipartFile file =  new MockMultipartFile("file", "filename.txt",
            "application/xml", "<xml>hello</xml>".getBytes(StandardCharsets.UTF_8));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertFalse(result.isValid());
        assertEquals("application/xml", result.getDetectedMimeType().orElse(null),
            "Should return the detected mime type even on failure");
    }

    @Test
    void testIgnoreClientMimeType() throws IOException {
        MultipartFile file = new MockMultipartFile("files", "filename.pdf",
            "some-incorrect-mime-type", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertTrue(result.isValid());
        assertEquals("application/pdf", result.getDetectedMimeType().orElse(null));
    }

    @Test
    void testFailureForDisallowedExt() throws Exception {
        MultipartFile file = new MockMultipartFile("files", "filename.dat",
            "application/pdf", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertFalse(result.isValid());
        assertTrue(result.getDetectedMimeType().isEmpty(), "Mime type should not be detected if extension fails");
    }

    @Test
    void testFailureForNoExt() throws Exception {
        MultipartFile file = new MockMultipartFile("files", "filename",
            "application/pdf", getClass().getClassLoader().getResourceAsStream(EXAMPLE_PDF_FILE));

        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertFalse(result.isValid());
    }

    @Test
    void testEmptyFileNameException() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn(null);


        FileVerificationResult result = fileContentVerifier.verifyContentType(file);

        assertFalse(result.isValid());
    }
}
