package uk.gov.hmcts.dm.controller.testing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TestControllerTest {

    @Mock
    BlobStorageReadService blobStorageReadService;

    @InjectMocks
    TestController testController;

    @Test
    public void getTrue() throws Exception {
        BDDMockito.given(blobStorageReadService.doesBinaryExist(Mockito.any())).willReturn(true);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertTrue(responseEntity.getBody().booleanValue());
    }

    @Test
    public void getFalse() throws Exception {
        BDDMockito.given(blobStorageReadService.doesBinaryExist(Mockito.any())).willReturn(false);
        ResponseEntity<Boolean> responseEntity = testController.get(UUID.randomUUID());
        assertFalse(responseEntity.getBody().booleanValue());
    }
}
