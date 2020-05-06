package uk.gov.hmcts.dm.security.domain;

import uk.gov.hmcts.dm.security.Classifications;

import java.util.Set;

public interface RolesAware extends CreatorAware {

    Set<String> getRoles();

    Classifications getClassification();

}
