package uk.gov.hmcts.dm.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.PasswordVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class MultipartListPasswordValidatorTest {
    @Mock
    PasswordVerifier passwordVerifier;

    @InjectMocks
    private MultipartFilePasswordValidator multipartFilePasswordValidator;

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
    void testPasswordValidatorSuccess_parameterized(String file, String mimetype) {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", file, mimetype, "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(passwordVerifier.checkPasswordProtectedFile(any(MultipartFile.class))).thenReturn(true);

        Assertions.assertTrue(multipartFilePasswordValidator.isValid(files, null));
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
    void testPasswordValidatorFailure_parameterized(String file, String mimetype) {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", file, mimetype, "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(passwordVerifier.checkPasswordProtectedFile(any())).thenReturn(false);

        Assertions.assertFalse(multipartFilePasswordValidator.isValid(files, null));
    }

    @Test
    void testPasswordValidatorFailure_pdf() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.pdf", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(passwordVerifier.checkPasswordProtectedFile(any(MultipartFile.class))).thenReturn(false);

        Assertions.assertFalse(multipartFilePasswordValidator.isValid(files, null));
    }

    @Test
    void testPasswordValidatorFailure_csv() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.csv", "text/csv", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(passwordVerifier.checkPasswordProtectedFile(any(MultipartFile.class))).thenReturn(false);

        Assertions.assertFalse(multipartFilePasswordValidator.isValid(files, null));
    }
}