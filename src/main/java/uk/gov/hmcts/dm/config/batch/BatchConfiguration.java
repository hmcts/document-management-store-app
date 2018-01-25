package uk.gov.hmcts.dm.config.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.dm.exception.AppConfigurationException;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@EnableConfigurationProperties(BatchProperties.class)
public class BatchConfiguration {

    @Bean
    public JobRepository jobRepository(
         DataSource dataSource,
         PlatformTransactionManager transactionManager) throws Exception {

        try {
            JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
            factory.setDataSource(dataSource);
            factory.setTransactionManager(transactionManager);
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (Exception e) {
            throw new AppConfigurationException("Could not create 'jobRepository' bean", e);
        }

        return null;
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository);
        return simpleJobLauncher;
    }



}
