package uk.gov.hmcts.dm.service;

import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.exception.CantCreateBlobException;

import javax.persistence.EntityManager;
import java.sql.Blob;

/**
 * Created by pawel on 09/08/2017.
 */
@Service
public class BlobCreator {

    @Autowired
    @Setter
    private EntityManager entityManager;

    protected Blob createBlob(MultipartFile file) {
        try {
            Session session = entityManager.unwrap(Session.class);
            return Hibernate.getLobCreator(session).createBlob(file.getInputStream(), file.getSize());
        }catch(Exception e) {
            throw new CantCreateBlobException(e);
        }
    }

}
