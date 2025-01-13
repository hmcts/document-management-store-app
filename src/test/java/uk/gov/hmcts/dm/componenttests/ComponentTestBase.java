package uk.gov.hmcts.dm.componenttests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.dm.componenttests.sugar.RestActions;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.security.DmServiceAuthFilter;
import uk.gov.hmcts.dm.controller.testing.TestController;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.BlobStorageDeleteService;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.SearchService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"embedded", "local", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
@Transactional
@EnableSpringDataWebSupport
public abstract class ComponentTestBase {


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
    protected AuditEntryService auditEntryService;

    protected RestActions restActions;

    @MockitoBean
    protected TestController testController;

    @BeforeEach
    public void setUp() {
        this.restActions = new RestActions(webApplicationContext, objectMapper);
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
