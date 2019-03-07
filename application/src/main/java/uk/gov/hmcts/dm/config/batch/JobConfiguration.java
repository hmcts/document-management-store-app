package uk.gov.hmcts.dm.config.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobConfiguration {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("deleteAllExpiredStoredDocumentTasklet")
    private Tasklet deleteAllExpiredStoredDocumentTasklet;

    @Bean
    protected Step step1() {
        return stepBuilderFactory.get("step1")
            .transactionManager(transactionManager)
            .tasklet(deleteAllExpiredStoredDocumentTasklet)
            .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory
            .get("documentRetentionJob")
            .incrementer(new RunIdIncrementer())
            .flow(step1())
            .end()
            .build();
    }

}
