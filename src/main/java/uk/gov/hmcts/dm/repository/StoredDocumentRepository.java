package uk.gov.hmcts.dm.repository;


import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.UUID;

/**
 * Created by pawel on 23/05/2017.
 */
@Repository
public interface StoredDocumentRepository extends PagingAndSortingRepository<StoredDocument, UUID> {

    @Query("select s from StoredDocument s join s.metadata m where KEY(m) = :#{#metadataSearchCommand.name} and m = :#{#metadataSearchCommand.value}")
    Page<StoredDocument> findAllByMetadata(@NonNull @Param("metadataSearchCommand") MetadataSearchCommand metadataSearchCommand, @NonNull Pageable pageable);


    Page<StoredDocument> findByCreatedBy(String creator, @NonNull Pageable pageable);

}
