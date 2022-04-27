package uk.gov.hmcts.dm.hateos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.core.Relation;
import uk.gov.hmcts.dm.controller.DocumentContentVersionController;
import uk.gov.hmcts.dm.controller.DocumentThumbnailController;
import uk.gov.hmcts.dm.controller.StoredDocumentController;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.util.Date;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "documentVersions")
public class DocumentContentVersionHalResource extends HalResource {

    private Long size;

    private String mimeType;

    private String originalDocumentName;

    private String createdBy;

    private Date createdOn;

    public DocumentContentVersionHalResource(DocumentContentVersion documentContentVersion) {
        BeanUtils.copyProperties(documentContentVersion, this);

        add(linkTo(methodOn(StoredDocumentController.class)
            .getMetaData(documentContentVersion.getStoredDocument().getId())).withRel("document"));

        add(linkTo(methodOn(DocumentContentVersionController.class).getDocumentContentVersionDocument(
            documentContentVersion.getStoredDocument().getId(),
            documentContentVersion.getId())).withRel("self"));

        add(linkTo(methodOn(DocumentContentVersionController.class).getDocumentContentVersionDocumentBinary(
            documentContentVersion.getStoredDocument().getId(),
            documentContentVersion.getId(), null, null)).withRel("binary"));

        add(linkTo(methodOn(DocumentThumbnailController.class).getDocumentContentVersionDocumentPreviewThumbnail(
            documentContentVersion.getStoredDocument().getId(),
            documentContentVersion.getId())).withRel("thumbnail"));
    }

    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

}
