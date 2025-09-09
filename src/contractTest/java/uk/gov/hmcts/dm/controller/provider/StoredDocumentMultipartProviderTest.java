package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.security.DmServiceAuthFilter;
import uk.gov.hmcts.dm.controller.testing.TestController;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.errorhandler.ExceptionStatusCodeAndMessageResolver;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.BlobStorageDeleteService;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.ScheduledTaskRunner;
import uk.gov.hmcts.dm.service.SearchService;
import uk.gov.hmcts.dm.service.SecurityUtilService;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator; // Import the validator

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Provider("dm_store_stored_document_multipart_provider")
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"embedded", "contract", "componenttest"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
    providerBranch = "${pact.provider.branch}"
)
public class StoredDocumentMultipartProviderTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    protected DmServiceAuthFilter filter;

    @MockitoBean
    protected ToggleConfiguration toggleConfiguration;

    @MockitoBean
    protected StoredDocumentService storedDocumentService;

    @MockitoBean
    protected StoredDocumentRepository storedDocumentRepository;

    @MockitoBean
    protected DocumentContentVersionService documentContentVersionService;

    @MockitoBean
    protected AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @MockitoBean
    protected AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @MockitoBean
    protected BlobStorageWriteService blobStorageWriteService;

    @MockitoBean
    protected BlobStorageDeleteService blobStorageDeleteService;

    @MockitoBean
    protected BlobStorageReadService blobStorageReadService;

    @MockitoBean
    protected SearchService searchService;

    @MockitoBean
    protected SecurityUtilService securityUtilService;

    @MockitoBean
    protected AuditEntryService auditEntryService;

    @MockitoBean
    protected ScheduledTaskRunner scheduledTaskRunner;

    @MockitoBean
    protected ExceptionStatusCodeAndMessageResolver exceptionStatusCodeAndMessageResolver;

    @MockitoBean
    protected TestController testController;

    @MockitoBean
    private AuthTokenValidator authTokenValidator;

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @BeforeEach
    void setupPactVerification(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        if (context != null) {
            context.setTarget(new HttpTestTarget("localhost", port));
        }
    }



    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @State("Can create Stored Documents from multipart upload")
    public void canCreateStoredDocumentsFromMultipartUpload() {
        // 1. Mock the AuthTokenValidator
        // It needs to return a service name that would be in your "authorisedServices" list.
        when(authTokenValidator.getServiceName("Bearer some-s2s-token"))
            .thenReturn("some_authorised_service");

        // 2. Mock the service that provides the data for the controller's response
        StoredDocument doc1 = new StoredDocument();
        doc1.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        doc1.setClassification(Classifications.PUBLIC);
        doc1.setCreatedBy("test-user-1");
        // ... set other doc1 properties

        StoredDocument doc2 = new StoredDocument();
        doc2.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        doc2.setClassification(Classifications.PUBLIC);
        doc2.setCreatedBy("test-user-2");
        // ... set other doc2 properties

        when(auditedStoredDocumentOperationsService.createStoredDocuments(any()))
            .thenReturn(List.of(doc1, doc2));
    }
}
