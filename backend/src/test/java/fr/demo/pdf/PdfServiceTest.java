package fr.demo.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PdfServiceTest {

    @Autowired
    PdfService service;

    static PieceHeader header() {
        return new PieceHeader("PROCÈS VERBAL", "Audition", "AUDITION GRANDE IDENTITÉ DE TANNEN Bifford",
                "24/09/2026 à 14h53", "12345/2019/000123", "2405280000489201",
                "Lucas RENAUD ; Personne 2", "Vol Lorem ipsum", "TANNEN Bifford",
                "CSP CONFLANS SAINTE HONORINE", "1 Square de Ramsgate 78700");
    }

    @Test
    void generatesMultiPagePdfWithHeaderAndFooter() throws Exception {
        String longContent = "<p>Paragraphe de test à répéter.</p>".repeat(150);
        byte[] pdf = service.render(header(), longContent);

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertTrue(doc.getNumberOfPages() > 1);
            String text = new PDFTextStripper().getText(doc);
            assertTrue(text.contains("République Française"));
            assertTrue(text.contains("12345/2019/000123"));
        }
    }
}
