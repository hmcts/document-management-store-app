package uk.gov.hmcts.dm.hateos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.dm.controller.DocumentThumbnailController;
import uk.gov.hmcts.dm.controller.FolderController;
import uk.gov.hmcts.dm.controller.StoredDocumentController;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "documents")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoredDocumentHalResource extends HalResource {

    private Long size;

    private String mimeType;

    private String originalDocumentName;

    private String createdBy;

    private String lastModifiedBy;

    private Date modifiedOn;

    private Date createdOn;

    private Classifications classification;

    private List<String> roles;

    private Map<String, String> metadata;

    private Date ttl;

    public StoredDocumentHalResource(@NonNull StoredDocument storedDocument) {
        BeanUtils.copyProperties(storedDocument, this);

        roles = storedDocument.getRoles() != null ? storedDocument.getRoles().stream().sorted().collect(Collectors.toList()) : null;

        DocumentContentVersion mostRecentDocumentContentVersion = storedDocument.getMostRecentDocumentContentVersion();
        if (mostRecentDocumentContentVersion != null) {
            BeanUtils.copyProperties(mostRecentDocumentContentVersion, this);
        }

        add(linkTo(methodOn(StoredDocumentController.class).getMetaData(storedDocument.getId())).withSelfRel());

        if (mostRecentDocumentContentVersion != null) {
            add(linkTo(methodOn(StoredDocumentController.class).getBinary(storedDocument.getId(), null)).withRel("binary"));
            add(linkTo(methodOn(DocumentThumbnailController.class).getPreviewThumbnail(storedDocument.getId())).withRel("thumbnail"));
        }

        if (storedDocument.getFolder() != null) {
            add(linkTo(methodOn(FolderController.class).get(storedDocument.getFolder().getId())).withRel("folder"));
        }

        if (!CollectionUtils.isEmpty(storedDocument.getDocumentContentVersions())) {
            Resources<DocumentContentVersionHalResource> versionResources =
                    new Resources<>(storedDocument.getDocumentContentVersions().stream().map(DocumentContentVersionHalResource::new).collect(Collectors.toList()));
            embedResource("allDocumentVersions", versionResources);

        }
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

}
