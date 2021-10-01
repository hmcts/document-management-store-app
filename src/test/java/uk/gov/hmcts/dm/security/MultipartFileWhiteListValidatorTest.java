package uk.gov.hmcts.dm.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
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

        Mockito.when(fileContentVerifier.verifyContentType(Matchers.any())).thenReturn(true);

        assertTrue(multipartFileListWhiteListValidator.isValid(files, null));
    }

    @Test
    public void testUploadDocumentsFail() throws Exception {
        List<MultipartFile> files = Stream.of(
            new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))
        ).collect(Collectors.toList());

        Mockito.when(fileContentVerifier.verifyContentType(Matchers.any())).thenReturn(false);

        assertFalse(multipartFileListWhiteListValidator.isValid(files, null));
    }

}
