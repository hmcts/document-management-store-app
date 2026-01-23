package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.client.EmNpaApi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
        doNothing().when(emNpaApi).deleteRedactions(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);

        emNpaService.deleteRedactionsForDocument(DOCUMENT_ID, USER_TOKEN, SERVICE_TOKEN);

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
        doNothing().when(emNpaApi).deleteRedactions(null, USER_TOKEN, SERVICE_TOKEN);

        emNpaService.deleteRedactionsForDocument(null, USER_TOKEN, SERVICE_TOKEN);

        verify(emNpaApi).deleteRedactions(null, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteRedactionsForDocument_WithEmptyTokens() {
        String emptyToken = "";
        doNothing().when(emNpaApi).deleteRedactions(DOCUMENT_ID, emptyToken, emptyToken);

        emNpaService.deleteRedactionsForDocument(DOCUMENT_ID, emptyToken, emptyToken);

        verify(emNpaApi).deleteRedactions(DOCUMENT_ID, emptyToken, emptyToken);
    }
}
