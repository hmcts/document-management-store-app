package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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
import uk.gov.hmcts.dm.service.DocumentContentVersionService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@RestController
@RequestMapping(
    path = "/documents/{documentId}")
@Api("Endpoint for Document Content Version")
public class DocumentContentVersionController {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Autowired
    private StoredDocumentService storedDocumentService;

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    // Please do not remove "" mapping. API is already consumed and might break backwards compatibility.
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Adds a Document Content Version and associates it with a given Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "JSON representation of a document version", response = DocumentContentVersionHalResource.class)
    })
    public ResponseEntity<Object> addDocumentContentVersionForVersionsMappingNotPresent(@PathVariable UUID documentId,
                                                            @Valid UploadDocumentVersionCommand command,
                                                            BindingResult result) {
        return addDocumentContentVersion(documentId, command, result);
    }

    @PostMapping(value = "/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Adds a Document Content Version and associates it with a given Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "JSON representation of a document version", response = DocumentContentVersionHalResource.class)
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
    @ApiOperation("Returns a specific version of the content of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "JSON representation of a document version", response = DocumentContentVersionHalResource.class)
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
    @ApiOperation("Streams a specific version of the content of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns contents of a document version")
    })
    public ResponseEntity<Object> getDocumentContentVersionDocumentBinary(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId,
        HttpServletResponse response) {

        DocumentContentVersion documentContentVersion = documentContentVersionService.findOne(versionId);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            throw new DocumentContentVersionNotFoundException(versionId);
        }

        response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
        response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
        response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
            String.format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

        try {
            if (isBlank(documentContentVersion.getContentUri())) {
                response.setHeader("data-source", "Postgres");
                auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(documentContentVersion, response.getOutputStream());
            } else {
                response.setHeader("data-source", "contentURI");
                auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
                    documentContentVersion,
                    response.getOutputStream());
            }
            response.flushBuffer();

        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e);
        }

        return null;
    }
}


