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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.dm.componenttests.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.dm.componenttests.backdoors.UserResolverBackdoor;
import uk.gov.hmcts.dm.componenttests.sugar.CustomResultMatcher;
import uk.gov.hmcts.dm.componenttests.sugar.RestActions;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.*;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@ActiveProfiles({"embedded", "local", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
@Transactional
@EnableSpringDataWebSupport
@DirtiesContext
public abstract class ComponentTestBase {

    // @Autowired
    // protected DbBackdoor db;

    @Autowired
    protected ServiceResolverBackdoor serviceRequestAuthorizer;

    @Autowired
    protected UserResolverBackdoor userRequestAuthorizer;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    protected AuthCheckerServiceOnlyFilter filter;

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
    protected BlobStorageMigrationService blobStorageMigrationService;

    @MockBean
    protected SearchService searchService;

    @MockBean
    protected AuditEntryService auditEntryService;

    protected RestActions restActions;

    @Before
    public void setUp() {
        MockMvc mvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        this.restActions = new RestActions(mvc, serviceRequestAuthorizer, userRequestAuthorizer, objectMapper);
        filter.setCheckForPrincipalChanges(true);
        filter.setInvalidateSessionOnPrincipalChange(true);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    CustomResultMatcher body() {
        return new CustomResultMatcher(objectMapper);
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
