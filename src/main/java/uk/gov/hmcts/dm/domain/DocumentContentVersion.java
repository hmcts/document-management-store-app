package uk.gov.hmcts.dm.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.domain.RolesAware;
import uk.gov.hmcts.dm.utils.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Blob;
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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
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
    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "documentContentVersion")
    @NotNull
    @Getter
    @Setter
    @JoinColumn(name="document_content_version_id")
    private DocumentContent documentContent;

    @ManyToOne
    @Getter
    @Setter
    @NotNull
    private StoredDocument storedDocument;

    @Getter
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "documentContentVersion")
    private Set<DocumentContentVersionAuditEntry> auditEntries;

    @Getter
    @Setter
    private Long size;

    public DocumentContentVersion(StoredDocument item, MultipartFile file, Blob data) {
        this.mimeType = file.getContentType();
        setOriginalDocumentName(file.getOriginalFilename());
        this.size = file.getSize();
        this.documentContent = new DocumentContent(this, data);
        this.storedDocument = item;
    }

    public DocumentContentVersion(UUID id, String mimeType, String originalDocumentName, String createdBy,
                                  Date createdOn, DocumentContent documentContent,
                                  StoredDocument storedDocument, Set<DocumentContentVersionAuditEntry> auditEntries, Long size) {
        this.id = id;
        this.mimeType = mimeType;
        setOriginalDocumentName(originalDocumentName);
        this.createdBy = createdBy;
        setCreatedOn(createdOn);
        this.documentContent = documentContent;
        this.storedDocument = storedDocument;
        this.auditEntries = auditEntries;
        this.size = size;
    }

    public Date getCreatedOn(){
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn){
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public static class DocumentContentVersionBuilder {
        public DocumentContentVersionBuilder createdOn(Date createdOn){
            this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
            return this;
        }

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

}
