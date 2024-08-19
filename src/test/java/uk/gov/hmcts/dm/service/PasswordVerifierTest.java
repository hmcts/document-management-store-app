package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;


@RunWith(SpringRunner.class)
class PasswordVerifierTest {

    @InjectMocks
    PasswordVerifier passwordVerifier;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void verifyPassword_PDF() {
        MultipartFile file =
            new MockMultipartFile("files", "file.pdf", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8));

        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @Test
    void verifyPassword_CSV() {
        MultipartFile file =
            new MockMultipartFile("files", "file.csv", "text/csv", "hello".getBytes(StandardCharsets.UTF_8));

        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @ParameterizedTest
    @CsvSource({
        "filename.pdf, application/pdf",
        "filename.xls, application/vnd.ms-excel",
        "filename.docx, application/msword",
        "filename.pptx, application/vnd.ms-powerpoint",
        "filename.txt, text/plain",
        "filename.csv, text/csv",
        "filename.jpg, image/jpeg",
        "filename.tiff, image/tiff",
        "filename.png, image/png",
        "filename.bmp, image/bmp",
        "filename.mp4, audio/mp4",
        "filename.mp3, audio/mpeg",
        "filename.rtf, application/rtf",
        "filename.docx, application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "filename.dotx, application/vnd.openxmlformats-officedocument.wordprocessingml.template",
        "filename.pptx, application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "filename.potx, application/vnd.openxmlformats-officedocument.presentationml.template",
        "filename.ppsx, application/vnd.openxmlformats-officedocument.presentationml.slideshow",
        "filename.xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "filename.xltx, application/vnd.openxmlformats-officedocument.spreadsheetml.template"
    })
    void testPasswordVerifierSuccess_parameterized(String filename, String mimetype) {
        MultipartFile file =
            new MockMultipartFile("files", filename, mimetype, "hello".getBytes(StandardCharsets.UTF_8));

        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }
}
