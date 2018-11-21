package uk.gov.hmcts.dm.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.dm.domain.BatchMigrationAuditEntry;

public interface BatchMigrationAuditEntryRepository extends CrudRepository<BatchMigrationAuditEntry, Long> {
}
