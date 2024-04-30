package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.dm.service.Constants.FALSE;

@Transactional
@Service
public class AuditedStoredDocumentOperationsService {

    private final StoredDocumentService storedDocumentService;

    private final AuditEntryService auditEntryService;

    @Autowired
    public AuditedStoredDocumentOperationsService(StoredDocumentService storedDocumentService,
                                                  AuditEntryService auditEntryService) {
        this.storedDocumentService = storedDocumentService;
        this.auditEntryService = auditEntryService;
    }

    public List<StoredDocument> createStoredDocuments(UploadDocumentsCommand uploadDocumentsCommand) {
        List<StoredDocument> storedDocuments = storedDocumentService.saveItems(uploadDocumentsCommand);
        storedDocuments.forEach(storedDocument -> {
            auditEntryService.createAndSaveEntry(storedDocument, AuditActions.CREATED);
            auditEntryService.createAndSaveEntry(storedDocument.getDocumentContentVersions().get(0),
                AuditActions.CREATED);
        });
        return storedDocuments;
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'READ')")
    public StoredDocument readStoredDocument(UUID id) {
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(id);
        return storedDocument.map(this::createReadAuditEntry).orElse(null);
    }

    @PreAuthorize("hasPermission(#storedDocument, 'READ')")
    private StoredDocument createReadAuditEntry(StoredDocument storedDocument) {
        auditEntryService.createAndSaveEntry(storedDocument, AuditActions.READ);
        return storedDocument;
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'UPDATE')")
    public StoredDocument updateDocument(UUID id, UpdateDocumentCommand updateDocumentCommand) {

        StoredDocument storedDocument = storedDocumentService.findOne(id)
            .orElseThrow(() -> new StoredDocumentNotFoundException(id));

        storedDocumentService.updateStoredDocument(storedDocument, updateDocumentCommand);

        auditEntryService.createAndSaveEntry(storedDocument, AuditActions.UPDATED);

        return storedDocument;
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'UPDATE')")
    public StoredDocument updateDocument(UUID id, Map<String, String> metadata, Date ttl) {
        StoredDocument storedDocument = storedDocumentService.findOne(id)
            .orElseThrow(() -> new StoredDocumentNotFoundException(id));
        storedDocumentService.updateStoredDocument(storedDocument, ttl, metadata);
        auditEntryService.createAndSaveEntry(storedDocument, AuditActions.UPDATED);

        return storedDocument;
    }

    @PreAuthorize("hasPermission(#storedDocument, 'UPDATE')")
    public DocumentContentVersion addDocumentVersion(StoredDocument storedDocument, MultipartFile file) {
        DocumentContentVersion documentContentVersion =
            storedDocumentService.addStoredDocumentVersion(storedDocument, file);
        auditEntryService.createAndSaveEntry(storedDocument, AuditActions.UPDATED);
        auditEntryService.createAndSaveEntry(documentContentVersion, AuditActions.CREATED);

        return documentContentVersion;
    }

    @PreAuthorize("hasPermission(#id, 'uk.gov.hmcts.dm.domain.StoredDocument', 'DELETE')")
    public void deleteStoredDocument(UUID id, boolean permanent) {
        Optional<StoredDocument> storedDocument = storedDocumentService.findOne(id);
        storedDocument.ifPresent(storedDoc -> deleteStoredDocument(storedDoc, permanent));
    }

    @PreAuthorize("hasPermission(#storedDocument, 'DELETE')")
    public void deleteStoredDocument(StoredDocument storedDocument, boolean permanent) {
        if (permanent && !storedDocument.isHardDeleted()) {
            storedDocumentService.deleteDocument(storedDocument, permanent);
            auditEntryService.createAndSaveEntry(storedDocument, AuditActions.HARD_DELETED);
        } else if (!permanent && !storedDocument.isDeleted()) {
            storedDocumentService.deleteDocument(storedDocument, permanent);
            auditEntryService.createAndSaveEntry(storedDocument, AuditActions.DELETED);
        }
    }

    public CaseDocumentsDeletionResults deleteCaseStoredDocuments(final List<StoredDocument> storedDocuments) {

        final AtomicInteger documentsMarkedForDeletion = new AtomicInteger(0);

        final CaseDocumentsDeletionResults caseDocumentsDeletionResults = new CaseDocumentsDeletionResults();
        caseDocumentsDeletionResults.setCaseDocumentsFound(storedDocuments.size());
        caseDocumentsDeletionResults.setMarkedForDeletion(documentsMarkedForDeletion.get());

        storedDocuments.forEach(storedDocument -> {
            storedDocument.setTtl(new Date());
            deleteStoredDocument(storedDocument, FALSE);
            caseDocumentsDeletionResults.setMarkedForDeletion(documentsMarkedForDeletion.incrementAndGet());
        });
        return caseDocumentsDeletionResults;
    }

}
