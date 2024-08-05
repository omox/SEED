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
public class Reportx101Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx101Dao(String JNDIname) {
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

    String szTxtFutaiEdaban = getMap().get("TXT_FUTAIEDABAN"); // 風袋枝番
    String szTxtFutaiShukubun = getMap().get("TXT_FUTAISHUKUBUN"); // 風袋種別

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    String sqlConditions = "where FTAI.FTAISHUKBN = '" + szTxtFutaiShukubun + "' and FTAI.UPDKBN = '0'";

    if (!StringUtils.isEmpty(szTxtFutaiEdaban)) {
      sqlConditions = " and FTAI.FTAIECD = '" + szTxtFutaiEdaban + "'";
    }
    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("  select ");
    sbSQL.append("  FTAI.FTAISHUKBN ,");
    sbSQL.append("  MEISHO.NMKN ,");
    sbSQL.append("  FTAI.FTAIECD ,");
    sbSQL.append("  FTAI.FTAIKN ,");
    sbSQL.append("  FTAI.FTAIAN ,");
    sbSQL.append("  FTAI.JRYO");
    sbSQL.append("  from ");
    sbSQL.append("  INAMS.MSTFTAI as FTAI ");
    sbSQL.append("  inner join INAMS.MSTMEISHO as MEISHO ");
    sbSQL.append("  on FTAI.FTAISHUKBN = MEISHO.MEISHOCD ");
    sbSQL.append("  and MEISHO.MEISHOKBN = '617' ");
    sbSQL.append(sqlConditions);
    sbSQL.append("  order by right ('0000' || RTRIM(CAST(FTAI.FTAIECD as CHAR)), 4)");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
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
    List<String> cells = new ArrayList<String>();

    // タイトル名称
    cells.add(jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    cells.add("");
    cells.add(jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);

    cells = new ArrayList<String>();
    cells.add("");
    cells.add("");
    cells.add(DefineReport.Select.KIKAN.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
    cells.add(DefineReport.Select.TENPO.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.TENPO.getObj()));
    cells.add(DefineReport.Select.BUMON.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.BUMON.getObj()));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);
  }
}
