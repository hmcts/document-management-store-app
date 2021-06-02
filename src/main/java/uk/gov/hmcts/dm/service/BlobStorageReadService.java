package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
public class BlobStorageReadService {


    private final BlobContainerClient cloudBlobContainer;
    private static final int DEFAULT_BUFFER_SIZE = 20480; // ..bytes = 20KB.

    @Autowired
    public BlobStorageReadService(BlobContainerClient cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    /**
     * Streams the content of a document from blob storage. Processes a Range request when possible
     */
    public void loadBlob(DocumentContentVersion documentContentVersion,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (request.getHeader(HttpHeaders.RANGE.toLowerCase()) == null) {
            loadFullBlob(documentContentVersion, response.getOutputStream());
        } else {
            loadPartialBlob(documentContentVersion, request, response);
        }
    }

    public void loadFullBlob(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        log.debug("No Range header provided; returning entire document {}", documentContentVersion.getId());

        blockBlobClient(documentContentVersion.getId().toString()).download(outputStream);

        log.debug("Reading document version {} from Azure Blob Storage: OK", documentContentVersion.getId());
    }

    private void loadPartialBlob(DocumentContentVersion documentContentVersion,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {

        log.debug("Range header provided {}", documentContentVersion.getId());

        String rangeHeader = request.getHeader(HttpHeaders.RANGE.toLowerCase());
        log.debug("Range requested: {}", rangeHeader);

        Long length = documentContentVersion.getSize();

        String patternString = "^bytes=\\d*-\\d*";

        Pattern pattern = Pattern.compile(patternString);

        Matcher matcher = pattern.matcher(rangeHeader);

        if (!matcher.matches()) {
            throw new InvalidRangeRequestException(response, length);
        }

        response.setBufferSize(DEFAULT_BUFFER_SIZE);

        // Range headers can request a multipart range but this is not to be supported yet
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests
        // Take only first requested range and process it
        String part = rangeHeader.substring(6).split(",")[0].trim();

        BlobRange b = processPart(part, length, response);

        if (b.getOffset() == 0 && b.getCount() > length) {
            loadFullBlob(documentContentVersion, response.getOutputStream());
            return;
        }

        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

        log.debug("Processing blob range: {}", b.toString());
        blockBlobClient(documentContentVersion.getId().toString())
            .downloadWithResponse(
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
        long start = subLong(part, 0, part.indexOf('-'));
        long end = subLong(part, part.indexOf('-') + 1, part.length());

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

        long rangeByteCount = (end - start) + 1;
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + length);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeByteCount));

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
