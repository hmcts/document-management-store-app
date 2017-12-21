package uk.gov.hmcts.reform.dm.security;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultipartFileWhiteValidatorTest {

    private final MultipartFileWhiteListValidator fileWhiteListValidator = new MultipartFileWhiteListValidator(
            Arrays.asList(
                    "text/plain",
                    "text/csv",
                    "image/gif",
                    "image/tiff",
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "application/pdf"
            )
    );

    //    Success scenarios
    @Test
    public void testUploadDocumentsSuccess() {
        MultipartFile file = new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
        boolean b = fileWhiteListValidator.isValid(file, null);
        assertTrue(b);
    }

    @Test
    public void testUploadDifferentDocumentsTypesSuccess() {
        MultipartFile file = new MockMultipartFile("files", "filename.txt", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8));
        boolean b = fileWhiteListValidator.isValid(file, null);
        assertTrue(b);
    }

    @Test
    public void testUploadNoDocumentsSuccess() {
        boolean b = fileWhiteListValidator.isValid(null, null);

        assertTrue(b);
    }

    //    Fail scenarios
    @Test
    public void testUploadDocumentsFail() {
        MultipartFile file =  new MockMultipartFile("filse", "filename.txt", "text/html", "hello".getBytes(StandardCharsets.UTF_8));
        boolean b = fileWhiteListValidator.isValid(file, null);
        assertFalse(b);
    }

    @Test
    public void testUploadDocumentsMalformedFail() {
        MultipartFile file = new MockMultipartFile("files", "filename.txt", "tex", "hello".getBytes(StandardCharsets.UTF_8));
        boolean b = fileWhiteListValidator.isValid(file, null);
        assertFalse(b);
    }

}
