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
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTR016Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  String copyTenCd = "23";

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTR016Dao(String JNDIname) {
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
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();

    JSONArray msgList = this.checkDel(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.deleteData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
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
    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 入力データのチェック
    JSONObject data = dataArray.getJSONObject(0);
    String tenCd = data.optString("F1"); // 店コード
    String bmnCd = data.optString("F2"); // 部門コード

    if (!StringUtils.isEmpty(tenCd) && Integer.valueOf(tenCd) > 400) {
      // 店コードは400以下を入力
      msg.add(mu.getDbMessageObj("E20110", new String[] {"店コード"}));
      return msg;
    }

    String sqlSelect = "";

    // 店コード入力値チェック
    if (StringUtils.isEmpty(tenCd)) {
      sqlSelect += ",null AS TENCD_INP ";
    } else {
      sqlSelect += ",? AS TENCD_INP ";
      paramData.add(tenCd);
    }

    // 部門コード入力値チェック
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "SUBSTR(SHNCD,1,2)=null AND ";
    } else {
      sqlWhere += "SUBSTR(SHNCD,1,2)=? AND ";
      paramData.add(bmnCd);
    }

    // 正規定量_店別数量の登録
    sbSQL.append("SELECT COUNT(SHNCD) CNT FROM (");
    sbSQL.append("SELECT SHNCD,BINKBN");
    sbSQL.append(sqlSelect);
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("TENCD = ? AND ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(") HT ");
    sbSQL.append("WHERE ");
    sbSQL.append("NOT EXISTS(");
    sbSQL.append("SELECT ");
    sbSQL.append("1 ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN HTS ");
    sbSQL.append("WHERE ");
    sbSQL.append("HT.SHNCD=HTS.SHNCD AND ");
    sbSQL.append("HT.BINKBN=HTS.BINKBN AND ");
    sbSQL.append("HT.TENCD_INP=HTS.TENCD)");

    paramData.add(copyTenCd);

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.getJSONObject(0).size() > 0) {
      if (Integer.parseInt(dbDatas.getJSONObject(0).optString("CNT")) == 0) {
        // 更新対象のデータなし
        msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
        return msg;
      }
    }
    return msg;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

    // 削除データ検索用コード
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // 格納用変数
    JSONArray msg = new JSONArray();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
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

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 正規定量_店別数量insert処理
    createSqlHt(data, userInfo);

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
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), "データ", String.valueOf(count)));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 正規定量_店別数量論理削除処理
    createDelSqlHt(data, userInfo);

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
   * 正規定量_店別数量、正規定量_商品論理削除処理
   *
   * @param data
   * @param userInfo
   */
  public String createDelSqlHt(JSONObject data, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 入力値
    String tenCd = data.optString("F1");
    String bmnCd = data.optString("F2");

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // 部門コード入力値チェック
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "SUBSTR(SHNCD,1,2)=null AND ";
    } else {
      sqlWhere += "SUBSTR(SHNCD,1,2)=? AND ";
      paramData.add(bmnCd);
    }

    // 店コード入力値チェック
    if (StringUtils.isEmpty(tenCd)) {
      sqlWhere += "TENCD=null AND ";
    } else {
      sqlWhere += "TENCD=? AND ";
      paramData.add(tenCd);
    }

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("SET ");
    sbSQL.append("SENDFLG = 0 ");
    sbSQL.append(",UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("正規定量_店別数量");

    sqlWhere = "";
    paramData = new ArrayList<String>();

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INATK.HATSTR_SHN ");
    sbSQL.append("SET ");
    sbSQL.append("SENDFLG = 0 ");
    sbSQL.append(",UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE SHNCD in");

    sbSQL.append("(select SHNCD from INATK.HATSTR_SHN");
    sbSQL.append(" where int (left (SHNCD, 2)) = ?");
    sbSQL.append(" and UPDKBN <> 1 and SHNCD in (");
    sbSQL.append(" select SHNCD");
    sbSQL.append(" from INATK.HATSTR_TEN");
    sbSQL.append(" group by SHNCD");
    sbSQL.append(" having SUM(case when UPDKBN = 0 then 1 else 0 end) = 0");
    sbSQL.append(")");
    sbSQL.append(")");
    paramData.add(bmnCd);

    /*
     * sbSQL.append("(SELECT SHN.SHNCD FROM INATK.HATSTR_SHN SHN "); sbSQL.append("WHERE ");
     * sbSQL.append("EXISTS( "); sbSQL.append("SELECT "); sbSQL.append("1 "); sbSQL.append("FROM( ");
     * sbSQL.append("SELECT DISTINCT "); sbSQL.append("HT.SHNCD  "); sbSQL.append("FROM ");
     * sbSQL.append("INATK.HATSTR_TEN HT "); sbSQL.append("WHERE ");
     *
     * // 部門コード if (StringUtils.isEmpty(bmnCd)) { sqlWhere = "SUBSTR(HT.SHNCD, 1, 2)=null AND "; }else{
     * sqlWhere = "SUBSTR(HT.SHNCD, 1, 2)=? AND "; paramData.add(bmnCd); }
     *
     * sbSQL.append(sqlWhere); sbSQL.append("NOT EXISTS (  "); sbSQL.append("SELECT DISTINCT ");
     * sbSQL.append("1 "); sbSQL.append("FROM "); sbSQL.append("INATK.HATSTR_TEN HTS  ");
     * sbSQL.append("WHERE "); sbSQL.append("HT.SHNCD = HTS.SHNCD AND ");
     *
     * // 店コード if (StringUtils.isEmpty(tenCd)) { sqlWhere = "HTS.TENCD <> null "; }else{ sqlWhere =
     * "HTS.TENCD <> ? "; paramData.add(tenCd); }
     *
     * sbSQL.append(sqlWhere); sbSQL.append(")) TEN "); sbSQL.append("WHERE ");
     * sbSQL.append("SHN.SHNCD=TEN.SHNCD))");
     */
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("正規定量_商品");

    return sbSQL.toString();
  }

  /**
   * 正規定量_店別数量INSERT処理
   *
   * @param data
   * @param userInfo
   */
  public String createSqlHt(JSONObject data, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    String sqlSelect = "";
    String sqlWhere = "";

    String tenCd = data.optString("F1");
    String bmnCd = data.optString("F2");

    // 部門コード入力値チェック
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "SUBSTR(SHNCD,1,2)=null AND ";
    } else {
      sqlWhere += "SUBSTR(SHNCD,1,2)=? AND ";
      prmData.add(bmnCd);
    }

    // 店コード入力値チェック
    if (StringUtils.isEmpty(tenCd)) {
      sqlWhere += "TENCD=null AND ";
    } else {
      sqlWhere += "TENCD=? AND ";
      prmData.add(tenCd);
    }

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

    // 店コード入力値チェック
    if (StringUtils.isEmpty(tenCd)) {
      sqlSelect += ",null AS TENCD_INP ";
    } else {
      sqlSelect += ",? AS TENCD_INP ";
      prmData.add(tenCd);
    }

    // 部門コード入力値チェック
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "SUBSTR(SHNCD,1,2)=null AND ";
    } else {
      sqlWhere += "SUBSTR(SHNCD,1,2)=? AND ";
      prmData.add(bmnCd);
    }

    // 正規定量_店別数量の登録
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.HATSTR_TEN(");
    sbSQL.append("SHNCD");
    sbSQL.append(",BINKBN");
    sbSQL.append(",TENCD");
    sbSQL.append(",SURYO_MON");
    sbSQL.append(",SURYO_TUE");
    sbSQL.append(",SURYO_WED");
    sbSQL.append(",SURYO_THU");
    sbSQL.append(",SURYO_FRI");
    sbSQL.append(",SURYO_SAT");
    sbSQL.append(",SURYO_SUN");
    sbSQL.append(",UPDKBN");
    sbSQL.append(",SENDFLG");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT) ");
    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD");
    sbSQL.append(",BINKBN");
    sbSQL.append(",TENCD_INP AS TENCD");
    sbSQL.append(",0 AS SURYO_MON");
    sbSQL.append(",0 AS SURYO_TUE");
    sbSQL.append(",0 AS SURYO_WED");
    sbSQL.append(",0 AS SURYO_THU");
    sbSQL.append(",0 AS SURYO_FRI");
    sbSQL.append(",0 AS SURYO_SAT");
    sbSQL.append(",0 AS SURYO_SUN");
    sbSQL.append("," + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
    sbSQL.append(",0 AS SENDFLG"); // 送信区分：未送信
    sbSQL.append(",'" + userId + "' AS OPERATOR "); // オペレーター：
    sbSQL.append(",CURRENT_TIMESTAMP AS ADDDT "); // 登録日：
    sbSQL.append(",CURRENT_TIMESTAMP AS UPDDT "); // 更新日：
    sbSQL.append("FROM(");
    sbSQL.append("SELECT DISTINCT ");
    sbSQL.append(" SHNCD");
    sbSQL.append(",BINKBN");
    sbSQL.append(",TENCD");
    sbSQL.append(sqlSelect);
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("TENCD = ?");
    sbSQL.append(") HT ");
    sbSQL.append("WHERE ");
    sbSQL.append("NOT EXISTS(");
    sbSQL.append("SELECT ");
    sbSQL.append("1 ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN HTS ");
    sbSQL.append("WHERE ");
    sbSQL.append("HT.SHNCD=HTS.SHNCD AND ");
    sbSQL.append("HT.BINKBN=HTS.BINKBN AND ");
    sbSQL.append("HT.TENCD_INP=HTS.TENCD)");

    prmData.add(copyTenCd);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/ " + sbSQL.toString());
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

    String szShncd = getMap().get("SHNCD"); // 商品コード
    String szBumon = getMap().get("BUMON"); // 部門
    String szDaibun = getMap().get("DAIBUN"); // 大分類
    String szChubun = getMap().get("CHUBUN"); // 中分類
    String szTencd = getMap().get("TENCD"); // 店コード
    String szShuno = getMap().get("SHUNO"); // 週No.
    String szSeiki = getMap().get("SEIKI"); // 正規
    String szJisyu = getMap().get("JISYU"); // 次週

    // 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
    // DB検索用パラメータ
    String sqlFrom = "";
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();
    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT * FROM (");
    // 正規が選択されていた場合
    if (szSeiki.equals("1")) {

      // 商品コードが未入力の場合は部門で検索
      if (StringUtils.isEmpty(szShncd)) {

        sqlFrom += "INATK.HATSTR_TEN TK LEFT JOIN INAMS.MSTTEN TEN ON TK.TENCD=TEN.TENCD ";
        sqlFrom += ",INAMS.MSTSHN MST";

        if (StringUtils.isEmpty(szBumon)) {
          sqlWhere += "MST.BMNCD=null AND ";
        } else {
          sqlWhere += "RIGHT('0'||MST.BMNCD,2)=? AND ";
          paramData.add(szBumon);
        }

        if (StringUtils.isEmpty(szDaibun)) {
          sqlWhere += "MST.DAICD=null AND ";
        } else {
          sqlWhere += "RIGHT('0'||MST.BMNCD,2)||RIGHT('0'||MST.DAICD,2)=? AND ";
          paramData.add(szDaibun);
        }

        if (StringUtils.isEmpty(szChubun)) {
          sqlWhere += "";
        } else {
          sqlWhere += "RIGHT('0'||MST.BMNCD,2)||RIGHT('0'||MST.DAICD,2)||RIGHT('0'||MST.CHUCD,2)=? AND ";
          paramData.add(szChubun);
        }

        sqlWhere += "MST.SHNCD=TK.SHNCD ";

      } else {
        sqlFrom += "INATK.HATSTR_TEN TK LEFT JOIN INAMS.MSTSHN MST ON TK.SHNCD=MST.SHNCD LEFT JOIN INAMS.MSTTEN TEN ON TK.TENCD=TEN.TENCD ";
        sqlWhere += "TK.SHNCD=? ";
        paramData.add(szShncd);
      }

      if (StringUtils.isEmpty(szTencd)) {
        sqlWhere += "";
      } else {
        sqlWhere += " AND TENCD=? ";
        paramData.add(szTencd);
      }

      sbSQL.append("SELECT  ");
      sbSQL.append("'0' AS JISEIKBN "); // F1: 次正区分
      sbSQL.append(",null AS SHUNO "); // F2: 週No.
      sbSQL.append(",TK.SHNCD "); // F3: 商品コード
      sbSQL.append(",MST.SHNKN "); // F4: 商品名
      sbSQL.append(",TK.TENCD "); // F5: 店コード
      sbSQL.append(",TEN.TENKN "); // F6: 店舗名
      sbSQL.append(",TK.SURYO_MON "); // F7: 数量＿月
      sbSQL.append(",TK.SURYO_TUE "); // F8: 数量＿火
      sbSQL.append(",TK.SURYO_WED "); // F9: 数量＿水
      sbSQL.append(",TK.SURYO_THU "); // F10: 数量＿木
      sbSQL.append(",TK.SURYO_FRI "); // F11: 数量＿金
      sbSQL.append(",TK.SURYO_SAT "); // F12: 数量＿土
      sbSQL.append(",TK.SURYO_SUN "); // F13: 数量＿日
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      if (szJisyu.equals("1")) {
        sbSQL.append(" UNION ALL ");
      }
    }

    if (szJisyu.equals("1")) {

      sqlFrom = "";
      sqlWhere = "";

      // 商品コードが未入力の場合は部門で検索
      if (StringUtils.isEmpty(szShncd)) {

        sqlFrom += "INATK.HATJTR_TEN TK LEFT JOIN INAMS.MSTTEN TEN ON TK.TENCD=TEN.TENCD ";
        sqlFrom += ",INAMS.MSTSHN MST";

        if (StringUtils.isEmpty(szBumon)) {
          sqlWhere += "MST.BMNCD=null AND ";
        } else {
          sqlWhere += "RIGHT('0'||MST.BMNCD,2)=? AND ";
          paramData.add(szBumon);
        }

        if (StringUtils.isEmpty(szDaibun)) {
          sqlWhere += "MST.DAICD=null AND ";
        } else {
          sqlWhere += "RIGHT('0'||MST.BMNCD,2)||RIGHT('0'||MST.DAICD,2)=? AND ";
          paramData.add(szDaibun);
        }

        if (StringUtils.isEmpty(szChubun)) {
          sqlWhere += "";
        } else {
          sqlWhere += "RIGHT('0'||MST.BMNCD,2)||RIGHT('0'||MST.DAICD,2)||RIGHT('0'||MST.CHUCD,2)=? AND ";
          paramData.add(szChubun);
        }

        sqlWhere += "MST.SHNCD=TK.SHNCD ";

      } else {
        sqlFrom += "INATK.HATJTR_TEN TK LEFT JOIN INAMS.MSTSHN MST ON TK.SHNCD=MST.SHNCD LEFT JOIN INAMS.MSTTEN TEN ON TK.TENCD=TEN.TENCD ";
        sqlWhere += "TK.SHNCD=? ";
        paramData.add(szShncd);
      }

      if (StringUtils.isEmpty(szTencd)) {
        sqlWhere += "";
      } else {
        sqlWhere += " AND TENCD=? ";
        paramData.add(szTencd);
      }

      if (StringUtils.isEmpty(szShuno)) {
        sqlWhere += "";
      } else {
        sqlWhere += " AND SHUNO=? ";
        paramData.add(szShuno);
      }

      sbSQL.append("SELECT  ");
      sbSQL.append("'1' AS JISEIKBN "); // F1: 次正区分
      sbSQL.append(",TK.SHUNO "); // F2: 週No.
      sbSQL.append(",TK.SHNCD "); // F3: 商品コード
      sbSQL.append(",MST.SHNKN "); // F4: 商品名
      sbSQL.append(",TK.TENCD "); // F5: 店コード
      sbSQL.append(",TEN.TENKN "); // F6: 店舗名
      sbSQL.append(",TK.SURYO_MON "); // F7: 数量＿月
      sbSQL.append(",TK.SURYO_TUE "); // F8: 数量＿火
      sbSQL.append(",TK.SURYO_WED "); // F9: 数量＿水
      sbSQL.append(",TK.SURYO_THU "); // F10: 数量＿木
      sbSQL.append(",TK.SURYO_FRI "); // F11: 数量＿金
      sbSQL.append(",TK.SURYO_SAT "); // F12: 数量＿土
      sbSQL.append(",TK.SURYO_SUN "); // F13: 数量＿日
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
    }

    sbSQL.append(")ORDER BY JISEIKBN,SHUNO,SHNCD,TENCD");

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
