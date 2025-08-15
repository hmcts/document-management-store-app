package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

}

