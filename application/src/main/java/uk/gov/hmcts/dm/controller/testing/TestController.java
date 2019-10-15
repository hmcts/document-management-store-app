package uk.gov.hmcts.dm.controller.testing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/testing")
@ConditionalOnProperty("toggle.testing")
public class TestController {

    private final BlobStorageReadService blobStorageReadService;

    public TestController(BlobStorageReadService blobStorageReadService) {
        this.blobStorageReadService = blobStorageReadService;
    }

    @GetMapping("/azure-storage-binary-exists/{id}")
    public ResponseEntity<Boolean> get(@PathVariable UUID id) throws Exception {
        return ResponseEntity.ok(blobStorageReadService.doesBinaryExist(id));
    }


}
