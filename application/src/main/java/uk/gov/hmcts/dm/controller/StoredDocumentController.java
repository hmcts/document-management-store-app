package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResourceCollection;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RestController
@RequestMapping(path = "/documents")
@Api("Endpoint for Stored Document Management")
public class StoredDocumentController {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Autowired
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    private MethodParameter uploadDocumentsCommandMethodParameter;

    @PostConstruct
    void init() throws NoSuchMethodException {
        uploadDocumentsCommandMethodParameter = new MethodParameter(
                StoredDocumentController.class.getMethod(
                        "createFrom",
                        UploadDocumentsCommand.class,
                        BindingResult.class), 0);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Creates a list of Stored Documents by uploading a list of binary/text files.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = StoredDocumentHalResourceCollection.class)
    })
    public ResponseEntity<Object> createFrom(
            @Valid UploadDocumentsCommand uploadDocumentsCommand,
            BindingResult result) throws MethodArgumentNotValidException {

        if (result.hasErrors()) {
            throw new MethodArgumentNotValidException(uploadDocumentsCommandMethodParameter, result);
        } else {
            List<StoredDocument> storedDocuments =
                    auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand);
            return ResponseEntity
                    .ok()
                    .contentType(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE)
                    .body(new StoredDocumentHalResourceCollection(storedDocuments));
        }
    }

    @GetMapping(value = "{documentId}")
    @ApiOperation("Retrieves JSON representation of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = StoredDocumentHalResource.class)
    })
    public ResponseEntity<Object> getMetaData(@PathVariable UUID documentId) {

        StoredDocument storedDocument = auditedStoredDocumentOperationsService.readStoredDocument(documentId);

        if (storedDocument == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE)
                .body(new StoredDocumentHalResource(storedDocument));
    }

    @GetMapping(value = "{documentId}/binary")
    @ApiOperation("Streams contents of the most recent Document Content Version associated with the Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns contents of a file")
    })
    public ResponseEntity<Object> getBinary(@PathVariable UUID documentId, HttpServletResponse response) {

        DocumentContentVersion documentContentVersion =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
        response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
        response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
            format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

        try {
            if (isBlank(documentContentVersion.getContentUri())) {
                response.setHeader("data-source", "Postgres");
                auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(
                    documentContentVersion,
                    response.getOutputStream());
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

