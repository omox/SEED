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
public class ReportBW003Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBW003Dao(String JNDIname) {
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

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append("  CTHD.OPERATOR");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(CTHD.INPUT_DATE, '%Y%m%d%H%i%s%f'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(CTHD.INPUT_DATE, '%Y%m%d%H%i%s%f')))");
    sbSQL.append(" ,DATE_FORMAT(CTHD.INPUT_DATE, '%H:%i')");
    sbSQL.append(" ,CTHD.COMMENTKN");
    sbSQL.append(" ,CTHD.SEQ");
    sbSQL.append(" from");
    sbSQL.append(" INATK.CSVTOKHEAD CTHD");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.CSVTOK_RSKKK CTRK");
    sbSQL.append(" on CTRK.SEQ = CTHD.SEQ");
    sbSQL.append(" where CTRK.UPDKBN = 0 ");
    sbSQL.append(" order by CTHD.SEQ ");


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
