package uk.gov.hmcts.dm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.security.domain.CreatorAware;
import uk.gov.hmcts.dm.security.domain.DomainPermissionEvaluator;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {

    private static final String CASE_DOCUMENT_ACCESS_API = "ccd_case_document_am_api";

    @Autowired
    private SecurityUtilService securityUtilService;

    @Autowired
    private DomainPermissionEvaluator domainPermissionEvaluator;

    @Autowired
    private RepositoryFinder repositoryFinder;

    @Override
    public boolean hasPermission(@NotNull Authentication authentication,
                                 @NotNull Object targetDomainObject,
                                 @NotNull Object permissionString) {

        if (Objects.equals(securityUtilService.getCurrentlyAuthenticatedServiceName(), CASE_DOCUMENT_ACCESS_API)) {
            return true;
        }

        if (targetDomainObject instanceof CreatorAware) {
            Collection<String> userRoles = securityUtilService.getUserRoles() != null
                ? securityUtilService.getUserRoles()
                : Collections.EMPTY_SET;

            return domainPermissionEvaluator.hasPermission(
                    (CreatorAware)targetDomainObject,
                    Permissions.valueOf((String)permissionString),
                    securityUtilService.getUserId(),
                    userRoles);
        }
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull Authentication authentication,
                                 @NotNull Serializable serializable,
                                 @NotNull String className,
                                 @NotNull Object permissions) {
        boolean result = false;
        CrudRepository<Object, Serializable> repository = repositoryFinder.find(className);
        if (repository != null) {
            result = repository.findById(serializable)
                .map(o -> hasPermission(authentication, o, permissions))
                .orElse(true);
        }
        return result;
    }
}
