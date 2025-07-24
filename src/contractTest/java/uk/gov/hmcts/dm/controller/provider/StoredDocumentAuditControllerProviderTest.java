package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.dm.config.AddMediaTypeSupportConfiguration;
import uk.gov.hmcts.dm.config.WebConfig;
import uk.gov.hmcts.dm.controller.StoredDocumentAuditController;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@Provider("dm_store_audit_provider")
@WebMvcTest(value = {StoredDocumentAuditController.class, WebConfig.class},
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class}
)
@Import({
    HypermediaAutoConfiguration.class,
    Jackson2HalModule.class,
    AddMediaTypeSupportConfiguration.class
})
public class StoredDocumentAuditControllerProviderTest extends BaseProviderTest {

    @MockitoBean
    private StoredDocumentRepository storedDocumentRepository;
    @MockitoBean
    private AuditEntryService auditEntryService;

    @State("Audit entries exist for a stored document")
    public void documentExistToSoftDelete() throws ParseException {
        UUID documentId = UUID.fromString("00351f93-dff5-46fa-af0d-b40c2cafb47f");
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(documentId);

        StoredDocumentAuditEntry auditEntry = new StoredDocumentAuditEntry();
        auditEntry.setStoredDocument(storedDocument);
        auditEntry.setAction(AuditActions.READ);
        auditEntry.setUsername("user@example.com");
        auditEntry.setServiceName("example-api");
        UUID auditId = UUID.fromString("5c64108c-ae46-427c-8235-48f3fbf04164");
        auditEntry.setId(auditId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date fixedDate = sdf.parse("2025-07-22T10:00:00Z");
        auditEntry.setRecordedDateTime(fixedDate);

        when(storedDocumentRepository.findById(documentId)).thenReturn(Optional.of(storedDocument));
        when(auditEntryService.findStoredDocumentAudits(any(StoredDocument.class))).thenReturn(List.of(auditEntry));

    }
}
