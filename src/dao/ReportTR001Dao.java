package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import authentication.defines.Consts;
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
public class ReportTR001Dao extends ItemDao {

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
  public ReportTR001Dao(String JNDIname) {
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

  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();
    JSONArray msgList = this.check(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }

    return option;
  }

  // 削除処理
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();
    /*
     * JSONArray msgList = this.checkDel(map, userInfo, sysdate);
     *
     * if(msgList.size() > 0){ option.put(MsgKey.E.getKey(), msgList); return option; }
     *
     * // 更新処理 try { option = this.deleteData(map, userInfo, sysdate); } catch (Exception e) {
     * e.printStackTrace(); option.put(MsgKey.E.getKey(),
     * MessageUtility.getMessage(Msg.E00001.getVal())); }
     */
    option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    return option;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    MessageUtility mu = new MessageUtility();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 入力データのチェック
    JSONObject data = dataArray.getJSONObject(0);
    String shnCd = data.optString("F1"); // 商品コード
    String binKbn = data.optString("F2"); // 便区分

    // 商品コードが8桁未満はエラー
    if (shnCd.length() < 8) {
      // 既に存在している配送グループ
      msg.add(mu.getDbMessageObj("E11089", new String[] {}));
      return msg;
    }

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(shnCd);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTSHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // マスタに登録のない商品
      msg.add(mu.getDbMessageObj("E11046", new String[] {}));
      return msg;
    }

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(shnCd);
    }

    if (StringUtils.isEmpty(binKbn)) {
      sqlWhere += "BINKBN=null AND ";
    } else {
      sqlWhere += "BINKBN=? AND ";
      paramData.add(binKbn);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_SHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() != 0) {
      // 既に登録のある商品
      msg.add(mu.getDbMessageObj("E20002", new String[] {}));
      return msg;
    }

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() != 0) {
      // 既に登録のある店舗
      msg.add(mu.getDbMessageObj("E20003", new String[] {}));
      return msg;
    }

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    // 商品コード入力値チェック
    if (StringUtils.isEmpty(shnCd) && shnCd.length() >= 2) {
      sqlWhere += "SUBSTR(SHNCD,1,2)=null AND ";
    } else {
      sqlWhere += "SUBSTR(SHNCD,1,2)=? AND ";
      paramData.add(shnCd.substring(0, 2));
    }

    if (StringUtils.isEmpty(binKbn)) {
      sqlWhere += "BINKBN=null AND ";
    } else {
      sqlWhere += "BINKBN=? AND ";
      paramData.add(binKbn);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // 既に登録のある商品
      msg.add(mu.getDbMessageObj("E20248", new String[] {}));
      return msg;
    }
    return msg;
  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
    createSqlHs(data, map, userInfo);

    // 排他チェック実行
    targetTable = "INATK.HATSTR_SHN";
    targetWhere = " SHNCD=? AND UPDKBN=? ";
    targetParam.add(data.optString("F1"));
    targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

    if (!super.checkExclusion(targetTable, targetWhere, targetParam, "")) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
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
   * 正規定量_商品INSERT処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlHs(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_HTSS")); // 更新情報(正規定量_商品)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String shnCd = data.optString("F1"); // 商品コード
    String binKbn = data.optString("F2"); // 便区分

    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";
    String sqlWhere = "";

    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      prmData.add(shnCd);
    }

    if (StringUtils.isEmpty(binKbn)) {
      sqlWhere += "BINKBN=null AND ";
    } else {
      sqlWhere += "BINKBN=? AND ";
      prmData.add(binKbn);
    }

    sbSQL.append("DELETE FROM INATK.HATSTR_SHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("正規定量_商品");

    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.HATSTR_TEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("正規定量_店別数量");

    sqlWhere = "";
    prmData = new ArrayList<String>();

    // 商品コード入力値チェック
    if (StringUtils.isEmpty(shnCd)) {
      values += "null";
    } else {
      values += "?";
      prmData.add(shnCd);
    }

    // 便区分入力値チェック
    if (StringUtils.isEmpty(binKbn)) {
      values += ", null";
    } else {
      values += ", ?";
      prmData.add(binKbn);
    }

    for (int i = 0; i < dataArrayT.size(); i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        String val = dataT.optString("F2");
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          values += ", ?";
          prmData.add(val);
        }
      }
    }

    // 正規定量_店別数量の登録
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.HATSTR_SHN(");
    sbSQL.append("SHNCD");
    sbSQL.append(",BINKBN");
    sbSQL.append(",TSKBN_MON");
    sbSQL.append(",TSKBN_TUE");
    sbSQL.append(",TSKBN_WED");
    sbSQL.append(",TSKBN_THU");
    sbSQL.append(",TSKBN_FRI");
    sbSQL.append(",TSKBN_SAT");
    sbSQL.append(",TSKBN_SUN");
    sbSQL.append(",UPDKBN");
    sbSQL.append(",SENDFLG");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT) VALUES (");
    sbSQL.append(values);
    sbSQL.append("," + DefineReport.ValUpdkbn.NML.getVal()); // 更新区分：
    sbSQL.append(",0"); // 送信区分：未送信
    sbSQL.append(",'" + userId + "'"); // オペレーター：
    sbSQL.append(",CURRENT_TIMESTAMP"); // 登録日：
    sbSQL.append(",CURRENT_TIMESTAMP)"); // 更新日：

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("正規定量_商品");

    sqlWhere = "";
    prmData = new ArrayList<String>();

    // 商品コード入力値チェック
    if (StringUtils.isEmpty(shnCd)) {
      values = "null";
    } else {
      values = "?";
      prmData.add(shnCd);
    }

    // 便区分入力値チェック
    if (StringUtils.isEmpty(binKbn)) {
      values += ", null";
    } else {
      values += ", ?";
      prmData.add(binKbn);
    }

    for (int i = 0; i < dataArrayT.size(); i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        String val = dataT.optString("F3");
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          values += ", ?";
          prmData.add(val);
        }
      }
    }

    // 商品コード入力値チェック
    if (StringUtils.isEmpty(shnCd) && shnCd.length() >= 2) {
      sqlWhere += "SUBSTR(SHNCD,1,2)=null AND ";
    } else {
      sqlWhere += "SUBSTR(SHNCD,1,2)=? AND ";
      prmData.add(shnCd.substring(0, 2));
    }

    // 便区分入力値チェック
    if (StringUtils.isEmpty(binKbn)) {
      sqlWhere += "BINKBN=null AND ";
    } else {
      sqlWhere += "BINKBN=? AND ";
      prmData.add(binKbn);
    }

    // 正規定量_店別数量の登録
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.HATSTR_TEN(");
    sbSQL.append("SHNCD");
    sbSQL.append(",BINKBN");
    sbSQL.append(",SURYO_MON");
    sbSQL.append(",SURYO_TUE");
    sbSQL.append(",SURYO_WED");
    sbSQL.append(",SURYO_THU");
    sbSQL.append(",SURYO_FRI");
    sbSQL.append(",SURYO_SAT");
    sbSQL.append(",SURYO_SUN");
    sbSQL.append(",TENCD");
    sbSQL.append(",UPDKBN");
    sbSQL.append(",SENDFLG");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT) ");
    sbSQL.append("SELECT ");
    sbSQL.append(values);
    sbSQL.append(",HT.TENCD");
    sbSQL.append("," + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
    sbSQL.append(",0 AS SENDFLG"); // 送信区分：未送信
    sbSQL.append(",'" + userId + "' AS OPERATOR "); // オペレーター：
    sbSQL.append(",CURRENT_TIMESTAMP AS ADDDT "); // 登録日：
    sbSQL.append(",CURRENT_TIMESTAMP AS UPDDT "); // 更新日：
    sbSQL.append("FROM INATK.HATSTR_TEN HT, ");
    sbSQL.append("(SELECT ");
    sbSQL.append("ROW_NUMBER() OVER (ORDER BY SUBSTR(SHNCD,3,7)) AS NUM ");
    sbSQL.append(",SHNCD ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + ") HTS  ");
    sbSQL.append("WHERE ");
    sbSQL.append("HTS.NUM=1 AND ");
    sbSQL.append("HT.SHNCD=HTS.SHNCD");

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("正規定量_店別数量");

    return sbSQL.toString();
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szHsgpcd = getMap().get("HSGPCD"); // 配送グループコード
    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";

    if (StringUtils.isEmpty(szHsgpcd)) {
      sqlWhere += "null";
    } else {
      sqlWhere += "?";
      paramData.add(szHsgpcd);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("HSGPCD "); // F1 : 配送グループ
    sbSQL.append(",HSGPKN "); // F2 : 配送グループ名称
    sbSQL.append(",HSGPAN "); // F3 : 配送グループ名称（カナ）
    sbSQL.append(",OPERATOR "); // F4 : オペレーター
    sbSQL.append(",DATE_FORMAT(ADDDT,'%y/%m/%d') AS ADDDT "); // F5 : 登録日
    sbSQL.append(",DATE_FORMAT(UPDDT,'%y/%m/%d') AS UPDDT "); // F6 : 更新日
    sbSQL.append(",AREAKBN "); // F7 : エリア区分
    sbSQL.append(",DATE_FORMAT(UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F8 : 更新日時
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTHSGP ");
    sbSQL.append("WHERE ");
    sbSQL.append("HSGPCD = " + sqlWhere + " AND ");
    sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append("ORDER BY HSGPCD");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
