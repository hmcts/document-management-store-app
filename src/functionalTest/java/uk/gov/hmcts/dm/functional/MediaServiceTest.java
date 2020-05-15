package uk.gov.hmcts.dm.functional;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.service.AzureMediaUploadService;

import java.io.File;

@NotThreadSafe
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DmApp.class)
public class MediaServiceTest {

    private static final String PDF_FILENAME = "files/ignite.mp4";

    @Autowired
    private AzureMediaUploadService azureMediaUploadService;

    // This Test is to check the upload of media file on to AMS, run the job, generate the Streaming URL
    @Test
    public void testUploadMediaFile() {

        azureMediaUploadService.uploadMediaFile("emshowcasespike-streaming-transform",
            new File(ClassLoader.getSystemResource(PDF_FILENAME).getFile()));
    }
}
