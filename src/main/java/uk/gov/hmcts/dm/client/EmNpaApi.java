package uk.gov.hmcts.dm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "em-npa-api", url = "${em-npa.api.url}")
public interface EmNpaApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @DeleteMapping("/api/markups/document/{documentId}")
    void deleteRedactions(
        @PathVariable("documentId") String documentId,
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization
    );
}
