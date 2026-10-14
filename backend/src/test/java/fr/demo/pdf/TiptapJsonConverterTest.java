package fr.demo.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class TiptapJsonConverterTest {

    @Test
    void convertsHeadingParagraphMarksAndList() throws Exception {
        String json = """
            {"type":"doc","content":[
              {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"Titre"}]},
              {"type":"paragraph","content":[
                {"type":"text","text":"a < b ","marks":[{"type":"bold"}]},
                {"type":"text","text":"fin"}]},
              {"type":"bulletList","content":[{"type":"listItem","content":[
                {"type":"paragraph","content":[{"type":"text","text":"x"}]}]}]}
            ]}""";
        String html = new TiptapJsonConverter().toHtml(new ObjectMapper().readTree(json));
        assertEquals("<h2>Titre</h2><p><strong>a &lt; b </strong>fin</p><ul><li><p>x</p></li></ul>", html);
    }
}
