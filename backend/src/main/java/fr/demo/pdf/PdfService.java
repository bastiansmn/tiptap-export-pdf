package fr.demo.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class PdfService {

    private static final Safelist CONTENT_SAFELIST = Safelist.relaxed()
            .addTags("hr", "colgroup", "col")
            .addAttributes(":all", "style", "class")
            .addAttributes("img", "width", "height")
            .addProtocols("img", "src", "http", "https", "data");

    private final TemplateEngine templateEngine;
    private final String css;

    public PdfService(TemplateEngine templateEngine) throws IOException {
        this.templateEngine = templateEngine;
        try (InputStream in = getClass().getResourceAsStream("/pdf/pdf-layout.css")) {
            this.css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Page complète (en-tête + contenu) en XHTML, prête pour OpenHTMLToPDF. */
    public String buildXhtml(PieceHeader header, String contentHtml) {
        Context ctx = new Context();
        ctx.setVariable("h", header);
        ctx.setVariable("css", css);
        ctx.setVariable("content", toXhtmlFragment(contentHtml));
        String html = templateEngine.process("piece", ctx);

        // OpenHTMLToPDF exige du XHTML bien formé.
        Document doc = Jsoup.parse(html);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml).escapeMode(Entities.EscapeMode.xhtml);
        return doc.html();
    }

    public byte[] render(PieceHeader header, String contentHtml) {
        String xhtml = buildXhtml(header, contentHtml);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String toXhtmlFragment(String contentHtml) {
        String cleaned = Jsoup.clean(contentHtml == null ? "" : contentHtml, CONTENT_SAFELIST);
        Document frag = Jsoup.parseBodyFragment(cleaned);
        frag.outputSettings().syntax(Document.OutputSettings.Syntax.xml).escapeMode(Entities.EscapeMode.xhtml);
        return frag.body().html();
    }
}
