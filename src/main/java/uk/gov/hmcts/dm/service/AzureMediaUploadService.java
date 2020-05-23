package uk.gov.hmcts.dm.service;

import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.AmsJob;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.TaskState;
import uk.gov.hmcts.dm.repository.AmsJobRepository;

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

    @Autowired
    private MediaManager manager;

    @Autowired
    private AzureMediaAssetService azureMediaAssetService;

    @Autowired
    private AmsJobRepository amsJobRepository;

    public void uploadMediaFile(final String tranformationName, final MultipartFile file,
                                final DocumentContentVersion documentContentVersion) {

        // Creating a unique suffix so that we don't have name collisions if you run the sample
        // multiple times without cleaning up.
        String uniqueness = UUID.randomUUID().toString();
        String locatorName = "locator-" + uniqueness;
        String outputAssetName = "output-" + uniqueness;
        String inputAssetName = "input-" + uniqueness;

        try {
            // Create a new input Asset and upload the specified local video file into it.
            azureMediaAssetService.createInputAsset(inputAssetName, file);

            // Output from the encoding Job must be written to an Asset, so let's create one. Note that we
            // are using a unique asset name, there should not be a name collision.
            log.info("Creating an output asset...");
            azureMediaAssetService.createOutputAsset(manager, resourceGroup, accountName, outputAssetName);

            amsJobRepository.save(AmsJob.builder().inputAssetName(inputAssetName)
                                                .outputAssetName(outputAssetName)
                                                .locatorName(locatorName)
                                                .documentContentVersion(documentContentVersion)
                                                .taskState(TaskState.NEW)
                                                .build());

        } catch (Exception e) {
            log.error("Error Creating Input  Asset : " + e.getMessage());
        }
    }

}
