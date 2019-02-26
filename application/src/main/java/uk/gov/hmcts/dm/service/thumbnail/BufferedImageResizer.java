package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImageResizer {

    public BufferedImage resizeImage(BufferedImage bufferedImage) {
        try {

            Image scaledInstance = bufferedImage.getScaledInstance(
                ThumbnailWidths.WIDTH_256.getWidth(),
                aspectRatio(bufferedImage),
                Image.SCALE_SMOOTH);

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

    private int aspectRatio(BufferedImage bufferedImage) {
        return aspectRatio(bufferedImage.getHeight(),bufferedImage.getWidth());
    }

    private int aspectRatio(int h, int w) {
        float ratio = (float) ThumbnailWidths.WIDTH_256.getWidth() / w;
        return Math.round(h * ratio);
    }
}
