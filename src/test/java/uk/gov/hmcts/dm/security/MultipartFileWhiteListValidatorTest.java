package uk.gov.hmcts.dm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
class MultipartFileWhiteListValidatorTest {

    @Mock
    FileContentVerifier fileContentVerifier;

    @InjectMocks
    private MultipartFileListWhiteListValidator multipartFileListWhiteListValidator;

    @Test
    void testUploadDocumentsSuccess() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(fileContentVerifier.verifyContentType(any(MultipartFile.class))).thenReturn(true);

        assertTrue(multipartFileListWhiteListValidator.isValid(files, null));
    }

    @Test
    void testUploadDocumentsFail() {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(fileContentVerifier.verifyContentType(any())).thenReturn(false);

        assertFalse(multipartFileListWhiteListValidator.isValid(files, null));
    }

}
