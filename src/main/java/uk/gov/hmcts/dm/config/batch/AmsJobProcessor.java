package uk.gov.hmcts.dm.config.batch;

import com.microsoft.azure.management.mediaservices.v2018_07_01.*;
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.domain.AmsJob;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.JobStatus;
import uk.gov.hmcts.dm.domain.StreamingUrlComponent;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StreamingUrlComponentRepository;
import uk.gov.hmcts.dm.service.AzureMediaUploadService;

import java.util.Objects;
import java.util.Set;

@Component
public class AmsJobProcessor implements ItemProcessor<AmsJob, AmsJob> {

    private static final Logger log = LoggerFactory.getLogger(AmsJobProcessor.class);

    @Value("${azure.media-services.streamingEndPointName}")
    private String streamingEndpointName;

    @Value("${azure.media-services.encodingTransformName}")
    private String encodingTransformName;

    @Value("${azure.media-services.resourcegroup}")
    private String resourceGroup;

    @Value("${azure.media-services.account.name}")
    private String accountName;

    @Autowired
    private MediaManager manager;

    @Autowired
    private AzureMediaUploadService azureMediaUploadService;

    @Autowired
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    private StreamingUrlComponentRepository streamingUrlComponentRepository;

    @Override
    public AmsJob process(AmsJob amsJob) throws Exception {

        Job job = manager.jobs().getAsync(resourceGroup, accountName,
                            encodingTransformName, amsJob.getJobName())
                            .toBlocking().first();
        if (job.state() == JobState.FINISHED) {

            StreamingLocator streamingLocator = null;
            try {

                streamingLocator = azureMediaUploadService.createStreamingLocator(amsJob.getOutputAssetName(), amsJob.getLocatorName());

            } catch (ApiErrorException aee) {
                log.warn(String.format("StreamingLocator with name : %s already exists",amsJob.getLocatorName()));
                streamingLocator = azureMediaUploadService.getStreamingLocator(resourceGroup, accountName, amsJob.getLocatorName());
            }


            StreamingEndpoint streamingEndpoint = manager.streamingEndpoints()
                .getAsync(resourceGroup, accountName, streamingEndpointName)
                .toBlocking().first();

            if (Objects.nonNull(streamingEndpoint)) {
                // Start The Streaming Endpoint if it is not running.
                if (streamingEndpoint.resourceState() != StreamingEndpointResourceState.RUNNING) {
                    manager.streamingEndpoints().startAsync(resourceGroup, accountName, streamingEndpointName).await();

                }

                Set<StreamingUrlComponent> urls = azureMediaUploadService.getStreamingUrls(streamingLocator.name(),
                    streamingEndpoint);


                DocumentContentVersion documentContentVersion = amsJob.getDocumentContentVersion();
                urls.stream().forEach(streamingUrlComponent -> streamingUrlComponent.setDocumentContentVersion(documentContentVersion));
                streamingUrlComponentRepository.saveAll(urls);

                documentContentVersion.setStreamingUris(urls);
                documentContentVersionRepository.save(documentContentVersion);

                amsJob.setJobStatus(JobStatus.DONE);

            } else {
                log.info("Could not find streaming endpoint: " + streamingEndpoint);
            }

        } else if (job.state() == JobState.ERROR || job.state() == JobState.CANCELED) {
            log.error(String.format("Encoding Job : %s  could not be completed. Amsjob Id : %s " + amsJob.getJobName(), amsJob.getId()));
            amsJob.setJobStatus(JobStatus.FAILED);
        }

        return amsJob;
    }

}
