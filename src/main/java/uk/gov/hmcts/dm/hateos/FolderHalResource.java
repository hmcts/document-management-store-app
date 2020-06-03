package uk.gov.hmcts.dm.hateos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.core.Relation;
import uk.gov.hmcts.dm.controller.FolderController;
import uk.gov.hmcts.dm.domain.Folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
            CollectionModel<StoredDocumentHalResource> itemResources =
                    CollectionModel.of(new ArrayList<>(folder.getStoredDocuments()
                            .stream()
                            .map(StoredDocumentHalResource::new)
                        .collect(Collectors.toList())));
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
