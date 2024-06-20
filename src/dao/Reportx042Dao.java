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
import org.apache.commons.lang.math.NumberUtils;
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
public class Reportx042Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx042Dao(String JNDIname) {
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
    ArrayList<String> paramData = new ArrayList<>();
    String szRTPattern = getMap().get("READTMPTN"); // 選択リードタイムパターン
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (szRTPattern == null || sendBtnid == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)) {

      sbSQL.append(" select");
      sbSQL.append(" right('00'||READTMPTN,3)"); // F1 ：リードタイムパターン
      sbSQL.append(", READTMPTNKN"); // F2 ：リードタイム名称
      sbSQL.append(", READTM_MON"); // F3 ：リードタイム_月
      sbSQL.append(", READTM_TUE"); // F4 ：リードタイム_火
      sbSQL.append(", READTM_WED"); // F5 ：リードタイム_水
      sbSQL.append(", READTM_THU"); // F6 ：リードタイム_木
      sbSQL.append(", READTM_FRI"); // F7 ：リードタイム_金
      sbSQL.append(", READTM_SAT"); // F8 ：リードタイム_土
      sbSQL.append(", READTM_SUN"); // F9 ：リードタイム_日
      sbSQL.append(", DATE_FORMAT(ADDDT, '%y/%m/%d')"); // F10 ：登録日
      sbSQL.append(", DATE_FORMAT(UPDDT, '%y/%m/%d')"); // F11 ：更新日
      sbSQL.append(", OPERATOR"); // F12 ：オペレータ
      sbSQL.append(", DATE_FORMAT(UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F13 : 排他チェック用：更新日(非表示)
      sbSQL.append(" from INAMS.MSTREADTM");
      sbSQL.append(" where COALESCE(UPDKBN, 0) = 0");
      sbSQL.append(" and READTMPTN = ? ");
      paramData.add(szRTPattern);
      sbSQL.append(" order by READTMPTN");
      setParamData(paramData);
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
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
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // 排他チェック用
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // リードタイムパターンマスタINSERT/UPDATE処理
    this.createSqlTREADTM(data, map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<>();
    if (dataArray.size() > 0) {
      targetTable = "INAMS.MSTREADTM";
      targetWhere = "COALESCE(UPDKBN, 0) <> 1 and READTMPTN = ?";
      targetParam.add(data.optString("F1"));
      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F13"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
    }

    ArrayList<Integer> countList = new ArrayList<>();
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
   * リードタイムパターンマスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTREADTM(JSONObject data, HashMap<String, String> map, User userInfo) {

    new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    map.get("SENDBTNID");

    ArrayList<String> prmData = new ArrayList<>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 9; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        values += String.valueOf(0 + 1);

      }

      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          values += ", ?";
          prmData.add(val);
        }
      }

      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }
    // sbSQL = new StringBuffer();

    //
    StringBuffer sb = new StringBuffer();
    sb.append(" INSERT into INAMS.MSTREADTM  ( ");
    sb.append(" READTMPTN "); // リードタイムパターン
    sb.append(",READTMPTNKN "); // リードタイム名称
    sb.append(",READTM_MON "); // リードタイム_月
    sb.append(",READTM_TUE "); // リードタイム_火
    sb.append(",READTM_WED "); // リードタイム_水
    sb.append(",READTM_THU "); // リードタイム_木
    sb.append(",READTM_FRI "); // リードタイム_金
    sb.append(",READTM_SAT "); // リードタイム_土
    sb.append(",READTM_SUN "); // リードタイム_日
    sb.append(",UPDKBN "); // 更新区分：
    sb.append(",SENDFLG "); // 送信フラグ
    sb.append(",OPERATOR "); // オペレーター：
    sb.append(",ADDDT "); // 登録日：
    sb.append(",UPDDT "); // 更新日：
    sb.append(")");
    sb.append("select ");
    sb.append(" READTMPTN AS READTMPTN "); // リードタイムパターン
    sb.append(",READTMPTNKN AS READTMPTNKN"); // リードタイム名称
    sb.append(",READTM_MON AS READTM_MON "); // リードタイム_月
    sb.append(",READTM_TUE AS READTM_TUE "); // リードタイム_火
    sb.append(",READTM_WED AS READTM_WED "); // リードタイム_水
    sb.append(",READTM_THU AS READTM_THU "); // リードタイム_木
    sb.append(",READTM_FRI AS READTM_FRI "); // リードタイム_金
    sb.append(",READTM_SAT AS READTM_SAT "); // リードタイム_土
    sb.append(",READTM_SUN AS READTM_SUN "); // リードタイム_日
    sb.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
    sb.append(", 0 as SENDFLG"); // 送信フラグ
    sb.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
    sb.append(", CURRENT_TIMESTAMP AS ADDDT "); // 登録日：
    sb.append(", CURRENT_TIMESTAMP  AS UPDDT "); // 更新日：
    sb.append(" FROM (VALUES ROW ");
    sb.append(StringUtils.join(valueData, ",") + ") as T1(NUM, ");
    sb.append(" READTMPTN");
    sb.append(", READTMPTNKN");
    sb.append(", READTM_MON");
    sb.append(", READTM_TUE");
    sb.append(", READTM_WED");
    sb.append(", READTM_THU");
    sb.append(", READTM_FRI");
    sb.append(", READTM_SAT");
    sb.append(", READTM_SUN)  ");
    sb.append(" ON DUPLICATE KEY UPDATE ");// リードタイムパターンが重複した時のUPDATE処理
    sb.append("READTMPTN = VALUES(READTMPTN) ");
    sb.append(",READTMPTNKN = VALUES(READTMPTNKN) ");
    sb.append(",READTM_MON = VALUES(READTM_MON) ");
    sb.append(",READTM_TUE = VALUES(READTM_TUE) ");
    sb.append(",READTM_WED = VALUES(READTM_WED) ");
    sb.append(",READTM_THU = VALUES(READTM_THU) ");
    sb.append(",READTM_FRI = VALUES(READTM_FRI ) ");
    sb.append(",READTM_SAT = VALUES(READTM_SAT) ");
    sb.append(",READTM_SUN= VALUES(READTM_SUN) ");
    sb.append(",UPDKBN= VALUES(UPDKBN) ");
    sb.append(",SENDFLG = VALUES(SENDFLG) ");
    sb.append(",OPERATOR = VALUES(OPERATOR) ");
    sb.append(",ADDDT= (case when (select COALESCE(UPDKBN, 0) FROM INAMS.MSTREADTM where READTMPTN = T1.READTMPTN) = 1 then CURRENT_TIMESTAMP  else ADDDT end ) ");
    sb.append(",UPDDT = VALUES(UPDDT) ");



    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sb.toString());

    sqlList.add(sb.toString());
    prmList.add(prmData);
    lblList.add("リードタイムパターンマスタ");

    // クリア
    prmData = new ArrayList<>();
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
    JSONArray.fromObject(map.get("DATA"));

    JSONObject msgObj = new JSONObject();
    new JSONArray();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String szRTPattern = map.get("READTMPTN");

    if (szRTPattern.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    new ItemList();
    new JSONArray();
    StringBuffer sbSQL;
    ArrayList<String> prmData = new ArrayList<>();

    // 削除処理：更新区分に"1"（削除）を登録
    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE INAMS.MSTREADTM ");
    sbSQL.append("SET ");
    sbSQL.append(" SENDFLG = 0");
    sbSQL.append(",UPDKBN =" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR ='" + userId + "'");
    sbSQL.append(",UPDDT = CURRENT_TIMESTAMP ");
    sbSQL.append(" WHERE READTMPTN = ? ");
    prmData.add(szRTPattern);

    int count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("リードタイムマスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
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
    MessageUtility mu = new MessageUtility();
    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<>();

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    // 入力値を取得
    JSONObject data = dataArray.getJSONObject(0);
    new ItemList();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 重複チェック：リードタイムパターン

    if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
      paramData = new ArrayList<>();
      paramData.add(data.getString("F1"));
      sqlcommand = "select count(*) as VALUE from INAMS.MSTREADTM where COALESCE(UPDKBN, 0) <> 1 and READTMPTN = ?";

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        JSONObject o = mu.getDbMessageObj("E00004", "リードタイムパターン");
        msg.add(o);
        return msg;
      }
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
    new ItemList();
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> prmData = new ArrayList<>();
    JSONArray msg = new JSONArray();
    JSONArray dbDatas = new JSONArray();
    MessageUtility mu = new MessageUtility();

    dataArray.getJSONObject(0);

    String szRTPattern = map.get("READTMPTN"); // リードタイムパターン

    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // チェック処理
    // 部門紐付チェック:商品マスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<>();
    sbSQL.append("select READTMPTN from INAMS.MSTSHN where READTMPTN = ? and COALESCE(UPDKBN, 0) <> 1 LIMIT 1");
    prmData.add(szRTPattern);
    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
    if (dbDatas.size() > 0) {
      msg.add(mu.getDbMessageObj("E00006", new String[] {}));
      return msg;
    }

    // 部門紐付チェック店舗部門マスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<>();
    sbSQL.append("select READTMPTN from INAMS.MSTTENBMN where READTMPTN = ? and COALESCE(UPDKBN, 0) <> 1 LIMIT 1");
    prmData.add(szRTPattern);
    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
    if (dbDatas.size() > 0) {
      msg.add(mu.getDbMessageObj("E00006", new String[] {}));
      return msg;
    }
    return msg;
  }
}
