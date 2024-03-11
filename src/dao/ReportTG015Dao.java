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
public class ReportTG015Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG015Dao(String JNDIname) {
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
    ArrayList<String> paramData = new ArrayList<String>();
    String szQayyyymm = "20" + getMap().get("QAYYYYMM"); // メーカコード
    String szQaend = getMap().get("QAEND"); // メーカー名
    String szTencd = getMap().get("TENCD"); // チェック_メーカー名無し
    String ckKyosei = getMap().get("CHK_KYOSEI"); // チェック_メーカー

    if (ckKyosei == null) {
      ckKyosei = "0";
    }
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    String sqlWhere = "";
    JSONArray array = getTOKMOYCDData(getMap());
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    if (sendBtnid.equals("btn_sel_change") || sendBtnid.equals("btn_new") || sendBtnid.equals("btn_back") || sendBtnid.equals("btn_upd") || sendBtnid.equals("btn_cancel")) {
      // 一覧表情報
      sbSQL.append("select ");
      sbSQL.append("  CONCAT(CONCAT(CONCAT(CONCAT(TTQG.MOYSKBN, '-'), TTQG.MOYSSTDT),'-'),TTQG.MOYSRBAN) as MOYOOSICODE");// F1
      sbSQL.append(" ,TMYD.MOYKN");// F2
      sbSQL.append(" , (case when TTQG.LDSYFLG = 1  then '参加' else '不参加' END)");// F3
      sbSQL.append(" , (case when TTQG.QASYUKBN = 1  then '1' else '0' END)");// F4
      sbSQL.append(
          " , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 and TTQG.QASYUKBN = 1 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 1  then '×' else '' END)");// F5
      sbSQL.append(" , (case when TTQG.QASYUKBN = 2  then '1' else '0' END)");// F6
      sbSQL.append(
          " , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 2 and TTQG.QASYUKBN = 2 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 2  then '×' else '' END)");// F7
      sbSQL.append(" , (case when TTQG.QASYUKBN = 3  then '1' else '0' END)");// F8
      sbSQL.append(
          " , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 3 and TTQG.QASYUKBN = 3 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 3  then '×' else '' END)");// F9
      sbSQL.append(" , (case when TTQG.QASYUKBN = 4  then '1' else '0' END)");// F10
      sbSQL.append(
          " , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 4 and TTQG.QASYUKBN = 4 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 4  then '×' else '' END)");// F11
      sbSQL.append(" , TTKH.MOYSKBN");// F12
      sbSQL.append(" , TTKH.MOYSSTDT");// F13
      sbSQL.append(" , TTKH.MOYSRBAN");// F14
      sbSQL.append(" , TTQT.TENCD");// F15
      sbSQL.append(" , TTQG.QASYUKBN");// F16
      sbSQL.append(" , TTQT.KYOSEIFLG");// F17
      sbSQL.append(" from");
      sbSQL.append(" INATK.TOKTG_KHN TTKH");
      sbSQL.append(" left join");
      sbSQL.append(" INATK.TOKTG_QATEN TTQT");
      sbSQL.append(" on TTQT.MOYSKBN = TTKH.MOYSKBN");
      sbSQL.append(" and TTQT.MOYSSTDT = TTKH.MOYSSTDT");
      sbSQL.append(" and TTQT.MOYSRBAN = TTKH.MOYSRBAN");
      sbSQL.append(" and TTQT.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" and TTQT.KYOSEIFLG = ? ");
      paramData.add(ckKyosei);
      sbSQL.append(" and TTQT.LDTENKBN = 1");
      sbSQL.append(" left join");
      sbSQL.append(" INATK.TOKTG_QAGP TTQG");
      sbSQL.append(" on TTQG.MOYSKBN =   TTQT.MOYSKBN");
      sbSQL.append(" and TTQG.MOYSSTDT = TTQT.MOYSSTDT");
      sbSQL.append(" and TTQG.MOYSRBAN = TTQT.MOYSRBAN");
      sbSQL.append(" and TTQG.TENGPCD =  TTQT.TENGPCD");
      sbSQL.append(" left join");
      sbSQL.append(" INATK.TOKMOYCD TMYD");
      sbSQL.append(" on TMYD.MOYSKBN =   TTQG.MOYSKBN");
      sbSQL.append(" and TMYD.MOYSSTDT = TTQG.MOYSSTDT");
      sbSQL.append(" and TMYD.MOYSRBAN = TTQG.MOYSRBAN");
      sbSQL.append(" left join INAMS.MSTTEN MTTN ");
      sbSQL.append(" on MTTN.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" where ");
      sbSQL.append(" TTKH.QAYYYYMM = ? ");
      paramData.add(szQayyyymm);
      sbSQL.append(" and TTKH.QAENO = ? ");
      paramData.add(szQaend);
      sbSQL.append(" and TTQG.QASYUKBN IN (1,2,3,4)");
      sbSQL.append(" and TTQT.MOYSKBN = TTKH.MOYSKBN");
      sbSQL.append(" and TTQT.MOYSSTDT = TTKH.MOYSSTDT");
      sbSQL.append(" and TTQT.MOYSRBAN = TTKH.MOYSRBAN");
      sbSQL.append(" and TTQT.KYOSEIFLG = ? ");
      paramData.add(ckKyosei);
      sbSQL.append(" and TTQT.LDTENKBN = 1");
      sbSQL.append(" and TTQT.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" and TTQG.MOYSKBN =   TTQT.MOYSKBN");
      sbSQL.append(" and TTQG.MOYSSTDT = TTQT.MOYSSTDT");
      sbSQL.append(" and TTQG.MOYSRBAN = TTQT.MOYSRBAN");
      sbSQL.append(" and TTQG.TENGPCD =  TTQT.TENGPCD");
      sbSQL.append(" and TMYD.MOYSKBN =   TTQG.MOYSKBN");
      sbSQL.append(" and TMYD.MOYSSTDT = TTQG.MOYSSTDT");
      sbSQL.append(" and TMYD.MOYSRBAN = TTQG.MOYSRBAN");
      sbSQL.append(" and MTTN.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" and TTKH.UPDKBN = 0");
      sbSQL.append(" order by MOYOOSICODE ");
    } else {

      sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
      sbSQL.append("select ");
      sbSQL.append("  (case when TTQT.MBSYFLG = 2  then 0 else 1 END)");// F1
      sbSQL.append(" , TTKH.TPNG1FLG");// F2
      sbSQL.append(" , (case when TTQT.HBSTRTFLG = 0  then 1 else 0 END)");// F3
      sbSQL.append(" , (case when TTQT.HBSTRTFLG = 1  then 1 else 0 END)");// F4
      sbSQL.append(" , (case when TTKH.HBOKUREFLG = 1  then '有' when TTKH.HBOKUREFLG = 0  then '無' END)");// F5
      sbSQL.append(" , (case when TTQG.ITMANSFLG = 1  then '済み' when TTQG.ITMANSFLG = 0  then '未' END)");// F6
      sbSQL.append(" , TTKH.TPNG2FLG");// F7
      sbSQL.append(" , TTQT.MOYSKBN || '-' || right (TTQT.MOYSSTDT, 6) || '-' || right ('000' || TTQT.MOYSRBAN, 3) as MOYOOSICODE");// F8
      sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(TMYC.HBSTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYC.HBSTDT, '%Y%m%d')))");
      sbSQL.append("    ||'～'||");
      sbSQL.append("    DATE_FORMAT(DATE_FORMAT(TMYC.HBEDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYC.HBEDDT, '%Y%m%d')))");
      sbSQL.append(" , TMYC.MOYKN");// F10
      sbSQL.append(" , DATE_FORMAT(TTQT.ADDDT, '%y/%m/%d')");// F11
      sbSQL.append(" , DATE_FORMAT(TTQT.UPDDT, '%y/%m/%d')");// F12
      sbSQL.append(" , TTQT.OPERATOR");// F13
      sbSQL.append(" , TTQT.MOYSKBN");// F14
      sbSQL.append(" , TTQT.MOYSSTDT");// F15
      sbSQL.append(" , TTQT.MOYSRBAN");// F16
      sbSQL.append(" , TTKH.JLSTCREFLG");// F17
      sbSQL.append(" , TTKH.SIMEFLG2_LD");// F18
      sbSQL.append(" , DATE_FORMAT(TTQT.UPDDT, '%Y%m%d%H%i%s%f') ");// F19
      sbSQL.append(" from");
      sbSQL.append(" INATK.TOKTG_KHN TTKH");
      sbSQL.append(" left join INATK.TOKTG_QATEN TTQT");
      sbSQL.append(" on TTQT.MOYSKBN = TTKH.MOYSKBN");
      sbSQL.append(" and TTQT.MOYSSTDT = TTKH.MOYSSTDT");
      sbSQL.append(" and TTQT.MOYSRBAN = TTKH.MOYSRBAN");
      sbSQL.append(" and TTQT.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" and TTQT.KYOSEIFLG = ? ");
      paramData.add(ckKyosei);
      sbSQL.append(" left join INATK.TOKTG_QAGP TTQG");
      sbSQL.append(" on TTQG.MOYSKBN = TTQT.MOYSKBN");
      sbSQL.append(" and TTQG.MOYSSTDT = TTQT.MOYSSTDT");
      sbSQL.append(" and TTQG.MOYSRBAN = TTQT.MOYSRBAN");
      sbSQL.append(" and TTQG.TENGPCD = TTQT.TENGPCD");
      sbSQL.append(" left join INAMS.MSTTEN MTTN");
      sbSQL.append(" on MTTN.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" left join INATK.TOKMOYCD TMYC");
      sbSQL.append(" on TMYC.MOYSKBN = TTKH.MOYSKBN");
      sbSQL.append(" and TMYC.MOYSSTDT = TTKH.MOYSSTDT");
      sbSQL.append(" and TMYC.MOYSRBAN = TTKH.MOYSRBAN");
      sbSQL.append(" where");
      sbSQL.append(" TTKH.QAYYYYMM = ? ");
      paramData.add(szQayyyymm);
      sbSQL.append(" and TTKH.QAENO = ? ");
      paramData.add(szQaend);
      sbSQL.append(" and TTQT.MOYSKBN = TTKH.MOYSKBN");
      sbSQL.append(" and TTQT.MOYSSTDT = TTKH.MOYSSTDT");
      sbSQL.append(" and TTQT.MOYSRBAN = TTKH.MOYSRBAN");
      sbSQL.append(" and TTQT.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" and TTQT.KYOSEIFLG = ? ");
      paramData.add(ckKyosei);
      sbSQL.append(" and TTQG.MOYSKBN = TTQT.MOYSKBN");
      sbSQL.append(" and TTQG.MOYSSTDT = TTQT.MOYSSTDT");
      sbSQL.append(" and TTQG.MOYSRBAN = TTQT.MOYSRBAN");
      sbSQL.append(" and TTQG.TENGPCD = TTQT.TENGPCD");
      sbSQL.append(" and MTTN.TENCD = ? ");
      paramData.add(szTencd);
      sbSQL.append(" and TMYC.MOYSKBN = TTKH.MOYSKBN");
      sbSQL.append(" and TMYC.MOYSSTDT = TTKH.MOYSSTDT");
      sbSQL.append(" and TMYC.MOYSRBAN = TTKH.MOYSRBAN");
      sbSQL.append(" and TTKH.UPDKBN = 0");
      sbSQL.append(" order by MOYOOSICODE");

    }

    if (!StringUtils.isEmpty(sqlWhere)) {
      sbSQL.append(sqlWhere);
    }
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }



  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<String>();

    String szQayymm = getMap().get("QAYYYYMM"); // メーカコード
    String szQayyyymm = "20" + getMap().get("QAYYYYMM"); // メーカコード
    String szQaend = getMap().get("QAEND"); // メーカー名
    String szTencd = getMap().get("TENCD"); // チェック_メーカー名無し
    String szTenkn = getMap().get("TENKN"); // チェック_メーカー名無し
    String ckKyosei = getMap().get("CHK_KYOSEI"); // チェック_メーカー
    if (ckKyosei == null) {
      ckKyosei = "0";
    }

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("select ");
    sbSQL.append(szQayymm);// F1
    sbSQL.append(" as F1 ,");
    sbSQL.append(szQaend);// F2
    sbSQL.append(" as F2 ,");
    sbSQL.append(szTencd);// F3
    sbSQL.append(" as F3 ,'");
    sbSQL.append(szTenkn);// F4
    sbSQL.append(" ' as F4 ,");
    sbSQL.append(ckKyosei);// F5
    sbSQL.append("  as F5 , (case when count(TTQT.MBANSFLG)>=1 then '×' else '〇' END ) as F6");// F2
    sbSQL.append(" from");// F13
    sbSQL.append(" INATK.TOKTG_KHN TTKH");// F14
    sbSQL.append(" left join INATK.TOKTG_QATEN TTQT");// F15
    sbSQL.append(" on TTQT.MOYSKBN = TTKH.MOYSKBN");// F16
    sbSQL.append(" and TTQT.MOYSSTDT = TTKH.MOYSSTDT");
    sbSQL.append(" and TTQT.MOYSRBAN = TTKH.MOYSRBAN");
    sbSQL.append(" and TTQT.TENCD = ? ");
    paramData.add(szTencd);
    sbSQL.append(" and TTQT.KYOSEIFLG = ? ");
    paramData.add(ckKyosei);
    sbSQL.append(" where");
    sbSQL.append(" TTKH.QAYYYYMM = ? ");
    paramData.add(szQayyyymm);
    sbSQL.append(" and TTKH.QAENO = ? ");
    paramData.add(szQaend);
    sbSQL.append(" and TTQT.MBANSFLG = 0");


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

  /**
   * チェック処理
   *
   * @throws Exception
   */
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
    dataArray.optJSONObject(0);

    // 更新情報
    for (int j = 0; j < dataArray.size(); j++) {
      for (int i = 7; i <= 8; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);
        if (i == 7 && !String.valueOf(val).equals("0")) {
          JSONObject o = mu.getDbMessageObj("EX1078", new String[] {});
          msg.add(o);
          return msg;
        }
        if (i == 8 && !String.valueOf(val).equals("1")) {
          JSONObject o = mu.getDbMessageObj("EX1078", new String[] {});
          msg.add(o);
          return msg;
        }
      }
    }

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
    int kryoColNum = 9; // テーブル列数
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    String TENCD = map.get("TENCD"); // 呼出しボタン
    String KYOUSEI = map.get("KYOUSEI"); // 呼出しボタン
    if (StringUtils.isEmpty(KYOUSEI)) {
      KYOUSEI = "0";
    }

    JSONObject msgObj = new JSONObject();
    JSONArray msg = new JSONArray();
    JSONObject option = new JSONObject();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String updateRows = ""; // 更新データ
    String hbsValue = "";

    // 更新情報
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (int i = 1; i <= kryoColNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (isTest) {
          if (i == 1) {
            values += " '" + val + "'";
          } else if (i == 4) {
            if (val.equals("1")) {
              values += ", '" + 1 + "'";
            } else {
              values += ", '" + 2 + "'";
            }
          } else if (i == 5) {
            if (val.equals("1")) {
              hbsValue = "0";
            } else {
              hbsValue = "2";
            }
          } else if (i == 6) {
            if (val.equals("1")) {
              hbsValue = "1";
            } else if (hbsValue != "0") {
              hbsValue = "2";
            }
            values += ", '" + hbsValue + "'";
          } else if (i == 7) {
            values += ", '" + 1 + "'";
          } else if (i == 8) {
            values += ", '" + TENCD + "'";
          } else if (i == 9) {
            values += " ,'" + KYOUSEI + "'";
          } else {
            values += ", '" + val + "'";
          }
        } else {
          if (i == 1) {
            prmData.add(val);
          } else if (i == 4) {
            if (val.equals("1")) {
              prmData.add("1");
            } else {
              prmData.add("2");
            }
          } else if (i == 5) {
            if (val.equals("1")) {
              hbsValue = "0";
            } else {
              hbsValue = "2";
            }
          } else if (i == 6) {
            if (val.equals("1")) {
              hbsValue = "1";
            } else if (hbsValue != "0") {
              hbsValue = "2";
            }
            prmData.add(hbsValue);
          } else if (i == 7) {
            prmData.add("1");
          } else if (i == 8) {
            prmData.add(TENCD);
          } else if (i == 9) {
            prmData.add(KYOUSEI);
          } else {
            prmData.add(val);
          }
          if (i != 5) {
            values += ", ?";
          }
        }
      }
      values += ", " + DefineReport.Values.SENDFLG_UN.getVal();
      values += ", '" + userId + "'";
      values += ", CURRENT_TIMESTAMP ";
      values += ", CURRENT_TIMESTAMP ";
      updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    updateRows = StringUtils.removeStart(updateRows, ",");

    if (values.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return msgObj;
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL = new StringBuffer();;

    sbSQL.append("MERGE INTO INATK.TOKTG_QATEN AS T USING (SELECT ");
    sbSQL.append("  MOYSKBN"); // 催し区分：
    sbSQL.append(", MOYSSTDT"); // 催し開始日：
    sbSQL.append(", MOYSRBAN"); // 催し連番：
    sbSQL.append(", MBSYFLG"); // 採用フラグ：
    sbSQL.append(", HBSTRTFLG"); // 回答フラグ：
    sbSQL.append(", MBANSFLG"); // エリア区分：
    sbSQL.append(", TENCD"); // 店コード：
    sbSQL.append(", KYOSEIFLG"); // 強制フラグ：
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

          targetTable = "INATK.TOKTG_QATEN";
          targetWhere = "MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and TENCD = ? and KYOSEIFLG = ?";
          targetParam = new ArrayList<String>();
          targetParam.add(data.optString("F1"));
          targetParam.add(data.optString("F2"));
          targetParam.add(data.optString("F3"));
          targetParam.add(TENCD);
          targetParam.add(KYOUSEI);

          if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F9"))) {
            // rownum = data.optString("idx");
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
          System.out.println("全店特売（アンケート有）基本を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }
    }
    return msgObj;
  }
}
