package fr.demo.pdf.pdfa3;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import fr.demo.pdf.PdfService;
import fr.demo.pdf.PieceHeader;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpSerializer;
import org.springframework.stereotype.Service;

/**
 * EXEMPLE ISOLÉ (non prioritaire) : génère le même PDF que {@link PdfService} mais au format PDF/A-3B,
 * avec les données d'en-tête embarquées de deux façons :
 * <ul>
 *   <li>métadonnées XMP (titre, auteur, mots-clés : n° de procédure, IDJ...) ;</li>
 *   <li>pièce jointe XML {@code procedure-metadata.xml} (relation AFRelationship = Source).</li>
 * </ul>
 */
@Service
public class PdfA3Service {

    static final String ATTACHMENT_NAME = "procedure-metadata.xml";

    private final PdfService pdfService;

    public PdfA3Service(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    public byte[] render(PieceHeader header, String contentHtml) {
        byte[] pdf = renderPdfA3(pdfService.buildXhtml(header, contentHtml));
        try (PDDocument doc = Loader.loadPDF(pdf); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeInfoAndXmp(doc, header);
            attachXml(doc, ATTACHMENT_NAME, headerToXml(header));
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Échec de l'enrichissement PDF/A-3", e);
        }
    }

    /** Étape 1 : rendu en PDF/A-3B (polices incorporées + profil ICC sRGB obligatoires). */
    private byte[] renderPdfA3(String xhtml) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.usePdfAConformance(PdfAConformance.PDFA_3_B);
            builder.useColorProfile(ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData());
            // Les familles du CSS (Helvetica/Arial/Georgia) sont mappées sur des polices libres incorporées.
            for (String family : List.of("Helvetica", "Arial")) {
                font(builder, "DejaVuSans.ttf", family, 400, FontStyle.NORMAL);
                font(builder, "DejaVuSans-Bold.ttf", family, 700, FontStyle.NORMAL);
                font(builder, "DejaVuSans-Oblique.ttf", family, 400, FontStyle.ITALIC);
            }
            font(builder, "DejaVuSerif-Italic.ttf", "Georgia", 400, FontStyle.ITALIC);
            font(builder, "DejaVuSerif-Italic.ttf", "Times New Roman", 400, FontStyle.ITALIC);
            builder.withHtmlContent(xhtml, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new IllegalStateException("Échec du rendu PDF/A-3", e);
        }
    }

    private void font(PdfRendererBuilder builder, String file, String family, int weight, FontStyle style) {
        builder.useFont(() -> getClass().getResourceAsStream("/pdfa3/fonts/" + file), family, weight, style, true);
    }

    /** Étape 2 : Info dictionary + XMP (doivent rester cohérents entre eux en PDF/A). */
    private void writeInfoAndXmp(PDDocument doc, PieceHeader h) throws Exception {
        String title = h.typePiece() + " - " + h.titre();
        String author = h.serviceNom();
        String keywords = "procédure " + h.numProcedure() + ", IDJ " + h.numIdj();
        Calendar now = new GregorianCalendar();

        PDDocumentInformation info = doc.getDocumentInformation();
        info.setTitle(title);
        info.setAuthor(author);
        info.setSubject(h.sousTypePiece());
        info.setKeywords(keywords);
        info.setCreationDate(now);
        info.setModificationDate(now);

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        XMPMetadata xmp;
        if (catalog.getMetadata() != null) {
            try (InputStream in = catalog.getMetadata().exportXMPMetadata()) {
                xmp = new DomXmpParser().parse(in);
            }
        } else {
            xmp = XMPMetadata.createXMPMetadata();
        }
        if (xmp.getPDFAIdentificationSchema() == null) {
            PDFAIdentificationSchema id = xmp.createAndAddPDFAIdentificationSchema();
            id.setPart(3);
            id.setConformance("B");
        }
        DublinCoreSchema dc = xmp.getDublinCoreSchema() != null ? xmp.getDublinCoreSchema() : xmp.createAndAddDublinCoreSchema();
        dc.setTitle(title);
        dc.addCreator(author);
        dc.setDescription(h.sousTypePiece());
        dc.addSubject("procédure " + h.numProcedure());
        dc.addSubject("IDJ " + h.numIdj());
        AdobePDFSchema pdf = xmp.getAdobePDFSchema() != null ? xmp.getAdobePDFSchema() : xmp.createAndAddAdobePDFSchema();
        pdf.setKeywords(keywords);
        pdf.setProducer("tipatp-export-pdf (OpenHTMLToPDF + PDFBox)");
        XMPBasicSchema basic = xmp.getXMPBasicSchema() != null ? xmp.getXMPBasicSchema() : xmp.createAndAddXMPBasicSchema();
        basic.setCreatorTool("tipatp-export-pdf");
        basic.setCreateDate(now);
        basic.setModifyDate(now);

        ByteArrayOutputStream xmpOut = new ByteArrayOutputStream();
        new XmpSerializer().serialize(xmp, xmpOut, true);
        PDMetadata metadata = new PDMetadata(doc);
        metadata.importXMPMetadata(xmpOut.toByteArray());
        catalog.setMetadata(metadata);
    }

    /** Étape 3 : pièce jointe XML, déclarée dans EmbeddedFiles ET dans /AF du catalogue (exigé par PDF/A-3). */
    private void attachXml(PDDocument doc, String name, String xml) throws IOException {
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        PDEmbeddedFile file = new PDEmbeddedFile(doc, new ByteArrayInputStream(bytes));
        file.setSubtype("application/xml");
        file.setSize(bytes.length);
        file.setCreationDate(new GregorianCalendar());
        file.setModDate(new GregorianCalendar());

        PDComplexFileSpecification spec = new PDComplexFileSpecification();
        spec.setFile(name);
        spec.setFileUnicode(name);
        spec.setFileDescription("Données d'en-tête de la pièce de procédure");
        spec.setEmbeddedFile(file);
        spec.setEmbeddedFileUnicode(file);
        spec.getCOSObject().setName(COSName.getPDFName("AFRelationship"), "Source");

        PDEmbeddedFilesNameTreeNode tree = new PDEmbeddedFilesNameTreeNode();
        tree.setNames(Map.of(name, spec));
        PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
        names.setEmbeddedFiles(tree);
        doc.getDocumentCatalog().setNames(names);

        COSArray af = new COSArray();
        af.add(spec.getCOSObject());
        doc.getDocumentCatalog().getCOSObject().setItem(COSName.getPDFName("AF"), af);
    }

    private String headerToXml(PieceHeader h) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <pieceProcedure>
                  <typePiece>%s</typePiece>
                  <sousTypePiece>%s</sousTypePiece>
                  <titre>%s</titre>
                  <dateHorodatage>%s</dateHorodatage>
                  <numProcedure>%s</numProcedure>
                  <numIdj>%s</numIdj>
                  <affaireContre>%s</affaireContre>
                  <qualification>%s</qualification>
                  <personne>%s</personne>
                  <service nom="%s" adresse="%s"/>
                </pieceProcedure>
                """.formatted(x(h.typePiece()), x(h.sousTypePiece()), x(h.titre()), x(h.dateHorodatage()),
                x(h.numProcedure()), x(h.numIdj()), x(h.affaireContre()), x(h.qualification()), x(h.personne()),
                x(h.serviceNom()), x(h.serviceAdresse()));
    }

    private static String x(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
