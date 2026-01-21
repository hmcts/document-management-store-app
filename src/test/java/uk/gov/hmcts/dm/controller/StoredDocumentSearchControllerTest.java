package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.SearchService;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredDocumentSearchControllerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private SecurityUtilService securityUtilService;

    @Mock
    private PagedResourcesAssembler<StoredDocumentHalResource> assembler;

    @Mock
    private Pageable pageable;

    @Mock
    private MetadataSearchCommand metadataSearchCommand;

    @Mock
    private StoredDocument storedDocument;

    @InjectMocks
    private StoredDocumentSearchController controller;

    @Test
    void initBinderShouldDisallowIsAdminField() {
        WebDataBinder webDataBinder = mock(WebDataBinder.class);

        controller.initBinder(webDataBinder);

        verify(webDataBinder).setDisallowedFields(Constants.IS_ADMIN);
    }

    @Test
    void searchShouldReturn200AndPagedResults() {
        List<StoredDocument> documents = Collections.singletonList(storedDocument);
        Page<StoredDocument> page = new PageImpl<>(documents);
        PagedModel<StoredDocumentHalResource> pagedModel = PagedModel.empty();

        when(searchService.findStoredDocumentsByMetadata(metadataSearchCommand, pageable)).thenReturn(page);
        when(assembler.toModel(any(Page.class))).thenReturn(pagedModel);

        ResponseEntity<Object> response = controller.search(metadataSearchCommand, pageable, assembler);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        MediaType contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.includes(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE));
        assertEquals(pagedModel, response.getBody());

        verify(searchService).findStoredDocumentsByMetadata(metadataSearchCommand, pageable);
        verify(assembler).toModel(any(Page.class));
    }

    @Test
    void searchOwnedShouldReturn200AndPagedResults() {
        String userId = "user-123";
        List<StoredDocument> documents = Collections.singletonList(storedDocument);
        Page<StoredDocument> page = new PageImpl<>(documents);
        PagedModel<StoredDocumentHalResource> pagedModel = PagedModel.empty();

        when(securityUtilService.getUserId()).thenReturn(userId);
        when(searchService.findStoredDocumentsByCreator(userId, pageable)).thenReturn(page);
        when(assembler.toModel(any(Page.class))).thenReturn(pagedModel);

        ResponseEntity<Object> response = controller.searchOwned(pageable, assembler);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        MediaType contentType = response.getHeaders().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.includes(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE));
        assertEquals(pagedModel, response.getBody());

        verify(securityUtilService).getUserId();
        verify(searchService).findStoredDocumentsByCreator(userId, pageable);
        verify(assembler).toModel(any(Page.class));
    }
}
