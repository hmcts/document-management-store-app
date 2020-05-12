package uk.gov.hmcts.dm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.repository.FolderRepository;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    public Optional<Folder> findById(UUID id) {
        return folderRepository.findById(id);
    }

    public void save(Folder folder) {
        folderRepository.save(folder);
    }

    public void delete(UUID id) {
        delete(findById(id));
    }

    public void delete(Optional<Folder> maybeFolder) {
        maybeFolder.ifPresent(folder -> folderRepository.delete(folder));
    }


}
