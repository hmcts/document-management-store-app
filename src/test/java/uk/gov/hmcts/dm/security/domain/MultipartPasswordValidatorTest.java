package uk.gov.hmcts.dm.security.domain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.hmcts.dm.security.MultipartFilePasswordValidator;
import uk.gov.hmcts.dm.service.FileContentVerifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
public class MultipartPasswordValidatorTest {
    @Mock
    FileContentVerifier fileContentVerifier;

    @InjectMocks
    MultipartFilePasswordValidator multipartFilePasswordValidator;

    //There must be a better way that surround in try-catch
//    public static MultipartFile NON_PW_MULTIPARTFILE = new MockMultipartFile(
//        "test.txt", new FileInputStream(
//            new File(ClassLoader.getSystemResource("files/not_password_protected.txt").getFile())
//        )
//    );

    public static MultipartFile NON_PW_MULTIPARTFILE;

    static {
        try {
            NON_PW_MULTIPARTFILE = new MockMultipartFile(
                "test.txt", new FileInputStream(
                new File(ClassLoader.getSystemResource("files/not_password_protected.txt").getFile())
            )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSuccess() {


        MultipartFile file = Mockito.mock(MultipartFile.class);
        List<MultipartFile> files = new ArrayList<>();

        files.add(NON_PW_MULTIPARTFILE);


        Mockito.when(fileContentVerifier.verifyContentType(file)).thenReturn(true);
        Assert.assertTrue(multipartFilePasswordValidator.isValid(files, null));
    }


}
