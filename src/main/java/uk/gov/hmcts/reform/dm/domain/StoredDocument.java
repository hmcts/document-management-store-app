package uk.gov.hmcts.reform.dm.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.dm.security.Classifications;
import uk.gov.hmcts.reform.dm.security.domain.RolesAware;

import java.util.*;
import javax.persistence.*;

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
    @CreatedBy
    private String createdBy;

    @Getter
    @Setter
    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Getter
    @Setter
    private boolean deleted;

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

    public StoredDocument() {
        documentContentVersions = new ArrayList<>();
    }

    public StoredDocument(UUID id, String createdBy, String lastModifiedBy, Date modifiedOn, Date createdOn,
                          boolean deleted, Folder folder, List<DocumentContentVersion> documentContentVersions,
                          Set<StoredDocumentAuditEntry> auditEntries,
                          Classifications classification, Set<String> roles, Map<String, String> metadata) {
        setId(id);
        setCreatedBy(createdBy);
        this.lastModifiedBy = lastModifiedBy;
        setModifiedOn(modifiedOn);
        setCreatedOn(createdOn);
        setDeleted(deleted);
        setFolder(folder);
        setDocumentContentVersions(documentContentVersions);
        setAuditEntries(auditEntries);
        setClassification(classification);
        setRoles(roles);
        setMetadata(metadata);
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
