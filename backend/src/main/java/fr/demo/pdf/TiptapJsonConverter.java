package fr.demo.pdf;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** Convertit un document TipTap (editor.getJSON()) en fragment HTML, ensuite nettoyé par PdfService. */
@Component
public class TiptapJsonConverter {

    public String toHtml(JsonNode doc) {
        StringBuilder sb = new StringBuilder();
        children(doc, sb);
        return sb.toString();
    }

    private void children(JsonNode node, StringBuilder sb) {
        JsonNode content = node.path("content");
        if (content.isArray()) {
            content.forEach(child -> node(child, sb));
        }
    }

    private void node(JsonNode n, StringBuilder sb) {
        String type = n.path("type").asText();
        JsonNode attrs = n.path("attrs");
        switch (type) {
            case "text" -> text(n, sb);
            case "paragraph" -> wrap("p", n, sb);
            case "heading" -> wrap("h" + Math.min(6, Math.max(1, attrs.path("level").asInt(1))), n, sb);
            case "bulletList" -> wrap("ul", n, sb);
            case "orderedList" -> wrap("ol", n, sb);
            case "listItem" -> wrap("li", n, sb);
            case "blockquote" -> wrap("blockquote", n, sb);
            case "codeBlock" -> {
                sb.append("<pre><code>");
                children(n, sb);
                sb.append("</code></pre>");
            }
            case "hardBreak" -> sb.append("<br/>");
            case "horizontalRule" -> sb.append("<hr/>");
            case "table" -> {
                sb.append("<table><tbody>");
                children(n, sb);
                sb.append("</tbody></table>");
            }
            case "tableRow" -> wrap("tr", n, sb);
            case "tableCell", "tableHeader" -> {
                String tag = type.equals("tableHeader") ? "th" : "td";
                sb.append('<').append(tag);
                span("colspan", attrs, sb);
                span("rowspan", attrs, sb);
                sb.append('>');
                children(n, sb);
                sb.append("</").append(tag).append('>');
            }
            case "image" -> sb.append("<img src=\"").append(HtmlUtils.htmlEscape(attrs.path("src").asText()))
                    .append("\" alt=\"").append(HtmlUtils.htmlEscape(attrs.path("alt").asText(""))).append("\"/>");
            default -> children(n, sb); // nœud inconnu : on garde au moins son contenu
        }
    }

    private void wrap(String tag, JsonNode n, StringBuilder sb) {
        sb.append('<').append(tag).append('>');
        children(n, sb);
        sb.append("</").append(tag).append('>');
    }

    private void span(String name, JsonNode attrs, StringBuilder sb) {
        int v = attrs.path(name).asInt(1);
        if (v > 1) {
            sb.append(' ').append(name).append("=\"").append(v).append('"');
        }
    }

    private void text(JsonNode n, StringBuilder sb) {
        StringBuilder open = new StringBuilder();
        StringBuilder close = new StringBuilder();
        for (JsonNode mark : n.path("marks")) {
            String tag = switch (mark.path("type").asText()) {
                case "bold" -> "strong";
                case "italic" -> "em";
                case "strike" -> "s";
                case "underline" -> "u";
                case "code" -> "code";
                case "link" -> "a";
                default -> null;
            };
            if (tag == null) continue;
            if (tag.equals("a")) {
                open.append("<a href=\"").append(HtmlUtils.htmlEscape(mark.path("attrs").path("href").asText())).append("\">");
            } else {
                open.append('<').append(tag).append('>');
            }
            close.insert(0, "</" + tag + ">");
        }
        sb.append(open).append(HtmlUtils.htmlEscape(n.path("text").asText())).append(close);
    }
}
