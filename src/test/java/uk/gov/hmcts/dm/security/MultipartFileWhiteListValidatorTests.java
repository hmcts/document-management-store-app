package uk.gov.hmcts.dm.security;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.service.FileContentVerifier;

@RunWith(SpringRunner.class)
public class MultipartFileWhiteListValidatorTests {

    @Mock
    FileContentVerifier fileContentVerifier;

    @InjectMocks
    private MultipartFileWhiteListValidator multipartFileWhiteListValidator;

    @Test
    public void testSuccess() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(fileContentVerifier.verifyContentType(file)).thenReturn(true);
        Assert.assertTrue(multipartFileWhiteListValidator.isValid(file, null));
    }

    @Test
    public void testFailure() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(fileContentVerifier.verifyContentType(file)).thenReturn(false);
        Assert.assertFalse(multipartFileWhiteListValidator.isValid(file, null));
    }



}
