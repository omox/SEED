package dao;

import java.util.ArrayList;
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
public class ReportTJ015Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTJ015Dao(String JNDIname) {
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

    // 処理日付取得
    String shoridt = this.getSHORIDT();

    if (userInfo == null || StringUtils.isEmpty(shoridt)) {
      return "";
    }
    String tencd = userInfo.getTenpo();
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append("  T1.LSTNO "); // F1 ：販売開始日
    sbSQL.append(" ,T1.TITLE "); // F1 ：販売開始日
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.STDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.STDT, '%Y%m%d')))");
    sbSQL.append("  ||'～'||");
    sbSQL.append("  DATE_FORMAT(DATE_FORMAT(T1.EDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.EDDT, '%Y%m%d'))) as KIKAN");
    sbSQL.append("  from");
    sbSQL.append("  INATK.TOKTJ T1");
    sbSQL.append("  left join");
    sbSQL.append("  INATK.TOKTJ_TEN T2");
    sbSQL.append("  on T2.TENCD = ");
    sbSQL.append(tencd);
    sbSQL.append("  where");
    sbSQL.append("  T1.SHWSTDT <= " + shoridt);
    sbSQL.append("  and T1.SHWEDDT >= " + shoridt);
    sbSQL.append("  and T2.TENCD = ");
    sbSQL.append(tencd);
    sbSQL.append("  group by");
    sbSQL.append("  T1.LSTNO,");
    sbSQL.append("  T1.TITLE,");
    sbSQL.append("  T1.STDT,");
    sbSQL.append("  T1.EDDT");
    sbSQL.append("  order by T1.LSTNO desc");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  // 処理日付取得
  public String getSHORIDT() {

    ItemList iL = new ItemList();
    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> paramData = new ArrayList<String>();

    String value = "";

    sbSQL.append(DefineReport.ID_SQLSHORIDT);
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0) {
      value = array.getJSONObject(0).optString("VALUE");
    }
    return value;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
