package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Provider("dm_store_document_content_version_provider")
public class DocumentContentVersionControllerProviderTest extends BaseProviderTest {

    private static final String DOCUMENT_ID = "969983aa-52ae-41bd-8cf3-4aabcc120783";
    private static final UUID DOCUMENT_CONTENT_VERSION_ID =
        UUID.fromString("2216a872-81f7-4cad-a474-32a59608b038");

    @State("Can add Document Content Version and associate it with a given Stored Document.")
    public void documentExistToSoftDelete() {

        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.fromString(DOCUMENT_ID));
        storedDocument.setClassification(Classifications.PUBLIC); // if required
        storedDocument.setCreatedBy("test-user");
        storedDocument.setCreatedOn(new Date());
        storedDocument.setRoles(Set.of("citizen")); // example roles

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("test-document.pdf");
        when(file.getSize()).thenReturn(1024L);

        DocumentContentVersion documentContentVersion = new DocumentContentVersion(
            DOCUMENT_CONTENT_VERSION_ID,
            "application/pdf",
            "sample.pdf",
            "test-user",
            "test-service",
            new Date(),
            storedDocument,
            1024L,
            "http://localhost/documents/1/versions/1",
            "abc123checksum"
        );
        when(storedDocumentService
            .findOne(any()))
            .thenReturn(Optional.of(storedDocument));
        when(auditedStoredDocumentOperationsService.addDocumentVersion(any(), any()))
            .thenReturn(documentContentVersion);
    }


    @State("A specific Document Content Version exists for a given Stored Document.")
    public void documentContentVersionExists() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.fromString(DOCUMENT_ID));
        storedDocument.setClassification(Classifications.PUBLIC);
        storedDocument.setCreatedBy("test-user");
        storedDocument.setCreatedOn(new Date());
        storedDocument.setRoles(Set.of("citizen"));

        DocumentContentVersion documentContentVersion = new DocumentContentVersion(
            DOCUMENT_CONTENT_VERSION_ID,
            "application/pdf",
            "sample.pdf",
            "test-user",
            "test-service",
            new Date(),
            storedDocument,
            1024L,
            "http://localhost/documents/" + DOCUMENT_ID + "/versions/" + DOCUMENT_CONTENT_VERSION_ID,
            "abc123checksum"
        );

        when(auditedDocumentContentVersionOperationsService.readDocumentContentVersion(DOCUMENT_CONTENT_VERSION_ID))
            .thenReturn(documentContentVersion);
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
            "sample.pdf",
            "test-user",
            "test-service",
            new Date(),
            storedDocument,
            8L, // size matches dummyData length
            "http://localhost/documents/" + DOCUMENT_ID + "/versions/" + DOCUMENT_CONTENT_VERSION_ID,
            "abc123checksum"
        );

        when(documentContentVersionService.findById(DOCUMENT_CONTENT_VERSION_ID))
            .thenReturn(Optional.of(documentContentVersion));


        byte[] dummyData = new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x20, 0x30, 0x40
        };

        // Mock binary streaming
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
