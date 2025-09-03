package uk.gov.hmcts.dm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileVerificationResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultipartFileWhiteListValidatorTests {

    @Mock
    private FileContentVerifier fileContentVerifier;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes attributes;

    @InjectMocks
    private MultipartFileWhiteListValidator multipartFileWhiteListValidator;

    @BeforeEach
    void setUp() {
        lenient().when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void testValidFile() {
        MultipartFile file = mock(MultipartFile.class);
        FileVerificationResult successResult = new FileVerificationResult(true, "application/pdf");
        when(fileContentVerifier.verifyContentType(file)).thenReturn(successResult);

        boolean isValid = multipartFileWhiteListValidator.isValid(file, null);

        assertTrue(isValid, "Validation should pass for a valid file");
        verify(request).setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, successResult);
    }

    @Test
    void testInvalidFile() {
        MultipartFile file = mock(MultipartFile.class);
        FileVerificationResult failureResult = new FileVerificationResult(false, "application/zip");
        when(fileContentVerifier.verifyContentType(file)).thenReturn(failureResult);

        boolean isValid = multipartFileWhiteListValidator.isValid(file, null);

        assertFalse(isValid, "Validation should fail for an invalid file");
        verify(request).setAttribute(MultipartFileWhiteListValidator.VERIFICATION_RESULT_KEY, failureResult);
    }

    @Test
    void testIsValidWhenRequestAttributesAreNull() {
        RequestContextHolder.setRequestAttributes(null);

        MultipartFile file = mock(MultipartFile.class);
        FileVerificationResult successResult = new FileVerificationResult(true, "application/pdf");
        when(fileContentVerifier.verifyContentType(file)).thenReturn(successResult);

        boolean isValid = multipartFileWhiteListValidator.isValid(file, null);

        assertTrue(isValid, "Validation should still succeed based on the file's content");

        verify(fileContentVerifier).verifyContentType(file);

        verifyNoInteractions(request);
    }

}
