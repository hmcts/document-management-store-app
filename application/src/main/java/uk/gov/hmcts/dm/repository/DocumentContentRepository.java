package uk.gov.hmcts.dm.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.DocumentContent;

import java.util.UUID;

@Repository
public interface DocumentContentRepository extends PagingAndSortingRepository<DocumentContent, UUID> {


}
