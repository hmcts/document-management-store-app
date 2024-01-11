package uk.gov.hmcts.dm.config.batch;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.orm.JpaQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import javax.sql.DataSource;

@EnableBatchProcessing
@EnableScheduling
@Configuration
@ConditionalOnProperty("toggle.ttl")
@EnableSchedulerLock(defaultLockAtMostFor = "PT2M")
public class BatchConfiguration {

    private final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);


    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

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

    @Value("${DOCUMENT_DELETE_PAGE_SIZE}")
    private int documentDeletePageSize;

    @Value("${DOCUMENT_DELETE_NUM_PAGES}")
    private int documentDeleteNumberPages;

    @Scheduled(cron = "${spring.batch.document-delete-task-cron}", zone = "Europe/London")
    @SchedulerLock(name = "DeleteDoc_scheduledTask",
        lockAtLeastFor = "PT3M", lockAtMostFor = "PT15M")
    public void schedule() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
        JobRestartException, JobInstanceAlreadyCompleteException {
        log.info("deleteJob starting");
        jobLauncher
            .run(processDocument(step1()), new JobParametersBuilder()
                .addDate("date", new Date())
                .toJobParameters());
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
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
            .pageSize(documentDeletePageSize)
            .maxItemCount(documentDeleteNumberPages)
            .build();
    }

    public JpaItemWriter itemWriter() {
        JpaItemWriter writer = new JpaItemWriter<StoredDocument>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    public Job processDocument(Step step1) {
        return new JobBuilder("processDocumentJob", jobRepository)
            .flow(step1)
            .end()
            .build();
    }

    public Step step1() {
        return new StepBuilder("step1",jobRepository)
            .<StoredDocument, StoredDocument>chunk(30, transactionManager)
            .reader(undeletedDocumentsWithTtl())
            .processor(deleteExpiredDocumentsProcessor)
            .writer(itemWriter())
            .taskExecutor(taskExecutor())
            .build();

    }

    private ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("del_w_ttl-" + ThreadLocalRandom.current().nextInt(1, 100) + "-");
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
        return new JobBuilder("clearHistoricBatchExecutions", jobRepository)
            .flow(new StepBuilder("deleteAllExpiredBatchExecutions",jobRepository)
                .tasklet(new RemoveSpringBatchHistoryTasklet(
                    historicExecutionsRetentionMilliseconds, jdbcTemplate), transactionManager)
                .build()).build().build();
    }

    private class QueryProvider implements JpaQueryProvider {
        private EntityManager entityManager;

        @Override
        public Query createQuery() {
            return entityManager
                .createQuery("select d from StoredDocument d JOIN FETCH d.documentContentVersions "
                            + "where d.hardDeleted = false AND d.ttl < current_timestamp() order by ttl asc")
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("jakarta.persistence.lock.timeout", LockMode.UPGRADE_SKIPLOCKED)
                .setMaxResults(400);
        }

        @Override
        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
    }
}
