package uk.gov.hmcts.dm.controller;

import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FolderControllerTests extends ComponentTestBase {

    @Test
    public void testGetSuccess() throws Exception {
        when(this.folderService.findOne(TestUtil.RANDOM_UUID))
            .thenReturn(TestUtil.TEST_FOLDER);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/folders/" + TestUtil.RANDOM_UUID)
            .andExpect(status().isOk());
    }

    @Test
    public void testGetFailure() throws Exception {
        when(this.folderService.findOne(TestUtil.RANDOM_UUID))
            .thenReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/folders/" + TestUtil.RANDOM_UUID)
            .andExpect(status().isNotFound());
    }


    @Test
    public void testPostSuccess() throws Exception {
        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/folders/", TestUtil.TEST_FOLDER).andExpect(status().isOk());
    }


    @Test
    public void postDocuments() throws Exception {
        given(this.folderService.findOne(TestUtil.RANDOM_UUID))
            .willReturn(TestUtil.folder);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/folders/" + TestUtil.RANDOM_UUID + "/documents", Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList()), Classifications.PUBLIC, null)
            .andExpect(status().is(204));
    }

    @Test
    public void postDocumentsToFolderThatDoesNotExist() throws Exception {
        given(this.folderService.findOne(TestUtil.RANDOM_UUID))
            .willReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/folders/" + TestUtil.RANDOM_UUID + "/documents", Stream.of(TestUtil.TEST_FILE).collect(Collectors.toList()), Classifications.PUBLIC, null)
            .andExpect(status().isNotFound());
    }


    @Test
    @Ignore("Code Removed at the moment called as 405")
    public void testDeleteSuccess() throws Exception {
        when(this.folderService.findOne(TestUtil.RANDOM_UUID))
            .thenReturn(TestUtil.TEST_FOLDER);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .delete("/folders/" + TestUtil.RANDOM_UUID).andExpect(status().isNoContent());

    }

    @Test
    @Ignore("Code Removed at the moment called as 405")
    public void testDeleteFailure() throws Exception {
        given(this.folderService.findOne(TestUtil.RANDOM_UUID)).willReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .delete("/folders/" + TestUtil.RANDOM_UUID).andExpect(status().isNotFound());
    }

}
