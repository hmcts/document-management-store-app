package uk.gov.hmcts.dm.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.security.domain.RolesAware;
import uk.gov.hmcts.dm.utils.StringUtils;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.dm.service.SecurityUtilService.sanitizedSetFrom;

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
    private String createdBy;

    @Getter
    @Setter
    @CreatedBy
    private String createdByService;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    /**
     * We will use {@link DocumentContentVersion#contentUri} instead.
     //     * @deprecated To be removed when we will migrate to AzureBlobStore.
     */
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "documentContentVersion", fetch = FetchType.LAZY)
    @Getter
    @JoinColumn(name = "document_content_version_id")
    private DocumentContent documentContent;

    @ManyToOne
    @Getter
    @Setter
    @NotNull
    private StoredDocument storedDocument;

    @Getter
    @Setter
    private Long size;

    @Getter
    @Setter
    @Column(name = "content_uri")
    private String contentUri;

    @Getter
    @Setter
    @Column(name = "content_checksum")
    private String contentChecksum;

    public DocumentContentVersion(StoredDocument item,
                                  MultipartFile file,
                                  String userId) {
        this.mimeType = file.getContentType();
        setOriginalDocumentName(file.getOriginalFilename());
        this.size = file.getSize();
        this.storedDocument = item;
        this.setCreatedBy(userId);
    }

    public DocumentContentVersion(UUID id,
                                  String mimeType,
                                  String originalDocumentName,
                                  String createdBy,
                                  String createdByService,
                                  Date createdOn,
                                  DocumentContent documentContent,
                                  StoredDocument storedDocument,
                                  Long size,
                                  String contentUri,
                                  String contentChecksum) {
        this.id = id;
        this.mimeType = mimeType;
        setOriginalDocumentName(originalDocumentName);
        this.createdBy = createdBy;
        setCreatedOn(createdOn);
        setCreatedByService(createdByService);
        this.storedDocument = storedDocument;
        this.size = size;
        this.contentUri = contentUri;
        this.contentChecksum = contentChecksum;
    }

    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public static class DocumentContentVersionBuilder {
        public DocumentContentVersionBuilder createdOn(Date createdOn) {
            this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
            return this;
        }
    }

    public void setOriginalDocumentName(String originalDocumentName) {
        this.originalDocumentName = StringUtils.sanitiseFileName(originalDocumentName);
    }

    public Set<String> getRoles() {
        return getStoredDocument() != null ? sanitizedSetFrom(getStoredDocument().getRoles()) : null;
    }

    public Classifications getClassification() {
        return getStoredDocument() != null ? getStoredDocument().getClassification() : null;
    }
}
