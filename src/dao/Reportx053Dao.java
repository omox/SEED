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
public class Reportx053Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx053Dao(String JNDIname) {
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
    String szMakercd = getMap().get("MAKERCD"); // メーカコード
    String sqlWhere = "";

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    if (szMakercd.length() > 0) {
      sqlWhere += " and (MAK1.DMAKERCD = ? or MAK1.MAKERCD = ? )";
      paramData.add(szMakercd);
      paramData.add(szMakercd);
    }

    sbSQL.append("select distinct");
    sbSQL.append(" case when MAK2.MAKERCD is not null then '代表' else '' end");; // F1 ：更新区分
    sbSQL.append(", MAK1.MAKERCD"); // F2 ：メーカーコード
    sbSQL.append(", MAK1.MAKERKN"); // F3 ：メーカー名（漢字）
    sbSQL.append(", MAK1.JANCD"); // F4 ：JANコード
    sbSQL.append(" from (select * from INAMS.MSTMAKER where IFNULL(UPDKBN, 0) = 0) MAK1");
    sbSQL.append(" left join (select * from INAMS.MSTMAKER where IFNULL(UPDKBN, 0) = 0) MAK2 on MAK2.DMAKERCD = MAK1.MAKERCD");
    sbSQL.append(" where IFNULL(MAK1.UPDKBN, 0) = 0");
    if (!StringUtils.isEmpty(sqlWhere)) {
      sbSQL.append(sqlWhere);
      setParamData(paramData);
    }
    sbSQL.append(" order by MAK1.MAKERCD");

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

  }
}
