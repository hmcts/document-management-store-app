package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/documents")
@Tag(name = "DocumentThumbnail Service", description = "Endpoint for Stored Document Thumbnails")
@ConditionalOnProperty("toggle.thumbnail")
public class DocumentThumbnailController {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @GetMapping(value = "{documentId}/thumbnail")
    @Operation(summary = "Streams contents of the most recent Document Content Version associated with the"
        + "Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-roles", description = "User Roles", required = true,
                schema = @Schema(type = "string"))
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns thumbnail of a file"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getPreviewThumbnail(@PathVariable UUID documentId) {
        return documentContentVersionService
            .findMostRecentDocumentContentVersionByStoredDocumentId(documentId)
                .map(documentContentVersion ->
                ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(auditedDocumentContentVersionOperationsService
                        .readDocumentContentVersionThumbnail(documentContentVersion))
            ).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping(value = "{documentId}/versions/{versionId}/thumbnail")
    @Operation(summary = "Streams a specific version of the content of a Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns contents of a document version"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getDocumentContentVersionDocumentPreviewThumbnail(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId) {
        return documentContentVersionService.findById(versionId)
            .filter(documentContentVersion -> !documentContentVersion.getStoredDocument().isDeleted())
            .map(documentContentVersion ->
                ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(auditedDocumentContentVersionOperationsService
                        .readDocumentContentVersionThumbnail(documentContentVersion)))
            .orElse(ResponseEntity.notFound().build());
    }
}
