package uk.gov.hmcts.dm.security.domain;

import uk.gov.hmcts.dm.security.Classifications;

import java.util.Set;

/**
 * Created by pawel on 22/11/2017.
 */
public interface RolesAware extends CreatorAware {

    Set<String> getRoles();

    Classifications getClassification();

}
