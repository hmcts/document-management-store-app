package uk.gov.hmcts.dm.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by pawel on 24/07/2017.
 */
@Entity
@DiscriminatorValue(value = "stored_document")
public class StoredDocumentAuditEntry extends AuditEntry {

    @Getter @Setter
    @ManyToOne
    private StoredDocument storedDocument;

}
