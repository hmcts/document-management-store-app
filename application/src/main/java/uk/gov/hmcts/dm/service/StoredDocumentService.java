package uk.gov.hmcts.dm.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Optional<StoredDocument> findOne(UUID id) {
        Optional<StoredDocument> storedDocument = Optional.ofNullable(storedDocumentRepository.findOne(id));
        if (storedDocument.isPresent() && storedDocument.get().isDeleted()) {
            return Optional.empty();
        }
        return storedDocument;
    }

    public Optional<StoredDocument> findOneWithBinaryData(UUID id) {
        Optional<StoredDocument> storedDocument = Optional.ofNullable(storedDocumentRepository.findOne(id));
        if (storedDocument.isPresent() && storedDocument.get().isHardDeleted()) {
            return Optional.empty();
        }
        return storedDocument;
    }

    public void save(StoredDocument storedDocument) {
        storedDocumentRepository.save(storedDocument);
    }

    public void saveItemsToBucket(Folder folder, List<MultipartFile> files)  {
        String userId = securityUtilService.getUserId();
        List<StoredDocument> items = files.stream().map(aFile -> {
            StoredDocument storedDocument = new StoredDocument();
            storedDocument.setFolder(folder);
            storedDocument.setCreatedBy(userId);
            storedDocument.setLastModifiedBy(userId);
            storedDocument.getDocumentContentVersions().add(new DocumentContentVersion(storedDocument, aFile, userId));
            storedDocumentRepository.save(storedDocument);
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
            document.getDocumentContentVersions().add(new DocumentContentVersion(document, file, userId));
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
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument, file, userId);
        documentContentVersionRepository.save(documentContentVersion);
        storedDocument.getDocumentContentVersions().add(documentContentVersion);
        return documentContentVersion;
    }

    public void deleteDocument(StoredDocument storedDocument, boolean permanent) {
        storedDocument.setDeleted(true);
        if (permanent) {
            storedDocument.setHardDeleted(true);
            storedDocument.getDocumentContentVersions().forEach(documentContentVersion -> {
                documentContentRepository.delete(documentContentVersion.getDocumentContent());
                documentContentVersion.setDocumentContent(null);
            });
        }
        storedDocumentRepository.save(storedDocument);
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
}
