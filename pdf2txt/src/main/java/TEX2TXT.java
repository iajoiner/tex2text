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
        File file = new File("/Users/CatLover/Documents/Tex/Examples/c4.pdf");
        PDDocument document = PDDocument.load(file);

        //Instantiate PDFTextStripper class
        PDFTextStripper pdfStripper = new PDFTextStripper() {
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                String prevBaseFont = "";
                StringBuilder builder = new StringBuilder();
                TextPosition lastPosition = null;
                for(TextPosition position: textPositions) {
                    String baseFont = position.getFont().getName();
                    if (baseFont != null && !baseFont.equals(prevBaseFont))
                    {
                        builder.append("\\{").append(baseFont).append("}");
                        prevBaseFont = baseFont;
                    }
                    /*builder.append("Y=");
                    builder.append(position.getY());
                    builder.append("H=");
                    builder.append(position.getHeight());*/
                    /*if((position.getY() < lastPosition.getY()
                            && position.getHeight() < lastPosition.getHeight())
                            || (position.getY() > lastPosition.getTextPosition().getY()
                            && position.getHeight() > lastPosition.getTextPosition().getHeight()))
                        line.add(WordSeparator.getSeparator());*/
                    int[] codes = position.getCharacterCodes();
                    for(int code: codes) {
                        builder.append(code);
                        builder.append(" ");
                    }
                    lastPosition = position;
                }

                writeString(builder.toString());

            }
        };

        //Retrieving text from PDF document
        String text = pdfStripper.getText(document);
        System.out.println(text);

        //Closing the document
        document.close();
    }
}
