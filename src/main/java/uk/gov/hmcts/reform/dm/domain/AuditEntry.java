package uk.gov.hmcts.reform.dm.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import java.util.Date;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by pawel on 24/07/2017.
 */

@Entity
@DiscriminatorColumn(name = "type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AuditEntry {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    private AuditActions action;

    @Getter
    @Setter
    @NotNull
    private String username;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date recordedDateTime;

    public Date getRecordedDateTime() {
        if (recordedDateTime == null) {
            return null;
        } else {
            return new Date(recordedDateTime.getTime());
        }
    }

    public void setRecordedDateTime(Date recordedDateTime) {
        if (recordedDateTime == null) {
            throw new IllegalArgumentException();
        } else {
            this.recordedDateTime = new Date(recordedDateTime.getTime());
        }
    }
}
