package uk.gov.hmcts.dm.service;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PDFPasswordCheckServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/files/not_pw_protected.pdf",
                            "src/test/resources/files/pw_protected.pdf",
                            "src/test/resources/files/non_existent.pdf",
                            "invalid/path/to/pdf.pdf"})

    void shouldCheckFileForPasswordProtection(String input) {
        PdfPasswordVerifier.checkPasswordProtectedPDF(input);
    }
    @Test
    public void shouldErrorWhenNoFilePathProvided() {
        String path = null;
        PdfPasswordVerifier.checkPasswordProtectedPDF(path);
    }
}
