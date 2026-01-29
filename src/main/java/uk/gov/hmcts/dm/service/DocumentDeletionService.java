package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DocumentDeletionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDeletionService.class);

    private final JdbcTemplate jdbcTemplate;
    private final int batchSize = 25000;

    public DocumentDeletionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void runBatchedDeletion(LocalDate cutoffDate) {

        int updated = deleteOneBatch(cutoffDate);
        // optional: log progress or throttle with Thread.sleep(...)
        LOGGER.info("Deleted batch, rows affected: {} ", updated);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteOneBatch(LocalDate cutoffDate) {
        String sql = """
                update storeddocument set ttl = createdon, deleted = true, harddeleted = false,
                lastmodifiedbyservice='ccd_case_disposer'
                where id in (select id from storeddocument where cast(createdon as DATE) <= ?
                and deleted = false order by createdon asc limit ?)
                """;
        return jdbcTemplate.update(sql, java.sql.Date.valueOf(cutoffDate), batchSize);
    }
}
