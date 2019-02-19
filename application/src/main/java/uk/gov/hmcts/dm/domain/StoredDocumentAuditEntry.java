package uk.gov.hmcts.dm.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = "stored_document")
public class StoredDocumentAuditEntry extends AuditEntry {

    @Getter @Setter
    @ManyToOne
    private StoredDocument storedDocument;

}
