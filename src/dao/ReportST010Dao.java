package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
public class ReportST010Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportST010Dao(String JNDIname) {
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

  /**
   * 他画面からの呼び出し検索実行
   *
   * @return
   */
  public String createCommandSub(HashMap<String, String> map, User userInfo) {

    // ユーザー情報を設定
    super.setUserInfo(userInfo);

    // 検索条件などの情報を設定
    super.setMap(map);

    // 検索コマンド生成
    String command = createCommand();

    // 出力用検索条件生成
    outputQueryList();

    // 検索実行
    return command;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szBmncd = getMap().get("BMNCD"); // 部門
    String szChkRinji = getMap().get("RINJI"); // チェック_臨時
    String szMoyscd = getMap().get("MOYSCD"); // 催しコード

    // パラメータ確認
    if (StringUtils.isEmpty(szBmncd)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    if (StringUtils.equals("0", szChkRinji) && !StringUtils.isEmpty(szMoyscd)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    if (StringUtils.equals("1", szChkRinji) && StringUtils.isEmpty(szMoyscd)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    // 催しコードテーブル検索
    if (StringUtils.equals("1", szChkRinji)) {
      boolean chkMoyscd = this.check(szMoyscd);
      if (!chkMoyscd) {
        System.out.println(super.getConditionLog());
        return "";
      }
    }
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    // // DB検索用パラメータ
    // ArrayList<String> paramData = new ArrayList<String>();

    // 【画面】臨時 = チェックありの場合
    if (StringUtils.equals("1", szChkRinji)) {

      sbSQL.append(" select");
      sbSQL.append(" right('000'||RANKNO,3) as F1"); // F1 ：ランクNo
      sbSQL.append(", RANKKN as F2"); // F2 ：ランク名称
      sbSQL.append(", length(REGEXP_REPLACE(TENRANK_ARR, '[^A-Z]', '')) as F3"); // F3 ：店舗数
      sbSQL.append(" from INATK.TOKRANKEX");
      sbSQL.append(" where BMNCD =" + szBmncd);
      sbSQL.append(" and MOYSKBN =" + StringUtils.substring(szMoyscd, 0, 1));
      sbSQL.append(" and MOYSSTDT = " + StringUtils.substring(szMoyscd, 1, 7));
      sbSQL.append(" and MOYSRBAN = " + StringUtils.substring(szMoyscd, 7, 10));
      sbSQL.append(" and COALESCE(UPDKBN, 0) <> 1");
      sbSQL.append(" order by RANKNO");

      // 【画面】臨時 = チェックなしの場合
    } else {

      sbSQL.append(" select");
      sbSQL.append(" right('000'||RANKNO,3) as F1"); // F1 ：ランクNo
      sbSQL.append(", RANKKN as F2"); // F2 ：ランク名称
      sbSQL.append(", length(REGEXP_REPLACE(TENRANK_ARR, '[^A-Z]', '')) as F3"); // F3 ：店舗数
      sbSQL.append(" from INATK.TOKRANK");
      sbSQL.append(" where BMNCD =" + szBmncd);
      sbSQL.append(" and COALESCE(UPDKBN, 0) <> 1");
      sbSQL.append(" order by RANKNO");
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // // DB検索用パラメータ設定
    // setParamData(paramData);

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

    // 共通箇所設定
    createCmnOutput(jad);

  }

  /**
   * チェック処理
   *
   * @param map
   * @return
   */
  @SuppressWarnings("static-access")
  public boolean check(String szMoyscd) {

    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";

    // 入力値を取得
    ItemList iL = new ItemList();

    paramData.add(StringUtils.substring(szMoyscd, 0, 1));
    paramData.add(StringUtils.substring(szMoyscd, 1, 7));
    paramData.add(StringUtils.substring(szMoyscd, 7, 10));
    sqlcommand = "select count(MOYSKBN) as VALUE from INATK.TOKMOYCD where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?";

    JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) < 1) {
      return false;
    }

    return true;
  }

}
