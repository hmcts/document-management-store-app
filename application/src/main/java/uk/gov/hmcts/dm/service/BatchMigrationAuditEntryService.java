package uk.gov.hmcts.dm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.BatchMigrationAuditEntry;
import uk.gov.hmcts.dm.repository.BatchMigrationAuditEntryRepository;

@Service
@Slf4j
class BatchMigrationAuditEntryService {

    private final BatchMigrationAuditEntryRepository batchMigrationAuditEntryRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    BatchMigrationAuditEntryService(BatchMigrationAuditEntryRepository batchMigrationAuditEntryRepository) {
        this.batchMigrationAuditEntryRepository = batchMigrationAuditEntryRepository;
    }

    BatchMigrationAuditEntry createAuditEntry(final String authToken, final Integer batchSize, final Boolean mockRun) {
        return batchMigrationAuditEntryRepository.save(new BatchMigrationAuditEntry(authToken, batchSize, mockRun));
    }

    void save(final BatchMigrationAuditEntry audit, final BatchMigrateProgressReport report) {
        try {
            audit.setStatusReport(MAPPER.writeValueAsString(report));
            batchMigrationAuditEntryRepository.save(audit);
        } catch (JsonProcessingException e) {
            log.error("Exception caught whilst saving an audit entry", e);
        }
    }
}
