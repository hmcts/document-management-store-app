package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobContainerClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

import java.util.Date;
import javax.sql.DataSource;

@EnableBatchProcessing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
@Configuration
@ConditionalOnProperty("toggle.orphandocumentdeletion")
public class OrphanDocumentDeletionBatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    public JobLauncher jobLauncher;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("orphandocument-storage")
    private BlobContainerClient blobClient;

    @Autowired
    private StoredDocumentService storedDocumentService;

    @Autowired
    private AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @Scheduled(cron = "${spring.batch.orphanFileDeletionCronJobSchedule}")
    @SchedulerLock(name = "${task.env}-orphanDocumentDeletion")
    public void scheduleDocumentMetaDataUpdate() throws JobParametersInvalidException,
        JobExecutionAlreadyRunningException,
        JobRestartException,
        JobInstanceAlreadyCompleteException {

        jobLauncher.run(orphanDocumentDeletionJob(), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    public Job orphanDocumentDeletionJob() {
        return new JobBuilder("orphanDocumentDeletionJob", jobRepository)
            .flow(new StepBuilder("orphanDocumentDeletionJob",jobRepository)
                .tasklet(new OrphanDocumentDeletionTasklet(blobClient, storedDocumentService,
                    auditedStoredDocumentBatchOperationsService),transactionManager)
                .build()).build().build();
    }
}
