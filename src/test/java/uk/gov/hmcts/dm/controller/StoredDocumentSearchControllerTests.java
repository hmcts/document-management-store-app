package uk.gov.hmcts.dm.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoredDocumentSearchControllerTests extends ComponentTestBase {

    @Mock
    SecurityUtilService securityUtilService;

    @Test
    void testValidCommandAndSearchReturn3Documents() throws Exception {
        MetadataSearchCommand searchCommand = new MetadataSearchCommand("name", "thename");

        List<StoredDocument> documents = Arrays.asList(
            new StoredDocument(),
            new StoredDocument(),
            new StoredDocument());

        PageRequest pageRequest = PageRequest.of(0, 2);

        when(
            this.searchService
                .findStoredDocumentsByMetadata(eq(searchCommand), any(Pageable.class)))
            .thenReturn(new PageImpl<>(documents, pageRequest, 3));

        restActions
            .withAuthorizedUser("userId")
            .post("/documents/filter", searchCommand)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.size", is(2)))
            .andExpect(jsonPath("$.page.totalElements", is(3)))
            .andExpect(jsonPath("$.page.totalPages", is(2)))
            .andExpect(jsonPath("$.page.number", is(0)))
            .andExpect(jsonPath("$._links.first.href", is("http://localhost/documents/filter?page=0&size=2")))
            .andExpect(jsonPath("$._links.self.href", is("http://localhost/documents/filter?page=0&size=2")))
            .andExpect(jsonPath("$._links.next.href", is("http://localhost/documents/filter?page=1&size=2")))
            .andExpect(jsonPath("$._links.last.href", is("http://localhost/documents/filter?page=1&size=2")));
    }

    @Test
    void testInValidCommandAnd() throws Exception {
        MetadataSearchCommand searchCommand = new MetadataSearchCommand("thename", null);

        restActions
            .withAuthorizedUser("userId")
            .post("/documents/filter", searchCommand)
            .andExpect(status().is4xxClientError());
    }

    @Test
    void testInitBinder() {

        WebDataBinder webDataBinder = new WebDataBinder(null);

        assertNull(webDataBinder.getDisallowedFields());
        new StoredDocumentSearchController(searchService, securityUtilService).initBinder(webDataBinder);
        assertTrue(Arrays.asList(webDataBinder.getDisallowedFields()).contains(Constants.IS_ADMIN));
    }
}
