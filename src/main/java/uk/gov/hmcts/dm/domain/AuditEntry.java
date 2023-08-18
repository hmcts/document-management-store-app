package uk.gov.hmcts.dm.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;



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
    private String username;

    @Getter
    @Setter
    @NotNull
    private String serviceName;

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
