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
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.Date;
import javax.sql.DataSource;

@EnableBatchProcessing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
@Configuration
@ConditionalOnProperty("toggle.metadatamigration")
public class MetadataMigrationConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public JobLauncher jobLauncher;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("metadata-storage")
    private BlobContainerClient blobClient;

    @Autowired
    private StoredDocumentService storedDocumentService;

    @Scheduled(fixedDelayString = "${spring.batch.documentMetaDataUpdateMilliseconds}")
    @SchedulerLock(name = "${task.env}-documentMetaDataUpdate")
    public void scheduleDocumentMetaDataUpdate() throws JobParametersInvalidException,
        JobExecutionAlreadyRunningException,
        JobRestartException,
        JobInstanceAlreadyCompleteException {

        jobLauncher.run(updateDocumentMetaDataJob(), new JobParametersBuilder()
            .addDate("date", new Date())
            .toJobParameters());
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    public Job updateDocumentMetaDataJob() {
        return jobBuilderFactory.get("updateDocumentMetaDataJob")
            .flow(stepBuilderFactory.get("updateDocumentMetaDataStep")
                .tasklet(new UpdateDocumentMetaDataTasklet(blobClient, storedDocumentService))
                .build()).build().build();
    }
}
