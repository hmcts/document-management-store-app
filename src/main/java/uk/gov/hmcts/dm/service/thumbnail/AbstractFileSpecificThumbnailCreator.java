package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

public abstract class AbstractFileSpecificThumbnailCreator implements FileSpecificThumbnailCreator {
    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        try {
            InputStream inputStream = documentContentVersion.getDocumentContent().getData().getBinaryStream();
            BufferedImage bufferedImage = getImg(inputStream);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage,THUMBNAIL_FORMAT, os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }

    abstract BufferedImage getImg(InputStream inputStream);
}
