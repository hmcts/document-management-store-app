package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.FolderRepository;

import java.util.UUID;

@Transactional
@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    public Folder findOne(UUID id) {
        return folderRepository.findOne(id);
    }

    public StoredDocument findOneItem(UUID id, Integer index) {
        Folder folder = findOne(id);
        if (folder != null) {
            return folder.getStoredDocuments().get(index);

        }
        return null;
    }

    public void save(Folder folder) {
        folderRepository.save(folder);
    }

    public void delete(UUID id) {
        delete(findOne(id));
    }

    public void delete(Folder folder) {
        folderRepository.delete(folder);
    }


}
