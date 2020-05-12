package uk.gov.hmcts.dm.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "document_content_version")
public class DocumentContentVersionAuditEntry extends StoredDocumentAuditEntry {

    @Getter @Setter
    @ManyToOne
    private DocumentContentVersion documentContentVersion;

}
