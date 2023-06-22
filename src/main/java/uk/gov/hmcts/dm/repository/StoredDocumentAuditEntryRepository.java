package uk.gov.hmcts.dm.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoredDocumentAuditEntryRepository extends PagingAndSortingRepository<StoredDocumentAuditEntry, UUID>, CrudRepository<StoredDocumentAuditEntry, UUID> {

    List<StoredDocumentAuditEntry> findByStoredDocumentOrderByRecordedDateTimeAsc(StoredDocument storedDocument);

}
