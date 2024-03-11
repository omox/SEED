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
public class ReportYH202Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportYH202Dao(String JNDIname) {
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

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    String szKkkcd = getMap().get("KKKCD"); // 企画No
    String szShncd = getMap().get("SHNCD"); // 商品No
    String szShoridt = getMap().get("SHORIDT"); // 処理日付

    // パラメータ確認
    // 必須チェック
    if (szKkkcd == null || szShncd == null || szShoridt == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append(" case");
    sbSQL.append(" when T1.F1 is not null then T1.UKESTDT || W1.JWEEK");
    sbSQL.append(" else '合計' end as F1");
    sbSQL.append(", T1.F2");
    sbSQL.append(", T1.F3");
    sbSQL.append(", T1.F4");
    sbSQL.append(", T1.F5");
    sbSQL.append(" from (select");
    sbSQL.append(" NNDT.NNDT as F1");
    sbSQL.append(", SUM(COALESCE(TEN.HTSU_Z, 0)) as F2");
    sbSQL.append(", SUM(COALESCE(TEN.HTSU_T, 0)) as F3");
    sbSQL.append(", SUM(NNDT.YOTEISU) as F4");
    sbSQL.append(", SUM(NNDT.GENDOSU) as F5");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || NNDT.NNDT, 6), '%Y%m%d'), '%y/%m/%d') as UKESTDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || NNDT.NNDT, 6), '%Y%m%d')) as UKESTDT_WNUM");
    sbSQL.append(" from INATK.HATYH_NNDT NNDT");
    sbSQL.append(" left join (select");
    sbSQL.append(" KKKCD");
    sbSQL.append(", SHNCD");
    sbSQL.append(", NNDT");
    sbSQL.append(", SUM(case when COALESCE(INPUTDT, 0) < " + szShoridt + " then COALESCE(HTSU, 0) else 0 end) as HTSU_Z");
    sbSQL.append(", SUM(case when COALESCE(INPUTDT, 0) = " + szShoridt + " then COALESCE(HTSU, 0) else 0 end) as HTSU_T");
    sbSQL.append(" from INATK.HATYH_TEN group by KKKCD, SHNCD, NNDT");
    sbSQL.append(") TEN on TEN.KKKCD = NNDT.KKKCD and TEN.SHNCD = NNDT.SHNCD and TEN.NNDT = NNDT.NNDT ");
    sbSQL.append(" where NNDT.KKKCD = " + szKkkcd);
    sbSQL.append(" and NNDT.SHNCD = " + szShncd);
    sbSQL.append(" group by NNDT.KKKCD, NNDT.SHNCD, NNDT.NNDT WITH ROLLUP having NNDT.NNDT is not null or NNDT.KKKCD is null order by NNDT.KKKCD, NNDT.SHNCD, NNDT.NNDT) T1");
    sbSQL.append(" left outer join WEEK W1 on T1.UKESTDT_WNUM = W1.CWEEK");
    sbSQL.append(" order by T1.F1");

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

  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

    String szKkkcd = map.get("KKKCD"); // 企画No
    String szShncd = map.get("SHNCD"); // 商品No
    map.get("SHORIDT");


    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append(" CAST(HKKK.KKKCD AS CHAR) || ' - ' || HKKK.KKKKM as F1");
    sbSQL.append(", LEFT(TRIM(HSHN.SHNCD), 4) || '-' || RIGHT(TRIM(HSHN.SHNCD), 4) || ' ' || SHN.SHNKN as F2");
    sbSQL.append(", HKKK.KKKCD as F3");
    sbSQL.append(", HSHN.SHNCD as F4");
    sbSQL.append(" from INATK.HATYH_KKK HKKK");
    sbSQL.append(" left join INATK.HATYH_SHN HSHN on HSHN.KKKCD = HKKK.KKKCD");
    sbSQL.append(" left join INAMS.MSTSHN SHN on SHN.SHNCD = HSHN.SHNCD");
    sbSQL.append(" where HKKK.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and HKKK.KKKCD = " + szKkkcd + " and HSHN.SHNCD = '" + szShncd + "'");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
