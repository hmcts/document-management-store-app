package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;

import java.util.UUID;

/**
 * Created by pawel on 08/06/2017.
 */
@RestController
@RequestMapping(
    path = "/documents/")
@Api("Endpoint for Deletion of Documents")
@ConditionalOnProperty("toggle.deleteenabled")
public class DeleteStoredDocumentController {

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @DeleteMapping(value = "{id}")
    @ApiOperation("Deletes a Stored Document.")
    public ResponseEntity<Object> deleteDocument(@PathVariable
                                                        UUID id,
                                                    @RequestParam(
                                                        value = "permanent",
                                                        required = false,
                                                        defaultValue = "false")
                                                        boolean permanent) {

        auditedStoredDocumentOperationsService.deleteStoredDocument(id, permanent);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}


