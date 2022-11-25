package uk.gov.hmcts.dm.config.batch;

import org.hibernate.LockOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.dm.domain.StoredDocument;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

@EnableBatchProcessing
@EnableScheduling
@Configuration
@ConditionalOnProperty("toggle.ttl")
public class BatchConfiguration {

    private final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private DeleteExpiredDocumentsProcessor deleteExpiredDocumentsProcessor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.batch.historicExecutionsRetentionMilliseconds}")
    private int historicExecutionsRetentionMilliseconds;

    @Value("${spring.batch.deleteThreadCount}")
    private int deleteThreadCount;

    @Value("${spring.batch.deleteExecutorQueueCapacity}")
    private int deleteExecutorQueueCapacity;

    @Scheduled(fixedRateString = "${spring.batch.document-task-milliseconds}")
    public void schedule() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        log.info("start deletejob");
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
            .pageSize(500)
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
            .taskExecutor(taskExecutor())
            .build();

    }

    private ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("del_w_ttl-");
        taskExecutor.setCorePoolSize(deleteThreadCount);
        taskExecutor.setMaxPoolSize(deleteThreadCount);
        taskExecutor.setQueueCapacity(deleteExecutorQueueCapacity);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.info("Delete execution rejected");
                super.rejectedExecution(r, e);
            }
        });
        taskExecutor.setDaemon(true);
        taskExecutor.initialize();
        return taskExecutor;
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
                            + "where d.hardDeleted = false AND d.ttl < current_timestamp() order by random()")
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("javax.persistence.lock.timeout", LockOptions.SKIP_LOCKED)
                .setMaxResults(500);
        }

        @Override
        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
    }
}
