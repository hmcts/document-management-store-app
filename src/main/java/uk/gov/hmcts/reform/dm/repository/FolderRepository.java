package uk.gov.hmcts.reform.dm.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dm.domain.Folder;
import java.util.UUID;

@Repository
public interface FolderRepository extends PagingAndSortingRepository<Folder, UUID> {

}
