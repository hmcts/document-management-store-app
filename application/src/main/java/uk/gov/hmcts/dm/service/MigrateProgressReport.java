package uk.gov.hmcts.dm.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({MigrateProgressReport.ATTRIBUTE_LEFT_TO_MIGRATE, MigrateProgressReport.ATTRIBUTE_MIGRATED})
public class MigrateProgressReport {

    static final String ATTRIBUTE_LEFT_TO_MIGRATE = "left_to_migrate";
    static final String ATTRIBUTE_MIGRATED = "migrated";

    @JsonProperty(ATTRIBUTE_LEFT_TO_MIGRATE)
    private Long leftToMigrate;

    @JsonProperty(ATTRIBUTE_MIGRATED)
    private Long migrated;

    protected MigrateProgressReport(Long leftToMigrate, Long migrated) {
        this.leftToMigrate = leftToMigrate;
        this.migrated = migrated;
    }
}
