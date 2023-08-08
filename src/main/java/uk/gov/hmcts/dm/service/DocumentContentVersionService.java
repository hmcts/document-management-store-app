package uk.gov.hmcts.dm.service;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.DocumentDaoImpl;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentContentVersionService {

    @Autowired
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    @Autowired
    private DocumentDaoImpl documentDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Optional<DocumentContentVersion> findById(UUID id) {
        return documentContentVersionRepository.findById(id);
    }

    public Optional<DocumentContentVersion> findMostRecentDocumentContentVersionByStoredDocumentId(UUID id) {
        HikariDataSource dataSource = (HikariDataSource) jdbcTemplate.getDataSource();
        System.out.println("Active in DocumentContentVersionService : " + dataSource.getHikariPoolMXBean().getActiveConnections());
        System.out.println("Idle in DocumentContentVersionService : " + dataSource.getHikariPoolMXBean().getIdleConnections());

//        Optional<DocumentContentVersion> documentContentVersion = storedDocumentRepository
//                    .findByIdAndDeleted(id, false)
//                    .map(StoredDocument::getMostRecentDocumentContentVersion);

        //Need ot check if deleted flag is set to true for StoredDocument
        return Optional.ofNullable(documentDao.getRecentDocumentContentVersion(id));
    }

}
