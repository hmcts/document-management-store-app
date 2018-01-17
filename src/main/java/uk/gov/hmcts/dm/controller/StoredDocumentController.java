package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResourceCollection;
import uk.gov.hmcts.dm.service.*;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;

import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.validation.Valid;

/**
 * Created by pawel on 08/06/2017.
 */
@RestController
@RequestMapping(
        path = "/documents")
@Api("Endpoint for Stored Document Management")
public class StoredDocumentController {

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Autowired
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Autowired
    private DocumentThumbnailService documentThumbnailService;

    private MethodParameter uploadDocumentsCommandMethodParamter;

    @PostConstruct
    private void init() throws Exception {
        uploadDocumentsCommandMethodParamter = new MethodParameter(
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
            throw new MethodArgumentNotValidException(uploadDocumentsCommandMethodParamter, result);
        } else {
            List<StoredDocument> storedDocuments =
                    auditedStoredDocumentOperationsService.createStoredDocuments(
                            uploadDocumentsCommand.getFiles(),
                            uploadDocumentsCommand.getClassification(),
                            uploadDocumentsCommand.getRoles(),
                            null);
            return ResponseEntity
                    .ok()
                    .contentType(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE)
                    .body(new StoredDocumentHalResourceCollection(storedDocuments));
        }
    }

    @GetMapping(value = "{id}")
    @ApiOperation("Retrieves JSON representation of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = StoredDocumentHalResource.class)
    })
    public ResponseEntity<Object> getMetaData(@PathVariable UUID id) {

        StoredDocument storedDocument = auditedStoredDocumentOperationsService.readStoredDocument(id);

        if (storedDocument == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE)
                .body(new StoredDocumentHalResource(storedDocument));
    }

    @DeleteMapping(value = "{id}")
    @ApiOperation("(Soft) Deletes a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 405, message = "Method not implemented at the moment")
    })
    public ResponseEntity<Object> delete(@PathVariable UUID id) {
        return ResponseEntity.status(405).build();
    }

    @GetMapping(value = "{id}/binary")
    @ApiOperation("Streams contents of the most recent Document Content Version associated with the Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns contents of a file")
    })
    public ResponseEntity<Object> getBinary(@PathVariable UUID id) {

        DocumentContentVersion documentContentVersion =
                documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(documentContentVersion);
        return null;

    }

    @GetMapping(value = "{id}/thumbnail")
    @ApiOperation("Streams contents of the most recent Document Content Version associated with the Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns thumbnail of a file")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getPreviewThumbnail(@PathVariable UUID id) {

        DocumentContentVersion documentContentVersion =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(id);

        if (documentContentVersion == null || documentContentVersion.getStoredDocument().isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(documentThumbnailService.generateThumbnail(documentContentVersion));

    }

}


