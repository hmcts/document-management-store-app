package uk.gov.hmcts.dm.domain;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;



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
