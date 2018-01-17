package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class ImageResizeService extends AbstractFileSpecificThumbnailCreator {

    public static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
        MediaType.IMAGE_GIF_VALUE,
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE
    );

    @Override
    public boolean supports(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType);
    }

    public BufferedImage getImg(InputStream img) {
        try {
            BufferedImage bufferedImage = ImageIO.read(img);
            return resizeImage(bufferedImage);
        } catch (IOException e) {
            throw new CantCreateThumbnailException(e);
        }
    }

    public static BufferedImage resizeImage(BufferedImage bufferedImage) {
        Image scaledInstance = bufferedImage.getScaledInstance(DEFAULT_WIDTH, aspectRatio(bufferedImage), Image.SCALE_SMOOTH);

        BufferedImage resizedBuffedImage = new BufferedImage(
            scaledInstance.getWidth(null),
            scaledInstance.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        );

        Graphics2D resizedBuffedImageGraphics = resizedBuffedImage.createGraphics();

        // Apply scaled Image to the Buffered Image Graphic.
        resizedBuffedImageGraphics.drawImage(
            scaledInstance,
            0,
            0,
            scaledInstance.getWidth(null),
            scaledInstance.getHeight(null),
            Color.WHITE,
            null
        );

        resizedBuffedImageGraphics.dispose();

        return resizedBuffedImage;
    }

    public static int aspectRatio(BufferedImage bufferedImage) {
        return aspectRatio(bufferedImage.getHeight(),bufferedImage.getWidth());
    }

    public static int aspectRatio(int h, int w) {
        float ratio = (float) DEFAULT_WIDTH / w;
        return Math.round(h * ratio);
    }

}
