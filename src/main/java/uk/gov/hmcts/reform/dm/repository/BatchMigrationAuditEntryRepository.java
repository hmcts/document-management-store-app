package uk.gov.hmcts.reform.dm.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.dm.domain.BatchMigrationAuditEntry;

public interface BatchMigrationAuditEntryRepository extends CrudRepository<BatchMigrationAuditEntry, Long> {
}
