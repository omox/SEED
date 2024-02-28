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
public class Reportx091Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx091Dao(String JNDIname) {
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
    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    String szTxtCallcd = getMap().get("TXT_CALLCD"); // 呼出コード
    String szTxtShncd = getMap().get("TXT_SHNCD"); // 商品コード
    String szSelBumon = getMap().get("BUMON[]"); // 部門コード

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    // 一覧表情報

    sbSQL.append("SELECT ");
    sbSQL.append(" RIGHT('00' || RTRIM(T1.BMNCD), 2)");
    sbSQL.append(",RIGHT ('000000' || RTRIM(T1.CALLCD), 6)");
    sbSQL.append(",TRIM(LEFT(RIGHT ('0000000' || RTRIM(T1.SHNCD), 8), 4) || '-' || SUBSTR(RIGHT('0000000' || RTRIM(T1.SHNCD), 8), 5)) as SHNCD");
    sbSQL.append(",T2.SHNKN");
    sbSQL.append(",T1.SHNKNUP");
    sbSQL.append(",T1.SHNKNDN");
    sbSQL.append(",RIGHT('00' || T2.DAICD, 2)");
    sbSQL.append(",RIGHT('00' || T2.CHUCD, 2)");
    sbSQL.append(",RIGHT('00' || T2.SHOCD, 2)");
    sbSQL.append(",T2.RG_IRISU");
    sbSQL.append(",T3.NMKN");
    sbSQL.append(",T4.NMKN");
    sbSQL.append(",T2.URICD");
    sbSQL.append(",T2.KIKKN");
    sbSQL.append(",T1.NAIKN");
    sbSQL.append(",T2.ODS_NATSUSU");
    sbSQL.append(",T1.UTRAY");
    sbSQL.append(",T1.KONPOU");
    sbSQL.append(",T1.FUTAI");
    sbSQL.append(",T1.JURYOUP");
    sbSQL.append(",T1.JURYODN");
    sbSQL.append(",RIGHT('0000000' || RTRIM(T1.SHNCD), 8)");
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTNETSUKE T1 ");
    sbSQL.append("LEFT JOIN INAMS.MSTSHN T2 ");
    sbSQL.append("ON T1.SHNCD = T2.SHNCD ");
    sbSQL.append("AND IFNULL(T2.UPDKBN, 0) <> 1 ");
    sbSQL.append("LEFT JOIN INAMS.MSTMEISHO T3 ");
    sbSQL.append("ON T3.MEISHOKBN = '430' ");
    sbSQL.append("AND T3.MEISHOCD = T1.KAKOKBN ");
    sbSQL.append("LEFT JOIN INAMS.MSTMEISHO T4 ");
    sbSQL.append("ON T4.MEISHOKBN = '121' ");
    sbSQL.append("AND T4.MEISHOCD = T1.TEIKANKBN ");
    sbSQL.append("WHERE IFNULL(T1.UPDKBN, 0) <> 1 ");
    if (!StringUtils.isEmpty(szTxtCallcd)) {
      sbSQL.append("  AND T1.CALLCD >= ? ");
      paramData.add(szTxtCallcd);
    }
    if (!StringUtils.isEmpty(szTxtShncd)) {
      sbSQL.append("  AND T1.SHNCD >= ? ");
      paramData.add(szTxtShncd);
    }
    if (!"-1".equals(szSelBumon)) {
      sbSQL.append("  AND T1.BMNCD = ? ");
      paramData.add(szSelBumon);
    }
    sbSQL.append(" ORDER BY T1.BMNCD, T1.CALLCD, RIGHT('0000000' || RTRIM(T1.SHNCD), 8) ");
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

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
