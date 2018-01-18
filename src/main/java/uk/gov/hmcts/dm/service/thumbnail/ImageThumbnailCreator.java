package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ImageThumbnailCreator extends AbstractFileSpecificThumbnailCreator {

    @Override
    public BufferedImage getImg(InputStream img) {
        try {
            BufferedImage bufferedImage = ImageIO.read(img);
            return resizeImage(bufferedImage);
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }

    public static BufferedImage resizeImage(BufferedImage bufferedImage) {
        try {
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
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }

    public static int aspectRatio(BufferedImage bufferedImage) {
        return aspectRatio(bufferedImage.getHeight(),bufferedImage.getWidth());
    }

    public static int aspectRatio(int h, int w) {
        float ratio = (float) DEFAULT_WIDTH / w;
        return Math.round(h * ratio);
    }

}
