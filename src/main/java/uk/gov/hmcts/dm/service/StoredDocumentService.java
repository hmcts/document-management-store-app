package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.*;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.FolderRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.List;
import java.util.Map;
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
    private BlobCreator blobCreator;

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

    public List<StoredDocument> saveDocuments(List<MultipartFile> files,
                                              Classifications classification,
                                              List<String> roles,
                                              Map<String, String > metadata)  {
        return files.stream().map(file -> {
            StoredDocument document = new StoredDocument();
            document.setClassification(classification);
            document.setRoles(roles != null ? roles.stream().collect(Collectors.toSet()) : null);
            document.setMetadata(metadata);
            document.getDocumentContentVersions().add(new DocumentContentVersion(document, file, blobCreator.createBlob(file)));
            save(document);
            return document;
        }).collect(Collectors.toList());

    }

    public List<StoredDocument> saveDocuments(List<MultipartFile> files)  {
        return saveDocuments(files, null, null, null);
    }

    public DocumentContentVersion addStoredDocumentVersion(StoredDocument storedDocument, MultipartFile file)  {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion(storedDocument, file, blobCreator.createBlob(file));
        documentContentVersionRepository.save(documentContentVersion);
        storedDocument.getDocumentContentVersions().add(documentContentVersion);
        return documentContentVersion;
    }


    public void deleteDocument(StoredDocument storedDocument) {
        if (storedDocument != null) {
            storedDocument.setDeleted(true);
            storedDocumentRepository.save(storedDocument);
        }
    }

    public void hardDeleteDocument(StoredDocument storedDocument) {
        if (storedDocument != null) {
            deleteDocument(storedDocument);
            storedDocument.hardDelete();
            storedDocumentRepository.save(storedDocument);
        }
    }
}
