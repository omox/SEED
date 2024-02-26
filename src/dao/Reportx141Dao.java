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
public class Reportx141Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx141Dao(String JNDIname) {
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
    ArrayList<String> paramData = new ArrayList<String>();
    String szTxtTencd = getMap().get("TXT_TENCD"); // 店コード
    String szTxtTenkyustdt = getMap().get("TXT_TENKYUSTDT"); // 日付from
    String szTxtTenkyuendt = getMap().get("TXT_TENKYUENDT"); // 日付to

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    String sqlConditions = "";

    sqlConditions = " where TNKY.UPDKBN = '0'";

    if (!StringUtils.isEmpty(szTxtTencd)) {
      sqlConditions += " and TNKY.TENCD = ? ";
      paramData.add(szTxtTencd);
    }
    if (!StringUtils.isEmpty(szTxtTenkyuendt)) {
      sqlConditions += " and TNKY.TENKYUDT <= ? ";
      paramData.add(szTxtTenkyuendt);
    }
    if (!StringUtils.isEmpty(szTxtTenkyustdt)) {
      sqlConditions += " and TNKY.TENKYUDT >= ? ";
      paramData.add(szTxtTenkyustdt);
    }

    // 一覧表情報
    StringBuffer sb = new StringBuffer();
    sb.append("SELECT ");
    sb.append("RIGHT(INSERT(INSERT(TNKY.TENKYUDT,5,0,'/'),8,0,'/'),8) ");// F1 : 店休日
    sb.append(",RIGHT('000' || RTRIM(TNKY.TENCD),3) ");// F2 : 店コード
    sb.append(",TEN.TENKN ");// F3 : 店舗名
    sb.append(",CASE WHEN TNKY.TENKYUFLG = 1 THEN '店休' WHEN TNKY.TENKYUFLG = 2 THEN '改装' END ");// F4 : 店休フラグ
    sb.append(",TNKY.TENKYUDT ");// F5 : 店休日
    sb.append(",TNKY.TENKYUFLG ");// F6 : 店休フラグ
    sb.append("FROM INAMS.MSTTENKYU AS TNKY ");
    sb.append("INNER JOIN INAMS.MSTTEN AS TEN ");
    sb.append("ON TNKY.TENCD = TEN.TENCD ");
    sb.append(sqlConditions);
    sb.append("ORDER BY TNKY.TENKYUDT,RIGHT('000' || RTRIM(TNKY.TENCD),3)");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sb.toString());
    return sb.toString();
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
