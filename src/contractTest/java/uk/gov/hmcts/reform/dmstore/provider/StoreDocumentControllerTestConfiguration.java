package uk.gov.hmcts.reform.dmstore.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.controller.StoredDocumentController;
import uk.gov.hmcts.dm.repository.*;
import uk.gov.hmcts.dm.security.MultipartFileListWhiteListValidator;
import uk.gov.hmcts.dm.security.MultipartFileSizeValidator;
import uk.gov.hmcts.dm.service.*;

import java.util.Arrays;


@TestConfiguration
@Lazy
public class StoreDocumentControllerTestConfiguration {

    @MockBean
    private FolderRepository folderRepository;

    @MockBean
    private StoredDocumentRepository storedDocumentRepository;

    @MockBean
    private DocumentContentVersionRepository documentContentVersionRepository;

    @MockBean
    private DocumentContentRepository documentContentRepository;

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
    private DocumentContentVersionService documentContentVersionService;

    @MockBean
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Autowired
    public ToggleConfiguration toggleConfiguration;

    @Bean
    @Primary
    public FileSizeVerifier fileSizeVerifier() {
        return new FileSizeVerifier();
    }

    @Bean
    @Primary
    public MultipartFileSizeValidator multipartFileSizeValidator(){
        return  new MultipartFileSizeValidator(fileSizeVerifier());
    }

    @Bean
    @Primary
    public FileContentVerifier fileContentVerifier() {
        return new FileContentVerifier(Arrays.asList("application/pdf"), Arrays.asList(".pdf"));
    }

    @Bean
    @Primary
    public MultipartFileListWhiteListValidator multipartFileListWhiteListValidator(){
        return  new MultipartFileListWhiteListValidator(fileContentVerifier());
    }

    @Bean
    @Primary
    public AuditEntryService auditEntryService() {
        return new AuditEntryService();
    }

    @Bean
    @Primary
    public StoredDocumentService storedDocumentService() {
        return new StoredDocumentService();
    }

    @Bean
    @Primary
    public AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService() {
        return new AuditedStoredDocumentOperationsService();
    }

    @Bean
    @Primary
    public StoredDocumentController storedDocumentController() {
        return new StoredDocumentController();
    }

    @Bean
    @Primary
    public ToggleConfiguration toggleConfiguration(){
        return new ToggleConfiguration();
    }


}
