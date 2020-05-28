package uk.gov.hmcts.dm.service;

import com.microsoft.azure.management.mediaservices.v2018_07_01.*;
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.*;
import uk.gov.hmcts.dm.repository.AmsJobRepository;

import java.net.URI;
import java.util.*;

@Service
public class AzureMediaUploadService {

    private static final Logger log = LoggerFactory.getLogger(AzureMediaUploadService.class);

    @Value("${azure.media-services.encodingTransformName}")
    String encodingTransformName;

    @Value("${azure.media-services.resourcegroup}")
    private String resourceGroup;

    @Value("${azure.media-services.account.name}")
    private String accountName;

    @Autowired
    private MediaManager manager;

    @Autowired
    private AmsJobRepository amsJobRepository;

    public AmsJob uploadMediaFile(final MultipartFile file,
                                final DocumentContentVersion documentContentVersion) {

        // Creating a unique suffix so that we don't have name collisions if you run the sample
        // multiple times without cleaning up.
        String uniqueness = UUID.randomUUID().toString();
        String locatorName = "locator-" + uniqueness;
        String outputAssetName = "output-" + uniqueness;
        String inputAssetName = "input-" + uniqueness;
        String jobName = "job-" + uniqueness.substring(0, 13);

        try {
            // Create a new input Asset and upload the specified local video file into it.
            createInputAsset(inputAssetName, file);

            // Output from the encoding Job must be written to an Asset, so let's create one. Note that we
            // are using a unique asset name, there should not be a name collision.
            log.info("Creating an output asset...");
            createOutputAsset(manager, resourceGroup, accountName, outputAssetName);

            AmsJob amsJob = AmsJob.builder().inputAssetName(inputAssetName)
                .outputAssetName(outputAssetName)
                .locatorName(locatorName)
                .jobName(jobName)
                .documentContentVersion(documentContentVersion)
                .jobStatus(JobStatus.NEW)
                .build();
            amsJobRepository.save(amsJob);

            return amsJob;

        } catch (Exception e) {
            log.error("Error Creating Input  Asset : " + e.getMessage());
        }

        return null;
    }

    /**
     * Create an asset.
     * @param manager       The entry point of Azure Media resource management.
     * @param resourceGroup The name of the resource group within the Azure subscription.
     * @param accountName   The Media Services account name.
     * @param assetName     The name of the asset to be created. It is known to be unique.
     * @return              The asset created.
     */
    public Asset createOutputAsset(MediaManager manager, String resourceGroup, String accountName,
                                   String assetName) {
        return manager.assets()
            .define(assetName)
            .withExistingMediaservice(resourceGroup, accountName)
            .create();
    }

    /**
     * Creates a new input Asset and uploads the specified local video file into it.
     *
     * @param assetName         The name of the asset where the media file to uploaded to.
     * @param file              The Media file to be uploaded into the asset.
     * @return                  The asset.
     */
    public Asset createInputAsset(String assetName, final MultipartFile file) throws Exception {
        Asset asset;
        try {
            // In this example, we are assuming that the asset name is unique.
            // If you already have an asset with the desired name, use the Assets.getAsync method
            // to get the existing asset.
            asset = manager.assets().getAsync(resourceGroup, accountName, assetName).toBlocking().first();
        } catch (NoSuchElementException nse) {
            asset = null;
        }

        if (asset == null) {
            log.info("Creating an input asset...");
            // Call Media Services API to create an Asset.
            // This method creates a container in storage for the Asset.
            // The files (blobs) associated with the asset will be stored in this container.
            asset = manager.assets().define(assetName).withExistingMediaservice(resourceGroup, accountName).create();
        } else {
            // The asset already exists and we are going to overwrite it. In your application, if you don't want to overwrite
            // an existing asset, use an unique name.
            log.warn("Warning: The asset named " + assetName + "already exists. It will be overwritten.");
        }

        // Use Media Services API to get back a response that contains
        // SAS URL for the Asset container into which to upload blobs.
        // That is where you would specify read-write permissions
        // and the expiration time for the SAS URL.
        ListContainerSasInput parameters = new ListContainerSasInput()
            .withPermissions(AssetContainerPermission.READ_WRITE).withExpiryTime(DateTime.now().plusHours(4));
        AssetContainerSas response = manager.assets()
            .listContainerSasAsync(resourceGroup, accountName, assetName, parameters).toBlocking().first();
        URI sasUri = new URI(response.assetContainerSasUrls().get(0));

        // Use Storage API to get a reference to the Asset container
        // that was created by calling Asset's create method.
        CloudBlobContainer container = new CloudBlobContainer(sasUri);

        CloudBlockBlob blob = container.getBlockBlobReference(file.getOriginalFilename());

        // Use Storage API to upload the file into the container in storage.
        log.info("Uploading a media file to the asset...");
        blob.upload(file.getInputStream(), file.getSize());

        return asset;

    }

    /**
     * Returns a StreamingLocator for the specified asset and with the specified streaming policy name.
     * Once the StreamingLocator is created the output asset is available to clients for playback.
     * @param resourceGroup The name of the resource group within the Azure subscription.
     * @param accountName   The Media Services account name.
     * @param locatorName         The StreamingLocator name (unique in this case).
     * @return                    The locator created.
     */
    public StreamingLocator getStreamingLocator(String resourceGroup, String accountName, String locatorName) {
        // Note that we are using one of the PredefinedStreamingPolicies which tell the Origin component
        // of Azure Media Services how to publish the content for streaming.
        StreamingLocator locator = manager
            .streamingLocators().getAsync(resourceGroup, accountName, locatorName)
            .toBlocking().first();

        return locator;
    }

    /**
     * Creates a StreamingLocator for the specified asset and with the specified streaming policy name.
     * Once the StreamingLocator is created the output asset is available to clients for playback.
     * @param outPutAssetName     The name of the output asset.
     * @param locatorName         The StreamingLocator name (unique in this case).
     * @return                    The locator created.
     */
    public StreamingLocator createStreamingLocator(String outPutAssetName, String locatorName) {
        // Note that we are using one of the PredefinedStreamingPolicies which tell the Origin component
        // of Azure Media Services how to publish the content for streaming.
        StreamingLocator locator = manager
            .streamingLocators().define(locatorName)
            .withExistingMediaservice(resourceGroup, accountName)
            .withAssetName(outPutAssetName)
            .withStreamingPolicyName("Predefined_ClearStreamingOnly")
            .create();

        return locator;
    }

    /**
     * Checks if the streaming endpoint is in the running state, if not, starts it.
     * @param locatorName   The name of the StreamingLocator that was created
     * @param streamingEndpoint     The streaming endpoint.
     * @return              List of streaming urls
     */
    public Set<StreamingUrlComponent> getStreamingUrls(String locatorName, StreamingEndpoint streamingEndpoint) {

        Set<StreamingUrlComponent> streamingUrls = new HashSet<>();

        ListPathsResponse paths = manager.streamingLocators().listPathsAsync(resourceGroup, accountName, locatorName)
            .toBlocking().first();

        for (StreamingPath path: paths.streamingPaths()) {
            StreamingUrlComponent streamingUrlComponent = null;
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append("https://")
                .append(streamingEndpoint.hostName())
                .append("/")
                .append(path.paths().get(0));

            if (path.streamingProtocol() == StreamingPolicyStreamingProtocol.HLS) {
                streamingUrlComponent = StreamingUrlComponent.builder()
                                                    .streamingProtocolType(StreamingProtocolType.HLS)
                                                    .streamingUrl(uriBuilder.toString())
                                                    .build();
            } else if (path.streamingProtocol() == StreamingPolicyStreamingProtocol.DASH) {
                streamingUrlComponent = StreamingUrlComponent.builder()
                    .streamingProtocolType(StreamingProtocolType.DASH)
                    .streamingUrl(uriBuilder.toString())
                    .build();
            } else if (path.streamingProtocol() == StreamingPolicyStreamingProtocol.SMOOTH_STREAMING) {
                streamingUrlComponent = StreamingUrlComponent.builder()
                    .streamingProtocolType(StreamingProtocolType.SMOOTH)
                    .streamingUrl(uriBuilder.toString())
                    .build();
            }
            streamingUrls.add(streamingUrlComponent);
        }
        return streamingUrls;
    }


    /**
     * Create and submit a job.
     * @param amsJob            The AmsJob.
     * @return                  The job created.
     */
    public void submitJob(AmsJob amsJob) {
        // Use the name of the created input asset to create the job input.
        JobInput jobInput = new JobInputAsset().withAssetName(amsJob.getInputAssetName());

        // Specify where the output(s) of the Job need to be written to
        List<JobOutput> jobOutputs = new ArrayList<>();
        jobOutputs.add(new JobOutputAsset().withAssetName(amsJob.getOutputAssetName()));

        Job job;
        try {
            log.info("Creating a job...");
            job = manager.jobs().define(amsJob.getJobName())
                .withExistingTransform(resourceGroup, accountName, encodingTransformName)
                .withInput(jobInput)
                .withOutputs(jobOutputs)
                .create();
        } catch (ApiErrorException exception) {
            log.error("ERROR: API call failed with error code " + exception.body().error().code()
                + " and message '" + exception.body().error().message() + "'");
            throw exception;
        }
    }

    /**
     * Checks for the status of the Job.
     *
     * @param amsJob        The AmsJob.
     * @return              True if encoding Job is completed else False.
     */
    public boolean isEncodingJobCompleted(final AmsJob amsJob) {

        //TODO - Do all below inside the Processor
        Job job = manager.jobs().getAsync(resourceGroup, accountName, encodingTransformName, amsJob.getJobName())
            .toBlocking().first();

        //TODO - How to handle failed Jobs?
        if (job.state() == JobState.FINISHED) {
            //TODO - Call the methods to create Streaming Locator, create Streaming URI's and Save the Streaming URI's against DocumentContentVersion
        } else if (job.state() == JobState.ERROR || job.state() == JobState.CANCELED) {
            amsJob.setJobStatus(JobStatus.FAILED);
            //TODO - Just log the error with failed AmsJob details
        }

        return false;

    }

    /**
     * Cleanup.
     * @param jobName       The job name.
     * @param amsJob        The AmsJob.
     */
    public void cleanup(String jobName, AmsJob amsJob) {

        manager.jobs().deleteAsync(resourceGroup, accountName, encodingTransformName, jobName).await();
        //TODO - Need to check if we need to delete below 3 things
//        manager.assets().deleteAsync(resourceGroupName, accountName, inputAssetName).await();
//        manager.assets().deleteAsync(resourceGroupName, accountName, outputAssetName).await();
//        manager.streamingLocators().deleteAsync(resourceGroupName, accountName, streamingLocatorName).await();

    }
}
