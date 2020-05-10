package uk.gov.hmcts.dm.service;

import com.microsoft.azure.management.mediaservices.v2018_07_01.*;
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AzureMediaAssetService {

    private static final Logger log = LoggerFactory.getLogger(AzureMediaAssetService.class);


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
     * Creates a StreamingLocator for the specified asset and with the specified streaming policy name.
     * Once the StreamingLocator is created the output asset is available to clients for playback.
     * @param manager       The entry point of Azure Media resource management.
     * @param resourceGroup The name of the resource group within the Azure subscription.
     * @param accountName   The Media Services account name.
     * @param assetName     The name of the output asset.
     * @param locatorName   The StreamingLocator name (unique in this case).
     * @return              The locator created.
     */
    public StreamingLocator getStreamingLocator(MediaManager manager, String resourceGroup, String accountName,
                                                        String assetName, String locatorName) {
        // Note that we are using one of the PredefinedStreamingPolicies which tell the Origin component
        // of Azure Media Services how to publish the content for streaming.
        StreamingLocator locator = manager
            .streamingLocators().define(locatorName)
            .withExistingMediaservice(resourceGroup, accountName)
            .withAssetName(assetName)
            .withStreamingPolicyName("Predefined_ClearStreamingOnly")
            .create();

        return locator;
    }

    /**
     * Checks if the streaming endpoint is in the running state, if not, starts it.
     * @param manager       The entry point of Azure Media resource management.
     * @param resourceGroup The name of the resource group within the Azure subscription.
     * @param accountName   The Media Services account name.
     * @param locatorName   The name of the StreamingLocator that was created.
     * @param streamingEndpoint     The streaming endpoint.
     * @return              List of streaming urls.
     */
    public List<String> getHlsAndDashStreamingUrls(MediaManager manager, String resourceGroup, String accountName,
                                                           String locatorName, StreamingEndpoint streamingEndpoint) {
        List<String> streamingUrls = new ArrayList<>();
        ListPathsResponse paths = manager.streamingLocators().listPathsAsync(resourceGroup, accountName, locatorName)
            .toBlocking().first();

        for (StreamingPath path: paths.streamingPaths()) {
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append("https://")
                .append(streamingEndpoint.hostName())
                .append("/")
                .append(path.paths().get(0));

            if (path.streamingProtocol() == StreamingPolicyStreamingProtocol.HLS) {
                streamingUrls.add("HLS url: " + uriBuilder.toString());
            } else if (path.streamingProtocol() == StreamingPolicyStreamingProtocol.DASH) {
                streamingUrls.add("DASH url: " + uriBuilder.toString());
            }
        }
        return streamingUrls;
    }

    /**
     * Creates a new input Asset and uploads the specified local video file into it.
     *
     * @param manager           This is the entry point of Azure Media resource management.
     * @param resourceGroupName The name of the resource group within the Azure subscription.
     * @param accountName       The Media Services account name.
     * @param assetName         The name of the asset where the media file to uploaded to.
     * @param fileToUpload      The Media file to be uploaded into the asset.
     * @return                  The asset.
     */
    public Asset createInputAsset(MediaManager manager, String resourceGroupName, String accountName,
                                          String assetName, final File fileToUpload) throws Exception {
        Asset asset;
        try {
            // In this example, we are assuming that the asset name is unique.
            // If you already have an asset with the desired name, use the Assets.getAsync method
            // to get the existing asset.
            asset = manager.assets().getAsync(resourceGroupName, accountName, assetName).toBlocking().first();
        } catch (NoSuchElementException nse) {
            asset = null;
        }

        if (asset == null) {
            log.info("Creating an input asset...");
            // Call Media Services API to create an Asset.
            // This method creates a container in storage for the Asset.
            // The files (blobs) associated with the asset will be stored in this container.
            asset = manager.assets().define(assetName).withExistingMediaservice(resourceGroupName, accountName).create();
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
            .listContainerSasAsync(resourceGroupName, accountName, assetName, parameters).toBlocking().first();
        URI sasUri = new URI(response.assetContainerSasUrls().get(0));

        // Use Storage API to get a reference to the Asset container
        // that was created by calling Asset's create method.
        CloudBlobContainer container = new CloudBlobContainer(sasUri);

        CloudBlockBlob blob = container.getBlockBlobReference(fileToUpload.getName());

        // Use Storage API to upload the file into the container in storage.
        log.info("Uploading a media file to the asset...");
        blob.uploadFromFile(fileToUpload.getPath());

        return asset;

    }

}
