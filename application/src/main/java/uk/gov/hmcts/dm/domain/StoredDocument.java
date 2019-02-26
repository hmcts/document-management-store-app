package uk.gov.hmcts.dm.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.domain.RolesAware;

import javax.persistence.*;
import java.util.*;

@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
public class StoredDocument implements RolesAware {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String createdBy;

    @Getter
    @Setter
    @CreatedBy
    private String createdByService;

    @Getter
    @Setter
    private String lastModifiedBy;

    @Getter
    @Setter
    @LastModifiedBy
    private String lastModifiedByService;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Getter
    @Setter
    private boolean deleted;

    @Getter
    @Setter
    private boolean hardDeleted;

    @ManyToOne
    @Getter
    @Setter
    private Folder folder;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storedDocument")
    @OrderColumn(name = "itm_idx")
    private List<DocumentContentVersion> documentContentVersions;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "storedDocument")
    private Set<StoredDocumentAuditEntry> auditEntries;

    @Getter
    @Setter
    @Enumerated
    private Classifications classification;

    @ElementCollection
    @Getter
    @Setter
    @CollectionTable(name = "documentroles", joinColumns = @JoinColumn(name = "documentroles_id"))
    private Set<String> roles;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @Getter
    @Setter
    @CollectionTable(name = "documentmetadata", joinColumns = @JoinColumn(name = "documentmetadata_id"))
    private Map<String, String> metadata;

    @Getter
    @Setter
    private Date ttl;

    public StoredDocument() {
        documentContentVersions = new ArrayList<>();
    }

    public StoredDocument(UUID id, String createdBy, String createdByService, String lastModifiedBy, String lastModifiedByService,
                          Date modifiedOn, Date createdOn,
                          boolean deleted, boolean hardDeleted, Folder folder, List<DocumentContentVersion> documentContentVersions,
                          Set<StoredDocumentAuditEntry> auditEntries,
                          Classifications classification, Set<String> roles, Map<String, String> metadata, Date ttl) {
        setId(id);
        setCreatedBy(createdBy);
        setCreatedByService(createdByService);
        this.lastModifiedBy = lastModifiedBy;
        this.setLastModifiedByService(lastModifiedByService);
        setModifiedOn(modifiedOn);
        setCreatedOn(createdOn);
        setDeleted(deleted);
        setHardDeleted(hardDeleted);
        setFolder(folder);
        setDocumentContentVersions(documentContentVersions);
        setAuditEntries(auditEntries);
        setClassification(classification);
        setRoles(roles);
        setMetadata(metadata);
        setTtl(ttl);
    }

    public DocumentContentVersion getMostRecentDocumentContentVersion() {
        return CollectionUtils.isEmpty(documentContentVersions) ? null : documentContentVersions.get(documentContentVersions.size() - 1);
    }

    public Date getModifiedOn() {
        return (modifiedOn == null) ? null : new Date(modifiedOn.getTime());
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = (modifiedOn == null) ? null : new Date(modifiedOn.getTime());
    }

    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public static class StoredDocumentBuilder {
        public StoredDocumentBuilder modifiedOn(Date modifiedOn) {
            this.modifiedOn = (modifiedOn == null) ? null : new Date(modifiedOn.getTime());
            return this;
        }

        public StoredDocumentBuilder createdOn(Date createdOn) {
            this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
            return this;
        }
    }

}
