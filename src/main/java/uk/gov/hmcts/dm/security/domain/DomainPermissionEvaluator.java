package uk.gov.hmcts.dm.security.domain;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.Permissions;

import java.util.Collection;
import java.util.HashSet;

@Component
public class DomainPermissionEvaluator {

    public boolean hasPermission(@NonNull final CreatorAware creatorAware,
                                 @NonNull final Permissions permission,
                                 @NonNull final String authenticatedUserId,
                                 @NonNull final Collection<String> authenticatedUserRoles,
                                 @NonNull final Collection<String> caseWorkerRoles) {

        boolean result = false;

        if (authenticatedUserId.equals(creatorAware.getCreatedBy())) {
            result = true;
        }

        if (!result && permission == Permissions.READ && creatorAware instanceof RolesAware) {
            RolesAware rolesAware = (RolesAware) creatorAware;
            if (rolesAware.getRoles() != null
                && authenticatedUserRoles != null
                && rolesAware.getClassification() != null
                && (Classifications.RESTRICTED.equals(rolesAware.getClassification())
                || Classifications.PUBLIC.equals(rolesAware.getClassification())
            )) {
                HashSet<String> authenticatedUserRolesSet = new HashSet<>(authenticatedUserRoles);
                authenticatedUserRolesSet.retainAll(rolesAware.getRoles());

                if (!authenticatedUserRolesSet.isEmpty()) {
                    result = true;
                }
            }
        }

        if (!result && permission == Permissions.READ) {

            HashSet<String> authenticatedUserRolesSet = new HashSet<>(authenticatedUserRoles);

            authenticatedUserRolesSet.retainAll(caseWorkerRoles);

            if (!authenticatedUserRolesSet.isEmpty()) {
                result = true;
            }
        }

        return result;

    }

}
