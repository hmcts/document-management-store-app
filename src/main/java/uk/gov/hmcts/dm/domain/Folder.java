package uk.gov.hmcts.dm.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.hmcts.dm.security.domain.CreatorAware;

import java.util.*;
import javax.persistence.*;


@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Folder implements CreatorAware {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Getter
    @Setter
    private UUID id;

    @Getter @Setter private String name;

    @OneToMany(mappedBy = "folder")
    @OrderColumn(name = "ds_idx")
    @Getter @Setter private List<StoredDocument> storedDocuments;

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

    public Folder(UUID id, String name, List<StoredDocument> storedDocuments, String createdBy,
                  String lastModifiedBy, Date modifiedOn, Date createdOn) {
        this.id = id;
        this.name = name;
        this.storedDocuments = storedDocuments;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        setModifiedOn(modifiedOn);
        setCreatedOn(createdOn);
    }

    public Folder() {
        storedDocuments = new ArrayList<>();
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

    public static class FolderBuilder {
        public Folder.FolderBuilder modifiedOn(Date modifiedOn) {
            this.modifiedOn = (modifiedOn == null) ? null : new Date(modifiedOn.getTime());
            return this;
        }

        public Folder.FolderBuilder createdOn(Date createdOn) {
            this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
            return this;
        }
    }

}
