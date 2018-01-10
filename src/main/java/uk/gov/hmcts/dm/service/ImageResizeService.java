package uk.gov.hmcts.dm.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageResizeService implements ThumbnailCreator {
    public static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
        MediaType.IMAGE_GIF_VALUE,
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE
    );

    public static BufferedImage resizeImage(InputStream inputStream){
        try {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            Image scaledInstance = bufferedImage.getScaledInstance(DEFAULT_WIDTH,apsectRatio(bufferedImage), Image.SCALE_SMOOTH);
            BufferedImage bimage = new BufferedImage(
                scaledInstance.getWidth(null),
                scaledInstance.getHeight(null),
                BufferedImage.TYPE_INT_RGB
            );

            Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(
                scaledInstance,
                0,
                0,
                scaledInstance.getWidth(null),
                scaledInstance.getHeight(null),
                Color.WHITE,
                null
            );

            bGr.dispose();

            return bimage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int apsectRatio(BufferedImage bufferedImage){
        return apsectRatio(bufferedImage.getHeight(),bufferedImage.getWidth());
    }

    public static int apsectRatio(int h, int w){
        float ratio = (float) DEFAULT_WIDTH / w;
        return Math.round(h * ratio);
    }

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        try {
            BufferedImage resizedImage = resizeImage(documentContentVersion.getDocumentContent().getData().getBinaryStream());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resizedImage,THUMBNAIL_FORMAT, os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType);
    }
}
