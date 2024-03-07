package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx052Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx052Dao(String JNDIname) {
    super(JNDIname);
  }

  /**
   * 検索実行
   *
   * @return
   */
  @Override
  public boolean selectBy() {

    // 検索コマンド生成
    String command = createCommand();

    // 出力用検索条件生成
    outputQueryList();

    // 検索実行
    return super.selectBySQL(command);
  }

  /**
   * 更新処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {

    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
  }

  /**
   * 削除処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {

    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.checkDel(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 削除処理
    try {
      msgObj = this.deleteData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
    }
    return msgObj;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szMakercd = getMap().get("MAKERCD"); // 選択メーカーコード
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (szMakercd == null || sendBtnid == null) {
      System.out.println(super.getConditionLog());
      return "";
    }
    ArrayList<String> paramData = new ArrayList<String>();
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();


    StringBuffer sbSQL = new StringBuffer();

    if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid)) {

      sbSQL.append("select");
      sbSQL.append(" MAK1.MAKERCD"); // F1 ：メーカーコード
      sbSQL.append(", MAK1.MAKERAN"); // F2 ：メーカー名（ｶﾅ）
      sbSQL.append(", MAK1.MAKERKN"); // F3 ：メーカー名（漢字）
      sbSQL.append(", MAK1.JANCD"); // F4 ：JANコード
      sbSQL.append(", MAK1.DMAKERCD"); // F5 ：代表メーカーコード
      sbSQL.append(", MAK2.MAKERKN"); // F6 ：代表メーカー名（漢字）
      sbSQL.append(", DATE_FORMAT(MAK1.ADDDT, '%y/%m/%d')"); // F7 ：画面下部表示用_登録日
      sbSQL.append(", DATE_FORMAT(MAK1.UPDDT, '%y/%m/%d')"); // F8 ：画面下部表示用_更新日
      sbSQL.append(", MAK1.OPERATOR"); // F9 ：画面下部表示用_オペレーター
      sbSQL.append(", DATE_FORMAT(MAK1.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F10 ：排他チェック用：更新日(非表示)
      sbSQL.append(" from INAMS.MSTMAKER MAK1");
      sbSQL.append(" left join INAMS.MSTMAKER MAK2 on MAK2.MAKERCD = MAK1.DMAKERCD");
      sbSQL.append(" where IFNULL(MAK1.UPDKBN, 0) = 0");
      sbSQL.append(" and MAK1.MAKERCD = ? ");
      paramData.add(szMakercd);
      sbSQL.append(" order by MAK1.MAKERCD");
      setParamData(paramData);
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    new ArrayList<String>();

    // 共通箇所設定
    createCmnOutput(jad);

  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    new JSONObject();
    JSONArray msg = new JSONArray();

    userInfo.getId();

    map.get("SENDBTNID");
    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // メーカーマスタINSERT/UPDATE処理
    this.createSqlMAKER(data, map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    if (dataArray.size() > 0) {
      targetTable = "INAMS.MSTMAKER";
      targetWhere = "IFNULL(UPDKBN, 0) <> 1 and MAKERCD = ?";
      targetParam.add(data.optString("F1"));
      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F10"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
      if (count == 0) {
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * メーカーマスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlMAKER(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sb = new StringBuffer();

    String userId = userInfo.getId();
    map.get("SENDBTNID");

    ArrayList<String> prmData = new ArrayList<String>();
    ArrayList<String> valData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 5; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        values += String.valueOf(0 + 1);

      }

      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);
        valData.add(val);
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          values += ", ?";
          prmData.add(val);
        }
      }

      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, values);
        values = "";
      }
    }

    // メーカーマスタの登録・更新
    sb = new StringBuffer();
    sb.append(" REPLACE INTO INAMS.MSTMAKER ( ");
    sb.append("MAKERCD "); // メーカーコード
    sb.append(",MAKERAN "); // メーカー名（カナ）
    sb.append(",MAKERKN "); // メーカー名（漢字）
    sb.append(",JANCD "); // JANコード
    sb.append(",DMAKERCD "); // 代表メーカーコード
    sb.append(",UPDKBN "); // 更新区分：
    sb.append(",SENDFLG "); // 送信フラグ
    sb.append(",OPERATOR "); // オペレーター：
    sb.append(",ADDDT "); // 登録日：
    sb.append(",UPDDT "); // 更新日：
    sb.append(") ");
    sb.append("VALUES (");
    sb.append(StringUtils.join(valueData, ",").substring(2));
    sb.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sb.append(",0 ");
    sb.append(",'" + userId + "' ");
    sb.append(",(SELECT * FROM (SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1  THEN CURRENT_TIMESTAMP ELSE ADDDT END ");
    sb.append("FROM INAMS.MSTMAKER WHERE MAKERCD =" + valData.get(0) + " ) T1 ) ");
    sb.append(",CURRENT_TIMESTAMP ");
    sb.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sb.toString());

    sqlList.add(sb.toString());
    prmList.add(prmData);
    lblList.add("メーカーマスタ");

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    return sb.toString();
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  @SuppressWarnings("static-access")
  private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報

    JSONObject msgObj = new JSONObject();
    new JSONArray();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String szMakercd = map.get("MAKERCD");

    // 更新情報
    String values = "";
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      // szRTPattern = data.optString("F1"); // リードタイムターン
    }
    values = StringUtils.removeStart(values, ",");

    if (szMakercd.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    new ItemList();
    new JSONArray();
    StringBuffer sbSQL;
    ArrayList<String> prmData = new ArrayList<String>();

    // 削除処理：更新区分に"1"（削除）を登録
    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE INAMS.MSTMAKER ");
    sbSQL.append("SET ");
    sbSQL.append(" SENDFLG = 0");
    sbSQL.append(",UPDKBN =" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR ='" + userId + "'");
    sbSQL.append(",UPDDT = CURRENT_TIMESTAMP ");
    sbSQL.append(" WHERE MAKERCD = ? ");
    prmData.add(szMakercd);

    int count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("メーカーマスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
    }
    return msgObj;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    map.get(DefineReport.ID_PARAM_OBJ);

    JSONArray msg = new JSONArray();

    dataArray.getJSONObject(0);
    new ArrayList<String>();
    new MessageUtility();
    new JSONArray();

    new ItemList();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }
    return msg;
  }

  /**
   * チェック処理(削除)
   *
   * @throws Exception
   */
  @SuppressWarnings("static-access")
  public JSONArray checkDel(HashMap<String, String> map) {
    // 関連情報取得
    ItemList iL = new ItemList();
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> prmData = new ArrayList<String>();
    JSONArray msg = new JSONArray();
    JSONArray dbDatas = new JSONArray();
    MessageUtility mu = new MessageUtility();

    dataArray.getJSONObject(0);

    String szMakercd = map.get("MAKERCD"); // メーカーコード


    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // チェック処理
    // 部門紐付チェック:メーカーマスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    sbSQL.append("select DMAKERCD from INAMS.MSTMAKER where DMAKERCD = ? and IFNULL(UPDKBN, 0) <> 1 limit 1 ");
    prmData.add(szMakercd);
    dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
    if (dbDatas.size() > 0) {
      msg.add(mu.getDbMessageObj("EX1095", "代表メーカーコードで使用している為、"));
      return msg;
    }
    return msg;
  }
}
