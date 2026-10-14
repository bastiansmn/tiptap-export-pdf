package fr.demo.pdf.pdfa3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.demo.pdf.PieceHeader;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PdfA3ServiceTest {

    @Autowired
    PdfA3Service service;

    @Test
    void embedsXmpMetadataAndXmlAttachment() throws Exception {
        PieceHeader h = new PieceHeader("PROCÈS VERBAL", "Audition", "AUDITION", "24/09/2026 à 14h53",
                "12345/2019/000123", "2405280000489201", "Lucas RENAUD & co", "Vol", "TANNEN Bifford",
                "CSP CONFLANS", "1 Square de Ramsgate");
        byte[] pdf = service.render(h, "<p>Contenu</p>");

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            // Info dictionary
            assertEquals("PROCÈS VERBAL - AUDITION", doc.getDocumentInformation().getTitle());
            assertTrue(doc.getDocumentInformation().getKeywords().contains("12345/2019/000123"));

            // XMP : identification PDF/A-3B + mots-clés
            String xmp = new String(doc.getDocumentCatalog().getMetadata().toByteArray(), StandardCharsets.UTF_8);
            assertTrue(xmp.contains("pdfaid:part>3<") || xmp.contains("pdfaid:part=\"3\""), xmp);
            assertTrue(xmp.contains("2405280000489201"));

            // Pièce jointe XML + relation AF
            var embedded = doc.getDocumentCatalog().getNames().getEmbeddedFiles().getNames();
            PDComplexFileSpecification spec = (PDComplexFileSpecification) embedded.get(PdfA3Service.ATTACHMENT_NAME);
            assertNotNull(spec);
            assertEquals("Source", spec.getCOSObject().getNameAsString("AFRelationship"));
            String xml = new String(spec.getEmbeddedFile().toByteArray(), StandardCharsets.UTF_8);
            assertTrue(xml.contains("<numProcedure>12345/2019/000123</numProcedure>"));
            assertTrue(xml.contains("Lucas RENAUD &amp; co"));
            assertNotNull(doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.getPDFName("AF")));

            // Polices toutes incorporées (obligatoire en PDF/A)
            PDResources res = doc.getPage(0).getResources();
            for (COSName n : res.getFontNames()) {
                assertTrue(res.getFont(n).isEmbedded(), "police non incorporée : " + n.getName());
            }
        }
    }
}
