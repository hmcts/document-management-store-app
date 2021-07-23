package uk.gov.hmcts.dm.service;

import lombok.NonNull;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentRepository;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
    private AzureStorageConfiguration azureStorageConfiguration;

    @Autowired
    private SecurityUtilService securityUtilService;

    @Autowired
    private BlobStorageWriteService blobStorageWriteService;

    @Autowired
    private BlobStorageDeleteService blobStorageDeleteService;

    public Optional<StoredDocument> findOne(UUID id) {
        Optional<StoredDocument> storedDocument = storedDocumentRepository.findById(id);
        if (storedDocument.isPresent() && storedDocument.get().isDeleted()) {
            return Optional.empty();
        }
        return storedDocument;
    }

    public Optional<StoredDocument> findOneWithBinaryData(UUID id) {
        Optional<StoredDocument> storedDocument = storedDocumentRepository.findById(id);
        if (storedDocument.isPresent() && storedDocument.get().isHardDeleted()) {
            return Optional.empty();
        }
        return storedDocument;
    }

    public StoredDocument save(StoredDocument storedDocument) {
        return storedDocumentRepository.save(storedDocument);
    }

    public void saveItemsToBucket(Folder folder, List<MultipartFile> files) {
        String userId = securityUtilService.getUserId();
        List<StoredDocument> items = files.stream().map(file -> {
            StoredDocument storedDocument = new StoredDocument();
            storedDocument.setFolder(folder);
            storedDocument.setCreatedBy(userId);
            storedDocument.setLastModifiedBy(userId);
            final DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument,
                                                                                             file,
                                                                                             userId,
                                                                                             azureStorageConfiguration
                                                                                                 .isPostgresBlobStorageEnabled());
            storedDocument.getDocumentContentVersions().add(documentContentVersion);

            save(storedDocument);
            storeInAzureBlobStorage(storedDocument, documentContentVersion, file);
            closeBlobInputStream(documentContentVersion);
            return storedDocument;
        }).collect(Collectors.toList());

        folder.getStoredDocuments().addAll(items);

        folderRepository.save(folder);
    }

    public List<StoredDocument> saveItems(UploadDocumentsCommand uploadDocumentsCommand) {
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
            DocumentContentVersion documentContentVersion = new DocumentContentVersion(document,
                                                                                       file,
                                                                                       userId,
                                                                                       azureStorageConfiguration
                                                                                           .isPostgresBlobStorageEnabled());
            document.getDocumentContentVersions().add(documentContentVersion);
            save(document);
            storeInAzureBlobStorage(document, documentContentVersion, file);
            closeBlobInputStream(documentContentVersion);
            return document;
        }).collect(Collectors.toList());

    }

    public List<StoredDocument> saveItems(List<MultipartFile> files) {
        UploadDocumentsCommand command = new UploadDocumentsCommand();
        command.setFiles(files);
        return saveItems(command);
    }

    @Transactional
    public void updateItems(UpdateDocumentsCommand command) {
        for (DocumentUpdate update : command.documents) {
            if (Objects.nonNull(update) && Objects.nonNull(update.metadata)) {
                findOne(update.documentId).ifPresent(d -> updateMigratedStoredDocument(d, update.metadata));
            }
        }
    }

    public DocumentContentVersion addStoredDocumentVersion(StoredDocument storedDocument, MultipartFile file) {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument,
                                                                                   file,
                                                                                   securityUtilService.getUserId(),
                                                                                   azureStorageConfiguration
                                                                                       .isPostgresBlobStorageEnabled());
        storedDocument.getDocumentContentVersions().add(documentContentVersion);
        documentContentVersionRepository.save(documentContentVersion);
        storeInAzureBlobStorage(storedDocument, documentContentVersion, file);
        closeBlobInputStream(documentContentVersion);

        return documentContentVersion;
    }

    public void deleteDocument(StoredDocument storedDocument, boolean permanent) {
        storedDocument.setDeleted(true);
        if (permanent) {
            storedDocument.setHardDeleted(true);
            storedDocument.getDocumentContentVersions().parallelStream().forEach(documentContentVersion -> {
                if (azureStorageConfiguration.isAzureBlobStoreEnabled()) {
                    blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
                } else if (documentContentVersion.getDocumentContent() != null) {
                    documentContentRepository.delete(documentContentVersion.getDocumentContent());
                    documentContentVersion.setDocumentContent(null);
                }
            });
        }
        storedDocumentRepository.save(storedDocument);
    }

    public void updateStoredDocument(@NonNull StoredDocument storedDocument, @NonNull UpdateDocumentCommand command) {
        updateStoredDocument(storedDocument, command.getTtl(), null);
    }

    public void updateStoredDocument(
        @NonNull StoredDocument storedDocument,
        Date ttl,
        Map<String, String> metadata
    ) {
        if (storedDocument.isDeleted()) {
            return;
        }

        if (metadata != null) {
            storedDocument.getMetadata().putAll(metadata);
        }

        storedDocument.setTtl(ttl);
        storedDocument.setLastModifiedBy(securityUtilService.getUserId());
        save(storedDocument);
    }


    public void updateMigratedStoredDocument(
        @NonNull StoredDocument storedDocument,
        Map<String, String> metadata
    ) {
        if (storedDocument.isDeleted() || Objects.isNull(metadata)) {
            return;
        }

        if (storedDocument.getMetadata().isEmpty() || toggleConfiguration.isOverridemetadata()) {
            storedDocument.getMetadata().putAll(metadata);
        } else {
            //Don't override existing value for key. Instead add only new key/value to the Metadata
            Map<String, String> newMetaData = metadata.entrySet().stream()
                .filter(entry -> !storedDocument.getMetadata().containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            storedDocument.getMetadata().putAll(newMetaData);
        }

        storedDocument.setLastModifiedBy(securityUtilService.getUserId());
        save(storedDocument);
    }

    public List<StoredDocument> findAllExpiredStoredDocuments() {
        return storedDocumentRepository.findByTtlLessThanAndHardDeleted(new Date(), false);
    }

    private void storeInAzureBlobStorage(StoredDocument storedDocument,
                                         DocumentContentVersion documentContentVersion,
                                         MultipartFile file) {
        if (azureStorageConfiguration.isAzureBlobStoreEnabled()) {
            blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
                documentContentVersion,
                file);
        }
    }

    /**
     * Force closure of the persisted <code>Blob</code>'s <code>InputStream</code>, to ensure the file handle is
     * released.
     *
     * @param documentContentVersion <code>DocumentContentVersion</code> instance wrapping a
     *                               <code>DocumentContent</code> that contains the <code>Blob</code>
     * @throws HibernateException If the <code>Blob</code>'s stream cannot be accessed
     * @throws UncheckedIOException If the stream cannot be closed
     */
    private void closeBlobInputStream(@NotNull final DocumentContentVersion documentContentVersion) {
        try {
            if (documentContentVersion.getDocumentContent() != null) {
                documentContentVersion.getDocumentContent().getData().getBinaryStream().close();
            }
        } catch (SQLException e) {
            throw new HibernateException("Unable to access blob stream", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to close blob stream", e);
        }
    }
}
