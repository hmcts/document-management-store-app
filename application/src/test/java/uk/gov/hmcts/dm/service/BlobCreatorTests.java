package uk.gov.hmcts.dm.service;

import org.hibernate.Session;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;

import javax.persistence.EntityManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BlobCreatorTests extends ComponentTestBase {

    @Mock
    EntityManager entityManager;

    @Autowired
    BlobCreator blobCreator;

    @Test(expected = RuntimeException.class)
    public void testRuntimeException() {
        when(entityManager.unwrap(any())).thenThrow(new Exception("x"));
        blobCreator.createBlob(TestUtil.TEST_FILE);
    }

    @Test
    public void testCreateBlob() {
        Session s = mock(Session.class);
        when(entityManager.unwrap(any())).thenReturn(s);
        blobCreator.createBlob(TestUtil.TEST_FILE);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateBlobNullFile() {
        blobCreator.createBlob(null);
    }
}
