package uk.gov.hmcts.dm.service;

import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.audio.MidiParser;
import org.apache.tika.parser.csv.TextAndCSVParser;
import org.apache.tika.parser.image.ImageParser;
import org.apache.tika.parser.image.JpegParser;
import org.apache.tika.parser.image.TiffParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.microsoft.rtf.RTFParser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;



@Service
public class PasswordVerifier {

    private final Logger logger = LoggerFactory.getLogger(PasswordVerifier.class);

    private static final String OPENXML_SHEET =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String OPENXML_SHEET_TEMPLATE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
    private static final String OPENXML_DOC =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String OPENXML_DOC_TEMPLATE =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
    private static final String OPENXML_PRESENTATION =
        "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    private static final String OPENXML_PRESENTATION_TEMPLATE =
        "application/vnd.openxmlformats-officedocument.presentationml.template";
    private static final String OPENXML_PRESENTATION_SLIDESHOW =
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
    private static final String APPLICATION_MSWORD = "application/msword";
    private static final String APPLICATION_MSEXCEL = "application/vnd.ms-excel";
    private static final String APPLICATION_MSPOWERPOINT = "application/vnd.ms-powerpoint";
    private static final String APPLICATION_RTF = "application/rtf";
    private static final String APPLICATION_OCTECTSTREAM = "application/octect-stream";
    private static final String TEXT_CSV = "text/csv";
    private static final String IMAGE_TIFF = "image/tiff";
    private static final String IMAGE_BMP = "image/bmp";
    private static final String AUDIO_MP4 = "audio/mp4";
    private static final String AUDIO_MPEG = "audio/mpeg";
    private static final String VIDEO_MP4 = "video/mp4";
    private static final String DUMMY = "dummy";

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) {

        InputStream inputStream;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            return false;
        }

        String mimeType = getRealMimeType(multipartFile);

        return switch (mimeType) {
            case org.springframework.http.MediaType.APPLICATION_PDF_VALUE -> checkPasswordWithParser(inputStream, new PDFParser());
            case APPLICATION_MSEXCEL, APPLICATION_MSWORD, APPLICATION_MSPOWERPOINT ->
                checkPasswordWithParser(inputStream, new OOXMLParser());
            case TEXT_CSV -> checkPasswordWithParser(inputStream, new TextAndCSVParser());
            case org.springframework.http.MediaType.TEXT_PLAIN_VALUE -> checkPasswordWithParser(inputStream, new TXTParser());
            case org.springframework.http.MediaType.IMAGE_JPEG_VALUE -> checkPasswordWithParser(inputStream, new JpegParser());
            case IMAGE_TIFF -> checkPasswordWithParser(inputStream, new TiffParser());
            case org.springframework.http.MediaType.IMAGE_GIF_VALUE, IMAGE_BMP -> checkPasswordWithParser(inputStream, new ImageParser());
            case AUDIO_MP4, VIDEO_MP4 -> checkPasswordWithParser(inputStream, new MP4Parser());
            case AUDIO_MPEG -> checkPasswordWithParser(inputStream, new MidiParser());
            //case ODF_FORMAT -> checkPasswordWithParser(inputStream, openDocumentParser);
            case APPLICATION_RTF -> checkPasswordWithParser(inputStream, new RTFParser());
            case APPLICATION_OCTECTSTREAM -> true; //Change to make correct call for format.
            case OPENXML_SHEET -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case OPENXML_SHEET_TEMPLATE -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case OPENXML_DOC -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case OPENXML_DOC_TEMPLATE -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case OPENXML_PRESENTATION -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case OPENXML_PRESENTATION_TEMPLATE -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case OPENXML_PRESENTATION_SLIDESHOW -> checkPasswordWithParser(inputStream, new OpenDocumentParser());
            case DUMMY -> true;
            default -> true;
// TODO - Need to log default to identitfy the missing parser scenarios.
//  logger.info("Document with Name : {} could not be find a parser", multipartFile.getOriginalFilename());
        };
    }

    public boolean checkPasswordWithParser(InputStream inputStream, Parser parser) {
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext pcontext = new ParseContext();

            parser.parse(inputStream, handler, metadata, pcontext);

        } catch (TikaException | IOException | SAXException e) {
            return false;
        }
        return true;
    }

    public String getRealMimeType(MultipartFile file) {
        AutoDetectParser parser = new AutoDetectParser();
        Detector detector = parser.getDetector();
        try {
            Metadata metadata = new Metadata();
            TikaInputStream stream = TikaInputStream.get(file.getInputStream());
            MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.toString();
        } catch (Exception e) {
            logger.info("Document with Name : {} could not be detected for pwd verification.", file.getOriginalFilename());
            return DUMMY;
        }
    }
}
