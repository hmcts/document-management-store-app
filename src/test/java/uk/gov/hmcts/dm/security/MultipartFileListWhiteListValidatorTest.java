package uk.gov.hmcts.dm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileVerificationResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultipartFileListWhiteListValidatorTest {

    @Mock
    private FileContentVerifier fileContentVerifier;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes attributes;

    @InjectMocks
    private MultipartFileListWhiteListValidator multipartFileListWhiteListValidator;

    @BeforeEach
    void setUp() {
        lenient().when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void testUploadDocumentsSuccess() {
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file1, file2);

        FileVerificationResult successResult1 = new FileVerificationResult(true, "application/pdf");
        FileVerificationResult successResult2 = new FileVerificationResult(true, "image/jpeg");

        when(fileContentVerifier.verifyContentType(file1)).thenReturn(successResult1);
        when(fileContentVerifier.verifyContentType(file2)).thenReturn(successResult2);

        boolean isValid = multipartFileListWhiteListValidator.isValid(files, null);

        assertTrue(isValid, "Validation should pass when all files are valid");

        Map<MultipartFile, FileVerificationResult> expectedMap = Map.of(
            file1, successResult1,
            file2, successResult2
        );

        verify(request).setAttribute(MultipartFileListWhiteListValidator.VERIFICATION_RESULTS_MAP_KEY, expectedMap);
    }

    @Test
    void testUploadDocumentsFail() {
        MultipartFile validFile = mock(MultipartFile.class);
        MultipartFile invalidFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(validFile, invalidFile);

        FileVerificationResult successResult = new FileVerificationResult(true, "application/pdf");
        FileVerificationResult failureResult = new FileVerificationResult(false, "application/zip");

        when(fileContentVerifier.verifyContentType(validFile)).thenReturn(successResult);
        when(fileContentVerifier.verifyContentType(invalidFile)).thenReturn(failureResult);

        boolean isValid = multipartFileListWhiteListValidator.isValid(files, null);

        assertFalse(isValid, "Validation should fail if any file is invalid");

        Map<MultipartFile, FileVerificationResult> expectedMap = Map.of(
            validFile, successResult,
            invalidFile, failureResult
        );

        var mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(request).setAttribute(
            eq(MultipartFileListWhiteListValidator.VERIFICATION_RESULTS_MAP_KEY),
            mapCaptor.capture()
        );
        assertEquals(expectedMap, mapCaptor.getValue());
    }

    @Test
    void testEmptyList() {
        boolean isValid = multipartFileListWhiteListValidator.isValid(Collections.emptyList(), null);

        assertTrue(isValid, "Validation should pass for an empty list");
        verifyNoInteractions(fileContentVerifier);
        verify(request, never()).setAttribute(anyString(), any());
    }

    @Test
    void testIsValidWhenRequestAttributesAreNull() {
        RequestContextHolder.setRequestAttributes(null);

        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);

        FileVerificationResult successResult = new FileVerificationResult(true, "application/pdf");
        when(fileContentVerifier.verifyContentType(file)).thenReturn(successResult);

        boolean isValid = multipartFileListWhiteListValidator.isValid(files, null);

        assertTrue(isValid, "Validation should still succeed based on file content");

        verify(fileContentVerifier).verifyContentType(file);

        verifyNoInteractions(request);
    }

}
