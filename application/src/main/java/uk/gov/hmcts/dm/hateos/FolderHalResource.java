package uk.gov.hmcts.dm.hateos;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.Relation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.dm.controller.FolderController;
import uk.gov.hmcts.dm.domain.Folder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "folders")
public class FolderHalResource extends HalResource {

    private String name;

    private String createdBy;

    private String lastModifiedBy;

    private Date modifiedOn;

    private Date createdOn;

    public FolderHalResource(Folder folder) {
        BeanUtils.copyProperties(folder, this);
        if (folder.getStoredDocuments() != null) {
            Resources<StoredDocumentHalResource> itemResources =
                    new Resources<>(folder.getStoredDocuments()
                            .stream()
                            .map(StoredDocumentHalResource::new)
                        .collect(Collectors.toList()));
            embedResource("items", itemResources);
        }
        add(linkTo(methodOn(FolderController.class).get(folder.getId())).withSelfRel());
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
