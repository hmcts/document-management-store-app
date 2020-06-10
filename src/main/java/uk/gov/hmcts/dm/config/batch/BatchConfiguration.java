package uk.gov.hmcts.dm.config.batch;

import org.hibernate.LockOptions;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.orm.JpaQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.hmcts.dm.domain.StoredDocument;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.Date;

@EnableBatchProcessing
@EnableScheduling
@Configuration
@ConditionalOnProperty("toggle.ttl")
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    @Autowired
    public JobLauncher jobLauncher;

    @Autowired
    public DeleteExpiredDocumentsProcessor deleteExpiredDocumentsProcessor;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${spring.batch.historicExecutionsRetentionMilliseconds}")
    int historicExecutionsRetentionMilliseconds;

    @Scheduled(fixedRateString = "${spring.batch.document-task-milliseconds}")
    public void schedule() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher
            .run(processDocument(step1()), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());

    }

    @Scheduled(fixedDelayString = "${spring.batch.historicExecutionsRetentionMilliseconds}")
    public void scheduleCleanup() throws JobParametersInvalidException,
        JobExecutionAlreadyRunningException,
        JobRestartException,
        JobInstanceAlreadyCompleteException {

        jobLauncher.run(clearHistoryData(), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());

    }

    public JpaPagingItemReader undeletedDocumentsWithTtl() {
        return new JpaPagingItemReaderBuilder<StoredDocument>()
            .name("documentTaskReader")
            .entityManagerFactory(entityManagerFactory)
            .queryProvider(new QueryProvider())
            .pageSize(100)
            .build();
    }

    public JpaItemWriter itemWriter() {
        JpaItemWriter writer = new JpaItemWriter<StoredDocument>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    public Job processDocument(Step step1) {
        return jobBuilderFactory.get("processDocumentJob")
            .flow(step1)
            .end()
            .build();
    }

    public Step step1() {
        return stepBuilderFactory.get("step1")
            .<StoredDocument, StoredDocument>chunk(10)
            .reader(undeletedDocumentsWithTtl())
            .processor(deleteExpiredDocumentsProcessor)
            .writer(itemWriter())
            .taskExecutor(new SimpleAsyncTaskExecutor("del_w_ttl"))
            .build();

    }

    public Job clearHistoryData() {
        return jobBuilderFactory.get("clearHistoricBatchExecutions")
            .flow(stepBuilderFactory.get("deleteAllExpiredBatchExecutions")
                .tasklet(new RemoveSpringBatchHistoryTasklet(historicExecutionsRetentionMilliseconds, jdbcTemplate))
                .build()).build().build();
    }

    private class QueryProvider implements JpaQueryProvider {
        private EntityManager entityManager;

        @Override
        public Query createQuery() {
            return entityManager
                .createQuery("select d from StoredDocument d JOIN FETCH d.documentContentVersions "
                            + "where d.hardDeleted = false AND d.ttl < current_timestamp()")
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("javax.persistence.lock.timeout", LockOptions.SKIP_LOCKED);
        }

        @Override
        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
    }
}
