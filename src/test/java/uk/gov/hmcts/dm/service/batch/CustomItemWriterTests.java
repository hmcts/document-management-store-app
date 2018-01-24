package uk.gov.hmcts.dm.service.batch;

import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pawel on 24/01/2018.
 */
public class CustomItemWriterTests {

    @Test(expected = IllegalArgumentException.class)
    public void testWriteWithoutEntityManager() {
        CustomItemWriter<Object> writer = new CustomItemWriter<>();
        writer.write(Stream.of(new Object()).collect(Collectors.toList()));
    }

}
