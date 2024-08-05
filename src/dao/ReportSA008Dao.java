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
import authentication.defines.Consts;
import common.CmnDate;
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
public class ReportSA008Dao extends ItemDao {

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
  public ReportSA008Dao(String JNDIname) {
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

    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = getMap().get("MOYSRBAN"); // 催し連番
    getMap().get("PUSHBTNID");
    getMap().get("QASYUKBN");
    getMap().get("BMN[]");
    String szTengpcd = getMap().get("TENGPCD"); // 店グループコード

    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    ArrayList<String> paramData = new ArrayList<String>();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    StringBuffer sbSQL = new StringBuffer();
    // 一覧表情報
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select  ");
    sbSQL.append(" CONCAT(CONCAT(left(TTSH.SHNCD,4),'-'),SUBSTRING(TTSH.SHNCD,5,4)) ");// F1
    sbSQL.append("  , ( case when TTSH.SANCHIKN <> null then CONCAT(CONCAT(TTSH.SANCHIKN, TTSH.POPKN), TTSH.KIKKN) else CONCAT(CONCAT(TTSH.MAKERKN, TTSH.POPKN), TTSH.KIKKN) END ) ");// F2
    sbSQL.append("  , TTSH.CHIRASFLG  ");// F3
    sbSQL.append("  , TTSH.HBSLIDEFLG ");// F4
    sbSQL.append("  , TO_CHAR(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    TO_CHAR(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD')))");// F5
    sbSQL.append("  , (case when TTSH.TKANPLUKBN = 2 then TTSH.GENKAAM_1KG else TTSH.GENKAAM_MAE END) ");// F6
    sbSQL.append("  , (case when TTSH.TKANPLUKBN = 2 then 'G' else '' END) ");// F7
    sbSQL.append("  , (case when TTQS.URISELKBN = 1 then '1' else '0' END)");// F8
    sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_A_BAIKAAN > 0 then TO_CHAR(TTSH.KO_A_BAIKAAN) else TO_CHAR(TTSH.A_BAIKAAM) END)");
    sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = cast(TTSH.A_WRITUKBN as char)) END)");// F9
    sbSQL.append("  , (case when TTSH.BD1_A_BAIKAAN = 0 or TTSH.BD1_A_BAIKAAN = null then '' else TO_CHAR(TTSH.BD1_A_BAIKAAN) END ) ");// F10
    sbSQL.append("  , (case when TTSH.BD2_A_BAIKAAN = 0 or TTSH.BD2_A_BAIKAAN = null then '' else TO_CHAR(TTSH.BD2_A_BAIKAAN) END ) ");// F11
    sbSQL.append("  , (case when TTQS.URISELKBN = 2 then '1' else '0' END)");// F12
    sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_B_BAIKAAN > 0 then TO_CHAR(TTSH.KO_B_BAIKAAN) else TO_CHAR(TTSH.B_BAIKAAM) END)");
    sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = cast(TTSH.B_WRITUKBN as char)) END)");// F13
    sbSQL.append("  , (case when TTSH.BD1_B_BAIKAAN = 0 or TTSH.BD1_B_BAIKAAN = null then '' else TO_CHAR(TTSH.BD1_B_BAIKAAN) END ) ");// F14
    sbSQL.append("  , (case when TTSH.BD2_B_BAIKAAN = 0 or TTSH.BD2_B_BAIKAAN = null then '' else TO_CHAR(TTSH.BD2_B_BAIKAAN) END ) ");// F15
    sbSQL.append("  , (case when TTQS.URISELKBN = 3 then '1' else '0' END)");// F16
    sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_C_BAIKAAN > 0 then TO_CHAR(TTSH.KO_C_BAIKAAN) else TO_CHAR(TTSH.C_BAIKAAM) END)");
    sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = cast(TTSH.C_WRITUKBN as char)) END)");// F17
    sbSQL.append("  , (case when TTSH.BD1_C_BAIKAAN = 0 or TTSH.BD1_C_BAIKAAN = null then '' else TO_CHAR(TTSH.BD1_C_BAIKAAN) END ) ");// F18
    sbSQL.append("  , (case when TTSH.BD2_C_BAIKAAN = 0 or TTSH.BD2_C_BAIKAAN = null then '' else TO_CHAR(TTSH.BD2_C_BAIKAAN) END ) ");// F19
    sbSQL.append("  , TTQS.BMNCD  ");// F20
    sbSQL.append("  , TTQS.KANRINO  ");// F21
    sbSQL.append("  , TTQS.KANRIENO  ");// F22
    sbSQL.append("  , TTSH.A_BAIKAAM  ");// F23
    sbSQL.append("  , TTSH.B_BAIKAAM  ");// F24
    sbSQL.append("  , TTSH.C_BAIKAAM  ");// F25
    sbSQL.append(" from ");
    sbSQL.append(" INATK.TOKTG_SHN TTSH ");
    sbSQL.append(" left join INATK.TOKTG_KHN TTKH");
    sbSQL.append(" on TTKH.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTKH.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTKH.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" left join INATK.TOKTG_QAGP TTQG  ");
    sbSQL.append(" on TTQG.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTQG.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTQG.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTQG.TENGPCD = ? ");
    paramData.add(szTengpcd);
    sbSQL.append(" left join INATK.TOKMOYCD TMYC ");
    sbSQL.append(" on TMYC.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TMYC.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TMYC.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" left join INATK.TOKTG_QASHN TTQS ");
    sbSQL.append(" on TTQS.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTQS.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTQS.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTQS.TENGPCD = ? ");
    paramData.add(szTengpcd);
    sbSQL.append(" and TTQS.KANRINO = TTSH.KANRINO");
    sbSQL.append(" and TTQS.BMNCD = TTSH.BMNCD ");
    sbSQL.append(" and TTQS.KANRIENO = TTSH.KANRIENO ");
    sbSQL.append(" where ");
    sbSQL.append(" TTSH.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTSH.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTSH.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTKH.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTKH.MOYSSTDT =  ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTKH.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTQG.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTQG.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTQG.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TMYC.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TMYC.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TMYC.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTQS.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append(" and TTQS.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append(" and TTQS.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTQS.TENGPCD = ? ");
    paramData.add(szTengpcd);
    sbSQL.append(" and TTQS.KANRINO = TTSH.KANRINO");
    sbSQL.append(" and TTQS.BMNCD = TTSH.BMNCD ");
    sbSQL.append(" and TTQS.KANRIENO = TTSH.KANRIENO");
    sbSQL.append(" and TTQS.UPDKBN = 0");
    sbSQL.append(" order by TTQS.CSVSEQNO");

    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
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
  }


  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番
    String szTENCD = map.get("TENCD"); // 催し連番
    String szTengpcd = getMap().get("TENGPCD"); // 店グループコード

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append("    MTTN.TENKN as F1");
    sbSQL.append("  , TTTG.TENGPKN as F2");
    sbSQL.append("  , TTQT.TENCD as F3");
    sbSQL.append("  , TO_CHAR(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    TO_CHAR(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'))) as F4");
    sbSQL.append("  , TMYC.MOYKN as F5");
    sbSQL.append("  , TTQG.LDSYFLG as F6");
    sbSQL.append("  , TTQG.URIASELKBN as F7");
    sbSQL.append("  , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 then '〇' else '×' END) as F8");
    sbSQL.append("  , TTKH.TPNG2FLG as F9");
    sbSQL.append("  , TTKH.TPNG1FLG as F10");
    sbSQL.append("  , TTKH.TPNG3FLG as F11");
    sbSQL.append("  , TTKH.HBOKUREFLG as F12");
    sbSQL.append("  , TTKH.JLSTCREFLG  as F13");
    sbSQL.append("  , TO_CHAR(TTQG.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as F14 ");
    sbSQL.append("  from ");
    sbSQL.append("  INATK.TOKTG_QATEN TTQT");
    sbSQL.append("  left join INAMS.MSTTEN MTTN");
    sbSQL.append("  on MTTN.TENCD = ? ");
    paramData.add(szTENCD);
    sbSQL.append("  left  join INATK.TOKTG_TENGP TTTG");
    sbSQL.append("  on TTTG.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append("  and TTTG.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and TTTG.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append(" and TTTG.TENGPCD = TTQT.TENGPCD");
    sbSQL.append(" left join INATK.TOKMOYCD TMYC");
    sbSQL.append(" on TMYC.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append("  and TMYC.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and TMYC.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append("  left join INATK.TOKTG_QAGP TTQG");
    sbSQL.append("  on TTQG.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append("  and TTQG.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and TTQG.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append("  and TTQG.TENGPCD = ? ");
    paramData.add(szTengpcd);
    sbSQL.append("  left join INATK.TOKTG_KHN TTKH");
    sbSQL.append("  on TTKH.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append("  and TTKH.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and TTKH.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append("  where");
    sbSQL.append("  TTQT.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and TTQT.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append("  and TTQT.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append("  and TTQT.TENCD = ? ");
    paramData.add(szTENCD);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
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
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map, userInfo, sysdate);

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
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("SHNDATA")); // 更新情報

    MessageUtility mu = new MessageUtility();

    List<JSONObject> msgList = this.checkData(map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }



  /**
   * 売価チェック処理
   *
   * @throws Exception
   */

  public List<JSONObject> checkData(HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）
  ) {
    JSONArray msg = new JSONArray();
    dataArray.optJSONObject(0);

    // // 更新情報
    // if(!data.isEmpty()){
    // String val1 = data.optString("F1");
    // String val2 = data.optString("F2");
    // String val3 = data.optString("F3");
    // if( (String.valueOf(val1).equals("1") && String.valueOf(val2).equals("1")) ||
    // (String.valueOf(val1).equals("1") && String.valueOf(val3).equals("1"))
    // || (String.valueOf(val2).equals("1") && String.valueOf(val3).equals("1")) ||
    // (String.valueOf(val1).equals("1") && String.valueOf(val2).equals("1") &&
    // String.valueOf(val3).equals("1"))){
    // JSONObject o = mu.getDbMessageObj("EX1089", new String[]{});
    // msg.add(o);
    // return msg;
    // }
    // }
    return msg;
  }

  boolean isTest = false;

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {

    CmnDate.dbDateFormat(sysdate);

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";
    int kryoColNum = 7; // テーブル列数
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    String TPNG2FLG = dataArray.optJSONObject(0).optString("F9");
    String ADOPT = map.get("ADOPT"); // 採用区分
    String TENGPCD = map.get("TENGPCD"); // 店グループコード
    String MOYSKBN = map.get("MOYSKBN"); // 催し区分
    String MOYSSTDT = map.get("MOYSSTDT"); // 催し開始日
    String MOYSRBAN = map.get("MOYSRBAN"); // 催し連番

    JSONObject msgObj = new JSONObject();
    JSONArray msg = new JSONArray();
    JSONObject option = new JSONObject();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String updateRows = ""; // 更新データ

    // 更新情報
    values = "";
    for (int i = 1; i <= kryoColNum; i++) {
      if (isTest) {
        if (i == 1) {
          values += " '" + ADOPT + "'";
          // LDSYFLG
        } else if (i == 2) {
          // SCHANSFLG
          values += ", '" + 1 + "'";
        } else if (i == 3) {
          // ITMANSFLG
          values += ", '" + 1 + "'";
        } else if (i == 4) {
          // MOYSKBN
          values += ", '" + MOYSKBN + "'";
        } else if (i == 5) {
          // MOYSSTDT
          values += ", '" + MOYSSTDT + "'";
        } else if (i == 6) {
          // MOYSRBAN
          values += ", '" + MOYSRBAN + "'";
        } else if (i == 7) {
          // TENGPCD
          values += ", '" + TENGPCD + "'";
        }
      } else {
        if (i == 1) {
          prmData.add(ADOPT);
          // LDSYFLG
        } else if (i == 2) {
          // SCHANSFLG
          prmData.add("1");
        } else if (i == 3) {
          // ITMANSFLG
          prmData.add("1");
        } else if (i == 4) {
          // MOYSKBN
          prmData.add(MOYSKBN);
        } else if (i == 5) {
          // MOYSSTDT
          prmData.add(MOYSSTDT);
        } else if (i == 6) {
          // MOYSRBAN
          prmData.add(MOYSRBAN);
        } else if (i == 7) {
          // TENGPCD
          prmData.add(TENGPCD);
        }

        values += ", ?";
      }
    }
    updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";
    updateRows = StringUtils.removeStart(updateRows, ",");

    if (values.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return msgObj;
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL = new StringBuffer();;

    sbSQL.append("MERGE INTO INATK.TOKTG_QAGP AS T USING (SELECT ");
    sbSQL.append("  LDSYFLG");
    sbSQL.append(", SCHANSFLG");
    sbSQL.append(", ITMANSFLG");
    sbSQL.append(", MOYSKBN");
    sbSQL.append(", MOYSSTDT");
    sbSQL.append(", MOYSRBAN");
    sbSQL.append(", TENGPCD");
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN");
    sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " AS SENDFLG");
    sbSQL.append(", '" + userId + "' AS OPERATOR ");
    sbSQL.append(", current timestamp AS ADDDT ");
    sbSQL.append(", current timestamp AS UPDDT ");
    sbSQL.append(" FROM (values " + updateRows + ") as T1(");
    sbSQL.append(" LDSYFLG ");
    sbSQL.append(" ,SCHANSFLG ");
    sbSQL.append(" ,ITMANSFLG ");
    sbSQL.append(" ,MOYSKBN ");
    sbSQL.append(" ,MOYSSTDT ");
    sbSQL.append(" ,MOYSRBAN ");
    sbSQL.append(" ,TENGPCD ");
    sbSQL.append(" )) as RE on (");
    sbSQL.append(" T.MOYSKBN = RE.MOYSKBN and ");
    sbSQL.append(" T.MOYSSTDT = RE.MOYSSTDT and ");
    sbSQL.append(" T.MOYSRBAN = RE.MOYSRBAN and ");
    sbSQL.append(" T.TENGPCD = RE.TENGPCD ");
    sbSQL.append(" ) WHEN MATCHED THEN UPDATE SET ");
    sbSQL.append("  LDSYFLG = RE.LDSYFLG");
    sbSQL.append(", SCHANSFLG = RE.SCHANSFLG");
    sbSQL.append(", ITMANSFLG = RE.ITMANSFLG");
    sbSQL.append(", SENDFLG=RE.SENDFLG ");
    sbSQL.append(", OPERATOR = RE.OPERATOR");
    sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
    sbSQL.append(", UPDDT = RE.UPDDT");
    if (msg.size() > 0) {
      // 重複チェック_エラー時
      msgObj.put(MsgKey.E.getKey(), msg);
    } else {
      // 更新処理実行
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(sbSQL);

      // 排他チェック実行
      String targetTable = null;
      String targetWhere = null;
      ArrayList<String> targetParam = new ArrayList<String>();
      // ⑨CSVエラー修正

      // 排他チェック：大分類グリッド
      if (dataArray.size() > 0) {
        String rownum = ""; // エラー行数

        for (int i = 0; i < dataArray.size(); i++) {
          JSONObject data = dataArray.getJSONObject(i);
          if (data.isEmpty()) {
            continue;
          }

          targetTable = "INATK.TOKTG_QAGP";
          targetWhere = "MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and TENGPCD = ? ";
          targetParam = new ArrayList<String>();
          targetParam.add(MOYSKBN);
          targetParam.add(MOYSSTDT);
          targetParam.add(MOYSRBAN);
          targetParam.add(TENGPCD);

          if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F14"))) {
            // rownum = data.optString("idx");
            rownum = String.valueOf(i + 1);
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[] {rownum}));
            option.put(MsgKey.E.getKey(), msg);
            return option;
          }
        }
      }

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("アンケート結果_店グループ");


      prmData = new ArrayList<String>();
      JSONArray shnDataArray = JSONArray.fromObject(map.get("SHNDATA")); // 対象情報
      int ColNum = 10; // テーブル列数
      updateRows = "";
      // 更新情報
      for (int j = 0; j < shnDataArray.size(); j++) {
        String hasValue = " '" + 0 + "'";
        values = "";
        for (int i = 1; i <= ColNum; i++) {
          String col = "F" + i;
          String val = shnDataArray.optJSONObject(j).optString(col);

          if (i == 1) {
            if (String.valueOf(val).equals("1")) {
              hasValue = "1";
            }
            // 希望総売価(1売り)
          } else if (i == 2) {
            // 希望総売価（バンドル１）
            if (String.valueOf(val).equals("1")) {
              hasValue = "2";
            }
          } else if (i == 3) {
            // 希望総売価（バンドル２）
            if (String.valueOf(val).equals("1")) {
              hasValue = "3";
            }
            if (String.valueOf(TPNG2FLG).equals("1")) {
              hasValue = "1";
            }
            prmData.add(hasValue);
          } else if (i == 4) {
            // 催し区分
            prmData.add(MOYSKBN);
          } else if (i == 5) {
            // 催し開始日
            prmData.add(MOYSSTDT);
          } else if (i == 6) {
            // 催し連番
            prmData.add(MOYSRBAN);
          } else if (i == 7) {
            // 店グループコード
            prmData.add(TENGPCD);
          } else if (i == 8) {
            // 部門コード
            prmData.add(val);
          } else if (i == 9) {
            // 管理番号
            prmData.add(val);
          } else if (i == 10) {
            // 枝番
            prmData.add(val);
          }
          if (i != 2 && i != 1) {
            values += ", ?";
          }
        }
        updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";
      }
      updateRows = StringUtils.removeStart(updateRows, ",");

      // 基本INSERT/UPDATE文
      StringBuffer sbSQL2 = new StringBuffer();;

      sbSQL2.append("MERGE INTO INATK.TOKTG_QASHN AS T USING (SELECT ");
      sbSQL2.append("  URISELKBN");
      sbSQL2.append(", MOYSKBN");
      sbSQL2.append(", MOYSSTDT");
      sbSQL2.append(", MOYSRBAN");
      sbSQL2.append(", TENGPCD");
      sbSQL2.append(", BMNCD");
      sbSQL2.append(", KANRINO");
      sbSQL2.append(", KANRIENO");
      sbSQL2.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN");
      sbSQL2.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " AS SENDFLG");
      sbSQL2.append(", '" + userId + "' AS OPERATOR ");
      sbSQL2.append(", current timestamp AS ADDDT ");
      sbSQL2.append(", current timestamp AS UPDDT ");
      sbSQL2.append(" FROM (values " + updateRows + ") as T1(");
      sbSQL2.append("  URISELKBN");
      sbSQL2.append(", MOYSKBN");
      sbSQL2.append(", MOYSSTDT");
      sbSQL2.append(", MOYSRBAN");
      sbSQL2.append(", TENGPCD");
      sbSQL2.append(", BMNCD");
      sbSQL2.append(", KANRINO");
      sbSQL2.append(", KANRIENO");
      sbSQL2.append(")) as RE on (");
      sbSQL2.append("T.MOYSKBN = RE.MOYSKBN and ");
      sbSQL2.append("T.MOYSSTDT = RE.MOYSSTDT and ");
      sbSQL2.append("T.MOYSRBAN = RE.MOYSRBAN and ");
      sbSQL2.append("T.TENGPCD = RE.TENGPCD and ");
      sbSQL2.append("T.BMNCD = RE.BMNCD and ");
      sbSQL2.append("T.KANRINO = RE.KANRINO and ");
      sbSQL2.append("T.KANRIENO = RE.KANRIENO ");
      sbSQL2.append(") WHEN MATCHED THEN UPDATE SET ");
      sbSQL2.append("  URISELKBN = RE.URISELKBN");
      sbSQL2.append(", SENDFLG=RE.SENDFLG ");
      sbSQL2.append(", OPERATOR = RE.OPERATOR");
      sbSQL2.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
      sbSQL2.append(", UPDDT=RE.UPDDT ");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL2.toString());
      prmList.add(prmData);
      lblList.add("アンケート結果_商品");


      // 登録処理
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

      /*
       * int count2 = super.executeSQL(sbSQL2.toString(), prmData); if(StringUtils.isEmpty(getMessage()))
       * { System.out.println("アンケート結果_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[]
       * { Integer.toString(dataArray.size()), Integer.toString(count) })); msgObj.put(MsgKey.S.getKey(),
       * MessageUtility.getMessage(Msg.S00001.getVal())); }else{ msgObj.put(MsgKey.E.getKey(),
       * getMessage()); }
       */


    }
    return msgObj;
  }
}
