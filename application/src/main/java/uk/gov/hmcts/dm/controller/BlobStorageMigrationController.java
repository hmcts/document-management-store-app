package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.hateos.DocumentContentVersionHalResource;
import uk.gov.hmcts.dm.service.BlobStorageMigrationService;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@Api("Endpoint for Document Content Migration from PostgreSQL to Azure BlobStorage")
public class BlobStorageMigrationController {

    private final BlobStorageMigrationService blobStorageMigrationService;

    public BlobStorageMigrationController(BlobStorageMigrationService blobStorageMigrationService) {
        this.blobStorageMigrationService = blobStorageMigrationService;
    }

    @PostMapping(value = "/documents/{documentId}/versions/{versionId}/migrate")
    @ApiOperation("Starts migration for a specific version of the content of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "JSON representation of a document version", response = DocumentContentVersionHalResource.class)
    })
    public ResponseEntity<Object> migrateDocument(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId) {

        blobStorageMigrationService.migrateDocumentContentVersion(documentId, versionId);

        return ResponseEntity.status(NO_CONTENT).build();
    }
}
