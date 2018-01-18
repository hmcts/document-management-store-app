package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsAndMetadataCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.ValidationErrorException;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResourceCollection;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

/**
 * Created by pawel on 23/11/2017.
 */
@RestController
@RequestMapping(
        path = "/documents")
@ConditionalOnProperty("toggle.documentandmetadatauploadendpoint")
public class DocumentAndMetadataUploadController {

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = { V1MediaType.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE_VALUE })
    @ApiOperation("Creates a list of Stored Documents by uploading a list of binary/text files.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = StoredDocumentHalResourceCollection.class)
    })
    public ResponseEntity<Object> createFrom(
            @Valid UploadDocumentsAndMetadataCommand uploadDocumentsCommand,
            BindingResult result) {

        if (result.hasErrors()) {
            throw new ValidationErrorException(result.getFieldErrors().stream().map(fe -> String.format("%s - %s", fe.getField(), fe.getCode())).collect(Collectors.joining(",")));
        } else {
            List<StoredDocument> storedDocuments =
                    auditedStoredDocumentOperationsService.createStoredDocuments(
                            uploadDocumentsCommand.getFiles(),
                            uploadDocumentsCommand.getClassification(),
                            uploadDocumentsCommand.getRoles(),
                            uploadDocumentsCommand.getMetadata());
            return ResponseEntity
                    .ok()
                    .contentType(V1MediaType.V1_HAL_DOCUMENT_AND_METADATA_COLLECTION_MEDIA_TYPE)
                    .body(new StoredDocumentHalResourceCollection(storedDocuments));
        }
    }

}
