package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedThumbnailServiceTest {

    @Test
    public void getThumbnail() throws IOException {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        UnsupportedThumbnailService unsupportedThumbnailService = new UnsupportedThumbnailService();
        InputStream thumbnail = unsupportedThumbnailService.getThumbnail(documentContentVersion);

        InputStream expectInputStream = getClass().getResourceAsStream(UnsupportedThumbnailService.DEFAULT_FILE_THUMBNAIL);
        assertTrue(IOUtils.contentEquals(thumbnail,expectInputStream));
    }

}
