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
public class PasswordVerifier1 {

    public boolean checkPasswordProtectedFile(MultipartFile multipartFile) throws IOException {

         String ODF_formats =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet," +
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template," +
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document," +
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template," +
            "application/vnd.openxmlformats-officedocument.presentationml.presentation," +
            "application/vnd.openxmlformats-officedocument.presentationml.template," +
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow,";

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

        switch (documentType) {
            case "application/pdf":
                return checkPasswordWithParser(inputStream, pdfparser);
            case "application/vnd.ms-excel | application/msword | application/vnd.ms-powerpoint":
                return checkPasswordWithParser(inputStream, ooxmlparser);
            case "text/plain":
                return checkPasswordWithParser(inputStream, textParser);
            case "text/csv":
                return checkPasswordWithParser(inputStream, textAndCSVParser);
            case "image/jpeg,":
                return checkPasswordWithParser(inputStream, jpegParser);
            case "image/tiff":
                return checkPasswordWithParser(inputStream, tiffParser);
            case "image/png | image/bmp": //NOT SURE IF THE imageParser is the right one
                return checkPasswordWithParser(inputStream, imageParser);
            case "audio/mp4 | video/mp4}":
                return checkPasswordWithParser(inputStream, mp4Parser);
            case "audio/mpeg":
                return checkPasswordWithParser(inputStream, midiParser);
            case ODF_formats:
                return checkPasswordWithParser(inputStream, openDocumentParser);
            case "application/rtf":
                return checkPasswordWithParser(inputStream, rtfParser);
            case "application/octect-stream":
                return checkPasswordWithParser(inputStream, ?);
            default:
                return false;
        }
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
