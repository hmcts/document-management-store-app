package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedThumbnailServiceTest {

    @Test
    public void getThumbnail() throws IOException {
        DocumentContentVersion documentContentVersion = mock(DocumentContentVersion.class);
        UnsupportedThumbnailService unsupportedThumbnailService = new UnsupportedThumbnailService();
        InputStream thumbnail = unsupportedThumbnailService.getThumbnail(documentContentVersion);

        File outputfile = new File("UnsupportedThumbnailService.jpg");
        FileUtils.copyInputStreamToFile(thumbnail,outputfile);

        InputStream expectInputStream = getClass().getResourceAsStream(UnsupportedThumbnailService.DEFAULT_FILE_THUMBNAIL);
        Assert.assertThat(thumbnail, CoreMatchers.equalTo(expectInputStream));
    }

}
