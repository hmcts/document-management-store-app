package uk.gov.hmcts.dm.service;

import lombok.NonNull;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.exception.CantCreateBlobException;

import javax.persistence.EntityManager;
import java.sql.Blob;

@Service
public class BlobCreator {

    @Autowired
    private EntityManager entityManager;

    protected Blob createBlob(@NonNull MultipartFile file) {
        try {
            Session session = entityManager.unwrap(Session.class);
            return Hibernate.getLobCreator(session).createBlob(file.getInputStream(), file.getSize());
        } catch (Exception e) {
            throw new CantCreateBlobException(e);
        }
    }

}
