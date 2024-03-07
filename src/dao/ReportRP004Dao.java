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
import authentication.defines.Consts;
import common.CmnDate;
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
public class ReportRP004Dao extends ItemDao {

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
  public ReportRP004Dao(String JNDIname) {
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
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szRinji = getMap().get("RINJI"); // 臨時
    String szMoyscd = getMap().get("MOYSCD"); // 催しコード
    String szSryptnno = getMap().get("SRYPTNNO"); // 数量パターンNo.
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";

    // パラメータ確認
    // 必須チェック
    if (szBmncd == null) {
      System.out.println(super.getConditionLog());
      return "";
    }
    if (!StringUtils.isEmpty(szMoyscd)) {
      moyskbn = StringUtils.substring(szMoyscd, 0, 1);
      moysstdt = StringUtils.substring(szMoyscd, 1, 7);
      moysrban = StringUtils.substring(szMoyscd, 7, 10);
    }

    StringBuffer sbSQL = new StringBuffer();
    if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
      // 新規の場合
      if (StringUtils.equals("1", szRinji)) {
        // 臨時チェックありの場合
        sbSQL.append("select ");
        sbSQL.append(szBmncd + " as F1 "); // F1 : 部門
        sbSQL.append(", 1 as F2"); // F2 : 臨時
        sbSQL.append(", right('0'||" + moyskbn + ", 1)||'-'||right('000000'||" + moysstdt + ", 6)||'-'||right('000'||" + moysrban + ", 3) as F3"); // F3 : 催しコード
        sbSQL.append(", NULL as F4"); // F4 : 数量パターンNo.
        sbSQL.append(", NULL as F5"); // F5 : 数量パターン名称
        sbSQL.append(" from (SELECT 1 AS DUMMY) DUMMY");
      } else {
        // 臨時チェックなしの場合
        sbSQL.append("select ");
        sbSQL.append(szBmncd + " as F1 "); // F1 : 部門
        sbSQL.append(", NULL as F2"); // F2 : 臨時
        sbSQL.append(", NULL as F3"); // F3 : 催しコード
        sbSQL.append(", NULL as F4"); // F4 : 数量パターンNo.
        sbSQL.append(", NULL as F5"); // F5 : 数量パターン名称
        sbSQL.append(" from (SELECT 1 AS DUMMY) DUMMY");
      }
    } else if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid)) {
      // 変更・参照の場合
      if (StringUtils.equals("1", szRinji)) {
        // 臨時チェックありの場合
        sbSQL.append("select");
        sbSQL.append(" BMNCD as F1 "); // F1 : 部門
        sbSQL.append(", 1 as F2"); // F2 : 臨時
        sbSQL.append(", right('0'||MOYSKBN, 1)||'-'||right('000000'||MOYSSTDT, 6)||'-'||right('000'||MOYSRBAN, 3) as F3"); // F3 : 催しコード
        sbSQL.append(", SRYPTNNO as F4"); // F4 : 数量パターンNo.
        sbSQL.append(", SRYPTNKN as F5"); // F5 : 数量パターン名称
        sbSQL.append(" from INATK.TOKSRPTNEX");
        sbSQL.append(" where BMNCD =" + szBmncd);
        sbSQL.append(" and MOYSKBN=" + moyskbn + " and MOYSSTDT=" + moysstdt + " and MOYSRBAN=" + moysrban + " ");
        sbSQL.append(" and SRYPTNNO =" + szSryptnno);
      } else {
        // 臨時チェックなしの場合
        sbSQL.append("select");
        sbSQL.append(" BMNCD as F1 "); // F1 : 部門
        sbSQL.append(", NULL as F2"); // F2 : 臨時
        sbSQL.append(", NULL as F3"); // F3 : 催しコード
        sbSQL.append(", SRYPTNNO as F4"); // F4 : 数量パターンNo.
        sbSQL.append(", SRYPTNKN as F5"); // F5 : 数量パターン名称
        sbSQL.append(" from");
        sbSQL.append(" (select");
        sbSQL.append(" BMNCD");
        sbSQL.append(", SRYPTNNO");
        sbSQL.append(", SRYPTNKN");
        sbSQL.append(" from INATK.TOKSRPTN");
        sbSQL.append(" where BMNCD =" + szBmncd);
        sbSQL.append(" and SRYPTNNO =" + szSryptnno);
        sbSQL.append(" ) as t1 ");
      }
    }

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

    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo, sysdate);
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

  boolean isTest = true;

  /**
   * 更新処理実行
   *
   * @param map
   * @param userInfo
   * @return
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    JSONObject option = new JSONObject();
    new JSONArray();

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray dataArrayRanksuryo = JSONArray.fromObject(map.get("DATA_RANKSURYO")); // 更新情報

    // 必須チェック
    if (dataArray.isEmpty() && dataArrayRanksuryo.isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    if (StringUtils.equals("0", dataArray.getJSONObject(0).getString("F2"))) {
      // SQL発行：数量パターン(登録)
      this.createSqlSrptn(data, map, userInfo);

      // SQL発行：数量ランク(登録)
      // this.createSqlSryrank(dataArray, dataArrayRanksuryo, map, userInfo, sysdate);

    } else {
      // SQL発行：臨時数量パターン(登録)
      this.createSqlSrptnex(data, map, userInfo);

      // SQL発行：臨時数量ランク(登録)
      // this.createSqlSryrankex(dataArray, dataArrayRanksuryo, map, userInfo, sysdate);
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


  /**
   * 数量パターン INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  /*
   * public String createSqlSrptn(JSONArray dataArray, HashMap<String, String> map, User userInfo,
   * String sysdate){
   *
   * String dbsysdate = CmnDate.dbDateFormat(sysdate); JSONObject option = new JSONObject(); // 更新情報
   * ArrayList<String> prmData = new ArrayList<String>(); String values = "";
   *
   *
   * String obj = map.get(DefineReport.ID_PARAM_OBJ); JSONObject msgObj = new JSONObject(); JSONArray
   * msg = new JSONArray();
   *
   * // パラメータ確認 int kryoColNum = 8; // テーブル列数 // ログインユーザー情報取得 int userId = userInfo.getCD_user(); //
   * ログインユーザー values = ""; // 更新情報 for (int i = 1; i <= kryoColNum; i++) { String col = "F" + i;
   * String val = dataArray.optJSONObject(0).optString(col); if (i==2) { // F2 : 数量パターンNo.： val =
   * dataArray.optJSONObject(0).optString("F4"); } else if (i==3) { // F3 : 数量パターン名称： val =
   * dataArray.optJSONObject(0).optString("F5"); } else if (i==4) { // F4 : 更新区分： val = "0"; } else if
   * (i==5) { // F5 : 送信フラグ： val = "0"; } else if (i==6) { // F6 : オペレーター： val = ""+userId; } else if
   * (i==7) { // F7 : 登録日： val = dbsysdate; } else if (i==8) { // F8 : 更新日： val = dbsysdate; } if
   * (isTest) { if (i == 1) { values += "( '" + val + "'"; // F1 : 部門： } else if (i == 8) { values +=
   * ", '" + val + "')"; // F8 : 更新日： } else { values += ", '" + val + "'"; } } else {
   * prmData.add(val); values += ", ?"; } } // 基本INSERT/UPDATE文 StringBuffer sbSQL; // 更新SQL sbSQL =
   * new StringBuffer(); sbSQL = new StringBuffer();
   * sbSQL.append("merge into INATK.TOKSRPTN as T using (select"); sbSQL.append("  BMNCD"); // F1 :
   * 部門： sbSQL.append(", SRYPTNNO"); // F2 : 数量パターンNo.： sbSQL.append(", SRYPTNKN"); // F3 : 数量パターン名称：
   * sbSQL.append(", UPDKBN"); // F4 : 更新区分 sbSQL.append(", SENDFLG"); // F5 : 送信フラグ
   * sbSQL.append(", OPERATOR"); // F6 : オペレータ sbSQL.append(", ADDDT"); // F7 : 登録日
   * sbSQL.append(", UPDDT"); // F8 : 更新日 sbSQL.append(" from (values "+values+" ) as T1(");
   * sbSQL.append("  BMNCD"); // F1 : 部門： sbSQL.append(", SRYPTNNO"); // F2 : 数量パターンNo.：
   * sbSQL.append(", SRYPTNKN"); // F3 : 数量パターン名称： sbSQL.append(", UPDKBN"); // F4 : 更新区分：
   * sbSQL.append(", SENDFLG"); // F5 : 送信フラグ： sbSQL.append(", OPERATOR"); // F6 : オペレーター：
   * sbSQL.append(", ADDDT"); // F7 : 登録日： sbSQL.append(", UPDDT"); // F8 : 更新日：
   * sbSQL.append("))as RE on (T.BMNCD = RE.BMNCD and T.SRYPTNNO = RE.SRYPTNNO)");
   * sbSQL.append(" when matched then update set"); sbSQL.append("  BMNCD = RE.BMNCD"); // F1 : 部門：
   * sbSQL.append(", SRYPTNNO = RE.SRYPTNNO"); // F2 : 数量パターンNo.：
   * sbSQL.append(", SRYPTNKN = RE.SRYPTNKN"); // F3 : 数量パターン名称： sbSQL.append(", UPDKBN = RE.UPDKBN");
   * // F4 : 更新区分： sbSQL.append(", SENDFLG = RE.SENDFLG"); // F5 : 送信フラグ：
   * sbSQL.append(", OPERATOR = RE.OPERATOR"); // F6 : オペレーター： sbSQL.append(", UPDDT = RE.UPDDT"); //
   * F8 : 更新日： sbSQL.append(" when not matched then insert("); sbSQL.append("  BMNCD"); // F1 : 部門：
   * sbSQL.append(", SRYPTNNO"); // F2 : 数量パターンNo.： sbSQL.append(", SRYPTNKN"); // F3 : 数量パターン名称：
   * sbSQL.append(", UPDKBN"); // F4 : 更新区分： sbSQL.append(", SENDFLG"); // F5 : 送信フラグ：
   * sbSQL.append(", OPERATOR"); // F6 : オペレーター： sbSQL.append(", ADDDT"); // F7 : 登録日：
   * sbSQL.append(", UPDDT"); // F8 : 更新日： sbSQL.append(") values ("); sbSQL.append("  RE.BMNCD"); //
   * F1 : 部門： sbSQL.append(", RE.SRYPTNNO"); // F2 : 数量パターンNo.： sbSQL.append(", RE.SRYPTNKN"); // F3 :
   * 数量パターン名称： sbSQL.append(", RE.UPDKBN"); // F4 : 更新区分： sbSQL.append(", RE.SENDFLG"); // F5 : 送信フラグ：
   * sbSQL.append(", RE.OPERATOR"); // F6 : オペレーター： sbSQL.append(", RE.ADDDT"); // F7 : 登録日：
   * sbSQL.append(", RE.UPDDT"); // F8 : 更新日： sbSQL.append(")");
   *
   * System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());
   *
   * sqlList.add(sbSQL.toString()); prmList.add(prmData); lblList.add("数量パターン");
   *
   * // // クリア // prmData = new ArrayList<String>(); // valueData = new Object[]{}; // values = "";
   *
   * return sbSQL.toString(); }
   */

  /**
   * 数量パターンINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlSrptn(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayRanksuryo = JSONArray.fromObject(map.get("DATA_RANKSURYO")); // 更新情報


    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    map.get("DISPTYPE");

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 3; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // values += String.valueOf(0 + 1);
      }
      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);

        if (StringUtils.equals("F2", key)) {
          val = data.optString("F4");

        } else if (StringUtils.equals("F3", key)) {
          val = data.optString("F5");
        }
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

    // ランクマスタの登録・更新
    sbSQL = new StringBuffer();
    sbSQL.append(" INSERT INTO INATK.TOKSRPTN (");
    sbSQL.append("  BMNCD"); // F1 : 部門
    sbSQL.append(" ,SRYPTNNO"); // F2 : 数量パターン№
    sbSQL.append(" ,SRYPTNKN"); // F3 : 数量パターン名
    sbSQL.append(", SENDFLG"); // F5 : 送信フラグ
    sbSQL.append(", UPDKBN"); // 更新区分
    sbSQL.append(", OPERATOR "); // オペレーター
    sbSQL.append(", ADDDT "); // 登録日
    sbSQL.append(", UPDDT "); // 更新日
    sbSQL.append(") values (");
    sbSQL.append(StringUtils.join(valueData, ",").substring(1));
    sbSQL.append(", 0 "); // F5 : 送信フラグ
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分
    sbSQL.append(", '" + userId + "' "); // オペレーター
    sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日
    sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
    sbSQL.append(")");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" BMNCD=VALUES(BMNCD) ");
    sbSQL.append(",SRYPTNNO=VALUES(SRYPTNNO) ");
    sbSQL.append(",SRYPTNKN=VALUES(SRYPTNKN) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("数量パターン");

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    maxField = 4; // Fxxの最大値
    int len = dataArrayRanksuryo.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayRanksuryo.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (k == 1) {
            // values += " ?";
          }

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);

            // 新規登録時は管理番号を使用する。
            if (StringUtils.equals("F1", key)) {
              val = data.optString("F1");

            } else if (StringUtils.equals("F2", key)) {
              val = data.optString("F4");

            } else if (StringUtils.equals("F3", key)) {
              val = dataT.optString("F1");

            } else if (StringUtils.equals("F4", key)) {
              val = dataT.optString("F2");
              if (StringUtils.isEmpty(val)) {
                val = "0";
              }
            }

            if (k == 1) {
              if (StringUtils.isEmpty(val)) {
                values += " null";
              } else {
                values += " ?";
                prmData.add(val);
              }
            } else {
              if (StringUtils.isEmpty(val)) {
                values += ", null";
              } else {
                values += ", ?";
                prmData.add(val);
              }
            }
            if (k == 4) {
              values += ", 0";
              values += ", '" + userId + "'";
              values += ", CURRENT_TIMESTAMP";
              values += ", CURRENT_TIMESTAMP";
            }
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 数量ランクの登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" INSERT INTO INATK.TOKSRYRANK (");
        sbSQL.append("  BMNCD"); // F1 : 部門
        sbSQL.append(" ,SRYPTNNO"); // F2 : 数量パターン№
        sbSQL.append(" ,TENRANK"); // F3 : 店ランク
        sbSQL.append(" ,SURYO"); // F4 : 数量
        sbSQL.append(", SENDFLG"); // F5 : 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター
        sbSQL.append(", ADDDT "); // 登録日
        sbSQL.append(", UPDDT "); // 更新日
        sbSQL.append(") values (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append("");
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" BMNCD=VALUES(BMNCD) ");
        sbSQL.append(",SRYPTNNO=VALUES(SRYPTNNO) ");
        sbSQL.append(",TENRANK=VALUES(TENRANK) ");
        sbSQL.append(",SURYO=VALUES(SURYO) ");
        sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
        sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
        // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
        sbSQL.append(",UPDDT=VALUES(UPDDT) ");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("数量ランク");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * 臨時数量パターンINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlSrptnex(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayRanksuryo = JSONArray.fromObject(map.get("DATA_RANKSURYO")); // 更新情報


    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    map.get("DISPTYPE");

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 6; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // values += String.valueOf(0 + 1);
      }
      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);

        String moyscd = data.optString("F3");
        if (StringUtils.equals("F2", key)) {
          val = StringUtils.substring(moyscd, 0, 1);

        } else if (StringUtils.equals("F3", key)) {
          val = StringUtils.substring(moyscd, 1, 7);

        } else if (StringUtils.equals("F4", key)) {
          val = StringUtils.substring(moyscd, 7, 10);

        } else if (StringUtils.equals("F5", key)) {
          val = data.optString("F4");

        } else if (StringUtils.equals("F6", key)) {
          val = data.optString("F5");
        }
        if (k == 1) {
          if (StringUtils.isEmpty(val)) {
            values += " null";
          } else {
            values += " ?";
            prmData.add(val);
          }
        } else {
          if (StringUtils.isEmpty(val)) {
            values += ", null";
          } else {
            values += ", ?";
            prmData.add(val);
          }
        }
        if (k == 6) {
          values += ", 0";
          values += ", " + DefineReport.ValUpdkbn.NML.getVal() + " ";
          values += ", '" + userId + "' ";
          values += ", CURRENT_TIMESTAMP";
          values += ", CURRENT_TIMESTAMP";
        }
      }
      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }

    // 臨時数量パターンの登録・更新
    sbSQL = new StringBuffer();
    sbSQL.append(" INSERT INTO INATK.TOKSRPTNEX (");
    sbSQL.append("  BMNCD"); // F1 : 部門
    sbSQL.append(" ,MOYSKBN"); // F2 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F3 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F4 : 催し連番
    sbSQL.append(" ,SRYPTNNO"); // F5 : 数量パターン№
    sbSQL.append(" ,SRYPTNKN"); // F6 : 数量パターン名称
    sbSQL.append(", SENDFLG"); // F5 : 送信フラグ
    sbSQL.append(", UPDKBN"); // 更新区分
    sbSQL.append(", OPERATOR "); // オペレーター
    sbSQL.append(", ADDDT "); // 登録日
    sbSQL.append(", UPDDT "); // 更新日
    sbSQL.append(") values (");
    sbSQL.append(StringUtils.join(valueData, ",").substring(1));
    sbSQL.append("");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" BMNCD=VALUES(BMNCD) ");
    sbSQL.append(",MOYSKBN=VALUES(MOYSKBN) ");
    sbSQL.append(",MOYSSTDT=VALUES(MOYSSTDT) ");
    sbSQL.append(",MOYSRBAN=VALUES(MOYSRBAN) ");
    sbSQL.append(",SRYPTNNO=VALUES(SRYPTNNO) ");
    sbSQL.append(",SRYPTNKN=VALUES(SRYPTNKN) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("臨時数量パターン");

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    maxField = 7; // Fxxの最大値
    int len = dataArrayRanksuryo.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayRanksuryo.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (k == 1) {
            // values += String.valueOf(0 + 1);

          }

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);

            // 新規登録時は管理番号を使用する。
            String moyscd = data.optString("F3");

            if (StringUtils.equals("F1", key)) {
              val = data.optString("F1");

            } else if (StringUtils.equals("F2", key)) {
              val = StringUtils.substring(moyscd, 0, 1);

            } else if (StringUtils.equals("F3", key)) {
              val = StringUtils.substring(moyscd, 1, 7);

            } else if (StringUtils.equals("F4", key)) {
              val = StringUtils.substring(moyscd, 7, 10);

            } else if (StringUtils.equals("F5", key)) {
              val = data.optString("F4");

            } else if (StringUtils.equals("F6", key)) {
              val = dataT.optString("F1");

            } else if (StringUtils.equals("F7", key)) {
              val = dataT.optString("F2");
              if (StringUtils.isEmpty(val)) {
                val = "0";
              }
            }
            if (k == 1) {
              if (StringUtils.isEmpty(val)) {
                values += " null";
              } else {
                values += " ?";
                prmData.add(val);
              }
            } else {
              if (StringUtils.isEmpty(val)) {
                values += ", null";
              } else {
                values += ", ?";
                prmData.add(val);
              }
            }
            if (k == 7) {
              values += ", 0";// F5 : 送信フラグ
              values += ", '" + userId + "'";// オペレーター
              values += ", CURRENT_TIMESTAMP";// 登録日
              values += ", CURRENT_TIMESTAMP";// 更新日
            }
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 臨時数量ランクの登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" INSERT INTO INATK.TOKSRYRANKEX (");
        sbSQL.append(" BMNCD"); // F1 : 部門
        sbSQL.append(", MOYSKBN"); // F2 : 催し区分
        sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日
        sbSQL.append(", MOYSRBAN"); // F4 : 催し連番
        sbSQL.append(", SRYPTNNO"); // F5 : 数量パターン№
        sbSQL.append(", TENRANK"); // F6 : 店ランク
        sbSQL.append(", SURYO"); // F7 : 数量
        sbSQL.append(", SENDFLG"); // F5 : 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター
        sbSQL.append(", ADDDT "); // 登録日
        sbSQL.append(", UPDDT "); // 更新日
        sbSQL.append(") values (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" BMNCD=VALUES(BMNCD) ");
        sbSQL.append(",MOYSKBN=VALUES(MOYSKBN) ");
        sbSQL.append(",MOYSSTDT=VALUES(MOYSSTDT) ");
        sbSQL.append(",MOYSRBAN=VALUES(MOYSRBAN) ");
        sbSQL.append(",SRYPTNNO=VALUES(SRYPTNNO) ");
        sbSQL.append(",TENRANK=VALUES(TENRANK) ");
        sbSQL.append(",SURYO=VALUES(SURYO) ");
        sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
        sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
        // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
        sbSQL.append(",UPDDT=VALUES(UPDDT) ");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("臨時数量ランク");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * 臨時数量パターン INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlSrptnex(JSONArray dataArray, HashMap<String, String> map, User userInfo, String sysdate) {
    String dbsysdate = CmnDate.dbDateFormat(sysdate);
    new JSONObject();
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";


    map.get(DefineReport.ID_PARAM_OBJ);
    new JSONObject();
    new JSONArray();

    // パラメータ確認
    int kryoColNum = 11; // テーブル列数
    // ログインユーザー情報取得
    int userId = userInfo.getCD_user(); // ログインユーザー
    values = "";
    // 更新情報
    for (int i = 1; i <= kryoColNum; i++) {
      String col = "F" + i;
      String val = dataArray.optJSONObject(0).optString(col);
      String moyscd = dataArray.optJSONObject(0).optString("F3");
      if (i == 2) { // F2 : 催し区分：
        val = StringUtils.substring(moyscd, 0, 1);
      } else if (i == 3) { // F3 : 催し開始日：
        val = StringUtils.substring(moyscd, 1, 7);
      } else if (i == 4) { // F4 : 催し連番：
        val = StringUtils.substring(moyscd, 7, 10);
      } else if (i == 5) { // F5 : 数量パターンNo.：
        val = dataArray.optJSONObject(0).optString("F4");
      } else if (i == 6) { // F6 : 数量パターン名称：
        val = dataArray.optJSONObject(0).optString("F5");
      } else if (i == 7) { // F7 : 更新区分：
        val = "0";
      } else if (i == 8) { // F8 : 送信フラグ：
        val = "0";
      } else if (i == 9) { // F9 : オペレーター：
        val = "" + userId;
      } else if (i == 10) { // F10 : 登録日：
        val = dbsysdate;
      } else if (i == 11) { // F11 : 更新日：
        val = dbsysdate;
      }
      if (isTest) {
        if (i == 1) {
          values += "( '" + val + "'"; // F1 : 部門：
        } else if (i == 11) {
          values += ", '" + val + "')"; // F11 : 更新日：
        } else {
          values += ", '" + val + "'";
        }
      } else {
        prmData.add(val);
        values += ", ?";
      }
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    // 更新SQL
    sbSQL = new StringBuffer();
    sbSQL.append("merge into INATK.TOKSRPTNEX as T using (select");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", SRYPTNKN"); // F6 : 数量パターン名称：
    sbSQL.append(", UPDKBN"); // F7 : 更新区分：
    sbSQL.append(", SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F9 : オペレーター：
    sbSQL.append(", ADDDT"); // F10 : 登録日：
    sbSQL.append(", UPDDT"); // F11 : 更新日：
    sbSQL.append(" from (values " + values + " ) as T1(");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", SRYPTNKN"); // F6 : 数量パターン名称：
    sbSQL.append(", UPDKBN"); // F7 : 更新区分：
    sbSQL.append(", SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F9 : オペレーター：
    sbSQL.append(", ADDDT"); // F10 : 登録日：
    sbSQL.append(", UPDDT"); // F11 : 更新日：
    sbSQL.append("))as RE on (T.BMNCD = RE.BMNCD and T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.SRYPTNNO = RE.SRYPTNNO)");
    sbSQL.append(" when matched then update set");
    sbSQL.append("  BMNCD = RE.BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN = RE.MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT = RE.MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN = RE.MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO = RE.SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", SRYPTNKN = RE.SRYPTNKN"); // F6 : 数量パターン名称：
    sbSQL.append(", UPDKBN = RE.UPDKBN"); // F7 : 更新区分：
    sbSQL.append(", SENDFLG = RE.SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR = RE.OPERATOR"); // F9 : オペレーター：
    sbSQL.append(", UPDDT = RE.UPDDT"); // F11 : 更新日：
    sbSQL.append(" when not matched then insert(");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", SRYPTNKN"); // F6 : 数量パターン名称：
    sbSQL.append(", UPDKBN"); // F7 : 更新区分：
    sbSQL.append(", SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F9 : オペレーター：
    sbSQL.append(", ADDDT"); // F10 : 登録日：
    sbSQL.append(", UPDDT"); // F11 : 更新日：
    sbSQL.append(") values (");
    sbSQL.append("  RE.BMNCD"); // F1 : 部門：
    sbSQL.append(", RE.MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", RE.MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", RE.MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", RE.SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", RE.SRYPTNKN"); // F6 : 数量パターン名称：
    sbSQL.append(", RE.UPDKBN"); // F7 : 更新区分：
    sbSQL.append(", RE.SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", RE.OPERATOR"); // F9 : オペレーター：
    sbSQL.append(", RE.ADDDT"); // F10 : 登録日：
    sbSQL.append(", RE.UPDDT"); // F11 : 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("臨時数量パターン");

    // // クリア
    // prmData = new ArrayList<String>();
    // valueData = new Object[]{};
    // values = "";

    return sbSQL.toString();
  }


  /**
   * 数量ランク INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlSryrank(JSONArray dataArray, JSONArray dataArrayRanksuryo, HashMap<String, String> map, User userInfo, String sysdate) {

    String dbsysdate = CmnDate.dbDateFormat(sysdate);
    new JSONObject();
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";

    map.get(DefineReport.ID_PARAM_OBJ);
    new JSONObject();
    new JSONArray();

    // パラメータ確認
    int kryoColNum = 8; // テーブル列数
    // ログインユーザー情報取得
    int userId = userInfo.getCD_user(); // ログインユーザー
    values = "";
    // 更新情報
    for (int j = 0; j < dataArrayRanksuryo.size(); j++) {
      for (int i = 1; i <= kryoColNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(0).optString(col);
        if (i == 2) { // F2 : 数量パターンNo.：
          val = dataArray.optJSONObject(0).optString("F4");
        } else if (i == 3) { // F3 : 店ランク：
          val = dataArrayRanksuryo.optJSONObject(j).optString("F1");
        } else if (i == 4) { // F4 : 数量：
          val = dataArrayRanksuryo.optJSONObject(j).optString("F2");
        } else if (i == 5) { // F5 : 送信フラグ：
          val = "0";
        } else if (i == 6) { // F6 : オペレータ
          val = "" + userId;
        } else if (i == 7) { // F7 : 登録日
          val = dbsysdate;
        } else if (i == 8) { // F8 : 更新日
          val = dbsysdate;
        }
        if (isTest) {
          if (i == 1) { // F1 : 部門：
            if (j != 0) {
              values += ",";
            }
            values += "( '" + val + "'";
          } else if (i == 8) { // F8 : 更新日
            values += ", '" + val + "')";
          } else {
            values += ", '" + val + "'";
          }
        } else {
          prmData.add(val);
          values += ", ?";
        }
      }
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    // 更新SQL
    sbSQL = new StringBuffer();
    sbSQL.append("merge into INATK.TOKSRYRANK as T using (select");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", SRYPTNNO"); // F2 : 数量パターンNo.：
    sbSQL.append(", TENRANK"); // F3 : 店ランク：
    sbSQL.append(", SURYO"); // F4 : 数量：
    sbSQL.append(", SENDFLG"); // F5 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F6 : オペレータ
    sbSQL.append(", ADDDT"); // F7 : 登録日
    sbSQL.append(", UPDDT"); // F8 : 更新日
    sbSQL.append(" from (values " + values + " ) as T1(");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", SRYPTNNO"); // F2 : 数量パターンNo.：
    sbSQL.append(", TENRANK"); // F3 : 店ランク：
    sbSQL.append(", SURYO"); // F4 : 数量：
    sbSQL.append(", SENDFLG"); // F5 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F6 : オペレータ
    sbSQL.append(", ADDDT"); // F7 : 登録日
    sbSQL.append(", UPDDT"); // F8 : 更新日
    sbSQL.append("))as RE on (T.BMNCD = RE.BMNCD and T.SRYPTNNO = RE.SRYPTNNO and T.TENRANK = RE.TENRANK)");
    sbSQL.append(" when matched then update set");
    sbSQL.append("  BMNCD = RE.BMNCD"); // F1 : 部門：
    sbSQL.append(", SRYPTNNO = RE.SRYPTNNO"); // F2 : 数量パターンNo.：
    sbSQL.append(", TENRANK = RE.TENRANK"); // F3 : 店ランク：
    sbSQL.append(", SURYO = RE.SURYO"); // F4 : 数量：
    sbSQL.append(", SENDFLG = RE.SENDFLG"); // F5 : 送信フラグ：
    sbSQL.append(", OPERATOR = RE.OPERATOR"); // F6 : オペレータ
    sbSQL.append(", UPDDT = RE.UPDDT"); // F8 : 更新日
    sbSQL.append(" when not matched then insert(");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", SRYPTNNO"); // F2 : 数量パターンNo.：
    sbSQL.append(", TENRANK"); // F3 : 店ランク：
    sbSQL.append(", SURYO"); // F4 : 数量：
    sbSQL.append(", SENDFLG"); // F5 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F6 : オペレータ
    sbSQL.append(", ADDDT"); // F7 : 登録日
    sbSQL.append(", UPDDT"); // F8 : 更新日
    sbSQL.append(") values (");
    sbSQL.append("  RE.BMNCD"); // F1 : 部門：
    sbSQL.append(", RE.SRYPTNNO"); // F2 : 数量パターンNo.：
    sbSQL.append(", RE.TENRANK"); // F3 : 店ランク：
    sbSQL.append(", RE.SURYO"); // F4 : 数量：
    sbSQL.append(", RE.SENDFLG"); // F5 : 送信フラグ：
    sbSQL.append(", RE.OPERATOR"); // F6 : オペレータ
    sbSQL.append(", RE.ADDDT"); // F7 : 登録日
    sbSQL.append(", RE.UPDDT"); // F8 : 更新日
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("数量ランク");

    // // クリア
    // prmData = new ArrayList<String>();
    // valueData = new Object[]{};
    // values = "";

    return sbSQL.toString();
  }

  /**
   * 臨時数量ランク INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlSryrankex(JSONArray dataArray, JSONArray dataArrayRanksuryo, HashMap<String, String> map, User userInfo, String sysdate) {

    String dbsysdate = CmnDate.dbDateFormat(sysdate);
    new JSONObject();
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";

    map.get(DefineReport.ID_PARAM_OBJ);
    new JSONObject();
    new JSONArray();

    // パラメータ確認
    int kryoColNum = 11; // テーブル列数
    // ログインユーザー情報取得
    int userId = userInfo.getCD_user(); // ログインユーザー
    values = "";
    // 更新情報
    for (int j = 0; j < dataArrayRanksuryo.size(); j++) {
      for (int i = 1; i <= kryoColNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(0).optString(col);
        String moyscd = dataArray.optJSONObject(0).optString("F3");
        if (i == 2) { // F2 : 催し区分：
          val = StringUtils.substring(moyscd, 0, 1);
        } else if (i == 3) { // F3 : 催し開始日：
          val = StringUtils.substring(moyscd, 1, 7);
        } else if (i == 4) { // F4 : 催し連番：
          val = StringUtils.substring(moyscd, 7, 10);
        } else if (i == 5) { // F5 : 数量パターンNo.：
          val = dataArray.optJSONObject(0).optString("F4");
        } else if (i == 6) { // F6 : 店ランク：
          val = dataArrayRanksuryo.optJSONObject(j).optString("F1");
        } else if (i == 7) { // F7 : 数量：
          val = dataArrayRanksuryo.optJSONObject(j).optString("F2");
        } else if (i == 8) { // F8 : 送信フラグ：
          val = "0";
        } else if (i == 9) { // F9 : オペレータ
          val = "" + userId;
        } else if (i == 10) { // F10 : 登録日
          val = dbsysdate;
        } else if (i == 11) { // F11 : 更新日
          val = dbsysdate;
        }
        if (isTest) {
          if (i == 1) { // F1 : 部門：
            if (j != 0) {
              values += ",";
            }
            values += "( '" + val + "'";
          } else if (i == 11) { // F8 : 更新日
            values += ", '" + val + "')";
          } else {
            values += ", '" + val + "'";
          }
        } else {
          prmData.add(val);
          values += ", ?";
        }
      }
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    // 更新SQL
    sbSQL = new StringBuffer();
    sbSQL.append("merge into INATK.TOKSRYRANKEX as T using (select");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", TENRANK"); // F6 : 店ランク：
    sbSQL.append(", SURYO"); // F7 : 数量：
    sbSQL.append(", SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F9 : オペレータ
    sbSQL.append(", ADDDT"); // F10 : 登録日
    sbSQL.append(", UPDDT"); // F11 : 更新日
    sbSQL.append(" from (values " + values + " ) as T1(");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", TENRANK"); // F6 : 店ランク：
    sbSQL.append(", SURYO"); // F7 : 数量：
    sbSQL.append(", SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F9 : オペレータ
    sbSQL.append(", ADDDT"); // F10 : 登録日
    sbSQL.append(", UPDDT"); // F11 : 更新日
    sbSQL.append("))as RE on (T.BMNCD = RE.BMNCD and T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.SRYPTNNO = RE.SRYPTNNO and T.TENRANK = RE.TENRANK)");
    sbSQL.append(" when matched then update set");
    sbSQL.append("  BMNCD = RE.BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN = RE.MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT = RE.MOYSSTDT"); // F3 : 催し開始日：
    sbSQL.append(", MOYSRBAN = RE.MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO = RE.SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", TENRANK = RE.TENRANK"); // F6 : 店ランク：
    sbSQL.append(", SURYO = RE.SURYO"); // F7 : 数量：
    sbSQL.append(", SENDFLG = RE.SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR = RE.OPERATOR"); // F9 : オペレータ
    sbSQL.append(", UPDDT = RE.UPDDT"); // F11 : 更新日
    sbSQL.append(" when not matched then insert(");
    sbSQL.append("  BMNCD"); // F1 : 部門：
    sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日
    sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", TENRANK"); // F6 : 店ランク：
    sbSQL.append(", SURYO"); // F7 : 数量：
    sbSQL.append(", SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F9 : オペレータ
    sbSQL.append(", ADDDT"); // F10 : 登録日
    sbSQL.append(", UPDDT"); // F11 : 更新日
    sbSQL.append(") values (");
    sbSQL.append("  RE.BMNCD"); // F1 : 部門：
    sbSQL.append(", RE.MOYSKBN"); // F2 : 催し区分：
    sbSQL.append(", RE.MOYSSTDT"); // F3 : 催し開始日
    sbSQL.append(", RE.MOYSRBAN"); // F4 : 催し連番：
    sbSQL.append(", RE.SRYPTNNO"); // F5 : 数量パターンNo.：
    sbSQL.append(", RE.TENRANK"); // F6 : 店ランク：
    sbSQL.append(", RE.SURYO"); // F7 : 数量：
    sbSQL.append(", RE.SENDFLG"); // F8 : 送信フラグ：
    sbSQL.append(", RE.OPERATOR"); // F9 : オペレータ
    sbSQL.append(", RE.ADDDT"); // F10 : 登録日
    sbSQL.append(", RE.UPDDT"); // F11 : 更新日
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("臨時数量ランク");

    // // クリア
    // prmData = new ArrayList<String>();
    // valueData = new Object[]{};
    // values = "";

    return sbSQL.toString();
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(通常率パターン)

    new ArrayList<String>();
    new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    if (StringUtils.equals("0", data.optString("F2"))) {
      // 数量パターンINSERT/UPDATE処理
      createDelSqlSrptn(data, map, userInfo);
    } else {
      // 臨時数量パターンINSERT/UPDATE処理
      createDelSqlSrptnex(data, map, userInfo);
    }

    // // 排他チェック実行
    // if(!StringUtils.isEmpty(data.optString("F1")) && !StringUtils.isEmpty(data.optString("F2"))){
    // targetTable = "INATK.TOKRTPTN";
    // targetWhere = " BMNCD=? and RTPTNNO=?";
    // targetParam.add(data.optString("F1"));
    // targetParam.add(data.optString("F2"));
    // if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F5"))){
    // msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
    // option.put(MsgKey.E.getKey(), msg);
    // return option;
    // }
    // }

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
   * 数量パターンDELETE(論理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlSrptn(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    sqlWhere += " where BMNCD=?";
    sqlWhere += " and SRYPTNNO=?";
    paramData.add(data.optString("F1")); // 部門
    paramData.add(data.optString("F4")); // 数量パターンNo.

    // 数量パターンの論理削除
    sbSQL.append("update INATK.TOKSRPTN");
    sbSQL.append(" set");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("数量パターン");

    return sbSQL.toString();
  }

  /**
   * 臨時数量パターンDELETE(論理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlSrptnex(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    sqlWhere += " where BMNCD=?";
    sqlWhere += " and MOYSKBN=?";
    sqlWhere += " and MOYSSTDT=?";
    sqlWhere += " and MOYSRBAN=?";
    sqlWhere += " and SRYPTNNO=?";
    paramData.add(data.optString("F1")); // 部門
    paramData.add(StringUtils.substring(data.optString("F3"), 0, 1)); // 催し区分
    paramData.add(StringUtils.substring(data.optString("F3"), 1, 7)); // 催し開始日
    paramData.add(StringUtils.substring(data.optString("F3"), 7, 10)); // 催し連番
    paramData.add(data.optString("F4")); // 数量パターンNo.

    // 数量パターンの論理削除
    sbSQL.append("update INATK.TOKSRPTNEX");
    sbSQL.append(" set");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("数量パターン");

    return sbSQL.toString();
  }

  /**
   * チェック処理
   *
   * @param map
   * @return
   */
  @SuppressWarnings("static-access")
  public JSONArray check(HashMap<String, String> map) {
    // 関連情報取得
    ItemList iL = new ItemList();
    // パラメータ確認
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray dataArrayRanksuryo = JSONArray.fromObject(map.get("DATA_RANKSURYO")); // 更新情報
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";

    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 入力値を取得
    JSONObject data = dataArray.getJSONObject(0);

    // チェック処理
    // 新規登録重複チェック
    if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
      if (dataArray.size() > 0) {
        paramData = new ArrayList<String>();
        if (StringUtils.equals("0", data.getString("F2"))) {
          paramData.add(data.getString("F1"));
          paramData.add(data.getString("F4"));
          sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKSRPTN where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and BMNCD = ? and SRYPTNNO = ?";

          JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
          if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
            JSONObject o = mu.getDbMessageObj("E00004", "数量パターン");
            msg.add(o);
            return msg;
          }

        } else {
          paramData = new ArrayList<String>();
          paramData.add(data.getString("F1"));
          paramData.add(StringUtils.substring(data.getString("F3"), 0, 1));
          paramData.add(StringUtils.substring(data.getString("F3"), 1, 7));
          paramData.add(StringUtils.substring(data.getString("F3"), 7, 10));
          paramData.add(data.getString("F4"));
          sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKSRPTNEX where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal()
              + " and BMNCD = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and SRYPTNNO = ?";

          JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
          if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
            JSONObject o = mu.getDbMessageObj("E00004", "臨時数量パターン");
            msg.add(o);
            return msg;
          }
        }
        if (dataArrayRanksuryo.size() > 0) {
          if (StringUtils.equals("0", data.getString("F2"))) {
            for (int j = 0; j < dataArrayRanksuryo.size(); j++) {
              paramData = new ArrayList<String>();
              String rank = dataArrayRanksuryo.getJSONObject(j).getString("F1");
              paramData.add(data.getString("F1"));
              paramData.add(data.getString("F4"));
              paramData.add(rank);
              sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKSRYRANK where BMNCD = ? and SRYPTNNO = ? and TENRANK = ?";

              JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
              if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
                JSONObject o = mu.getDbMessageObj("E00004", "数量ランク");
                msg.add(o);
                return msg;
              }
            }
          } else {
            for (int j = 0; j < dataArrayRanksuryo.size(); j++) {
              paramData = new ArrayList<String>();
              String rank = dataArrayRanksuryo.getJSONObject(j).getString("F1");
              paramData.add(data.getString("F1"));
              paramData.add(StringUtils.substring(data.getString("F3"), 0, 1));
              paramData.add(StringUtils.substring(data.getString("F3"), 1, 7));
              paramData.add(StringUtils.substring(data.getString("F3"), 7, 10));
              paramData.add(data.getString("F4"));
              paramData.add(rank);
              sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKSRYRANKEX where BMNCD = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and SRYPTNNO = ? and TENRANK = ?";

              JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
              if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
                JSONObject o = mu.getDbMessageObj("E00004", "臨時数量ランク");
                msg.add(o);
                return msg;
              }
            }
          }
        }
      }
    }
    return msg;
  }
}
