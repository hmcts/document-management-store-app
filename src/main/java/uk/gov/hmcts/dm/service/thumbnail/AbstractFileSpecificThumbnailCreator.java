package uk.gov.hmcts.dm.service.thumbnail;

import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantCreateThumbnailException;
import uk.gov.hmcts.dm.service.BlobStorageReadService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class AbstractFileSpecificThumbnailCreator implements ThumbnailCreator {

    private static final Logger LOGGER = Logger.getLogger(AbstractFileSpecificThumbnailCreator.class.getName());

    private final BlobStorageReadService blobStorageReadService;

    public AbstractFileSpecificThumbnailCreator(BlobStorageReadService blobStorageReadService) {
        this.blobStorageReadService = blobStorageReadService;
    }

    @Override
    public InputStream getThumbnail(DocumentContentVersion documentContentVersion) {
        try {
            BufferedImage bufferedImage;
            if (isBlank(documentContentVersion.getContentUri())) {
                bufferedImage = getImgFromPostgres(documentContentVersion);
            } else {
                bufferedImage = getImgFromAzureBlobStore(documentContentVersion);
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, ThumbnailFormats.JPG.toString().toLowerCase(Locale.UK), os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            throw new CantCreateThumbnailException(e);
        }
    }

    abstract BufferedImage getImg(InputStream inputStream);

    private BufferedImage getImgFromPostgres(DocumentContentVersion documentContentVersion) throws SQLException {
        InputStream inputStream = documentContentVersion.getDocumentContent().getData().getBinaryStream();
        return getImg(inputStream);
    }

    private BufferedImage getImgFromAzureBlobStore(DocumentContentVersion documentContentVersion) throws IOException {
        // Pipe blob store output into thumbnail creator input
        try (
            final PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in)) {
            final CountDownLatch latch = new CountDownLatch(1);

            Thread loadThread = new Thread(() -> {
                try {
                    blobStorageReadService.loadBlob(documentContentVersion, out);
                    // Await end of image buffering to terminate thread.
                    latch.await();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Error while loading blob", e);
                    Thread.currentThread().interrupt();
                }
            });
            loadThread.start();

            final BufferedImage bufferedImage = getImg(in);

            // Notify thread of image buffering completion
            latch.countDown();

            return bufferedImage;
        }
    }

}
