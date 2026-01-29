package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Reads a CSV/line-delimited spreadsheet of StoredDocument UUIDs and "recovers" them:
 * - sets deleted = false and hardDeleted = false on StoredDocument
 * - recalculates contentUri for each DocumentContentVersion and saves it
 * - logs creation of RECOVERED audit action
 */
@Service
public class CaseDocumentsRecoveryTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CaseDocumentsRecoveryTask.class);

    private final BlobContainerClient blobClient;
    private final StoredDocumentRepository storedDocumentRepository;
    private final AuditEntryService auditEntryService;

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    @Value("${spring.batch.caseDocumentsRecovery.blobPath}")
    private String blobPath;

    public CaseDocumentsRecoveryTask(@Qualifier("recoveredDocumentsStorage") BlobContainerClient blobClient,
                                     StoredDocumentRepository storedDocumentRepository,
                                     AuditEntryService auditEntryService) {
        this.blobClient = blobClient;
        this.storedDocumentRepository = storedDocumentRepository;
        this.auditEntryService = auditEntryService;
    }

    @Override
    public void run() {
        log.info("Started Recovery job");

        Optional<BlobClient> blob = blobClient.listBlobs()
                .stream()
                .map(blobItem -> blobClient.getBlobClient(blobItem.getName()))
                .findAny();


        blob.ifPresent(this::processItem);

        log.info("Completed Recovery job");
    }

    private void processItem(BlobClient client) {

        var blobName = client.getBlobName();
        log.info("ProcessItem of Recovered Documents started for : {} ", blobName);
        try {

            Set<UUID> documentIds = getCsvFileAndParse(client);

            if (documentIds == null || documentIds.isEmpty()) {
                log.info("No recovered item found in the file to processed in {}", client.getBlobUrl());
                return;
            }

            documentIds.forEach(id -> {

                try {
                    Optional<StoredDocument> sdOpt = storedDocumentRepository.findById(id);
                    if (sdOpt.isEmpty()) {
                        log.warn("StoredDocument not found for id: {}", id);
                        return;
                    }

                    StoredDocument storedDocument = sdOpt.get();
                    storedDocument.setDeleted(false);
                    storedDocument.setHardDeleted(false);

                    var versions = storedDocument.getDocumentContentVersions();
                    if (CollectionUtils.isNotEmpty(versions)) {
                        storedDocument.getDocumentContentVersions()
                                .parallelStream()
                                .forEach(this::calculateContentUri);
                    } else {
                        log.info("No content versions found for documentId={}", storedDocument.getId());
                        return; //Nothing to update.
                    }

                    storedDocumentRepository.save(storedDocument);
                    auditEntryService.createAndSaveEntry(storedDocument, AuditActions.RECOVERED);

                } catch (Exception e) {
                    log.error("Failed to recover document {} : {}", id, e.getMessage(), e);
                }
            });

            log.info("Number of Recovered Documents: {} csv file name {} ",
                    documentIds.size(),
                    client.getBlobName()
            );


        } catch (Exception ex) {
            log.error("ProcessItem of Recovered Documents failed for : {} ", client.getBlobName(), ex);
        } finally {
            client.delete();
        }

    }

    private Set<UUID> getCsvFileAndParse(BlobClient client) {
        String csvPath = TMP_DIR + File.separatorChar + "recovered-documents.csv";
        try {

            client.downloadToFile(csvPath);

            return getUuids(csvPath);

        } catch (Exception ex) {
            log.info("File processing failed", ex);

        } finally {
            try {
                Files.delete(Paths.get(csvPath));
            } catch (Exception ex) {
                log.info("Deleting temp file failed, {} ", csvPath);
            }
        }
        return Collections.emptySet();
    }

    private static Set<UUID> getUuids(String csvPath) {
        try (Stream<String> stream = Files.lines(Paths.get(csvPath), StandardCharsets.UTF_8)) {
            return stream
                    .flatMap(line -> Arrays.stream(line.split(",")))
                    .map(getStringUuidFunction())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            log.info("File Parsing failed", e);
        }
        return Collections.emptySet();
    }

    private static Function<String, UUID> getStringUuidFunction() {
        return str -> {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                log.info("Skipping {} as it is not UUID", str);
                return null;
            }
        };
    }

    private void calculateContentUri(DocumentContentVersion documentContentVersion) {
        documentContentVersion
                .setContentUri(blobPath.concat(documentContentVersion.getId().toString()));

    }
}
