package uk.gov.hmcts.reform.dm.service.thumbnail;

import uk.gov.hmcts.reform.dm.exception.CantCreateThumbnailException;
import uk.gov.hmcts.reform.dm.service.BlobStorageReadService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ImageThumbnailCreator extends AbstractFileSpecificThumbnailCreator {

    public ImageThumbnailCreator(BlobStorageReadService blobStorageReadService) {
        super(blobStorageReadService);
    }

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
