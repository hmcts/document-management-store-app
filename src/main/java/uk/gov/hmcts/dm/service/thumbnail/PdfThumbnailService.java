package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import java.util.List;

@Service
public class PdfThumbnailService extends AbstractFileSpecificThumbnailCreator {

    public static final List<String> SUPPORTED_MIME_TYPES = Collections.singletonList(
        MediaType.APPLICATION_PDF_VALUE
    );


    @Override
    public boolean supports(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType);
    }

    public BufferedImage getImg(InputStream pdf) {
        try {
            PDDocument document = PDDocument.load(pdf);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            return ImageResizeService.resizeImage(bufferedImage);
        } catch (IOException e) {
            throw new CantCreateThumbnailException(e);
        }
    }
}
