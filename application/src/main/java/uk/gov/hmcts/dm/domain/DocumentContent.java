package uk.gov.hmcts.dm.domain;

import java.sql.Blob;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.dm.dialect.ByteWrappingBlobType;

/**
 * Make checkstyles happy with a javadoc summary.
 * @deprecated To be removed when we will migrate to AzureBlobStore.
 */
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "BlobDataUserType", typeClass = ByteWrappingBlobType.class)
@Deprecated
public class DocumentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @JsonIgnore
    @Getter
    @Setter
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "BlobDataUserType")
    private Blob data;

    @OneToOne
    @Getter
    @Setter
    private DocumentContentVersion documentContentVersion;

    @Getter
    @Setter
    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;



    public DocumentContent(Blob blob) {
        this.data = blob;
    }

    public DocumentContent(DocumentContentVersion documentContentVersion, Blob blob) {
        this(blob);
        this.documentContentVersion = documentContentVersion;
    }


    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

}
