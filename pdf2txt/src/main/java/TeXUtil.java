import com.google.common.base.CharMatcher;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

public class TeXUtil {
    private Stack<SSStatus> ssstatus;
    private boolean accentMode;
    private String fs;
    private boolean mathMode;
    private SymbolDB db;
    float endY;//Positions
    float endX;
    float Y;
    int height;//Height
    //boolean test;
    public TeXUtil() throws IOException {
        ssstatus = new Stack<SSStatus>();
        fs = "rm";
        accentMode = false;//as in the state of being right after an accent
        mathMode = false;
        db = new SymbolDB();
        endY = 0;
        endX = 0;
        Y = 0;
        height = 0;
        //test = false;
        //System.out.println("TeXUtil initialized!");
    }
    private static String fontShortName(PDFont font) {
        String[] segments = font.getName().split("\\+");
        return segments[segments.length - 1];
    }
    private static int fontHeight(PDFont font) {
        CharMatcher matcher = CharMatcher.inRange('0', '9');
        return Integer.parseInt(matcher.retainFrom(fontShortName(font)));
    }
    private static String fontClass(PDFont font) {
        CharMatcher matcher = CharMatcher.inRange('A', 'Z');
        return (matcher.retainFrom(fontShortName(font))).toLowerCase();
    }
    private String textToTeX(String shortFontName, int code) throws JSONException {
        JSONObject info = db.getInfo(shortFontName, code);
        return info.getString("value");
    }
    public String fullTextToTeX(PDFont font, int code, float newEndX, float newY, float newEndY){
        String shortFontName = fontClass(font);
        try {
            JSONObject info = db.getInfo(shortFontName, code);
            String teXCode = info.getString("value");
            StringBuilder preamble1 = new StringBuilder("");
            StringBuilder preamble2 = new StringBuilder("");
            StringBuilder postamble = new StringBuilder("");
            String usage = info.getString("usage");
            String newFont;
            if (info.has("font"))
                newFont = info.getString("font");
            else
                newFont = "";
            int newHeight = fontHeight(font);
            //Font change, rm is seen as having no font
            if (!newFont.equals(fs)) {
                if (!fs.isEmpty())
                    preamble1.insert(0, '}');
                if (!newFont.isEmpty()) {
                    preamble2.append('\\');
                    preamble2.append(newFont);
                    preamble2.append('{');
                }
               //preamble1.insert(0,  " fs = " + fs + " nFs = " + newFont + "\n");
                fs = newFont;
            }
            if (height == 0) {
                //preamble2.append(" Meow! am = " + accentMode + " fs = " + fs + " mm = " + mathMode + "\n");
            }
            //Subscripts/Superscripts
            if (height > newHeight && newEndX > endX) {//New subscript/superscript
                if (newEndY < endY) {//New superscript
                    //ssstatus.push(SSStatus.SUP);
                    preamble2.insert(0, "^{");
                }
                else if (newY > Y) {//New subscript
                    //ssstatus.push(SSStatus.SUB);
                    preamble2.insert(0, "_{");
                }
                //else {
                  //  System.out.println("Please investigate the situation: texcode = " + teXCode + "endY = " + endY + " Y=" + Y + " endX=" + endX + " newEndY=" + newEndY + " newY=" + newY + " newEndX= " + newEndX);
                //}
            }
            else if (height < newHeight && height != 0) {
                //ssstatus.pop();
                preamble1.append('}');
            }
            height = newHeight;
            endX = newEndX;
            endY = newEndY;
            Y = newY;
            //Enter or leave math mode
            if (mathMode && (usage == "t" || usage == "ta")) {
                mathMode = false;
                preamble1.append('$');
            }
            else if (!mathMode && (usage == "m" || usage == "ma")) {
                mathMode = true;
                preamble2.insert(0,'$');
            }
            //Accents
            if (accentMode) {//If accent mode is ever entered we need to leave it at once
                postamble.append('}');
                accentMode = false;
            }
            if ((mathMode && (usage == "tma" || usage == "ma")) || (!mathMode && (usage == "tma" || usage == "ta"))) {//Right now assume that anything that can be an accent is an accent
                postamble.append('{');
                if (mathMode)
                    teXCode = info.getString("mav");
                accentMode = true;
            }
            if (teXCode.charAt(0) == '\\')
                return preamble1.toString() + preamble2.toString() + teXCode + ' ' + postamble.toString();
            else
                return preamble1.toString() + preamble2.toString() + teXCode + postamble.toString();
        }
        catch(JSONException e) {
            StringBuilder preamble = new StringBuilder("");
            if (!fs.isEmpty()) {
                preamble.append('}');
                fs = "";
            }
            return preamble.toString() + "\\" + shortFontName + "{" + code + "}";
        }
    }
}
