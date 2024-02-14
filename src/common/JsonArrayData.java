/**
 *
 */
package common;

import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


/**
 * @author Omoto_Yuki
 *
 */
public class JsonArrayData {

  private String jsonString;

  public void setJsonString(String jsonString) {
    // 変換したオブジェクトを JSONArray にキャスト
    if (jsonString != null) {
      try {
        array = (JSONArray) JSONSerializer.toJSON(jsonString);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      // if (DefineReport.ID_DEBUG_MODE)
      // System.out.println("setJsonString Cast:" + jsonString);
    }
    this.jsonString = jsonString;
  }

  public String getJsonString() {
    return jsonString;
  }

  private static JSONArray array;

  /**
   * JSON ARRAY から指定のIDの"text"を戻す
   *
   * @param id 検索条件 KEY
   * @return データを戻す VALUE
   */
  public String getJSONText(String id) {

    // 定義
    String rtn = "";

    if (array != null) {
      for (Iterator it = array.iterator(); it.hasNext();) {

        JSONObject json = (JSONObject) it.next();
        if (json.getString("id").equals(id)) {
          rtn = json.getString(DefineReport.ID_JSON_TEXT);
          break;
        }

      }
    }
    return rtn;

  }

  /**
   * JSON ARRAY から指定のIDの"value"を戻す
   *
   * @param id 検索条件 KEY
   * @return データを戻す VALUE
   */
  public String getJSONValue(String id) {

    // 定義
    String rtn = "";

    if (array != null) {
      for (Iterator it = array.iterator(); it.hasNext();) {

        JSONObject json = (JSONObject) it.next();
        if (json.getString("id").equals(id)) {
          rtn = json.getString(DefineReport.ID_JSON_VALUE);
          break;
        }

      }
    }
    return rtn;

  }

}
