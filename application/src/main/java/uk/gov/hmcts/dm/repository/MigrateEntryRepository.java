package uk.gov.hmcts.dm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.MigrateEntry;

import java.util.UUID;

@Repository
public interface MigrateEntryRepository extends JpaRepository<MigrateEntry, UUID> {
}
