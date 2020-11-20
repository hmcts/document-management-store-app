package uk.gov.hmcts.dm.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class MultipartFileWhiteListValidatorTest {

    @Mock
    FileContentVerifier fileContentVerifier;

    @InjectMocks
    private MultipartFileListWhiteListValidator multipartFileListWhiteListValidator;

    @Test
    public void testUploadDocumentsSuccess() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(fileContentVerifier.verifyContentType(ArgumentMatchers.any())).thenReturn(true);

        assertTrue(multipartFileListWhiteListValidator.isValid(files, null));
    }

    @Test
    public void testUploadDocumentsFail() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(fileContentVerifier.verifyContentType(ArgumentMatchers.any())).thenReturn(false);

        assertFalse(multipartFileListWhiteListValidator.isValid(files, null));
    }

}
