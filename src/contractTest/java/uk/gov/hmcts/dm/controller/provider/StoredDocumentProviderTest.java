package uk.gov.hmcts.dm.controller.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.WebConfig;
import uk.gov.hmcts.dm.controller.StoredDocumentController;
import uk.gov.hmcts.dm.controller.StoredDocumentDeleteController;
import uk.gov.hmcts.dm.controller.StoredDocumentUpdateController;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.errorhandler.ExceptionStatusCodeAndMessageResolver;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.ScheduledTaskRunner;
import uk.gov.hmcts.dm.service.SearchService;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(
    value = {
        StoredDocumentController.class,
        StoredDocumentUpdateController.class,
        StoredDocumentDeleteController.class
    },
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ClientAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebConfig.class)
)
@PactBroker(
    url = "${PACT_BROKER_FULL_URL:http://localhost:80}",
    providerBranch = "${pact.provider.branch}"
)
@IgnoreNoPactsToVerify
@Provider("em_dm_store")
public class StoredDocumentProviderTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected ScheduledTaskRunner scheduledTaskRunner;

    @MockitoBean
    protected ExceptionStatusCodeAndMessageResolver exceptionStatusCodeAndMessageResolver;

    @MockitoBean
    AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @MockitoBean
    DocumentContentVersionService documentContentVersionService;

    @MockitoBean
    AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @MockitoBean
    ToggleConfiguration toggleConfiguration;

    @MockitoBean
    SearchService searchService;

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
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        if (context != null) {
            context.setTarget(new MockMvcTestTarget(mockMvc));
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Primary
        @Bean
        public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
            builder.indentOutput(true);
            builder.modules(new Jackson2HalModule());
            return builder;
        }

        @Bean
        @Primary
        public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
            ObjectMapper objectMapper = builder.build();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
            objectMapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(
                new DefaultLinkRelationProvider(),
                CurieProvider.NONE,
                MessageResolver.DEFAULTS_ONLY));
            return objectMapper;
        }

        @Bean
        public WebMvcConfigurer webMvcConfigurer(ObjectMapper objectMapper) {
            return new WebMvcConfigurer() {
                @Override
                public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
                    MappingJackson2HttpMessageConverter halConverter =
                        new MappingJackson2HttpMessageConverter(objectMapper);
                    halConverter.setSupportedMediaTypes(Arrays.asList(
                        MediaType.APPLICATION_JSON,
                        MediaTypes.HAL_JSON));
                    converters.add(0, halConverter);
                }
            };
        }
    }

    @State("document exists to GET")
    Map<String, Object> givenDocumentExists() {
        DocumentContentVersion version = DocumentContentVersion.builder()
            .mimeType("application/pdf")
            .size(12345L)
            .originalDocumentName("hearingAdjourned 8 Jun 2025 0942.pdf")
            .build();

        StoredDocument storedDocument = StoredDocument.builder()
            .id(UUID.randomUUID())
            .classification(Classifications.RESTRICTED)
            .createdBy(UUID.randomUUID().toString())
            .createdOn(new Date())
            .lastModifiedBy(UUID.randomUUID().toString())
            .modifiedOn(new Date())
            .documentContentVersions(List.of(version))
            .metadata(Map.of(
                "case_id", "1742576321265600",
                "case_type_id", "Civil",
                "jurisdiction", "Probate"
            ))
            .roles(Set.of("citizen"))
            .build();
        version.setStoredDocument(storedDocument);
        version.setCreatedBy(storedDocument.getCreatedBy());
        version.setCreatedOn(storedDocument.getCreatedOn());
        given(auditedStoredDocumentOperationsService.readStoredDocument(storedDocument.getId()))
            .willReturn(storedDocument);

        return Map.of("documentId", storedDocument.getId());
    }

    @State("a list of documents exist")
    void listOfDocumentsExist() {
        // mock by default does nothing, and we can't use doNothing() on non-void methods
    }

    @State("document exists to DELETE")
    Map<String, Object> documentExistsToDelete() {
        UUID documentId = UUID.randomUUID();
        doNothing().when(auditedStoredDocumentOperationsService).deleteStoredDocument(documentId, true);
        return Map.of("documentId", documentId);
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
        UUID id = UUID.fromString("5c3c3906-2b51-468e-8cbb-a4002eded075");
        when(this.documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id))
            .thenReturn(Optional.of(documentContentVersion));
    }
}
