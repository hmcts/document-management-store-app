package uk.gov.hmcts.dm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class MultipartFileWhiteListValidatorTests {

    @Mock
    FileContentVerifier fileContentVerifier;

    @InjectMocks
    private MultipartFileWhiteListValidator multipartFileWhiteListValidator;

    @Test
    void testSuccess() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(fileContentVerifier.verifyContentType(file)).thenReturn(true);
        assertTrue(multipartFileWhiteListValidator.isValid(file, null));
    }

    @Test
    void testFailure() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(fileContentVerifier.verifyContentType(file)).thenReturn(false);
        assertFalse(multipartFileWhiteListValidator.isValid(file, null));
    }



}
