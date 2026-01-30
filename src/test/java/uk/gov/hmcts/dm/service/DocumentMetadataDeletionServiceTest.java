package uk.gov.hmcts.dm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DocumentMetadataDeletionServiceTest {

    @Mock
    private EmAnnoService emAnnoService;

    @Mock
    private EmNpaService emNpaService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamClient idamClient;

    private DocumentMetadataDeletionService documentMetadataDeletionService;

    private static final String SYSTEM_USERNAME = "system-user@example.com";
    private static final String SYSTEM_PASSWORD = "system-password";
    private static final String USER_TOKEN = "Bearer user-token-12345";
    private static final String SERVICE_TOKEN = "Bearer service-token-67890";
    private static final UUID DOCUMENT_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final String DOCUMENT_ID_STRING = DOCUMENT_ID.toString();

    @BeforeEach
    void setUp() {
        documentMetadataDeletionService = new DocumentMetadataDeletionService(
            emAnnoService,
            emNpaService,
            authTokenGenerator,
            idamClient,
            SYSTEM_USERNAME,
            SYSTEM_PASSWORD
        );

        when(idamClient.getAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD)).thenReturn(USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_Success() {
        when(emAnnoService.deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(emNpaService.deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        assertTrue(result);
        verify(idamClient).getAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD);
        verify(authTokenGenerator).generate();
        verify(emAnnoService).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        verify(emNpaService).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_CallsServicesInCorrectOrder() {
        when(emAnnoService.deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(emNpaService.deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        InOrder inOrder = inOrder(emAnnoService, emNpaService);
        inOrder.verify(emAnnoService).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        inOrder.verify(emNpaService).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_EmAnnoFailure_ReturnsFalse() {
        when(emAnnoService.deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        assertFalse(result);
        verify(emAnnoService).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        verify(emNpaService, never()).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_EmNpaFailure_ReturnsFalse() {
        when(emAnnoService.deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(emNpaService.deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        assertFalse(result);
        verify(emAnnoService).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        verify(emNpaService).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_IdamTokenGenerationFailure_ReturnsFalse() {
        when(idamClient.getAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD))
            .thenThrow(new RuntimeException("IDAM authentication failed"));

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        assertFalse(result);
        verify(idamClient).getAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD);
        verify(emAnnoService, never()).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        verify(emNpaService, never()).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_ServiceTokenGenerationFailure_ReturnsFalse() {
        when(authTokenGenerator.generate())
            .thenThrow(new RuntimeException("S2S token generation failed"));

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        assertFalse(result);
        verify(idamClient).getAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD);
        verify(authTokenGenerator).generate();
        verify(emAnnoService, never()).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        verify(emNpaService, never()).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }

    @Test
    void deleteExternalMetadata_GeneratesTokensOnce() {
        when(emAnnoService.deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(emNpaService.deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        verify(idamClient).getAccessToken(SYSTEM_USERNAME, SYSTEM_PASSWORD);
        verify(authTokenGenerator).generate();
    }

    @Test
    void deleteExternalMetadata_EmAnnoThrows_DoesNotCallEmNpa_ReturnsFalse() {
        doThrow(new RuntimeException("em-anno API error"))
            .when(emAnnoService).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(DOCUMENT_ID);

        assertFalse(result);
        verify(emAnnoService).deleteDocumentData(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
        verify(emNpaService, never()).deleteRedactionsForDocument(DOCUMENT_ID_STRING, USER_TOKEN, SERVICE_TOKEN);
    }
}
