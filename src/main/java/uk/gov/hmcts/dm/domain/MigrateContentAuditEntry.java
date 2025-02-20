package uk.gov.hmcts.dm.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Migrate content")
public class MigrateContentAuditEntry extends DocumentContentVersionAuditEntry {

}
