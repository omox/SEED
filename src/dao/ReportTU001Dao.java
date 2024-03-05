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
public class ReportTU001Dao extends ItemDao {



  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTU001Dao(String JNDIname) {
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

    String szshoridt = getMap().get("SHORIDT"); // 催し連番
    String ten = getMap().get("TEN"); // ログイン店舗

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append(" T1.MOYKN ");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.NNSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNSTDT, '%Y-%m-%d')))");
    sbSQL.append("  ||'～'||");
    sbSQL.append("  DATE_FORMAT(DATE_FORMAT(T1.NNEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNEDDT, '%Y-%m-%d'))) as KIKAN");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT(T2.QASMDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QASMDT, '%Y-%m-%d')))");
    sbSQL.append(", T1.MOYSKBN");
    sbSQL.append(", T1.MOYSSTDT");
    sbSQL.append(", T1.MOYSRBAN");
    sbSQL.append("  from INATK.TOKMOYCD T1");
    sbSQL.append("  ,INATK.TOKQJU_MOY T2");
    sbSQL.append("  ,INATK.TOKQJU_SHN T3");
    sbSQL.append("  where T1.MOYSKBN = T2.MOYSKBN");
    sbSQL.append("  and T1.MOYSSTDT = T2.MOYSSTDT");
    sbSQL.append("  and T1.MOYSRBAN = T2.MOYSRBAN");
    sbSQL.append("  and T1.NNEDDT >= ");
    sbSQL.append(szshoridt);
    sbSQL.append("  and T2.MOYSKBN = T3.MOYSKBN");
    sbSQL.append("  and T2.MOYSSTDT = T3.MOYSSTDT");
    sbSQL.append("  and T2.MOYSRBAN = T3.MOYSRBAN");
    sbSQL.append("  and COALESCE(T1.UPDKBN, 0) <> 1");
    sbSQL.append("  and COALESCE(T2.UPDKBN, 0) <> 1");
    sbSQL.append("  and COALESCE(T3.UPDKBN, 0) <> 1");
    sbSQL.append("  and TRIM(SUBSTRING(T3.TENHTSU_ARR, 1 + (" + ten + " - 1) * 5, 5)) <> ''");
    sbSQL.append("  group by");
    sbSQL.append("  T1.MOYKN,");
    sbSQL.append("  T1.NNSTDT,");
    sbSQL.append("  T1.NNEDDT,");
    sbSQL.append("  T2.QASMDT,");
    sbSQL.append("  T1.MOYSKBN,");
    sbSQL.append("  T1.MOYSSTDT,");
    sbSQL.append("  T1.MOYSRBAN");
    sbSQL.append("  order by T2.QASMDT");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
