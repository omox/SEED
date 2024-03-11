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
public class ReportSA003Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportSA003Dao(String JNDIname) {
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
    sbSQL.append("   select  ");
    sbSQL.append("   CONCAT(CONCAT(left(TTSH.SHNCD,4),'-'),SUBSTRING(TTSH.SHNCD,5,4)) ");// F1
    sbSQL.append("  , ( case when TTSH.SANCHIKN <> null then CONCAT(CONCAT(TTSH.SANCHIKN, TTSH.POPKN), TTSH.KIKKN) else CONCAT(CONCAT(TTSH.MAKERKN, TTSH.POPKN), TTSH.KIKKN) END ) ");// F2
    sbSQL.append("  , TTSH.CHIRASFLG  ");// F3
    sbSQL.append("  , TTSH.HBSLIDEFLG ");// F4
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(TMYC.HBSTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYC.HBSTDT, '%Y%m%d')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    DATE_FORMAT(DATE_FORMAT(TMYC.HBEDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYC.HBEDDT, '%Y%m%d'))) ");// F5
    sbSQL.append("  , ( case when TTSH.TKANPLUKBN = 2 then TTSH.GENKAAM_1KG else TTSH.GENKAAM_MAE END ) ");// F6
    sbSQL.append("  , ( case when TTSH.TKANPLUKBN = 2 then 'G' else '' END  ) ");// F7
    sbSQL.append("  , ( case when TTSH.ADDSHUKBN <> 01 then ( case when TTSH.BD1_A_BAIKAAN > 0 then TTSH.KO_A_BAIKAAN else TTSH.A_BAIKAAM END)");
    sbSQL.append("  else  (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char(TTSH.A_WRITUKBN) ) END)  ");// B割引始F8
    sbSQL.append("  , (case when TTSH.BD1_A_BAIKAAN = 0 or TTSH.BD1_A_BAIKAAN is null then '' else CONCAT(TTSH.BD1_TENSU,CONCAT('個/',TTSH.BD1_A_BAIKAAN)) END ) ");// F9
    sbSQL.append("  , (case when TTSH.BD2_A_BAIKAAN = 0 or TTSH.BD2_A_BAIKAAN is null then '' else CONCAT(TTSH.BD2_TENSU,CONCAT('個/',TTSH.BD2_A_BAIKAAN)) END ) ");// F10
    sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_B_BAIKAAN > 0 then TTSH.KO_B_BAIKAAN else TTSH.B_BAIKAAM END)");
    sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char (TTSH.B_WRITUKBN)) END)");// F11
    sbSQL.append("  , (case when TTSH.BD1_B_BAIKAAN = 0 or TTSH.BD1_B_BAIKAAN is null then '' else CONCAT(TTSH.BD1_TENSU,CONCAT('個/',TTSH.BD1_B_BAIKAAN)) END ) ");// F12
    sbSQL.append("  , (case when TTSH.BD2_B_BAIKAAN = 0 or TTSH.BD2_B_BAIKAAN is null then '' else CONCAT(TTSH.BD2_TENSU,CONCAT('個/',TTSH.BD2_B_BAIKAAN)) END ) ");// F13
    sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_C_BAIKAAN > 0 then TTSH.KO_C_BAIKAAN else TTSH.C_BAIKAAM END)");
    sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char (TTSH.C_WRITUKBN)) END)");// F14
    sbSQL.append("  , (case when TTSH.BD1_C_BAIKAAN = 0 or TTSH.BD1_C_BAIKAAN is null then '' else CONCAT(TTSH.BD1_TENSU,CONCAT('個/',TTSH.BD1_C_BAIKAAN)) END ) ");// F15
    sbSQL.append("  , (case when TTSH.BD2_C_BAIKAAN = 0 or TTSH.BD2_C_BAIKAAN is null then '' else CONCAT(TTSH.BD2_TENSU,CONCAT('個/',TTSH.BD2_C_BAIKAAN)) END ) ");// F16
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
    sbSQL.append(" and TTKH.MOYSSTDT = ? ");
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
    sbSQL.append(" order by TTQS.CSVSEQNO");

    setParamData(paramData);

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
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
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(TMYC.HBSTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYC.HBSTDT, '%Y%m%d')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    DATE_FORMAT(DATE_FORMAT(TMYC.HBEDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYC.HBEDDT, '%Y%m%d'))) as F4");
    sbSQL.append("  , TMYC.MOYKN as F5");
    sbSQL.append("  , TTQG.LDSYFLG as F6");
    sbSQL.append("  , TTQG.URIASELKBN as F7");
    sbSQL.append("  , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 then '〇' else '×' END) as F8");
    sbSQL.append("  , TTKH.TPNG2FLG as F9");
    sbSQL.append("  , TTKH.TPNG1FLG as F10");
    sbSQL.append("  , TTKH.TPNG3FLG as F11");
    sbSQL.append("  , TTKH.HBOKUREFLG as F12");
    sbSQL.append("  , TTKH.JLSTCREFLG  as F13");
    sbSQL.append("  , DATE_FORMAT(TTQG.UPDDT, '%Y%m%d%H%i%s%f') as F14 ");
    sbSQL.append("  from ");
    sbSQL.append("  INATK.TOKTG_KHN TTKH");
    sbSQL.append("  left join INATK.TOKTG_QAGP TTQG");
    sbSQL.append("  on TTQG.MOYSKBN = TTKH.MOYSKBN ");
    sbSQL.append("  and TTQG.MOYSSTDT = TTKH.MOYSSTDT ");
    sbSQL.append("  and TTQG.MOYSRBAN =TTKH.MOYSRBAN ");
    sbSQL.append("  and TTQG.TENGPCD = ? ");
    paramData.add(szTengpcd);
    sbSQL.append(" left join INATK.TOKTG_QATEN TTQT");
    sbSQL.append(" on TTKH.MOYSKBN = TTQT.MOYSKBN");
    sbSQL.append(" and TTKH.MOYSSTDT = TTQT.MOYSSTDT ");
    sbSQL.append(" and TTKH.MOYSRBAN = TTQT.MOYSRBAN");
    sbSQL.append(" left join INAMS.MSTTEN MTTN");
    sbSQL.append(" on MTTN.TENCD = ? ");
    paramData.add(szTENCD);
    sbSQL.append(" left join INATK.TOKTG_TENGP TTTG ");
    sbSQL.append(" on TTTG.MOYSKBN = TTKH.MOYSKBN ");
    sbSQL.append(" and TTTG.MOYSSTDT = TTKH.MOYSSTDT ");
    sbSQL.append(" and TTTG.MOYSRBAN = TTKH.MOYSRBAN ");
    sbSQL.append(" and TTTG.TENGPCD = TTQT.TENGPCD");
    sbSQL.append(" left join INATK.TOKMOYCD TMYC ");
    sbSQL.append(" on TMYC.MOYSKBN = TTKH.MOYSKBN ");
    sbSQL.append(" and TMYC.MOYSSTDT = TTKH.MOYSSTDT ");
    sbSQL.append(" and TMYC.MOYSRBAN = TTKH.MOYSRBAN ");
    sbSQL.append("  where");
    sbSQL.append("  TTKH.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and TTKH.MOYSRBAN = ? ");
    paramData.add(szMoysrban);
    sbSQL.append("  and TTKH.MOYSKBN = ? ");
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
    // msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
    return msgObj;
  }

  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    MessageUtility mu = new MessageUtility();

    List<JSONObject> msgList = this.checkData(map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public List<JSONObject> checkData(HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）
  ) {
    JSONArray msg = new JSONArray();
    JSONObject data = dataArray.optJSONObject(0);

    // 更新情報
    String val = data.optString("F13");
    if (String.valueOf(val).equals("1")) {
      JSONObject o = mu.getDbMessageObj("E20205", new String[] {});
      msg.add(o);
      return msg;
    }

    return msg;
  }

  boolean isTest = true;

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
    int kryoColNum = 8; // テーブル列数
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    String ADOPT = map.get("ADOPT"); // 採用区分
    String AURIASELKBN = map.get("AURIASELKBN"); // 総売価
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
          // URIASELKBN
          values += ", '" + AURIASELKBN + "'";
        } else if (i == 3) {
          // SCHANSFLG
          values += ", '" + 1 + "'";
        } else if (i == 4) {
          // ITMANSFLG
          values += ", '" + 1 + "'";
        } else if (i == 5) {
          // MOYSKBN
          values += ", '" + MOYSKBN + "'";
        } else if (i == 6) {
          // MOYSSTDT
          values += ", '" + MOYSSTDT + "'";
        } else if (i == 7) {
          // MOYSRBAN
          values += ", '" + MOYSRBAN + "'";
        } else if (i == 8) {
          // TENGPCD
          values += ", '" + TENGPCD + "'";
        }
      } else {
        if (i == 1) {
          prmData.add(ADOPT);
          // LDSYFLG
        } else if (i == 2) {
          // URIASELKBN
          prmData.add(AURIASELKBN);
        } else if (i == 3) {
          // SCHANSFLG
          prmData.add("1");
        } else if (i == 4) {
          // ITMANSFLG
          prmData.add("1");
        } else if (i == 5) {
          // MOYSKBN
          prmData.add(MOYSKBN);
        } else if (i == 6) {
          // MOYSSTDT
          prmData.add(MOYSSTDT);
        } else if (i == 7) {
          // MOYSRBAN
          prmData.add(MOYSRBAN);
        } else if (i == 8) {
          // TENGPCD
          prmData.add(TENGPCD);
        }
        values += ", ?";
      }
    }
    values += ", " + DefineReport.Values.SENDFLG_UN.getVal();
    values += ", '" + userId + "'";
    values += ", CURRENT_TIMESTAMP ";
    values += ", CURRENT_TIMESTAMP ";
    updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";
    updateRows = StringUtils.removeStart(updateRows, ",");

    if (values.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return msgObj;
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL = new StringBuffer();;

    sbSQL.append("REPLACE INTO INATK.TOKTG_QAGP ( ");
    sbSQL.append("  LDSYFLG"); // リーダー店採用フラグ：
    sbSQL.append(", URIASELKBN"); // 売価一括選択フラグ
    sbSQL.append(", SCHANSFLG"); // スケジュール回答フラグ
    sbSQL.append(", ITMANSFLG"); // アイテム回答フラグ：
    sbSQL.append(", MOYSKBN"); // 催し区分：
    sbSQL.append(", MOYSSTDT"); // 催し開始日：
    sbSQL.append(", MOYSRBAN"); // 催し連番：
    sbSQL.append(", TENGPCD"); // 店グループコード：
    sbSQL.append(", SENDFLG"); // 送信区分：
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(") VALUES ");
    sbSQL.append(updateRows);
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
            rownum = String.valueOf(i + 1);
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[] {rownum}));
            option.put(MsgKey.E.getKey(), msg);
            return option;
          }
        }
      }

      int count = super.executeSQL(sbSQL.toString(), prmData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("アンケート結果_店グループを " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }
    }
    return msgObj;
  }
}
