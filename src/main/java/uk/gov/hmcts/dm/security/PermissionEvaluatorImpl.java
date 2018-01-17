package uk.gov.hmcts.dm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.repository.RepositoryFinder;
import uk.gov.hmcts.dm.security.domain.CreatorAware;
import uk.gov.hmcts.dm.security.domain.DomainPermissionEvaluator;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {

    @Autowired
    private DomainPermissionEvaluator domainPermissionEvaluator;

    @Value("${authorization.case-worker-roles}")
    private String [] caseWorkerRoles;

    @Autowired
    private RepositoryFinder repositoryFinder;

    @Override
    public boolean hasPermission(@NotNull Authentication authentication,
                                 @NotNull Object targetDomainObject,
                                 @NotNull Object permissionString) {
        boolean result = false;
        if (targetDomainObject instanceof CreatorAware) {
            result = domainPermissionEvaluator.hasPermission(
                    (CreatorAware)targetDomainObject,
                    Permissions.valueOf((String)permissionString),
                    ((ServiceAndUserDetails) authentication.getPrincipal()).getUsername(),
                    authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
                    new HashSet<>(Arrays.asList(caseWorkerRoles)));
        }
        return result;
    }


    @Override
    public boolean hasPermission(@NotNull Authentication authentication,
                                 @NotNull Serializable serializable,
                                 @NotNull String className,
                                 @NotNull Object permissions) {
        boolean result = false;
        CrudRepository<Object, Serializable> repository = repositoryFinder.find(className);
        if (repository != null) {
            Object targetDomainObject = repository.findOne(serializable);
            if (targetDomainObject instanceof CreatorAware) {
                result = hasPermission(authentication, targetDomainObject, permissions);
            } else if (targetDomainObject == null) {
                result = true;
            }
        }
        return result;
    }




}
