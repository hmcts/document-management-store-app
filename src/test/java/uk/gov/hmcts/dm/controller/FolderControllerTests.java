package uk.gov.hmcts.dm.controller;

import net.thucydides.core.annotations.Pending;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.security.Classifications;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FolderControllerTests extends ComponentTestBase {

    private List<MultipartFile> files = Arrays.asList(
        new MockMultipartFile("files", "filename.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)),
        new MockMultipartFile("files", "filename.txt", "text/plain", "hello2".getBytes(StandardCharsets.UTF_8)));

    @Test
    public void testGetSuccess() throws Exception {
        when(this.folderService.findById(TestUtil.RANDOM_UUID))
            .thenReturn(Optional.of(TestUtil.TEST_FOLDER));

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .get("/folders/" + TestUtil.RANDOM_UUID)
            .andExpect(status().isOk());
    }

    @Test
    public void testGetFailure() throws Exception {
        when(this.folderService.findById(TestUtil.RANDOM_UUID))
            .thenReturn(Optional.empty());

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
        given(this.folderService.findById(TestUtil.RANDOM_UUID))
            .willReturn(Optional.of(TestUtil.folder));

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/folders/" + TestUtil.RANDOM_UUID + "/documents", files, Classifications.PUBLIC, null)
            .andExpect(status().is(204));
    }

    @Test
    public void postDocumentsToFolderThatDoesNotExist() throws Exception {
        given(this.folderService.findById(TestUtil.RANDOM_UUID))
            .willReturn(Optional.empty());

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .postDocuments("/folders/" + TestUtil.RANDOM_UUID + "/documents", files, Classifications.PUBLIC, null)
            .andExpect(status().isNotFound());
    }


    @Test
    @Ignore("Code Removed at the moment called as 405")
    @Pending
    public void testDeleteSuccess() throws Exception {
        when(this.folderService.findById(TestUtil.RANDOM_UUID))
            .thenReturn(Optional.of(TestUtil.TEST_FOLDER));

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .delete("/folders/" + TestUtil.RANDOM_UUID).andExpect(status().isNoContent());

    }

    @Test
    @Ignore("Code Removed at the moment called as 405")
    @Pending
    public void testDeleteFailure() throws Exception {
        given(this.folderService.findById(TestUtil.RANDOM_UUID)).willReturn(null);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .delete("/folders/" + TestUtil.RANDOM_UUID).andExpect(status().isNotFound());
    }

}
