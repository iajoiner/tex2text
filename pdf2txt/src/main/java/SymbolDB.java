import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.json.*;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;

public class SymbolDB {
    private JSONObject db;
    public SymbolDB() throws IOException {
        URL url = Resources.getResource("expanded_symbol_db.json");
        String text = Resources.toString(url, Charsets.US_ASCII);
        db = new JSONObject(text);
        //System.out.println(text.substring(0, 100));
    }
    public JSONObject getInfo(String font, int code) throws JSONException {
            JSONObject fontDict = (JSONObject) db.get(font);
            JSONObject codeDict = (JSONObject) fontDict.get("values");
            String codeStr = Integer.toString(code);
            return (JSONObject) codeDict.get(codeStr);
    }
}
