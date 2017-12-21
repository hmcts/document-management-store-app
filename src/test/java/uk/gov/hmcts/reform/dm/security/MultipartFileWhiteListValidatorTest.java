package uk.gov.hmcts.reform.dm.security;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultipartFileWhiteListValidatorTest {

    private final MultipartFileListWhiteListValidator fileWhiteListValidator = new MultipartFileListWhiteListValidator(
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
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertTrue(b);
    }

    @Test
    public void testUploadNoDocumentsSuccess() {
        List<MultipartFile> files = new ArrayList<>();

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertTrue(b);
    }

    @Test
    public void testUploadMultipleDifferentDocumentsTypesSuccess() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertTrue(b);
    }

    @Test
    public void testUploadMultipleDocumentsSuccess() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertTrue(b);
    }

    //    Fail scenarios
    @Test
    public void testUploadDocumentsFail() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/html", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertFalse(b);
    }

    @Test
    public void testUploadDocumentsMalformedFail() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "tex", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertFalse(b);
    }

    @Test
    public void testUploadMultipleDocumentsFail() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
            new MockMultipartFile("files", "filename.txt", "text/html", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        boolean b = fileWhiteListValidator.isValid(files, null);

        assertFalse(b);
    }
}
