package fr.demo.pdf;

import com.fasterxml.jackson.databind.JsonNode;

/** Le contenu est fourni soit en HTML (editor.getHTML()), soit en JSON TipTap (editor.getJSON()). */
public record PdfRequest(PieceHeader header, String contentHtml, JsonNode contentJson) {
}
