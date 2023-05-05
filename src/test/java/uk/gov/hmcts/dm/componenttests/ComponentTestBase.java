package uk.gov.hmcts.dm.componenttests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.dm.componenttests.sugar.RestActions;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.controller.testing.TestController;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.*;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@RunWith(SpringRunner.class)
@ActiveProfiles({"embedded", "local", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
@Transactional
@EnableSpringDataWebSupport
@DirtiesContext
public abstract class ComponentTestBase {



    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    protected ServiceAuthFilter filter;

    @MockBean
    protected ToggleConfiguration toggleConfiguration;

    @MockBean
    protected FolderService folderService;

    @MockBean
    protected StoredDocumentService storedDocumentService;

    @MockBean
    protected StoredDocumentRepository storedDocumentRepository;

    @MockBean
    protected DocumentContentVersionService documentContentVersionService;

    @MockBean
    protected AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @MockBean
    protected AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @MockBean
    protected BlobStorageWriteService blobStorageWriteService;

    @MockBean
    protected BlobStorageDeleteService blobStorageDeleteService;

    @MockBean
    protected BlobStorageReadService blobStorageReadService;

    @MockBean
    protected SearchService searchService;

    @MockBean
    protected AuditEntryService auditEntryService;

    protected RestActions restActions;

    @MockBean
    TestController testController;

    @Before
    public void setUp() {
        this.restActions = new RestActions(webApplicationContext, objectMapper);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @SneakyThrows
    String contentsOf(String fileName) {
        String content = new String(Files.readAllBytes(Paths.get(ResourceUtils.getURL("classpath:" + fileName).toURI())), StandardCharsets.UTF_8);
        return resolvePlaceholders(content);
    }

    String resolvePlaceholders(String content) {
        return configurableListableBeanFactory.resolveEmbeddedValue(content);
    }
}
