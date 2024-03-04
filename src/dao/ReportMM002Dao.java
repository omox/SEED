package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportMM002Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportMM002Dao(String JNDIname) {
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
    String szMoyscd = getMap().get("MOYSCD"); // 催しコード
    String szBmflg = getMap().get("BMFLG"); // B/Mフラグ
    String szBmnno = getMap().get("BMNNO"); // B/M番号

    // // DB検索用パラメータ
    // ArrayList<String> paramData = new ArrayList<String>();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append(" PARNO as F1"); // F1 : GNo.
    sbSQL.append(", CHLDNO as F2"); // F2 : 子No.
    sbSQL.append(", SHNCD as F3"); // F3 : 商品コード
    sbSQL.append(", SHNKN as F4"); // F4 : 商品マスター名称
    sbSQL.append(", case");
    sbSQL.append(" when (HBSTDTD IS NULL OR HBSTDTD='') and (HBEDDTD IS NULL OR HBEDDTD='') then ''");
    sbSQL.append(" when (HBSTDTD IS NOT NULL and HBSTDTD <> '') and (HBEDDTD IS NULL OR HBEDDTD='') then HBSTDTD || HBSTDTW");
    sbSQL.append(" when (HBSTDTD IS NULL OR HBSTDTD='') and (HBEDDTD IS NOT NULL and HBEDDTD <> '') then HBEDDTD || HBEDDTW");
    sbSQL.append(" else HBSTDTD || HBSTDTW || '～' || HBEDDTD || HBEDDTW end as F5"); // F5 : 販売期間
    sbSQL.append(", case");
    sbSQL.append(" when (NNSTDTD IS NULL OR NNSTDTD='') and (NNEDDTD IS NULL OR NNEDDTD='') then ''");
    sbSQL.append(" when (NNSTDTD IS NOT NULL and NNSTDTD <> '') and (NNEDDTD IS NULL OR NNEDDTD='') then NNSTDTD || NNSTDTW");
    sbSQL.append(" when (NNSTDTD IS NULL OR NNSTDTD='') and (NNEDDTD IS NOT NULL and NNEDDTD <> '') then NNEDDTD || NNEDDTW");
    sbSQL.append(" else NNSTDTD || NNSTDTW || '～' || NNEDDTD || NNEDDTW end as F6"); // F6 : 納入期間
    sbSQL.append(", KANRINO as F7"); // F7 : 管理番号
    sbSQL.append(", MOYSCD as F8"); // F8 : 催しコード
    sbSQL.append(", MOYSKBN as F9"); // F9 : 催し区分
    sbSQL.append(", MOYSSTDT as F10"); // F10 : 催し開始日
    sbSQL.append(", MOYSRBAN as F11"); // F11 : 催し連番
    sbSQL.append(", ADDSHUKBN as F12"); // F12 : 登録種別
    sbSQL.append(", BMFLG as F13"); // F13 : B/Mフラグ
    sbSQL.append(", BMNO as F14"); // F14 : B/M番号
    sbSQL.append(" from");
    sbSQL.append(" (select");
    sbSQL.append(" T1.PARNO");
    sbSQL.append(", T1.CHLDNO");
    sbSQL.append(", T1.SHNCD");
    sbSQL.append(", (select T2.SHNKN from INAMS.MSTSHN T2 where T2.SHNCD=T1.SHNCD) AS SHNKN ");
    sbSQL.append(", DATE_FORMAT(T1.HBSTDT,'%y/%m/%d') AS HBSTDTD");
    sbSQL.append(", DATE_FORMAT(T1.HBEDDT,'%y/%m/%d') as HBEDDTD");
    sbSQL.append(", DATE_FORMAT(T1.NNSTDT,'%y/%m/%d') as NNSTDTD");
    sbSQL.append(", DATE_FORMAT(T1.NNEDDT,'%y/%m/%d') as NNEDDTD");
    sbSQL.append(", (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(T1.HBSTDT,'%Y%m%d'))) AS HBSTDTW");
    sbSQL.append(", (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(T1.HBEDDT,'%Y%m%d'))) AS HBEDDTW");
    sbSQL.append(", (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(T1.NNSTDT,'%Y%m%d'))) AS NNSTDTW");
    sbSQL.append(", (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(T1.NNEDDT,'%Y%m%d'))) AS NNEDDTW");
    sbSQL.append(", T1.KANRINO");
    sbSQL.append(", (T1.MOYSKBN || right('000000'||T1.MOYSSTDT, 6) || right('000'||T1.MOYSRBAN, 3)) AS MOYSCD ");
    sbSQL.append(", T1.MOYSKBN");
    sbSQL.append(", T1.MOYSSTDT");
    sbSQL.append(", T1.MOYSRBAN");
    sbSQL.append(", T1.ADDSHUKBN");
    sbSQL.append(", T1.BMFLG");
    sbSQL.append(", T1.BMNO");
    sbSQL.append(" from INATK.TOKMM_SHN T1");
    sbSQL.append(" where T1.MOYSKBN=" + StringUtils.substring(szMoyscd, 0, 1));
    sbSQL.append(" and T1.MOYSSTDT=" + StringUtils.substring(szMoyscd, 1, 7));
    sbSQL.append(" and T1.MOYSRBAN=" + StringUtils.substring(szMoyscd, 7, 10));
    sbSQL.append(" and T1.BMNCD=" + szBmncd);
    sbSQL.append(" and T1.BMNO=" + szBmnno);
    sbSQL.append(" and T1.BMFLG=" + szBmflg);
    sbSQL.append(" ) S1 ");
    sbSQL.append(" order by PARNO, CHLDNO, SHNCD, KANRINO");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    // // DB検索用パラメータ設定
    // setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szTencd = getMap().get("TENCD"); // 店コード
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szMoyscd = getMap().get("MOYSCD"); // 催しコード
    String szBmflg = getMap().get("BMFLG"); // B/Mフラグ

    ArrayList<String> paramData = new ArrayList<String>();


    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append(" right('000'||" + szTencd + ", 3) as F1"); // F1 : 店コード
    // sbSQL.append(" ,T1.MOYSKBN || ,T1.MOYSSTDT || ,T1.MOYSRBAN AS F2 "); // F2 : 催しコード
    sbSQL.append(" ,right('0'||T1.MOYSKBN, 1)||'-'||right('000000'||T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F2"); // F2 : 催しコード
    // sbSQL.append(" ,"+szBmncd+" as F3"); // F3 : 部門コード
    sbSQL.append(" ,right('00'||" + szBmncd + ", 2) as F3"); // F3 : 部門コード
    sbSQL.append(" from INATK.TOKMM T1");
    sbSQL.append(" where T1.MOYSKBN=" + StringUtils.substring(szMoyscd, 0, 1));
    sbSQL.append(" and T1.MOYSSTDT=" + StringUtils.substring(szMoyscd, 1, 7));
    sbSQL.append(" and T1.MOYSRBAN=" + StringUtils.substring(szMoyscd, 7, 10));
    sbSQL.append(" and T1.BMFLG=" + szBmflg);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
