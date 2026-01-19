package uk.gov.hmcts.dm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.Authentication;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.security.domain.CreatorAware;
import uk.gov.hmcts.dm.security.domain.DomainPermissionEvaluator;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionEvaluatorImplTests {

    @Mock
    private DomainPermissionEvaluator domainPermissionEvaluator;

    @Mock
    private RepositoryFinder repositoryFinder;

    @Mock
    private SecurityUtilService securityUtilService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PermissionEvaluatorImpl permissionEvaluator;

    @Test
    void testPermissionOnNullObjectIsFalse() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("em_gw");
        assertFalse(permissionEvaluator.hasPermission(authentication, null, "READ"));
    }

    @Test
    void testPermissionOnNonCreatorAwareObjectIsFalse() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("em_gw");
        assertFalse(permissionEvaluator.hasPermission(authentication, new Object(), "READ"));
    }

    @Test
    void testLetCaseDocumentAccessManagementDoAnything() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("ccd_case_document_am_api");
        assertTrue(permissionEvaluator.hasPermission(authentication, null, "READ"));
    }

    @Test
    void testHasPermissionDelegatesToDomainEvaluator() {
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("other_service");
        CreatorAware creatorAware = mock(CreatorAware.class);

        when(securityUtilService.getUserId()).thenReturn("user1");
        when(securityUtilService.getUserRoles()).thenReturn(Collections.singleton("role1"));

        when(domainPermissionEvaluator.hasPermission(
            creatorAware,
            Permissions.READ,
            "user1",
            Collections.singleton("role1")
        )).thenReturn(true);

        assertTrue(permissionEvaluator.hasPermission(authentication, creatorAware, "READ"));
    }

    @Test
    void testHasPermissionByIdAndClassNameSuccess() {
        UUID id = UUID.randomUUID();
        String className = "uk.gov.hmcts.dm.domain.StoredDocument";
        CreatorAware storedDocument = mock(CreatorAware.class);
        CrudRepository<Object, Serializable> repository = mock(CrudRepository.class);

        when(repositoryFinder.find(className)).thenReturn(repository);
        when(repository.findById(id)).thenReturn(Optional.of(storedDocument));

        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn("ccd_case_document_am_api");

        assertTrue(permissionEvaluator.hasPermission(authentication, id, className, "READ"));
    }

    @Test
    void testHasPermissionByIdReturnsTrueWhenObjectNotFound() {
        UUID id = UUID.randomUUID();
        String className = "uk.gov.hmcts.dm.domain.StoredDocument";
        CrudRepository<Object, Serializable> repository = mock(CrudRepository.class);

        when(repositoryFinder.find(className)).thenReturn(repository);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertTrue(permissionEvaluator.hasPermission(authentication, id, className, "READ"));
    }

    @Test
    void testHasPermissionByIdReturnsFalseWhenRepositoryNotFound() {
        UUID id = UUID.randomUUID();
        String className = "UnknownClass";

        when(repositoryFinder.find(className)).thenReturn(null);

        assertFalse(permissionEvaluator.hasPermission(authentication, id, className, "READ"));
    }
}
