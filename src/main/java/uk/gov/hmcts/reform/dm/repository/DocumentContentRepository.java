package uk.gov.hmcts.reform.dm.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dm.domain.DocumentContent;

import java.util.UUID;

@Repository
public interface DocumentContentRepository extends PagingAndSortingRepository<DocumentContent, UUID> {


}
