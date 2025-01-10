package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
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
public class ReportRP007Dao extends ItemDao {

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
  public ReportRP007Dao(String JNDIname) {
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

  private String createCommand() {

    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    StringBuffer sbSQL = new StringBuffer();

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

    // 共通箇所設定
    createCmnOutput(jad);

  }


  /**
   * 更新処理
   *
   * @param request
   * @param session
   * @param map
   * @param userInfo
   * @return
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
   * 更新処理実行
   *
   * @param map
   * @param userInfo
   * @return
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
    JSONObject option = new JSONObject();
    new JSONArray();

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    if (StringUtils.equals("1", dataArray.getJSONObject(0).getString("F4"))) {
      // SQL発行：ランクマスタ
      this.createSqlRank(dataArray, map, userInfo);

    } else if (StringUtils.equals("2", dataArray.getJSONObject(0).getString("F4"))) {
      // SQL発行：通常率パターンマスタ
      this.createSqlRtptn(dataArray, map, userInfo);

    } else if (StringUtils.equals("3", dataArray.getJSONObject(0).getString("F4"))) {
      // SQL発行：実績率パターンマスタ
      this.createSqlJrtptn(dataArray, map, userInfo);
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      // 更新処理実行
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(super.getMessage())) {
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
      option.put(MsgKey.E.getKey(), super.getMessage());
    }
    return option;
  }

  boolean isTest = false;

  /**
   * ランクマスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlRank(JSONArray dataArray, HashMap<String, String> map, User userInfo) {

    String szBmncd = dataArray.optJSONObject(0).optString("F1"); // 部門
    String szCptotenno = dataArray.optJSONObject(0).optString("F2"); // コピー先店番
    String szCpfromtenno = dataArray.optJSONObject(0).optString("F3"); // コピー元店番
    String szDatakbn = dataArray.optJSONObject(0).optString("F5"); // データ指定区分
    String szRanknost = dataArray.optJSONObject(0).optString("F6"); // ランク№開始
    String szRanknoed = dataArray.optJSONObject(0).optString("F7"); // ランク№終了
    String sqlWhere = "";

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String updateRows = ""; // 更新データ

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    StringBuffer sbSQL;
    new ItemList();
    new JSONArray();

    String header = "substr(TENRANK_ARR,1,( ? -1))";
    prmData.add(szCptotenno);
    String cpfromno = "(substr(TENRANK_ARR, ? , 1))";
    prmData.add(szCpfromtenno);
    String footer = "substr(TENRANK_ARR,( ? +1))";
    prmData.add(szCptotenno);

    prmData.add(szBmncd);
    if (StringUtils.equals("1", szDatakbn)) {
      sqlWhere += " and ? <=RANKNO";
      prmData.add(szRanknost);
      sqlWhere += " and RANKNO <= ? ";
      prmData.add(szRanknoed);
    }

    // 更新データ
    updateRows += " SELECT";
    updateRows += " BMNCD";
    updateRows += ", RANKNO";
    updateRows += ", RANKKN";
    updateRows += ", (" + header + "||" + cpfromno + "||" + footer + ") AS TENRANK_ARR";
    updateRows += ", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN ";
    updateRows += ", 0 AS SENDFLG";
    updateRows += ", '" + userId + "'  AS OPERATOR";
    updateRows += ", CURRENT_TIMESTAMP AS ADDDT";
    updateRows += ", CURRENT_TIMESTAMP AS UPDDT";
    updateRows += " from INATK.TOKRANK";
    updateRows += " where BMNCD= ? ";

    updateRows += sqlWhere;
    updateRows += " and COALESCE(UPDKBN, 0) <> 1";
    updateRows += " ";

    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKRANK (");
    sbSQL.append(" BMNCD"); // F1 : 部門
    sbSQL.append(", RANKNO"); // F2 : ランクNo.
    sbSQL.append(", RANKKN"); // F3 : ランク名称
    sbSQL.append(", TENRANK_ARR"); // F4 : 店ランク配列
    sbSQL.append(", UPDKBN"); // 更新区分：
    sbSQL.append(", SENDFLG"); // 送信フラグ
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(" ) ");
    sbSQL.append("SELECT * FROM (");
    sbSQL.append("  " + updateRows + " ) AS NEW");
    sbSQL.append(" ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" BMNCD = NEW.BMNCD ");
    sbSQL.append(" , RANKNO = NEW.RANKNO ");
    sbSQL.append(" , RANKKN = NEW.RANKKN ");
    sbSQL.append(" , TENRANK_ARR = NEW.TENRANK_ARR ");
    sbSQL.append(" , UPDKBN = NEW.UPDKBN ");
    sbSQL.append(" , SENDFLG = NEW.SENDFLG ");
    sbSQL.append(" , OPERATOR = NEW.OPERATOR ");
    sbSQL.append(" , UPDDT = NEW.UPDDT ");
    

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("ランクパターンマスタ");

    return sbSQL.toString();

  }

  /**
   * 通常率パターンマスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlRtptn(JSONArray dataArray, HashMap<String, String> map, User userInfo) {

    String szBmncd = dataArray.optJSONObject(0).optString("F1"); // 部門
    String szCptotenno = dataArray.optJSONObject(0).optString("F2"); // コピー先店番
    String szCpfromtenno = dataArray.optJSONObject(0).optString("F3"); // コピー元店番
    String szDatakbn = dataArray.optJSONObject(0).optString("F5"); // データ指定区分
    String szRtptnnost = dataArray.optJSONObject(0).optString("F8"); // 通常率パターン№開始
    String szRtptnnoed = dataArray.optJSONObject(0).optString("F9"); // 通常率パターン№終了
    String sqlWhere = "";

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String updateRows = ""; // 更新データ
    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    StringBuffer sbSQL;
    new ItemList();
    new JSONArray();

    String header = "substr(TENRT_ARR,1,(( ? -1) * 5))";
    prmData.add(szCptotenno);
    String cpfromno = "(substr(TENRT_ARR,(( ? -1) * 5 + 1), 5))";
    prmData.add(szCpfromtenno);
    String footer = "substr(TENRT_ARR,( ? * 5 +1))";
    prmData.add(szCptotenno);

    prmData.add(szBmncd);

    if (StringUtils.equals("1", szDatakbn)) {
      sqlWhere += " and ? <=RTPTNNO";
      prmData.add(szRtptnnost);
      sqlWhere += " and RTPTNNO <= ? ";
      prmData.add(szRtptnnoed);
    }

    updateRows += " select";
    updateRows += " BMNCD";
    updateRows += ", RTPTNNO";
    updateRows += ", RTPTNKN";
    updateRows += ", (" + header + "||" + cpfromno + "||" + footer + ") as TENRANK_ARR";
    updateRows += ", " + DefineReport.ValUpdkbn.NML.getVal() + "";
    updateRows += ", 0";
    updateRows += ", '" + userId + "' ";
    updateRows += ", ADDDT";
    updateRows += ", CURRENT_TIMESTAMP";
    updateRows += " from INATK.TOKRTPTN";
    updateRows += " where BMNCD= ? ";
    updateRows += sqlWhere;
    updateRows += " and COALESCE(UPDKBN, 0) <> 1";
    updateRows += " ";

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKRTPTN (");
    sbSQL.append(" BMNCD"); // F1 : 部門
    sbSQL.append(", RTPTNNO"); // F2 : 率パターンNo.
    sbSQL.append(", RTPTNKN"); // F3 : 率パターン名称
    sbSQL.append(", TENRT_ARR"); // F4 : 店分配率配列
    sbSQL.append(", UPDKBN"); // 更新区分：
    sbSQL.append(", SENDFLG"); // 送信フラグ
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(") ");
    sbSQL.append(" " + updateRows + " ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("通常率パターンマスタ");

    return sbSQL.toString();
  }

  /**
   * 実績率パターンマスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlJrtptn(JSONArray dataArray, HashMap<String, String> map, User userInfo) {

    // String szBmncd = map.get("BMNCD"); // 部門
    // String szCptotenno = map.get("CPTOTENNO"); // コピー先店番
    // String szCpfromtenno = map.get("CPFROMTENNO"); // コピー元店番
    // String szDatakbn = map.get("DATAKBN"); // データ指定区分
    // String szJrtptnnost = map.get("JRTPTNNOST"); // 実績率パターン№開始
    // String szJrtptnnoed = map.get("JRTPTNNOED"); // 実績率パターン№終了
    String szBmncd = dataArray.optJSONObject(0).optString("F1"); // 部門
    String szCptotenno = dataArray.optJSONObject(0).optString("F2"); // コピー先店番
    String szCpfromtenno = dataArray.optJSONObject(0).optString("F3"); // コピー元店番
    String szDatakbn = dataArray.optJSONObject(0).optString("F5"); // データ指定区分
    String szJrtptnnost = dataArray.optJSONObject(0).optString("F10"); // 実績率パターン№開始
    String szJrtptnnoed = dataArray.optJSONObject(0).optString("F11"); // 実績率パターン№終了
    String sqlWhere = "";

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String updateRows = ""; // 更新データ

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    StringBuffer sbSQL;
    new ItemList();
    new JSONArray();



    String header_uri = "substr(TENURI_ARR,1,( ? - 1) * 9)";
    prmData.add(szCptotenno);
    String cpfromno_uri = "(substr(TENURI_ARR,(( ? - 1) * 9 + 1), 9))";
    prmData.add(szCpfromtenno);
    String footer_uri = "substr(TENURI_ARR,( ? * 9 + 1))";
    prmData.add(szCptotenno);

    String header_ten = "substr(TENTEN_ARR,1,( ? - 1) * 9)";
    prmData.add(szCptotenno);
    String cpfromno_ten = "(substr(TENTEN_ARR,(( ? - 1) * 9 + 1), 9))";
    prmData.add(szCpfromtenno);
    String footer_ten = "substr(TENTEN_ARR,( ? * 9 + 1))";
    prmData.add(szCptotenno);

    prmData.add(szBmncd);

    if (StringUtils.equals("1", szDatakbn)) {
      String jrtptnno = "(right('000' || BMNCD, 3) || right('0' || WWMMFLG, 1) || right('0000' || YYMM, 4) || right('00' || DAICD, 2) || right('00' || CHUCD, 2))";
      sqlWhere += " and ? <= " + jrtptnno;
      prmData.add(szJrtptnnost);
      sqlWhere += " and " + jrtptnno + " <= ? ";
      prmData.add(szJrtptnnoed);
    }

    updateRows += " select";
    updateRows += " BMNCD";
    updateRows += ", WWMMFLG";
    updateRows += ", YYMM";
    updateRows += ", DAICD";
    updateRows += ", CHUCD";
    updateRows += ", (" + header_uri + "||" + cpfromno_uri + "||" + footer_uri + ") as TENURI_ARR";
    updateRows += ", (" + header_ten + "||" + cpfromno_ten + "||" + footer_ten + ") as TENTEN_ARR";
    updateRows += ", '" + userId + "' ";
    updateRows += ", ADDDT";
    updateRows += ", CURRENT_TIMESTAMP";
    updateRows += " from INATK.TOKJRTPTN";
    updateRows += " where BMNCD= ? ";
    updateRows += sqlWhere;
    updateRows += " ";

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKJRTPTN (");
    sbSQL.append(" BMNCD"); // F1 : 部門
    sbSQL.append(", WWMMFLG"); // F2 : 週月フラグ
    sbSQL.append(", YYMM"); // F3 : 年月(週No.)
    sbSQL.append(", DAICD"); // F4 : 大分類
    sbSQL.append(", CHUCD"); // F5 : 中分類
    sbSQL.append(", TENURI_ARR"); // F6 : 売上配列
    sbSQL.append(", TENTEN_ARR"); // F7 : 点数配列
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(") ");
    sbSQL.append(" " + updateRows + "");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("実績率パターンマスタ");

    return sbSQL.toString();
  }


  /**
   * チェック処理
   *
   * @param map
   * @return
   */
  public JSONArray check(HashMap<String, String> map) {
    new ItemList();
    map.get("SENDBTNID");
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    new ArrayList<String>();
    JSONArray msg = new JSONArray();
    new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }
    dataArray.getJSONObject(0);

    return msg;
  }
}
