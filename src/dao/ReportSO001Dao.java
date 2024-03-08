package dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSO001Dao extends ItemDao {

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
  public ReportSO001Dao(String JNDIname) {
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
   * 検索実行
   *
   * @return
   */
  @Override
  public boolean selectForDL() {

    // 検索コマンド生成
    String command = createCommandForDl();

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

    String moysstdt = getMap().get("MOYSSTDT"); // 催し開始日
    String bmncd = getMap().get("BMNCD"); // 部門

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();
    ArrayList<String> paramData = new ArrayList<>();
    // 一覧表情報
    StringBuffer sb = new StringBuffer();

    sb.append(DefineReport.ID_SQL_CMN_WEEK);
    sb.append(" select");
    sb.append(" T1.MOYSCD"); // F1 : 企画NO
    sb.append(", T1.MOYKN"); // F2 : 催し名称
    sb.append(", T1.HBSTDT || W1.JWEEK || '～' || T1.HBEDDT || W2.JWEEK"); // F3 : 催し期間
    sb.append(", T1.COUNT"); // F4 : 登録件数
    sb.append(", T1.MOYSKBN"); // F5 : 非表示：催し区分
    sb.append(", T1.MOYSSTDT"); // F6 : 非表示：催し開始日
    sb.append(", T1.MOYSRBAN"); // F7 : 非表示：催し連番
    sb.append(" from (select");
    sb.append(" MYCD.MOYSKBN || '-' || MYCD.MOYSSTDT || '-' || right('000'||MYCD.MOYSRBAN, 3) as MOYSCD");
    sb.append(", MAX(MYCD.MOYKN) as MOYKN");
    sb.append(", MYCD.MOYSKBN");
    sb.append(", MYCD.MOYSSTDT");
    sb.append(", MYCD.MOYSRBAN");
    sb.append(", DATE_FORMAT(DATE_FORMAT(MAX(MYCD.HBSTDT), '%Y%m%d'), '%y/%m/%d') as HBSTDT");
    sb.append(", DAYOFWEEK(DATE_FORMAT(MAX(MYCD.HBSTDT), '%Y%m%d')) as HBSTDT_WNUM");
    sb.append(", DATE_FORMAT(DATE_FORMAT(MAX(MYCD.HBEDDT), '%Y%m%d'), '%y/%m/%d') as HBEDDT");
    sb.append(", DAYOFWEEK(DATE_FORMAT(MAX(MYCD.HBEDDT), '%Y%m%d')) as HBEDDT_WNUM");
    sb.append(", case when SUM(SHN.COUNT) is null then 0 else SUM(SHN.COUNT) end COUNT ");
    sb.append(" from INATK.TOKMOYCD MYCD");
    if (!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)) {
      sb.append(" left join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN and BMN.UPDKBN = MYCD.UPDKBN");
      sb.append(" and BMN.BMNCD = ? ");
      paramData.add(bmncd);
    }
    sb.append(
        " left join (select MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, COUNT(COALESCE(SHNCD,0)) as COUNT from INATK.TOKSO_SHN where COALESCE(UPDKBN, 0) <> 1 group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD) SHN on SHN.MOYSKBN = MYCD.MOYSKBN and SHN.MOYSSTDT = MYCD.MOYSSTDT and SHN.MOYSRBAN = MYCD.MOYSRBAN");
    if (!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)) {
      sb.append("  and SHN.BMNCD = BMN.BMNCD ");
    }
    sb.append(" where MYCD.MOYSKBN = 5 and MYCD.MOYSSTDT >= ? and COALESCE(MYCD.UPDKBN, 0) <> 1 ");
    paramData.add(moysstdt);
    sb.append(" group by MYCD.MOYSKBN, MYCD.MOYSSTDT, MYCD.MOYSRBAN) T1");
    sb.append(" left outer join WEEK W1 on T1.HBSTDT_WNUM = W1.CWEEK");
    sb.append(" left outer join WEEK W2 on T1.HBEDDT_WNUM = W2.CWEEK");
    sb.append(" order by T1.MOYSCD");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sb.toString());
    return sb.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }

  private String createCommandForDl() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<>();

    String moysstdt = getMap().get("MOYSSTDT"); // 催し開始日
    String bmncd = getMap().get("BMNCD"); // 部門

    // 一覧表情報
    StringBuffer sb = new StringBuffer();
    sb.append(DefineReport.ID_SQL_CMN_WEEK);

    sb.append(" select distinct");
    sb.append(" MYCD.MOYSKBN || MYCD.MOYSSTDT || right ('000' || MYCD.MOYSRBAN, 3) as MOYSCD");
    sb.append(", MYCD.MOYKN");
    sb.append(", MYCD.MOYSKBN");
    sb.append(", MYCD.MOYSSTDT");
    sb.append(", MYCD.HBSTDT");
    sb.append(", MYCD.HBSTDT");
    sb.append(", ' '");
    sb.append(", '0D0A'");
    sb.append(" from INATK.TOKMOYCD MYCD");
    sb.append(" inner join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN and BMN.UPDKBN = MYCD.UPDKBN");
    sb.append(" inner join (select");
    sb.append("  MOYSKBN");
    sb.append(" , MOYSSTDT");
    sb.append(" , MOYSRBAN");
    sb.append(" , BMNCD");
    sb.append("  from INATK.TOKSO_SHN where COALESCE(UPDKBN, 0) <> 1");
    sb.append("  group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD");
    sb.append(") SHN on SHN.MOYSKBN = MYCD.MOYSKBN and SHN.MOYSSTDT = MYCD.MOYSSTDT and SHN.MOYSRBAN = MYCD.MOYSRBAN and SHN.BMNCD = BMN.BMNCD");
    sb.append(" where COALESCE(BMN.UPDKBN, 0) <> 1");
    sb.append(" and MYCD.MOYSKBN = 5");
    sb.append(" and BMN.MOYSSTDT = " + moysstdt);

    if (!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)) {
      sb.append(" and BMN.BMNCD = " + bmncd);
    }

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sb.toString());
    return sb.toString();
  }
}
