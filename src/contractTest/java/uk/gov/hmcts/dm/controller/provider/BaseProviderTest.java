package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.security.DmServiceAuthFilter;
import uk.gov.hmcts.dm.controller.testing.TestController;
import uk.gov.hmcts.dm.errorhandler.ExceptionStatusCodeAndMessageResolver;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"embedded", "contract", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
@Transactional
@EnableSpringDataWebSupport
@IgnoreNoPactsToVerify
@AutoConfigureMockMvc(addFilters = false)
//Uncomment @PactFolder and comment the @PactBroker line to test local consumer.
//using this, import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
//@PactFolder("target/pacts")
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
    providerBranch = "${pact.provider.branch}"
)
public abstract class BaseProviderTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
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

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void setupPactVerification(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        if (context != null) {
            MockMvcTestTarget testTarget = new MockMvcTestTarget(mockMvc);
            testTarget.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
            context.setTarget(testTarget);
        }
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @SneakyThrows
    String contentsOf(String fileName) {
        String content = new String(Files.readAllBytes(Paths.get(ResourceUtils
            .getURL("classpath:" + fileName).toURI())), StandardCharsets.UTF_8);
        return resolvePlaceholders(content);
    }

    String resolvePlaceholders(String content) {
        return configurableListableBeanFactory.resolveEmbeddedValue(content);
    }
}
