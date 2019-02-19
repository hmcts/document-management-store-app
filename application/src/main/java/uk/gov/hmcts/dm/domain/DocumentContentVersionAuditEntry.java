package uk.gov.hmcts.dm.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = "document_content_version")
public class DocumentContentVersionAuditEntry extends StoredDocumentAuditEntry {

    @Getter @Setter
    @ManyToOne
    private DocumentContentVersion documentContentVersion;

}
