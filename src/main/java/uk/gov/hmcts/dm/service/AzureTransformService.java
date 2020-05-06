package uk.gov.hmcts.dm.service;

import com.microsoft.azure.management.mediaservices.v2018_07_01.*;
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class AzureTransformService {

    private static final Logger log = LoggerFactory.getLogger(AzureTransformService.class);

    /**
     * If the specified transform exists, return that transform. If the it does not
     * exist, creates a new transform with the specified output. In this case, the
     * output is set to encode a video using a custom preset.
     *
     * @param manager       This is the entry point of Azure Media resource management.
     * @param resourceGroup The name of the resource group within the Azure subscription.
     * @param accountName   The Media Services account name.
     * @param transformName The name of the transform.
     * @return              The transform found or created.
     */
    public Transform createCustomTransform(MediaManager manager, String resourceGroup, String accountName,
                                           String transformName) {
        Transform transform;
        try {
            // Does a transform already exist with the desired name? Assume that an existing Transform with the desired name
            // also uses the same recipe or preset for processing content.
            transform = manager.transforms().getAsync(resourceGroup, accountName, transformName)
                .toBlocking().first();
        }
        catch (NoSuchElementException e) {
            transform = null;
        }

        if (transform == null) {
            log.info("Creating a custom transform...");
            // Create a new Transform Outputs List - this defines the set of outputs for the Transform
            List<TransformOutput> outputs = new ArrayList<>();

            // Create a new TransformOutput with a custom Standard Encoder Preset
            // This demonstrates how to create custom codec and layer output settings
            TransformOutput transformOutput = new TransformOutput();

            // Add it to output list.
            outputs.add(transformOutput);

            // Create a customer preset and add it to transform output
            StandardEncoderPreset preset = new StandardEncoderPreset();
            transformOutput.withPreset(preset)
                .withOnError(OnErrorType.STOP_PROCESSING_JOB)
                .withRelativePriority(Priority.NORMAL);

            // Create codecs for the preset and add it to the preset
            List<Codec> codecs = new ArrayList<>();
            preset.withCodecs(codecs);

            // Add an AAC Audio layer for the audio encoding
            codecs.add(new AacAudio()
                .withProfile(AacAudioProfile.AAC_LC)
                .withChannels(2)
                .withSamplingRate(48000)
                .withBitrate(128000));

            // Next, add a H264Video with two layers, HD and SD for the video encoding
            List<H264Layer> layers = new ArrayList<>();
            // Add H264Layers, one at HD and the other at SD. Assign a label that you can use for the output filename
            H264Layer hdLayer = new H264Layer();
            hdLayer.withBitrate(1000000)    // Units are in bits per second
                .withWidth("1280")
                .withHeight("720")
                .withLabel("HD");           // This label is used to modify the file name in the output formats
            H264Layer sdLayer = new H264Layer();
            sdLayer.withBitrate(600000)
                .withWidth("640")
                .withHeight("360")
                .withLabel("SD");
            layers.add(hdLayer);
            layers.add(sdLayer);

            codecs.add(new H264Video()      // Add a H264Video to codecs
                .withLayers(layers)         // Add the 2 layers
                .withKeyFrameInterval(Period.seconds(2))    //Set the GOP interval to 2 seconds for both H264Layers
            );

            // Also generate a set of PNG thumbnails
            List<PngLayer> pngLayers = new ArrayList<>();
            PngLayer pngLayer = new PngLayer();
            pngLayer.withWidth("50%");
            pngLayer.withHeight("50%");
            pngLayers.add(pngLayer);
            codecs.add(new PngImage()
                .withLayers(pngLayers)
                .withStart("25%")
                .withStep("25%")
                .withRange("80%"));

            // Specify the format for the output files - one for video+audio, and another for the thumbnails
            List<Format> formats = new ArrayList<>();
            // Mux the H.264 video and AAC audio into MP4 files, using basename, label, bitrate and extension macros
            // Note that since you have multiple H264Layers defined above, you have to use a macro that produces unique names per H264Layer
            // Either {Label} or {Bitrate} should suffice
            formats.add(new Mp4Format().withFilenamePattern("Video-{Basename}-{Label}-{Bitrate}{Extension}"));
            formats.add(new PngFormat().withFilenamePattern("Thumbnail-{Basename}-{Index}{Extension}"));
            preset.withFormats(formats);

            // Create the custom Transform with the outputs defined above
            transform = manager.transforms().define(transformName)
                .withExistingMediaservice(resourceGroup, accountName)
                .withOutputs(outputs)
                .withDescription("A simple custom encoding transform with 2 MP4 bitrates")
                .create();
        }

        return transform;
    }

}
