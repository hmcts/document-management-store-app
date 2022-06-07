package uk.gov.hmcts.dm.security.domain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.Permissions;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DomainPermissionEvaluatorTests {

    private static final String MRS_CASE_WORKER = "Mrs Case Worker";
    private static final String MR_A = "Mr A";
    private static final String CCD_CASE_DISPOSER = "ccd_case_disposer";
    private static final String CASE_DOCUMENT_ACCESS_API = "ccd_case_document_am_api";

    @Mock
    SecurityUtilService securityUtilService;

    @InjectMocks
    DomainPermissionEvaluator domainPermissionEvaluator;

    @Test
    public void testAsCreatorIAmGrantedAllPermissions() {
        String credentials = MR_A;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(credentials);


        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                credentials,
                Arrays.asList("granted", "x")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                credentials,
                Arrays.asList("granted", "x")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                credentials,
                Arrays.asList("granted", "x")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                credentials,
                Arrays.asList("granted", "x")
            ));
    }

    @Test
    public void testAsCaseWorkerIHaveOnlyReadPermission() {
        String caseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);

        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CASE_DOCUMENT_ACCESS_API);

        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                caseWorkerCredentials,
                Arrays.asList("caseworker", "x")
            ));
    }

    @Test
    public void testAsADynamicCaseWorkerIHaveOnlyReadPermission() {
        String caseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);

        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CASE_DOCUMENT_ACCESS_API);

        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                caseWorkerCredentials,
                Arrays.asList("caseworker_123", "x")
            ));
    }

    @Test
    public void testAsCcdCaseDisposerIHaveDeletePermission() {
        String notCaseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CCD_CASE_DISPOSER);

        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
    }

    @Test
    public void testAsNotCreatorAndNotTestWorkerIdontHavePermissions() {
        String notCaseWorkerCredentials = MRS_CASE_WORKER;

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(MR_A);
        when(securityUtilService.getCurrentlyAuthenticatedServiceName()).thenReturn(CASE_DOCUMENT_ACCESS_API);

        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x")
            ));
    }

    @Test
    public void testRolesReadPermissionsWithClassificationRestrictedAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[] { "allowingrole" }).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.RESTRICTED);

        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }

    @Test
    public void testRolesReadPermissionsWithClassificationPrivateAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[] { "allowingrole" }).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.PRIVATE);


        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }


    @Test
    public void testRolesReadPermissionsWithClassificationPublicAndMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[] { "allowingrole" }).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.PUBLIC);


        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }

    @Test
    public void testRolesReadPermissionsWithClassificationRestrictedAndNotMatchingRoles() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[] { "notallowingrole" }).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.RESTRICTED);


        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList("allowingrole", "x")
            ));
    }

    @Test
    public void testDocumentRolesWithLeadingSpaceCharacter() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[] { " valid-role" }).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.RESTRICTED);


        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList(" valid-role", "x")
            ));
    }

    @Test
    public void testUserRolesWithLeadingSpaceCharacter() {
        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[] { "valid-role" }).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.RESTRICTED);


        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                MRS_CASE_WORKER,
                Arrays.asList(" valid-role", "x")
            ));
    }

}
