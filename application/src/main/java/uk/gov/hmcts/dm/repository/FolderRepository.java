package uk.gov.hmcts.dm.repository;

import java.util.UUID;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import uk.gov.hmcts.dm.domain.Folder;

@Repository
public interface FolderRepository extends PagingAndSortingRepository<Folder, UUID> {

}
