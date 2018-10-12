package uk.gov.hmcts.dm.functional.utilities;

import org.springframework.http.MediaType;

import java.io.Serializable;

public class V1MimeTypes extends MediaType implements Serializable {


    public static final MediaType APPLICATION_DOCX;
    public static final String APPLICATION_DOCX_VALUE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public static final MediaType APPLICATION_RTF;
    public static final String APPLICATION_RTF_VALUE = "application/rtf";

    public static final MediaType IMAGE_TIF;
    public static final String IMAGE_TIF_VALUE = "image/tiff";

    public static final MediaType IMAGE_BMP;
    public static final String IMAGE_BMP_VALUE = "image/bmp";

    public static final MediaType IMAGE_SVG;
    public static final String IMAGE_SVG_VALUE = "image/svg+xml";

    public static final MediaType APPLICATION_PPT;
    public static final String APPLICATION_PPT_VALUE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    public static final MediaType APPLICATION_XLS;
    public static final String APPLICATION_XLS_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final MediaType APPLICATION_ODT;
    public static final String APPLICATION_ODT_VALUE = "application/vnd.oasis.opendocument.text";

    public static final MediaType APPLICATION_ODS;
    public static final String APPLICATION_ODS_VALUE = "application/vnd.oasis.opendocument.spreadsheet";

    public static final MediaType APPLICATION_ODP;
    public static final String APPLICATION_ODP_VALUE = "application/vnd.oasis.opendocument.presentation";

    public static final MediaType AUDIO_WAV;
    public static final String AUDIO_WAV_VALUE = "audio/vnd.wave";

    public static final MediaType AUDIO_MIDI;
    public static final String AUDIO_MIDI_VALUE = "audio/midi";

    public static final MediaType AUDIO_MPEG;
    public static final String AUDIO_MPEG_VALUE = "audio/mpeg";

    public static final MediaType AUDIO_WEBM;
    public static final String AUDIO_WEBM_VALUE = "audio/webm";

    public static final MediaType AUDIO_OGG;
    public static final String AUDIO_OGG_VALUE = "audio/ogg";

    public static final MediaType AUDIO_3GPP;
    public static final String AUDIO_3GPP_VALUE = "audio/3gpp";

    public static final MediaType AUDIO_3GPP2;
    public static final String AUDIO_3GPP2_VALUE = "audio/3gpp2";

    public static final MediaType AUDIO_XWAV;
    public static final String AUDIO_XWAV_VALUE = "audio/x-wav";

    public static final MediaType VIDEO_OGG;
    public static final String VIDEO_OGG_VALUE = "video/ogg";

    public static final MediaType VIDEO_3GPP;
    public static final String VIDEO_3GPP_VALUE = "video/3gpp";

    public static final MediaType VIDEO_3GPP2;
    public static final String VIDEO_3GPP2_VALUE = "video/3gpp2";

    public static final MediaType VIDEO_WEBM;
    public static final String VIDEO_WEBM_VALUE = "video/webm";

    public static final MediaType VIDEO_MPEG;
    public static final String VIDEO_MPEG_VALUE = "video/mp4";

    static {
        //    whitelist: ${DM_MULTIPART_WHITELIST:text/plain,text/csv,image/gif,image/tiff,image/jpeg,image/png,image/webp,image/svg+xml,application/pdf,application/rtf,application/msword,application/vnd.ms-powerpoint,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.oasis.opendocument.text,application/vnd.oasis.opendocument.spreadsheet,application/vnd.oasis.opendocument.presentation,application/xml,audio/wav,audio/midi,audio/mpeg,audio/webm,audio/ogg,video/ogg,audio/3gpp,video/3gpp,audio/3gpp2,video/3gpp2,audio/x-wav,audio/wav,video/webm,video/mpeg}
        APPLICATION_DOCX = valueOf(APPLICATION_DOCX_VALUE);
        APPLICATION_RTF = valueOf(APPLICATION_RTF_VALUE);
        IMAGE_TIF = valueOf(IMAGE_TIF_VALUE);
        IMAGE_SVG = valueOf(IMAGE_SVG_VALUE);
        IMAGE_BMP = valueOf(IMAGE_BMP_VALUE);
        APPLICATION_PPT = valueOf(APPLICATION_PPT_VALUE);
        APPLICATION_XLS = valueOf(APPLICATION_XLS_VALUE);
        APPLICATION_ODT = valueOf(APPLICATION_ODT_VALUE);
        APPLICATION_ODS = valueOf(APPLICATION_ODS_VALUE);
        APPLICATION_ODP = valueOf(APPLICATION_ODP_VALUE);
        AUDIO_WAV = valueOf(AUDIO_WAV_VALUE);
        AUDIO_MIDI = valueOf(AUDIO_MIDI_VALUE);
        AUDIO_MPEG = valueOf(AUDIO_MPEG_VALUE);
        AUDIO_WEBM = valueOf(AUDIO_WEBM_VALUE);
        AUDIO_OGG = valueOf(AUDIO_OGG_VALUE);
        AUDIO_3GPP = valueOf(AUDIO_3GPP_VALUE);
        AUDIO_3GPP2 = valueOf(AUDIO_3GPP2_VALUE);
        AUDIO_XWAV = valueOf(AUDIO_XWAV_VALUE);
        VIDEO_OGG = valueOf(VIDEO_OGG_VALUE);
        VIDEO_3GPP = valueOf(VIDEO_3GPP_VALUE);
        VIDEO_3GPP2 = valueOf(VIDEO_3GPP2_VALUE);
        VIDEO_WEBM = valueOf(VIDEO_WEBM_VALUE);
        VIDEO_MPEG = valueOf(VIDEO_MPEG_VALUE);
    }

    public V1MimeTypes(String type) {
        super(type);
    }
}
