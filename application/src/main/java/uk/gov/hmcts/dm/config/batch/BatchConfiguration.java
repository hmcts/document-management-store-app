package uk.gov.hmcts.dm.config.batch;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SchedulerLock;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.dm.domain.StoredDocument;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Date;

@EnableBatchProcessing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
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

    @Autowired
    @Qualifier("transactionManager") PlatformTransactionManager transactionManager;

    @Value("${spring.batch.historicExecutionsRetentionMilliseconds}")
    int historicExecutionsRetentionMilliseconds;

    @Scheduled(fixedRateString = "${spring.batch.document-task-milliseconds}")
    @SchedulerLock(name = "${task.env}")
    public void schedule() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher
            .run(processDocument(step1()), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());

    }

    @Scheduled(fixedDelayString = "${spring.batch.historicExecutionsRetentionMilliseconds}")
    @SchedulerLock(name = "${task.env}-historicExecutionsRetention")
    public void scheduleCleanup() throws JobParametersInvalidException,
        JobExecutionAlreadyRunningException,
        JobRestartException,
        JobInstanceAlreadyCompleteException {

        jobLauncher.run(clearHistoryData(), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());

    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    @Bean
    public JpaPagingItemReader undeletedDocumentsWithTtl() {
        return new JpaPagingItemReaderBuilder<StoredDocument>()
            .name("documentTaskReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("select d from StoredDocument d JOIN FETCH d.documentContentVersions "
                + "where d.hardDeleted = false AND d.ttl < current_timestamp()")
            .pageSize(100)
            .build();
    }


    @Bean
    public JpaItemWriter itemWriter() {
        JpaItemWriter writer = new JpaItemWriter<StoredDocument>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job processDocument(Step step1) {
        return jobBuilderFactory.get("processDocumentJob")
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
            .transactionManager(transactionManager)
            .<StoredDocument, StoredDocument>chunk(10)
            .reader(undeletedDocumentsWithTtl())
            .processor(deleteExpiredDocumentsProcessor)
            .writer(itemWriter())
            .build();

    }

    @Bean
    public Job clearHistoryData() {
        return jobBuilderFactory.get("clearHistoricBatchExecutions")
            .flow(stepBuilderFactory.get("deleteAllExpiredBatchExecutions")
                .transactionManager(transactionManager)
                .tasklet(new RemoveSpringBatchHistoryTasklet(historicExecutionsRetentionMilliseconds, jdbcTemplate))
                .build()).build().build();
    }


}
