package uk.gov.hmcts.dm.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentRepository;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@Service
public class StoredDocumentService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    @Autowired
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    private DocumentContentRepository documentContentRepository;

    @Autowired
    private ToggleConfiguration toggleConfiguration;

    @Autowired
    private SecurityUtilService securityUtilService;

    @Autowired
    private BlobStorageWriteService blobStorageWriteService;

    @Autowired
    private BlobStorageDeleteService blobStorageDeleteService;

    @Setter
    @VisibleForTesting
    @Value("${azure.storage.enabled}")
    private Boolean azureBlobStorageEnabled;

    public Optional<StoredDocument> findOne(UUID id) {
        Optional<StoredDocument> storedDocument = Optional.ofNullable(storedDocumentRepository.findOne(id));
        if (storedDocument.isPresent() && storedDocument.get().isDeleted()) {
            return Optional.empty();
        }
        return storedDocument;
    }

    public StoredDocument save(StoredDocument storedDocument) {
        return storedDocumentRepository.save(storedDocument);
    }

    public void saveItemsToBucket(Folder folder, List<MultipartFile> files)  {
        String userId = securityUtilService.getUserId();
        List<StoredDocument> items = files.stream().map(aFile -> {
            StoredDocument storedDocument = new StoredDocument();
            storedDocument.setFolder(folder);
            storedDocument.setCreatedBy(userId);
            storedDocument.setLastModifiedBy(userId);
            final DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument,
                                                                                             aFile,
                                                                                             userId,
                                                                                             isAzureBlobStoreEnabled());
            storedDocument.getDocumentContentVersions().add(documentContentVersion);

            if (isAzureBlobStoreEnabled()) {
                blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
                                                                     documentContentVersion,
                                                                     aFile);
            }
            save(storedDocument);
            return storedDocument;

        }).collect(Collectors.toList());

        folder.getStoredDocuments().addAll(items);

        folderRepository.save(folder);
    }

    public List<StoredDocument> saveItems(UploadDocumentsCommand uploadDocumentsCommand)  {
        String userId = securityUtilService.getUserId();
        return uploadDocumentsCommand.getFiles().stream().map(file -> {
            StoredDocument document = new StoredDocument();
            document.setCreatedBy(userId);
            document.setLastModifiedBy(userId);
            document.setClassification(uploadDocumentsCommand.getClassification());
            document.setRoles(uploadDocumentsCommand.getRoles() != null
                ? uploadDocumentsCommand.getRoles().stream().collect(Collectors.toSet()) : null);

            if (toggleConfiguration.isMetadatasearchendpoint()) {
                document.setMetadata(uploadDocumentsCommand.getMetadata());
            }
            if (toggleConfiguration.isTtl()) {
                document.setTtl(uploadDocumentsCommand.getTtl());
            }
            final DocumentContentVersion documentContentVersion = new DocumentContentVersion(document,
                                                                                             file,
                                                                                             userId,
                                                                                             isAzureBlobStoreEnabled());
            document.getDocumentContentVersions().add(documentContentVersion);
            if (isAzureBlobStoreEnabled()) {
                blobStorageWriteService.uploadDocumentContentVersion(document,
                                                                     documentContentVersion,
                                                                     file);
            }
            save(document);
            return document;
        }).collect(Collectors.toList());

    }

    public List<StoredDocument> saveItems(List<MultipartFile> files)  {
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(files);
        return saveItems(command);
    }

    public DocumentContentVersion addStoredDocumentVersion(StoredDocument storedDocument, MultipartFile file)  {
        String userId = securityUtilService.getUserId();
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument,
                                                                                   file,
                                                                                   userId,
                                                                                   isAzureBlobStoreEnabled());
        documentContentVersionRepository.save(documentContentVersion);
        storedDocument.getDocumentContentVersions().add(documentContentVersion);
        return documentContentVersion;
    }

    public void deleteDocument(StoredDocument storedDocument, boolean permanent) {
        storedDocument.setDeleted(true);
        if (permanent) {
            storedDocument.setHardDeleted(true);
            storedDocument.getDocumentContentVersions().forEach(documentContentVersion -> {
                Optional.ofNullable(documentContentVersion.getDocumentContent())
                        .ifPresent(dc -> {
                            documentContentRepository.delete(dc);
                            documentContentVersion.setDocumentContent(null);
                        });
                blobStorageDeleteService.deleteIfExists(storedDocument.getId(), documentContentVersion);
            });
        }
        save(storedDocument);
    }

    public void updateStoredDocument(@NonNull StoredDocument storedDocument, @NonNull UpdateDocumentCommand command) {

        if (!storedDocument.isDeleted()) {
            storedDocument.setTtl(command.getTtl());
            storedDocument.setLastModifiedBy(securityUtilService.getUserId());
            save(storedDocument);
        }

    }

    public List<StoredDocument> findAllExpiredStoredDocuments() {
        return storedDocumentRepository.findByTtlLessThanAndHardDeleted(new Date(), false);
    }

    private Boolean isAzureBlobStoreEnabled() {
        return Optional.ofNullable(azureBlobStorageEnabled).orElse(false);
    }

}
