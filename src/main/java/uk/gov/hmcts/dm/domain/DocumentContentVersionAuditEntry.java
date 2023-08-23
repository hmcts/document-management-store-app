package uk.gov.hmcts.dm.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = "document_content_version")
public class DocumentContentVersionAuditEntry extends StoredDocumentAuditEntry {

    @Getter @Setter
    @ManyToOne
    private DocumentContentVersion documentContentVersion;

}
