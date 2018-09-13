package uk.gov.hmcts.dm.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.util.UUID;

/**
 * Created by pawel on 22/05/2017.
 */
@Repository
public interface DocumentContentVersionRepository extends PagingAndSortingRepository<DocumentContentVersion, UUID> {

    @Modifying(clearAutomatically = true)
    @Query("update DocumentContentVersion dcv set dcv.contentUri = :contentUri where dcv.id = :id")
    void update(@Param("id") UUID id, @Param("contentUri") String contentUri);
}
