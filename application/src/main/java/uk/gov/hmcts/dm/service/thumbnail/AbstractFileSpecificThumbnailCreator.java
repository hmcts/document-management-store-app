package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class AbstractFileSpecificThumbnailCreator implements ThumbnailCreator {

    private final BlobStorageReadService blobStorageReadService;

    public AbstractFileSpecificThumbnailCreator(BlobStorageReadService blobStorageReadService) {
        this.blobStorageReadService = blobStorageReadService;
    }

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        try {
            InputStream inputStream;
            if (isBlank(documentContentVersion.getContentUri())) {
                inputStream = documentContentVersion.getDocumentContent().getData().getBinaryStream();
            } else {
                // Pipe blob store output into thumbnail creator input
                final PipedInputStream in = new PipedInputStream();
                final PipedOutputStream out = new PipedOutputStream(in);

                Thread loadThread = new Thread(() -> blobStorageReadService.loadBlob(documentContentVersion, out));
                loadThread.start();

                inputStream = in;
            }

            BufferedImage bufferedImage = getImg(inputStream);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, ThumbnailFormats.JPG.toString().toLowerCase(Locale.UK), os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }

    abstract BufferedImage getImg(InputStream inputStream);

}
