package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.config.security.DmServiceAuthFilter;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.dm.controller.Const.EXAMPLE_SERVICE;
import static uk.gov.hmcts.dm.controller.Const.EXAMPLE_USER;

@Provider("dm_store_update_document_provider")
public class StoredDocumentUpdateControllerProviderTest extends BaseProviderTest {

    @Autowired
    public StoredDocumentUpdateControllerProviderTest(
        MockMvc mockMvc,
        WebApplicationContext webApplicationContext,
        ObjectMapper objectMapper,
        ConfigurableListableBeanFactory configurableListableBeanFactory,
        DmServiceAuthFilter filter
    ) {
        super(mockMvc, webApplicationContext, objectMapper, configurableListableBeanFactory, filter);
    }

    @State("Documents exist and can be updated with new TTL")
    public void documentsExistToUpdate() {
        when(auditedStoredDocumentOperationsService.updateDocument(
            any(UUID.class),
            any(Map.class),
            any(Date.class))
        ).thenReturn(new StoredDocument());
    }


    @State("Document exist and can be updated with new TTL")
    public void specificDocumentExistToUpdate() {

        UUID documentId = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

        StoredDocument storedDocument = StoredDocument.builder()
            .id(documentId)
            .createdBy(EXAMPLE_USER)
            .createdByService(EXAMPLE_SERVICE)
            .lastModifiedBy(EXAMPLE_USER)
            .lastModifiedByService(EXAMPLE_SERVICE)
            .createdOn(new Date())
            .modifiedOn(new Date())
            .deleted(false)
            .hardDeleted(false)
            .classification(Classifications.PRIVATE)
            .roles(Set.of("citizen", "caseworker"))
            .metadata(Map.of("caseId", "123456", "docType", "evidence"))
            .ttl(new Date(System.currentTimeMillis() + 86400000L)) // 1 day in future
            .documentContentVersions(List.of(mockDocumentContentVersion(documentId)))
            .auditEntries(Set.of())
            .build();

        when(auditedStoredDocumentOperationsService.updateDocument(
            any(UUID.class),
            any(UpdateDocumentCommand.class))
        ).thenReturn(storedDocument);
    }

    private DocumentContentVersion mockDocumentContentVersion(UUID documentId) {
        DocumentContentVersion version = new DocumentContentVersion();
        version.setId(UUID.randomUUID());
        version.setStoredDocument(new StoredDocument(documentId));
        version.setCreatedOn(new Date());
        version.setMimeType("application/pdf");
        version.setSize(2048L);
        version.setCreatedBy(EXAMPLE_USER);
        version.setCreatedByService(EXAMPLE_SERVICE);
        return version;
    }
}
