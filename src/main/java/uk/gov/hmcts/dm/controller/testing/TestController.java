package uk.gov.hmcts.dm.controller.testing;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(
    path = "/testing")
@ConditionalOnProperty("toggle.testing")
public class TestController {

    private final BlobStorageReadService blobStorageReadService;

    private final BlobContainerClient blobClient;

    public TestController(
        BlobStorageReadService blobStorageReadService,
        @Autowired(required = false) @Qualifier("metadata-storage") BlobContainerClient blobClient
    ) {
        this.blobStorageReadService = blobStorageReadService;
        this.blobClient = blobClient;
    }

    @GetMapping("/azure-storage-binary-exists/{id}")
    public ResponseEntity<Boolean> get(@PathVariable UUID id) {
        return ResponseEntity.ok(blobStorageReadService.doesBinaryExist(id));
    }

    @PostMapping(value = "/metadata-migration-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Boolean> uploadCsv(@Valid UploadDocumentsCommand command) throws Exception {
        MultipartFile file = command.getFiles().get(0);
        BlockBlobClient client = blobClient.getBlobClient(file.getName()).getBlockBlobClient();

        try {
            client.delete();
        } catch (BlobStorageException ignored) {
            // ignored
        }

        client.upload(file.getInputStream(), file.getSize());

        return ResponseEntity.ok(true);
    }

}
