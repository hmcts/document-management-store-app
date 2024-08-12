package uk.gov.hmcts.dm.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.parser.image.JpegParser;
import org.apache.tika.parser.image.TiffParser;
import org.apache.tika.parser.image.ImageParser;
import org.apache.tika.parser.csv.TextAndCSVParser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.parser.audio.MidiParser;
import org.apache.tika.parser.microsoft.rtf.RTFParser;

import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PasswordVerifier {

    private static final String ODF_FORMAT =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet," +
        "application/vnd.openxmlformats-officedocument.spreadsheetml.template," +
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document," +
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template," +
        "application/vnd.openxmlformats-officedocument.presentationml.presentation," +
        "application/vnd.openxmlformats-officedocument.presentationml.template," +
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow";


    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) throws IOException {

        InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream());
        String documentType = detectDocTypeUsingFacade(inputStream);

        PDFParser pdfparser = new PDFParser();
        OOXMLParser ooxmlparser = new OOXMLParser();
        TXTParser  textParser = new TXTParser();
        TextAndCSVParser textAndCSVParser = new TextAndCSVParser();
        JpegParser jpegParser = new JpegParser();
        TiffParser tiffParser = new TiffParser();
        ImageParser imageParser = new ImageParser();
        MP4Parser mp4Parser = new MP4Parser();
        MidiParser midiParser = new MidiParser();
        OpenDocumentParser openDocumentParser = new OpenDocumentParser();
        RTFParser rtfParser = new RTFParser();

        return switch (documentType) {
            case "application/pdf" -> checkPasswordWithParser(inputStream, pdfparser);
            case "application/vnd.ms-excel | application/msword | application/vnd.ms-powerpoint" ->
                    checkPasswordWithParser(inputStream, ooxmlparser);
            case "text/plain" -> checkPasswordWithParser(inputStream, textParser);
            case "text/csv" -> checkPasswordWithParser(inputStream, textAndCSVParser);
            case "image/jpeg," -> checkPasswordWithParser(inputStream, jpegParser);
            case "image/tiff" -> checkPasswordWithParser(inputStream, tiffParser);
            case "image/png | image/bmp" ->
                    checkPasswordWithParser(inputStream, imageParser); //NOT SURE IF THE imageParser is the right one
            case "audio/mp4 | video/mp4}" -> checkPasswordWithParser(inputStream, mp4Parser);
            case "audio/mpeg" -> checkPasswordWithParser(inputStream, midiParser);
            case ODF_FORMAT -> checkPasswordWithParser(inputStream, openDocumentParser);
            case "application/rtf" -> checkPasswordWithParser(inputStream, rtfParser);
            case "application/octect-stream" -> true; //Change to make correct call for format.
            default -> false;
        };
    }

    private boolean checkPasswordWithParser(InputStream inputStream, Parser parser) {
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

    private static String detectDocTypeUsingFacade(InputStream stream)
        throws IOException {

        Tika tika = new Tika();
        return tika.detect(stream);
    }
}
