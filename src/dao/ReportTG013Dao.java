package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class ReportTG013Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG013Dao(String JNDIname) {
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

    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    for (int i = 0; i < 3; i++) {
      paramData.add(szMoyskbn);
      paramData.add(szMoysstdt);
      paramData.add(szMoysrban);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append("    right('000' || TTQT2.TENGPCD, 3) as F1");
    sbSQL.append("  , MAX(TTTG.TENGPKN) as F2");
    sbSQL.append("  , MAX(right ('000' || TTQT1.TENCD, 3)) as F3");
    sbSQL.append("  , MAX(MTTN.TENKN) as F4");
    sbSQL.append("  , SUM(TTQT2.CNT) as F5");
    sbSQL.append("  , MAX( case when TTQT1.MBANSFLG = 1 then '〇' else '×' END) as F6");
    sbSQL.append("  , SUM(TTQT2.TCNT) as F7");
    sbSQL.append("  , SUM(TTQT2.NCNT) as F8");
    sbSQL.append("  , MAX(case when TTQG.LDSYFLG = 1 then '参加' else '不参加' END) as F9");
    sbSQL.append("  , MAX(case when TTQG.QASYUKBN = 1 then '1' else '0' END) as F10");
    sbSQL.append(
        "  , MAX(case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 and TTQG.QASYUKBN = 1 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 1 then '×' else '' END) as F11");
    sbSQL.append("  , MAX(case when TTQG.QASYUKBN = 2  then '1' else '0' END) as F12");
    sbSQL.append(
        "  , MAX(case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 and TTQG.QASYUKBN = 2 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 2  then '×' else '' END) as F13");
    sbSQL.append("  , MAX(case when TTQG.QASYUKBN = 3  then '1' else '0' END) as F14");
    sbSQL.append(
        "  , MAX(case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 and TTQG.QASYUKBN = 3 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 3  then '×' else '' END) as F15");
    sbSQL.append("  , MAX(case when TTQG.QASYUKBN = 4  then '1' else '0' END) as F16");
    sbSQL.append(
        "  , MAX(case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 and TTQG.QASYUKBN = 4 then '〇' when (TTQG.ITMANSFLG = 0 or TTQG.SCHANSFLG = 0) and TTQG.QASYUKBN = 4  then '×' else '' END) as F17");
    sbSQL.append("  , MAX(TTQG.MOYSKBN) as F18");
    sbSQL.append("  , MAX(TTQG.MOYSSTDT) as F19");
    sbSQL.append("  , MAX(TTQG.MOYSRBAN) as F20");
    sbSQL.append("  , MAX(TTQG.KYOSEIFLG) as F21");
    sbSQL.append(" from");
    sbSQL.append("  INATK.TOKTG_KHN TTKH ");
    sbSQL.append(" left join");
    sbSQL.append("  INATK.TOKTG_QAGP TTQG ");
    sbSQL.append(" on TTKH.MOYSKBN = TTQG.MOYSKBN");
    sbSQL.append(" and TTKH.MOYSSTDT = TTQG.MOYSSTDT");
    sbSQL.append(" and TTKH.MOYSRBAN = TTQG.MOYSRBAN");
    sbSQL.append(" left join");
    sbSQL.append(" (select");
    sbSQL.append(" TTQT.MOYSKBN");
    sbSQL.append(" , TTQT.MOYSSTDT");
    sbSQL.append(" , TTQT.MOYSRBAN");
    sbSQL.append(" , TTQT.TENCD");
    sbSQL.append(" , TTQT.TENGPCD");
    sbSQL.append(" , TTQT.MBANSFLG");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTG_QAGP TTQG");
    sbSQL.append(" inner join INATK.TOKTG_QATEN TTQT");
    sbSQL.append(" on  TTQG.MOYSKBN  = TTQT.MOYSKBN");
    sbSQL.append(" and TTQG.MOYSSTDT = TTQT.MOYSSTDT");
    sbSQL.append(" and TTQG.MOYSRBAN = TTQT.MOYSRBAN");
    sbSQL.append(" where");
    sbSQL.append(" TTQT.MOYSKBN = ?");
    sbSQL.append(" and TTQT.MOYSSTDT = ?");
    sbSQL.append(" and TTQT.MOYSRBAN = ?");
    sbSQL.append(" and  TTQT.LDTENKBN = 1");
    sbSQL.append(" group by");
    sbSQL.append("  TTQT.MOYSKBN");
    sbSQL.append(" ,TTQT.MOYSSTDT");
    sbSQL.append(" ,TTQT.MOYSRBAN");
    sbSQL.append(" ,TTQT.TENCD");
    sbSQL.append(" ,TTQT.TENGPCD");
    sbSQL.append(" ,TTQT.MBANSFLG");
    sbSQL.append(" ) TTQT1");
    sbSQL.append(" on TTQT1.TENGPCD =  TTQG.TENGPCD");
    sbSQL.append(" left join");
    sbSQL.append(" (select");
    sbSQL.append(" count(*) AS CNT");
    sbSQL.append(" , sum(case when TTQT.MBSYFLG = 1 then 1 else 0 end) as TCNT");
    sbSQL.append(" , sum(case when TTQT.MBSYFLG = 2 then 1 else 0 end) as NCNT");
    sbSQL.append(" , TTQT.TENGPCD");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTG_QATEN TTQT");
    sbSQL.append(" where");
    sbSQL.append(" TTQT.MOYSKBN = ?");
    sbSQL.append(" and TTQT.MOYSSTDT = ?");
    sbSQL.append(" and TTQT.MOYSRBAN = ?");
    sbSQL.append(" group by");
    sbSQL.append(" TTQT.MOYSKBN");
    sbSQL.append(" , TTQT.MOYSSTDT");
    sbSQL.append(" , TTQT.MOYSRBAN");
    sbSQL.append(" , TTQT.TENGPCD) TTQT2");
    sbSQL.append(" on TTQT2.TENGPCD =  TTQG.TENGPCD");
    sbSQL.append(" left join INAMS.MSTTEN MTTN");
    sbSQL.append(" on MTTN.TENCD = TTQT1.TENCD");
    sbSQL.append(" left join INATK.TOKTG_TENGP TTTG");
    sbSQL.append(" on  TTTG.TENGPCD  = TTQT1.TENGPCD");
    sbSQL.append(" and TTTG.MOYSKBN  = TTQT1.MOYSKBN");
    sbSQL.append(" and TTTG.MOYSSTDT = TTQT1.MOYSSTDT");
    sbSQL.append(" and TTTG.MOYSRBAN = TTQT1.MOYSRBAN");
    sbSQL.append(" where");
    sbSQL.append(" TTQG.MOYSKBN = ?");
    sbSQL.append(" and TTQG.MOYSSTDT = ?");
    sbSQL.append(" and TTQG.MOYSRBAN = ?");
    sbSQL.append(" group by TTQT2.TENGPCD");
    sbSQL.append(" order by F1");

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
    String szQaend = map.get("QAEND"); // 催し連番

    ArrayList<String> paramData = new ArrayList<String>();


    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append("    CONCAT(CONCAT(left(T2.MOYSSTDT, 2), '/'), SUBSTRING(T2.MOYSSTDT, 3,2)) as F1");
    sbSQL.append("  , '");
    sbSQL.append(szQaend);
    sbSQL.append("' as F2");
    sbSQL.append("  , T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F3");
    sbSQL.append("  , T1.MOYKN as F4");
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as F5");
    sbSQL.append("  , T2.HBOKUREFLG as F6");
    sbSQL.append("  from INATK.TOKMOYCD T1");
    sbSQL.append("  left join INATK.TOKTG_KHN T2");
    sbSQL.append("  on T1.MOYSKBN = T2.MOYSKBN");
    sbSQL.append("  and T1.MOYSSTDT = T2.MOYSSTDT");
    sbSQL.append("  and T1.MOYSRBAN = T2.MOYSRBAN");
    sbSQL.append("  where T1.UPDKBN = 0");
    sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn + "");
    sbSQL.append("  and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("  and T1.MOYSRBAN = " + szMoysrban + "");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
