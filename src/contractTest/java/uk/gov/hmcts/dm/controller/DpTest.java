package uk.gov.hmcts.dm.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.dmstore.provider.StoreDocumentControllerTestConfiguration;


@Provider("em_dm_store")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "localhost",
    consumerVersionSelectors = {
        @VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")})
@WebMvcTest(StoredDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(StoreDocumentControllerTestConfiguration.class)
public class DpTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private RequestAuthorizer<Service> serviceRequestAuthorizer;

    @MockBean
    private AuthenticationManager authenticationManager;

//    @MockBean
//    private DocumentContentVersionService documentContentVersionService;

    @MockBean
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;
//
//    @MockBean
//    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }


    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        context.setTarget(new MockMvcTestTarget(mockMvc));

    }

    @State({"I have authenticated with service"})
    public void toUploadDocuments() {
    }

}
