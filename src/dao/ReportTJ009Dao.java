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
public class ReportTJ009Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTJ009Dao(String JNDIname) {
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

    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№

    String SQL_TOKTJ_TEN = ""; // 結合時に使用する事前発注店舗のSQLを格納

    String bmncd = szBmncd.substring(0, 2);

    JSONArray array = getTOKMOYCDData(getMap());
    JSONArray array2 = new JSONArray();
    JSONArray array3 = getTOKMOYCDData3(getMap());
    JSONArray array4 = getTOKMOYCDData4(getMap());

    String maxPageno = this.getMaxPageno(getMap()); // 検索対象のレコード中の最大ページ番号を取得

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append(" TEN.LSTNO");
    sbSQL.append(", TEN.BMNCD");
    sbSQL.append(", TEN.HYOSEQNO");
    sbSQL.append(", TEN.TENCD");
    sbSQL.append(", TEN.IRISU_RG");
    sbSQL.append(", TEN.IRISU_TB");
    sbSQL.append(", TEN.SBAIKAAM_TB");
    sbSQL.append(", TEN.BAIKAAM_TB");
    sbSQL.append(", TEN.SBAIKAAM_RG");
    sbSQL.append(", TEN.BAIKAAM_RG");
    sbSQL.append(", TEN.GENKAAM_MAE");
    sbSQL.append(", TEN.GENKAAM_ATO");
    sbSQL.append(", TEN.GENKAAM_RG");
    sbSQL.append(", TEN.GENKAAM_PACK");
    sbSQL.append(", TEN.BAIKAAM_PACK");
    sbSQL.append(", TEN.JTDT_01");
    sbSQL.append(", TEN.JTDT_02");
    sbSQL.append(", TEN.JTDT_03");
    sbSQL.append(", TEN.JTDT_04");
    sbSQL.append(", TEN.JTDT_05");
    sbSQL.append(", TEN.JTDT_06");
    sbSQL.append(", TEN.JTDT_07");
    sbSQL.append(", TEN.JTDT_08");
    sbSQL.append(", TEN.JTDT_09");
    sbSQL.append(", TEN.JTDT_10");
    sbSQL.append(", TEN.TSEIKBN_01");
    sbSQL.append(", TEN.TSEIKBN_02");
    sbSQL.append(", TEN.TSEIKBN_03");
    sbSQL.append(", TEN.TSEIKBN_04");
    sbSQL.append(", TEN.TSEIKBN_05");
    sbSQL.append(", TEN.TSEIKBN_06");
    sbSQL.append(", TEN.TSEIKBN_07");
    sbSQL.append(", TEN.TSEIKBN_08");
    sbSQL.append(", TEN.TSEIKBN_09");
    sbSQL.append(", TEN.TSEIKBN_10");
    sbSQL.append(", TEN.SHNKBN_01");
    sbSQL.append(", TEN.SHNKBN_02");
    sbSQL.append(", TEN.SHNKBN_03");
    sbSQL.append(", TEN.SHNKBN_04");
    sbSQL.append(", TEN.SHNKBN_05");
    sbSQL.append(", TEN.SHNKBN_06");
    sbSQL.append(", TEN.SHNKBN_07");
    sbSQL.append(", TEN.SHNKBN_08");
    sbSQL.append(", TEN.SHNKBN_09");
    sbSQL.append(", TEN.SHNKBN_10");
    sbSQL.append(", case when TEN.TSEIKBN_01 is null then null else TEN.HTSU_01 end as HTSU_01");
    sbSQL.append(", case when TEN.TSEIKBN_02 is null then null else TEN.HTSU_02 end as HTSU_02");
    sbSQL.append(", case when TEN.TSEIKBN_03 is null then null else TEN.HTSU_03 end as HTSU_03");
    sbSQL.append(", case when TEN.TSEIKBN_04 is null then null else TEN.HTSU_04 end as HTSU_04");
    sbSQL.append(", case when TEN.TSEIKBN_05 is null then null else TEN.HTSU_05 end as HTSU_05");
    sbSQL.append(", case when TEN.TSEIKBN_06 is null then null else TEN.HTSU_06 end as HTSU_06");
    sbSQL.append(", case when TEN.TSEIKBN_07 is null then null else TEN.HTSU_07 end as HTSU_07");
    sbSQL.append(", case when TEN.TSEIKBN_08 is null then null else TEN.HTSU_08 end as HTSU_08");
    sbSQL.append(", case when TEN.TSEIKBN_09 is null then null else TEN.HTSU_09 end as HTSU_09");
    sbSQL.append(", case when TEN.TSEIKBN_10 is null then null else TEN.HTSU_10 end as HTSU_10");
    sbSQL.append(", TEN.PAGENO");
    sbSQL.append(", TEN.SENDFLG");
    sbSQL.append(", TEN.OPERATOR");
    sbSQL.append(", TEN.ADDDT");
    sbSQL.append(", TEN.UPDDT");
    sbSQL.append(", '0' as ISADDSHN");
    sbSQL.append(", SHN.SHNCD");
    sbSQL.append(", SHN.SHNKN");
    sbSQL.append(", SHN.SANCHIKN");
    sbSQL.append(", SHN.COMMENTKN");
    sbSQL.append(", SHN.NHKETAIKBN");
    sbSQL.append(", SHN.TANIKBNKN");
    sbSQL.append(", SHN.BINKBN");
    sbSQL.append(", SHN.JRYO");
    sbSQL.append(", SHN.JISKAVG");
    sbSQL.append(", SHN.ODS");
    sbSQL.append(" from INATK.TOKTJ_SHN SHN");
    sbSQL.append(" left join INATK.TOKTJ_TEN TEN on SHN.LSTNO = TEN.LSTNO and SHN.BMNCD = TEN.BMNCD and SHN.HYOSEQNO = TEN.HYOSEQNO");
    sbSQL.append(" where TEN.LSTNO = " + szLstno);
    sbSQL.append(" and TEN.BMNCD = " + bmncd);
    sbSQL.append(" and TEN.TENCD = " + tenpo);
    sbSQL.append(" union all");
    sbSQL.append(" select");
    sbSQL.append(" LSTNO");
    sbSQL.append(", BMNCD");
    sbSQL.append(", HYOSEQNO");
    sbSQL.append(", TENCD");
    sbSQL.append(", null as IRISU_RG");
    sbSQL.append(", null as IRISU_TB");
    sbSQL.append(", null as SBAIKAAM_TB");
    sbSQL.append(", null as BAIKAAM_TB");
    sbSQL.append(", null as SBAIKAAM_RG");
    sbSQL.append(", null as BAIKAAM_RG");
    sbSQL.append(", null as GENKAAM_MAE");
    sbSQL.append(", null as GENKAAM_ATO");
    sbSQL.append(", null as GENKAAM_RG");
    sbSQL.append(", null as GENKAAM_PACK");
    sbSQL.append(", null as BAIKAAM_PACK");
    sbSQL.append(", JTDT_01");
    sbSQL.append(", JTDT_02");
    sbSQL.append(", JTDT_03");
    sbSQL.append(", JTDT_04");
    sbSQL.append(", JTDT_05");
    sbSQL.append(", JTDT_06");
    sbSQL.append(", JTDT_07");
    sbSQL.append(", JTDT_08");
    sbSQL.append(", JTDT_09");
    sbSQL.append(", JTDT_10");
    sbSQL.append(", null as TSEIKBN_01");
    sbSQL.append(", null as TSEIKBN_02");
    sbSQL.append(", null as TSEIKBN_03");
    sbSQL.append(", null as TSEIKBN_04");
    sbSQL.append(", null as TSEIKBN_05");
    sbSQL.append(", null as TSEIKBN_06");
    sbSQL.append(", null as TSEIKBN_07");
    sbSQL.append(", null as TSEIKBN_08");
    sbSQL.append(", null as TSEIKBN_09");
    sbSQL.append(", null as TSEIKBN_10");
    sbSQL.append(", null as SHNKBN_01");
    sbSQL.append(", null as SHNKBN_02");
    sbSQL.append(", null as SHNKBN_03");
    sbSQL.append(", null as SHNKBN_04");
    sbSQL.append(", null as SHNKBN_05");
    sbSQL.append(", null as SHNKBN_06");
    sbSQL.append(", null as SHNKBN_07");
    sbSQL.append(", null as SHNKBN_08");
    sbSQL.append(", null as SHNKBN_09");
    sbSQL.append(", null as SHNKBN_10");
    sbSQL.append(", HTSU_01");
    sbSQL.append(", HTSU_02");
    sbSQL.append(", HTSU_03");
    sbSQL.append(", HTSU_04");
    sbSQL.append(", HTSU_05");
    sbSQL.append(", HTSU_06");
    sbSQL.append(", HTSU_07");
    sbSQL.append(", HTSU_08");
    sbSQL.append(", HTSU_09");
    sbSQL.append(", HTSU_10");
    sbSQL.append(", PAGENO");
    sbSQL.append(", SENDFLG");
    sbSQL.append(", OPERATOR");
    sbSQL.append(", ADDDT");
    sbSQL.append(", UPDDT");
    sbSQL.append(", '1' as ISADDSHN");
    sbSQL.append(", SHNCD");
    sbSQL.append(", null as SHNKN");
    sbSQL.append(", null as SANCHIKN");
    sbSQL.append(", null as COMMENTKN");
    sbSQL.append(", null as NHKETAIKBN");
    sbSQL.append(", null as TANIKBNKN");
    sbSQL.append(", BINKBN");
    sbSQL.append(", null as JRYO");
    sbSQL.append(", null as JISKAVG");
    sbSQL.append(", null as ODS");
    sbSQL.append(" from INATK.TOKTJ_ADDSHN");
    sbSQL.append(" where LSTNO = " + szLstno);
    sbSQL.append(" and BMNCD = " + bmncd);
    sbSQL.append(" and TENCD = " + tenpo);

    // 事前発注_店舗の訂正区分が未設定の発注数を0に置き換えてから計算を行う
    SQL_TOKTJ_TEN = sbSQL.toString();


    sbSQL = new StringBuffer();

    sbSQL.append(" with RECURSIVE WK_PAGENO(IDX) as (");
    sbSQL.append("  select 1");
    sbSQL.append("   from");
    sbSQL.append("  (SELECT 1 AS DUMMY) DUMMY");
    sbSQL.append("  union all");
    sbSQL.append("  select");
    sbSQL.append("   IDX + 1");
    sbSQL.append("  from WK_PAGENO where IDX < " + maxPageno);

    sbSQL.append(" ),WK_GYONO(IDX) as (");
    sbSQL.append("  select 1");
    sbSQL.append("   FROM");
    sbSQL.append("  (SELECT 1 AS DUMMY) DUMMY");
    sbSQL.append("  union all");
    sbSQL.append("  select");
    sbSQL.append("   IDX + 1");
    sbSQL.append("  from WK_GYONO");
    sbSQL.append("  where IDX < 15");

    sbSQL.append(" ), WK_LIST as (");
    sbSQL.append("  select");
    sbSQL.append(" T1.IDX as PAGENO");
    sbSQL.append(", T2.IDX as GYONO");
    sbSQL.append(" from WK_PAGENO T1");
    sbSQL.append(", WK_GYONO T2");

    sbSQL.append(" ),WK as ( ");
    sbSQL.append("  select ");
    sbSQL.append("  T3.SHNKN as F1 ");
    sbSQL.append(" ,T4.NMKN as F2");
    sbSQL.append(" ,'販売日' as F3");
    sbSQL.append(" ,T6.NMKN as F4");
    sbSQL.append(" ,T7.NMKN as F5");
    sbSQL.append(" ,T8.NMKN as F6 ");
    sbSQL.append(" ,T9.NMKN as F7 ");
    sbSQL.append(" ,T10.NMKN as F8 ");
    sbSQL.append(" ,T11.NMKN as F9 ");
    sbSQL.append(" ,T12.NMKN as F10 ");
    sbSQL.append(" ,T13.NMKN as F11");
    sbSQL.append(" ,T14.NMKN as F12 ");
    sbSQL.append(" ,T15.NMKN as F13 ");
    sbSQL.append(" ,'' as F14 ");
    sbSQL.append(" ,RIGHT('0000000' || RTRIM(CAST(T3.SHNCD as CHAR)), 8) as F15 ");
    sbSQL.append(" ,T4.NMKN as F16");
    sbSQL.append(" ,T3.IRISU_TB as F17");
    sbSQL.append(" ,T3.GENKAAM_MAE as F18"); // 事前原価
    sbSQL.append(" ,T3.SBAIKAAM_TB as F19"); // 特売総倍
    sbSQL.append(" ,'発注' as F20");
    sbSQL.append(" ,T16.NMKN as F21 ");
    sbSQL.append(" ,T17.NMKN as F22 ");
    sbSQL.append(" ,T18.NMKN as F23 ");
    sbSQL.append(" ,T19.NMKN as F24 ");
    sbSQL.append(" ,T20.NMKN as F25 ");
    sbSQL.append(" ,T21.NMKN as F26 ");
    sbSQL.append(" ,T22.NMKN as F27 ");
    sbSQL.append(" ,T23.NMKN as F28 ");
    sbSQL.append(" ,T24.NMKN as F29 ");
    sbSQL.append(" ,T25.NMKN as F30 ");
    sbSQL.append(" ,'' as F31 ");
    sbSQL.append(" ,'' as F32 ");
    sbSQL.append(" ,'' as F33 ");
    sbSQL.append(" ,'' as F34 ");
    sbSQL.append(" ,'' as F35 ");
    sbSQL.append(" ,'' as F36 ");
    sbSQL.append(" ,'ケース数' as F37 ");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_01 when (T3.TSEIKBN_01 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_01 end as F38");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_02 when (T3.TSEIKBN_02 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_02 end as F39");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_03 when (T3.TSEIKBN_03 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_03 end as F40");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_04 when (T3.TSEIKBN_04 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_04 end as F41");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_05 when (T3.TSEIKBN_05 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_05 end as F42");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_06 when (T3.TSEIKBN_06 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_06 end as F43");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_07 when (T3.TSEIKBN_07 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_07 end as F44");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_08 when (T3.TSEIKBN_08 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_08 end as F45");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_09 when (T3.TSEIKBN_09 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_09 end as F46");
    sbSQL.append(", case when T3.ISADDSHN = '1' then T3.HTSU_10 when (T3.TSEIKBN_10 is null and T3.ISADDSHN = '0') then 99999 else T3.HTSU_10 end as F47");
    sbSQL.append(" ,( coalesce(T3.HTSU_01, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_02, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_03, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_04, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_05, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_06, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_07, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_08, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_09, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_10, 0)   ");
    sbSQL.append("  ) * coalesce(T3.IRISU_TB, 0) as F48 ");
    sbSQL.append(" ,replace(rtrim(replace(T3.SANCHIKN, '　', ' ')), ' ', '　') as F49 "); // 産地
    sbSQL.append(" ,'' as F50");
    sbSQL.append(" ,T5.NMKN as F51 ");
    sbSQL.append(" ,T3.GENKAAM_ATO as F52");
    sbSQL.append(" ,T3.BAIKAAM_TB as F53 ");
    sbSQL.append(" ,'納品売価' as F54 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) END as F55 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) END as F56 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) END as F57 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) END as F58 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0) END as F59 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0) END as F60 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0) END as F61 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0) END as F62 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0) END as F63 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0) END as F64 ");
    sbSQL.append(" ,ROUND(( case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_01 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_01 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_02 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_02 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_03 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_03 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_04 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_04 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS double) * NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS double)* NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0), 0) END)  / 1000, 0) as F65 ");
    sbSQL.append(" ,replace(rtrim(replace(T3.COMMENTKN, '　', ' ')), ' ', '　') as F66 ");
    sbSQL.append(" ,T3.GENKAAM_PACK as F67 ");
    sbSQL.append(" ,T3.BAIKAAM_PACK as F68 ");
    sbSQL.append(" ,'納品原価' as F69 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) END as F70 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) END as F71 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) END as F72 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) END as F73 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0) END as F74 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0) END as F75 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0) END as F76 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0) END as F77 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0) END as F78 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0) END as F79 ");
    sbSQL.append(" , ROUND((case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS double) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS double) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0), 0) END) / 1000, 0) as F80 ");
    sbSQL.append(" , T3.LSTNO as F81 ");
    sbSQL.append(" , T3.BMNCD as F82 ");
    sbSQL.append(" , T3.HYOSEQNO as F83 ");
    sbSQL.append(" , T3.JRYO as F84 ");
    sbSQL.append(" , T3.NHKETAIKBN as F85 ");
    sbSQL.append(" , T3.ISADDSHN as F86 ");
    sbSQL.append(" , T3.TSEIKBN_01 as F87 ");
    sbSQL.append(" , T3.TSEIKBN_02 as F88 ");
    sbSQL.append(" , T3.TSEIKBN_03 as F89 ");
    sbSQL.append(" , T3.TSEIKBN_04 as F90 ");
    sbSQL.append(" , T3.TSEIKBN_05 as F91 ");
    sbSQL.append(" , T3.TSEIKBN_06 as F92 ");
    sbSQL.append(" , T3.TSEIKBN_07 as F93 ");
    sbSQL.append(" , T3.TSEIKBN_08 as F94 ");
    sbSQL.append(" , T3.TSEIKBN_09 as F95 ");
    sbSQL.append(" , T3.TSEIKBN_10 as F96 ");
    sbSQL.append(" , T3.BINKBN as F97 ");
    sbSQL.append(" , T26.NMKN as F98 ");
    sbSQL.append(" , T3.TENCD as F99 ");
    sbSQL.append(" , T3.JISKAVG as F100");
    sbSQL.append(" , T3.GENKAAM_RG as F101");
    sbSQL.append(" , T3.SBAIKAAM_RG as F102");
    sbSQL.append(" , T3.IRISU_RG as F103 ");
    sbSQL.append(" , T3. PAGENO ");
    sbSQL.append(" , ROW_NUMBER() over(PARTITION BY T3.PAGENO  order by T3.HYOSEQNO) as ROWNO");
    sbSQL.append(" from ( select LSTNO from INATK.TOKTJ group by LSTNO ) T1 ");
    sbSQL.append(" left join ");
    sbSQL.append(" ( " + SQL_TOKTJ_TEN + " ) T3 ");
    sbSQL.append(" on T1.LSTNO = T3.LSTNO ");
    sbSQL.append(" left join INAMS.MSTMEISHO T4 ");
    sbSQL.append(" on T4.MEISHOCD = T3.TANIKBNKN ");
    sbSQL.append(" and T4.MEISHOKBN = '10803' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T5 ");
    sbSQL.append(" on T5.MEISHOCD = T3.BINKBN ");
    sbSQL.append(" and T5.MEISHOKBN = '10665' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T6 ");
    sbSQL.append(" on T6.MEISHOCD = T3.SHNKBN_01 ");
    sbSQL.append(" and T6.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T7 ");
    sbSQL.append(" on T7.MEISHOCD = T3.SHNKBN_02 ");
    sbSQL.append(" and T7.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T8 ");
    sbSQL.append(" on T8.MEISHOCD = T3.SHNKBN_03 ");
    sbSQL.append(" and T8.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T9 ");
    sbSQL.append(" on T9.MEISHOCD = T3.SHNKBN_04 ");
    sbSQL.append(" and T9.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T10 ");
    sbSQL.append(" on T10.MEISHOCD = T3.SHNKBN_05 ");
    sbSQL.append(" and T10.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T11 ");
    sbSQL.append(" on T11.MEISHOCD = T3.SHNKBN_06 ");
    sbSQL.append(" and T11.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T12 ");
    sbSQL.append(" on T12.MEISHOCD = T3.SHNKBN_07 ");
    sbSQL.append(" and T12.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T13 ");
    sbSQL.append(" on T13.MEISHOCD = T3.SHNKBN_08 ");
    sbSQL.append(" and T13.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T14 ");
    sbSQL.append(" on T14.MEISHOCD = T3.SHNKBN_09 ");
    sbSQL.append(" and T14.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T15 ");
    sbSQL.append(" on T15.MEISHOCD = T3.SHNKBN_10 ");
    sbSQL.append(" and T15.MEISHOKBN = '10805' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T16 ");
    sbSQL.append(" on T16.MEISHOCD = T3.TSEIKBN_01 ");
    sbSQL.append(" and T16.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T17 ");
    sbSQL.append(" on T17.MEISHOCD = T3.TSEIKBN_02 ");
    sbSQL.append(" and T17.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T18 ");
    sbSQL.append(" on T18.MEISHOCD = T3.TSEIKBN_03 ");
    sbSQL.append(" and T18.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T19 ");
    sbSQL.append(" on T19.MEISHOCD = T3.TSEIKBN_04 ");
    sbSQL.append(" and T19.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T20 ");
    sbSQL.append(" on T20.MEISHOCD = T3.TSEIKBN_05 ");
    sbSQL.append(" and T20.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T21 ");
    sbSQL.append(" on T21.MEISHOCD = T3.TSEIKBN_06 ");
    sbSQL.append(" and T21.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T22 ");
    sbSQL.append(" on T22.MEISHOCD = T3.TSEIKBN_07 ");
    sbSQL.append(" and T22.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T23 ");
    sbSQL.append(" on T23.MEISHOCD = T3.TSEIKBN_08 ");
    sbSQL.append(" and T23.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T24 ");
    sbSQL.append(" on T24.MEISHOCD = T3.TSEIKBN_09 ");
    sbSQL.append(" and T24.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T25 ");
    sbSQL.append(" on T25.MEISHOCD = T3.TSEIKBN_10 ");
    sbSQL.append(" and T25.MEISHOKBN = '10804' ");
    sbSQL.append(" left join INAMS.MSTMEISHO T26");
    sbSQL.append(" on T26.MEISHOCD = T3.NHKETAIKBN");
    sbSQL.append(" and T26.MEISHOKBN = '10803'");
    sbSQL.append(" where ");
    sbSQL.append(" T1.LSTNO =  ");
    sbSQL.append(szLstno);
    sbSQL.append(" order by F83 ) ");
    // sbSQL.append(" select IDX, WK.* ");
    sbSQL.append(" select");
    sbSQL.append(" IDX"); // F1 Idx
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F1 AS CHAR)"); // 1行目 F2 アイテム
    sbSQL.append("  when IDX = 2 then CAST(WK.F15 AS CHAR)"); // 2行目 F2 商品コード
    sbSQL.append("  when IDX = 3 then null"); // 3行目 F2
    sbSQL.append("  when IDX = 4 then CAST(WK.F66 AS CHAR)"); // 4行目 F2 コメント
    sbSQL.append("  end as F2");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F3
    sbSQL.append("  when IDX = 2 then CAST(WK.F17 AS CHAR)"); // 2行目 F3 特売入数
    sbSQL.append("  when IDX = 3 then CAST(WK.F103 AS CHAR)"); // 3行目 F3 標準入数
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F3
    sbSQL.append("  end as F3");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F5
    sbSQL.append("  when IDX = 2 then CAST(WK.F18 AS CHAR)"); // 2行目 F5 特売原価
    sbSQL.append("  when IDX = 3 then CAST(WK.F52 AS CHAR)"); // 3行目 F5 追加原価
    sbSQL.append("  when IDX = 4 then CAST(WK.F101 AS CHAR)"); // 4行目 F5 標準原価
    sbSQL.append("  end as F4");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F5
    sbSQL.append("  when IDX = 2 then CAST(WK.F19 AS CHAR)"); // 2行目 F6 特売総売
    sbSQL.append("  when IDX = 3 then CAST(WK.F53 AS CHAR)"); // 3行目 F6 特売本売
    sbSQL.append("  when IDX = 4 then CAST(WK.F102 AS CHAR)"); // 4行目 F6 標準売価
    sbSQL.append("  end as F5");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F6
    sbSQL.append("  when IDX = 2 then null"); // 2行目 F6
    sbSQL.append("  when IDX = 3 then CAST(WK.F100 AS CHAR)"); // 3行目 F6 平均実績
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F6
    sbSQL.append("  end as F6");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F3 AS CHAR)"); // 1行目 F7 販売日（配列）
    sbSQL.append("  when IDX = 2 then CAST(WK.F20 AS CHAR)"); // 2行目 F7 訂正区分（配列）
    sbSQL.append("  when IDX = 3 then CAST(WK.F37 AS CHAR)"); // 3行目 F7 ケース数（配列）
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F7
    sbSQL.append("  end as F7");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F4 AS CHAR)"); // 1行目 F8 販売日_1
    sbSQL.append("  when IDX = 2 then CAST(WK.F21 AS CHAR)"); // 2行目 F8 訂正区分_1
    sbSQL.append("  when IDX = 3 then CAST(WK.F38 AS CHAR)"); // 3行目 F8 ケース数_1
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F8
    sbSQL.append("  end as F8");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F5 AS CHAR)"); // 1行目 F9 販売日_2
    sbSQL.append("  when IDX = 2 then CAST(WK.F22 AS CHAR)"); // 2行目 F9 訂正区分_2
    sbSQL.append("  when IDX = 3 then CAST(WK.F39 AS CHAR)"); // 3行目 F9 ケース数_2
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F9
    sbSQL.append("  end as F9");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F6 AS CHAR)"); // 1行目 F10 販売日_3
    sbSQL.append("  when IDX = 2 then CAST(WK.F23 AS CHAR)"); // 2行目 F10 訂正区分_3
    sbSQL.append("  when IDX = 3 then CAST(WK.F40 AS CHAR)"); // 3行目 F10 ケース数_3
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F10
    sbSQL.append("  end as F10");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F7 AS CHAR)"); // 1行目 F11 販売日_4
    sbSQL.append("  when IDX = 2 then CAST(WK.F24 AS CHAR)"); // 2行目 F11 訂正区分_4
    sbSQL.append("  when IDX = 3 then CAST(WK.F41 AS CHAR)"); // 3行目 F11 ケース数_4
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F11
    sbSQL.append("  end as F11");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F8 AS CHAR)"); // 1行目 F12 販売日_5
    sbSQL.append("  when IDX = 2 then CAST(WK.F25 AS CHAR)"); // 2行目 F12 訂正区分_5
    sbSQL.append("  when IDX = 3 then CAST(WK.F42 AS CHAR)"); // 3行目 F12 ケース数_5
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F12
    sbSQL.append("  end as F12");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F9 AS CHAR)"); // 1行目 F13 販売日_6
    sbSQL.append("  when IDX = 2 then CAST(WK.F26 AS CHAR)"); // 2行目 F13 訂正区分_6
    sbSQL.append("  when IDX = 3 then CAST(WK.F43 AS CHAR)"); // 3行目 F13 ケース数_6
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F13
    sbSQL.append("  end as F13");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F10 AS CHAR)"); // 1行目 F14 販売日_7
    sbSQL.append("  when IDX = 2 then CAST(WK.F27 AS CHAR)"); // 2行目 F14 訂正区分_7
    sbSQL.append("  when IDX = 3 then CAST(WK.F44 AS CHAR)"); // 3行目 F14 ケース数_7
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F14
    sbSQL.append("  end as F14");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F11 AS CHAR)"); // 1行目 F15 販売日_8
    sbSQL.append("  when IDX = 2 then CAST(WK.F28 AS CHAR)"); // 2行目 F15 訂正区分_8
    sbSQL.append("  when IDX = 3 then CAST(WK.F45 AS CHAR)"); // 3行目 F15 ケース数_8
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F15
    sbSQL.append("  end as F15");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F12 AS CHAR)"); // 1行目 F16 販売日_9
    sbSQL.append("  when IDX = 2 then CAST(WK.F29 AS CHAR)"); // 2行目 F16 訂正区分_9
    sbSQL.append("  when IDX = 3 then CAST(WK.F46 AS CHAR)"); // 3行目 F16 ケース数_9
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F16
    sbSQL.append("  end as F16");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F13 AS CHAR)"); // 1行目 F17 販売日_10
    sbSQL.append("  when IDX = 2 then CAST(WK.F30 AS CHAR)"); // 2行目 F17 訂正区分_10
    sbSQL.append("  when IDX = 3 then CAST(WK.F47 AS CHAR)"); // 3行目 F17 ケース数_10
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F17
    sbSQL.append("  end as F17");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F18
    sbSQL.append("  when IDX = 2 then null"); // 2行目 F18
    sbSQL.append("  when IDX = 3 then CAST(WK.F48 AS CHAR)"); // 3行目 F18 ケース数計
    sbSQL.append("  when IDX = 4 then"); // 4行目 F18 実績過不足
    sbSQL.append("   case");
    sbSQL.append("   when WK.F100 is not null then CAST((WK.F48 - WK.F100) AS CHAR)");
    sbSQL.append("   else ''");
    sbSQL.append("   end");
    sbSQL.append("  end as F18");
    sbSQL.append(", WK.F81"); // F19 リストNo
    sbSQL.append(", WK.F82"); // F20 部門
    sbSQL.append(", WK.F83"); // F21 表示番号
    sbSQL.append(", WK.F86"); // F22 追加商品フラグ(画面制御に使用)
    sbSQL.append(", WK.F87"); // F23 訂正区分_01(画面制御に使用)
    sbSQL.append(", WK.F88"); // F24 訂正区分_02(画面制御に使用)
    sbSQL.append(", WK.F89"); // F25 訂正区分_03(画面制御に使用)
    sbSQL.append(", WK.F90"); // F26 訂正区分_04(画面制御に使用)
    sbSQL.append(", WK.F91"); // F27 訂正区分_05(画面制御に使用)
    sbSQL.append(", WK.F92"); // F28 訂正区分_06(画面制御に使用)
    sbSQL.append(", WK.F93"); // F29 訂正区分_07(画面制御に使用)
    sbSQL.append(", WK.F94"); // F30 訂正区分_08(画面制御に使用)
    sbSQL.append(", WK.F95"); // F31 訂正区分_09(画面制御に使用)
    sbSQL.append(", WK.F96"); // F32 訂正区分_10(画面制御に使用)
    sbSQL.append(", WK.F96"); // F33 訂正区分_10(画面制御に使用)
    sbSQL.append(", WK.F99"); // F34 店コード
    sbSQL.append(", WK.F68"); // F35 パック売価(再計算に使用)
    sbSQL.append(", WK.F67"); // F36 パック原価(再計算に使用)
    sbSQL.append(", WK.F84"); // F37 重量
    sbSQL.append(", WK.F85"); // F38 商品形態(再計算に使用)
    sbSQL.append(", WK.PAGENO "); // F39 ページング処理に使用
    sbSQL.append(" from WK_LIST");
    sbSQL.append(" left join WK on WK_LIST.PAGENO = WK.PAGENO and WK_LIST.GYONO = WK.ROWNO, ");
    sbSQL.append(" (select * from (values ROW(1),ROW(2),ROW(3),ROW(4)) as X(IDX) order by IDX)T5 ");
    sbSQL.append(" where not (WK_LIST.PAGENO = " + maxPageno + " and WK.PAGENO is null) ");
    sbSQL.append(" order by WK_LIST.PAGENO,GYONO,IDX");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    option.put("rows_2", array2);
    option.put("rows_3", array3);
    option.put("rows_4", array4);
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
    new ArrayList<String>();

    // 共通箇所設定
    createCmnOutput(jad);

  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String yobi2 = "";

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT YOBI_2 FROM KEYSYS.SYS_USERS WHERE CD_USER =" + userInfo.getCD_user());
    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray susUsers = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (susUsers.size() != 0) {
      yobi2 = susUsers.getJSONObject(0).getString("YOBI_2");
    }

    String szLstno = getMap().get("LSTNO"); // リスト№
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String bmncd = szBmncd.substring(0, 2);
    String bmnText = szBmncd.substring(3);

    paramData.add(szLstno);
    paramData.add(bmncd);
    paramData.add(tenpo);
    paramData.add(szLstno);

    sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append(" '");
    sbSQL.append(szLstno.substring(0, 4));
    sbSQL.append("'");
    sbSQL.append("  ||'-'||");
    sbSQL.append(" '");
    sbSQL.append(szLstno.substring(4));
    sbSQL.append("' as F1");
    sbSQL.append(" ,T1.TITLE as F2");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.STDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.STDT, '%Y%m%d')))");
    sbSQL.append("  ||'～'||");
    sbSQL.append("  DATE_FORMAT(DATE_FORMAT(T1.EDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.EDDT, '%Y%m%d'))) as F3");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.SNDSIMEDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.SNDSIMEDT, '%Y%m%d'))) as F4");
    sbSQL.append(" ,'");
    sbSQL.append(bmncd);
    sbSQL.append(" ' as F5");
    sbSQL.append(" ,'");
    sbSQL.append(bmnText);
    sbSQL.append(" ' as F6");
    sbSQL.append(", DATE_FORMAT(T2.ADDDT, '%y/%m/%d') as F7"); // F7 ： 登録日
    sbSQL.append(", DATE_FORMAT(T2.UPDDT, '%y/%m/%d') as F8"); // F8 ： 更新日
    sbSQL.append(", T2.OPERATOR as F9"); // F9 ： オペレータ
    sbSQL.append(", (SELECT SHORIDT FROM INAAD.SYSSHORIDT) > ");
    // 予備2が空なら本部
    if (StringUtils.isEmpty(yobi2)) {
      sbSQL.append(" CAST(DATE_FORMAT(DATE_ADD(DATE_FORMAT(T1.SNDSIMEDT,'%Y%m%d'),INTERVAL + 1 day),'%Y%m%d')AS SIGNED) as F10");
    } else {
      sbSQL.append(" CAST(T1.SNDSIMEDTAS SIGNED) as F10");
    }
    sbSQL.append("  from");
    sbSQL.append("  INATK.TOKTJ T1");
    sbSQL.append(" left join (");
    sbSQL.append("  select * from INATK.TOKTJ_TEN");
    sbSQL.append("  where LSTNO = ?");
    sbSQL.append("   and BMNCD = ?");
    sbSQL.append("   and TENCD = ?");
    sbSQL.append("  order by UPDDT desc");
    sbSQL.append("  LIMIT 1 ) T2 on T2.LSTNO = T1.LSTNO");
    sbSQL.append("  where");
    sbSQL.append("  T1.LSTNO = ?");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData2(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String bmncd = szBmncd.substring(0, 2);

    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("  select");
    sbSQL.append("  T1.BMNYSANAM as V1");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ_BMNYSAN T1");
    sbSQL.append(" where");
    sbSQL.append("  T1.LSTNO = ?");
    sbSQL.append("  and T1.TENCD = ?");
    sbSQL.append("  and T1.BMNCD = ?");
    sbSQL.append("  order by T1.TJDT");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData3(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String bmncd = szBmncd.substring(0, 2);
    paramData.add(szLstno);
    paramData.add(bmncd);
    paramData.add(tenpo);
    paramData.add(szLstno);
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("WITH WEEK as ( select CWEEK , JWEEK as JWEEK from ( values ROW(1, '日') , ROW(2, '月') , ROW(3, '火') , ROW(4, '水') , ROW(5, '木') , ROW(6, '金') , ROW(7, '土') ) as TMP(CWEEK, JWEEK) )");
    sbSQL.append("  select");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT(T3.JTDT_01, '%Y%m%d'), '%m/%d') as V1");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_02, '%Y%m%d'), '%m/%d') as V2");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_03, '%Y%m%d'), '%m/%d') as V3");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_04, '%Y%m%d'), '%m/%d') as V4");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_05, '%Y%m%d'), '%m/%d') as V5");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_06, '%Y%m%d'), '%m/%d') as V6");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_07, '%Y%m%d'), '%m/%d') as V7");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_08, '%Y%m%d'), '%m/%d') as V8");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_09, '%Y%m%d'), '%m/%d') as V9");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_10, '%Y%m%d'), '%m/%d') as V10");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_01, '%Y%m%d'))) as W1");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_02, '%Y%m%d'))) as W2");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_03, '%Y%m%d'))) as W3");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_04, '%Y%m%d'))) as W4");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_05, '%Y%m%d'))) as W5");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_06, '%Y%m%d'))) as W6");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_07, '%Y%m%d'))) as W7");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_08, '%Y%m%d'))) as W8");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_09, '%Y%m%d'))) as W9");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_10, '%Y%m%d'))) as W10");
    sbSQL.append(" ,T3.JTDT_01 as X1");
    sbSQL.append(" ,T3.JTDT_02 as X2");
    sbSQL.append(" ,T3.JTDT_03 as X3");
    sbSQL.append(" ,T3.JTDT_04 as X4");
    sbSQL.append(" ,T3.JTDT_05 as X5");
    sbSQL.append(" ,T3.JTDT_06 as X6");
    sbSQL.append(" ,T3.JTDT_07 as X7");
    sbSQL.append(" ,T3.JTDT_08 as X8");
    sbSQL.append(" ,T3.JTDT_09 as X9");
    sbSQL.append(" ,T3.JTDT_10 as X10");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ T1");
    sbSQL.append(" left join INATK.TOKTJ_SHN T2");
    sbSQL.append(" on T2.LSTNO = ? ");
    sbSQL.append(" and T2.BMNCD = ? ");
    sbSQL.append(" left join INATK.TOKTJ_TEN T3");
    sbSQL.append(" on T3.BMNCD = T2.BMNCD");
    sbSQL.append(" and T3.LSTNO = T2.LSTNO");
    sbSQL.append(" and T3.TENCD = ? ");
    sbSQL.append(" and T3.HYOSEQNO = T2.HYOSEQNO ");
    sbSQL.append(" where ");
    sbSQL.append("  T1.LSTNO = ? ");
    sbSQL.append("  order by T3.HYOSEQNO  IS NULL ASC, T3.HYOSEQNO");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData4(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String szLstno = getMap().get("LSTNO"); // リスト№
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String bmncd = szBmncd.substring(0, 2);

    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append("  SURICMPRT as F1");
    sbSQL.append(" ,SGRSCMPRT as F2");
    sbSQL.append(" ,GRSYSANAM as F3");
    sbSQL.append("  from");
    sbSQL.append("  INATK.TOKTJ_BMNCMPRT");
    sbSQL.append("  where");
    sbSQL.append("  LSTNO = ?");
    sbSQL.append("  and  TENCD = ?");
    sbSQL.append("  and  BMNCD = ?");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 画面に表示するレコードの最大ページ数を取得
   *
   * @throws Exception
   */
  public String getMaxPageno(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String bmncd = szBmncd.substring(0, 2);

    String maxPageno = "0";

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" select");
    sbSQL.append(" MAX(PAGENO) as VALUE");
    sbSQL.append(" from (");
    sbSQL.append("  select");
    sbSQL.append("   LSTNO");
    sbSQL.append("  , BMNCD");
    sbSQL.append("  , TENCD");
    sbSQL.append("  , PAGENO");
    sbSQL.append("  , HYOSEQNO");
    sbSQL.append("  from INATK.TOKTJ_TEN");
    sbSQL.append("  union all");
    sbSQL.append("  select");
    sbSQL.append("   LSTNO");
    sbSQL.append("  , BMNCD");
    sbSQL.append("  , TENCD");
    sbSQL.append("  , PAGENO");
    sbSQL.append("  , HYOSEQNO");
    sbSQL.append("  from INATK.TOKTJ_ADDSHN");
    sbSQL.append(" ) AS T1");
    sbSQL.append(" where LSTNO = ?");
    sbSQL.append(" and BMNCD = ?");
    sbSQL.append(" and TENCD = ?");

    paramData.add(szLstno);
    paramData.add(bmncd);
    paramData.add(tenpo);

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (array.size() > 0) {
      maxPageno = array.optJSONObject(0).optString("VALUE");
    }

    return maxPageno;
  }

}
