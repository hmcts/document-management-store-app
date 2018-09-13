package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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

import javax.validation.Valid;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by pawel on 08/06/2017.
 */
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

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Adds a Document Content Version and associates it with a given Stored Document.")
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
    public ResponseEntity<InputStreamResource> getDocumentContentVersionDocumentBinary(
        @PathVariable UUID documentId,
        @PathVariable UUID versionId) {

        DocumentContentVersion documentContentVersion = documentContentVersionService.findOne(versionId);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            throw new DocumentContentVersionNotFoundException(versionId);
        } else {
            auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(documentContentVersion);
        }

        return null;

    }

}


