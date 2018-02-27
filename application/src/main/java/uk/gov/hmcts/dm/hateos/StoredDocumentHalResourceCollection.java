package uk.gov.hmcts.dm.hateos;

import org.springframework.hateoas.Resources;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by pawel on 17/10/2017.
 */
public class StoredDocumentHalResourceCollection extends Resources<StoredDocumentHalResource> {

    public StoredDocumentHalResourceCollection(List<StoredDocument> storedDocuments) {
        super(storedDocuments
                .stream()
                .map(StoredDocumentHalResource::new)
                .collect(Collectors.toList()));

    }
}
