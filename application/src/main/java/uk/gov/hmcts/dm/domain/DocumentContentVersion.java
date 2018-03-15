package uk.gov.hmcts.dm.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.domain.RolesAware;
import uk.gov.hmcts.dm.utils.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by pawel on 08/06/2017.
 */
@Entity
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DocumentContentVersion implements RolesAware {

    @Id
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    private String mimeType;

    @Getter
    private String originalDocumentName;

    @Getter
    @Setter
    private String createdBy;

    @Getter
    @Setter
    @CreatedBy
    private String createdByService;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "documentContentVersion")
    private Set<DocumentContentVersionAuditEntry> auditEntries;

    @Getter
    @Setter
    private Long size;

    @ManyToOne
    @Getter
    @Setter
    @NotNull
    private StoredDocument storedDocument;

    public DocumentContentVersion(UUID id, StoredDocument storedDocument, MultipartFile file, String userId) {
        this.id = id;
        this.mimeType = file.getContentType();
        setOriginalDocumentName(file.getOriginalFilename());
        this.size = file.getSize();
        this.setCreatedBy(userId);
        this.setStoredDocument(storedDocument);
    }

    public DocumentContentVersion(UUID id, String mimeType, String originalDocumentName, String createdBy, String createdByService, Date createdOn, Set<DocumentContentVersionAuditEntry> auditEntries, Long size, StoredDocument storedDocument) {
        this.id = id;
        this.mimeType = mimeType;
        setOriginalDocumentName(originalDocumentName);
        this.createdBy = createdBy;
        this.createdByService = createdByService;
        this.createdOn = createdOn;
        this.auditEntries = auditEntries;
        this.size = size;
        this.storedDocument = storedDocument;
    }

    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setOriginalDocumentName(String originalDocumentName) {
        this.originalDocumentName = StringUtils.sanitiseFileName(originalDocumentName);
    }

    public Set<String> getRoles() {
        return getStoredDocument() != null ? getStoredDocument().getRoles() : null;
    }

    public Classifications getClassification() {
        return getStoredDocument() != null ? getStoredDocument().getClassification() : null;
    }

    public static class DocumentContentVersionBuilder {
        public DocumentContentVersionBuilder createdOn(Date createdOn) {
            this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
            return this;
        }

    }

}
