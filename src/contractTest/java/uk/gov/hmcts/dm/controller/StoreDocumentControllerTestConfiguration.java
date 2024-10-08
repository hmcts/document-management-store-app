package uk.gov.hmcts.dm.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.config.security.DmServiceAuthFilter;
import uk.gov.hmcts.dm.errorhandler.ExceptionStatusCodeAndMessageResolver;
import uk.gov.hmcts.dm.repository.DocumentContentVersionAuditEntryRepository;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.repository.StoredDocumentAuditEntryRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.security.MultipartFileListWhiteListValidator;
import uk.gov.hmcts.dm.security.MultipartFilePasswordValidator;
import uk.gov.hmcts.dm.security.MultipartFileSizeValidator;
import uk.gov.hmcts.dm.service.AuditEntryService;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.BlobStorageDeleteService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileSizeVerifier;
import uk.gov.hmcts.dm.service.PasswordVerifier;
import uk.gov.hmcts.dm.service.SecurityUtilService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.Arrays;


@TestConfiguration
public class StoreDocumentControllerTestConfiguration {

    @MockBean
    private DmServiceAuthFilter serviceAuthFilter;

    @MockBean
    private StoredDocumentRepository storedDocumentRepository;

    @MockBean
    private DocumentContentVersionRepository documentContentVersionRepository;

    @MockBean
    private AzureStorageConfiguration azureStorageConfiguration;

    @MockBean
    private SecurityUtilService securityUtilService;

    @MockBean
    private BlobStorageWriteService blobStorageWriteService;

    @MockBean
    private BlobStorageDeleteService blobStorageDeleteService;

    @MockBean
    private StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository;

    @MockBean
    private DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository;

    @MockBean
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @MockBean
    public ToggleConfiguration toggleConfiguration;

    @MockBean
    public StoredDocumentService storedDocumentService;

    @MockBean
    public AuditEntryService auditEntryService;

    @Bean
    @Primary
    public FileSizeVerifier fileSizeVerifier() {
        return new FileSizeVerifier();
    }

    @Bean
    @Primary
    public MultipartFileSizeValidator multipartFileSizeValidator() {
        return  new MultipartFileSizeValidator(fileSizeVerifier());
    }

    @Bean
    @Primary
    public FileContentVerifier fileContentVerifier() {
        return new FileContentVerifier(Arrays.asList("application/pdf", "text/plain"), Arrays.asList(".pdf",".txt"));
    }

    @Bean
    @Primary
    public MultipartFileListWhiteListValidator multipartFileListWhiteListValidator() {
        return  new MultipartFileListWhiteListValidator(fileContentVerifier());
    }

    @Bean
    @Primary
    public PasswordVerifier passwordVerifier() {
        return new PasswordVerifier(toggleConfiguration);
    }

    @Bean
    @Primary
    public MultipartFilePasswordValidator multipartFilePasswordValidator() {
        return new MultipartFilePasswordValidator(passwordVerifier());
    }

    @MockBean
    RepositoryFinder repositoryFinder;

    @Bean
    @Primary
    public AuditEntryService auditEntryService() {
        return new AuditEntryService(storedDocumentAuditEntryRepository,
            documentContentVersionAuditEntryRepository,
            securityUtilService);
    }

    @Bean
    @Primary
    public StoredDocumentService storedDocumentService() {
        return new StoredDocumentService(storedDocumentRepository,
            documentContentVersionRepository,
            toggleConfiguration,
            securityUtilService,
            blobStorageWriteService,
            blobStorageDeleteService);
    }

    @Bean
    @Primary
    public AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService() {
        return new AuditedStoredDocumentOperationsService(storedDocumentService, auditEntryService);
    }


    @Bean
    @Primary
    public ToggleConfiguration toggleConfiguration() {
        return new ToggleConfiguration();
    }


    @Bean
    @Primary
    public ExceptionStatusCodeAndMessageResolver exceptionStatusCodeAndMessageResolver() {
        return new ExceptionStatusCodeAndMessageResolver();
    }


}
