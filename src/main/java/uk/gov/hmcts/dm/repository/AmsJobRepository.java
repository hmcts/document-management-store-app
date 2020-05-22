package uk.gov.hmcts.dm.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.AmsJob;

@Repository
public interface AmsJobRepository extends CrudRepository<AmsJob, Long> {
}
