package uk.gov.hmcts.dm.security.domain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.Permissions;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by pawel on 03/10/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class DomainPermissionEvaluatorTests {

    private final DomainPermissionEvaluator domainPermissionEvaluator = new DomainPermissionEvaluator();

    @Test
    public void testAsCreatorIAmGrantedAllPermissions() {

        String credentials = "Mr A";

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(credentials);


        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                credentials,
                Arrays.asList("granted", "x"),
                Arrays.asList("granted", "y")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                credentials,
                Arrays.asList("granted", "x"),
                Arrays.asList("granted", "y")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                credentials,
                Arrays.asList("granted", "x"),
                Arrays.asList("granted", "y")
            ));
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                credentials,
                Arrays.asList("granted", "x"),
                Arrays.asList("granted", "y")
            ));
    }

    @Test
    public void testAsCaseWorkerIHaveOnlyReadPermission() {
        String creatorCredentials = "Mr A";
        String caseWorkerCredentials = "Mrs Case Worker";

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(creatorCredentials);


        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                caseWorkerCredentials,
                Arrays.asList("case-worker", "x"),
                Arrays.asList("case-worker", "y")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                caseWorkerCredentials,
                Arrays.asList("case-worker", "x"),
                Arrays.asList("case-worker", "y")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                caseWorkerCredentials,
                Arrays.asList("case-worker", "x"),
                Arrays.asList("case-worker", "y")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                caseWorkerCredentials,
                Arrays.asList("case-worker", "x"),
                Arrays.asList("case-worker", "y")
            ));
    }

    @Test
    public void testAsNotCreatorAndNotTestWorkerIdontHavePermissions() {
        String creatorCredentials = "Mr A";
        String notCaseWorkerCredentials = "Mrs Case Worker";

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy(creatorCredentials);


        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x"),
                Arrays.asList("case-worker", "y")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.UPDATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x"),
                Arrays.asList("case-worker", "y")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.CREATE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x"),
                Arrays.asList("case-worker", "y")
            ));
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.DELETE,
                notCaseWorkerCredentials,
                Arrays.asList("a", "x"),
                Arrays.asList("case-worker", "y")
            ));
    }

    @Test
    public void testRolesReadPermissionsWithClassificationRestrictedAndMatchingRoles() {

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[]{"allowingrole"}).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.RESTRICTED);

        String authenticatedUsedId = "Mrs Case Worker";
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                authenticatedUsedId,
                Arrays.asList("allowingrole", "x"),
                Arrays.asList("case-worker", "y")
            ));
    }

    @Test
    public void testRolesReadPermissionsWithClassificationPrivateAndMatchingRoles() {

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[]{"allowingrole"}).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.PRIVATE);

        String authenticatedUsedId = "Mrs Case Worker";
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                authenticatedUsedId,
                Arrays.asList("allowingrole", "x"),
                Arrays.asList("case-worker", "y")
            ));
    }


    @Test
    public void testRolesReadPermissionsWithClassificationPublicAndMatchingRoles() {

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[]{"allowingrole"}).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.PUBLIC);

        String authenticatedUsedId = "Mrs Case Worker";
        Assert.assertTrue(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                authenticatedUsedId,
                Arrays.asList("allowingrole", "x"),
                Arrays.asList("case-worker", "y")
            ));
    }

    @Test
    public void testRolesReadPermissionsWithClassificationRestrictedAndNotMatchingRoles() {

        StoredDocument storedFile = new StoredDocument();
        storedFile.setCreatedBy("nobody");
        storedFile.setRoles(Arrays.asList(new String[]{"notallowingrole"}).stream().collect(Collectors.toSet()));
        storedFile.setClassification(Classifications.RESTRICTED);

        String authenticatedUsedId = "Mrs Case Worker";
        Assert.assertFalse(domainPermissionEvaluator
            .hasPermission(
                storedFile,
                Permissions.READ,
                authenticatedUsedId,
                Arrays.asList("allowingrole", "x"),
                Arrays.asList("case-worker", "y")
            ));
    }

}
