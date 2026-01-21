package uk.gov.hmcts.dm.security.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.Permissions;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainPermissionEvaluatorTests {

    private static final String CREATOR_ID = "Mr A";
    private static final String NOT_CREATOR_ID = "Mrs Case Worker";

    private static final String ROLE_CASEWORKER = "caseworker";
    private static final String ROLE_CITIZEN = "citizen";

    private static final String SERVICE_CCD_DISPOSER = "ccd_case_disposer";
    private static final String SERVICE_CCD_AM_API = "ccd_case_document_am_api";

    @Mock
    private SecurityUtilService securityUtilService;

    @InjectMocks
    private DomainPermissionEvaluator domainPermissionEvaluator;

    @BeforeEach
    void setUp() {
        lenient().when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(SERVICE_CCD_AM_API);
    }

    @Test
    void testAsCreatorIAmGrantedAllPermissions() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);
        List<String> roles = List.of("granted", "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, CREATOR_ID, roles));
        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.UPDATE, CREATOR_ID, roles));
        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.CREATE, CREATOR_ID, roles));
        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.DELETE, CREATOR_ID, roles));
    }

    @Test
    void testAsCaseWorkerIHaveOnlyReadPermission() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);

        List<String> roles = List.of(ROLE_CASEWORKER, "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.UPDATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.CREATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.DELETE, NOT_CREATOR_ID, roles));
    }

    @Test
    void testAsADynamicCaseWorkerIHaveOnlyReadPermission() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);

        List<String> roles = List.of("caseworker_123", "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.UPDATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.CREATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.DELETE, NOT_CREATOR_ID, roles));
    }

    @Test
    void testAsCcdCaseDisposerIHaveDeletePermission() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);

        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(SERVICE_CCD_DISPOSER);
        List<String> roles = List.of(ROLE_CITIZEN, "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.DELETE, NOT_CREATOR_ID, roles));

        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.UPDATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.CREATE, NOT_CREATOR_ID, roles));
    }

    @Test
    void testAsNotCreatorAndNotCaseworkerIDontHavePermissions() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);

        List<String> roles = List.of(ROLE_CITIZEN, "x"); // No caseworker prefix

        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.UPDATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.CREATE, NOT_CREATOR_ID, roles));
        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.DELETE, NOT_CREATOR_ID, roles));
    }

    @Test
    void testRolesReadPermissionsWithClassificationRestrictedAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);
        storedFile.setRoles(Set.of("allowingrole"));
        storedFile.setClassification(Classifications.RESTRICTED);

        List<String> userRoles = List.of("allowingrole", "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, userRoles));
    }

    @Test
    void testRolesReadPermissionsWithClassificationPrivateAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);
        storedFile.setRoles(Set.of("allowingrole"));
        storedFile.setClassification(Classifications.PRIVATE);

        List<String> userRoles = List.of("allowingrole", "x");

        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, userRoles));
    }


    @Test
    void testRolesReadPermissionsWithClassificationPublicAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);
        storedFile.setRoles(Set.of("allowingrole"));
        storedFile.setClassification(Classifications.PUBLIC);

        List<String> userRoles = List.of("allowingrole", "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, userRoles));
    }

    @Test
    void testRolesReadPermissionsWithClassificationRestrictedAndNotMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);
        storedFile.setRoles(Set.of("notallowingrole"));
        storedFile.setClassification(Classifications.RESTRICTED);

        List<String> userRoles = List.of("allowingrole", "x");

        assertFalse(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, userRoles));
    }

    @Test
    void testDocumentRolesWithLeadingSpaceCharacter() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(CREATOR_ID);
        storedFile.setRoles(Set.of(" valid-role"));
        storedFile.setClassification(Classifications.RESTRICTED);

        List<String> userRoles = List.of(" valid-role", "x");

        assertTrue(domainPermissionEvaluator.hasPermission(storedFile, Permissions.READ, NOT_CREATOR_ID, userRoles));
    }
}
