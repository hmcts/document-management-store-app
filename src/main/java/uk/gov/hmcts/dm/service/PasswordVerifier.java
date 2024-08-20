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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;



@Service
public class PasswordVerifier {

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

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) {

        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(multipartFile.getInputStream());
        } catch (IOException e) {
            return false;
        }

        String mimeType = getRealMimeType(multipartFile);

        PDFParser pdfparser = new PDFParser();
        OOXMLParser ooxmlparser = new OOXMLParser();
        TXTParser textParser = new TXTParser();
        TextAndCSVParser textAndCSVParser = new TextAndCSVParser();
        JpegParser jpegParser = new JpegParser();
        TiffParser tiffParser = new TiffParser();
        ImageParser imageParser = new ImageParser();
        MP4Parser mp4Parser = new MP4Parser();
        MidiParser midiParser = new MidiParser();
        OpenDocumentParser openDocumentParser = new OpenDocumentParser();
        RTFParser rtfParser = new RTFParser();

        return switch (mimeType) {
            case "application/pdf" -> checkPasswordWithParser(inputStream, pdfparser);
            case "application/vnd.ms-excel", "application/msword", "application/vnd.ms-powerpoint" ->
                checkPasswordWithParser(inputStream, ooxmlparser);
            case "text/csv" -> checkPasswordWithParser(inputStream, textAndCSVParser);
            case "text/plain" -> checkPasswordWithParser(inputStream, textParser);
            case "image/jpeg" -> checkPasswordWithParser(inputStream, jpegParser);
            case "image/tiff" -> checkPasswordWithParser(inputStream, tiffParser);
            case "image/png", "image/bmp" -> checkPasswordWithParser(inputStream, imageParser);
            case "audio/mp4", "video/mp4" -> checkPasswordWithParser(inputStream, mp4Parser);
            case "audio/mpeg" -> checkPasswordWithParser(inputStream, midiParser);
            //case ODF_FORMAT -> checkPasswordWithParser(inputStream, openDocumentParser);
            case "application/rtf" -> checkPasswordWithParser(inputStream, rtfParser);
            case "application/octect-stream" -> true; //Change to make correct call for format.
            case OPENXML_SHEET -> checkPasswordWithParser(inputStream, openDocumentParser);
            case OPENXML_SHEET_TEMPLATE -> checkPasswordWithParser(inputStream, openDocumentParser);
            case OPENXML_DOC -> checkPasswordWithParser(inputStream, openDocumentParser);
            case OPENXML_DOC_TEMPLATE -> checkPasswordWithParser(inputStream, openDocumentParser);
            case OPENXML_PRESENTATION -> checkPasswordWithParser(inputStream, openDocumentParser);
            case OPENXML_PRESENTATION_TEMPLATE -> checkPasswordWithParser(inputStream, openDocumentParser);
            case OPENXML_PRESENTATION_SLIDESHOW -> checkPasswordWithParser(inputStream, openDocumentParser);
            default -> false;
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

    public static String getRealMimeType(MultipartFile file) {
        AutoDetectParser parser = new AutoDetectParser();
        Detector detector = parser.getDetector();
        try {
            Metadata metadata = new Metadata();
            TikaInputStream stream = TikaInputStream.get(file.getInputStream());
            MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.toString();
        } catch (IOException e) {
            return MimeTypes.OCTET_STREAM;
        }
    }
}
