package dao;

import java.util.ArrayList;
import java.util.List;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBW005Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBW005Dao(String JNDIname) {
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

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append("  DATE_FORMAT(DATE_FORMAT(TRKK.HBSTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TRKK.HBSTDT, '%Y%m%d')))"); // F1 ：販売開始日
    sbSQL.append(", right('0'|| TRKK.BMNCD,2)"); // F2 ：部門
    sbSQL.append(", TRKK.MEISHOKN"); // F3 ：名称（漢字）
    sbSQL.append(", MMSH1.NMKN"); // F4 ：割引率区分
    sbSQL.append(", MMSH2.NMKN"); // F5 ：正規・カット
    sbSQL.append(", CONCAT(CONCAT(left(TRKK.DUMMYCD,4),'-'),SUBSTRING(TRKK.DUMMYCD,5,4))"); // F6 ：ダミーコード（商品コード）
    sbSQL.append(", MTSH.POPKN"); // F7 ：POP名称
    sbSQL.append(", SUBSTRING(TRKK.HBSTDT,3)"); // F8 ：販売開始日(パラメータ)
    sbSQL.append(", TRKK.WRITUKBN"); // F9 ：割引率区分(パラメータ)
    sbSQL.append(", TRKK.SEICUTKBN"); // F10 ：正規・カット(パラメータ)
    sbSQL.append(", TRKK.DUMMYCD"); // F11 ：ダミーコード(パラメータ)
    sbSQL.append(", TRKK.HBSTDT"); // F11 ：ダミーコード(パラメータ)
    sbSQL.append(" from INATK.TOKRS_KKK TRKK");
    sbSQL.append(" left join INAMS.MSTSHN MTSH");
    sbSQL.append(" on MTSH.SHNCD = TRKK.DUMMYCD");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH1");
    sbSQL.append(" on MMSH1.MEISHOCD = TRKK.WRITUKBN");
    sbSQL.append(" and MMSH1.MEISHOKBN = 10302");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH2");
    sbSQL.append(" on MMSH2.MEISHOCD = TRKK.SEICUTKBN");
    sbSQL.append(" and MMSH2.MEISHOKBN = 10303");
    sbSQL.append(" where TRKK.UPDKBN = ");
    sbSQL.append(DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" order by TRKK.HBSTDT DESC , right('0'|| TRKK.BMNCD,2) ,TRKK.DUMMYCD,TRKK.SEICUTKBN,TRKK.WRITUKBN");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
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

  }



  public String createCommandFTP(String userId, JSONObject obj, String outpage) {
    obj.optString("BTN");

    obj.optString("FILE");
    obj.optString("BMNCD");
    obj.optInt("REQLEN");

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("with WK as (");
    sbSQL.append("select ");
    sbSQL.append(" cast(? as INTEGER ) as HBSTDT ");
    sbSQL.append(", cast(? as SMALLINT ) as BMNCD ");
    sbSQL.append(", cast(? as INTEGER ) as WARIBIKI ");
    sbSQL.append(", cast(? as SMALLINT ) as SEIKI ");
    sbSQL.append(", cast(? as CHARACTER (14) ) as DUMMY ");
    sbSQL.append(" from SYSIBM.SYSDUMMY1");
    sbSQL.append(")");
    sbSQL.append("select");
    sbSQL.append(" D1_1");
    sbSQL.append(" from (");
    sbSQL.append("select ");
    sbSQL.append(" 1 as RNO");
    sbSQL.append(", T1.HBSTDT as HBSTDT");
    sbSQL.append(", T1.BMNCD as BMNCD");
    sbSQL.append(", T1.WRITUKBN as WARIBIKI");
    sbSQL.append(", T1.SEICUTKBN as SEIKI");
    sbSQL.append(", T1.DUMMYCD as DUMMY");
    sbSQL.append(", 'D1'");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T1.HBSTDT), ''), 8, '0')");
    sbSQL.append(" || LPAD(T1.BMNCD, 3, '0')");
    sbSQL.append(" || LPAD(NVL(T1.MEISHOKN,''), 40, '　')");
    sbSQL.append(" || NVL(T1.WRITUKBN, ' ')");
    sbSQL.append(" || NVL(T1.SEICUTKBN, ' ') ");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T1.DUMMYCD), ''), 14, ' ')");
    sbSQL.append(" || RPAD(NVL(TO_CHAR(T2.SHNKN), ''), 40, '　')");
    sbSQL.append(" as D1_1");
    sbSQL.append(" from INATK.TOKRS_KKK T1 ");
    sbSQL.append(" inner join INAMS.MSTSHN T2 on NVL(T1.UPDKBN, 0) <> 1 and NVL(T2.UPDKBN, 0) <> 1 and T2.SHNCD = T1.DUMMYCD");
    sbSQL.append(" inner join WK on T1.HBSTDT = WK.HBSTDT and T1.BMNCD = WK.BMNCD and T1.WRITUKBN = WK.WARIBIKI and T1.SEICUTKBN = WK.SEIKI and T1.DUMMYCD = WK.DUMMY");
    sbSQL.append(" union all");
    sbSQL.append(" select ");
    sbSQL.append(" 2 as RNO");
    sbSQL.append(", T1.HBSTDT as HBSTDT");
    sbSQL.append(", T1.BMNCD as BMNCD");
    sbSQL.append(", T1.WRITUKBN as WARIBIKI");
    sbSQL.append(", T1.SEICUTKBN as SEIKI");
    sbSQL.append(", T1.DUMMYCD as DUMMY");
    sbSQL.append(", 'D2'");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T2.HBSTDT), ''), 8, '0')");
    sbSQL.append(" || LPAD(T2.BMNCD, 3, '0')");
    sbSQL.append(" || NVL(T2.WRITUKBN, ' ')");
    sbSQL.append(" || NVL(T2.SEICUTKBN, ' ')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T2.DUMMYCD), ''), 14, ' ')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T2.SHNCD), ''), 14, ' ')");
    sbSQL.append(" || RPAD(NVL(T3.SANCHIKN, ''), 40, '　')");
    sbSQL.append(" || RPAD(NVL(T2.MAKERKN, ''), 28, case when NVL(T2.MAKERKN, '') = '' then ' ' else '　' end )");
    sbSQL.append(" || RPAD(NVL(T2.SHNKN, ''), 40, '　')");
    sbSQL.append(" || RPAD(NVL(T2.KIKKN, ''), 46, '　')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T2.IRISU), ''), 3, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.RG_BAIKAAM), ''), 6, '0')");
    sbSQL.append(" || LPAD( NVL(TO_CHAR(INTEGER (T3.RG_GENKAAM * 100)), ''), 8, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T2.BAIKAAM), ''), 6, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(INTEGER (T2.GENKAAM * 100)), ''), 8, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.DAICD), ''), 2, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.CHUCD), ''), 2, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.SHOCD), ''), 2, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.ZEIKBN), ''), 1, ' ')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.ZEIRTKBN), ''), 3, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.ZEIRTKBN_OLD), ''), 3, '0')");
    sbSQL.append(" || LPAD(NVL(TO_CHAR(T3.ZEIRTHENKODT), ''), 8, '0') as D1_1");
    sbSQL.append(" from INATK.TOKRS_KKK T1 ");
    sbSQL.append(
        " inner join INATK.TOKRS_SHN T2 on NVL(T1.UPDKBN, 0) <> 1 and NVL(T2.UPDKBN, 0) <> 1 and T1.HBSTDT = T2.HBSTDT and T1.BMNCD = T2.BMNCD and T1.WRITUKBN = T2.WRITUKBN and T1.SEICUTKBN = T2.SEICUTKBN and T1.DUMMYCD = T2.DUMMYCD");
    sbSQL.append(" inner join INAMS.MSTSHN T3 on NVL(T1.UPDKBN, 0) <> 1 and NVL(T2.UPDKBN, 0) <> 1 and NVL(T3.UPDKBN, 0) <> 1 and T2.SHNCD = T3.SHNCD");
    sbSQL.append(" inner join WK on T1.HBSTDT = WK.HBSTDT and T1.BMNCD = WK.BMNCD and T1.WRITUKBN = WK.WARIBIKI and T1.SEICUTKBN = WK.SEIKI and T1.DUMMYCD = WK.DUMMY");
    sbSQL.append(") order by RNO, HBSTDT, BMNCD, WARIBIKI, SEIKI, DUMMY");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }
}
