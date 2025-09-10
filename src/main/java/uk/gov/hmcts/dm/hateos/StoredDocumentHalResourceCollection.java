package uk.gov.hmcts.dm.hateos;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.List;
import java.util.stream.Collectors;

public class StoredDocumentHalResourceCollection {

    private StoredDocumentHalResourceCollection() {
    }

    public static RepresentationModel<?> of(List<StoredDocument> storedDocuments) {
        List<StoredDocumentHalResource> storedDocumentResource =
            storedDocuments.stream()
                .map(StoredDocumentHalResource::new)
                .collect(Collectors.toList());
        return CollectionModel.of(storedDocumentResource);
    }
}
