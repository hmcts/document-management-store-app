package uk.gov.hmcts.dm.service.thumbnail;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public class PdfThumbnailCreator extends AbstractFileSpecificThumbnailCreator {

    public PdfThumbnailCreator(BlobStorageReadService blobStorageReadService) {
        super(blobStorageReadService);
    }

    public BufferedImage getImg(InputStream pdf) {
        try {
            PDDocument document = PDDocument.load(pdf);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            return new BufferedImageResizer().resizeImage(bufferedImage);
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }
}
