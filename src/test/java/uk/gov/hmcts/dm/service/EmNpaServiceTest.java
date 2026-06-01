package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.client.EmNpaApi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EmNpaServiceTest {

    @Mock
    private EmNpaApi emNpaApi;

    @InjectMocks
    private EmNpaService emNpaService;

    private static final String DOCUMENT_ID = "12345678-1234-1234-1234-123456789012";
    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "Bearer service-token";

    @BeforeEach
    void setUp() {
        emNpaService = new EmNpaService(emNpaApi);
    }

    @Test
    void deleteRedactionsForDocument_Success() {
        when(emNpaApi.deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN))
            .thenReturn(ResponseEntity.noContent().build());

        boolean result = emNpaService.deleteRedactionsForDocument(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);

        assertTrue(result);

        verify(emNpaApi).deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteRedactionsForDocument_Non204_ReturnsFalse() {
        when(emNpaApi.deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN))
            .thenReturn(ResponseEntity.ok().build());

        boolean result = emNpaService.deleteRedactionsForDocument(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);

        assertFalse(result);
        verify(emNpaApi).deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteRedactionsForDocument_ThrowsException() {
        RuntimeException exception = new RuntimeException("API call failed");
        doThrow(exception).when(emNpaApi).deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);

        assertThrows(RuntimeException.class, () ->
            emNpaService.deleteRedactionsForDocument(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN)
        );

        verify(emNpaApi).deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteRedactionsForDocument_WithNullDocumentId() {
        when(emNpaApi.deleteRedactions(null, USER_TOKEN, SERVICE_TOKEN))
            .thenReturn(ResponseEntity.noContent().build());

        boolean result = emNpaService.deleteRedactionsForDocument(null, USER_TOKEN, SERVICE_TOKEN);

        assertTrue(result);

        verify(emNpaApi).deleteRedactions(null, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteRedactionsForDocument_WithEmptyTokens() {
        String emptyToken = "";
        when(emNpaApi.deleteRedactions(DOCUMENT_ID, emptyToken, emptyToken))
            .thenReturn(ResponseEntity.noContent().build());

        boolean result = emNpaService.deleteRedactionsForDocument(DOCUMENT_ID, emptyToken, emptyToken);

        assertTrue(result);

        verify(emNpaApi).deleteRedactions(DOCUMENT_ID, emptyToken, emptyToken);
    }
}
