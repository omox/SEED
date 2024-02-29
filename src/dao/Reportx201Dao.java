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
public class Reportx201Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx201Dao(String JNDIname) {
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

    String szHsgpkn = getMap().get("HSGPKN"); // 配送グループ名称（漢字）

    // 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    if (StringUtils.isEmpty(szHsgpkn)) {
      sqlWhere += "";
    } else {
      sqlWhere += "HSGPKN LIKE ? AND ";
      paramData.add("%" + szHsgpkn + "%");
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("HSGPCD "); // F1 : 配送グループ
    sbSQL.append(",HSGPKN "); // F2 : 配送グループ名称
    sbSQL.append(",DATE_FORMAT(ADDDT,'%y/%m/%d') AS ADDDT "); // F3 : 登録日
    sbSQL.append(",DATE_FORMAT(UPDDT,'%y/%m/%d') AS UPDDT "); // F4 : 更新日
    sbSQL.append(",HSGPAN "); // F5 : 配送グループ名称（カナ）
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTHSGP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append("ORDER BY HSGPCD");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

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
