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
public class ReportJU033Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU033Dao(String JNDIname) {
    super(JNDIname);
  }

  /**
   * 催し商品件数
   */
  int shnCnt = 0;

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
   * 他画面からの呼び出し検索実行
   *
   * @return
   */
  public String createCommandSub(HashMap<String, String> map, User userInfo) {

    // ユーザー情報を設定
    super.setUserInfo(userInfo);

    // 検索条件などの情報を設定
    super.setMap(map);

    // 検索コマンド生成
    String command = createCommandGrd();

    // 出力用検索条件生成
    outputQueryList();

    // 検索実行
    return command;
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

    // 紐付く商品件数によって遷移先画面を変更
    option.put("shnCnt", shnCnt);

    return option;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szMoyscd = getMap().get("MOYSCD"); // 催しコード
    String szKanrino = getMap().get("KANRINO"); // 管理番号

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";
    String sqlWhere2 = "";

    String szMoyskbn = "";
    String szHbstdt = "";
    String szMoysrban = "";

    if (!StringUtils.isEmpty(szMoyscd)) {
      szMoyskbn = szMoyscd.substring(0, 1);
      szHbstdt = szMoyscd.substring(1, 7);
      szMoysrban = szMoyscd.substring(7, 10);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("SELECT ");
    sbSQL.append("T1.MOYSKBN||'-'||T1.MOYSSTDT||'-'||RIGHT('000'||T1.MOYSRBAN,3) ");
    sbSQL.append(",T1.MOYKN ");
    sbSQL.append(
        ",TO_CHAR(TO_DATE(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNSTDT, 'YYYYMMDD')))||'～'||TO_CHAR(TO_DATE(T1.NNEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNEDDT, 'YYYYMMDD')))");
    sbSQL.append(",TO_CHAR(TO_DATE(T2.QASMDT,'YYYYMMDD'),'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.QASMDT, 'YYYYMMDD')))");
    sbSQL.append(",T3.SHNCD ");
    sbSQL.append(",T4.SHNKN ");
    sbSQL.append(",T3.SHNKBN ");
    sbSQL.append(",T3.TSEIKBN ");
    sbSQL.append(",T3.JUKBN ");
    sbSQL.append(",T3.BDENKBN ");
    sbSQL.append(",T3.WAPPNKBN ");
    sbSQL.append(",T3.IRISU ");
    sbSQL.append(",T3.GENKAAM ");
    sbSQL.append(",T3.BAIKAAM ");
    sbSQL.append(",CASE WHEN T4.ZEIRT IS NULL THEN T3.BAIKAAM  ");
    sbSQL.append("ELSE CEILING(DOUBLE(T3.BAIKAAM) / NULLIF(1 + DECIMAL (T4.ZEIRT) / 100, 0)) END HONBAIKA ");
    sbSQL.append(",TO_CHAR(TO_DATE(T3.HTDT,'YYYYMMDD'),'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T3.HTDT, 'YYYYMMDD')))");
    sbSQL.append(",TO_CHAR(TO_DATE(T3.NNDT,'YYYYMMDD'),'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T3.NNDT, 'YYYYMMDD')))");
    sbSQL.append(",T3.OPERATOR "); // F18 : オペレーター
    sbSQL.append(",TO_CHAR(T3.ADDDT,'YY/MM/DD') AS ADDDT "); // F19 : 登録日
    sbSQL.append(",TO_CHAR(T3.UPDDT,'YY/MM/DD') AS UPDDT "); // F20 : 更新日
    sbSQL.append(",TO_CHAR(T3.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT "); // F21 : 更新日時
    sbSQL.append(",T3.JUTENKAIKBN ");
    sbSQL.append(",T3.RANKNO_ADD ");
    sbSQL.append(",T3.HTSU ");
    sbSQL.append(",T3.SURYOPTN ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD T1, ");
    sbSQL.append("INATK.TOKQJU_MOY T2, ");
    sbSQL.append("INATK.TOKQJU_SHN T3 LEFT JOIN ");
    sbSQL.append("(SELECT ");
    sbSQL.append("T1.SHNCD ");
    sbSQL.append(",T1.SHNKN ");
    sbSQL.append(",T2.ZEIRT ");
    sbSQL.append("FROM ");
    sbSQL.append("(SELECT ");
    sbSQL.append("T1.SHNCD ");
    sbSQL.append(",T1.SHNKN ");
    sbSQL.append(",CASE WHEN T1.ZEIKBN <> '3' THEN ");
    sbSQL.append("CASE WHEN T1.ZEIKBN <> '0' THEN NULL ");
    sbSQL.append("WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) <= ? THEN T1.ZEIRTKBN ");
    sbSQL.append("WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) > ? THEN T1.ZEIRTKBN_OLD END ");
    sbSQL.append("ELSE CASE WHEN T2.ZEIKBN <> '0' THEN NULL ");
    sbSQL.append("WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) <= ? THEN T2.ZEIRTKBN ");
    sbSQL.append("WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) > ? THEN T2.ZEIRTKBN_OLD END END ZEIRTKBN ");
    sbSQL.append("FROM INAMS.MSTSHN T1 LEFT JOIN INAMS.MSTBMN T2 ON SUBSTR(T1.SHNCD,1,2) = T2.BMNCD ");
    paramData.add(szHbstdt);
    paramData.add(szHbstdt);
    paramData.add(szHbstdt);
    paramData.add(szHbstdt);
    sbSQL.append(") T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN = T2.ZEIRTKBN) AS T4 ON T3.SHNCD=T4.SHNCD ");
    sbSQL.append("WHERE ");

    // 催し区分
    if (StringUtils.isEmpty(szMoyskbn)) {
      sqlWhere += "T1.MOYSKBN=null AND ";
    } else {
      sqlWhere += "T1.MOYSKBN=? AND ";
      paramData.add(szMoyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(szHbstdt)) {
      sqlWhere += "T1.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "T1.MOYSSTDT=? AND ";
      paramData.add(szHbstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(szMoysrban)) {
      sqlWhere += "T1.MOYSRBAN=null AND ";
    } else {
      sqlWhere += "T1.MOYSRBAN=? AND ";
      paramData.add(szMoysrban);
    }

    sbSQL.append(sqlWhere);
    sbSQL.append("T1.MOYSKBN=T2.MOYSKBN AND ");
    sbSQL.append("T1.MOYSSTDT=T2.MOYSSTDT AND ");
    sbSQL.append("T1.MOYSRBAN=T2.MOYSRBAN AND ");
    sbSQL.append("T2.MOYSKBN=T3.MOYSKBN AND ");
    sbSQL.append("T2.MOYSSTDT=T3.MOYSSTDT AND ");
    sbSQL.append("T2.MOYSRBAN=T3.MOYSRBAN AND ");

    // 管理番号
    if (StringUtils.isEmpty(szKanrino)) {
      sqlWhere2 += "T3.KANRINO=null AND ";
    } else {
      sqlWhere2 += "T3.KANRINO=? AND ";
      paramData.add(szKanrino);
    }
    sbSQL.append(sqlWhere2);
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  private String createCommandGrd() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String callPage = getMap().get("callpage");

    ItemList iL = new ItemList();
    JSONArray dbDatasAddTen = new JSONArray();
    JSONArray dbDatasSuryo = new JSONArray();

    // SQL構文
    StringBuffer sbSQL = new StringBuffer();
    String sqlcommand = "";

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    HashMap<String, String> arrMap = new HashMap<String, String>();
    HashMap<String, String> mergeArrMap = new HashMap<String, String>();

    String moyskbn = ""; // 催し区分
    String moysstdt = ""; // 催し開始日
    String moysrban = ""; // 催し連番

    if (!StringUtils.isEmpty(getMap().get("MOYSCD")) && getMap().get("MOYSCD").length() >= 8) {
      moyskbn = getMap().get("MOYSCD").substring(0, 1);
      moysstdt = getMap().get("MOYSCD").substring(1, 7);
      moysrban = getMap().get("MOYSCD").substring(7);
    }

    String kanrino = getMap().get("KANRINO"); // 管理番号

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT=null AND ";
    } else {
      sqlWhere += "MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN=null AND ";
    } else {
      sqlWhere += "MOYSRBAN=? AND ";
      paramData.add(moysrban);
    }

    // 管理番号
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "KANRINO=null AND ";
    } else {
      sqlWhere += "KANRINO=? AND ";
      paramData.add(kanrino);
    }

    sbSQL = new StringBuffer();
    if (callPage.equals(DefineReport.ID_PAGE_JU013)) {
      sbSQL.append("SELECT TENHTSU_ARR, SHNCD FROM INATK.TOKJU_SHN WHERE " + sqlWhere + " UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    } else {
      sbSQL.append("SELECT TENHTSU_ARR, SHNCD FROM INATK.TOKQJU_SHN WHERE " + sqlWhere + " UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    }

    dbDatasSuryo = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // データが存在する場合、配列の展開を実施
    if (dbDatasSuryo.size() != 0) {

      String arr = dbDatasSuryo.getJSONObject(0).optString("TENHTSU_ARR");

      int st = 0;
      int ed = 0;
      int digit = 5;

      // 配列をdigitで指定された桁ずつで区切りkey、valueを保持(key=店舗、value=取得結果)
      for (int i = 0; i < (arr.length() / digit); i++) {

        ed += digit;

        // valueがスペースのものは登録しない
        if (!StringUtils.isEmpty(arr.substring(st, ed).trim())) {
          arrMap.put(String.valueOf(i + 1), arr.substring(st, ed));
        }

        st += digit;
      }

      paramData = new ArrayList<String>();
      sbSQL = new StringBuffer();

      if (callPage.equals(DefineReport.ID_PAGE_JU013)) {
        sbSQL.append("select TEN.TENCD AS TENCD from INAMS.MSTTEN TEN where TEN.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and TEN.TENCD < 400 and TEN.MISEUNYOKBN <> 9 order by TEN.TENCD");
      } else {
        sbSQL.append("select TEN.TENCD AS TENCD from INAMS.MSTTEN TEN where TEN.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and TEN.TENCD < 800 and TEN.MISEUNYOKBN <> 9 order by TEN.TENCD");
      }
      dbDatasAddTen = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatasAddTen.size() != 0) {

        for (int i = 0; i < dbDatasAddTen.size(); i++) {

          String tencd = dbDatasAddTen.getJSONObject(i).getString("TENCD");
          if (!arrMap.containsKey(tencd)) {
            mergeArrMap.put(tencd, "");
          } else {
            mergeArrMap.put(tencd, arrMap.get(tencd));
          }
        }
      }
    }

    sqlcommand = "SELECT T1.TENCD,CASE WHEN T1.SURYO IS NULL THEN NULL ELSE T2.TENKN END AS TENKN,T1.SURYO FROM (values";

    int cnt = 0;

    for (int i = 0; i < 400; i++) {

      String tencd = String.valueOf(i + 1);

      if (mergeArrMap.containsKey(tencd)) {

        String value = StringUtils.isEmpty(mergeArrMap.get(tencd)) ? "null" : mergeArrMap.get(tencd);

        sqlcommand += " ROW(" + tencd + "," + value;
        cnt++;

        if (mergeArrMap.size() != cnt) {
          sqlcommand += "),";
        }
      }

      if (mergeArrMap.size() == cnt) {
        break;
      }
    }

    sqlcommand += ")) AS T1(TENCD,SURYO) LEFT JOIN INAMS.MSTTEN T2 ON T1.TENCD = T2.TENCD ORDER BY T1.TENCD";

    // オプション情報（タイトル）設定
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sqlcommand);
    return sqlcommand;
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

  boolean isTest = true;

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  // 対象店関連
  String tenht = "1"; // 1:同一発注数量 2:ランク別発注数量 3:店別発注数量
  String kanrino = "";
  HashMap<String, String> tenSuryo = new HashMap<String, String>();

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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(店別アンケート付き送り付け_催し)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_TENHT")); // 更新情報(数量パターン)

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 店舗アンケート付き送り付けINSERT/UPDATE処理
    createSqlQju(data, dataArrayG, userInfo);

    // 排他チェック実行
    if (!StringUtils.isEmpty(data.optString("F1"))) {

      String moyskbn = "";
      String moysstdt = "";
      String moysrban = "";

      if (data.optString("F1").length() >= 8) {
        moyskbn = data.optString("F1").substring(0, 1);
        moysstdt = data.optString("F1").substring(1, 7);
        moysrban = data.optString("F1").substring(7);
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        targetWhere = "MOYSKBN=null AND ";
      } else {
        targetWhere = "MOYSKBN=? AND ";
        targetParam.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        targetWhere += "MOYSSTDT=null AND ";
      } else {
        targetWhere += "MOYSSTDT=? AND ";
        targetParam.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        targetWhere += "MOYSRBAN=null AND ";
      } else {
        targetWhere += "MOYSRBAN=? AND ";
        targetParam.add(moysrban);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        targetWhere += "KANRINO=null AND ";
      } else {
        targetWhere += "KANRINO=? AND ";
        targetParam.add(kanrino);
      }

      targetWhere += " UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ";

      targetTable = "INATK.TOKQJU_SHN";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F21"))) {
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
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(店別アンケート付き送り付け_催し)

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);
    kanrino = map.get("KANRINO"); // 管理番号

    // BM催し送信情報DELETE(論理削除)処理
    createDelSqlQju(data, userInfo);

    // 排他チェック実行
    if (!StringUtils.isEmpty(data.optString("F1"))) {

      String moyskbn = "";
      String moysstdt = "";
      String moysrban = "";

      if (data.optString("F1").length() >= 8) {
        moyskbn = data.optString("F1").substring(0, 1);
        moysstdt = data.optString("F1").substring(1, 7);
        moysrban = data.optString("F1").substring(7);
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        targetWhere = "MOYSKBN=null AND ";
      } else {
        targetWhere = "MOYSKBN=? AND ";
        targetParam.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        targetWhere += "MOYSSTDT=null AND ";
      } else {
        targetWhere += "MOYSSTDT=? AND ";
        targetParam.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        targetWhere += "MOYSRBAN=null AND ";
      } else {
        targetWhere += "MOYSRBAN=? AND ";
        targetParam.add(moysrban);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        targetWhere += "KANRINO=null ";
      } else {
        targetWhere += "KANRINO=? ";
        targetParam.add(kanrino);
      }

      targetTable = "INATK.TOKQJU_SHN";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F21"))) {
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
   * 店舗アンケート付き送り付けINSERT/UPDATE処理
   *
   * @param data
   * @param dataArrayG
   * @param userInfo
   */
  public String createSqlQju(JSONObject data, JSONArray dataArrayG, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    StringBuffer sbSQL = new StringBuffer();
    Object[] valueData = new Object[] {};
    String values = "";
    ArrayList<String> prmData = new ArrayList<String>();

    // 入力値格納用変数
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String tenHtSu_Arr = "";
    String moyCd_Arr = "";
    String kanriNo_Arr = "";
    String dblCnt_Arr = "";
    int lastTenCd = 1;

    new HashMap<String, String>();
    new HashMap<String, String>();
    new HashMap<String, String>();
    HashMap<String, String> tenSuryoMap = new HashMap<String, String>();

    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    for (int i = 0; i < dataArrayG.size(); i++) {
      JSONObject dataG = dataArrayG.getJSONObject(i);

      int tencd = dataG.optInt("F1");

      if ((tencd - lastTenCd) != 0) {
        tenHtSu_Arr += String.format("%" + (tencd - lastTenCd) * 5 + "s", "");
      }

      String suryo = "";
      if (dataG.size() > 1) {
        suryo = dataG.getString("F2");
      }

      if (!StringUtils.isEmpty(suryo)) {
        tenHtSu_Arr += String.format("%05d", Integer.valueOf(suryo));
      } else {
        tenHtSu_Arr += String.format("%5s", "");
        suryo = "";
      }

      if (tenSuryoMap.containsKey(String.valueOf(tencd))) {
        tenSuryoMap.replace(String.valueOf(tencd), suryo);
      } else {
        tenSuryoMap.put(String.valueOf(tencd), suryo);
      }

      lastTenCd = (tencd + 1);
    }

    tenHtSu_Arr = new ReportJU012Dao(JNDIname).spaceArr(tenHtSu_Arr, 5);

    int maxField = 26; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        values += String.valueOf(0 + 1);
        // 催しコードを追加
        values += ", ?, ?, ?, ?";
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(kanrino);
      }

      if (!ArrayUtils.contains(new String[] {"F1", "F2", "F3", "F4", "F6", "F15", "F18", "F19", "F20", "F21"}, key)) {
        String val = data.optString(key);
        if (key.equals("F26")) {
          values += ", ?";
          prmData.add(tenHtSu_Arr);
        } else if (key.equals("F10") && StringUtils.isEmpty(val)) {
          values += ", ?";
          prmData.add("0");
        } else if (StringUtils.isEmpty(val)) {
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

    sbSQL = new StringBuffer();
    sbSQL.append("MERGE INTO INATK.TOKQJU_SHN AS T USING (SELECT ");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",KANRINO"); // 管理番号
    sbSQL.append(",SHNCD"); // 商品コード
    sbSQL.append(",SHNKBN"); // 商品区分
    sbSQL.append(",TSEIKBN"); // 訂正区分
    sbSQL.append(",JUKBN"); // 事前区分
    sbSQL.append(",BDENKBN"); // 別伝区分
    sbSQL.append(",WAPPNKBN"); // ワッペン区分
    sbSQL.append(",IRISU"); // 入数
    sbSQL.append(",GENKAAM"); // 原価
    sbSQL.append(",BAIKAAM"); // 売価
    sbSQL.append(",HTDT"); // 発注日
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",JUTENKAIKBN"); // 展開方法
    sbSQL.append(",RANKNO_ADD"); // 対象ランク
    sbSQL.append(",HTSU"); // 発注数
    sbSQL.append(",SURYOPTN"); // 数量パターン
    sbSQL.append(",TENHTSU_ARR"); // 店発注数配列
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
    sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " AS SENDFLG"); // 送信区分：
    sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
    sbSQL.append(", current timestamp AS ADDDT "); // 登録日：
    sbSQL.append(", current timestamp AS UPDDT "); // 更新日：
    sbSQL.append(" FROM (values " + StringUtils.join(valueData, ",") + ") as T1(NUM");
    sbSQL.append(",MOYSKBN"); // F1 : 催し区分
    sbSQL.append(",MOYSSTDT"); // F1 : 催し開始日
    sbSQL.append(",MOYSRBAN"); // F1 : 催し連番
    sbSQL.append(",KANRINO"); // 特殊 : 管理番号
    sbSQL.append(",SHNCD"); // F2 : 商品コード
    sbSQL.append(",SHNKBN"); // F3 : 商品区分
    sbSQL.append(",TSEIKBN"); // F4 : 訂正区分
    sbSQL.append(",JUKBN"); // F5 : 事前区分
    sbSQL.append(",BDENKBN"); // F6 : 別伝区分
    sbSQL.append(",WAPPNKBN"); // F7 : ワッペン区分
    sbSQL.append(",IRISU"); // F8 : 入数
    sbSQL.append(",GENKAAM"); // F9 : 原価
    sbSQL.append(",BAIKAAM"); // F10 : 売価
    sbSQL.append(",HTDT"); // F11 : 発注日
    sbSQL.append(",NNDT"); // F12 : 納入日
    sbSQL.append(",JUTENKAIKBN"); // 特殊 : 展開方法
    sbSQL.append(",RANKNO_ADD"); // F14,16 : 対象ランク
    sbSQL.append(",HTSU"); // F15 : 発注数
    sbSQL.append(",SURYOPTN"); // F17 : 数量パターン
    sbSQL.append(",TENHTSU_ARR"); // 特殊 : 店発注数配列
    sbSQL.append(")) as RE on (");
    sbSQL.append("T.MOYSKBN=RE.MOYSKBN AND ");
    sbSQL.append("T.MOYSSTDT=RE.MOYSSTDT AND ");
    sbSQL.append("T.MOYSRBAN=RE.MOYSRBAN AND ");
    sbSQL.append("T.KANRINO=RE.KANRINO ");
    sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
    sbSQL.append("SHNCD=RE.SHNCD");
    sbSQL.append(",SHNKBN=RE.SHNKBN");
    sbSQL.append(",TSEIKBN=RE.TSEIKBN");
    sbSQL.append(",JUKBN=RE.JUKBN");
    sbSQL.append(",BDENKBN=RE.BDENKBN");
    sbSQL.append(",WAPPNKBN=RE.WAPPNKBN");
    sbSQL.append(",IRISU=RE.IRISU");
    sbSQL.append(",GENKAAM=RE.GENKAAM");
    sbSQL.append(",BAIKAAM=RE.BAIKAAM");
    sbSQL.append(",HTDT=RE.HTDT");
    sbSQL.append(",NNDT=RE.NNDT");
    sbSQL.append(",JUTENKAIKBN=RE.JUTENKAIKBN");
    sbSQL.append(",RANKNO_ADD=RE.RANKNO_ADD");
    sbSQL.append(",HTSU=RE.HTSU");
    sbSQL.append(",SURYOPTN=RE.SURYOPTN");
    sbSQL.append(",TENHTSU_ARR=RE.TENHTSU_ARR");
    sbSQL.append(",UPDKBN=RE.UPDKBN ");
    sbSQL.append(",SENDFLG=RE.SENDFLG ");
    sbSQL.append(",OPERATOR=RE.OPERATOR ");
    sbSQL.append(",UPDDT=RE.UPDDT");
    sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",KANRINO"); // 管理番号
    sbSQL.append(",SHNCD"); // 商品コード
    sbSQL.append(",SHNKBN"); // 商品区分
    sbSQL.append(",TSEIKBN"); // 訂正区分
    sbSQL.append(",JUKBN"); // 事前区分
    sbSQL.append(",BDENKBN"); // 別伝区分
    sbSQL.append(",WAPPNKBN"); // ワッペン区分
    sbSQL.append(",IRISU"); // 入数
    sbSQL.append(",GENKAAM"); // 原価
    sbSQL.append(",BAIKAAM"); // 売価
    sbSQL.append(",HTDT"); // 発注日
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",JUTENKAIKBN"); // 展開方法
    sbSQL.append(",RANKNO_ADD"); // 対象ランク
    sbSQL.append(",HTSU"); // 発注数
    sbSQL.append(",SURYOPTN"); // 数量パターン
    sbSQL.append(",TENHTSU_ARR"); // 店発注数配列
    sbSQL.append(",UPDKBN");
    sbSQL.append(",SENDFLG");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT");
    sbSQL.append(") VALUES (");
    sbSQL.append(" RE.MOYSKBN"); // 催し区分
    sbSQL.append(",RE.MOYSSTDT"); // 催し開始日
    sbSQL.append(",RE.MOYSRBAN"); // 催し連番
    sbSQL.append(",RE.KANRINO"); // 管理番号
    sbSQL.append(",RE.SHNCD"); // 商品コード
    sbSQL.append(",RE.SHNKBN"); // 商品区分
    sbSQL.append(",RE.TSEIKBN"); // 訂正区分
    sbSQL.append(",RE.JUKBN"); // 事前区分
    sbSQL.append(",RE.BDENKBN"); // 別伝区分
    sbSQL.append(",RE.WAPPNKBN"); // ワッペン区分
    sbSQL.append(",RE.IRISU"); // 入数
    sbSQL.append(",RE.GENKAAM"); // 原価
    sbSQL.append(",RE.BAIKAAM"); // 売価
    sbSQL.append(",RE.HTDT"); // 発注日
    sbSQL.append(",RE.NNDT"); // 納入日
    sbSQL.append(",RE.JUTENKAIKBN"); // 展開方法
    sbSQL.append(",RE.RANKNO_ADD"); // 対象ランク
    sbSQL.append(",RE.HTSU"); // 発注数
    sbSQL.append(",RE.SURYOPTN"); // 数量パターン
    sbSQL.append(",RE.TENHTSU_ARR"); // 店発注数配列
    sbSQL.append(",RE.UPDKBN");
    sbSQL.append(",RE.SENDFLG");
    sbSQL.append(",RE.OPERATOR");
    sbSQL.append(",RE.ADDDT");
    sbSQL.append(",RE.UPDDT");
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("店舗アンケート付き送り付け_商品");

    // 事前打出し_商品納入日用配列作成
    String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));
    HashMap<String, String> moyCdMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F5"), data.optString("F17"), "1");
    HashMap<String, String> kanriNoMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F5"), data.optString("F17"), "2");
    HashMap<String, String> cblCntMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F5"), data.optString("F17"), "3");

    for (HashMap.Entry<String, String> getKeyVal : tenSuryoMap.entrySet()) {
      String key = getKeyVal.getKey();
      String val = getKeyVal.getValue();

      String getMoysCd = "";
      String getKanriNo = "";
      int flg = 0;

      // 数量ゼロは対象外
      if (StringUtils.isEmpty(val)) {
        if (moyCdMap.containsKey(key)) {
          getMoysCd = moyCdMap.get(key);
        }

        if (kanriNoMap.containsKey(key)) {
          getKanriNo = kanriNoMap.get(key);
        }

        // 同一のものだった場合
        if (moyscd.equals(getMoysCd) && kanrino.equals(getKanriNo)) {

          if (moyCdMap.containsKey(key)) {
            moyCdMap.remove(key);
          }

          if (kanriNoMap.containsKey(key)) {
            kanriNoMap.remove(key);
          }

          if (cblCntMap.containsKey(key)) {

            flg = Integer.valueOf(cblCntMap.get(key));
            if (flg > 0) {
              cblCntMap.replace(key, String.valueOf(flg - 1));
            } else {
              cblCntMap.remove(key);
            }
          }
        }
      } else {
        if (moyCdMap.containsKey(key)) {
          getMoysCd = moyCdMap.get(key);
          moyCdMap.replace(key, moyscd);
        } else {
          moyCdMap.put(key, moyscd);
        }

        if (kanriNoMap.containsKey(key)) {
          getKanriNo = kanriNoMap.get(key);
          kanriNoMap.replace(key, String.valueOf(Integer.valueOf(kanrino)));
        } else {
          kanriNoMap.put(key, String.valueOf(Integer.valueOf(kanrino)));
        }

        // 同一のものじゃなかった場合
        if (!moyscd.equals(getMoysCd) || !kanrino.equals(getKanriNo)) {
          if (tenSuryo.containsKey(key) && !StringUtils.isEmpty(tenSuryo.get(key))) {
            if (cblCntMap.containsKey(key)) {
              flg = Integer.valueOf(cblCntMap.get(key)) + 1;
              cblCntMap.replace(key, String.valueOf(flg));
            } else {
              cblCntMap.put(key, "1");
            }
          }
        }
      }
    }

    moyCd_Arr = new ReportJU012Dao(JNDIname).createArr(moyCdMap, "1");
    kanriNo_Arr = new ReportJU012Dao(JNDIname).createArr(kanriNoMap, "2");
    dblCnt_Arr = new ReportJU012Dao(JNDIname).createArr(cblCntMap, "3");

    // 事前打出し_商品納入日更新
    values = "1,";
    prmData = new ArrayList<String>();
    valueData = new Object[] {};

    // 商品コード
    if (StringUtils.isEmpty(data.optString("F5"))) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(data.optString("F5"));
    }

    // 納入日
    if (StringUtils.isEmpty(data.optString("F17"))) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(data.optString("F17"));
    }

    // 催し配列
    if (StringUtils.isEmpty(moyCd_Arr)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(moyCd_Arr);
    }

    // 管理番号配列
    if (StringUtils.isEmpty(kanriNo_Arr)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(kanriNo_Arr);
    }

    // 重複件数配列
    if (StringUtils.isEmpty(dblCnt_Arr)) {
      values += "null ";
    } else {
      values += "? ";
      prmData.add(dblCnt_Arr);
    }

    valueData = ArrayUtils.add(valueData, "(" + values + ")");

    sbSQL = new StringBuffer();
    sbSQL.append("MERGE INTO INATK.TOKJU_SHNNNDT AS T USING (SELECT ");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
    sbSQL.append(", current timestamp AS ADDDT "); // 登録日：
    sbSQL.append(", current timestamp AS UPDDT "); // 更新日：
    sbSQL.append(" FROM (values " + StringUtils.join(valueData, ",") + ") as T1(NUM");
    sbSQL.append(",SHNCD"); // F2 : 商品コード
    sbSQL.append(",NNDT"); // F13 : 納入日
    sbSQL.append(",MOYCD_ARR"); // 特殊 : 催し配列
    sbSQL.append(",KANRINO_ARR"); // 特殊 : 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 特殊 : 重複件数配列
    sbSQL.append(")) as RE on (");
    sbSQL.append("T.SHNCD=RE.SHNCD AND ");
    sbSQL.append("T.NNDT=RE.NNDT ");
    sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
    sbSQL.append("MOYCD_ARR=RE.MOYCD_ARR");
    sbSQL.append(",KANRINO_ARR=RE.KANRINO_ARR");
    sbSQL.append(",DBLCNT_ARR=RE.DBLCNT_ARR");
    sbSQL.append(",OPERATOR=RE.OPERATOR ");
    sbSQL.append(",UPDDT=RE.UPDDT");
    sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT");
    sbSQL.append(") VALUES (");
    sbSQL.append(" RE.SHNCD"); // 商品コード
    sbSQL.append(",RE.NNDT"); // 納入日
    sbSQL.append(",RE.MOYCD_ARR"); // 催し配列
    sbSQL.append(",RE.KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",RE.DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(",RE.OPERATOR");
    sbSQL.append(",RE.ADDDT");
    sbSQL.append(",RE.UPDDT");
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("事前打出し_商品納入日");

    return sbSQL.toString();
  }

  /**
   * 店舗アンケート付き送り付けDELETE処理(論理削除)
   *
   * @param data
   * @param userInfo
   */
  public String createDelSqlQju(JSONObject data, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    StringBuffer sbSQL = new StringBuffer();
    Object[] valueData = new Object[] {};
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";
    String values = "";

    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // 入力値格納用変数
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String moyCd_Arr = "";
    String kanriNo_Arr = "";
    String dblCnt_Arr = "";

    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT=null AND ";
    } else {
      sqlWhere += "MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN=null AND ";
    } else {
      sqlWhere += "MOYSRBAN=? AND ";
      paramData.add(moysrban);
    }

    // 管理番号
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "KANRINO=null";
    } else {
      sqlWhere += "KANRINO=?";
      paramData.add(kanrino);
    }

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE INATK.TOKQJU_SHN ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal()); // 送信フラグ=未送信
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=current timestamp ");
    sbSQL.append(" WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("店舗アンケート付送り付_商品");

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT=null AND ";
    } else {
      sqlWhere += "MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN=null AND ";
    } else {
      sqlWhere += "MOYSRBAN=? AND ";
      paramData.add(moysrban);
    }

    // 管理番号
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "KANRINOM<>null AND ";
    } else {
      sqlWhere += "KANRINO<>? AND ";
      paramData.add(kanrino);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("COUNT(MOYSKBN) AS CNT ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKQJU_SHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // 子レコードが0件の場合親
    if (dbDatas.size() == 0 || (dbDatas.size() != 0 && !StringUtils.isEmpty(dbDatas.getJSONObject(0).optString("CNT")) && dbDatas.getJSONObject(0).optString("CNT").equals("0"))) {

      // 変数を初期化
      sbSQL = new StringBuffer();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null";
      } else {
        sqlWhere += "MOYSRBAN=?";
        paramData.add(moysrban);
      }

      sbSQL = new StringBuffer();
      sbSQL.append("UPDATE INATK.TOKQJU_MOY ");
      sbSQL.append("SET ");
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=current timestamp ");
      sbSQL.append(" WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("店舗アンケート付送り付_催し");


      // 変数を初期化
      sbSQL = new StringBuffer();
      sbSQL.append("DELETE FROM INATK.SYSMOYCD ");
      sbSQL.append(" WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("催しコード内部管理");
    } else {
      shnCnt = 1;
    }

    // 登録済みの発注数を取得
    tenSuryo = new ReportJU012Dao(JNDIname).getHtsuArrMap(data.optString("F1"), kanrino, "5");

    String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

    HashMap<String, String> moyCdMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F5"), data.optString("F17"), "1");
    HashMap<String, String> kanriNoMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F5"), data.optString("F17"), "2");
    HashMap<String, String> cblCntMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F5"), data.optString("F17"), "3");

    for (HashMap.Entry<String, String> getKeyVal : tenSuryo.entrySet()) {
      String key = getKeyVal.getKey();

      String getMoysCd = "";
      String getKanriNo = "";
      int flg = 0;
      if (moyCdMap.containsKey(key)) {
        getMoysCd = moyCdMap.get(key);
      }

      if (kanriNoMap.containsKey(key)) {
        getKanriNo = kanriNoMap.get(key);
      }

      // 同一のものだった場合
      if (moyscd.equals(getMoysCd) && String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {

        if (moyCdMap.containsKey(key)) {
          moyCdMap.remove(key);
        }

        if (kanriNoMap.containsKey(key)) {
          kanriNoMap.remove(key);
        }

        if (cblCntMap.containsKey(key)) {

          flg = Integer.valueOf(cblCntMap.get(key));
          if (flg > 0) {
            cblCntMap.replace(key, String.valueOf(flg - 1));
          } else {
            cblCntMap.remove(key);
          }
        }
      }
    }

    moyCd_Arr = new ReportJU012Dao(JNDIname).createArr(moyCdMap, "1");
    kanriNo_Arr = new ReportJU012Dao(JNDIname).createArr(kanriNoMap, "2");
    dblCnt_Arr = new ReportJU012Dao(JNDIname).createArr(cblCntMap, "3");

    // 事前打出し_商品納入日更新
    values = "1,";
    paramData = new ArrayList<String>();
    valueData = new Object[] {};

    // 商品コード
    if (StringUtils.isEmpty(data.optString("F5"))) {
      values += "null,";
    } else {
      values += "?,";
      paramData.add(data.optString("F5"));
    }

    // 納入日
    if (StringUtils.isEmpty(data.optString("F17"))) {
      values += "null,";
    } else {
      values += "?,";
      paramData.add(data.optString("F17"));
    }

    // 催し配列
    if (StringUtils.isEmpty(moyCd_Arr)) {
      values += "null,";
    } else {
      values += "?,";
      paramData.add(moyCd_Arr);
    }

    // 管理番号配列
    if (StringUtils.isEmpty(kanriNo_Arr)) {
      values += "null,";
    } else {
      values += "?,";
      paramData.add(kanriNo_Arr);
    }

    // 重複件数配列
    if (StringUtils.isEmpty(dblCnt_Arr)) {
      values += "null ";
    } else {
      values += "? ";
      paramData.add(dblCnt_Arr);
    }

    valueData = ArrayUtils.add(valueData, "(" + values + ")");

    sbSQL = new StringBuffer();
    sbSQL.append("MERGE INTO INATK.TOKJU_SHNNNDT AS T USING (SELECT ");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
    sbSQL.append(", current timestamp AS ADDDT "); // 登録日：
    sbSQL.append(", current timestamp AS UPDDT "); // 更新日：
    sbSQL.append(" FROM (values " + StringUtils.join(valueData, ",") + ") as T1(NUM");
    sbSQL.append(",SHNCD"); // F2 : 商品コード
    sbSQL.append(",NNDT"); // F13 : 納入日
    sbSQL.append(",MOYCD_ARR"); // 特殊 : 催し配列
    sbSQL.append(",KANRINO_ARR"); // 特殊 : 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 特殊 : 重複件数配列
    sbSQL.append(")) as RE on (");
    sbSQL.append("T.SHNCD=RE.SHNCD AND ");
    sbSQL.append("T.NNDT=RE.NNDT ");
    sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
    sbSQL.append("MOYCD_ARR=RE.MOYCD_ARR");
    sbSQL.append(",KANRINO_ARR=RE.KANRINO_ARR");
    sbSQL.append(",DBLCNT_ARR=RE.DBLCNT_ARR");
    sbSQL.append(",OPERATOR=RE.OPERATOR ");
    sbSQL.append(",UPDDT=RE.UPDDT");
    sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT");
    sbSQL.append(") VALUES (");
    sbSQL.append(" RE.SHNCD"); // 商品コード
    sbSQL.append(",RE.NNDT"); // 納入日
    sbSQL.append(",RE.MOYCD_ARR"); // 催し配列
    sbSQL.append(",RE.KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",RE.DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(",RE.OPERATOR");
    sbSQL.append(",RE.ADDDT");
    sbSQL.append(",RE.UPDDT");
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("事前打出し_商品納入日");

    return sbSQL.toString();
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(店別アンケート付き送り付け_催し)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_TENHT")); // 更新情報(数量パターン)
    String shoriDt = map.get("SHORIDT"); // 処理日付
    kanrino = map.get("KANRINO"); // 管理番号

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    MessageUtility mu = new MessageUtility();
    JSONArray dbDatas = new JSONArray();

    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    if (dataArrayG.size() == 0 || dataArrayG.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 催しコード存在チェック
    JSONObject data = dataArray.getJSONObject(0);

    if (!StringUtils.isEmpty(data.optString("F1")) && data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    } else {
      msg.add(mu.getDbMessageObj("E20005", new String[] {}));
      return msg;
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT=null AND ";
    } else {
      sqlWhere += "MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN=null";
    } else {
      sqlWhere += "MOYSRBAN=?";
      paramData.add(moysrban);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("NNSTDT "); // 納入開始日
    sbSQL.append(",NNEDDT "); // 納入終了日
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // 登録のない催しコード
      msg.add(mu.getDbMessageObj("E20005", new String[] {}));
      return msg;
    }

    // 発注日範囲チェック
    if (!StringUtils.isEmpty(data.optString("F17")) && !StringUtils.isEmpty(data.optString("F16")) && data.optInt("F17") <= data.optInt("F16")) {
      // 納入日 <= 発注日はエラー
      msg.add(mu.getDbMessageObj("E20264", new String[] {}));
      return msg;
    }

    // 納入日の範囲チェック
    if (dbDatas.getJSONObject(0).optInt("NNSTDT") > data.optInt("F17") || dbDatas.getJSONObject(0).optInt("NNEDDT") < data.optInt("F17")) {
      // 納入日 <= 発注日はエラー
      msg.add(mu.getDbMessageObj("E20274", new String[] {}));
      return msg;
    }

    // 処理日付 < 発注日 < 納入日以外エラー
    if (!StringUtils.isEmpty(shoriDt)) {
      if (!(Integer.valueOf(shoriDt) < data.optInt("F16"))) {
        // 発注日>処理日付の条件で入力してください。
        msg.add(mu.getDbMessageObj("E20127", new String[] {}));
        return msg;
      } else if (!(data.optInt("F16") < data.optInt("F17"))) {
        // 発注日 ＜ 納入日の条件で入力してください。
        msg.add(mu.getDbMessageObj("E20264", new String[] {}));
        return msg;
      }
    }

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    // 商品存在チェック
    if (StringUtils.isEmpty(data.optString("F5"))) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(data.optString("F5"));
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTSHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // マスタに登録のない商品
      msg.add(mu.getDbMessageObj("E11046", new String[] {}));
      return msg;
    }

    // 登録済みの発注数を取得
    HashMap<String, String> suryoMap = new ReportJU012Dao(JNDIname).getHtsuArrMap(data.optString("F1"), kanrino, "5");

    boolean suryoEmpty = true;

    for (int i = 0; i < dataArrayG.size(); i++) {
      JSONObject dataG = new JSONObject();
      dataG = dataArrayG.getJSONObject(i);

      if (!dataG.isEmpty() && !StringUtils.isEmpty(dataG.optString("F1"))) {

        int tencd = dataG.optInt("F1");
        String suryo = dataG.optString("F2");

        if (!StringUtils.isEmpty(suryo)) {
          suryoEmpty = false;
        }

        if (suryoMap.containsKey(String.valueOf(tencd))) {
          if (!suryoMap.get(String.valueOf(tencd)).equals(suryo)) {
            tenSuryo.put(String.valueOf(tencd), suryo);
          } else {
            tenSuryo.put(String.valueOf(tencd), "");
          }
        } else {
          if (!suryo.equals("0")) {
            tenSuryo.put(String.valueOf(tencd), suryo);
          }
        }
      }
    }

    if (suryoEmpty) {
      // 数量欄に入力なしは不可
      msg.add(mu.getDbMessageObj("E20550", new String[] {}));
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(店舗別発注数量)

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
}
