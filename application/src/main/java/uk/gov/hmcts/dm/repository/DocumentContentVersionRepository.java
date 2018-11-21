package uk.gov.hmcts.dm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentContentVersionRepository extends PagingAndSortingRepository<DocumentContentVersion, UUID> {

    @Modifying(clearAutomatically = true)
    @Query("update DocumentContentVersion dcv set dcv.contentUri = :contentUri, dcv.contentChecksum = "
        + ":contentChecksum where dcv.id = :id")
    void updateContentUriAndContentCheckSum(@Param("id") UUID id,
                                            @Param("contentUri") String contentUri,
                                            @Param("contentChecksum") String contentCheckSum);

    Long countByContentChecksumIsNull();

    Long countByContentChecksumIsNotNull();

    List<DocumentContentVersion> findByContentChecksumIsNullAndDocumentContentIsNotNull(Pageable pageable);
}
