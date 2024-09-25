package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.config.ToggleConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordVerifierTest {

    @Mock
    private ToggleConfiguration toggleConfiguration;

    PasswordVerifier passwordVerifier;

    @BeforeEach
    void init() {
        passwordVerifier = new PasswordVerifier(toggleConfiguration);
    }

    @DisplayName("""
        Test passwordVerifier with all accepted mime times for non-password protected files
            and expect success""")
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

    void testPasswordVerifier_parameterized_success(String filename, String mimetype) {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        MultipartFile file =
            new MockMultipartFile("files", filename, mimetype, "hello".getBytes(StandardCharsets.UTF_8));

        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @Test
    @DisplayName("Test passwordVerifier to check toggle turned off and expect success")
    public void testToggleConfiguration() {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(false);
        MultipartFile file = Mockito.mock(MockMultipartFile.class);
        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @Test
    @DisplayName("Test passwordVerifier for encrypted file and expect success")
    public void testPasswordVerifier_encrypted_file_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/passwordencryptedprotected.pdf").getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", "passwordencryptedprotected.pdf",
            "application/pdf", inputStream);
        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @Test
    @DisplayName("Test passwordVerifier to throw IOException and expect success")
    public void testInputException() throws Exception {
        MultipartFile file = Mockito.mock(MockMultipartFile.class);
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IOException("x"));
        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @DisplayName("Test passwordVerifier with password protected docx/pdf file and expect failure")
    @ParameterizedTest
    @CsvSource({
        "files/pw_protected_docx.docx, sample.docx,"
            + "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "files/pw_protected.pdf, sample.pdf, application/pdf",
    })
    void testPasswordVerifier_docx_pdf_failure(String filePath, String fileName, String mimetype) throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource(filePath).getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName, mimetype, inputStream);

        assertFalse(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

}
