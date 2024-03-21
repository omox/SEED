package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
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
public class ReportJU017Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU017Dao(String JNDIname) {
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

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatasHead = new JSONArray();
    JSONArray dbDatasKei = new JSONArray();
    JSONArray dbDatasDt = new JSONArray();

    // SQL構文
    String sqlcommand = "";

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szBmnCd = getMap().get("BMNCD"); // 部門コード
    String szRankNo = getMap().get("RANKNO"); // 対象店ランクNO
    String moyskbn = ""; // 催し区分
    String moysstdt = ""; // 催し開始日
    String moysrban = ""; // 催し連番

    if (!StringUtils.isEmpty(getMap().get("MOYSCD")) && getMap().get("MOYSCD").length() >= 8) {
      moyskbn = getMap().get("MOYSCD").substring(0, 1);
      moysstdt = getMap().get("MOYSCD").substring(1, 7);
      moysrban = getMap().get("MOYSCD").substring(7);
    }

    String[] taisyoTen = new String[] {};
    String[] rankArr = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    HashMap<String, Integer> tenRank = new HashMap<String, Integer>();
    HashMap<String, Integer> suryo = new HashMap<String, Integer>();

    // ①店舗数の作成(一番上の行)

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    sqlFrom = "INATK.TOKRANK ";

    // 部門コードを取得
    if (StringUtils.isEmpty(szBmnCd)) {
      sqlWhere += "BMNCD=null AND ";
    } else {
      sqlWhere += "BMNCD=? AND ";
      paramData.add(szBmnCd);
    }

    if (Integer.valueOf(szRankNo) >= 900) {

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      sqlFrom = "INATK.TOKRANKEX ";
    }

    // ランクNo.
    sqlWhere += "RANKNO=? AND ";
    paramData.add(szRankNo);

    // 一覧表情報
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("TENRANK_ARR ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatasHead = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    int soukei = 0;

    if (dbDatasHead.size() != 0) {
      // 対象店の取得
      taisyoTen = dbDatasHead.getJSONObject(0).getString("TENRANK_ARR").split("");
      for (int i = 0; i < taisyoTen.length; i++) {

        String rank = StringUtils.trim(taisyoTen[i]);

        if (!StringUtils.isEmpty(rank)) {

          // 店ランク取得用
          if (!tenRank.containsKey(rank)) {
            tenRank.put(rank, 1);
          } else {
            tenRank.replace(rank, tenRank.get(rank) + 1);
          }
          soukei++;
        }
      }
    }

    sqlcommand = "SELECT * FROM (values ROW('','店舗数　','" + soukei + "'";
    String col = "AS T1(F1,F2,F3";

    for (int i = 0; i < rankArr.length; i++) {
      if (tenRank.containsKey(rankArr[i])) {
        sqlcommand += ",'" + tenRank.get(rankArr[i]) + "'";
      } else {
        sqlcommand += ",'0'";
      }
      col += ",F" + (i + 4);
    }
    sqlcommand += "), ";

    // ②数量パターンNO単位の集計結果を取得

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    sqlFrom = "INATK.TOKSRPTN T1, INATK.TOKSRYRANK T2 ";

    // 部門コードを取得
    if (StringUtils.isEmpty(szBmnCd)) {
      sqlWhere += "T1.BMNCD=null AND ";
    } else {
      sqlWhere += "T1.BMNCD=? AND ";
      paramData.add(szBmnCd);
    }

    // 一覧表情報
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("SRYPTNNO ");
    sbSQL.append(",SRYPTNKN ");
    sbSQL.append(",SUM(SURYO) AS SURYO ");
    sbSQL.append("FROM( ");
    sbSQL.append("SELECT ");
    sbSQL.append("T1.SRYPTNNO ");
    sbSQL.append(",T1.SRYPTNKN ");
    sbSQL.append(",T2.SURYO ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("T1.BMNCD = T2.BMNCD AND ");
    sbSQL.append("T1.SRYPTNNO = T2.SRYPTNNO AND ");
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append("UNION ALL ");

    sqlFrom = "INATK.TOKSRPTNEX T1, INATK.TOKSRYRANKEX T2 ";

    // 部門コードを取得
    if (StringUtils.isEmpty(szBmnCd)) {
      sqlWhere = "T1.BMNCD=null AND ";
    } else {
      sqlWhere = "T1.BMNCD=? AND ";
      paramData.add(szBmnCd);
    }

    if (Integer.valueOf(szRankNo) >= 900) {

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "T1.MOYSKBN=null AND ";
      } else {
        sqlWhere += "T1.MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "T1.MOYSSTDT=null AND ";
      } else {
        sqlWhere += "T1.MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "T1.MOYSRBAN=null AND ";
      } else {
        sqlWhere += "T1.MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }
    }

    sbSQL.append("SELECT ");
    sbSQL.append("T1.SRYPTNNO ");
    sbSQL.append(",T1.SRYPTNKN ");
    sbSQL.append(",T2.SURYO ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("T1.BMNCD = T2.BMNCD AND ");
    sbSQL.append("T1.MOYSKBN = T2.MOYSKBN AND ");
    sbSQL.append("T1.MOYSSTDT = T2.MOYSSTDT AND ");
    sbSQL.append("T1.MOYSRBAN = T2.MOYSRBAN AND ");
    sbSQL.append("T1.SRYPTNNO = T2.SRYPTNNO AND ");
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append(") AS MT GROUP BY ");
    sbSQL.append("SRYPTNNO ");
    sbSQL.append(",SRYPTNKN ");
    sbSQL.append(" ORDER BY ");
    sbSQL.append("SRYPTNNO ");

    dbDatasKei = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // ③数量の詳細を取得

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    sqlFrom = "INATK.TOKSRPTN T1, INATK.TOKSRYRANK T2 ";

    // 部門コードを取得
    if (StringUtils.isEmpty(szBmnCd)) {
      sqlWhere += "T1.BMNCD=null AND ";
    } else {
      sqlWhere += "T1.BMNCD=? AND ";
      paramData.add(szBmnCd);
    }

    // 一覧表情報
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("TENRANK||'-'||SRYPTNNO AS 'KEY' ");
    sbSQL.append(",SURYO ");
    sbSQL.append("FROM( ");
    sbSQL.append("SELECT ");
    sbSQL.append("T1.SRYPTNNO ");
    sbSQL.append(",T1.SRYPTNKN ");
    sbSQL.append(",T2.TENRANK ");
    sbSQL.append(",T2.SURYO ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("T1.BMNCD = T2.BMNCD AND ");
    sbSQL.append("T1.SRYPTNNO = T2.SRYPTNNO AND ");
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append("UNION ALL ");

    sqlFrom = "INATK.TOKSRPTNEX T1, INATK.TOKSRYRANKEX T2 ";

    // 部門コードを取得
    if (StringUtils.isEmpty(szBmnCd)) {
      sqlWhere = "T1.BMNCD=null AND ";
    } else {
      sqlWhere = "T1.BMNCD=? AND ";
      paramData.add(szBmnCd);
    }

    if (Integer.valueOf(szRankNo) >= 900) {

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "T1.MOYSKBN=null AND ";
      } else {
        sqlWhere += "T1.MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "T1.MOYSSTDT=null AND ";
      } else {
        sqlWhere += "T1.MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "T1.MOYSRBAN=null AND ";
      } else {
        sqlWhere += "T1.MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }
    }

    sbSQL.append("SELECT ");
    sbSQL.append("T1.SRYPTNNO ");
    sbSQL.append(",T1.SRYPTNKN ");
    sbSQL.append(",T2.TENRANK ");
    sbSQL.append(",T2.SURYO ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("T1.BMNCD = T2.BMNCD AND ");
    sbSQL.append("T1.MOYSKBN = T2.MOYSKBN AND ");
    sbSQL.append("T1.MOYSSTDT = T2.MOYSSTDT AND ");
    sbSQL.append("T1.MOYSRBAN = T2.MOYSRBAN AND ");
    sbSQL.append("T1.SRYPTNNO = T2.SRYPTNNO AND ");
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append("ORDER BY ");
    sbSQL.append("SRYPTNNO) AS MT ");

    dbDatasDt = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatasDt.size() != 0) {
      for (int i = 0; i < dbDatasDt.size(); i++) {

        JSONObject Dt = dbDatasDt.getJSONObject(i);

        if (!suryo.containsKey(Dt.getString("KEY"))) {
          suryo.put(Dt.getString("KEY"), Dt.getInt("SURYO"));
        }
      }
    }

    if (dbDatasKei.size() != 0) {

      sqlcommand += " ROW(";

      for (int i = 0; i < dbDatasKei.size(); i++) {

        JSONObject kei = dbDatasKei.getJSONObject(i);

        String no = kei.getString("SRYPTNNO");
        sqlcommand += "'" + no + "','" + kei.getString("SRYPTNKN") + "','" + kei.getString("SURYO") + "'";

        for (int j = 0; j < rankArr.length; j++) {
          if (suryo.containsKey(rankArr[j] + "-" + no)) {
            sqlcommand += ",'" + suryo.get(rankArr[j] + "-" + no) + "'";
          } else {
            sqlcommand += ",'0'";
          }
        }

        if (dbDatasKei.size() != (i + 1)) {
          sqlcommand += "), ROW(";
        }
      }
      sqlcommand += "))" + col + ")";
    } else {
      sqlcommand = "";
    }

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sqlcommand);

    return sqlcommand;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<String>();

    // タイトル名称
    cells.add("催し別送信情報");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

  }
}
