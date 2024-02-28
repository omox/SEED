package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class Reportx007Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx007Dao(String JNDIname) {
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

    String szSeq = getMap().get("SEQ"); // SEQ
    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getBaseData(getMap());

    // 一覧表情報
    // TODO:天気・気温情報今適当
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append("  T1.INPUTNO"); // F1 : 2.入力番号
    sbSQL.append(" ,trim(left(T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) as SHNCD"); // F2: 商品コード
    sbSQL.append(" ,T1.SHNAN"); // F3: 商品名（漢字）
    sbSQL.append(" ,T1.ERRTBLNM||T1.ERRFLD"); // F4 : エラー箇所
    sbSQL.append(" ,T1.ERRCD"); // F5 : エラーコード TODO:未確定
    sbSQL.append(" ,T1.ERRVL"); // F6 : エラー値

    sbSQL.append(" ,T1.SEQ"); // F7 : 1.SEQ
    sbSQL.append(" ,T1.ERRTBLNM"); // F8 : エラーテーブル名
    sbSQL.append(" ,T1.CSV_UPDKBN"); // F9 : CSV登録区分

    sbSQL.append(" from INAMS.CSVSHNHEAD T2");
    sbSQL.append(" inner join INAMS.CSVSHN T1 on T1.SEQ = T2.SEQ and T2.SEQ = '" + szSeq + "'");
    sbSQL.append(" where nvl(UPDKBN, 0) <> 1 ");
    sbSQL.append(" order by ");
    sbSQL.append("  T1.SHNCD ");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  /**
   * 基本情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getBaseData(HashMap<String, String> map) {

    String szSeq = getMap().get("SEQ"); // SEQ
    getMap().get("BTN");

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append("  T1.SEQ as F1"); // F1 : 2.入力番号
    sbSQL.append(" , T1.OPERATOR as F2");
    sbSQL.append(" , DATE_FORMAT(T1.INPUT_DATE, '%Y%m%d') as F3");
    sbSQL.append(" , TIME_FORMAT(T1.INPUT_DATE, '%H%i%S') as F4");
    sbSQL.append(" , trim(COMMENTKN) as F5");
    sbSQL.append(" , DATE_FORMAT(T1.INPUT_DATE, '%Y%m%d%H%i%S') as F6");
    sbSQL.append(" from INAMS.CSVSHNHEAD T1");
    sbSQL.append(" where T1.SEQ = '" + szSeq + "'");
    sbSQL.append(" order by ");
    sbSQL.append("  T1.SEQ ");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<String>();

    // タイトル名称
    cells.add("商品マスタ");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);

    cells = new ArrayList<String>();
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
