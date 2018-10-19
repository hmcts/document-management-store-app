package uk.gov.hmcts.dm.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * A simpler mapping to auditentry table.
 */
@Entity
@Table(name="auditentry")
@NoArgsConstructor
public class MigrateEntry {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Getter
    private UUID id;

    @Getter
    private String type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Getter
    private AuditActions action;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date recordedDateTime;

    @Getter
    private UUID storeddocument_id;

    @Getter
    private UUID documentcontentversion_id;

    @Getter
    private String servicename;

    public MigrateEntry(String type,
                        AuditActions action,
                        DocumentContentVersion documentcontentversion,
                        String serviceName) {
        this.type = type;
        this.action = action;
        this.storeddocument_id = documentcontentversion.getStoredDocument().getId();
        this.documentcontentversion_id = documentcontentversion.getId();
        this.servicename = serviceName;
        this.recordedDateTime = new Date();
    }
}
