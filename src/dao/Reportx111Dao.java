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
public class Reportx111Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx111Dao(String JNDIname) {
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

    String szTencd = getMap().get("TENCD"); // 店コード
    String szTenkn = getMap().get("TENKN"); // 店舗名

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    String sqlWhere = "";

    if (!StringUtils.isEmpty(szTencd)) {
      sqlWhere += " and TEN.TENCD >= ?";
      paramData.add(szTencd);
    }
    if (!StringUtils.isEmpty(szTenkn)) {
      sqlWhere += " and TEN.TENKN like ?";
      paramData.add("%" + szTenkn + "%");
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append(" right('000' || cast(TEN.TENCD as char), 3)"); // F1 ：店コード
    sbSQL.append(", TEN.TENKN"); // F2 ：店舗名(漢字)
    sbSQL.append(" from INAMS.MSTTEN TEN");
    sbSQL.append(" where COALESCE(TEN.UPDKBN, 0) = 0");
    if (!StringUtils.isEmpty(sqlWhere)) {
      sbSQL.append(sqlWhere);
    }
    sbSQL.append(" order by TEN.TENCD");

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

  }
}
