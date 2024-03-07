package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportST011Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportST011Dao(String JNDIname) {
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
    String szJissekibun = getMap().get("JISSEKIBUN"); // 実績分類
    String szDaibun = getMap().get("DAIBUN"); // 大分類選択
    String szWwmmflg = getMap().get("WWMMFLG"); // 週月フラグ
    String szYyww = getMap().get("YYWW"); // 年月(週No.)
    String szYymm = getMap().get("YYMM"); // 年月
    ArrayList<String> paramData = new ArrayList<String>();
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    String sqlWhere = "";

    if (StringUtils.equals("1", szWwmmflg)) {
      // 【画面】週データが選択された場合
      sqlWhere += " and T1.WWMMFLG=1";
      sqlWhere += " and T1.YYMM = ? ";

    } else if (StringUtils.equals("2", szWwmmflg)) {
      // 【画面】月データが選択された場合
      sqlWhere += " and T1.WWMMFLG=2";
      sqlWhere += " and T1.YYMM= ? ";

    }


    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    if (StringUtils.equals("1", szJissekibun)) {
      // 【画面】部門実績が選択された場合
      sbSQL.append("select");
      sbSQL.append(" right('00'|| DAICD,2) as F1"); // F1 : 大分類
      sbSQL.append(", right('00'|| CHUCD,2) as F2"); // F2 : 中分類
      sbSQL.append(", BMNKN as F3"); // F3 : 分類名称
      sbSQL.append(", TENTEN_ARR as F4"); // F4 : 点数配列
      sbSQL.append(" from");
      sbSQL.append(" (select");
      sbSQL.append(" T1.DAICD");
      sbSQL.append(", T1.CHUCD");
      sbSQL.append(", T1.TENTEN_ARR");
      sbSQL.append(" from INATK.TOKJRTPTN T1");
      sbSQL.append(" where T1.BMNCD= ? ");
      paramData.add(szBmncd);
      sbSQL.append(" and T1.DAICD=0");
      sbSQL.append(" and T1.CHUCD=0");
      sbSQL.append(sqlWhere);
      sbSQL.append(" ) AS T1");
      sbSQL.append(", (select");
      sbSQL.append(" T2.BMNKN");
      sbSQL.append(" from INAMS.MSTBMN T2");
      sbSQL.append(" where T2.BMNCD= ? ");
      sbSQL.append(" ) AS T2 order by DAICD, CHUCD");

    } else if (StringUtils.equals("2", szJissekibun)) {
      // 【画面】大分類実績が選択された場合
      sbSQL.append("select");
      sbSQL.append(" right('00'|| DAICD,2) as F1"); // F1 : 大分類
      sbSQL.append(", right('00'|| CHUCD,2) as F2"); // F2 : 中分類
      sbSQL.append(", DAIBRUIKN as F3"); // F3 : 分類名称
      sbSQL.append(", TENTEN_ARR as F4"); // F4 : 点数配列
      sbSQL.append(" from");
      sbSQL.append(" (select");
      sbSQL.append(" T1.DAICD");
      sbSQL.append(", T1.CHUCD");
      sbSQL.append(", (select T2.DAIBRUIKN from INAMS.MSTDAIBRUI T2 where T2.BMNCD= ? and T2.DAICD=T1.DAICD) AS DAIBRUIKN");
      paramData.add(szBmncd);
      sbSQL.append(", T1.TENTEN_ARR");
      sbSQL.append(" from INATK.TOKJRTPTN T1");
      sbSQL.append(" where T1.BMNCD= ? ");
      paramData.add(szBmncd);
      sbSQL.append(" and T1.DAICD<>0");
      sbSQL.append(" and T1.CHUCD=0");
      sbSQL.append(sqlWhere);
      sbSQL.append(" ) AS L2 order by DAICD, CHUCD");

    } else if (StringUtils.equals("3", szJissekibun)) {
      // 【画面】中分類実績が選択された場合
      sbSQL.append("select");
      sbSQL.append(" right('00'|| DAICD,2) as F1"); // F1 : 大分類
      sbSQL.append(", right('00'|| CHUCD,2) as F2"); // F2 : 中分類
      sbSQL.append(", CHUBRUIKN as F3"); // F3 : 分類名称
      sbSQL.append(", TENTEN_ARR as F4"); // F4 : 点数配列
      sbSQL.append(" from");
      sbSQL.append(" (select");
      sbSQL.append(" T1.DAICD");
      sbSQL.append(", T1.CHUCD");
      sbSQL.append(", (select T2.CHUBRUIKN from INAMS.MSTCHUBRUI T2 where T2.BMNCD= ? and T2.DAICD= ? and T2.CHUCD=T1.CHUCD) AS CHUBRUIKN ");
      paramData.add(szBmncd);
      paramData.add(szDaibun);
      sbSQL.append(", T1.TENTEN_ARR");
      sbSQL.append(" from INATK.TOKJRTPTN T1");
      sbSQL.append(" where T1.BMNCD= ? ");
      sbSQL.append(" and T1.DAICD= ? ");
      paramData.add(szBmncd);
      paramData.add(szDaibun);
      sbSQL.append(" and T1.CHUCD<>0");
      sbSQL.append(sqlWhere);
      sbSQL.append(" ) AS L2 order by DAICD, CHUCD");

    }
    if (StringUtils.equals("1", szWwmmflg)) {
      // 【画面】週データが選択された場合
      paramData.add(szYyww);

    } else if (StringUtils.equals("2", szWwmmflg)) {
      // 【画面】月データが選択された場合
      paramData.add(szYymm);

    }

    if (StringUtils.equals("1", szJissekibun)) {
      paramData.add(szBmncd);
    }

    setParamData(paramData);

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
