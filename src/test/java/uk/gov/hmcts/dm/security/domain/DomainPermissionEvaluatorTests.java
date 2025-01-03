package uk.gov.hmcts.dm.security.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.Permissions;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class DomainPermissionEvaluatorTests {

    private static final String MRS_CASE_WORKER = "Mrs Case Worker";
    private static final String MR_A = "Mr A";
    private static final String CCD_CASE_DISPOSER = "ccd_case_disposer";
    private static final String CASE_DOCUMENT_ACCESS_API = "ccd_case_document_am_api";

    @Mock
    SecurityUtilService securityUtilService;

    @InjectMocks
    DomainPermissionEvaluator domainPermissionEvaluator;

    @Test
    void testAsCreatorIAmGrantedAllPermissions() {
        String credentials = MR_A;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(credentials);


        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                credentials,
                Arrays.asList("granted", "x")
            ));
        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                credentials,
                Arrays.asList("granted", "x")
            ));
        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                credentials,
                Arrays.asList("granted", "x")
            ));
        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                credentials,
                Arrays.asList("granted", "x")
            ));
    }

    @Test
    void testAsCaseWorkerIHaveOnlyReadPermission() {
        String caseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);

        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CASE_DOCUMENT_ACCESS_API);

        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
    }

    @Test
    void testAsADynamicCaseWorkerIHaveOnlyReadPermission() {
        String caseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);

        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CASE_DOCUMENT_ACCESS_API);

        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
    }

    @Test
    void testAsCcdCaseDisposerIHaveDeletePermission() {
        String notCaseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CCD_CASE_DISPOSER);

        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
    }

    @Test
    void testAsNotCreatorAndNotTestWorkerIdontHavePermissions() {
        String notCaseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CASE_DOCUMENT_ACCESS_API);

        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
    }

    @Test
    void testRolesReadPermissionsWithClassificationRestrictedAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Set.of("allowingrole"));
        storedFile.setClassification(Classifications.RESTRICTED);

        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }

    @Test
    void testRolesReadPermissionsWithClassificationPrivateAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Set.of("allowingrole"));
        storedFile.setClassification(Classifications.PRIVATE);


        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }


    @Test
    void testRolesReadPermissionsWithClassificationPublicAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Set.of("allowingrole"));
        storedFile.setClassification(Classifications.PUBLIC);


        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }

    @Test
    void testRolesReadPermissionsWithClassificationRestrictedAndNotMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Set.of("notallowingrole"));
        storedFile.setClassification(Classifications.RESTRICTED);


        assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }

    @Test
    void testDocumentRolesWithLeadingSpaceCharacter() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Set.of(" valid-role"));
        storedFile.setClassification(Classifications.RESTRICTED);


        assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList(" valid-role", "x")
            ));
    }
}
