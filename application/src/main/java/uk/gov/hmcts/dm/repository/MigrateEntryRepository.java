package uk.gov.hmcts.dm.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.dm.domain.MigrateEntry;

@Repository
public interface MigrateEntryRepository extends JpaRepository<MigrateEntry, UUID> {
}
