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
public class ReportJU013Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU013Dao(String JNDIname) {
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
        ",DATE_FORMAT(DATE_FORMAT(T1.NNSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNSTDT, '%Y%m%d')))||'～'||DATE_FORMAT(DATE_FORMAT(T1.NNEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNEDDT, '%Y%m%d')))");
    sbSQL.append(",T2.SHNCD ");
    sbSQL.append(",T3.SHNKN ");
    sbSQL.append(",T2.SHNKBN ");
    sbSQL.append(",T2.TSEIKBN ");
    sbSQL.append(",T2.JUKBN ");
    sbSQL.append(",T2.BDENKBN ");
    sbSQL.append(",T2.WAPPNKBN ");
    sbSQL.append(",T2.IRISU ");
    sbSQL.append(",T2.GENKAAM ");
    sbSQL.append(",T2.BAIKAAM ");
    sbSQL.append(",CASE WHEN T3.ZEIRT IS NULL THEN T2.BAIKAAM  ");
    sbSQL.append("ELSE CEILING(CAST(T2.BAIKAAM AS DECIMAL(8,2)) / NULLIF(1 + CAST(T3.ZEIRT AS DECIMAL(4,2)) / 100, 0)) END HONBAIKA ");
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(T2.HTDT,'%Y%m%d'),'%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.HTDT, '%Y%m%d')))");
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(T2.NNDT,'%Y%m%d'),'%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.NNDT, '%Y%m%d')))");
    sbSQL.append(",T2.OPERATOR "); // F17 : オペレーター
    sbSQL.append(",DATE_FORMAT(T2.ADDDT,'%y/%m/%d') AS ADDDT "); // F18 : 登録日
    sbSQL.append(",DATE_FORMAT(T2.UPDDT,'%y/%m/%d') AS UPDDT "); // F19 : 更新日
    sbSQL.append(",DATE_FORMAT(T2.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F20 : 更新日時
    sbSQL.append(",T2.JUTENKAIKBN ");
    sbSQL.append(",T2.RANKNO_ADD ");
    sbSQL.append(",T2.HTSU ");
    sbSQL.append(",T2.SURYOPTN ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD T1, ");
    sbSQL.append("INATK.TOKJU_SHN T2 LEFT JOIN ");
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
    sbSQL.append(") T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN = T2.ZEIRTKBN) AS T3 ON T2.SHNCD=T3.SHNCD ");
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

    // 管理番号
    if (StringUtils.isEmpty(szKanrino)) {
      sqlWhere2 += "T2.KANRINO=null AND ";
    } else {
      sqlWhere2 += "T2.KANRINO=? AND ";
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(事前打出し商品)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_TENHT")); // 更新情報(数量パターン)

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 事前打出し商品INSERT/UPDATE処理
    createSqlJu(data, dataArrayG, userInfo);

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

      targetTable = "INATK.TOKJU_SHN";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F20"))) {
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(事前打出し商品)

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);
    kanrino = map.get("KANRINO"); // 管理番号

    // 事前打出し商品DELETE(論理削除)処理
    createDelSqlJu(data, userInfo);

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

      targetTable = "INATK.TOKJU_SHN";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F20"))) {
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
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(事前打出し商品)
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
    if (!StringUtils.isEmpty(data.optString("F16")) && !StringUtils.isEmpty(data.optString("F15")) && data.optInt("F16") <= data.optInt("F15")) {
      // 納入日 <= 発注日はエラー
      msg.add(mu.getDbMessageObj("E20264", new String[] {}));
      return msg;
    }

    // 納入日の範囲チェック
    if (dbDatas.getJSONObject(0).optInt("NNSTDT") > data.optInt("F16") || dbDatas.getJSONObject(0).optInt("NNEDDT") < data.optInt("F16")) {
      // 納入日 <= 発注日はエラー
      msg.add(mu.getDbMessageObj("E20274", new String[] {}));
      return msg;
    }

    // 処理日付 < 発注日 < 納入日以外エラー
    if (!StringUtils.isEmpty(shoriDt)) {
      if (!(Integer.valueOf(shoriDt) < data.optInt("F15"))) {
        // 発注日>処理日付の条件で入力してください。
        msg.add(mu.getDbMessageObj("E20127", new String[] {}));
        return msg;
      } else if (!(data.optInt("F15") < data.optInt("F16"))) {
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
    if (StringUtils.isEmpty(data.optString("F4"))) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(data.optString("F4"));
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
    HashMap<String, String> suryoMap = new ReportJU012Dao(JNDIname).getHtsuArrMap(data.optString("F1"), kanrino, "4");

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

  /**
   * 事前打出し商品INSERT/UPDATE処理
   *
   * @param data
   * @param dataArrayG
   * @param userInfo
   */
  public String createSqlJu(JSONObject data, JSONArray dataArrayG, User userInfo) {

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

    // 余白を追加
    tenHtSu_Arr = new ReportJU012Dao(JNDIname).spaceArr(tenHtSu_Arr, 5);

    int maxField = 25; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // 催しコードを追加
        values += " ?, ?, ?, ?";
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(kanrino);
      }

      if (!ArrayUtils.contains(new String[] {"F1", "F2", "F3", "F5", "F14", "F17", "F18", "F19", "F20"}, key)) {
        String val = data.optString(key);
        if (key.equals("F25")) {
          values += ", ?";
          prmData.add(tenHtSu_Arr);
        } else if (key.equals("F9") && StringUtils.isEmpty(val)) {
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
        valueData = ArrayUtils.add(valueData, values);
        values = "";
      }
    }

    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKJU_SHN ( ");
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
    sbSQL.append(",UPDKBN"); // 更新区分：
    sbSQL.append(",SENDFLG"); // 送信区分：
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES ( " + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分：
    sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " "); // 送信区分：
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日：
    sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日：
    sbSQL.append(")");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" MOYSKBN=VALUES(MOYSKBN) ");
    sbSQL.append(",MOYSSTDT=VALUES(MOYSSTDT) ");
    sbSQL.append(",MOYSRBAN=VALUES(MOYSRBAN) ");
    sbSQL.append(",KANRINO=VALUES(KANRINO) ");
    sbSQL.append(",SHNCD=VALUES(SHNCD) ");
    sbSQL.append(",SHNKBN=VALUES(SHNKBN) ");
    sbSQL.append(",TSEIKBN=VALUES(TSEIKBN) ");
    sbSQL.append(",JUKBN=VALUES(JUKBN) ");
    sbSQL.append(",BDENKBN=VALUES(BDENKBN) ");
    sbSQL.append(",WAPPNKBN=VALUES(WAPPNKBN) ");
    sbSQL.append(",IRISU=VALUES(IRISU) ");
    sbSQL.append(",GENKAAM=VALUES(GENKAAM) ");
    sbSQL.append(",BAIKAAM=VALUES(BAIKAAM) ");
    sbSQL.append(",HTDT=VALUES(HTDT) ");
    sbSQL.append(",NNDT=VALUES(NNDT) ");
    sbSQL.append(",JUTENKAIKBN=VALUES(JUTENKAIKBN) ");
    sbSQL.append(",RANKNO_ADD=VALUES(RANKNO_ADD) ");
    sbSQL.append(",HTSU=VALUES(HTSU) ");
    sbSQL.append(",SURYOPTN=VALUES(SURYOPTN) ");
    sbSQL.append(",TENHTSU_ARR=VALUES(TENHTSU_ARR) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("事前打出し商品");

    // 事前打出し_商品納入日用配列作成
    String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));
    HashMap<String, String> moyCdMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "1");
    HashMap<String, String> kanriNoMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "2");
    HashMap<String, String> cblCntMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "3");

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
    values = " ";
    prmData = new ArrayList<String>();
    valueData = new Object[] {};

    // 商品コード
    if (StringUtils.isEmpty(data.optString("F4"))) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(data.optString("F4"));
    }

    // 納入日
    if (StringUtils.isEmpty(data.optString("F16"))) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(data.optString("F16"));
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

    valueData = ArrayUtils.add(valueData, values);

    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKJU_SHNNNDT ( ");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES ( " + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" SHNCD=VALUES(SHNCD) ");
    sbSQL.append(",NNDT=VALUES(NNDT) ");
    sbSQL.append(",MOYCD_ARR=VALUES(MOYCD_ARR) ");
    sbSQL.append(",KANRINO_ARR=VALUES(KANRINO_ARR) ");
    sbSQL.append(",DBLCNT_ARR=VALUES(DBLCNT_ARR) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("事前打出し_商品納入日");

    return sbSQL.toString();
  }

  /**
   * 事前打出し商品DELETE処理(論理削除)
   *
   * @param data
   * @param userInfo
   */
  public String createDelSqlJu(JSONObject data, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    StringBuffer sbSQL = new StringBuffer();
    Object[] valueData = new Object[] {};
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";
    String values = "";

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
    sbSQL.append("UPDATE INATK.TOKJU_SHN ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal()); // 送信フラグ=未送信
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=current_timestamp ");
    sbSQL.append(" WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("事前打出し商品");

    String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

    HashMap<String, String> moyCdMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "1");
    HashMap<String, String> kanriNoMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "2");
    HashMap<String, String> cblCntMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "3");

    for (HashMap.Entry<String, String> getKeyVal : new ReportJU012Dao(JNDIname).getArrMap(data.optString("F4"), data.optString("F16"), "1").entrySet()) {

      String key = getKeyVal.getKey();
      String getMoysCd = getKeyVal.getValue();
      String getKanriNo = kanriNoMap.containsKey(key) ? kanriNoMap.get(key) : "";
      int flg = 0;

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
    values = " ";
    paramData = new ArrayList<String>();
    valueData = new Object[] {};

    // 商品コード
    if (StringUtils.isEmpty(data.optString("F4"))) {
      values += "null,";
    } else {
      values += "?,";
      paramData.add(data.optString("F4"));
    }

    // 納入日
    if (StringUtils.isEmpty(data.optString("F16"))) {
      values += "null,";
    } else {
      values += "?,";
      paramData.add(data.optString("F16"));
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

    valueData = ArrayUtils.add(valueData, values);

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKJU_SHNNNDT ( ");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(" )VALUES( " + values + " ");
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("事前打出し_商品納入日");

    return sbSQL.toString();
  }
}
