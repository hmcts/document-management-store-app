package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ImageThumbnailCreator extends AbstractFileSpecificThumbnailCreator {

    @Override
    public BufferedImage getImg(InputStream img) {
        try {
            BufferedImage bufferedImage = ImageIO.read(img);
            return new BufferedImageResizer().resizeImage(bufferedImage);
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }



}
