import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import com.google.common.base.CharMatcher;
import org.json.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class TEX2TXT {

    public static void main(String args[]) throws IOException {
        TeXUtil util = new TeXUtil();
        //Loading an existing document
        File file = new File("/Users/CatLover/Documents/Tex/Examples/c5.pdf");
        PDDocument document = PDDocument.load(file);
        //Instantiate PDFTextStripper class
        PDFTextStripper pdfStripper = new PDFTextStripper() {
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                StringBuilder builder = new StringBuilder();
                boolean firstchar = true;
                for(TextPosition position: textPositions) {
                    float Y = position.getY();
                    float endY = position.getEndY();
                    float endX = position.getEndX();
                    PDFont font = position.getFont();
                    int[] codes = position.getCharacterCodes();
                    for(int code: codes) {
                        builder.append(util.fullTextToTeX(font, code, endX, Y, endY, firstchar));
                    }
                    if (firstchar)
                        firstchar = false;
                }
                writeString(builder.toString());
            }
        };
        pdfStripper.setWordSeparator("");
        //Retrieving text from PDF document
        String text = pdfStripper.getText(document);
        //The article ended. Now it is time to do the cleanup.
        String postamble = util.clearState();
        System.out.println(text + postamble);
        //Closing the document
        document.close();
    }
}
