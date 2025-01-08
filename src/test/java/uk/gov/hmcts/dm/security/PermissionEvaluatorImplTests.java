package uk.gov.hmcts.dm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.security.domain.DomainPermissionEvaluator;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PermissionEvaluatorImplTests {

    @Mock
    DomainPermissionEvaluator domainPermissionEvaluator;

    @Mock
    RepositoryFinder repositoryFinder;

    @Mock
    SecurityUtilService securityUtilService;

    PermissionEvaluatorImpl permissionEvaluator;

    @BeforeEach
    public void setup() {
        permissionEvaluator = new PermissionEvaluatorImpl(securityUtilService,
            domainPermissionEvaluator, repositoryFinder);
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("em_gw");
    }

    @Test
    void testPermissionOnCreatorAwareObject() {
        assertFalse(permissionEvaluator.hasPermission(null, null, "READ"));
    }

    @Test
    void testLetCaseDocumentAccessManagementDoAnything() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("ccd_case_document_am_api");
        assertTrue(permissionEvaluator.hasPermission(null, null, "READ"));
    }
}
