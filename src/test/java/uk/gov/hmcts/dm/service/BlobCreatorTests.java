package uk.gov.hmcts.dm.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.componenttests.TestUtil;

import javax.persistence.EntityManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pawel on 11/07/2017.
 */

@RunWith(SpringRunner.class)
@ActiveProfiles({"embedded", "local", "componenttest"})
@SpringBootTest
@Transactional
public class BlobCreatorTests {

    @Autowired
    EntityManager entityManager;

    @Autowired
    BlobCreator blobCreator;

    @Test(expected = RuntimeException.class)
    public void testRuntimeException() {
        EntityManager riggedEntityManager = mock(EntityManager.class);
        when(riggedEntityManager.unwrap(any())).thenThrow(new Exception("x"));
        blobCreator.setEntityManager(riggedEntityManager);
        blobCreator.createBlob(TestUtil.TEST_FILE);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateBlob() {
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.unwrap(any())).thenThrow(new Exception("x"));
        blobCreator.setEntityManager(entityManager);
        blobCreator.createBlob(TestUtil.TEST_FILE);
    }

}
