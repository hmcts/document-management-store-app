package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.dm.config.WebConfig;
import uk.gov.hmcts.dm.controller.StoredDocumentDeleteController;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.SearchService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Provider("dm_store_delete_document_provider")
@WebMvcTest(value = StoredDocumentDeleteController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebConfig.class)
)
public class StoredDocumentDeleteControllerProviderTest extends BaseProviderTest {

    @MockitoBean
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;
    @MockitoBean
    private SearchService searchService;

    @State("Document exists and can be deleted")
    public void documentExistToDelete() {
        doNothing().when(auditedStoredDocumentOperationsService).deleteStoredDocument(any(UUID.class), anyBoolean());
    }

    @State("Document exists and can be soft deleted")
    public void documentExistToSoftDelete() {
        when(searchService.findStoredDocumentsIdsByCaseRef(any()))
            .thenReturn(List.of(new StoredDocument()));
        when(auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(any()))
            .thenReturn(new CaseDocumentsDeletionResults(5, 4));
    }
}
