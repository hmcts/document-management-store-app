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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.hmcts.dm.domain.AmsJob;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.Date;

@EnableBatchProcessing
@EnableScheduling
@Configuration
public class AmsJobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    @Autowired
    public JobLauncher jobLauncher;

    @Autowired
    public AmsJobProcessor amsJobProcessor;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelay = 60000)
    public void scheduleDocumentMetaDataUpdate() throws JobParametersInvalidException,
        JobExecutionAlreadyRunningException,
        JobRestartException,
        JobInstanceAlreadyCompleteException {

        jobLauncher.run(processAmsJob(step1()), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());
    }

    public JpaPagingItemReader getInProgressAmsJob() {
        return new JpaPagingItemReaderBuilder<AmsJob>()
            .name("amsJobReader")
            .entityManagerFactory(entityManagerFactory)
            .queryProvider(new AmsJobConfiguration.QueryProvider())
            .pageSize(100)
            .build();
    }

    @Bean
    public JpaItemWriter itemWriter() {
        JpaItemWriter writer = new JpaItemWriter<AmsJob>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job processAmsJob(Step step1) {
        return jobBuilderFactory.get("processAmsJob")
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
            .<AmsJob, AmsJob>chunk(10)
            .reader(getInProgressAmsJob())
            .processor(amsJobProcessor)
            .writer(itemWriter())
            .build();

    }

    private class QueryProvider implements JpaQueryProvider {
        private EntityManager entityManager;

        @Override
        public Query createQuery() {
            return entityManager
                .createQuery("select aj from AmsJob aj "
                    + "where aj.jobStatus = 'IN_PROGRESS' ")
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("javax.persistence.lock.timeout", LockOptions.SKIP_LOCKED);
        }

        @Override
        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
    }

}
