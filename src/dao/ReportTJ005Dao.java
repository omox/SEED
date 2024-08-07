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
public class ReportTJ005Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTJ005Dao(String JNDIname) {
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
    JSONArray msg = this.check(map, userInfo);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 更新処理
    try {
      String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
      if (StringUtils.equals("workTableDel", sendBtnid)) {
        // 更新対象:ワークテーブル
        msgObj = this.updateWorkDataDel(map, userInfo);
      } else if (sendBtnid.startsWith("workTable")) {
        // 更新対象:ワークテーブル
        msgObj = this.updateWorkData(map, userInfo);
      } else {
        // 通常処理
        msgObj = this.updateData(map, userInfo);
      }
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
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

    JSONArray array = getTOKMOYCDData(getMap()); // ヘッダー部(リストNo、タイトル、期間、送信締切日、部門)
    // JSONArray array2 = getTOKMOYCDData2(getMap()); // ヘッダー部(部門予算）
    JSONArray array2 = new JSONArray();
    JSONArray array3 = getTOKMOYCDData3(getMap()); // ヘッダー部(日付、曜日）

    String maxPageno = this.getMaxPageno(getMap()); // 検索対象のレコード中の最大ページ番号を取得

    // 事前発注_発注明細wk管理テーブルに作業ユーザー情報を登録する。
    // HashMap<String, String> workMap = new HashMap<>();
    // workMap.put("BMNCD", bmncd);
    // workMap.put("LSTNO", szLstno);
    // try {
    // this.updateWork_MNG_Data(workMap, userInfo);
    // } catch (Exception e) {
    // // TODO 自動生成された catch ブロック
    // e.printStackTrace();
    // }

    int addShnDataNum = getAddShndataNum(getMap()); // 追加商品登録数

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    new ArrayList<String[]>();

    // 事前発注_店舗検索SQL
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

    // 検索パターン：精肉
    sbSQL = new StringBuffer();

    sbSQL.append(" with RECURSIVE WK_PAGENO(IDX) as (");
    sbSQL.append("  select");
    sbSQL.append("   1");
    sbSQL.append("  from (SELECT 1 AS DUMMY) AS DUMMY");
    sbSQL.append("  union all");
    sbSQL.append("  select");
    sbSQL.append("   IDX + 1");
    sbSQL.append("  from WK_PAGENO where IDX < " + maxPageno);

    sbSQL.append(" ),WK_GYONO as (");
    sbSQL.append("  select");
    sbSQL.append("   1 AS IDX");
    sbSQL.append("  from (SELECT 1 AS DUMMY) AS DUMMY");
    for (int i = 2; i <= 15; i++) {
      sbSQL.append(" union all ");
      sbSQL.append(" SELECT ");
      sbSQL.append(i + " AS IDX ");
      sbSQL.append(" FROM (SELECT 1 AS DUMMY) AS DUMMY ");
    }

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
    sbSQL.append(" ,RIGHT('0000000' || RTRIM(CAST(T3.SHNCD AS CHAR)), 8) as F15 ");
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
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) END as F55 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) END as F56 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) END as F57 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) END as F58 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0) END as F59 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0) END as F60 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0) END as F61 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0) END as F62 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0) END as F63 ");
    sbSQL.append(" , case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0) END as F64 ");
    sbSQL.append(" ,ROUND(( case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_01 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_01 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_02 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_02 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_03 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_03 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_04 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_04 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_05 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_06 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_07 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_08 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_09 , 0) * T3.IRISU_TB, 0), 0) END ");
    sbSQL.append(" + case when T3.BAIKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.BAIKAAM_TB AS DECIMAL) * NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.BAIKAAM_PACK AS DECIMAL)* NULLIF(T3.HTSU_10 , 0) * T3.IRISU_TB, 0), 0) END)  / 1000, 0) as F65 ");
    sbSQL.append(" ,replace(rtrim(replace(T3.COMMENTKN, '　', ' ')), ' ', '　') as F66 ");
    sbSQL.append(" ,T3.GENKAAM_PACK as F67 ");
    sbSQL.append(" ,T3.BAIKAAM_PACK as F68 ");
    sbSQL.append(" ,'納品原価' as F69 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0) END as F70 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0) END as F71 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0) END as F72 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0) END as F73 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0) END as F74 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0) END as F75 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0) END as F76 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0) END as F77 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0) END as F78 ");
    sbSQL.append(" , case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0) ");
    sbSQL.append("   else ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0) END as F79 ");
    sbSQL.append(" , ROUND((case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_01, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_02, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_03, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_04, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_05, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_06, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_07, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_08, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_09, 0) * T3.IRISU_TB, 0), 0) END  ");
    sbSQL.append(" + case when T3.GENKAAM_PACK = 0 ");
    sbSQL.append("   then COALESCE(ROUND(CAST(T3.GENKAAM_MAE AS DECIMAL) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0), 0) ");
    sbSQL.append("   else COALESCE(ROUND(CAST(T3.GENKAAM_PACK AS DECIMAL) * NULLIF(T3.HTSU_10, 0) * T3.IRISU_TB, 0), 0) END) / 1000, 0) as F80 ");
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
    sbSQL.append(" , T3.ODS as F104 ");
    sbSQL.append("  ,");
    sbSQL.append("  case");
    sbSQL.append("   when T3.NHKETAIKBN = 2 then CAST(ROUND(CAST(COALESCE(");
    sbSQL.append(" ( coalesce(T3.HTSU_01, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_02, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_03, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_04, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_05, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_06, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_07, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_08, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_09, 0) + ");
    sbSQL.append("  coalesce(T3.HTSU_10, 0)   ");
    sbSQL.append("  ) * coalesce(T3.IRISU_TB, 0)");
    sbSQL.append("  , 0) AS DECIMAL ) * COALESCE(T3.IRISU_TB, 0) * COALESCE(T3.JRYO, 0), 0) AS CHAR) || 'Kg'");
    sbSQL.append("  else '' end as F105");
    sbSQL.append(" , TRIM(T3.TANIKBNKN) as F106 ");
    sbSQL.append(" , T3.SHNKBN_01 as F107 ");
    sbSQL.append(" , T3.SHNKBN_02 as F108 ");
    sbSQL.append(" , T3.SHNKBN_03 as F109 ");
    sbSQL.append(" , T3.SHNKBN_04 as F110 ");
    sbSQL.append(" , T3.SHNKBN_05 as F111 ");
    sbSQL.append(" , T3.SHNKBN_06 as F112 ");
    sbSQL.append(" , T3.SHNKBN_07 as F113 ");
    sbSQL.append(" , T3.SHNKBN_08 as F114 ");
    sbSQL.append(" , T3.SHNKBN_09 as F115 ");
    sbSQL.append(" , T3.SHNKBN_10 as F116 ");
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
    sbSQL.append(" select");
    sbSQL.append(" IDX as F1"); // F1 Idx
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F1 AS CHAR)"); // 1行目 F2 アイテム
    sbSQL.append("  when IDX = 2 then CAST(WK.F15 AS CHAR)"); // 2行目 F2 商品コード
    sbSQL.append("  when IDX = 3 then null"); // 3行目 F2
    sbSQL.append("  when IDX = 4 then CAST(WK.F49 AS CHAR)"); // 4行目 F2 産地
    sbSQL.append("  when IDX = 5 then CAST(WK.F66 AS CHAR)"); // 5行目 F2 コメント
    sbSQL.append("  end as F2");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F3
    sbSQL.append("  when IDX = 2 then null"); // 2行目 F3
    sbSQL.append("  when IDX = 3 then CAST(WK.F106 AS CHAR)"); // 3行目 F3 単位
    sbSQL.append("  when IDX = 4 then null"); // 4行目 F3
    sbSQL.append("  when IDX = 5 then null"); // 5行目 F3
    sbSQL.append("  end as F3");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F4
    sbSQL.append("  when IDX = 2 then null"); // 2行目 F4
    sbSQL.append("  when IDX = 3 then CAST(WK.F17 AS CHAR)"); // 3行目 F4 入数
    sbSQL.append("  when IDX = 4 then CAST(WK.F97 AS CHAR)"); // 4行目 F4 便
    sbSQL.append("  when IDX = 5 then null"); // 5行目 F4
    sbSQL.append("  end as F4");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then null"); // 1行目 F5
    sbSQL.append("  when IDX = 2 then null"); // 2行目 F5
    sbSQL.append("  when IDX = 3 then CAST(WK.F18 AS CHAR)"); // 3行目 F5 特売原価
    sbSQL.append("  when IDX = 4 then CAST(WK.F52 AS CHAR)"); // 4行目 F5 追加原価
    sbSQL.append("  when IDX = 5 then CAST(WK.F67 AS CHAR)"); // 5行目 F5 パック原価
    sbSQL.append("  end as F5");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F98 AS CHAR)"); // 1行目 F6 納品形態
    sbSQL.append("  when IDX = 2 then null"); // 2行目 F6
    sbSQL.append("  when IDX = 3 then CAST(WK.F19 AS CHAR)"); // 3行目 F6 特売総売
    sbSQL.append("  when IDX = 4 then CAST(WK.F53 AS CHAR)"); // 4行目 F6 特売本売
    sbSQL.append("  when IDX = 5 then CAST(WK.F68 AS CHAR)"); // 5行目 F6 パック売価
    sbSQL.append("  end as F6");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F3 AS CHAR)"); // 1行目 F7 販売日（配列）
    sbSQL.append("  when IDX = 2 then CAST(WK.F20 AS CHAR)"); // 2行目 F7 訂正区分（配列）
    sbSQL.append("  when IDX = 3 then CAST(WK.F37 AS CHAR)"); // 3行目 F7 ケース数（配列）
    sbSQL.append("  when IDX = 4 then CAST(WK.F54 AS CHAR)"); // 4行目 F7 納品売価（配列）
    sbSQL.append("  when IDX = 5 then CAST(WK.F69 AS CHAR)"); // 5行目 F7 納品原価（配列）
    sbSQL.append("  end as F7");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F4 AS CHAR)"); // 1行目 F8 販売日_1
    sbSQL.append("  when IDX = 2 then CAST(WK.F21 AS CHAR)"); // 2行目 F8 訂正区分_1
    sbSQL.append("  when IDX = 3 then CAST(WK.F38 AS CHAR)"); // 3行目 F8 ケース数_1
    sbSQL.append("  when IDX = 4 then CAST(WK.F55 AS CHAR)"); // 4行目 F8 納品売価_1
    sbSQL.append("  when IDX = 5 then CAST(WK.F70 AS CHAR)"); // 5行目 F8 納品原価_1
    sbSQL.append("  end as F8");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F5 AS CHAR)"); // 1行目 F9 販売日_2
    sbSQL.append("  when IDX = 2 then CAST(WK.F22 AS CHAR)"); // 2行目 F9 訂正区分_2
    sbSQL.append("  when IDX = 3 then CAST(WK.F39 AS CHAR)"); // 3行目 F9 ケース数_2
    sbSQL.append("  when IDX = 4 then CAST(WK.F56 AS CHAR)"); // 4行目 F9 納品売価_2
    sbSQL.append("  when IDX = 5 then CAST(WK.F71 AS CHAR)"); // 5行目 F9 納品原価_2
    sbSQL.append("  end as F9");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F6 AS CHAR)"); // 1行目 F10 販売日_3
    sbSQL.append("  when IDX = 2 then CAST(WK.F23 AS CHAR)"); // 2行目 F10 訂正区分_3
    sbSQL.append("  when IDX = 3 then CAST(WK.F40 AS CHAR)"); // 3行目 F10 ケース数_3
    sbSQL.append("  when IDX = 4 then CAST(WK.F57 AS CHAR)"); // 4行目 F10 納品売価_3
    sbSQL.append("  when IDX = 5 then CAST(WK.F72 AS CHAR)"); // 5行目 F10 納品原価_3
    sbSQL.append("  end as F10");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F7 AS CHAR)"); // 1行目 F11 販売日_4
    sbSQL.append("  when IDX = 2 then CAST(WK.F24 AS CHAR)"); // 2行目 F11 訂正区分_4
    sbSQL.append("  when IDX = 3 then CAST(WK.F41 AS CHAR)"); // 3行目 F11 ケース数_4
    sbSQL.append("  when IDX = 4 then CAST(WK.F58 AS CHAR)"); // 4行目 F11 納品売価_4
    sbSQL.append("  when IDX = 5 then CAST(WK.F73 AS CHAR)"); // 5行目 F11 納品原価_4
    sbSQL.append("  end as F11");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F8 AS CHAR)"); // 1行目 F12 販売日_5
    sbSQL.append("  when IDX = 2 then CAST(WK.F25 AS CHAR)"); // 2行目 F12 訂正区分_5
    sbSQL.append("  when IDX = 3 then CAST(WK.F42 AS CHAR)"); // 3行目 F12 ケース数_5
    sbSQL.append("  when IDX = 4 then CAST(WK.F59 AS CHAR)"); // 4行目 F12 納品売価_5
    sbSQL.append("  when IDX = 5 then CAST(WK.F74 AS CHAR)"); // 5行目 F12 納品原価_5
    sbSQL.append("  end as F12");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F9 AS CHAR)"); // 1行目 F13 販売日_6
    sbSQL.append("  when IDX = 2 then CAST(WK.F26 AS CHAR)"); // 2行目 F13 訂正区分_6
    sbSQL.append("  when IDX = 3 then CAST(WK.F43 AS CHAR)"); // 3行目 F13 ケース数_6
    sbSQL.append("  when IDX = 4 then CAST(WK.F60 AS CHAR)"); // 4行目 F13 納品売価_6
    sbSQL.append("  when IDX = 5 then CAST(WK.F75 AS CHAR)"); // 5行目 F13 納品原価_6
    sbSQL.append("  end as F13");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F10 AS CHAR)"); // 1行目 F14 販売日_7
    sbSQL.append("  when IDX = 2 then CAST(WK.F27 AS CHAR)"); // 2行目 F14 訂正区分_7
    sbSQL.append("  when IDX = 3 then CAST(WK.F44 AS CHAR)"); // 3行目 F14 ケース数_7
    sbSQL.append("  when IDX = 4 then CAST(WK.F61 AS CHAR)"); // 4行目 F14 納品売価_7
    sbSQL.append("  when IDX = 5 then CAST(WK.F76 AS CHAR)"); // 5行目 F14 納品原価_7
    sbSQL.append("  end as F14");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F11 AS CHAR)"); // 1行目 F15 販売日_8
    sbSQL.append("  when IDX = 2 then CAST(WK.F28 AS CHAR)"); // 2行目 F15 訂正区分_8
    sbSQL.append("  when IDX = 3 then CAST(WK.F45 AS CHAR)"); // 3行目 F15 ケース数_8
    sbSQL.append("  when IDX = 4 then CAST(WK.F62 AS CHAR)"); // 4行目 F15 納品売価_8
    sbSQL.append("  when IDX = 5 then CAST(WK.F77 AS CHAR)"); // 5行目 F15 納品原価_8
    sbSQL.append("  end as F15");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F12 AS CHAR)"); // 1行目 F16 販売日_9
    sbSQL.append("  when IDX = 2 then CAST(WK.F29 AS CHAR)"); // 2行目 F16 訂正区分_9
    sbSQL.append("  when IDX = 3 then CAST(WK.F46 AS CHAR)"); // 3行目 F16 ケース数_9
    sbSQL.append("  when IDX = 4 then CAST(WK.F63 AS CHAR)"); // 4行目 F16 納品売価_9
    sbSQL.append("  when IDX = 5 then CAST(WK.F78 AS CHAR)"); // 5行目 F16 納品原価_9
    sbSQL.append("  end as F16");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F13 AS CHAR)"); // 1行目 F17 販売日_10
    sbSQL.append("  when IDX = 2 then CAST(WK.F30 AS CHAR)"); // 2行目 F17 訂正区分_10
    sbSQL.append("  when IDX = 3 then CAST(WK.F47 AS CHAR)"); // 3行目 F17 ケース数_10
    sbSQL.append("  when IDX = 4 then CAST(WK.F64 AS CHAR)"); // 4行目 F17 納品売価_10
    sbSQL.append("  when IDX = 5 then CAST(WK.F79 AS CHAR)"); // 5行目 F17 納品原価_10
    sbSQL.append("  end as F17");
    sbSQL.append(", case");
    sbSQL.append("  when IDX = 1 then CAST(WK.F105 AS CHAR)"); // 1行目 F18 重量計
    sbSQL.append("  when IDX = 2 then CAST(WK.F31 AS CHAR)"); // 2行目 F18 発注
    sbSQL.append("  when IDX = 3 then CAST(WK.F48 AS CHAR)"); // 3行目 F18 ケース数計
    sbSQL.append("  when IDX = 4 then CAST(WK.F65 AS CHAR)"); // 4行目 F18 納品売価計
    sbSQL.append("  when IDX = 5 then CAST(WK.F80 AS CHAR)"); // 5行目 F18 納品原価計
    sbSQL.append("  end as F18");
    sbSQL.append(", WK.F81 as F19"); // F19 リストNo
    sbSQL.append(", WK.F82 as F20"); // F20 部門
    sbSQL.append(", WK.F83 as F21"); // F21 表示番号
    sbSQL.append(", WK.F86 as F22"); // F22 追加商品フラグ(画面制御に使用)
    sbSQL.append(", WK.F87 as F23"); // F23 訂正区分_01(画面制御に使用)
    sbSQL.append(", WK.F88 as F24"); // F24 訂正区分_02(画面制御に使用)
    sbSQL.append(", WK.F89 as F25"); // F25 訂正区分_03(画面制御に使用)
    sbSQL.append(", WK.F90 as F26"); // F26 訂正区分_04(画面制御に使用)
    sbSQL.append(", WK.F91 as F27"); // F27 訂正区分_05(画面制御に使用)
    sbSQL.append(", WK.F92 as F28"); // F28 訂正区分_06(画面制御に使用)
    sbSQL.append(", WK.F93 as F29"); // F29 訂正区分_07(画面制御に使用)
    sbSQL.append(", WK.F94 as F30"); // F30 訂正区分_08(画面制御に使用)
    sbSQL.append(", WK.F95 as F31"); // F31 訂正区分_09(画面制御に使用)
    sbSQL.append(", WK.F96 as F32"); // F32 訂正区分_10(画面制御に使用)
    sbSQL.append(", WK.F96 as F33"); // F33 訂正区分_10(画面制御に使用)
    sbSQL.append(", WK.F99 as F34"); // F34 店コード
    sbSQL.append(", WK.F68 as F35"); // F35 パック売価(再計算に使用)
    sbSQL.append(", COALESCE(WK.F67, 0) as F36"); // F36 パック原価(再計算に使用)
    sbSQL.append(", COALESCE(WK.F84, 0) as F37"); // F37 重量
    sbSQL.append(", WK.F85 as F38"); // F38 商品形態(再計算に使用)
    sbSQL.append(", WK.F107 as F39"); // F39 商品区分_01(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F108 as F40"); // F40 商品区分_02(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F109 as F41"); // F41 商品区分_03(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F110 as F42"); // F42 商品区分_04(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F111 as F43"); // F43 商品区分_05(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F112 as F44"); // F44 商品区分_06(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F113 as F45"); // F45 商品区分_07(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F114 as F46"); // F46 商品区分_08(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F115 as F47"); // F47 商品区分_09(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.F116 as F48"); // F48 商品区分_10(ワークテーブル登録処理に使用)
    sbSQL.append(", WK.PAGENO as F49"); // F49 ページング処理に使用
    sbSQL.append(" from WK_LIST");
    sbSQL.append(" left join WK on WK_LIST.PAGENO = WK.PAGENO and WK_LIST.GYONO = WK.ROWNO, ");
    sbSQL.append(" (select * from (values row(1),row(2),row(3),row(4),row(5)) as X(IDX) order by IDX) as T1 ");
    sbSQL.append(" where not (WK_LIST.PAGENO = " + maxPageno + " and WK.PAGENO is null)");
    sbSQL.append(" order by WK_LIST.PAGENO,GYONO,IDX");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    option.put("rows_2", array2);
    option.put("rows_3", array3);
    option.put("countAddShn", addShnDataNum);
    option.put("allRows", new ReportTJ001Dao(JNDIname).getAllRecord(sbSQL));

    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    }
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
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.STDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.STDT, '%Y%m%d')))");
    sbSQL.append("  ||'～'||");
    sbSQL.append("  DATE_FORMAT(DATE_FORMAT(T1.EDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.EDDT, '%Y%m%d'))) as F3");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.SNDSIMEDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.SNDSIMEDT, '%Y%m%d'))) as F4");
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
      sbSQL.append(" CAST(DATE_FORMAT(DATE_FORMAT(T1.SNDSIMEDT,'%Y%m%d') +INTERVAL 1 DAY,'%Y%m%d')AS SIGNED) as F10");
    } else {
      sbSQL.append(" CAST(T1.SNDSIMEDT AS SIGNED) as F10");
    }
    sbSQL.append("  from");
    sbSQL.append("  INATK.TOKTJ T1");
    sbSQL.append(" left join (");
    sbSQL.append("  select * from INATK.TOKTJ_TEN");
    sbSQL.append("  where LSTNO = ?");
    sbSQL.append("   and BMNCD = ?");
    sbSQL.append("   and TENCD = ?");
    sbSQL.append("  order by UPDDT desc");
    sbSQL.append("  LIMIT 1) T2 on T2.LSTNO = T1.LSTNO");
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
    sbSQL.append("WITH WEEK as ( select CWEEK , JWEEK as JWEEK from ( values row(1, '日') , row(2, '月') , row(3, '火') , row(4, '水') , row(5, '木') , row(6, '金') , row(7, '土') ) as TMP(CWEEK, JWEEK) )");
    sbSQL.append("  select");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT(T3.JTDT_01, '%m/%d'), '%m/%d') as V1");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_02, '%m/%d'), '%m/%d') as V2");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_03, '%m/%d'), '%m/%d') as V3");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_04, '%m/%d'), '%m/%d') as V4");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_05, '%m/%d'), '%m/%d') as V5");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_06, '%m/%d'), '%m/%d') as V6");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_07, '%m/%d'), '%m/%d') as V7");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_08, '%m/%d'), '%m/%d') as V8");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_09, '%m/%d'), '%m/%d') as V9");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_10, '%m/%d'), '%m/%d') as V10");
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
    sbSQL.append(" on T2.LSTNO = ?");
    sbSQL.append(" and T2.BMNCD = ?");
    sbSQL.append(" left join INATK.TOKTJ_TEN T3");
    sbSQL.append(" on T3.BMNCD = T2.BMNCD");
    sbSQL.append(" and T3.LSTNO = T2.LSTNO");
    sbSQL.append(" and T3.TENCD = ?");
    sbSQL.append(" and T3.HYOSEQNO = T2.HYOSEQNO ");
    sbSQL.append(" where ");
    sbSQL.append("  T1.LSTNO = ? ");
    sbSQL.append("  order by T3.HYOSEQNO");

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
  public int getAddShndataNum(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String bmncd = szBmncd.substring(0, 2);

    int countShndata = 0;

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("select");
    sbSQL.append(" COALESCE(COUNT(T2.HYOSEQNO), 0) as value");
    sbSQL.append(" from INATK.TOKTJ T1");
    sbSQL.append(" inner join INATK.TOKTJ_ADDSHN T2 on T1.LSTNO = T2.LSTNO");
    sbSQL.append(" where T2.LSTNO = ?");
    sbSQL.append(" and T2.BMNCD = ?");
    sbSQL.append(" and T2.TENCD = ?");

    paramData.add(szLstno);
    paramData.add(bmncd);
    paramData.add(tenpo);

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (array.size() > 0) {
      countShndata = array.optJSONObject(0).optInt("VALUE");
    }

    return countShndata;
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

  // ページング時のワークテーブルへの更新処理
  private JSONObject updateWorkData(HashMap<String, String> map, User userInfo) throws Exception {

    JSONObject option = new JSONObject();

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（事前発注_発注明細wk）

    String userId = userInfo.getId(); // ログインユーザー
    String szLstno = map.get("LSTNO"); // リスト№
    String szBmncd = map.get("BMNCD"); // 部門コード
    String tenpoCd = userInfo.getTenpo(); // 店舗コード

    ArrayList<String> prmData = new ArrayList<>();
    Object[] valueData = new Object[] {};
    String values = "";

    // 事前発注_発注明細wk管理
    int maxField = 3; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      String val = "";
      if (StringUtils.equals("F1", key)) {
        val = szLstno; // リストNo

      } else if (StringUtils.equals("F2", key)) {
        val = tenpoCd; // 店舗コード

      } else if (StringUtils.equals("F3", key)) {
        val = szBmncd; // 部門コード

      }

      if (StringUtils.isEmpty(val)) {
        values += "null ,";
      } else {
        values += "? ,";
        prmData.add(val);
      }

      if (k == maxField) {
        values += " '" + userId + "'";
        values += ", CURRENT_TIMESTAMP ";
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKTJ_WK_MNG ( ");
    sbSQL.append(" LSTNO"); // リスト№
    sbSQL.append(", TENCD"); // 店コード
    sbSQL.append(", BMNCD"); // 部門
    sbSQL.append(", OPERATOR "); // オペレーター
    sbSQL.append(", UPDDT "); // 更新日
    sbSQL.append(") VALUES ");
    sbSQL.append(StringUtils.join(valueData, ","));
    sbSQL.append(" ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" LSTNO = VALUES(LSTNO)"); // リスト№
    sbSQL.append(", TENCD = VALUES(TENCD)"); // 店コード
    sbSQL.append(", BMNCD = VALUES(BMNCD)"); // 部門
    sbSQL.append(", OPERATOR = VALUES(OPERATOR) "); // オペレーター
    sbSQL.append(", UPDDT = VALUES(UPDDT) "); // 更新日

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("事前発注_発注明細wk管理");

    // クリア
    prmData = new ArrayList<>();
    valueData = new Object[] {};
    values = "";

    maxField = 44; // Fxxの最大値
    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArray.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {"F7", "F8"}, key)) {
            String val = dataT.optString(key);

            if (StringUtils.equals("F6", key)) {

              ArrayList paramData = new ArrayList<String>();
              sbSQL = new StringBuffer();
              if (!StringUtils.isEmpty(val)) {
                sbSQL.append(" select DAICD,BINKBN from INAMS.MSTSHN WHERE SHNCD=?");
                paramData.add(val);
              } else {
                sbSQL.append(" select DAICD,BINKBN from INAMS.MSTSHN WHERE SHNCD=null");
              }

              new ItemList();
              @SuppressWarnings("static-access")
              JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

              String daicd = "";
              String binkbn = "";
              if (array.size() != 0) {
                daicd = array.optJSONObject(0).getString("DAICD");
                binkbn = array.optJSONObject(0).getString("BINKBN");
                values += "CAST(? AS CHAR(14)),CAST(? AS SIGNED),CAST(? AS SIGNED),";
                prmData.add(val);
                prmData.add(daicd);
                prmData.add(binkbn);
              } else {
                values += "CAST(null AS CHAR(14)),CAST(null AS SIGNED),CAST(null AS SIGNED),";
              }
            } else {
              if (StringUtils.isEmpty(val)) {
                values += "null ,";
              } else {
                values += "? ,";
                prmData.add(val);
              }
            }
          }

          if (k == maxField) {
            values += "CURRENT_TIMESTAMP";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 生活応援_商品の登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.TOKTJ_SHNDT_WK ( ");
        sbSQL.append(" LSTNO"); // リスト№
        sbSQL.append(", TENCD"); // 店コード
        sbSQL.append(", BMNCD"); // 部門
        sbSQL.append(", HYOSEQNO"); // 表示順番
        sbSQL.append(", ADDSHNFLG"); // 追加商品区分
        sbSQL.append(", SHNCD"); // 商品コード
        sbSQL.append(", DAICD"); // 大分類
        sbSQL.append(", BINKBN"); // 便区分
        sbSQL.append(", IRISU_TB"); // 入数_特売
        sbSQL.append(", GENKAAM_MAE"); // 事前原価
        sbSQL.append(", BAIKAAM_TB"); // 特売本体売価
        sbSQL.append(", JRYO"); // 重量
        sbSQL.append(", SHNKBN_01"); // 商品区分_01
        sbSQL.append(", SHNKBN_02"); // 商品区分_02
        sbSQL.append(", SHNKBN_03"); // 商品区分_03
        sbSQL.append(", SHNKBN_04"); // 商品区分_04
        sbSQL.append(", SHNKBN_05"); // 商品区分_05
        sbSQL.append(", SHNKBN_06"); // 商品区分_06
        sbSQL.append(", SHNKBN_07"); // 商品区分_07
        sbSQL.append(", SHNKBN_08"); // 商品区分_08
        sbSQL.append(", SHNKBN_09"); // 商品区分_09
        sbSQL.append(", SHNKBN_10"); // 商品区分_10
        sbSQL.append(", TSEIKBN_01"); // 訂正区分_01
        sbSQL.append(", TSEIKBN_02"); // 訂正区分_02
        sbSQL.append(", TSEIKBN_03"); // 訂正区分_03
        sbSQL.append(", TSEIKBN_04"); // 訂正区分_04
        sbSQL.append(", TSEIKBN_05"); // 訂正区分_05
        sbSQL.append(", TSEIKBN_06"); // 訂正区分_06
        sbSQL.append(", TSEIKBN_07"); // 訂正区分_07
        sbSQL.append(", TSEIKBN_08"); // 訂正区分_08
        sbSQL.append(", TSEIKBN_09"); // 訂正区分_09
        sbSQL.append(", TSEIKBN_10"); // 訂正区分_10
        sbSQL.append(", HTSU_01"); // 発注数_01
        sbSQL.append(", HTSU_02"); // 発注数_02
        sbSQL.append(", HTSU_03"); // 発注数_03
        sbSQL.append(", HTSU_04"); // 発注数_04
        sbSQL.append(", HTSU_05"); // 発注数_05
        sbSQL.append(", HTSU_06"); // 発注数_06
        sbSQL.append(", HTSU_07"); // 発注数_07
        sbSQL.append(", HTSU_08"); // 発注数_08
        sbSQL.append(", HTSU_09"); // 発注数_09
        sbSQL.append(", HTSU_10"); // 発注数_10
        sbSQL.append(", PAGENO"); // ページ番号
        sbSQL.append(", NHKETAIKBN"); // 納品形態区分
        sbSQL.append(", UPDDT "); // 更新日
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append(" ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" LSTNO = VALUES(LSTNO)"); // リスト№
        sbSQL.append(", TENCD = VALUES(TENCD)"); // 店コード
        sbSQL.append(", BMNCD = VALUES(BMNCD)"); // 部門
        sbSQL.append(", HYOSEQNO = VALUES(HYOSEQNO)"); // 表示順番
        sbSQL.append(", ADDSHNFLG = VALUES(ADDSHNFLG)"); // 追加商品区分
        sbSQL.append(", SHNCD = VALUES(SHNCD)"); // 商品コード
        sbSQL.append(", DAICD = VALUES(DAICD)"); // 大分類
        sbSQL.append(", BINKBN = VALUES(BINKBN)"); // 便区分
        sbSQL.append(", IRISU_TB = VALUES(IRISU_TB)"); // 入数_特売
        sbSQL.append(", GENKAAM_MAE = VALUES(GENKAAM_MAE)"); // 事前原価
        sbSQL.append(", BAIKAAM_TB = VALUES(BAIKAAM_TB)"); // 特売本体売価
        sbSQL.append(", JRYO = VALUES(JRYO)"); // 重量
        sbSQL.append(", SHNKBN_01 = VALUES(SHNKBN_01)"); // 商品区分_01
        sbSQL.append(", SHNKBN_02 = VALUES(SHNKBN_02)"); // 商品区分_02
        sbSQL.append(", SHNKBN_03 = VALUES(SHNKBN_03)"); // 商品区分_03
        sbSQL.append(", SHNKBN_04 = VALUES(SHNKBN_04)"); // 商品区分_04
        sbSQL.append(", SHNKBN_05 = VALUES(SHNKBN_05)"); // 商品区分_05
        sbSQL.append(", SHNKBN_06 = VALUES(SHNKBN_06)"); // 商品区分_06
        sbSQL.append(", SHNKBN_07 = VALUES(SHNKBN_07)"); // 商品区分_07
        sbSQL.append(", SHNKBN_08 = VALUES(SHNKBN_08)"); // 商品区分_08
        sbSQL.append(", SHNKBN_09 = VALUES(SHNKBN_09)"); // 商品区分_09
        sbSQL.append(", SHNKBN_10 = VALUES(SHNKBN_10)"); // 商品区分_10
        sbSQL.append(", TSEIKBN_01 = VALUES(TSEIKBN_01)"); // 訂正区分_01
        sbSQL.append(", TSEIKBN_02 = VALUES(TSEIKBN_02)"); // 訂正区分_02
        sbSQL.append(", TSEIKBN_03 = VALUES(TSEIKBN_03)"); // 訂正区分_03
        sbSQL.append(", TSEIKBN_04 = VALUES(TSEIKBN_04)"); // 訂正区分_04
        sbSQL.append(", TSEIKBN_05 = VALUES(TSEIKBN_05)"); // 訂正区分_05
        sbSQL.append(", TSEIKBN_06 = VALUES(TSEIKBN_06)"); // 訂正区分_06
        sbSQL.append(", TSEIKBN_07 = VALUES(TSEIKBN_07)"); // 訂正区分_07
        sbSQL.append(", TSEIKBN_08 = VALUES(TSEIKBN_08)"); // 訂正区分_08
        sbSQL.append(", TSEIKBN_09 = VALUES(TSEIKBN_09)"); // 訂正区分_09
        sbSQL.append(", TSEIKBN_10 = VALUES(TSEIKBN_10)"); // 訂正区分_10
        sbSQL.append(", HTSU_01 = VALUES(HTSU_01)"); // 発注数_01
        sbSQL.append(", HTSU_02 = VALUES(HTSU_02)"); // 発注数_02
        sbSQL.append(", HTSU_03 = VALUES(HTSU_03)"); // 発注数_03
        sbSQL.append(", HTSU_04 = VALUES(HTSU_04)"); // 発注数_04
        sbSQL.append(", HTSU_05 = VALUES(HTSU_05)"); // 発注数_05
        sbSQL.append(", HTSU_06 = VALUES(HTSU_06)"); // 発注数_06
        sbSQL.append(", HTSU_07 = VALUES(HTSU_07)"); // 発注数_07
        sbSQL.append(", HTSU_08 = VALUES(HTSU_08)"); // 発注数_08
        sbSQL.append(", HTSU_09 = VALUES(HTSU_09)"); // 発注数_09
        sbSQL.append(", HTSU_10 = VALUES(HTSU_10)"); // 発注数_10
        sbSQL.append(", PAGENO = VALUES(PAGENO)"); // ページ番号
        sbSQL.append(", NHKETAIKBN = VALUES(NHKETAIKBN)"); // 納品形態区分
        sbSQL.append(", UPDDT = VALUES(UPDDT) "); // 更新日

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/*" + this.getClass().getName() + "*/ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("事前発注_発注明細wk");

        // クリア
        prmData = new ArrayList<>();
        valueData = new Object[] {};
        values = "";
      }
    }


    // 登録処理
    ArrayList<Integer> countList = new ArrayList<>();
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

  // 戻るボタン押下時のワークテーブルへの削除処理
  private JSONObject updateWorkDataDel(HashMap<String, String> map, User userInfo) throws Exception {

    JSONObject option = new JSONObject();

    this.createSqlDelWorkData(userInfo, map);

    // 登録処理
    ArrayList<Integer> countList = new ArrayList<>();
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
   * 事前発注_発注明細wk管理、事前発注_発注明細wk 削除SQL作成処理
   *
   * @param userInfo
   * @param map
   * @throws Exception
   */
  public JSONObject createSqlDelWorkData(User userInfo, HashMap<String, String> map) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    userInfo.getId();
    String szLstno = map.get("LSTNO"); // リスト№
    String szBmncd = map.get("BMNCD"); // 部門コード
    String tenpoCd = userInfo.getTenpo(); // 店舗コード

    // 事前発注_発注明細wk管理
    if (StringUtils.isNotEmpty(szLstno) || StringUtils.isNotEmpty(tenpoCd) || StringUtils.isNotEmpty(szBmncd)) {

      prmData = new ArrayList<>();
      prmData.add(szLstno);
      prmData.add(tenpoCd);
      prmData.add(szBmncd);

      StringBuffer sbSQL;
      sbSQL = new StringBuffer();
      sbSQL.append("delete from INATK.TOKTJ_WK_MNG where LSTNO = ? and TENCD = ? and BMNCD = ?");

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/*" + this.getClass().getName() + "*/" + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("事前発注_発注明細wk管理");
    }

    // 事前発注_発注明細wk
    if (StringUtils.isNotEmpty(szLstno) || StringUtils.isNotEmpty(tenpoCd) || StringUtils.isNotEmpty(szBmncd)) {

      prmData = new ArrayList<>();
      prmData.add(szLstno);
      prmData.add(tenpoCd);
      prmData.add(szBmncd);

      StringBuffer sbSQL;
      sbSQL = new StringBuffer();
      sbSQL.append("delete from INATK.TOKTJ_SHNDT_WK where LSTNO = ? and TENCD = ? and BMNCD = ?");

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("事前発注_発注明細wk");
    }
    return result;
  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONArray dataArrayYSN = JSONArray.fromObject(map.get("DATA_YSN")); // 対象情報:事前発注_部門予算
    JSONArray dataArraySHN = JSONArray.fromObject(map.get("DATA_SHN")); // 対象情報:事前発注_店舗
    JSONArray dataArraySHN2 = JSONArray.fromObject(map.get("DATA_SHN2")); // 対象情報:事前発注_追加商品
    JSONArray dataArrayKOUSEIHI = JSONArray.fromObject(map.get("DATA_KOUSEIHI")); // 対象情報:事前発注_構成比

    ArrayList<String> paramData = new ArrayList<>();
    JSONObject option = new JSONObject();
    String szLstno = map.get("LSTNO"); // リスト№
    String szBmncd = map.get("BMNCD"); // 部門コード
    String tenpoCd = userInfo.getTenpo();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String values = "";
    StringBuffer sbSQL = new StringBuffer();

    int kouseihiColnum = 3;

    if (dataArrayKOUSEIHI.size() > 0) {
      paramData.add(szLstno);
      paramData.add(szBmncd);
      paramData.add(tenpoCd);
      values += "(? , ? , ? , ? , ? , ?, '" + userId + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP )";
      for (int i = 1; i <= kouseihiColnum; i++) {
        String kouseihiValues = dataArrayKOUSEIHI.optJSONObject(0).optString("F" + i);
        if ("".equals(kouseihiValues)) {
          kouseihiValues = "0";

        }
        paramData.add(kouseihiValues);
      }
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKTJ_BMNCMPRT ( ");
      sbSQL.append(" LSTNO");
      sbSQL.append(",BMNCD");
      sbSQL.append(",TENCD");
      sbSQL.append(",SURICMPRT");
      sbSQL.append(",SGRSCMPRT");
      sbSQL.append(",GRSYSANAM");
      sbSQL.append(",OPERATOR ");
      sbSQL.append(",ADDDT ");
      sbSQL.append(",UPDDT ");
      sbSQL.append(") VALUES ");
      sbSQL.append(values);
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" LSTNO = VALUES(LSTNO)");
      sbSQL.append(",BMNCD = VALUES(BMNCD)");
      sbSQL.append(",TENCD = VALUES(TENCD)");
      sbSQL.append(",SURICMPRT = VALUES(SURICMPRT)");
      sbSQL.append(",SGRSCMPRT = VALUES(SGRSCMPRT)");
      sbSQL.append(",GRSYSANAM = VALUES(GRSYSANAM)");
      sbSQL.append(",OPERATOR = VALUES(OPERATOR) ");
      sbSQL.append(",UPDDT = VALUES(UPDDT) ");

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_構成比");

      // クリア
      paramData = new ArrayList<>();
      values = "";
    }

    // 登録処理：事前発注_部門予算
    int ysnColNum = 5;
    // int count = 0;

    for (int i = 0; i < dataArrayYSN.size(); i++) {
      for (int j = 1; j <= ysnColNum; j++) {
        if (j == 5) {
          paramData.add(dataArray.optJSONObject(0).optString("F5"));
          values += " ? , '" + userId + "', CURRENT_TIMESTAMP, CURENT_TIMESTAMP)";
        } else if (j == 1) {
          if (!StringUtils.isEmpty(dataArrayYSN.optJSONObject(i).optString("F" + j))) {
            paramData.add(dataArrayYSN.optJSONObject(i).optString("F" + j));
            values += ",(? , ";
          } else {
            values += ",(null , ";
          }
        } else {
          if (j == 2) {
            if (!StringUtils.isEmpty(dataArrayYSN.optJSONObject(i).optString("F" + j))) {
              paramData.add(dataArrayYSN.optJSONObject(i).optString("F" + j));
              values += "?, ";
            } else {
              values += " null, ";
            }
          } else {
            if (j == 4) {
              paramData.add(szLstno);
              values += "?, ";
            } else if (j == 3) {
              paramData.add(tenpoCd);
              values += "?, ";
            }
          }
        }
      }
    }
    if (dataArrayYSN.size() > 0) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKTJ_BMNYSAN ( ");
      sbSQL.append(" BMNYSANAM"); // 発注数_01 F5
      sbSQL.append(",TJDT"); // 表示順番 F3
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",LSTNO"); // リスト番号 F1
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",OPERATOR "); // オペレーター：
      sbSQL.append(",ADDDT "); // 登録日：
      sbSQL.append(",UPDDT "); // 更新日：
      sbSQL.append(") VALUES ");
      sbSQL.append(values);
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" BMNYSANAM = VALUES(BMNYSANAM)"); // 発注数_01 F5
      sbSQL.append(",TJDT = VALUES(TJDT)"); // 表示順番 F3
      sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード F4
      sbSQL.append(",LSTNO = VALUES(LSTNO)"); // リスト番号 F1
      sbSQL.append(",BMNCD = VALUES(BMNCD)"); // 部門 F2
      sbSQL.append(",OPERATOR = VALUES(OPERATOR)"); // オペレーター：
      sbSQL.append(",UPDDT = VALUES(UPDDT)"); // 更新日：

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_部門予算");

      // クリア
      paramData = new ArrayList<>();
      values = "";
    }

    // 登録処理：事前発注_店舗
    values = "";
    int shnColNum = 14;
    String shnValues = "";
    paramData = new ArrayList<>();
    for (int i = 0; i < dataArraySHN.size(); i++) {
      for (int j = 1; j <= shnColNum; j++) {
        shnValues = dataArraySHN.optJSONObject(i).optString("F" + j);

        if (j == 14) {
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            values += " ? ,0 , '" + userId + "' ,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
          } else {
            values += " null ,0 ,'" + userId + "' .CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
          }
        } else if (j == 1) {
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            values += ",(? , ";
          } else {
            values += ",(null , ";
          }
        } else if (j == 4) {
          paramData.add(tenpoCd);
          values += "?, ";
        } else {
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            values += "?, ";
          } else {
            values += " null, ";
          }
        }
      }
    }
    if (dataArraySHN.size() > 0) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKTJ_TEN ( ");
      sbSQL.append(" LSTNO"); // リスト番号 F1
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",HYOSEQNO"); // 表示順番 F3
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",HTSU_01"); // 発注数_01 F5
      sbSQL.append(",HTSU_02"); // 発注数_02 F6
      sbSQL.append(",HTSU_03"); // 発注数_03 F7
      sbSQL.append(",HTSU_04"); // 発注数_04 F8
      sbSQL.append(",HTSU_05"); // 発注数_05 F9
      sbSQL.append(",HTSU_06"); // 発注数_06 F10
      sbSQL.append(",HTSU_07"); // 発注数_07 F11
      sbSQL.append(",HTSU_08"); // 発注数_08 F12
      sbSQL.append(",HTSU_09"); // 発注数_09 F13
      sbSQL.append(",HTSU_10"); // 発注数_10 F14
      sbSQL.append(",SENDFLG"); // 送信フラグ
      sbSQL.append(",OPERATOR"); // オペレーター：
      sbSQL.append(",ADDDT"); // 登録日：
      sbSQL.append(",UPDDT"); // 更新日：
      sbSQL.append(") VALUES ");
      sbSQL.append(values);
      sbSQL.append("  ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" LSTNO = VALUES(LSTNO)"); // リスト番号 F1
      sbSQL.append(",BMNCD = VALUES(BMNCD)"); // 部門 F2
      sbSQL.append(",HYOSEQNO = VALUES(HYOSEQNO)"); // 表示順番 F3
      sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード F4
      sbSQL.append(",HTSU_01 = VALUES(HTSU_01)"); // 発注数_01 F5
      sbSQL.append(",HTSU_02 = VALUES(HTSU_02)"); // 発注数_02 F6
      sbSQL.append(",HTSU_03 = VALUES(HTSU_03)"); // 発注数_03 F7
      sbSQL.append(",HTSU_04 = VALUES(HTSU_04)"); // 発注数_04 F8
      sbSQL.append(",HTSU_05 = VALUES(HTSU_05)"); // 発注数_05 F9
      sbSQL.append(",HTSU_06 = VALUES(HTSU_06)"); // 発注数_06 F10
      sbSQL.append(",HTSU_07 = VALUES(HTSU_07)"); // 発注数_07 F11
      sbSQL.append(",HTSU_08 = VALUES(HTSU_08)"); // 発注数_08 F12
      sbSQL.append(",HTSU_09 = VALUES(HTSU_09)"); // 発注数_09 F13
      sbSQL.append(",HTSU_10 = VALUES(HTSU_10)"); // 発注数_10 F14
      sbSQL.append(",SENDFLG = VALUES(SENDFLG)"); // 送信フラグ
      sbSQL.append(",OPERATOR = VALUES(OPERATOR)"); // オペレーター：
      sbSQL.append(",UPDDT = VALUES(UPDDT)"); // 更新日：

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_店舗");

      // クリア
      paramData = new ArrayList<>();
      values = "";
    }


    // 使用されている表示番号の最大値を取得
    String hyoseqNo = "";
    if (dataArraySHN2.size() > 0) {
      paramData = new ArrayList<>();
      paramData.add(szLstno);
      paramData.add(szBmncd);
      paramData.add(tenpoCd);
      new ItemList();

      String sqlcommand = "with INP as (select LSTNO, BMNCD, TENCD" + " from (values (cast(? as varchar), cast(? as varchar), cast(? as varchar))) as X(LSTNO, BMNCD, TENCD)"
          + ") select MAX(T.HYOSEQNO) as VALUE" + " from (select distinct SHN.HYOSEQNO from INATK.TOKTJ_SHN SHN" + " inner join INP on SHN.LSTNO = INP.LSTNO and SHN.BMNCD = INP.BMNCD"
          + " union all select ADS.HYOSEQNO from INATK.TOKTJ_ADDSHN ADS" + " inner join INP on ADS.LSTNO = INP.LSTNO and ADS.BMNCD = INP.BMNCD and ADS.TENCD = INP.TENCD) T";

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      hyoseqNo = array.getJSONObject(0).optString("VALUE");
    }

    // 登録処理：事前発注_追加商品
    values = "";
    shnColNum = 24;
    shnValues = "";
    paramData = new ArrayList<>();
    String addHyoseqNo = hyoseqNo;
    for (int i = 0; i < dataArraySHN2.size(); i++) {
      paramData.add(szLstno);
      paramData.add(tenpoCd);
      paramData.add(szBmncd);
      values += ",(? , ? , ? , ";
      for (int j = 1; j <= shnColNum; j++) {
        String value = dataArraySHN2.getJSONObject(i).optString("F" + j);

        if (j == 3) {
          if (!StringUtils.isEmpty(value) & StringUtils.isNumeric(value)) {
            paramData.add(value);
          } else {
            addHyoseqNo = String.valueOf(Integer.parseInt(addHyoseqNo) + 1);
            paramData.add(addHyoseqNo);
          }
          values += "? , ";
        } else if (j == shnColNum) {
          if (!StringUtils.isEmpty(value)) {
            paramData.add(value);
            values += " ? ,0 ,'" + userId + "' ,CURRNET_TIMESTAMP ,CURRENT_TIMESTAMP)";
          } else {
            values += " null ,0 ,'" + userId + "' ,CURRNET_TIMESTAMP ,CURRENT_TIMESTAMP)";
          }
        } else {
          if (!StringUtils.isEmpty(value)) {
            paramData.add(value);
            values += "? , ";
          } else {
            values += "null , ";
          }
        }
      }
    }

    if (dataArraySHN2.size() > 0) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKTJ_ADDSHN ( ");
      sbSQL.append(" LSTNO"); // リスト番号 F1
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",SHNCD"); // 商品コード F5
      sbSQL.append(",BINKBN"); // 便区分 F6
      sbSQL.append(",HYOSEQNO"); // 表示順番 F3
      sbSQL.append(",HTSU_01"); // 発注数_01 F7
      sbSQL.append(",HTSU_02"); // 発注数_02 F8
      sbSQL.append(",HTSU_03"); // 発注数_03 F9
      sbSQL.append(",HTSU_04"); // 発注数_04 F10
      sbSQL.append(",HTSU_05"); // 発注数_05 F11
      sbSQL.append(",HTSU_06"); // 発注数_06 F12
      sbSQL.append(",HTSU_07"); // 発注数_07 F13
      sbSQL.append(",HTSU_08"); // 発注数_08 F14
      sbSQL.append(",HTSU_09"); // 発注数_09 F15
      sbSQL.append(",HTSU_10"); // 発注数_10 F16
      sbSQL.append(",JTDT_01"); // 日付_01 F16
      sbSQL.append(",JTDT_02"); // 日付_02 F16
      sbSQL.append(",JTDT_03"); // 日付_03 F16
      sbSQL.append(",JTDT_04"); // 日付_04 F16
      sbSQL.append(",JTDT_05"); // 日付_05 F16
      sbSQL.append(",JTDT_06"); // 日付_06 F16
      sbSQL.append(",JTDT_07"); // 日付_07 F16
      sbSQL.append(",JTDT_08"); // 日付_08 F16
      sbSQL.append(",JTDT_09"); // 日付_09 F16
      sbSQL.append(",JTDT_10"); // 日付_10 F16
      sbSQL.append(",PAGENO"); // ページ番号
      sbSQL.append(",SENDFLG"); // 送信フラグ
      sbSQL.append(",OPERATOR "); // オペレーター：
      sbSQL.append(",ADDDT "); // 登録日：
      sbSQL.append(",UPDDT "); // 更新日：
      sbSQL.append(") VALUES ");
      sbSQL.append(values);
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" LSTNO = VALUES(LSTNO)"); // リスト番号 F1
      sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード F4
      sbSQL.append(",BMNCD = VALUES(BMNCD)"); // 部門 F2
      sbSQL.append(",SHNCD = VALUES(SHNCD)"); // 商品コード F5
      sbSQL.append(",BINKBN = VALUES(BINKBN)"); // 便区分 F6
      sbSQL.append(",HYOSEQNO = VALUES(HYOSEQNO)"); // 表示順番 F3
      sbSQL.append(",HTSU_01 = VALUES(HTSU_01)"); // 発注数_01 F7
      sbSQL.append(",HTSU_02 = VALUES(HTSU_02)"); // 発注数_02 F8
      sbSQL.append(",HTSU_03 = VALUES(HTSU_03)"); // 発注数_03 F9
      sbSQL.append(",HTSU_04 = VALUES(HTSU_04)"); // 発注数_04 F10
      sbSQL.append(",HTSU_05 = VALUES(HTSU_05)"); // 発注数_05 F11
      sbSQL.append(",HTSU_06 = VALUES(HTSU_06)"); // 発注数_06 F12
      sbSQL.append(",HTSU_07 = VALUES(HTSU_07)"); // 発注数_07 F13
      sbSQL.append(",HTSU_08 = VALUES(HTSU_08)"); // 発注数_08 F14
      sbSQL.append(",HTSU_09 = VALUES(HTSU_09)"); // 発注数_09 F15
      sbSQL.append(",HTSU_10 = VALUES(HTSU_10)"); // 発注数_10 F16
      sbSQL.append(",JTDT_01 = VALUES(JTDT_01)"); // 日付_01 F16
      sbSQL.append(",JTDT_02 = VALUES(JTDT_02)"); // 日付_02 F16
      sbSQL.append(",JTDT_03 = VALUES(JTDT_03)"); // 日付_03 F16
      sbSQL.append(",JTDT_04 = VALUES(JTDT_04)"); // 日付_04 F16
      sbSQL.append(",JTDT_05 = VALUES(JTDT_05)"); // 日付_05 F16
      sbSQL.append(",JTDT_06 = VALUES(JTDT_06)"); // 日付_06 F16
      sbSQL.append(",JTDT_07 = VALUES(JTDT_07)"); // 日付_07 F16
      sbSQL.append(",JTDT_08 = VALUES(JTDT_08)"); // 日付_08 F16
      sbSQL.append(",JTDT_09 = VALUES(JTDT_09)"); // 日付_09 F16
      sbSQL.append(",JTDT_10 = VALUES(JTDT_10)"); // 日付_10 F16
      sbSQL.append(",PAGENO = VALUES(PAGENO)"); // ページ番号
      sbSQL.append(",SENDFLG = VALUES(SENDFLG)"); // 送信フラグ
      sbSQL.append(",OPERATOR = VALUES(OPERATOR)"); // オペレーター：
      sbSQL.append(",UPDDT = VALUES(UPDDT)"); // 更新日：

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_追加商品");

      // クリア
      paramData = new ArrayList<>();
      values = "";

    }

    // ジャーナルへの登録処理を行う
    this.createSqlTOKTJ_JNL(map, userInfo);

    // ワークテーブルの削除を行う
    this.createSqlDelWorkData(userInfo, map);

    // 登録処理
    ArrayList<Integer> countList = new ArrayList<>();
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
   * 事前発注JNL INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTOKTJ_JNL(HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONArray dataArrayYSN = JSONArray.fromObject(map.get("DATA_YSN")); // 対象情報:事前発注_部門予算
    JSONArray dataArraySHN = JSONArray.fromObject(map.get("DATA_SHN")); // 対象情報:事前発注_店舗
    JSONArray dataArraySHN2 = JSONArray.fromObject(map.get("DATA_SHN2")); // 対象情報:事前発注_追加商品
    JSONArray dataArrayKOUSEIHI = JSONArray.fromObject(map.get("DATA_KOUSEIHI")); // 対象情報:事前発注_構成比

    String szLstno = map.get("LSTNO"); // リスト№
    String szBmncd = map.get("BMNCD"); // 部門コード
    String tenpoCd = userInfo.getTenpo();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> paramData = new ArrayList<>();
    ArrayList<String> SearchParams = new ArrayList<>();
    String values = "";

    // ジャーナル情報の登録

    // 登録処理：事前発注_構成比_JNL
    int kouseihiColnum = 3;
    paramData = new ArrayList<>();
    if (dataArrayKOUSEIHI.size() > 0) {

      // 変更データか確認を行う
      SearchParams = new ArrayList<>();
      SearchParams.add(szLstno);
      SearchParams.add(szBmncd);
      SearchParams.add(tenpoCd);

      if (this.checkExistData("TOKTJ_BMNCMPRT", SearchParams)) {
        paramData = new ArrayList<>();
        paramData.add(this.get_SEQ_TOKTJ004());
        paramData.add(szLstno);
        paramData.add(szBmncd);
        paramData.add(tenpoCd);
        values += "(? ,?, ?, ?, ?, ?, ?, '" + userId + "', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        for (int i = 1; i <= kouseihiColnum; i++) {
          String kouseihiValues = dataArrayKOUSEIHI.optJSONObject(0).optString("F" + i);
          if ("".equals(kouseihiValues)) {
            kouseihiValues = "0";
          }
          paramData.add(kouseihiValues);
        }
      }
      if (StringUtils.isNotEmpty(values)) {
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.JNL_TOKTJ_BMNCMPRT ( ");
        sbSQL.append(" SEQ");
        sbSQL.append(",LSTNO");
        sbSQL.append(",BMNCD");
        sbSQL.append(",TENCD");
        sbSQL.append(",SURICMPRT");
        sbSQL.append(",SGRSCMPRT");
        sbSQL.append(",GRSYSANAM");
        sbSQL.append(",OPERATOR");
        sbSQL.append(",ADDDT");
        sbSQL.append(",UPDDT");
        sbSQL.append(") VALUES ");
        sbSQL.append(values);
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" SEQ = VALUES(SEQ)");
        sbSQL.append(",LSTNO = VALUES(LSTNO)");
        sbSQL.append(",BMNCD = VALUES(BMNCD)");
        sbSQL.append(",TENCD = VALUES(TENCD)");
        sbSQL.append(",SURICMPRT = VALUES(SURICMPRT)");
        sbSQL.append(",SGRSCMPRT = VALUES(SGRSCMPRT)");
        sbSQL.append(",GRSYSANAM = VALUES(GRSYSANAM)");
        sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
        sbSQL.append(",UPDDT = VALUES(UPDDT)");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(paramData);
        lblList.add("事前発注_構成比_JNL");

        // クリア
        paramData = new ArrayList<>();
        values = "";
      }
    }

    // 登録処理：事前発注_部門予算_JNL
    // 変更データか確認を行う
    int ysnColNum = 5;
    // int count = 0;

    for (int i = 0; i < dataArrayYSN.size(); i++) {
      // 変更データか確認を行う
      SearchParams = new ArrayList<>();
      SearchParams.add(szLstno);
      SearchParams.add(szBmncd);
      SearchParams.add(tenpoCd);

      if (this.checkExistData("TOKTJ_BMNYSAN", SearchParams)) {
        paramData.add(this.get_SEQ_TOKTJ003());
        values += ",(? , ";
        for (int j = 1; j <= ysnColNum; j++) {

          if (j == 5) {
            paramData.add(dataArray.optJSONObject(0).optString("F5"));
            values += " ? ,'" + userId + "' ,CURRNET_TIMESTAMP ,CURRENT_STAMP)";
          } else if (j == 1) {
            if (!StringUtils.isEmpty(dataArrayYSN.optJSONObject(i).optString("F" + j))) {
              paramData.add(dataArrayYSN.optJSONObject(i).optString("F" + j));
              // values += ",(? , ";
              values += "?, ";
            } else {
              // values += ",(null , ";
              values += "null , ";
            }
          } else {
            if (j == 2) {
              if (!StringUtils.isEmpty(dataArrayYSN.optJSONObject(i).optString("F" + j))) {
                paramData.add(dataArrayYSN.optJSONObject(i).optString("F" + j));
                values += "?, ";
              } else {
                values += " null, ";
              }
            } else {
              if (j == 4) {
                paramData.add(szLstno);
                values += "?, ";
              } else if (j == 3) {
                paramData.add(tenpoCd);
                values += "?, ";
              }
            }
          }
        }
      }
    }
    if (StringUtils.isNotEmpty(values)) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.JNL_TOKTJ_BMNYSAN ( ");
      sbSQL.append(" SEQ");
      sbSQL.append(",BMNYSANAM"); // 発注数_01 F5
      sbSQL.append(",TJDT"); // 表示順番 F3
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",LSTNO"); // リスト番号 F1
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",OPERATOR "); // オペレーター
      sbSQL.append(",ADDDT "); // 登録日
      sbSQL.append(",UPDDT "); // 更新日
      sbSQL.append(") VALUES ");
      sbSQL.append(values);
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" SEQ = VALUES(SEQ)");
      sbSQL.append(",BMNYSANAM = VALUES(BMNYSANAM)"); // 発注数_01 F5
      sbSQL.append(",TJDT = VALUES(TJDT)"); // 表示順番 F3
      sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード F4
      sbSQL.append(",LSTNO = VALUES(LSTNO)"); // リスト番号 F1
      sbSQL.append(",BMNCD = VALUES(BMNCD)"); // 部門 F2
      sbSQL.append(",OPERATOR = VALUES(OPERATOR)"); // オペレーター
      sbSQL.append(",UPDDT = VALUES(UPDDT)"); // 更新日

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_部門予算_JNL");

      // クリア
      paramData = new ArrayList<>();
      values = "";
    }

    // 登録処理：事前発注_店舗_JNL
    values = "";
    int shnColNum = 14;
    String shnValues = "";
    String List = "";
    String Bumon = "";
    String No = "";
    String Tenpo = "";
    paramData = new ArrayList<>();
    for (int i = 0; i < dataArraySHN.size(); i++) {
      paramData.add(this.get_SEQ_TOKTJ001()); // F1 : SEQ
      values += ",(SELECT ? , ";
      for (int j = 1; j <= shnColNum; j++) {
        shnValues = dataArraySHN.optJSONObject(i).optString("F" + j);

        if (j == 14) { // F47: 発注数_10
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            values += " ? ,PAGENO,0 ,'" + userId + "' ,CURRENT_TIMESTAMP ,CURRENT_TIMESTAMP ";
            values += "FROM INATK.TOKTJ_TEN ";
            values += "WHERE LSTNO =" + List;
            values += " AND BMNCD =" + Bumon;
            values += " AND HYOSEQNO =" + No;
            values += " AND TENCD =" + Tenpo;
            values += ") ";
          } else {
            values += " null,PAGENO,0 ,'" + userId + "' ,CURRENT_TIMESTAMP ,CURRENT_TIMESTAMP ";
            values += "FROM INATK.TOKTJ_TEN ";
            values += "WHERE LSTNO =" + List;
            values += " AND BMNCD =" + Bumon;
            values += " AND HYOSEQNO =" + No;
            values += " AND TENCD =" + Tenpo;
            values += ") ";
          }
        } else if (j == 1) { // F2 : リスト№
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            List = shnValues;
            values += "? , ";
          } else {
            values += "null , ";
          }

        } else if (j == 2) { // F3 : 部門
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            Bumon = shnValues;
            values += "?, ";
          } else {
            values += " null, ";
          }
        } else if (j == 3) { // F4 : 表示順番
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            No = shnValues;
            values += "?, ";
          } else {
            values += " null, ";
          }
        } else if (j == 4) { // F5 : 店コード
          paramData.add(tenpoCd);
          Tenpo = tenpoCd;
          values += "?, ";
          values += "IRISU_RG,IRISU_TB,SBAIKAAM_TB,BAIKAAM_TB,SBAIKAAM_RG,BAIKAAM_RG,GENKAAM_MAE,GENKAAM_ATO,GENKAAM_RG,GENKAAM_PACK,BAIKAAM_PACK,";
          values += "JTDT_01,JTDT_02,JTDT_03,JTDT_04,JTDT_05,JTDT_06,JTDT_07,JTDT_08,JTDT_09,JTDT_10,";
          values += "TSEIKBN_01,TSEIKBN_02,TSEIKBN_03,TSEIKBN_04,TSEIKBN_05,TSEIKBN_06,TSEIKBN_07,TSEIKBN_08,TSEIKBN_09,TSEIKBN_10,";
          values += "SHNKBN_01,SHNKBN_02,SHNKBN_03,SHNKBN_04,SHNKBN_05,SHNKBN_06,SHNKBN_07,SHNKBN_08,SHNKBN_09,SHNKBN_10,";
        } else {
          if (!StringUtils.isEmpty(shnValues)) {
            paramData.add(shnValues);
            values += "?, ";
          } else {
            values += " null, ";
          }
        }

      }
    }
    if (dataArraySHN.size() > 0) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.JNL_TOKTJ_TEN ( ");
      sbSQL.append("  SEQ"); // F1 : SEQ
      sbSQL.append(" ,LSTNO"); // F2 : リスト№
      sbSQL.append(" ,BMNCD"); // F3 : 部門
      sbSQL.append(" ,HYOSEQNO"); // F4 : 表示順番
      sbSQL.append(" ,TENCD"); // F5 : 店コード
      sbSQL.append(" ,IRISU_RG"); // F6 : 入数_標準
      sbSQL.append(" ,IRISU_TB"); // F7 : 入数_特売
      sbSQL.append(" ,SBAIKAAM_TB"); // F8 : 特売総額売価
      sbSQL.append(" ,BAIKAAM_TB"); // F9 : 特売本体売価
      sbSQL.append(" ,SBAIKAAM_RG"); // F10: 標準総額売価
      sbSQL.append(" ,BAIKAAM_RG"); // F11: 標準本体売価
      sbSQL.append(" ,GENKAAM_MAE"); // F12: 事前原価
      sbSQL.append(" ,GENKAAM_ATO"); // F13: 追加原価
      sbSQL.append(" ,GENKAAM_RG"); // F14: 標準原価
      sbSQL.append(" ,GENKAAM_PACK"); // F15: パック原価
      sbSQL.append(" ,BAIKAAM_PACK"); // F16: パック売価
      sbSQL.append(" ,JTDT_01"); // F17: 日付_01
      sbSQL.append(" ,JTDT_02"); // F18: 日付_02
      sbSQL.append(" ,JTDT_03"); // F19: 日付_03
      sbSQL.append(" ,JTDT_04"); // F20: 日付_04
      sbSQL.append(" ,JTDT_05"); // F21: 日付_05
      sbSQL.append(" ,JTDT_06"); // F22: 日付_06
      sbSQL.append(" ,JTDT_07"); // F23: 日付_07
      sbSQL.append(" ,JTDT_08"); // F24: 日付_08
      sbSQL.append(" ,JTDT_09"); // F25: 日付_09
      sbSQL.append(" ,JTDT_10"); // F26: 日付_10
      sbSQL.append(" ,TSEIKBN_01"); // F27: 訂正区分_01
      sbSQL.append(" ,TSEIKBN_02"); // F28: 訂正区分_02
      sbSQL.append(" ,TSEIKBN_03"); // F29: 訂正区分_03
      sbSQL.append(" ,TSEIKBN_04"); // F30: 訂正区分_04
      sbSQL.append(" ,TSEIKBN_05"); // F31: 訂正区分_05
      sbSQL.append(" ,TSEIKBN_06"); // F32: 訂正区分_06
      sbSQL.append(" ,TSEIKBN_07"); // F33: 訂正区分_07
      sbSQL.append(" ,TSEIKBN_08"); // F34: 訂正区分_08
      sbSQL.append(" ,TSEIKBN_09"); // F35: 訂正区分_09
      sbSQL.append(" ,TSEIKBN_10"); // F36: 訂正区分_10
      sbSQL.append(" ,SHNKBN_01"); // F37: 商品区分_01
      sbSQL.append(" ,SHNKBN_02"); // F38: 商品区分_02
      sbSQL.append(" ,SHNKBN_03"); // F39: 商品区分_03
      sbSQL.append(" ,SHNKBN_04"); // F40: 商品区分_04
      sbSQL.append(" ,SHNKBN_05"); // F41: 商品区分_05
      sbSQL.append(" ,SHNKBN_06"); // F42: 商品区分_06
      sbSQL.append(" ,SHNKBN_07"); // F43: 商品区分_07
      sbSQL.append(" ,SHNKBN_08"); // F44: 商品区分_08
      sbSQL.append(" ,SHNKBN_09"); // F45: 商品区分_09
      sbSQL.append(" ,SHNKBN_10"); // F46: 商品区分_10
      sbSQL.append(" ,HTSU_01"); // F47: 発注数_01
      sbSQL.append(" ,HTSU_02"); // F48: 発注数_02
      sbSQL.append(" ,HTSU_03"); // F49: 発注数_03
      sbSQL.append(" ,HTSU_04"); // F50: 発注数_04
      sbSQL.append(" ,HTSU_05"); // F51: 発注数_05
      sbSQL.append(" ,HTSU_06"); // F52: 発注数_06
      sbSQL.append(" ,HTSU_07"); // F53: 発注数_07
      sbSQL.append(" ,HTSU_08"); // F54: 発注数_08
      sbSQL.append(" ,HTSU_09"); // F55: 発注数_09
      sbSQL.append(" ,HTSU_10"); // F56: 発注数_10
      sbSQL.append(" ,PAGENO"); // F57: ページ番号
      sbSQL.append(" ,SENDFLG"); // F58: 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F59: オペレータ
      sbSQL.append(" ,ADDDT"); // F60: 登録日
      sbSQL.append(" ,UPDDT"); // F61: 更新日
      sbSQL.append(") ");
      sbSQL.append(values);
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SEQ = VALUES(SEQ)"); // F1 : SEQ
      sbSQL.append(" ,LSTNO = VALUES(LSTNO)"); // F2 : リスト№
      sbSQL.append(" ,BMNCD = VALUES(BMNCD)"); // F3 : 部門
      sbSQL.append(" ,HYOSEQNO = VALUES(HYOSEQNO)"); // F4 : 表示順番
      sbSQL.append(" ,TENCD = VALUES(TENCD)"); // F5 : 店コード
      sbSQL.append(" ,IRISU_RG = VALUES(IRISU_RG)"); // F6 : 入数_標準
      sbSQL.append(" ,IRISU_TB = VALUES(IRISU_TB)"); // F7 : 入数_特売
      sbSQL.append(" ,SBAIKAAM_TB = VALUES(SBAIKAAM_TB)"); // F8 : 特売総額売価
      sbSQL.append(" ,BAIKAAM_TB = VALUES(BAIKAAM_TB)"); // F9 : 特売本体売価
      sbSQL.append(" ,SBAIKAAM_RG = VALUES(SBAIKAAM_RG)"); // F10: 標準総額売価
      sbSQL.append(" ,BAIKAAM_RG = VALUES(BAIKAAM_RG)"); // F11: 標準本体売価
      sbSQL.append(" ,GENKAAM_MAE = VALUES(GENKAAM_MAE)"); // F12: 事前原価
      sbSQL.append(" ,GENKAAM_ATO = VALUES(GENKAAM_ATO)"); // F13: 追加原価
      sbSQL.append(" ,GENKAAM_RG = VALUES(GENKAAM_RG)"); // F14: 標準原価
      sbSQL.append(" ,GENKAAM_PACK = VALUES(GENKAAM_PACK)"); // F15: パック原価
      sbSQL.append(" ,BAIKAAM_PACK = VALUES(BAIKAAM_PACK)"); // F16: パック売価
      sbSQL.append(" ,JTDT_01 = VALUES(JTDT_01)"); // F17: 日付_01
      sbSQL.append(" ,JTDT_02 = VALUES(JTDT_02)"); // F18: 日付_02
      sbSQL.append(" ,JTDT_03 = VALUES(JTDT_03)"); // F19: 日付_03
      sbSQL.append(" ,JTDT_04 = VALUES(JTDT_04)"); // F20: 日付_04
      sbSQL.append(" ,JTDT_05 = VALUES(JTDT_05)"); // F21: 日付_05
      sbSQL.append(" ,JTDT_06 = VALUES(JTDT_06)"); // F22: 日付_06
      sbSQL.append(" ,JTDT_07 = VALUES(JTDT_07)"); // F23: 日付_07
      sbSQL.append(" ,JTDT_08 = VALUES(JTDT_08)"); // F24: 日付_08
      sbSQL.append(" ,JTDT_09 = VALUES(JTDT_09)"); // F25: 日付_09
      sbSQL.append(" ,JTDT_10 = VALUES(JTDT_10)"); // F26: 日付_10
      sbSQL.append(" ,TSEIKBN_01 = VALUES(TSEIKBN_01)"); // F27: 訂正区分_01
      sbSQL.append(" ,TSEIKBN_02 = VALUES(TSEIKBN_02)"); // F28: 訂正区分_02
      sbSQL.append(" ,TSEIKBN_03 = VALUES(TSEIKBN_03)"); // F29: 訂正区分_03
      sbSQL.append(" ,TSEIKBN_04 = VALUES(TSEIKBN_04)"); // F30: 訂正区分_04
      sbSQL.append(" ,TSEIKBN_05 = VALUES(TSEIKBN_05)"); // F31: 訂正区分_05
      sbSQL.append(" ,TSEIKBN_06 = VALUES(TSEIKBN_06)"); // F32: 訂正区分_06
      sbSQL.append(" ,TSEIKBN_07 = VALUES(TSEIKBN_07)"); // F33: 訂正区分_07
      sbSQL.append(" ,TSEIKBN_08 = VALUES(TSEIKBN_08)"); // F34: 訂正区分_08
      sbSQL.append(" ,TSEIKBN_09 = VALUES(TSEIKBN_09)"); // F35: 訂正区分_09
      sbSQL.append(" ,TSEIKBN_10 = VALUES(TSEIKBN_10)"); // F36: 訂正区分_10
      sbSQL.append(" ,SHNKBN_01 = VALUES(SHNKBN_01)"); // F37: 商品区分_01
      sbSQL.append(" ,SHNKBN_02 = VALUES(SHNKBN_02)"); // F38: 商品区分_02
      sbSQL.append(" ,SHNKBN_03 = VALUES(SHNKBN_03)"); // F39: 商品区分_03
      sbSQL.append(" ,SHNKBN_04 = VALUES(SHNKBN_04)"); // F40: 商品区分_04
      sbSQL.append(" ,SHNKBN_05 = VALUES(SHNKBN_05)"); // F41: 商品区分_05
      sbSQL.append(" ,SHNKBN_06 = VALUES(SHNKBN_06)"); // F42: 商品区分_06
      sbSQL.append(" ,SHNKBN_07 = VALUES(SHNKBN_07)"); // F43: 商品区分_07
      sbSQL.append(" ,SHNKBN_08 = VALUES(SHNKBN_08)"); // F44: 商品区分_08
      sbSQL.append(" ,SHNKBN_09 = VALUES(SHNKBN_09)"); // F45: 商品区分_09
      sbSQL.append(" ,SHNKBN_10 = VALUES(SHNKBN_10)"); // F46: 商品区分_10
      sbSQL.append(" ,HTSU_01 = VALUES(HTSU_01)"); // F47: 発注数_01
      sbSQL.append(" ,HTSU_02 = VALUES(HTSU_02)"); // F48: 発注数_02
      sbSQL.append(" ,HTSU_03 = VALUES(HTSU_03)"); // F49: 発注数_03
      sbSQL.append(" ,HTSU_04 = VALUES(HTSU_04)"); // F50: 発注数_04
      sbSQL.append(" ,HTSU_05 = VALUES(HTSU_05)"); // F51: 発注数_05
      sbSQL.append(" ,HTSU_06 = VALUES(HTSU_06)"); // F52: 発注数_06
      sbSQL.append(" ,HTSU_07 = VALUES(HTSU_07)"); // F53: 発注数_07
      sbSQL.append(" ,HTSU_08 = VALUES(HTSU_08)"); // F54: 発注数_08
      sbSQL.append(" ,HTSU_09 = VALUES(HTSU_09)"); // F55: 発注数_09
      sbSQL.append(" ,HTSU_10 = VALUES(HTSU_10)"); // F56: 発注数_10
      sbSQL.append(" ,PAGENO = VALUES(PAGENO)"); // F57: ページ番号
      sbSQL.append(" ,SENDFLG = VALUES(SENDFLG)"); // F58: 送信フラグ
      sbSQL.append(" ,OPERATOR = VALUES(OPERATOR)"); // F59: オペレータ
      sbSQL.append(" ,UPDDT = VALUES(UPDDT)"); // F61: 更新日

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_店舗_JNL");

      // クリア
      paramData = new ArrayList<>();
      values = "";
    }

    // 登録処理：事前発注_追加商品_JNL
    values = "";
    shnColNum = 23;
    paramData = new ArrayList<>();
    for (int i = 0; i < dataArraySHN2.size(); i++) {

      // 変更データ(表示番号が存在するデータ)の場合のみ追加する
      String hyoziNo = dataArraySHN2.getJSONObject(i).optString("F3");
      if (StringUtils.isNotEmpty(hyoziNo) & StringUtils.isNumeric(hyoziNo)) {
        paramData.add(this.get_SEQ_TOKTJ002());
        paramData.add(szLstno);
        paramData.add(tenpoCd);
        paramData.add(szBmncd);
        values += ",(? , ? , ? , ? , ";
        for (int j = 1; j <= shnColNum; j++) {
          String value = dataArraySHN2.getJSONObject(i).optString("F" + j);

          if (j == 3) {
            if (!StringUtils.isEmpty(value)) {
              paramData.add(value);
            }
            values += "? , ";
          } else if (j == 23) {
            if (!StringUtils.isEmpty(value)) {
              paramData.add(value);
              values += " ? ) ";
            } else {
              values += " null ) ";
            }
          } else {
            if (!StringUtils.isEmpty(value)) {
              paramData.add(value);
              values += "? , ";
            } else {
              values += "null , ";
            }
          }
        }
      }
    }

    if (StringUtils.isNotEmpty(values)) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("MERGE INTO INATK.JNL_TOKTJ_ADDSHN AS T USING (SELECT ");
      sbSQL.append(" SEQ"); // SEQ F1
      sbSQL.append(",LSTNO"); // リスト番号 F1
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",SHNCD"); // 商品コード F5
      sbSQL.append(",BINKBN"); // 便区分 F6
      sbSQL.append(",HYOSEQNO"); // 表示順番 F3
      sbSQL.append(",HTSU_01"); // 発注数_01 F7
      sbSQL.append(",HTSU_02"); // 発注数_02 F8
      sbSQL.append(",HTSU_03"); // 発注数_03 F9
      sbSQL.append(",HTSU_04"); // 発注数_04 F10
      sbSQL.append(",HTSU_05"); // 発注数_05 F11
      sbSQL.append(",HTSU_06"); // 発注数_06 F12
      sbSQL.append(",HTSU_07"); // 発注数_07 F13
      sbSQL.append(",HTSU_08"); // 発注数_08 F14
      sbSQL.append(",HTSU_09"); // 発注数_09 F15
      sbSQL.append(",HTSU_10"); // 発注数_10 F16
      sbSQL.append(",JTDT_01"); // 日付_01 F16
      sbSQL.append(",JTDT_02"); // 日付_02 F16
      sbSQL.append(",JTDT_03"); // 日付_03 F16
      sbSQL.append(",JTDT_04"); // 日付_04 F16
      sbSQL.append(",JTDT_05"); // 日付_05 F16
      sbSQL.append(",JTDT_06"); // 日付_06 F16
      sbSQL.append(",JTDT_07"); // 日付_07 F16
      sbSQL.append(",JTDT_08"); // 日付_08 F16
      sbSQL.append(",JTDT_09"); // 日付_09 F16
      sbSQL.append(",JTDT_10"); // 日付_10 F16
      sbSQL.append(",0 as SENDFLG"); // 送信フラグ
      sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
      sbSQL.append(", current timestamp AS ADDDT "); // 登録日：
      sbSQL.append(", current timestamp AS UPDDT "); // 更新日：
      sbSQL.append(" FROM (values " + values + " ) as T1(");
      sbSQL.append(" SEQ"); // SEQ F1
      sbSQL.append(",LSTNO"); // リスト番号 F1
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",SHNCD"); // 商品コード F5
      sbSQL.append(",BINKBN"); // 便区分 F6
      sbSQL.append(",HYOSEQNO"); // 表示順番 F3
      sbSQL.append(",HTSU_01"); // 発注数_01 F7
      sbSQL.append(",HTSU_02"); // 発注数_02 F8
      sbSQL.append(",HTSU_03"); // 発注数_03 F9
      sbSQL.append(",HTSU_04"); // 発注数_04 F10
      sbSQL.append(",HTSU_05"); // 発注数_05 F11
      sbSQL.append(",HTSU_06"); // 発注数_06 F12
      sbSQL.append(",HTSU_07"); // 発注数_07 F13
      sbSQL.append(",HTSU_08"); // 発注数_08 F14
      sbSQL.append(",HTSU_09"); // 発注数_09 F15
      sbSQL.append(",HTSU_10"); // 発注数_10 F16
      sbSQL.append(",JTDT_01"); // 発注数_10 F16
      sbSQL.append(",JTDT_02"); // 発注数_10 F16
      sbSQL.append(",JTDT_03"); // 発注数_10 F16
      sbSQL.append(",JTDT_04"); // 発注数_10 F16
      sbSQL.append(",JTDT_05"); // 発注数_10 F16
      sbSQL.append(",JTDT_06"); // 発注数_10 F16
      sbSQL.append(",JTDT_07"); // 発注数_10 F16
      sbSQL.append(",JTDT_08"); // 発注数_10 F16
      sbSQL.append(",JTDT_09"); // 発注数_10 F16
      sbSQL.append(",JTDT_10"); // 発注数_10 F16
      sbSQL.append(" )) as RE on (");
      sbSQL.append(" T.SEQ = RE.SEQ and ");
      sbSQL.append(" T.LSTNO = RE.LSTNO and ");
      sbSQL.append(" T.BMNCD = RE.BMNCD and ");
      sbSQL.append(" T.HYOSEQNO = RE.HYOSEQNO and ");
      sbSQL.append(" T.TENCD = RE.TENCD ");
      sbSQL.append(" )  WHEN NOT MATCHED THEN INSERT ( ");
      sbSQL.append(" SEQ"); // SEQ F1
      sbSQL.append(",LSTNO"); // 販売開始日
      sbSQL.append(",TENCD"); // 部門
      sbSQL.append(",BMNCD"); // 割引率区分
      sbSQL.append(",SHNCD"); // ダミーコード
      sbSQL.append(",BINKBN"); // 管理番号
      sbSQL.append(",HYOSEQNO"); // 管理番号
      sbSQL.append(",HTSU_01"); // 発注数_01 F7
      sbSQL.append(",HTSU_02"); // 発注数_02 F8
      sbSQL.append(",HTSU_03"); // 発注数_03 F9
      sbSQL.append(",HTSU_04"); // 発注数_04 F10
      sbSQL.append(",HTSU_05"); // 発注数_05 F11
      sbSQL.append(",HTSU_06"); // 発注数_06 F12
      sbSQL.append(",HTSU_07"); // 発注数_07 F13
      sbSQL.append(",HTSU_08"); // 発注数_08 F14
      sbSQL.append(",HTSU_09"); // 発注数_09 F15
      sbSQL.append(",HTSU_10"); // 発注数_10 F16
      sbSQL.append(",JTDT_01"); // 発注数_10 F16
      sbSQL.append(",JTDT_02"); // 発注数_10 F16
      sbSQL.append(",JTDT_03"); // 発注数_10 F16
      sbSQL.append(",JTDT_04"); // 発注数_10 F16
      sbSQL.append(",JTDT_05"); // 発注数_10 F16
      sbSQL.append(",JTDT_06"); // 発注数_10 F16
      sbSQL.append(",JTDT_07"); // 発注数_10 F16
      sbSQL.append(",JTDT_08"); // 発注数_10 F16
      sbSQL.append(",JTDT_09"); // 発注数_10 F16
      sbSQL.append(",JTDT_10"); // 発注数_10 F16
      sbSQL.append(",SENDFLG"); // 送信フラグ
      sbSQL.append(",OPERATOR"); // オペレーターコード
      sbSQL.append(",ADDDT"); // 登録日
      sbSQL.append(",UPDDT"); // 更新日
      sbSQL.append(") values (");
      sbSQL.append(" RE.SEQ"); // SEQ F1
      sbSQL.append(",RE.LSTNO"); // 販売開始日
      sbSQL.append(",RE.TENCD"); // 部門
      sbSQL.append(",RE.BMNCD"); // 割引率区分
      sbSQL.append(",RE.SHNCD"); // ダミーコード
      sbSQL.append(",RE.BINKBN"); // 管理番号
      sbSQL.append(",RE.HYOSEQNO"); // 管理番号
      sbSQL.append(",RE.HTSU_01"); // 発注数_01 F7
      sbSQL.append(",RE.HTSU_02"); // 発注数_02 F8
      sbSQL.append(",RE.HTSU_03"); // 発注数_03 F9
      sbSQL.append(",RE.HTSU_04"); // 発注数_04 F10
      sbSQL.append(",RE.HTSU_05"); // 発注数_05 F11
      sbSQL.append(",RE.HTSU_06"); // 発注数_06 F12
      sbSQL.append(",RE.HTSU_07"); // 発注数_07 F13
      sbSQL.append(",RE.HTSU_08"); // 発注数_08 F14
      sbSQL.append(",RE.HTSU_09"); // 発注数_09 F15
      sbSQL.append(",RE.HTSU_10"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_01"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_02"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_03"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_04"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_05"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_06"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_07"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_08"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_09"); // 発注数_10 F16
      sbSQL.append(",RE.JTDT_10"); // 発注数_10 F16
      sbSQL.append(",RE.SENDFLG"); // 送信フラグ
      sbSQL.append(",RE.OPERATOR"); // オペレーターコード
      sbSQL.append(",RE.ADDDT"); // 登録日
      sbSQL.append(",RE.UPDDT"); // 更新日
      sbSQL.append(")");

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_追加商品_JNL");

      // クリア
      paramData = new ArrayList<>();
      values = "";

    }
    return sbSQL.toString();
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray dataArraySHN2 = JSONArray.fromObject(map.get("DATA_SHN2")); // 対象情報(追加商品情報)

    map.get(DefineReport.ID_PARAM_OBJ);

    new ItemList();
    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();
    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<>();

    String szLstno = map.get("LSTNO"); // リスト№
    String szBmncd = map.get("BMNCD"); // 部門コード
    String tenpoCd = userInfo.getTenpo(); // 店コード

    dataArray.getJSONObject(0);

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 追加商品数確認
    if (dataArraySHN2.size() > 0) {

      dataArraySHN2.optJSONObject(0);

      paramData.add(szLstno);
      paramData.add(szBmncd);
      paramData.add(tenpoCd);
      sqlcommand = "select COUNT(LSTNO) from INATK.TOKTJ_ADDSHN where LSTNO = ? and BMNCD = ? and TENCD = ? ";

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

      if (array.size() > 0) {
        if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) >= 35) {
          // 追加商品が35以上の場合、新たな商品の追加は不可
          JSONObject o = mu.getDbMessageObj("E35000", new String[] {});
          msg.add(o);
          return msg;
        }
      }

    }
    return msg;
  }

  @SuppressWarnings("static-access")
  public boolean checkExistData(String tableName, ArrayList<String> parmList) {
    boolean exist = false;
    new ItemList();
    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<>();

    for (int i = 0; i < parmList.size(); i++) {
      String value = parmList.get(i);
      paramData.add(value);
    }

    if (StringUtils.equals("JNL_TOKTJ_TEN", tableName)) {
      // 事前発注_店舗
      sqlcommand = "select LSTNO as value from INATK.TOKTJ_TEN where LSTNO = ? and BMNCD = ? and TENCD = ? and HYOSEQNO = ? ";

    } else if (StringUtils.equals("TOKTJ_BMNCMPRT", tableName)) {
      // 事前発注_部門構成比
      sqlcommand = "select LSTNO as value from INATK.TOKTJ_BMNCMPRT where LSTNO = ? and BMNCD = ? and TENCD = ?";

    } else if (StringUtils.equals("TOKTJ_ADDSHN", tableName)) {
      // 事前発注_追加商品
      sqlcommand = "select LSTNO as value from INATK.TOKTJ_ADDSHN where LSTNO = ? and BMNCD = ? and TENCD = ? and HYOSEQNO = ? ";

    } else if (StringUtils.equals("TOKTJ_BMNYSAN", tableName)) {
      // 事前発注_部門予算
      sqlcommand = "select LSTNO as value from INATK.TOKTJ_BMNYSAN where LSTNO = ? and BMNCD = ? and TENCD = ?";
    }
    JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0) {
      exist = true;
    }
    return exist;
  }

  /**
   * SEQ情報取得処理(事前発注_店舗_JNL)
   *
   * @throws Exception
   */
  public String get_SEQ_TOKTJ001() {
    new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ001') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }

  /**
   * SEQ情報取得処理(事前発注_追加商品_JNL)
   *
   * @throws Exception
   */
  public String get_SEQ_TOKTJ002() {
    new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ002') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }

  /**
   * SEQ情報取得処理(事前発注_部門予算_JNL)
   *
   * @throws Exception
   */
  public String get_SEQ_TOKTJ003() {
    new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ003') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }

  /**
   * SEQ情報取得処理(事前発注_構成比_JNL)
   *
   * @throws Exception
   */
  public String get_SEQ_TOKTJ004() {
    new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ004') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }
}
