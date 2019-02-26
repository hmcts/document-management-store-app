package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/documents")
@Api("Endpoint for Stored Document Thumbnails")
@ConditionalOnProperty("toggle.thumbnail")
public class DocumentThumbnailController {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @GetMapping(value = "{documentId}/thumbnail")
    @ApiOperation("Streams contents of the most recent Document Content Version associated with the Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns thumbnail of a file")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getPreviewThumbnail(@PathVariable UUID documentId) {

        DocumentContentVersion documentContentVersion =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(auditedDocumentContentVersionOperationsService.readDocumentContentVersionThumbnail(documentContentVersion));

    }


    @GetMapping(value = "{documentId}/versions/{versionId}/thumbnail")
    @ApiOperation("Streams a specific version of the content of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns contents of a document version")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getDocumentContentVersionDocumentPreviewThumbnail(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId) {

        DocumentContentVersion documentContentVersion = documentContentVersionService.findOne(versionId);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            return ResponseEntity.notFound().build();
        } else {
            return
                ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(auditedDocumentContentVersionOperationsService
                        .readDocumentContentVersionThumbnail(documentContentVersion));
        }
    }
}
