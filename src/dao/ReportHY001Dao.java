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
public class ReportHY001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportHY001Dao(String JNDIname) {
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

    // String moysstdt = getMap().get("MOYSSTDT"); // 催し開始日
    // String bmncd = getMap().get("BMNCD"); // 部門
    String shoridt = getMap().get("SHORIDT"); // 処理日付

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append(" right ('0000' || T1.KKKCD, 4)  as F1"); // F1 : 企画NO
    sbSQL.append(", T1.KKKKM as F2"); // F2 : 企画名称
    sbSQL.append(", T1.NNSTDT || W1.JWEEK as F3"); // F3 : 納入開始日
    sbSQL.append(", T1.NNEDDT || W2.JWEEK as F4"); // F4 : 納入終了日
    sbSQL.append(" from (select");
    sbSQL.append(" YHK.KKKCD");
    sbSQL.append(", YHK.KKKKM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHK.NNSTDT, 6), '%y/%m/%d'), '%y/%m/%d') as NNSTDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHK.NNSTDT, 6), '%Y%m%d')) as NNSTDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHK.NNEDDT, 6), '%y/%m/%d'), '%y/%m/%d') as NNEDDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHK.NNEDDT, 6), '%Y%m%d')) as NNEDDT_WNUM");
    sbSQL.append(", YHK.NNEDDT as NNEDDT_ from INATK.HATYH_KKK YHK where YHK.UPDKBN = 0) T1");
    sbSQL.append(" left outer join WEEK W1 on T1.NNSTDT_WNUM = W1.CWEEK");
    sbSQL.append(" left outer join WEEK W2 on T1.NNEDDT_WNUM = W2.CWEEK");
    sbSQL.append(" left join (select");
    sbSQL.append(" YHS.KKKCD");
    sbSQL.append(", case");
    sbSQL.append("  when MIN(YHS.UKESTDT) > MIN(YHS.TENISTDT) then MIN(YHS.TENISTDT)");
    sbSQL.append("  else MIN(YHS.UKESTDT)");
    sbSQL.append("  end as SHRSTDT");
    sbSQL.append(" from INATK.HATYH_SHN YHS");
    sbSQL.append(" group by YHS.KKKCD) T2 on T2.KKKCD = T1.KKKCD");
    sbSQL.append(" where T2.SHRSTDT - 1 <= " + shoridt);
    sbSQL.append(" and " + shoridt + " <= T1.NNEDDT_ + 7");
    sbSQL.append(" order by T1.KKKCD");

    /*
     * sbSQL.append("with WEEK as (select"); sbSQL.append(" CWEEK"); sbSQL.append(", JWEEK"); sbSQL.
     * append(" from (values (0, '(日'), (1, '(月)'), (2, '(火)'), (3, '(水)'), (4, '(木)'), (5, '(金)'), (6, '(土)')) as TMP(CWEEK, JWEEK))"
     * ); sbSQL.append(" select"); sbSQL.append(" T1.MOYSCD"); // F1 : 企画NO sbSQL.append(", T1.MOYKN");
     * // F2 : 催し名称 sbSQL.append(", T1.HBSTDT || W1.JWEEK || '～' || T1.HBEDDT || W2.JWEEK"); // F3 :
     * 催し期間 sbSQL.append(", T1.COUNT"); // F4 : 登録件数 sbSQL.append(", T1.MOYSKBN"); // F5 : 非表示：催し区分
     * sbSQL.append(", T1.MOYSSTDT"); // F6 : 非表示：催し開始日 sbSQL.append(", T1.MOYSRBAN"); // F7 : 非表示：催し連番
     * sbSQL.append(" from (select");
     * sbSQL.append(" MYCD.MOYSKBN || '-' || MYCD.MOYSSTDT || '-' || MYCD.MOYSRBAN as MOYSCD");
     * sbSQL.append(", MAX(MYCD.MOYKN) as MOYKN"); sbSQL.append(", MYCD.MOYSKBN");
     * sbSQL.append(", MYCD.MOYSSTDT"); sbSQL.append(", MYCD.MOYSRBAN");
     * sbSQL.append(", TO_CHAR(TO_DATE(MAX(MYCD.HBSTDT), 'YYYYMMDD'), 'YY/MM/DD') as HBSTDT");
     * sbSQL.append(", DAYOFWEEK(TO_DATE(MAX(MYCD.HBSTDT), 'YYYYMMDD')) as HBSTDT_WNUM");
     * sbSQL.append(", TO_CHAR(TO_DATE(MAX(MYCD.HBEDDT), 'YYYYMMDD'), 'YY/MM/DD') as HBEDDT");
     * sbSQL.append(", DAYOFWEEK(TO_DATE(MAX(MYCD.HBEDDT), 'YYYYMMDD')) as HBEDDT_WNUM");
     * sbSQL.append(", SUM(SHN.COUNT) as COUNT"); sbSQL.append(" from INATK.TOKMOYCD MYCD"); sbSQL.
     * append(" left join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN"
     * ); sbSQL.
     * append(" left join (select MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, COUNT(SHNCD) as COUNT from INATK.TOKSO_SHN group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD) SHN on SHN.MOYSKBN = MYCD.MOYSKBN and SHN.MOYSSTDT = MYCD.MOYSSTDT and SHN.MOYSRBAN = MYCD.MOYSRBAN and SHN.BMNCD = BMN.BMNCD"
     * ); sbSQL.append(" where SHN.MOYSSTDT = "+moysstdt);
     *
     * if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)){
     * sbSQL.append(" and BMN.BMNCD = "+bmncd); }
     * sbSQL.append(" group by MYCD.MOYSKBN, MYCD.MOYSSTDT, MYCD.MOYSRBAN) T1");
     * sbSQL.append(" left outer join WEEK W1 on T1.HBSTDT_WNUM = W1.CWEEK");
     * sbSQL.append(" left outer join WEEK W2 on T1.HBEDDT_WNUM = W2.CWEEK");
     * sbSQL.append(" order by T1.MOYSCD");
     */

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
}
