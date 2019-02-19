package uk.gov.hmcts.dm.service.thumbnail;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedThumbnailCreatorTest {

    @Test
    public void getThumbnail() throws IOException {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        UnsupportedThumbnailCreator unsupportedThumbnailService = new UnsupportedThumbnailCreator();
        InputStream thumbnail = unsupportedThumbnailService.getThumbnail(documentContentVersion);

        InputStream expectInputStream = getClass().getResourceAsStream(UnsupportedThumbnailCreator.DEFAULT_FILE_THUMBNAIL);
        assertTrue(IOUtils.contentEquals(thumbnail,expectInputStream));
    }

}
