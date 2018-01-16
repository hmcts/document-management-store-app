package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public abstract class AbstractFileSpecificThumbnailCreator implements FileSpecificThumbnailCreator{
    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        try {
            InputStream inputStream = documentContentVersion.getDocumentContent().getData().getBinaryStream();
            BufferedImage bImage = getImg(inputStream);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bImage,THUMBNAIL_FORMAT, os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

     abstract BufferedImage getImg(InputStream inputStream);
}
