package uk.gov.hmcts.dm.controller;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.FileContentVerifier;
import uk.gov.hmcts.dm.service.FileSizeVerifier;
import uk.gov.hmcts.dm.service.PasswordVerifier;
import uk.gov.hmcts.dm.service.SearchService;
import uk.gov.hmcts.dm.service.SecurityUtilService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.Arrays;


@TestConfiguration
public class StoreDocumentControllerTestConfiguration {


    @Bean
    public DocumentContentVersionService documentContentVersionService() {
        return Mockito.mock(DocumentContentVersionService.class);
    }

    @Bean
    public SearchService searchService() {
        return Mockito.mock(SearchService.class);
    }

    @Bean
    public DmServiceAuthFilter serviceAuthFilter() {
        return Mockito.mock(DmServiceAuthFilter.class);
    }

    @Bean
    public StoredDocumentRepository storedDocumentRepository() {
        return Mockito.mock(StoredDocumentRepository.class);
    }

    @Bean
    public DocumentContentVersionRepository documentContentVersionRepository() {
        return Mockito.mock(DocumentContentVersionRepository.class);
    }

    @Bean
    public AzureStorageConfiguration azureStorageConfiguration() {
        return Mockito.mock(AzureStorageConfiguration.class);
    }

    @Bean
    public SecurityUtilService securityUtilService() {
        return Mockito.mock(SecurityUtilService.class);
    }

    @Bean
    public BlobStorageWriteService blobStorageWriteService() {
        return Mockito.mock(BlobStorageWriteService.class);
    }

    @Bean
    public BlobStorageDeleteService blobStorageDeleteService() {
        return Mockito.mock(BlobStorageDeleteService.class);
    }

    @Bean
    public StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository() {
        return Mockito.mock(StoredDocumentAuditEntryRepository.class);
    }

    @Bean
    public DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository() {
        return Mockito.mock(DocumentContentVersionAuditEntryRepository.class);
    }

    @Bean
    public AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService() {
        return Mockito.mock(AuditedDocumentContentVersionOperationsService.class);
    }

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
        return new PasswordVerifier();
    }

    @Bean
    @Primary
    public MultipartFilePasswordValidator multipartFilePasswordValidator(
        PasswordVerifier passwordVerifier
    ) {
        return new MultipartFilePasswordValidator(passwordVerifier);
    }

    @MockitoBean
    RepositoryFinder repositoryFinder;

    @Bean
    @Primary
    public AuditEntryService auditEntryService(
        StoredDocumentAuditEntryRepository storedDocumentAuditEntryRepository,
        DocumentContentVersionAuditEntryRepository documentContentVersionAuditEntryRepository,
        SecurityUtilService securityUtilService
    ) {
        return new AuditEntryService(storedDocumentAuditEntryRepository,
            documentContentVersionAuditEntryRepository,
            securityUtilService);
    }

    @Bean
    @Primary
    public StoredDocumentService storedDocumentService(
        StoredDocumentRepository storedDocumentRepository,
        DocumentContentVersionRepository documentContentVersionRepository,
        ToggleConfiguration toggleConfiguration,
        SecurityUtilService securityUtilService,
        BlobStorageWriteService blobStorageWriteService,
        BlobStorageDeleteService blobStorageDeleteService
    ) {
        return new StoredDocumentService(storedDocumentRepository,
            documentContentVersionRepository,
            toggleConfiguration,
            securityUtilService,
            blobStorageWriteService,
            blobStorageDeleteService);
    }

    @Bean
    @Primary
    public AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService(
        StoredDocumentService storedDocumentService,
        AuditEntryService auditEntryService
    ) {
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
