package uk.gov.hmcts.dm.config.batch;

import com.google.common.collect.ImmutableMap;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.batch.CustomItemWriter;

import javax.persistence.EntityManagerFactory;
import java.util.Date;

/**
 * Created by pawel on 24/01/2018.
 */
@Configuration
public class JobConfiguration {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ItemProcessor<StoredDocument, StoredDocument> batchStoredDocumentProcessor;

    private static final String RETRIEVEEXPIREDDOCUMENTSQUERY =
        "from StoredDocument d JOIN FETCH d.documentContentVersions where hardDeleted = false AND ttl < :now";

    @Bean
    protected Step step1() {
        return stepBuilderFactory.get("step1")
            .transactionManager(transactionManager)
            .<StoredDocument, StoredDocument>chunk(50)
            .reader(reader())
            .processor(batchStoredDocumentProcessor)
            .writer(writer())
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

    @Bean
    public ItemReader<StoredDocument> reader() {
        JpaPagingItemReader<StoredDocument> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString(RETRIEVEEXPIREDDOCUMENTSQUERY);
        reader.setParameterValues(ImmutableMap.of("now", new Date()));
        reader.setPageSize(50);
        return reader;
    }

    @Bean
    public ItemWriter<StoredDocument> writer() {
        CustomItemWriter<StoredDocument> writer = new CustomItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}
