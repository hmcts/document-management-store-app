package uk.gov.hmcts.dm.hateos;

import org.springframework.hateoas.CollectionModel;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.List;

public class StoredDocumentHalResourceCollection {

    private StoredDocumentHalResourceCollection() {
    }

    public static CollectionModel<StoredDocumentHalResource> of(List<StoredDocument> storedDocuments) {
        List<StoredDocumentHalResource> storedDocumentResource =
            storedDocuments.stream()
                .map(StoredDocumentHalResource::new)
                .toList();
        return CollectionModel.of(storedDocumentResource);
    }
}
