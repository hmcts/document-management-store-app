package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Provider("dm_store_stored_document_provider")
public class StoredDocumentControllerProviderTest extends BaseProviderTest {

    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";

    private static final UUID DOCUMENT_CONTENT_VERSION_ID =
        UUID.fromString("2216a872-81f7-4cad-a474-32a59608b038");

    private StoredDocument createSampleStoredDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.fromString(DOCUMENT_ID));
        storedDocument.setClassification(Classifications.PUBLIC);
        storedDocument.setCreatedBy("test-user");
        storedDocument.setCreatedOn(new Date());
        storedDocument.setModifiedOn(new Date());
        storedDocument.setRoles(Set.of("citizen"));
        return storedDocument;
    }

    @State("A Stored Document exists and can be retrieved by documentId")
    public void storedDocumentExists() {
        StoredDocument storedDocument = createSampleStoredDocument();
        when(auditedStoredDocumentOperationsService.readStoredDocument(UUID.fromString(DOCUMENT_ID)))
            .thenReturn(storedDocument);
    }

    @State("A specific Document Content Version binary exists for a given Stored Document.")
    public void documentContentVersionBinaryExists() throws IOException {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.fromString(DOCUMENT_ID));
        storedDocument.setClassification(Classifications.PUBLIC);
        storedDocument.setCreatedBy("test-user");
        storedDocument.setCreatedOn(new Date());
        storedDocument.setRoles(Set.of("citizen"));

        DocumentContentVersion documentContentVersion = new DocumentContentVersion(
            DOCUMENT_CONTENT_VERSION_ID,
            "application/octet-stream",
            true,
            "sample.pdf",
            "test-user",
            "test-service",
            new Date(),
            storedDocument,
            8L, // size matches dummyData length
            "http://localhost/documents/" + DOCUMENT_ID + "/versions/" + DOCUMENT_CONTENT_VERSION_ID,
            "abc123checksum"
        );

        when(documentContentVersionService
            .findMostRecentDocumentContentVersionByStoredDocumentId(UUID.fromString(DOCUMENT_ID)))
            .thenReturn(Optional.of(documentContentVersion));

        byte[] dummyData = new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x20, 0x30, 0x40
        };

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(2);
            try (ServletOutputStream os = response.getOutputStream()) {
                os.write(dummyData);
                os.flush();
            }
            return null;
        }).when(auditedDocumentContentVersionOperationsService)
            .readDocumentContentVersionBinaryFromBlobStore(eq(documentContentVersion), any(), any());
    }

}
