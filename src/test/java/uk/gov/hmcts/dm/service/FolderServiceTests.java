package uk.gov.hmcts.dm.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.repository.FolderRepository;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class FolderServiceTests {

    @Mock
    FolderRepository folderRepository;

    @InjectMocks
    FolderService folderService;

    @Test
    public void testFindOne() {

        when(this.folderRepository.findById(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(TestUtil.folder));

        Optional<Folder> folder = folderService.findById(TestUtil.RANDOM_UUID);

        Assert.assertEquals(Optional.of(TestUtil.folder), folder);

    }

    @Test
    public void testSave() {

        Folder folder = new Folder();

        folderService.save(folder);

        verify(folderRepository, times(1)).save(folder);

    }


    @Test
    public void testDelete() {

        when(this.folderRepository.findById(TestUtil.RANDOM_UUID)).thenReturn(Optional.of(TestUtil.folder));

        folderService.delete(TestUtil.RANDOM_UUID);

        verify(folderRepository, times(1)).delete(TestUtil.folder);

    }

}
