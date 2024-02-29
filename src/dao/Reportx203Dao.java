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
public class Reportx203Dao extends ItemDao {

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
  public Reportx203Dao(String JNDIname) {
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
    String szHsgpcd = getMap().get("HSGPCD"); // 配送グループコード
    ArrayList<String> paramData = new ArrayList<>();
    String sqlWhere = "";

    if (StringUtils.isEmpty(szHsgpcd)) {
      sqlWhere += "null";
    } else {
      sqlWhere += "?";
      paramData.add(szHsgpcd);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // 前画面から引き継いだ配送グループコードで検索
    StringBuffer sb = new StringBuffer();
    sb.append("SELECT ");
    sb.append("HSGPCD "); // F1 : 配送グループ
    sb.append(",HSGPKN "); // F2 : 配送グループ名称
    sb.append(",OPERATOR "); // F3 : オペレーター
    sb.append(",DATE_FORMAT(ADDDT,'%y/%m/%d') AS ADDDT "); // F4 : 登録日
    sb.append(",DATE_FORMAT(UPDDT,'%y/%m/%d') AS UPDDT "); // F5 : 更新日
    sb.append(",DATE_FORMAT(UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F6 : 更新日時
    sb.append(",AREAKBN "); // F7 : エリア区分
    sb.append("FROM ");
    sb.append("INAMS.MSTHSGP ");
    sb.append("WHERE ");
    sb.append("HSGPCD=" + sqlWhere + " AND ");
    sb.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sb.append("ORDER BY HSGPCD");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sb.toString());
    return sb.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
