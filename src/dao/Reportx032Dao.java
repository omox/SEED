package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx032Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx032Dao(String JNDIname) {
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
    JSONArray msg = this.check(map);

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
    ArrayList<String> paramData = new ArrayList<String>();
    String bunrui = getMap().get("BUNRUI"); // 検索条件分類区分
    String bmoncd = getMap().get("BUMON"); // 部門コード
    String daicd = getMap().get("DAICD"); // 大分類コード
    String tablename = "";

    // パラメータ確認
    // 必須チェック
    if (bmoncd == null || daicd == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    if (StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)) {
      tablename = "INAMS.MSTCHUBRUI";

    } else if (StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)) {
      tablename = "INAMS.MSTCHUBRUI_URI";

    } else if (StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)) {
      tablename = "INAMS.MSTCHUBRUI_YOT";

    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" select");
    sbSQL.append(" null as CHUCD");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", null");
    sbSQL.append(", 1 as SUPDKBN");
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(", null");
    // TODO 暫定
    sbSQL.append(" from (values ROW('', '')) as X(value, TEXT)");
    sbSQL.append(" union all");
    sbSQL.append(" select");
    sbSQL.append(" TCH.CHUCD"); // F1 ：中分類コード
    sbSQL.append(", TCH.CHUBRUIAN"); // F2 ：大分類名（カナ）
    sbSQL.append(", TCH.CHUBRUIKN"); // F3 ：大分類名（漢字）
    sbSQL.append(", TCH.ATR1"); // F4 ：属性1
    sbSQL.append(", TCH.ATR2"); // F5 ：属性2
    sbSQL.append(", TCH.ATR3"); // F6 ：属性3
    sbSQL.append(", TCH.ATR4"); // F7 ：属性4
    sbSQL.append(", TCH.ATR5"); // F8 ：属性5
    sbSQL.append(", TCH.ATR6"); // F9 ：属性6
    sbSQL.append(", DATE_FORMAT(TCH.ADDDT, '%y/%m/%d')"); // F10 ：登録日
    sbSQL.append(", DATE_FORMAT(TCH.UPDDT, '%y/%m/%d')"); // F11 ：更新日
    sbSQL.append(", TCH.OPERATOR"); // F12 ：オペレータ
    sbSQL.append(", 0 as SUPDKBN"); // F13 ：新規登録区分(非表示)
    sbSQL.append(", TCH.UPDKBN"); // F14 ：更新区分
    sbSQL.append(", DATE_FORMAT(TCH.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F15 ：(非表示)
    sbSQL.append(" from " + tablename + " TCH");
    sbSQL.append(" where COALESCE(TCH.UPDKBN, 0) = 0 and TCH.BMNCD = ? and TCH.DAICD = ? ");
    paramData.add(bmoncd);
    paramData.add(daicd);
    sbSQL.append(" order by CHUCD IS NULL ASC, CHUCD, SUPDKBN");
    setParamData(paramData);

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

  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  @SuppressWarnings("static-access")
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONObject msgObj = new JSONObject();
    JSONArray msg = new JSONArray();

    String updateRows = ""; // 更新データ
    // Object[] createRows = new Object[]{}; // 新規登録データ

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String bmoncd = map.get("BUMON"); // 入力部門コード
    String daicd = map.get("DAICD"); // 入力大分類コード
    String chucd = ""; // 入力中分類コード
    String adddt = ""; // 登録日
    String updflg = ""; // 更新フラグ
    String supdate = ""; // 新規登録フラグ
    // 入力部門コード
    // 更新情報
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      chucd = data.optString("F1");
      supdate = data.optString("F13");
      updflg = data.optString("F14");
      String values = "";
      values += bmoncd + ",";
      values += daicd + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F1"), "null") + ",";
      values += StringUtils.defaultIfEmpty("'" + data.optString("F2") + "'", "null") + ",";
      values += StringUtils.defaultIfEmpty("'" + data.optString("F3") + "'", "null") + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F4"), "null") + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F5"), "null") + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F6"), "null") + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F7"), "null") + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F8"), "null") + ",";
      values += StringUtils.defaultIfEmpty(data.optString("F9"), "null") + ",";
      values += (StringUtils.isEmpty(updflg) ? DefineReport.ValUpdkbn.NML.getVal() : DefineReport.ValUpdkbn.DEL.getVal()) + ",";;
      values += "null,";
      values += "null,";
      values += "null,";
      values += "null";

      // 削除処理
      if (StringUtils.equals(updflg, DefineReport.ValUpdkbn.DEL.getVal())) {
        JSONArray msgdl = this.deleteChildData(map, data);
        if (msgdl.size() > 0) {
          msg.add(msgdl);
        }
      }

      if (!StringUtils.isEmpty(bmoncd) && !StringUtils.isEmpty(daicd)) {
        // 未入力新規登録データを省く
        updateRows += ",(" + values + ")";
      }
    }
    updateRows = StringUtils.removeStart(updateRows, ",");

    if (updateRows.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return msgObj;
    }

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    ArrayList<String> prmData = new ArrayList<String>();

    // 重複チェック：大分類コード
    if (StringUtils.equals(supdate, "1")) {
      sbSQL = new StringBuffer();
      sbSQL.append("select * from INAMS.MSTCHUBRUI CHU where COALESCE(CHU.UPDKBN, 0) = 0 and CHU.BMNCD = " + bmoncd + " and CHU.DAICD = " + daicd + " and CHU.CHUCD = " + chucd);
      dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
      if (dbDatas.size() > 0) {
        msg.add(MessageUtility.getMsg("中分類コードが重複しています。"));
      }

      // 新規登録(論理削除したデータの上書きの場合)
      sbSQL = new StringBuffer();
      sbSQL.append("select * from INAMS.MSTCHUBRUI CHU where COALESCE(CHU.UPDKBN, 0) = " + DefineReport.ValUpdkbn.DEL.getVal() + " and CHU.BMNCD = " + bmoncd + " and CHU.DAICD = " + daicd
          + " and CHU.CHUCD = " + chucd);
      dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
      if (dbDatas.size() > 0) {
        adddt = ", ADDDT = RE.ADDDT";
      }
    }

    // 更新SQL
    sbSQL = new StringBuffer();
    sbSQL.append("merge into INAMS.MSTCHUBRUI as T using (select");
    sbSQL.append(" T1.BMNCD");
    sbSQL.append(", T1.DAICD");
    sbSQL.append(", T1.CHUCD");
    sbSQL.append(", T1.CHUBRUIAN");
    sbSQL.append(", T1.CHUBRUIKN");
    sbSQL.append(", T1.ATR1");
    sbSQL.append(", T1.ATR2");
    sbSQL.append(", T1.ATR3");
    sbSQL.append(", T1.ATR4");
    sbSQL.append(", T1.ATR5");
    sbSQL.append(", T1.ATR6");
    sbSQL.append(", T1.UPDKBN");
    sbSQL.append(", T1.SENDFLG");
    // TODO 暫定
    sbSQL.append(", '" + userId + "' as OPERATOR");
    sbSQL.append(", CURRENT_TIMESTAMP as ADDDT");
    sbSQL.append(", CURRENT_TIMESTAMP as UPDDT");
    sbSQL.append(" from (values " + updateRows + ") as T1(");
    sbSQL.append(" BMNCD");
    sbSQL.append(", DAICD");
    sbSQL.append(", CHUCD");
    sbSQL.append(", CHUBRUIAN");
    sbSQL.append(", CHUBRUIKN");
    sbSQL.append(", ATR1");
    sbSQL.append(", ATR2");
    sbSQL.append(", ATR3");
    sbSQL.append(", ATR4");
    sbSQL.append(", ATR5");
    sbSQL.append(", ATR6");
    sbSQL.append(", UPDKBN");
    sbSQL.append(", SENDFLG");
    sbSQL.append(", OPERATOR");
    sbSQL.append(", ADDDT");
    sbSQL.append(", UPDDT)) as RE on T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD");
    sbSQL.append(" when matched then update set");
    sbSQL.append(" BMNCD = RE.BMNCD");
    sbSQL.append(", DAICD = RE.DAICD");
    sbSQL.append(", CHUCD = RE.CHUCD");
    sbSQL.append(", CHUBRUIAN = RE.CHUBRUIAN");
    sbSQL.append(", CHUBRUIKN = RE.CHUBRUIKN");
    sbSQL.append(", ATR1 = RE.ATR1");
    sbSQL.append(", ATR2 = RE.ATR2");
    sbSQL.append(", ATR3 = RE.ATR3");
    sbSQL.append(", ATR4 = RE.ATR4");
    sbSQL.append(", ATR5 = RE.ATR5");
    sbSQL.append(", ATR6 = RE.ATR6");
    sbSQL.append(", UPDKBN = RE.UPDKBN");
    sbSQL.append(", SENDFLG = RE.SENDFLG");
    sbSQL.append(", OPERATOR = RE.OPERATOR");
    sbSQL.append(adddt);
    sbSQL.append(", UPDDT = RE.UPDDT");
    sbSQL.append(" when not matched then insert values (");
    sbSQL.append(" RE.BMNCD");
    sbSQL.append(", RE.DAICD");
    sbSQL.append(", RE.CHUCD");
    sbSQL.append(", RE.CHUBRUIAN");
    sbSQL.append(", RE.CHUBRUIKN");
    sbSQL.append(", RE.ATR1");
    sbSQL.append(", RE.ATR2");
    sbSQL.append(", RE.ATR3");
    sbSQL.append(", RE.ATR4");
    sbSQL.append(", RE.ATR5");
    sbSQL.append(", RE.ATR6");
    sbSQL.append(", RE.UPDKBN");
    sbSQL.append(", RE.SENDFLG");
    sbSQL.append(", RE.OPERATOR");
    sbSQL.append(", RE.ADDDT");
    sbSQL.append(", RE.UPDDT)");

    if (msg.size() > 0) {
      // 重複チェック_エラー時
      msgObj.put(MsgKey.E.getKey(), msg);

    } else {
      // 更新処理実行
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(sbSQL);
      int count = super.executeSQL(sbSQL.toString(), prmData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("中分類を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }
    }
    return msgObj;
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  @SuppressWarnings("static-access")
  private JSONArray deleteChildData(HashMap<String, String> map, JSONObject date) throws Exception {

    String Bmoncd = map.get("BUMON"); // 部門コード
    String Daicd = map.get("DAICD"); // 大分類コード
    String Chucd = date.optString("F1"); // 中分類コード

    JSONObject msgObj = new JSONObject();
    JSONArray msg = new JSONArray();

    // 基本INSERT/UPDATE文
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    StringBuffer sbSQL;
    ArrayList<String> prmData = new ArrayList<String>();

    // 部門紐付チェック:商品マスタ
    sbSQL = new StringBuffer();
    sbSQL.append("select SHN.* from INAMS.MSTSHN SHN where SHN.BMNCD = " + Bmoncd + " and SHN.DAICD = " + Daicd + " and SHN.CHUCD = " + Chucd + " and COALESCE(SHN.UPDKBN, 0) <> 1");
    dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
    if (dbDatas.size() > 0) {
      msg.add(MessageUtility.getMsg("商品マスタにて中分類コードを使用している為、削除出来ません"));

    }

    if (msg.size() > 0) {

    } else {
      // 中分類マスタ_他中分類検索
      sbSQL = new StringBuffer();
      sbSQL.append("select CHU.* from INAMS.MSTCHUBRUI CHU where CHU.BMNCD = " + Bmoncd + " and CHU.DAICD = " + Daicd + " and COALESCE(CHU.UPDKBN, 0) <> 1");
      dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
      if (dbDatas.size() > 1) {

      } else {
        // 紐付く親要素の削除
        // 削除処理：大分類マスタの更新区分に"1"（削除）を登録
        sbSQL = new StringBuffer();
        sbSQL.append(
            "update INAMS.MSTDAIBRUI DAI set DAI.UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + ", DAI.UPDDT = CURRENT_TIMESTAMP where DAI.BMNCD = " + Bmoncd + " and DAI.DAICD = " + Daicd);

        int count = super.executeSQL(sbSQL.toString(), prmData);
        if (StringUtils.isEmpty(getMessage())) {
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("大分類を " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
          msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
        } else {
          msgObj.put(MsgKey.E.getKey(), getMessage());
        }
      }
      // 紐付く子要素の削除
      // 削除処理：小分類マスタの更新区分に"1"（削除）を登録
      sbSQL = new StringBuffer();
      sbSQL.append("update INAMS.MSTSHOBRUI SHO set SHO.UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + ", SHO.UPDDT = CURRENT_TIMESTAMP where SHO.BMNCD = " + Bmoncd + " and SHO.DAICD = " + Daicd
          + " and SHO.CHUCD = " + Chucd);

      int count = super.executeSQL(sbSQL.toString(), prmData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("小分類を " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }
    }
    return msg;
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

    userInfo.getId();

    // 更新情報
    String values = "";
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      data.optString("F2");
      data.optString("F2");
      values += ",'" + data.optString("F2") + "'"; // 大分類コード
    }
    values = StringUtils.removeStart(values, ",");

    if (values.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    new ItemList();
    new JSONArray();
    new ArrayList<String>();

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

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 各データ行チェック
    // if(!outobj.equals(DefineReport.Button.DELETE.getObj())) {
    // FieldType fieldType = FieldType.GRID;
    // for (int i = 0; i < dataArray.size(); i++) {
    // JSONObject data = dataArray.getJSONObject(i);
    // String[] addParam = {Integer.toString(i+1)};
    // // 比較対象店舗必須チェック
    // if(!InputChecker.isNotNull(data.optString("F2"))){
    // msg.add(MessageUtility.getCheckNullMessage(DefineReport.Select.M_TENPO.getTxt(), fieldType,
    // addParam));
    // }
    // }
    //
    // }
    return msg;
  }
}
