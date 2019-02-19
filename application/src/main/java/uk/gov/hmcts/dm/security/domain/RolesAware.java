package uk.gov.hmcts.dm.security.domain;

import java.util.Set;

import uk.gov.hmcts.dm.security.Classifications;

public interface RolesAware extends CreatorAware {

    Set<String> getRoles();

    Classifications getClassification();

}
