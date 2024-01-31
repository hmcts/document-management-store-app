package uk.gov.hmcts.dm.config.batch;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RemoveSpringBatchHistoryTasklet implements Tasklet {

    /**
     * SQL statements removing step and job executions compared to a given date.
     */
    private static final String SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT = "DELETE FROM %PREFIX%STEP_EXECUTION_CONTEXT "
        + "WHERE STEP_EXECUTION_ID IN (SELECT STEP_EXECUTION_ID FROM %PREFIX%STEP_EXECUTION WHERE"
        + " JOB_EXECUTION_ID IN   (SELECT JOB_EXECUTION_ID FROM  %PREFIX%JOB_EXECUTION where CREATE_TIME < ?))";

    private static final String SQL_DELETE_BATCH_STEP_EXECUTION = "DELETE FROM %PREFIX%STEP_EXECUTION "
        + "WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM %PREFIX%JOB_EXECUTION where CREATE_TIME < ?)";

    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT = "DELETE FROM %PREFIX%JOB_EXECUTION_CONTEXT"
        + " WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  %PREFIX%JOB_EXECUTION where CREATE_TIME < ?)";
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS = "DELETE FROM %PREFIX%JOB_EXECUTION_PARAMS "
        + "WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM %PREFIX%JOB_EXECUTION where CREATE_TIME < ?)";

    private static final String SQL_DELETE_BATCH_JOB_EXECUTION =
        "DELETE FROM %PREFIX%JOB_EXECUTION WHERE CREATE_TIME < ?";
    private static final String SQL_DELETE_BATCH_JOB_INSTANCE = "DELETE FROM %PREFIX%JOB_INSTANCE WHERE "
        + "JOB_INSTANCE_ID NOT IN (SELECT JOB_INSTANCE_ID FROM %PREFIX%JOB_EXECUTION)";

    /**
     * Default value for the table prefix property.
     */
    private static final String DEFAULT_TABLE_PREFIX = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

    /**
     * Default value for the data retention (in month).
     */
    private static final String tablePrefix = DEFAULT_TABLE_PREFIX;

    private final Integer historicRetentionMiliseconds;

    private final JdbcTemplate jdbcTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(RemoveSpringBatchHistoryTasklet.class);

    public RemoveSpringBatchHistoryTasklet(Integer historicRetentionMiliseconds, JdbcTemplate jdbcTemplate) {
        this.historicRetentionMiliseconds = historicRetentionMiliseconds;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int totalCount = 0;
        Date date = DateUtils.addMilliseconds(new Date(), -historicRetentionMiliseconds);
        DateFormat df = new SimpleDateFormat();
        LOG.info("Remove the Spring Batch history before the {}", df.format(date));

        int rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT), date);
        LOG.info("Deleted rows number from the BATCH_STEP_EXECUTION_CONTEXT table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_STEP_EXECUTION), date);
        LOG.info("Deleted rows number from the BATCH_STEP_EXECUTION table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT), date);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION_CONTEXT table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS), date);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION_PARAMS table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION), date);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_INSTANCE));
        LOG.info("Deleted rows number from the BATCH_JOB_INSTANCE table: {}", rowCount);
        totalCount += rowCount;

        contribution.incrementWriteCount(totalCount);

        return RepeatStatus.FINISHED;
    }

    protected String getQuery(String base) {
        return StringUtils.replace(base, "%PREFIX%", tablePrefix);
    }

}
