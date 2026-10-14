package fr.demo.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final PdfService pdfService;

    private final TiptapJsonConverter converter;

    public PdfController(PdfService pdfService, TiptapJsonConverter converter) {
        this.pdfService = pdfService;
        this.converter = converter;
    }

    @PostMapping("/preview")
    public ResponseEntity<byte[]> preview(@RequestBody PdfRequest request) {
        boolean fromJson = request.contentJson() != null && !request.contentJson().isNull();
        String html;
        if (fromJson) {
            log.info("Contenu TipTap reçu (JSON) :\n{}", request.contentJson().toPrettyString());
            html = converter.toHtml(request.contentJson());
            log.info("HTML converti depuis le JSON :\n{}", html);
        } else {
            html = request.contentHtml();
            log.info("Contenu TipTap reçu (HTML) :\n{}", html);
        }
        byte[] pdf = pdfService.render(request.header(), html);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("piece.pdf").build().toString())
                .body(pdf);
    }
}
