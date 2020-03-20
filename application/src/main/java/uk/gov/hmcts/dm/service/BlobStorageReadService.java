package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobDownloadResponse;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

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

    public void loadBlob(DocumentContentVersion documentContentVersion, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Reading document version {} from Azure Blob Storage...", documentContentVersion.getId());
        BlockBlobClient blobClient = loadBlob(documentContentVersion.getId().toString());

        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ACCEPT_RANGES);

        response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
            format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

        response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());

        String rangeHeader = request.getHeader(HttpHeaders.RANGE);
        Long length = documentContentVersion.getSize();

        if (rangeHeader == null) {
            log.debug("No Range header provided; returning entire document");
            response.setHeader(HttpHeaders.CONTENT_LENGTH, length.toString());
            blobClient.download(response.getOutputStream());
            return;
        }

        log.debug("Range requested: {}", rangeHeader);

        response.setBufferSize(DEFAULT_BUFFER_SIZE);

        List<BlobRange> blobRanges = new ArrayList<>();

        if (!rangeHeader.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + length); // Required in 416.
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        Long lengthAccumulator = 0L;
        for (String part : rangeHeader.substring(6).split(",")) {
            // Assuming a file with length of 100, the following examples returns bytes at:
            // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
            long start = sublong(part, 0, part.indexOf("-"));
            long end = sublong(part, part.indexOf("-") + 1, part.length());

            if (start == -1) {
                start = length - end;
                end = length;
            } else if (end == -1 || end > length) {
                end = length;
            }

            log.debug("Start {}", start);
            log.debug("End {}", end);
            log.debug("Length {}", length);

            // Check if Range is syntactically valid. If not, then return 416.
            if (start > end) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + length); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // Add range.
            BlobRange b = new BlobRange(start, end - start + 1);
            blobRanges.add(b);

            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + length);
            lengthAccumulator += end + 1;
            // @todo add multipart support here
        }
        response.setHeader(HttpHeaders.CONTENT_LENGTH, lengthAccumulator.toString());

        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

        for (BlobRange b : blobRanges) {
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
    }

    private BlockBlobClient loadBlob(String id) {
        return cloudBlobContainer.getBlobClient(id).getBlockBlobClient();
    }

    public boolean doesBinaryExist(UUID uuid) {
        return loadBlob(uuid.toString()).exists();
    }

    private long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

}
