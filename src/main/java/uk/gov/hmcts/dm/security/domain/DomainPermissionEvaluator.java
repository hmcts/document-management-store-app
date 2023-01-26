package uk.gov.hmcts.dm.security.domain;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.Permissions;
import uk.gov.hmcts.dm.service.SecurityUtilService;
import uk.gov.hmcts.dm.utils.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.dm.service.SecurityUtilService.sanitizedSetFrom;

@Component
public class DomainPermissionEvaluator {

    public static final String CASE_WORKER_PREFIX = "caseworker";
    private final Logger log = LoggerFactory.getLogger(DomainPermissionEvaluator.class);

    private static final String CCD_CASE_DISPOSER = "ccd_case_disposer";

    @Autowired
    private SecurityUtilService securityUtilService;

    @Deprecated
    public boolean hasPermission(@NonNull final CreatorAware creatorAware,
                                 @NonNull final Permissions permission,
                                 final String authenticatedUserId,
                                 @NonNull final Collection<String> authenticatedUserRoles,
                                 @NonNull final Collection<String> caseWorkerRoles) {
        return hasPermission(creatorAware, permission, authenticatedUserId, authenticatedUserRoles);
    }

    public boolean hasPermission(@NonNull final CreatorAware creatorAware,
                                 @NonNull final Permissions permission,
                                 final String authenticatedUserId,
                                 @NonNull final Collection<String> authenticatedUserRoles) {

        boolean result = false;

        if (authenticatedUserId != null && authenticatedUserId.equals(creatorAware.getCreatedBy())) {
            result = true;
        }

        HashSet<String> authenticatedUserRolesSet = sanitizedSetFrom(authenticatedUserRoles);

        if (!result && permission == Permissions.READ && creatorAware instanceof RolesAware) {
            RolesAware rolesAware = (RolesAware) creatorAware;
            if (rolesAware.getRoles() != null
                && authenticatedUserRoles != null
                && rolesAware.getClassification() != null
                && (Classifications.RESTRICTED.equals(rolesAware.getClassification())
                || Classifications.PUBLIC.equals(rolesAware.getClassification()))
                ) {
                Set<String> documentRoles = sanitizedSetFrom(rolesAware.getRoles());
                log.info("User with roles {} accessing document that accepts roles {}",
                    StringUtils.convertValidLogStrings(authenticatedUserRolesSet),
                    StringUtils.convertValidLogStrings(documentRoles));

                documentRoles.retainAll(authenticatedUserRolesSet);
                if (documentRoles.size() > 0) {
                    result = true;
                }
            }
        }

        if (!result && permission == Permissions.READ) {
            boolean hasCaseworkerRole = !authenticatedUserRolesSet.stream()
                .filter(role -> role.startsWith(CASE_WORKER_PREFIX))
                .collect(Collectors.toList())
                .isEmpty();

            result = hasCaseworkerRole;
        }

        if (!result && permission == Permissions.DELETE
                && securityUtilService.getCurrentlyAuthenticatedServiceName().equalsIgnoreCase(CCD_CASE_DISPOSER)) {
            result = true;
        }
        log.info(
            "AuthenticatedUserId {}, CreatorAware CreatedBy {}, Permissions {}, "
                + "CurrentlyAuthenticatedServiceName {}, Result {}",
            authenticatedUserId,
            creatorAware.getCreatedBy(),
            permission,
            securityUtilService.getCurrentlyAuthenticatedServiceName(),
            result
        );

        return result;
    }
}
