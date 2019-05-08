import com.google.common.base.CharMatcher;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

public class TeXUtil {
    private Stack<SSStatus> ssstatus;
    private boolean accentMode;
    private String fs;
    private boolean mathMode;
    private SymbolDB db;
    private Hashtable<String, String> maccDict;
    public TeXUtil() throws IOException {
        ssstatus = new Stack<SSStatus>();
        fs = "rm";
        accentMode = false;//as in the state of being right after an accent
        mathMode = false;
        db = new SymbolDB();
        maccDict = new Hashtable<String, String>();
        maccDict.put("\\vec","\\vec");
        maccDict.put("\\widehat","\\widehat");
        maccDict.put("\\widetilde","\\widetilde");
        maccDict.put("\\^","\\hat");
        maccDict.put("\\v","\\check");
        maccDict.put("\\u","\\breve");
        maccDict.put("\\`","\\grave");
        maccDict.put("\\~","\\tilde");
        maccDict.put("\\=","\\bar");
        maccDict.put("\\.","\\dot");
        maccDict.put("\\","\\ddot");
        maccDict.put("\\'","\\acute");
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
    private String fullTextToTeX(PDFont font, int code){
        String shortFontName = fontShortName(font);
        try {
            JSONObject info = db.getInfo(shortFontName, code);
            String teXCode = info.getString("value");
            StringBuilder preamble1 = new StringBuilder("");
            StringBuilder preamble2 = new StringBuilder("");
            StringBuilder postamble = new StringBuilder("");
            Boolean text = info.getBoolean("text");
            Boolean math = info.getBoolean("math");
            Boolean tacc = info.getBoolean("tacc");
            Boolean macc = info.getBoolean("macc");
            String newFont = info.getString("font");
            //Font change, rm is seen as having no font
            if (!newFont.equals(fs)) {
                if (!fs.equals("rm"))
                    preamble1.insert(0, '}');
                if (!newFont.equals("rm")) {
                    preamble2.append('\\');
                    preamble2.append(newFont);
                    preamble2.append('{');
                }
                fs = newFont;
            }
            //Subscripts/Superscripts

            //Enter or leave math mode
            if (mathMode && !math && !macc) {
                mathMode = false;
                preamble1.append('$');
            }
            else if (!mathMode && !text && !tacc) {
                mathMode = true;
                preamble2.insert(0,'$');
            }
            //Accents
            if (accentMode) {//If accent mode is ever entered we need to leave it at once
                postamble.append('}');
                accentMode = false;
            }
            if ((mathMode && macc) || (!mathMode && tacc)) {//Right now assume that anything that can be an accent is an accent
                postamble.append('{');
                if (mathMode)
                    teXCode = maccDict.get(teXCode);
                accentMode = true;
            }
        }
        catch(JSONException e) {
            return "\\" + shortFontName + "{" + code + "}";
        }
    }
}
