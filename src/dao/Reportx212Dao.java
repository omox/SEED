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
public class Reportx212Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx212Dao(String JNDIname) {
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
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("DATE_FORMAT(UPDDT,'%y/%m/%d') AS UPDDT "); // F1 : 処理日
    sbSQL.append(",OPERATOR "); // F2 : 入力者
    sbSQL.append(",COMAN "); // F3 : コメント
    sbSQL.append(",MST_YOYAKUDT "); // F4 : マスタ予約日付
    sbSQL.append(",SAKUBAIKAKB "); // F5 : 作成売価区分
    sbSQL.append(",MAISUHOHOKB "); // F6 : 枚数指定方法
    sbSQL.append(",INPUTNO "); // F7 : 入力NO
    sbSQL.append(",PCARDSZ "); // F8 : プライスカードサイズ
    sbSQL.append(",COPYSU "); // F9 : コピー枚数
    sbSQL.append(",UPDDT AS SORT "); // F10 : 処理日
    sbSQL.append("FROM ");
    sbSQL.append("	INAMS.TRNPCARD ");
    sbSQL.append("WHERE ");
    sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append("ORDER BY INPUTNO");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
