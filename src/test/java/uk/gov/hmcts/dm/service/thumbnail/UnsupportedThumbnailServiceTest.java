package uk.gov.hmcts.dm.service.thumbnail;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;


import java.io.InputStream;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedThumbnailServiceTest {

    @Test
    public void getThumbnail() {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        UnsupportedThumbnailService unsupportedThumbnailService = new UnsupportedThumbnailService();
        InputStream thumbnail = unsupportedThumbnailService.getThumbnail(documentContentVersion);

        InputStream expectInputStream = getClass().getResourceAsStream(UnsupportedThumbnailService.DEFAULT_FILE_THUMBNAIL);
        Assert.assertThat(thumbnail, CoreMatchers.equalTo(expectInputStream));
    }

}
