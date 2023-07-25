package uk.gov.hmcts.dm.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "stored_document")
public class StoredDocumentAuditEntry extends AuditEntry {

    @Getter @Setter
    @ManyToOne
    private StoredDocument storedDocument;

}
