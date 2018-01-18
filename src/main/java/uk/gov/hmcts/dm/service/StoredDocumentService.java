package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.*;
import uk.gov.hmcts.dm.repository.DocumentContentRepository;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by pawel on 26/05/2017.
 */
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
    private BlobCreator blobCreator;

    @Autowired
    private ToggleConfiguration toggleConfiguration;

    public StoredDocument findOne(UUID id) {
        return storedDocumentRepository.findOne(id);
    }

    public void save(StoredDocument storedDocument) {
        storedDocumentRepository.save(storedDocument);
    }

    public void saveItemsToBucket(Folder folder, List<MultipartFile> files)  {
        List<StoredDocument> items = files.stream().map(aFile -> {
            StoredDocument storedDocument = new StoredDocument();
            storedDocument.setFolder(folder);
            storedDocument.getDocumentContentVersions().add(new DocumentContentVersion(storedDocument, aFile, blobCreator.createBlob(aFile)));
            storedDocumentRepository.save(storedDocument);
            return storedDocument;

        }).collect(Collectors.toList());

        folder.getStoredDocuments().addAll(items);

        folderRepository.save(folder);
    }

    public List<StoredDocument> saveItems(UploadDocumentsCommand uploadDocumentsCommand)  {
        return uploadDocumentsCommand.getFiles().stream().map(file -> {
            StoredDocument document = new StoredDocument();
            document.setClassification(uploadDocumentsCommand.getClassification());
            document.setRoles(uploadDocumentsCommand.getRoles() != null
                ? uploadDocumentsCommand.getRoles().stream().collect(Collectors.toSet()) : null);

            if (toggleConfiguration.getMetadatasearchendpoint()) {
                document.setMetadata(uploadDocumentsCommand.getMetadata());
            }
            if (toggleConfiguration.getTtl()) {
                document.setTtl(uploadDocumentsCommand.getTtl());
            }
            document.getDocumentContentVersions().add(new DocumentContentVersion(document, file, blobCreator.createBlob(file)));
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
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument, file, blobCreator.createBlob(file));
        documentContentVersionRepository.save(documentContentVersion);
        storedDocument.getDocumentContentVersions().add(documentContentVersion);
        return documentContentVersion;
    }

    public void deleteDocument(StoredDocument storedDocument, boolean permanent) {
        storedDocument.setDeleted(true);
        if (permanent) {
            storedDocument.getDocumentContentVersions().forEach(documentContentVersion -> {
                documentContentRepository.delete(documentContentVersion.getDocumentContent());
                documentContentVersion.setDocumentContent(null);
            });
        }
        storedDocumentRepository.save(storedDocument);
    }
}
