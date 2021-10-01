package uk.gov.hmcts.dm.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.security.domain.DomainPermissionEvaluator;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PermissionEvaluatorImplTests {

    @Mock
    DomainPermissionEvaluator domainPermissionEvaluator;

    @Mock
    RepositoryFinder repositoryFinder;

    @Mock
    SecurityUtilService securityUtilService;

    @InjectMocks
    PermissionEvaluatorImpl permissionEvaluator;

    @Before
    public void setup() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("em_gw");
    }

    @Test
    public void testPermissionOnCreatorAwareObject() {
        Assert.assertFalse(permissionEvaluator.hasPermission(null, null, "READ"));
    }

    @Test
    public void testLetCaseDocumentAccessManagementDoAnything() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("ccd_case_document_am_api");
        Assert.assertTrue(permissionEvaluator.hasPermission(null, null, "READ"));
    }
}
