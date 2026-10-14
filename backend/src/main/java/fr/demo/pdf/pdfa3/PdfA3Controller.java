package fr.demo.pdf.pdfa3;

import fr.demo.pdf.PdfRequest;
import fr.demo.pdf.TiptapJsonConverter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** EXEMPLE ISOLÉ : même contrat que /api/pdf/preview, mais renvoie un PDF/A-3B avec métadonnées + XML joint. */
@RestController
@RequestMapping("/api/pdf")
public class PdfA3Controller {

    private final PdfA3Service service;
    private final TiptapJsonConverter converter;

    public PdfA3Controller(PdfA3Service service, TiptapJsonConverter converter) {
        this.service = service;
        this.converter = converter;
    }

    @PostMapping("/preview-pdfa3")
    public ResponseEntity<byte[]> previewPdfA3(@RequestBody PdfRequest request) {
        String html = request.contentJson() != null && !request.contentJson().isNull()
                ? converter.toHtml(request.contentJson())
                : request.contentHtml();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("piece-pdfa3.pdf").build().toString())
                .body(service.render(request.header(), html));
    }
}
