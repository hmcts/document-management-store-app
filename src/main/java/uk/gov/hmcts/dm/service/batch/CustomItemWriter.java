package uk.gov.hmcts.dm.service.batch;

import org.springframework.batch.item.database.JpaItemWriter;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by pawel on 24/01/2018.
 *
 * Workaround for problem of lack of transaction
 */
public class CustomItemWriter<T> extends JpaItemWriter<T> {
    @Override
    @Transactional
    public void write(List<? extends T> items) {
        super.write(items);
    }
}
