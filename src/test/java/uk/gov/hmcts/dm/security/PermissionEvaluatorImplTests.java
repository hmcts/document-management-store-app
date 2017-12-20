package uk.gov.hmcts.dm.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.security.domain.DomainPermissionEvaluator;

import static org.mockito.Mockito.mock;

/**
 * Created by pawel on 03/10/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class PermissionEvaluatorImplTests {

    @Mock
    protected DomainPermissionEvaluator domainPermissionEvaluator;

    @Mock
    protected RepositoryFinder repositoryFinder;

    @InjectMocks
    protected PermissionEvaluatorImpl permissionEvaluator;

    protected Authentication authentication;

    @Before
    public void setUp() {
        authentication = mock(Authentication.class);
    }

    @Test
    public void testPermissionOnCreatorAwareObject() {
        Assert.assertFalse(permissionEvaluator.hasPermission(null, null, "READ"));
    }

}
