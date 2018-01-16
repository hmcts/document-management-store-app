package uk.gov.hmcts.dm.service.thumbnail;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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

    public BufferedImage getImg(InputStream img){
        try {
            BufferedImage bufferedImage = ImageIO.read(img);
            return resizeImage(bufferedImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage resizeImage(BufferedImage bufferedImage) {
        Image scaledInstance = bufferedImage.getScaledInstance(DEFAULT_WIDTH,apsectRatio(bufferedImage), Image.SCALE_SMOOTH);

        BufferedImage bimage = new BufferedImage(
            scaledInstance.getWidth(null),
            scaledInstance.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        );

        Graphics2D bGr = bimage.createGraphics();

        // Apply scaled Image to the Buffered Image Graphic.
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
    }

//    Unsure if needed
//    public static int getWidth(InputStream img){
//        try {
//            BufferedImage bufferedImage = ImageIO.read(img);
//            return bufferedImage.getWidth();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static int getHeight(InputStream img){
//        try {
//            BufferedImage bufferedImage = ImageIO.read(img);
//            return bufferedImage.getHeight();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static int apsectRatio(InputStream img){
//        try {
//            BufferedImage bufferedImage = ImageIO.read(img);
//            return apsectRatio(bufferedImage.getHeight(),bufferedImage.getWidth());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static int apsectRatio(BufferedImage bufferedImage){
        return apsectRatio(bufferedImage.getHeight(),bufferedImage.getWidth());
    }

    public static int apsectRatio(int h, int w){
        float ratio = (float) DEFAULT_WIDTH / w;
        return Math.round(h * ratio);
    }

}
