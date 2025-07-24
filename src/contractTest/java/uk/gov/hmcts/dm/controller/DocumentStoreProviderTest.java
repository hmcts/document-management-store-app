package uk.gov.hmcts.dm.controller;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.controller.provider.BaseProviderTest;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.SearchService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@Provider("em_dm_store")
@WebMvcTest({StoredDocumentController.class, StoredDocumentDeleteController.class})
@Import(StoreDocumentControllerTestConfiguration.class)
public class DocumentStoreProviderTest extends BaseProviderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @MockitoBean
    protected DocumentContentVersionService documentContentVersionService;

    @MockitoBean
    private SearchService searchService;

    private final UUID id = UUID.fromString("5c3c3906-2b51-468e-8cbb-a4002eded075");

    @Override
    protected Object[] getControllersUnderTest() {
        return new Object[]{StoredDocumentController.class, StoredDocumentDeleteController.class};
    }


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        if (context != null) {
            context.setTarget(new MockMvcTestTarget(mockMvc));
        }
    }

    @State({"I have existing document"})
    public void toDeleteDocuments() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(new StoredDocument(),
            new MockMultipartFile("files",
                "filename.txt",
                "text/plain",
                "hello".getBytes(
                    StandardCharsets.UTF_8)),
            "user");

        documentContentVersion.setCreatedBy("userId");
        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(Optional.of(documentContentVersion));

    }
}
