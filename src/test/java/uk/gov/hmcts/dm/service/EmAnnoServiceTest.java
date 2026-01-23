package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.client.EmAnnoApi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class EmAnnoServiceTest {

    @Mock
    private EmAnnoApi emAnnoApi;

    @InjectMocks
    private EmAnnoService emAnnoService;

    private static final String DOC_ID = "12345678-1234-1234-1234-123456789012";
    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "Bearer service-token";

    @BeforeEach
    void setUp() {
        emAnnoService = new EmAnnoService(emAnnoApi);
    }

    @Test
    void deleteDocumentData_Success() {
        doNothing().when(emAnnoApi).deleteDocumentData(DOC_ID, USER_TOKEN, SERVICE_TOKEN);

        emAnnoService.deleteDocumentData(DOC_ID, USER_TOKEN, SERVICE_TOKEN);

        verify(emAnnoApi).deleteDocumentData(DOC_ID, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteDocumentData_ThrowsException() {
        RuntimeException exception = new RuntimeException("API call failed");
        doThrow(exception).when(emAnnoApi).deleteDocumentData(DOC_ID, USER_TOKEN, SERVICE_TOKEN);

        assertThrows(RuntimeException.class, () ->
            emAnnoService.deleteDocumentData(DOC_ID, USER_TOKEN, SERVICE_TOKEN)
        );

        verify(emAnnoApi).deleteDocumentData(DOC_ID, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteDocumentData_WithNullDocId() {
        doNothing().when(emAnnoApi).deleteDocumentData(null, USER_TOKEN, SERVICE_TOKEN);

        emAnnoService.deleteDocumentData(null, USER_TOKEN, SERVICE_TOKEN);

        verify(emAnnoApi).deleteDocumentData(null, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteDocumentData_WithEmptyTokens() {
        String emptyToken = "";
        doNothing().when(emAnnoApi).deleteDocumentData(DOC_ID, emptyToken, emptyToken);

        emAnnoService.deleteDocumentData(DOC_ID, emptyToken, emptyToken);

        verify(emAnnoApi).deleteDocumentData(DOC_ID, emptyToken, emptyToken);
    }
}
