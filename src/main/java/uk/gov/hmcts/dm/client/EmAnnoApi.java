package uk.gov.hmcts.dm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "em-anno-api", url = "${em-anno.api.url}")
public interface EmAnnoApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @DeleteMapping("/api/documents/{docId}/data")
    void deleteDocumentData(
        @PathVariable("docId") String docId,
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization
    );
}
