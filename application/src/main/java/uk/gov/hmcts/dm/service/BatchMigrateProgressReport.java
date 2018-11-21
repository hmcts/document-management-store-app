package uk.gov.hmcts.dm.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import net.time4j.PrettyTime;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.time.Duration;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static net.time4j.format.TextWidth.NARROW;
import static org.springframework.http.HttpStatus.OK;

@Data
@JsonPropertyOrder({BatchMigrateProgressReport.ATTRIBUTE_BEFORE_JOB, BatchMigrateProgressReport.ATTRIBUTE_MIGRATED,
    BatchMigrateProgressReport.ATTRIBUTE_AFTER_JOB, BatchMigrateProgressReport.ATTRIBUTE_STATUS,
    BatchMigrateProgressReport.ATTRIBUTE_DURATION, BatchMigrateProgressReport.ATTRIBUTE_ERRORS})
public class BatchMigrateProgressReport {

    protected static final String ATTRIBUTE_BEFORE_JOB = "before_job";
    protected static final String ATTRIBUTE_AFTER_JOB = "after_job";
    protected static final String ATTRIBUTE_MIGRATED = "migrated";
    protected static final String ATTRIBUTE_STATUS = "status";
    protected static final String ATTRIBUTE_ERRORS = "errors";
    protected static final String ATTRIBUTE_DURATION = "duration";

    @JsonProperty(ATTRIBUTE_BEFORE_JOB)
    private final MigrateProgressReport beforeJob;

    @JsonProperty(ATTRIBUTE_AFTER_JOB)
    private final MigrateProgressReport afterJob;

    @JsonProperty(ATTRIBUTE_MIGRATED)
    private final List<DocumentContentVersionModel> migratedDocumentContentVersions;

    @JsonProperty(ATTRIBUTE_STATUS)
    private final HttpStatus status;

    @JsonInclude(NON_EMPTY)
    @JsonProperty(ATTRIBUTE_ERRORS)
    private List<String> errors;

    private final Duration duration;

    protected BatchMigrateProgressReport(MigrateProgressReport beforeJob,
                                         List<DocumentContentVersion> migratedDocumentContentVersions,
                                         MigrateProgressReport afterJob,
                                         Duration duration) {
        this.beforeJob = beforeJob;
        this.migratedDocumentContentVersions = migratedDocumentContentVersions.stream()
            .map(DocumentContentVersionModel::new)
            .collect(toList());
        this.afterJob = afterJob;
        this.status = OK;
        this.duration = duration;
    }

    @JsonProperty(ATTRIBUTE_DURATION)
    public String getDuration() {
        return PrettyTime.of(ENGLISH).print(duration, NARROW);
    }
}
