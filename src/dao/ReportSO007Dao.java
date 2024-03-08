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
public class ReportSO007Dao extends ItemDao {

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
  public ReportSO007Dao(String JNDIname) {
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

    String szSEQ = getMap().get("SEQ"); // 部門

    // パラメータ確認
    // 必須チェック
    if (szSEQ == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" select");
    sbSQL.append(" SO.INPUTNO"); // F1 : 行番号
    sbSQL.append(", trim(left(SHNCD, 4) || '-' || SUBSTR(SHNCD, 5))"); // F2 : 商品コード
    sbSQL.append(", SO.SHNKN"); // F3 : 商品名
    sbSQL.append(", SO.ERRFLD"); // F4 : エラー個所
    // sbSQL.append(", right( '000' || SO.ERRCD, 3)"); // F5 : エラー理由
    sbSQL.append(", SO.ERRTBLNM"); // F5 : エラー理由
    sbSQL.append(", SO.ERRVL"); // F6 : エラー値
    sbSQL.append(" from INATK.CSVTOK_SO SO");
    sbSQL.append(" where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" and SEQ = " + szSEQ);

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

    String szSeq = map.get("SEQ"); // SEQ

    ArrayList<String> paramData = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("select");
    sbSQL.append(" MAX(CHAD.OPERATOR) as F1");
    sbSQL.append(", DATE_FORMAT(MAX(CHAD.INPUT_DATE), '%y%m%d')  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(MAX(CHAD.INPUT_DATE))) as F2");
    sbSQL.append(", DATE_FORMAT(MAX(CHAD.INPUT_DATE), '%H:%i') as F3");
    sbSQL.append(", COUNT(CSO.INPUTNO) as F4");
    sbSQL.append(", MAX(CSO.MOYSKBN) || '-' || MAX(CSO.MOYSSTDT) || '-' || MAX(CSO.MOYSRBAN) as F5");
    sbSQL.append(", MAX(MYCD.MOYKN) as F6");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT(MAX(MYCD.HBSTDT), '%Y%m%d'), '%y%m%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(MAX(MYCD.HBSTDT), '%Y%m%d')))");
    sbSQL.append(" || '～' || DATE_FORMAT(DATE_FORMAT(MAX(MYCD.HBEDDT), '%Y%m%d'), '%y%m%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(MAX(MYCD.HBEDDT), '%Y%m%d'))) as F7");
    sbSQL.append(", right('00' || MAX(CSO.BMNCD), 2) as F8");
    sbSQL.append(", MAX(CHAD.COMMENTKN) as F9");
    sbSQL.append(" from INATK.TOKMOYCD MYCD");
    sbSQL.append(" left join INATK.CSVTOK_SO CSO on CSO.MOYSKBN = MYCD.MOYSKBN and CSO.MOYSSTDT = MYCD.MOYSSTDT and CSO.MOYSRBAN = MYCD.MOYSRBAN");
    sbSQL.append(" left join INATK.CSVTOKHEAD CHAD on CHAD.SEQ = CSO.SEQ");
    sbSQL.append(" where MYCD.UPDKBN = 0");
    sbSQL.append(" and CSO.UPDKBN in (0, 9)");
    sbSQL.append(" and CSO.SEQ = " + szSeq);
    sbSQL.append(" group by CHAD.SEQ");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
