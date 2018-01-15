package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class PdfThumbnailService implements FileSpecificThumbnailCreator {
    public static final List<String> SUPPORTED_MIME_TYPES = Collections.singletonList(
        MediaType.APPLICATION_PDF_VALUE
    );

    private final ImageResizeService imageResizeService;

    @Autowired
    public PdfThumbnailService(ImageResizeService imageResizeService){
        this.imageResizeService = imageResizeService;
    }

    @Override
    public boolean supports(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType);
    }

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        try {
            InputStream inputStream = documentContentVersion.getDocumentContent().getData().getBinaryStream();
            BufferedImage bImage = getPdfImage(inputStream);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bImage,THUMBNAIL_FORMAT, os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static BufferedImage getPdfImage(InputStream pdf) {
        try {
            PDDocument document = PDDocument.load(pdf);

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            if(document.getNumberOfPages() > 0){
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);

                Image scaledInstance = bufferedImage.getScaledInstance(DEFAULT_WIDTH,ImageResizeService.apsectRatio(bufferedImage), Image.SCALE_SMOOTH);
                BufferedImage bImage = new BufferedImage(
                    scaledInstance.getWidth(null),
                    scaledInstance.getHeight(null),
                    BufferedImage.TYPE_INT_RGB
                );

                Graphics2D bGr = bImage.createGraphics();
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


                document.close();

                return bImage;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
