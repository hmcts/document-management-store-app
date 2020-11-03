package uk.gov.hmcts.reform.dmstore.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.controller.StoredDocumentController;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentAuditEntryRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;


@ExtendWith(SpringExtension.class)
@Provider("em_dm_store")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "localhost",
    consumerVersionSelectors = {
        @VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")})
@TestPropertySource(properties = { "dm.mediafile.sizelimit=500", "dm.nonmediafile.sizelimit=300" })
@Import(StoreDocumentControllerTestConfiguration.class)
public class DocumentsProviderTest {

    @Value("${PACT_BRANCH_NAME}")
    String branchName;

    @Autowired
    public StoredDocumentController storedDocumentController;


    @Autowired
    public SecurityUtilService securityUtilServiceMock;

    @Autowired
    public AzureStorageConfiguration azureStorageConfigurationMock;

    @Autowired
    public StoredDocumentRepository storedDocumentRepositoryMock;

    @Autowired
    public StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepositoryMock;


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }


    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(storedDocumentController);
        context.setTarget(testTarget);

    }

    @State({"I have authenticated with service"})
    public void toUploadDocuments() {
        when(securityUtilServiceMock.getUserId()).thenReturn("divorceUserId");
        when(storedDocumentRepositoryMock.save(any(StoredDocument.class))).thenReturn(new StoredDocument());
    }



}
