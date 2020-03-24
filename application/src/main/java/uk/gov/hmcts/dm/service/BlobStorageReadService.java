package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.InvalidRangeRequestException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class BlobStorageReadService {


    private final BlobContainerClient cloudBlobContainer;
    private HttpServletRequest request;
    private static final int DEFAULT_BUFFER_SIZE = 20480; // ..bytes = 20KB.

    @Autowired
    public BlobStorageReadService(BlobContainerClient cloudBlobContainer, HttpServletRequest request) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.request = request;
    }

    public void loadBlob(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        log.debug("Reading document version {} from Azure Blob Storage...", documentContentVersion.getId());
        BlockBlobClient blobClient = blockBlobClient(documentContentVersion.getId().toString());
        blobClient.download(outputStream);
        log.debug("Reading document version {} from Azure Blob Storage: OK", documentContentVersion.getId());
    }

    /**
     * Streams the content of a document from blob storage. Processes a Range request when possible
     */
    public void loadBlob(DocumentContentVersion documentContentVersion, HttpServletResponse response) throws IOException {
        log.debug("Reading document version {} from Azure Blob Storage...", documentContentVersion.getId());
        BlockBlobClient blobClient = blockBlobClient(documentContentVersion.getId().toString());

        String rangeHeader = request.getHeader(HttpHeaders.RANGE);

        log.debug("Range requested: {}", rangeHeader);

        if (rangeHeader == null) {
            log.debug("No Range header provided; returning entire document");
            loadBlob(documentContentVersion, response.getOutputStream());
            return;
        }

        processRangeRequest(rangeHeader, documentContentVersion, blobClient, response);
    }

    private void processRangeRequest(String rangeHeader,
                                     DocumentContentVersion documentContentVersion,
                                     BlockBlobClient blobClient,
                                     HttpServletResponse response) throws IOException {

        Long length = documentContentVersion.getSize();

        if (!rangeHeader.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
            throw new InvalidRangeRequestException(response, length);
        }

        response.setBufferSize(DEFAULT_BUFFER_SIZE);

        // Range headers can request a multipart range but this is not to be supported yet
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests
        // Take only first requested range and process it
        String part = rangeHeader.substring(6).split(",")[0].trim();

        BlobRange b = processPart(part, length, response);
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

        log.debug("Processing blob range: {}", b.toString());
        BlobDownloadResponse r = blobClient.downloadWithResponse(
            response.getOutputStream(),
            b,
            new DownloadRetryOptions().setMaxRetryRequests(5),
            null,
            false,
            null,
            null
        );
    }

    private BlobRange processPart(String part, Long length, HttpServletResponse response) {
        long start = subLong(part, 0, part.indexOf("-"));
        long end = subLong(part, part.indexOf("-") + 1, part.length());

        if (start == -1) {
            start = length - end;
            end = length;
        } else if (end == -1 || end > length) {
            end = length;
        }

        // Check if Range is syntactically valid. If not, then return 416.
        if (start > end) {
            throw new InvalidRangeRequestException(response, length);
        }

        long rangeByteCount = (end - start) + 2;
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + length);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeByteCount - 1));

        return new BlobRange(start, rangeByteCount);
    }

    private BlockBlobClient blockBlobClient(String id) {
        return cloudBlobContainer.getBlobClient(id).getBlockBlobClient();
    }

    public boolean doesBinaryExist(UUID uuid) {
        return blockBlobClient(uuid.toString()).exists();
    }

    private long subLong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

}
