package uk.gov.hmcts.dm.service;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class StoredDocumentService {

    private static final Logger log = LoggerFactory.getLogger(StoredDocumentService.class);

    private final StoredDocumentRepository storedDocumentRepository;

    private final DocumentContentVersionRepository documentContentVersionRepository;

    private final ToggleConfiguration toggleConfiguration;

    private final SecurityUtilService securityUtilService;

    private final BlobStorageWriteService blobStorageWriteService;

    private final BlobStorageDeleteService blobStorageDeleteService;

    @Value("${spring.batch.caseDocumentsDeletion.batchSize}")
    private int batchSize;

    @Value("${spring.batch.caseDocumentsDeletion.noOfIterations}")
    private int noOfIterations;

    @Value("${spring.batch.caseDocumentsDeletion.threadLimit}")
    private int threadLimit;

    @Autowired
    public StoredDocumentService(StoredDocumentRepository storedDocumentRepository,
                                 DocumentContentVersionRepository documentContentVersionRepository,
                                 ToggleConfiguration toggleConfiguration,
                                 SecurityUtilService securityUtilService,
                                 BlobStorageWriteService blobStorageWriteService,
                                 BlobStorageDeleteService blobStorageDeleteService) {
        this.storedDocumentRepository = storedDocumentRepository;
        this.documentContentVersionRepository = documentContentVersionRepository;
        this.toggleConfiguration = toggleConfiguration;
        this.securityUtilService = securityUtilService;
        this.blobStorageWriteService = blobStorageWriteService;
        this.blobStorageDeleteService = blobStorageDeleteService;
    }

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


    public List<StoredDocument> saveItems(UploadDocumentsCommand uploadDocumentsCommand) {
        String userId = securityUtilService.getUserId();
        log.info(
            "Save items for userID:{}, service name:{}",
            userId,
            securityUtilService.getCurrentlyAuthenticatedServiceName()
        );
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
            document.setTtl(uploadDocumentsCommand.getTtl());

            DocumentContentVersion documentContentVersion = new DocumentContentVersion(document,
                                                                                       file,
                                                                                       userId);
            document.getDocumentContentVersions().add(documentContentVersion);
            save(document);
            storeInAzureBlobStorage(document, documentContentVersion, file);
            return document;
        }).toList();
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
                                                                                   securityUtilService.getUserId());
        storedDocument.getDocumentContentVersions().add(documentContentVersion);
        documentContentVersionRepository.save(documentContentVersion);
        storeInAzureBlobStorage(storedDocument, documentContentVersion, file);

        return documentContentVersion;
    }

    public void deleteDocument(StoredDocument storedDocument, boolean permanent) {
        storedDocument.setDeleted(true);
        if (permanent) {
            storedDocument.setHardDeleted(true);
            storedDocument.getDocumentContentVersions()
                .parallelStream()
                .forEach(blobStorageDeleteService::deleteDocumentContentVersion);
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
            Map<String, String> newMetaData = new HashMap<>();
            metadata.forEach((key, val) -> {
                if (!storedDocument.getMetadata().containsKey(key)) {
                    newMetaData.put(key, val);
                } else {
                    log.info("Docuemnt Id with : {} has existing value for : {} as : {}. And new value is : "
                            + "{}.", storedDocument.getId(), key,
                        storedDocument.getMetadata().get(key), val);
                }
            });
            storedDocument.getMetadata().putAll(newMetaData);
        }

        storedDocument.setLastModifiedBy(securityUtilService.getUserId());
        save(storedDocument);
    }

    public List<StoredDocument> findAllExpiredStoredDocuments() {
        return storedDocumentRepository.findByTtlLessThanAndHardDeleted(new Date(), false);
    }

    /**
     * This method will delete the Case Documents marked for hard deletion.
     * It will delete the document Binary from the blob storage and delete the related rows from the database
     * like the DocumentContentVersions and the Audit related Entries.
     */
    public void deleteCaseDocuments() {

        for (int i = 0; i < noOfIterations; i++) {
            StopWatch iterationStopWatch = new StopWatch();
            iterationStopWatch.start();

            StopWatch dbGetQueryStopWatch = new StopWatch();
            dbGetQueryStopWatch.start();

            List<StoredDocument> storedDocuments = storedDocumentRepository.findCaseDocumentsForDeletion(batchSize);

            dbGetQueryStopWatch.stop();
            log.info("Time taken to get {} rows from DB : {} ms", storedDocuments.size(),
                    dbGetQueryStopWatch.getDuration().toMillis());

            if (CollectionUtils.isEmpty(storedDocuments)) {
                iterationStopWatch.stop();
                log.info("Time taken to complete iteration number :  {} was : {} ms", i,
                        iterationStopWatch.getDuration().toMillis());
                break;
            }

            try (ExecutorService executorService = Executors.newFixedThreadPool(threadLimit)) {
                storedDocuments.forEach(
                        storedDocument -> executorService.submit(() -> deleteDocumentDetails(storedDocument))
                );
            }
            iterationStopWatch.stop();
            log.info("Time taken to complete iteration number :  {} was : {} ms", i,
                    iterationStopWatch.getDuration().toMillis());

        }
    }

    private void storeInAzureBlobStorage(StoredDocument storedDocument,
                                         DocumentContentVersion documentContentVersion,
                                         MultipartFile file) {
        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
            documentContentVersion,
            file);
    }

    /**
     * This method will delete the Case Documents marked for hard deletion.
     * It will delete the document Binary from the blob storage and delete the related rows from the database
     * like the DocumentContentVersions and the Audit related Entries.
     */
    private void deleteDocumentDetails(StoredDocument storedDocument) {

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            storedDocument.getDocumentContentVersions()
                    .parallelStream()
                    .filter(Objects::nonNull)
                    .forEach(blobStorageDeleteService::deleteDocumentContentVersion);
            storedDocumentRepository.delete(storedDocument);

            stopWatch.stop();
            log.info("Deletion of StoredDocument with Id: {} took {} ms",
                    storedDocument.getId(), stopWatch.getDuration().toMillis());

        } catch (Exception e) {
            log.error("Error while deleting the document with Id : {}. Error message : {}",
                    storedDocument.getId(), e.getMessage());
        }

    }
}
