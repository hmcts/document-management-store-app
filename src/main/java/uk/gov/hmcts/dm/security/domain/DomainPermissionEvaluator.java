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
import java.util.Set;

import static uk.gov.hmcts.dm.service.SecurityUtilService.sanitizedSetFrom;

@Component
public class DomainPermissionEvaluator {

    public static final String CASE_WORKER_PREFIX = "caseworker";
    private final Logger log = LoggerFactory.getLogger(DomainPermissionEvaluator.class);

    private static final String CCD_CASE_DISPOSER = "ccd_case_disposer";

    private final SecurityUtilService securityUtilService;

    @Autowired
    public DomainPermissionEvaluator(SecurityUtilService securityUtilService) {
        this.securityUtilService = securityUtilService;
    }

    public boolean hasPermission(@NonNull final CreatorAware creatorAware,
                                 @NonNull final Permissions permission,
                                 final String authenticatedUserId,
                                 @NonNull final Collection<String> authenticatedUserRoles) {

        boolean result = false;

        if (authenticatedUserId != null && authenticatedUserId.equals(creatorAware.getCreatedBy())) {
            result = true;
        }

        Set<String> authenticatedUserRolesSet = sanitizedSetFrom(authenticatedUserRoles);

        if (!result && permission == Permissions.READ && creatorAware instanceof RolesAware rolesAware
            && (rolesAware.getRoles() != null
            && rolesAware.getClassification() != null
            && (Classifications.RESTRICTED.equals(rolesAware.getClassification())
            || Classifications.PUBLIC.equals(rolesAware.getClassification()))
            )) {
            Set<String> documentRoles = sanitizedSetFrom(rolesAware.getRoles());
            log.debug("User with roles {} accessing document that accepts roles {}",
                StringUtils.convertValidLogStrings(authenticatedUserRolesSet),
                StringUtils.convertValidLogStrings(documentRoles));

            documentRoles.retainAll(authenticatedUserRolesSet);
            if (!documentRoles.isEmpty()) {
                result = true;
            }
        }

        if (!result && permission == Permissions.READ) {

            result = !authenticatedUserRolesSet.stream()
                .filter(role -> role.startsWith(CASE_WORKER_PREFIX))
                .toList()
                .isEmpty();
        }

        if (!result && permission == Permissions.DELETE
                && securityUtilService.getCurrentlyAuthenticatedServiceName().equalsIgnoreCase(CCD_CASE_DISPOSER)) {
            result = true;
        }
        log.debug(
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
