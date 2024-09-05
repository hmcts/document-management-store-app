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

    private static final String OPENXML_DOC =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String OPENXML_DOTX =
        "filename.dotx, application/vnd.openxmlformats-officedocument.wordprocessingml.template";
    private static final String OPENXML_SHEET =
        "filename.xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String OPENXML_SHEET_TEMPLATE =
        "filename.xltx, application/vnd.openxmlformats-officedocument.spreadsheetml.template";
    private static final String OPENXML_PRESENTATION =
        "filename.pptx, application/vnd.openxmlformats-officedocument.presentationml.presentation";
    private static final String OPENXML_PRESENTATION_TEMPLATE =
        "filename.potx, application/vnd.openxmlformats-officedocument.presentationml.template";
    private static final String OPENXML_PRESENTATION_SLIDESHOW =
        "filename.ppsx, application/vnd.openxmlformats-officedocument.presentationml.slideshow";

    @Mock
    private ToggleConfiguration toggleConfiguration;

    PasswordVerifier passwordVerifier;

    @BeforeEach
    void init() {
        passwordVerifier = new PasswordVerifier(toggleConfiguration);
    }

    @DisplayName("Test successful Password Verifier with all accepted mime types")
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

    @DisplayName("Test passwordVerifier with pdf file and expect success")
    @Test
    void testPasswordVerifier_pdf_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/test.pdf").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.pdf", "application/pdf", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with docx file and expect success")
    @Test
    void testPasswordVerifier_docx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/test.docx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.docx", OPENXML_DOC, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with docx file and expect failure")
    @Test
    void testPasswordVerifier_docx_failure() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/pw_protected_docx.docx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.docx", OPENXML_DOC, inputStream);

        assertFalse(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with dotx file and expect success")
    @Test
    void testPasswordVerifier_dotx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.dotx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.dotx", OPENXML_DOTX, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with pptx file and expect success")
    @Test
    void testPasswordVerifier_pptx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.pptx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.dotx", OPENXML_PRESENTATION, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with potx file and expect success")
    @Test
    void testPasswordVerifier_potx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.potx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.dotx", OPENXML_PRESENTATION_TEMPLATE, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with ppsx file and expect success")
    @Test
    void testPasswordVerifier_ppsx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.ppsx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.dotx", OPENXML_PRESENTATION_SLIDESHOW, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with xlsx file and expect success")
    @Test
    void testPasswordVerifier_xlsx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.xlsx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.xlsx", OPENXML_SHEET, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with xltx file and expect success")
    @Test
    void testPasswordVerifier_xltx_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.xltx").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.xltx", OPENXML_SHEET_TEMPLATE, inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with jpeg file and expect success")
    @Test
    void testPasswordVerifier_jpeg_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.jpg").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.jpg", "application/jpeg", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with png file and expect success")
    @Test
    void testPasswordVerifier_png_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.png").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.png", "application/png", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with tif file and expect success")
    @Test
    void testPasswordVerifier_tif_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.tif").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.tif", "image/tiff", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with bmp file and expect success")
    @Test
    void testPasswordVerifier_bmp_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.bmp").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.bmp", "image/bmp", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with mp3 file and expect success")
    @Test
    void testPasswordVerifier_mp3_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/audio_test.mp3").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.mp3", "audio/mpeg", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with mp4 file and expect success")
    @Test
    void testPasswordVerifier_mp4_video_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.mp4").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.mp4", "video/mp4", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with mp4 audio file and expect success")
    @Test
    void testPasswordVerifier_mp4_audio_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.mp4").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.mp4", "audio/mp4", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with txt file and expect success")
    @Test
    void testPasswordVerifier_txt_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.txt").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.txt", "text/plain", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with rtf file and expect success")
    @Test
    void testPasswordVerifier_rtf_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.rtf").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file", "sample.rtf", "application/rtf", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @DisplayName("Test passwordVerifier with csv file and expect success")
    @Test
    void testPasswordVerifier_csv_success() throws IOException {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        InputStream inputStream = new ClassPathResource("files/file.csv").getInputStream();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "sample.csv", "text/csv", inputStream);

        assertTrue(passwordVerifier.checkPasswordProtectedFile(mockMultipartFile));
    }

    @Test
    @DisplayName("Test passwordVerifier to throw IOException and expect success")
    public void testInputException() throws Exception {
        MultipartFile file = Mockito.mock(MockMultipartFile.class);
        when(toggleConfiguration.isPasswordcheck()).thenReturn(true);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IOException("x"));
        assertFalse(passwordVerifier.checkPasswordProtectedFile(file));
    }

    @Test
    @DisplayName("Test passwordVerifier to check toggle turned off and expect success")
    public void testToggleConfiguration() throws Exception {
        when(toggleConfiguration.isPasswordcheck()).thenReturn(false);
        MultipartFile file = Mockito.mock(MockMultipartFile.class);
        assertTrue(passwordVerifier.checkPasswordProtectedFile(file));
    }
}
