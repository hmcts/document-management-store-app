package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.UploadDocumentVersionCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.exception.ValidationErrorException;
import uk.gov.hmcts.dm.hateos.DocumentContentVersionHalResource;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
    path = "/documents/{documentId}")
@Tag(name = "DocumentContentVersion Service", description = "Endpoint for Document Content Version")
public class DocumentContentVersionController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {

        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    private final DocumentContentVersionService documentContentVersionService;

    private final AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    private final StoredDocumentService storedDocumentService;

    private final AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Autowired
    public DocumentContentVersionController(DocumentContentVersionService documentContentVersionService,
                                            AuditedDocumentContentVersionOperationsService
                                                auditedDocumentContentVersionOperationsService,
                                            StoredDocumentService storedDocumentService,
                                            AuditedStoredDocumentOperationsService
                                                    auditedStoredDocumentOperationsService) {
        this.documentContentVersionService = documentContentVersionService;
        this.auditedDocumentContentVersionOperationsService = auditedDocumentContentVersionOperationsService;
        this.storedDocumentService = storedDocumentService;
        this.auditedStoredDocumentOperationsService = auditedStoredDocumentOperationsService;
    }

    // Please do not remove "" mapping. API is already consumed and might break backwards compatibility.
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adds a Document Content Version and associates it with a given Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "JSON representation of a document version"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> addDocumentContentVersionForVersionsMappingNotPresent(@PathVariable UUID documentId,
                                                            @Valid UploadDocumentVersionCommand command,
                                                            BindingResult result) {
        return addDocumentContentVersion(documentId, command, result);
    }

    @PostMapping(value = "/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adds a Document Content Version and associates it with a given Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "JSON representation of a document version"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> addDocumentContentVersion(@PathVariable UUID documentId,
                                                            @Valid UploadDocumentVersionCommand command,
                                                            BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationErrorException(result.getFieldErrors().stream()
                .map(fe -> String.format("%s - %s", fe.getField(), fe.getCode()))
                .collect(Collectors.joining(",")));
        } else {
            StoredDocument storedDocument = storedDocumentService.findOne(documentId)
                .orElseThrow(() -> new StoredDocumentNotFoundException(documentId));

            DocumentContentVersionHalResource resource =
                new DocumentContentVersionHalResource(
                    auditedStoredDocumentOperationsService.addDocumentVersion(storedDocument, command.getFile())
                );

            return ResponseEntity
                .created(resource.getUri())
                .contentType(V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE)
                .body(resource);

        }
    }

    @GetMapping(value = "/versions/{versionId}")
    @Operation(summary = "Returns a specific version of the content of a Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-roles", description = "User Roles", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "JSON representation of a document version"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> getDocumentContentVersionDocument(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId) {

        DocumentContentVersion documentContentVersion =
            auditedDocumentContentVersionOperationsService.readDocumentContentVersion(versionId);

        return ResponseEntity
            .ok()
            .contentType(V1MediaType.V1_HAL_DOCUMENT_CONTENT_VERSION_MEDIA_TYPE)
            .body(new DocumentContentVersionHalResource(documentContentVersion));

    }

    @GetMapping(value = "/versions/{versionId}/binary")
    @Operation(summary = "Streams a specific version of the content of a Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-roles", description = "User Roles", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns contents of a document version"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> getDocumentContentVersionDocumentBinary(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId,
        HttpServletRequest request,
        HttpServletResponse response) {

        DocumentContentVersion documentContentVersion = documentContentVersionService.findById(versionId)
            .orElseThrow(() -> new DocumentContentVersionNotFoundException(versionId));

        if (documentContentVersion.getStoredDocument().isDeleted()) {
            throw new DocumentContentVersionNotFoundException(versionId);
        }

        response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
        response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
        response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
            String.format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

        try {
            response.setHeader("data-source", "contentURI");
            auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
                documentContentVersion, request,
                response);
            response.flushBuffer();

        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e);
        }

        return null;
    }
}


