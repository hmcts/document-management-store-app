package uk.gov.hmcts.dm.service;

import java.io.InputStream;
import java.sql.Blob;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;
import uk.gov.hmcts.dm.exception.CantCreateBlobException;

@Service
public class BlobCreator {

    @Autowired
    private EntityManager entityManager;

    protected Blob createBlob(@NonNull MultipartFile file) {
        try (final InputStream inputStream = file.getInputStream()) {
            Session session = entityManager.unwrap(Session.class);
            return Hibernate.getLobCreator(session).createBlob(inputStream, file.getSize());
        } catch (Exception e) {
            throw new CantCreateBlobException(e);
        }
    }

}
