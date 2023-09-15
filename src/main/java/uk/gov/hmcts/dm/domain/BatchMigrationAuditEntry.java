package uk.gov.hmcts.dm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "batch_migration_audit_entry")
@NoArgsConstructor
public class BatchMigrationAuditEntry {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Getter
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started", nullable = false, updatable = false)
    @Getter
    private Date started;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified")
    @Version
    @Getter
    private Date modified;

    @Column(name = "status_report")
    @Getter
    @Setter
    private String statusReport;

    @Column(name = "migration_key")
    @Getter
    @Setter
    private String migrationKey;

    @Column(name = "batch_size")
    @Getter
    @Setter
    private Integer batchSize;

    @Column(name = "mock_run")
    @Getter
    @Setter
    private Boolean mockRun;

    public BatchMigrationAuditEntry(final String migrationKey, final Integer batchSize, final Boolean mockRun) {
        this.migrationKey = migrationKey;
        this.batchSize = batchSize;
        this.mockRun = mockRun;
    }
}
