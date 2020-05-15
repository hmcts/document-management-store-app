package uk.gov.hmcts.dm.service;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.azure.management.mediaservices.v2018_07_01.*;
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class AzureMediaUploadService {

    private static final Logger log = LoggerFactory.getLogger(AzureMediaUploadService.class);

    //TODO -  Please change this to your endpoint name
    private static final String STREAMING_ENDPOINT_NAME = "jason-streaming-endpoint";

    @Value("${azure.media-services.resourcegroup}")
    private String resourceGroup;

    @Value("${azure.media-services.account.name}")
    private String accountName;


    private MediaManager manager;

    private AzureTransformService azureTransformService;

    private AzureMediaAssetService azureMediaAssetService;

    private AzureMediaJobService azureMediaJobService;


    public AzureMediaUploadService(MediaManager manager, AzureTransformService azureTransformService,
                                   AzureMediaAssetService azureMediaAssetService, AzureMediaJobService azureMediaJobService) {
        this.manager = manager;
        this.azureTransformService = azureTransformService;
        this.azureMediaAssetService = azureMediaAssetService;
        this.azureMediaJobService = azureMediaJobService;
    }

    public void uploadMediaFile(final String tranformationName, final File fileToUpload) {

        // Creating a unique suffix so that we don't have name collisions if you run the sample
        // multiple times without cleaning up.
        UUID uuid = UUID.randomUUID();
        String uniqueness = uuid.toString();
        String jobName = "job-" + uniqueness.substring(0, 13);
        String locatorName = "locator-" + uniqueness;
        String outputAssetName = "output-" + uniqueness;
        String inputAssetName = "input-" + uniqueness;
        boolean stopEndpoint = false;

        try {
            // Ensure that you have customized encoding Transform with builtin preset. This is really a one time
            // setup operation.
            Transform transform = azureTransformService.ensureTransformExists(manager, resourceGroup,
                accountName, tranformationName);

            // Create a new input Asset and upload the specified local video file into it.
            Asset inputAsset = azureMediaAssetService.createInputAsset(manager, resourceGroup, accountName, inputAssetName,
                fileToUpload);

            // Output from the encoding Job must be written to an Asset, so let's create one. Note that we
            // are using a unique asset name, there should not be a name collision.
            log.info("Creating an output asset...");
            Asset outputAsset = azureMediaAssetService.createOutputAsset(manager, resourceGroup, accountName,
                outputAssetName);

            Job job = azureMediaJobService.submitJob(manager, resourceGroup, accountName, transform.name(), jobName,
                inputAsset.name(), outputAsset.name());

            long startedTime = System.currentTimeMillis();

            // In this demo code, we will poll for Job status. Polling is not a recommended best practice for production
            // applications because of the latency it introduces. Overuse of this API may trigger throttling. Developers
            // should instead use Event Grid. To see how to implement the event grid, see the sample
            // https://github.com/Azure-Samples/media-services-v3-java/tree/master/ContentProtection/BasicAESClearKey.
            job = azureMediaJobService.waitForJobToFinish(manager, resourceGroup, accountName, transform.name(),
                jobName);

            long elapsed = (System.currentTimeMillis() - startedTime) / 1000; // Elapsed time in seconds
            log.info("Job elapsed time: " + elapsed + " second(s).");

            if (job.state() == JobState.FINISHED) {
                log.info("Job finished.");

                // Now that the content has been encoded, publish it for Streaming by creating
                // a StreamingLocator.
                StreamingLocator locator = azureMediaAssetService.getStreamingLocator(manager, resourceGroup, accountName,
                    outputAsset.name(), locatorName);

                StreamingEndpoint streamingEndpoint = manager.streamingEndpoints()
                    .getAsync(resourceGroup, accountName, STREAMING_ENDPOINT_NAME)
                    .toBlocking().first();

                if (streamingEndpoint != null) {
                    // Start The Streaming Endpoint if it is not running.
                    if (streamingEndpoint.resourceState() != StreamingEndpointResourceState.RUNNING) {
                        manager.streamingEndpoints().startAsync(resourceGroup, accountName, STREAMING_ENDPOINT_NAME).await();

                        // We started the endpoint, we should stop it in cleanup.
                        stopEndpoint = true;
                    }

                    List<String> urls = azureMediaAssetService.getHlsAndDashStreamingUrls(manager, resourceGroup, accountName, locator.name(), streamingEndpoint);

                    for (String url: urls) {
                        log.info("Streaming URL : " + url);
                    }

                    log.info("Copy and paste the Streaming URL into the Azure Media Player at 'http://aka.ms/azuremediaplayer'.");
                } else {
                    log.info("Could not find streaming endpoint: " + STREAMING_ENDPOINT_NAME);
                }

            } else if (job.state() == JobState.ERROR) {
                log.error("ERROR: Job finished with error message: " + job.outputs().get(0).error().message());
                log.error("ERROR:                   error details: "
                    + job.outputs().get(0).error().details().get(0).message());
            }
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof AuthenticationException) {
                    log.error("ERROR: Authentication error, please check your account settings in appsettings.json.");
                    break;
                } else if (cause instanceof ApiErrorException) {
                    ApiErrorException apiException = (ApiErrorException) cause;
                    log.error("ERROR: " + apiException.body().error().message());
                    break;
                }
                cause = cause.getCause();
            }
            log.error(e.getMessage());

        } finally {
            log.info("Cleaning up...");
            azureMediaJobService.cleanup(manager, resourceGroup, accountName, tranformationName, jobName, inputAssetName,
                outputAssetName, locatorName, stopEndpoint, STREAMING_ENDPOINT_NAME);
            log.info("Done.");
        }
    }

}
