package uk.gov.hmcts.dm.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "document_content_version")
public class DocumentContentVersionAuditEntry extends StoredDocumentAuditEntry {

    @Getter @Setter
    @ManyToOne
    private DocumentContentVersion documentContentVersion;

}
