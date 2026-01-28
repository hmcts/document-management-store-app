package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.client.EmNpaApi;

@Service
public class EmNpaService {

    private static final Logger log = LoggerFactory.getLogger(EmNpaService.class);

    private final EmNpaApi emNpaApi;

    public EmNpaService(EmNpaApi emNpaApi) {
        this.emNpaApi = emNpaApi;
    }

    public void deleteRedactionsForDocument(String documentId, String userToken, String serviceToken) {
        log.info("Deleting redactions for document: {}", documentId);
        try {
            emNpaApi.deleteRedactions(documentId, userToken, serviceToken);
            log.info("Successfully deleted redactions for document: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to delete redactions for document: {}", documentId, e);
            throw e;
        }
    }
}
