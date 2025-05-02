package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
public class ReportBM015Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBM015Dao(String JNDIname) {
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

    String szBmnCd = getMap().get("BMNCD"); // 部門コード
    String szMoysKbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysStDt = getMap().get("MOYSSTDT"); // 催し販売開始日
    String szMoysRban = getMap().get("MOYSRBAN"); // 催し連番
    String szRankNoAdd = getMap().get("RANKNOADD"); // 対象店ランクNO
    String szRankNoDel = getMap().get("RANKNODEL"); // 除外店ランクNO

    JSONArray szTjTenAdd = JSONArray.fromObject(getMap().get("TJTENADD")); // 対象店
    JSONArray szTjTenDel = JSONArray.fromObject(getMap().get("TJTENDEL")); // 除外店

    String szTenRank_Arr = getMap().get("TENRANK_ARR");

    // 重複チェック用
    Set<Integer> tencds = new TreeSet<>();
    if (!StringUtils.isEmpty(szTenRank_Arr)) {
      tencds = getTenCdAddArr(szTenRank_Arr, szTjTenAdd, szTjTenDel);
    } else {
      tencds = getTenCdAdd(szBmnCd, szMoysKbn, szMoysStDt, szMoysRban, szRankNoAdd, szRankNoDel, szTjTenAdd, szTjTenDel);
    }

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    sbSQL = new StringBuffer();
    sbSQL.append("SELECT  ");
    sbSQL.append("MAX(M.TENPOSU) OVER () AS TENPOSU ");
    sbSQL.append(",M.TENCD ");
    sbSQL.append(",M.TENKN ");
    sbSQL.append(",M.AREACD ");
    sbSQL.append("FROM( ");
    sbSQL.append("SELECT  ");
    sbSQL.append("ROW_NUMBER() OVER () AS TENPOSU ");
    sbSQL.append(",T.TENCD ");
    sbSQL.append(",T.TENKN ");
    sbSQL.append(",B.AREACD ");
    sbSQL.append("FROM ");
    Iterator<Integer> ten = tencds.iterator();
    sbSQL.append("(values ");
    for (int i = 0; i < tencds.size(); i++) {
      sbSQL.append("row(" + szBmnCd + "," + ten.next() + "),");
    }
    sbSQL.append("row(null,null)) AS BT(BMNCD,TENCD) LEFT JOIN ");
    sbSQL.append(" INAMS.MSTTEN T ON BT.TENCD=T.TENCD LEFT JOIN ");
    sbSQL.append(" INAMS.MSTTENBMN B ON BT.TENCD=B.TENCD AND BT.BMNCD=B.BMNCD ");
    sbSQL.append("WHERE BT.TENCD IS NOT NULL ");
    sbSQL.append("GROUP BY ");
    sbSQL.append("T.TENCD ");
    sbSQL.append(",T.TENKN ");
    sbSQL.append(",B.AREACD ");
    sbSQL.append("ORDER BY ");
    sbSQL.append("T.TENCD ");
    sbSQL.append(",T.TENKN ");
    sbSQL.append(",B.AREACD )M");

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

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<>();

    // タイトル名称
    cells.add("催し別送信情報");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

  }

  // 対象店取得処理
  public Set<Integer> getTenCdAdd(String bmnCd, String moysKbn, String moysStDt, String moysRban, String rankNoAdd, String rankNoDel, JSONArray tenCdAdds, JSONArray tenCdDels) {

    ArrayList<String> paramData = new ArrayList<>();
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKRANK ";

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    JSONObject data = new JSONObject();
    String[] taisyoTen = new String[] {};
    String[] jyogaiTen = new String[] {};
    new TreeSet<Integer>();

    // 部門コード
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "BMNCD=null AND ";
    } else {
      sqlWhere += "BMNCD=? AND ";
      paramData.add(bmnCd);
    }

    // ランクNo.が900未満の場合参照テーブルを変更
    if (!StringUtils.isEmpty(rankNoAdd) && Integer.valueOf(rankNoAdd) >= 900) {
      sqlFrom = "INATK.TOKRANKEX ";

      // 催し区分
      if (StringUtils.isEmpty(moysKbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moysKbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysStDt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysStDt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysRban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysRban);
      }
    }

    // ランクNo.
    if (StringUtils.isEmpty(rankNoAdd)) {
      sqlWhere += "RANKNO=null AND ";
    } else {
      sqlWhere += "RANKNO=? AND ";
      paramData.add(rankNoAdd);
    }

    // 一覧表情報
    sbSQL.append("SELECT ");
    sbSQL.append("TENRANK_ARR ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() > 0) {
      data = dbDatas.getJSONObject(0);
      taisyoTen = data.optString("TENRANK_ARR").split("");
    }

    if (!StringUtils.isEmpty(rankNoDel)) {
      sqlFrom = "INATK.TOKRANK ";
      sqlWhere = "";
      paramData = new ArrayList<>();

      // 部門コード
      sqlWhere += "BMNCD=? AND ";
      paramData.add(bmnCd);

      // ランクNo.が900未満の場合参照テーブルを変更
      if (Integer.valueOf(rankNoDel) >= 900) {
        sqlFrom = "INATK.TOKRANKEX ";

        // 催し区分
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moysKbn);

        // 催し開始日
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysStDt);

        // 催し連番
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysRban);
      }

      // ランクNo.
      sqlWhere += "RANKNO=? AND ";
      paramData.add(rankNoDel);

      new ItemList();
      sbSQL = new StringBuffer();
      dbDatas = new JSONArray();
      data = new JSONObject();
      sbSQL.append("SELECT ");
      sbSQL.append("TENRANK_ARR ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
      if (dbDatas.size() > 0) {
        data = dbDatas.getJSONObject(0);
        jyogaiTen = data.optString("TENRANK_ARR").split("");
      }
    }
    return getTenCds(taisyoTen, jyogaiTen, tenCdAdds, tenCdDels);
  }

  public Set<Integer> getTenCdAddArr(String tenRank_Arr, JSONArray tenCdAdds, JSONArray tenCdDels) {

    String[] taisyoTen = new String[] {};
    String[] jyogaiTen = new String[] {};

    if (tenRank_Arr.split("_").length >= 2) {
      taisyoTen = tenRank_Arr.split("_")[1].split("");
    }

    if (tenRank_Arr.split("_").length == 3) {
      jyogaiTen = tenRank_Arr.split("_")[2].split("");
    }

    return getTenCds(taisyoTen, jyogaiTen, tenCdAdds, tenCdDels);
  }

  public Set<Integer> getTenCds(String[] taisyoTen, String[] jyogaiTen, JSONArray tenCdAdds, JSONArray tenCdDels) {

    Set<Integer> tencds = new TreeSet<>();

    for (int i = 0; i < taisyoTen.length; i++) {
      if (jyogaiTen.length <= i) {
        if (!StringUtils.isEmpty(taisyoTen[i].trim())) {
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }
        }
      } else {
        if (!StringUtils.isEmpty(taisyoTen[i].trim()) && StringUtils.isEmpty(jyogaiTen[i].trim())) {
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }
        }
      }
    }

    // 対象店を追加
    for (int i = 0; i < tenCdAdds.size(); i++) {
      if (!tenCdAdds.getString(i).equals("") && !tencds.contains(tenCdAdds.getInt(i))) {
        tencds.add(tenCdAdds.getInt(i));
      }
    }

    // 除外店を削除
    for (int i = 0; i < tenCdDels.size(); i++) {
      if (!tenCdDels.getString(i).equals("")) {
        tencds.remove(tenCdDels.getInt(i));
      }
    }

    return tencds;
  }

  // 対象店配列作成
  public ArrayList<String> getTenrankArray(String bmnCd, String moysKbn, String moysStDt, String moysRban, String rankNoAdd, String rankNoDel, JSONArray tenCdAdds, JSONArray rankAdds,
      JSONArray tenCdDels, String saveTenrankArr) {
    ArrayList<String> paramData = new ArrayList<>();
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKRANK ";

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    JSONObject data = new JSONObject();
    String[] taisyoTen = new String[] {};
    String[] jyogaiTen = new String[] {};
    ArrayList<String> tenranks = new ArrayList<>();

    // １.ランクNo展開配列作成機能
    // 保存済みのランクNo.展開配列がある場合（※TG016の変更モード）
    if (StringUtils.isNotEmpty(saveTenrankArr)) {

      for (int i = 0; i < saveTenrankArr.split("").length; i++) {
        tenranks.add(saveTenrankArr.split("")[i].trim());
      }

      // 保存済みのランクNo.展開配列がない場合
    } else {
      // 部門コード
      if (StringUtils.isEmpty(bmnCd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmnCd);
      }

      // ランクNo.が900未満の場合参照テーブルを変更
      if (!StringUtils.isEmpty(rankNoAdd) && Integer.valueOf(rankNoAdd) >= 900) {
        sqlFrom = "INATK.TOKRANKEX ";

        // 催し区分
        if (StringUtils.isEmpty(moysKbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moysKbn);
        }

        // 催し開始日
        if (StringUtils.isEmpty(moysStDt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysStDt);
        }

        // 催し連番
        if (StringUtils.isEmpty(moysRban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysRban);
        }
      }

      // ランクNo.
      if (StringUtils.isEmpty(rankNoAdd)) {
        sqlWhere += "RANKNO=null AND ";
      } else {
        sqlWhere += "RANKNO=? AND ";
        paramData.add(rankNoAdd);
      }

      // 一覧表情報
      sbSQL.append("SELECT ");
      sbSQL.append("TENRANK_ARR ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() > 0) {
        data = dbDatas.getJSONObject(0);
        taisyoTen = data.optString("TENRANK_ARR").split("");
      }

      if (!StringUtils.isEmpty(rankNoDel)) {
        sqlFrom = "INATK.TOKRANK ";
        sqlWhere = "";
        paramData = new ArrayList<>();

        // 部門コード
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmnCd);

        // ランクNo.が900未満の場合参照テーブルを変更
        if (Integer.valueOf(rankNoDel) >= 900) {
          sqlFrom = "INATK.TOKRANKEX ";

          // 催し区分
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moysKbn);

          // 催し開始日
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysStDt);

          // 催し連番
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysRban);
        }

        // ランクNo.
        sqlWhere += "RANKNO=? AND ";
        paramData.add(rankNoDel);

        new ItemList();
        sbSQL = new StringBuffer();
        dbDatas = new JSONArray();
        data = new JSONObject();
        sbSQL.append("SELECT ");
        sbSQL.append("TENRANK_ARR ");
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere);
        sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

        dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
        if (dbDatas.size() > 0) {
          data = dbDatas.getJSONObject(0);
          jyogaiTen = data.optString("TENRANK_ARR").split("");
        }
      }

      for (int i = 0; i < taisyoTen.length; i++) {
        String rank = taisyoTen[i].trim();
        if (jyogaiTen.length > i && !StringUtils.isEmpty(jyogaiTen[i].trim())) {
          rank = "";
        }
        tenranks.add(rank);
      }
    }

    // 対象店を追加
    if (tenCdAdds != null) {
      for (int i = 0; i < tenCdAdds.size(); i++) {
        if (!tenCdAdds.optString(i).equals("")) {
          // 追加対象店にランクの指定がない場合9をセット
          int tencd = tenCdAdds.optInt(i);
          String tenrank = rankAdds.optString(i, "9");
          if (tenranks.size() >= tencd - 1) {
            tenranks.set(tencd - 1, tenrank);
          }
        }
      }
    }

    // 除外店を削除
    if (tenCdDels != null) {
      for (int i = 0; i < tenCdDels.size(); i++) {
        if (!tenCdDels.getString(i).equals("")) {
          int tencd = tenCdDels.optInt(i);
          if (tenranks.size() >= tencd - 1) {
            tenranks.set(tencd - 1, "");
          }
        }
      }
    }
    return tenranks;
  }

  // 対象店配列からランクごとの店舗数配列取得
  public TreeMap<String, Integer> getTenrankCount(ArrayList<String> tenranks) {
    TreeMap<String, Integer> map = new TreeMap<>();

    String[] ranks = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    for (String rank : ranks) {
      map.put(rank, 0);
    }
    int sum = 0;
    for (String rank : tenranks) {
      if (map.containsKey(rank)) {
        map.replace(rank, map.get(rank) + 1);
        sum += 1;
      }
    }
    map.put("SUM", sum);
    return map;
  }

  // 対象店取得処理
  public String checkTenCdAdd(String bmnCd, String moysKbn, String moysStDt, String moysRban, String rankNoAdd, String rankNoDel, JSONArray tenCdAdds, JSONArray tenCdDels) {

    ArrayList<String> paramData = new ArrayList<>();
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKRANK ";

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    JSONObject data = new JSONObject();
    String[] taisyoTen = new String[] {};
    String[] jyogaiTen = new String[] {};
    Set<Integer> tencds = new TreeSet<>();
    new JSONArray();

    // 部門コード
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "BMNCD=null AND ";
    } else {
      sqlWhere += "BMNCD=? AND ";
      paramData.add(bmnCd);
    }

    // ランクNo.が900未満の場合参照テーブルを変更
    if (!StringUtils.isEmpty(rankNoAdd) && Integer.valueOf(rankNoAdd) >= 900) {
      sqlFrom = "INATK.TOKRANKEX ";

      // 催し区分
      if (StringUtils.isEmpty(moysKbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moysKbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysStDt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysStDt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysRban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysRban);
      }
    }

    // ランクNo.
    if (StringUtils.isEmpty(rankNoAdd)) {
      sqlWhere += "RANKNO=null AND ";
    } else {
      sqlWhere += "RANKNO=? AND ";
      paramData.add(rankNoAdd);
    }

    // 一覧表情報
    sbSQL.append("SELECT ");
    sbSQL.append("TENRANK_ARR ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() > 0) {
      data = dbDatas.getJSONObject(0);
      taisyoTen = data.optString("TENRANK_ARR").split("");
    } else {
      return "E20014";
    }

    if (!StringUtils.isEmpty(rankNoDel)) {
      sqlFrom = "INATK.TOKRANK ";
      sqlWhere = "";
      paramData = new ArrayList<>();

      // 部門コード
      if (StringUtils.isEmpty(bmnCd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmnCd);
      }

      // ランクNo.が900未満の場合参照テーブルを変更
      if (!StringUtils.isEmpty(rankNoDel) && Integer.valueOf(rankNoDel) >= 900) {
        sqlFrom = "INATK.TOKRANKEX ";

        // 催し区分
        if (StringUtils.isEmpty(moysKbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moysKbn);
        }

        // 催し開始日
        if (StringUtils.isEmpty(moysStDt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysStDt);
        }

        // 催し連番
        if (StringUtils.isEmpty(moysRban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysRban);
        }
      }

      // ランクNo.
      if (StringUtils.isEmpty(rankNoDel)) {
        sqlWhere += "RANKNO=null AND ";
      } else {
        sqlWhere += "RANKNO=? AND ";
        paramData.add(rankNoDel);
      }

      new ItemList();
      sbSQL = new StringBuffer();
      dbDatas = new JSONArray();
      data = new JSONObject();
      sbSQL.append("SELECT ");
      sbSQL.append("TENRANK_ARR ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
      if (dbDatas.size() > 0) {
        data = dbDatas.getJSONObject(0);
        jyogaiTen = data.optString("TENRANK_ARR").split("");
      } else {
        return "E20015";
      }
    }

    for (int i = 0; i < taisyoTen.length; i++) {
      if (jyogaiTen.length <= i) {
        if (!StringUtils.isEmpty(taisyoTen[i].trim())) {
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }
        }
      } else {
        if (!StringUtils.isEmpty(taisyoTen[i].trim()) && StringUtils.isEmpty(jyogaiTen[i].trim())) {
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }
        }
      }
    }

    // 対象店を追加
    boolean errFlg = true;
    for (int i = 0; i < tenCdAdds.size(); i++) {
      if (!tenCdAdds.getString(i).equals("") && !tencds.contains(tenCdAdds.getInt(i))) {
        tencds.add(tenCdAdds.getInt(i));
      } else if (!tenCdAdds.getString(i).equals("") && tencds.contains(tenCdAdds.getInt(i))) {

        for (String element : jyogaiTen) {
          if (element.equals(tenCdAdds.getInt(i))) {
            tencds.add(tenCdAdds.getInt(i));
            errFlg = false;
            break;
          }
        }

        if (errFlg) {
          return "E20025";
        }
      }
    }

    // 除外店を削除
    for (int i = 0; i < tenCdDels.size(); i++) {
      if (!tenCdDels.getString(i).equals("") && !tencds.contains(tenCdDels.getInt(i))) {
        return "E20026";
      } else if (!tenCdDels.getString(i).equals("") && tencds.contains(tenCdDels.getInt(i))) {
        tencds.remove(tenCdDels.getInt(i));
      }
    }

    if (tencds.size() == 0) {
      return "E20027";
    }

    return "";
  }

  // 対象店取得処理
  public String checkTenCdAddArr(String tenAtuk_Arr, JSONArray tenCdAdds, JSONArray tenCdDels) {

    // 格納用変数
    String[] taisyoTen = new String[] {};
    String[] jyogaiTen = new String[] {};
    Set<Integer> tencds = new TreeSet<>();

    if (tenAtuk_Arr.split("_").length >= 2) {
      taisyoTen = tenAtuk_Arr.split("_")[1].split("");
    }

    for (int i = 0; i < taisyoTen.length; i++) {
      if (jyogaiTen.length <= i) {
        if (!StringUtils.isEmpty(taisyoTen[i].trim())) {
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }
        }
      } else {
        if (!StringUtils.isEmpty(taisyoTen[i].trim()) && StringUtils.isEmpty(jyogaiTen[i].trim())) {
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }
        }
      }
    }

    // 対象店を追加
    boolean errFlg = true;
    for (int i = 0; i < tenCdAdds.size(); i++) {
      if (!tenCdAdds.getString(i).equals("") && !tencds.contains(tenCdAdds.getInt(i))) {
        tencds.add(tenCdAdds.getInt(i));
      } else if (!tenCdAdds.getString(i).equals("") && tencds.contains(tenCdAdds.getInt(i))) {

        for (String element : jyogaiTen) {
          if (element.equals(tenCdAdds.getInt(i))) {
            tencds.add(tenCdAdds.getInt(i));
            errFlg = false;
            break;
          }
        }

        if (errFlg) {
          return "E20025";
        }
      }
    }

    // 除外店を削除
    for (int i = 0; i < tenCdDels.size(); i++) {
      if (!tenCdDels.getString(i).equals("") && !tencds.contains(tenCdDels.getInt(i))) {
        return "E20026";
      } else if (!tenCdDels.getString(i).equals("") && tencds.contains(tenCdDels.getInt(i))) {
        tencds.remove(tenCdDels.getInt(i));
      }
    }

    if (tencds.size() == 0) {
      return "E20027";
    }

    return "";
  }

  /**
   * 存在チェック
   *
   * @param bmnCd
   * @param moysKbn
   * @param moysStDt
   * @param moysRban
   * @param sryPtnNo
   * @return err
   */
  public String chkSuryoPtn(String bmnCd, String moysKbn, String moysStDt, String moysRban, String sryPtnNo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKSRPTN "; // 数量パターン
    ArrayList<String> paramData = new ArrayList<>();

    // 初期化
    sqlWhere = "";
    paramData = new ArrayList<>();
    dbDatas = new JSONArray();

    // 商品コードより部門コードを取得
    if (StringUtils.isEmpty(bmnCd)) {
      sqlWhere += "BMNCD=null AND ";
    } else {
      sqlWhere += "BMNCD=? AND ";
      paramData.add(bmnCd);
    }

    if (!StringUtils.isEmpty(sryPtnNo) && Integer.valueOf(sryPtnNo) >= 900) {

      // 催し区分
      if (StringUtils.isEmpty(moysKbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moysKbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysStDt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysStDt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysRban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysRban);
      }
      sqlFrom = "INATK.TOKSRPTNEX ";
    }

    // ランクNo.
    if (StringUtils.isEmpty(sryPtnNo)) {
      sqlWhere += "SRYPTNNO=null AND ";
    } else {
      sqlWhere += "SRYPTNNO=? AND ";
      paramData.add(sryPtnNo);
    }

    // 一覧表情報
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("SRYPTNNO ");
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      return "EX1079";
    }

    return "";
  }

  /**
   * 配列をkye,valueの形で返却(key:店、value:数量)
   *
   * @param tenRankArray
   * @return arrMap
   */
  public ArrayList<String> getSuryoArr(String bmnCd, String moysKbn, String moysStDt, String moysRban, String rankNoAdd, String rankNoDel, JSONArray tenCdAdds, JSONArray rankAdds, JSONArray tenCdDels,
      String saveTenrankArr, String sryPtnNo) {

    // 店ランクを取得
    ArrayList<String> tenranks = getTenrankArray(bmnCd, moysKbn, moysStDt, moysRban, rankNoAdd, rankNoDel, tenCdAdds, rankAdds, tenCdDels, saveTenrankArr);

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKSRYRANK "; // 数量ランク
    ArrayList<String> paramData = new ArrayList<>();

    ArrayList<String> calcList = new ArrayList<>();
    HashMap<String, String> arrMap = new HashMap<>();

    int tenhtsukei = 0;
    int tensu = 0;

    for (int i = 0; i < tenranks.size(); i++) {

      String key = String.valueOf(i + 1);
      String val = tenranks.get(i);

      if (StringUtils.isEmpty(val)) {
        continue;
      }

      // 初期化
      sqlWhere = "";
      sqlFrom = "INATK.TOKSRYRANK "; // 数量ランク
      paramData = new ArrayList<>();
      dbDatas = new JSONArray();

      // 商品コードより部門コードを取得
      if (StringUtils.isEmpty(bmnCd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmnCd);
      }

      if (!StringUtils.isEmpty(sryPtnNo) && Integer.valueOf(sryPtnNo) >= 900) {

        // 催し区分
        if (StringUtils.isEmpty(moysKbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moysKbn);
        }

        // 催し開始日
        if (StringUtils.isEmpty(moysStDt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysStDt);
        }

        // 催し連番
        if (StringUtils.isEmpty(moysRban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysRban);
        }
        sqlFrom = "INATK.TOKSRYRANKEX ";
      }

      // ランクNo.
      if (StringUtils.isEmpty(sryPtnNo)) {
        sqlWhere += "SRYPTNNO=null AND ";
      } else {
        sqlWhere += "SRYPTNNO=? AND ";
        paramData.add(sryPtnNo);
      }

      // ランクNo.
      sqlWhere += "TENRANK=? ";
      paramData.add(val);

      // 一覧表情報
      sbSQL = new StringBuffer();
      sbSQL.append("SELECT ");
      sbSQL.append("SURYO ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      int suryo = 0;
      if (dbDatas.size() != 0) {
        suryo = dbDatas.getJSONObject(0).optInt("SURYO");
      }

      tensu++;
      tenhtsukei += suryo;

      if (arrMap.containsKey(key)) {
        arrMap.replace(key, String.valueOf(suryo));
      } else {
        arrMap.put(key, String.valueOf(suryo));
      }
    }

    String arr = "";
    int tennum = 0;

    for (int j = 1; j <= 400; j++) {

      String key = String.valueOf(j);
      String val = "";

      if (arrMap.containsKey(key)) {
        val = arrMap.get(key);

        for (int jj = tennum + 1; jj < Integer.valueOf(key); jj++) {
          arr += String.format("%5s", "");
        }
        arr += String.format("%05d", Integer.valueOf(val));
        tennum = Integer.valueOf(key);
      }
    }

    arr = new ReportJU012Dao(JNDIname).spaceArr(arr, 5);

    // 発注数配列、店舗数、展開数を返却
    calcList.add(arr);
    calcList.add(String.valueOf(tensu));
    calcList.add(String.valueOf(tenhtsukei));

    return calcList;
  }

  /**
   * 率パターンマスタ存在チェック
   *
   * @param bmncd 部門コード
   * @param ptnno パターン№
   * @param wwmm 週月フラグ
   * @param daicd 大分類
   * @param chucd 中分類
   * @param getFlg 1:通常率パターン 3:実績率パターン
   * @return htsuMap
   */
  public String chkRtPt(String bmncd, String ptnno, String wwmm, String syukbn, String daicd, String chucd, String getFlg) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKRTPTN "; // 通常率パターン
    String sqlSelect = "TENRT_ARR AS ARR ";
    ArrayList<String> paramData = new ArrayList<>();

    // 部門コード
    if (StringUtils.isEmpty(bmncd)) {
      sqlWhere += "BMNCD=null AND ";
    } else {
      sqlWhere += "BMNCD=? AND ";
      paramData.add(bmncd);
    }

    // 通常率パターンの場合
    if (getFlg.equals("1")) {
      // 率パターン№
      if (StringUtils.isEmpty(ptnno)) {
        sqlWhere += "RTPTNNO=null ";
      } else {
        sqlWhere += "RTPTNNO=? ";
        paramData.add(ptnno);
      }
    } else {

      sqlFrom = "INATK.TOKJRTPTN "; // 実績率パターン

      if (syukbn.equals("2")) {
        sqlSelect = "TENTEN_ARR AS ARR ";
      } else {
        sqlSelect = "TENURI_ARR AS ARR ";
      }

      // 週月フラグ
      if (StringUtils.isEmpty(wwmm)) {
        sqlWhere += "WWMMFLG=null AND ";
      } else {
        sqlWhere += "WWMMFLG=? AND ";
        paramData.add(wwmm);
      }

      // 年月
      if (StringUtils.isEmpty(ptnno)) {
        sqlWhere += "YYMM=null AND ";
      } else {
        sqlWhere += "YYMM=? AND ";
        paramData.add(ptnno);
      }

      // 大分類
      if (StringUtils.isEmpty(daicd)) {
        sqlWhere += "DAICD=null AND ";
      } else {
        sqlWhere += "DAICD=? AND ";
        paramData.add(daicd);
      }

      // 中分類
      if (StringUtils.isEmpty(chucd)) {
        sqlWhere += "CHUCD=null ";
      } else {
        sqlWhere += "CHUCD=? ";
        paramData.add(chucd);
      }
    }

    sbSQL.append("SELECT ");
    sbSQL.append(sqlSelect); // 配列
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    if (getFlg.equals("1")) {
      sbSQL.append(" AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    }

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // データが存在する場合、配列の展開を実施
    if (dbDatas.size() == 0) {
      if (getFlg.equals("1")) {
        return "EX1080";
      } else {
        return "EX1081";
      }
    }

    return "";
  }

  /**
   * 配列をkye,valueの形で返却(key:店、value:数量)
   *
   * @param bmncd 部門コード
   * @param ptnno パターン№
   * @param wwmm 週月フラグ
   * @param shuno 週№
   * @param daicd 大分類
   * @param chucd 中分類
   * @param htasu 発注総数
   * @param tencds 対象店
   * @param getFlg 1:通常率パターン 3:実績率パターン
   * @return htsuMap
   */
  public ArrayList<String> getRtPtArr(String bmncd, String ptnno, String wwmm, String syukbn, String shuno, String daicd, String chucd, int htasu, Set<Integer> tencds, String getFlg) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKRTPTN "; // 通常率パターン
    String sqlSelect = "TENRT_ARR AS ARR ";
    ArrayList<String> paramData = new ArrayList<>();

    HashMap<String, String> getRitsuMap = new HashMap<>();
    HashMap<String, String> arrMap = new HashMap<>();
    HashMap<String, Integer> htsuMap = new HashMap<>();
    ArrayList<String> calcList = new ArrayList<>();
    int digit = 5;

    // 部門コード
    if (StringUtils.isEmpty(bmncd)) {
      sqlWhere += "BMNCD=null AND ";
    } else {
      sqlWhere += "BMNCD=? AND ";
      paramData.add(bmncd);
    }

    // 通常率パターンの場合
    if (getFlg.equals("1")) {
      // 率パターン№
      if (StringUtils.isEmpty(ptnno)) {
        sqlWhere += "RTPTNNO=null ";
      } else {
        sqlWhere += "RTPTNNO=? ";
        paramData.add(ptnno);
      }
    } else {

      sqlFrom = "INATK.TOKJRTPTN "; // 実績率パターン

      if (syukbn.equals("2")) {
        sqlSelect = "TENTEN_ARR AS ARR ";
      } else {
        sqlSelect = "TENURI_ARR AS ARR ";
      }

      digit = 9;

      // 週月フラグ
      if (StringUtils.isEmpty(wwmm)) {
        sqlWhere += "WWMMFLG=null AND ";
      } else {
        sqlWhere += "WWMMFLG=? AND ";
        paramData.add(wwmm);
      }

      // 年月
      if (StringUtils.isEmpty(shuno)) {
        sqlWhere += "YYMM=null AND ";
      } else {
        sqlWhere += "YYMM=? AND ";
        paramData.add(shuno);
      }

      // 大分類
      if (StringUtils.isEmpty(daicd)) {
        sqlWhere += "DAICD=null AND ";
      } else {
        sqlWhere += "DAICD=? AND ";
        paramData.add(daicd);
      }

      // 中分類
      if (StringUtils.isEmpty(chucd)) {
        sqlWhere += "CHUCD=null ";
      } else {
        sqlWhere += "CHUCD=? ";
        paramData.add(chucd);
      }
    }

    sbSQL.append("SELECT ");
    sbSQL.append(sqlSelect); // 配列
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // データが存在する場合、配列の展開を実施
    if (dbDatas.size() != 0) {
      getRitsuMap = new ReportJU012Dao(JNDIname).getDigitMap(dbDatas.getJSONObject(0).optString("ARR"), digit, "1");
    } else {
      calcList.add("");
      calcList.add(String.valueOf(tencds.size()));
      calcList.add("0");
      return calcList;
    }

    int ritsukei = 0;
    int tensukei = 0;

    // ①分配率総計を求める(対象店以外をremoveする)
    for (HashMap.Entry<String, String> ritsu : getRitsuMap.entrySet()) {

      String key = ritsu.getKey();
      String val = ritsu.getValue().trim();

      if (tencds.contains(Integer.valueOf(key))) {
        arrMap.put(key, val);
        ritsukei += Integer.valueOf(val);
      }
    }

    Iterator<Integer> ten = tencds.iterator();
    for (int i = 0; i < tencds.size(); i++) {
      String key = String.valueOf(ten.next());
      if (!arrMap.containsKey(key)) {
        arrMap.put(key, "0");
      }
    }

    // 対象店に対する分配率が取得できない場合空のリストを返却
    if (arrMap.size() == 0) {
      calcList.add("");
      calcList.add(String.valueOf(tencds.size()));
      calcList.add("0");
      return calcList;
    }

    // ②(店の分配率/対象店の合計分配率) * 発注総数(小数点切り捨て) を実施
    for (HashMap.Entry<String, String> ritsu : arrMap.entrySet()) {

      int result = 0;

      // 分配率+-+店舗でkeyを作成
      // String key = bunpai + "-" + ritsu.getKey();
      String key = ritsu.getKey();
      String val = ritsu.getValue();

      // 分配率が0の店舗は0とする
      if (Integer.valueOf(val) != 0) {
        result = (int) ((Double.valueOf(val) / ritsukei) * htasu);
      }

      // 計算結果が1に満たない場合は0
      if (htsuMap.containsKey(key)) {
        if (result < 1) {
          htsuMap.replace(key, 0);
        } else {
          htsuMap.replace(key, result);
        }
      } else {
        if (result < 1) {
          htsuMap.put(key, 0);
        } else {
          htsuMap.put(key, result);
        }
      }
    }

    // ③展開結果の総数を求める
    for (HashMap.Entry<String, Integer> htsu : htsuMap.entrySet()) {
      // 発注数
      int val = htsu.getValue();

      // 発注数 > 0 の店舗数を求める
      tensukei += val;
    }

    // ④展開数総計と発注数総計を比較
    int plus = 0;
    if (htasu != 0 && tensukei < htasu) {
      plus = 1;
    } else if (htasu != 0 && tensukei > htasu) {
      plus = -1;
    }

    LinkedHashMap<String, Integer> sortArrMap = new LinkedHashMap<>();
    while (sortArrMap.size() != htsuMap.size()) {

      String azKey = "";
      int azVal = 0;

      for (HashMap.Entry<String, Integer> htsu : htsuMap.entrySet()) {

        String key = htsu.getKey();
        int val = htsu.getValue();

        // 既に登録されているkeyは除外
        if (sortArrMap.containsKey(key)) {
          continue;
        }

        // 降順
        if (plus > 0) {

          // ここに到達したkey,valueで一番大きいものを求める
          if (val >= azVal) {
            azVal = val;
            azKey = key;
          }
          // 昇順
        } else {

          // ここに到達したkey,valueで一番小さいものを求める
          if (val <= azVal) {
            azVal = val;
            azKey = key;
          }
        }
      }

      // 指定のソート順でput
      sortArrMap.put(azKey, azVal);
    }

    // key:店 val:発注数を格納(上のMapを初期化して使いまわす)
    arrMap = new HashMap<>();
    if (htasu == 0) {
      for (HashMap.Entry<String, Integer> htsu : sortArrMap.entrySet()) {
        String key = htsu.getKey(); // 店
        int val = htsu.getValue(); // 発注数

        if (!arrMap.containsKey(key)) {
          arrMap.put(key, String.valueOf(val));
        } else {
          arrMap.replace(key, String.valueOf(val));
        }

      }
    } else {
      while (tensukei != htasu) {
        for (HashMap.Entry<String, Integer> htsu : sortArrMap.entrySet()) {

          String key = htsu.getKey(); // 店
          int val = htsu.getValue(); // 発注数

          if (tensukei == htasu) {
            if (arrMap.size() == sortArrMap.size()) {
              break;
            } else if (!arrMap.containsKey(key)) {
              arrMap.put(key, String.valueOf(val));
            }
          } else {
            val += plus;
            if (!arrMap.containsKey(key)) {
              arrMap.put(key, String.valueOf(val));
            } else {
              arrMap.replace(key, String.valueOf(val));
            }
            tensukei += plus;
          }
        }
      }
    }

    String arr = "";
    int tennum = 0;

    for (int j = 1; j <= 400; j++) {

      String key = String.valueOf(j);
      String val = "";

      if (arrMap.containsKey(key)) {
        val = arrMap.get(key);

        for (int jj = tennum + 1; jj < Integer.valueOf(key); jj++) {
          arr += String.format("%5s", "");
        }
        arr += String.format("%05d", Integer.valueOf(val));
        tennum = Integer.valueOf(key);
      }
    }
    arr = new ReportJU012Dao(JNDIname).spaceArr(arr, 5);

    // 発注数配列、店舗数、展開数を返却
    calcList.add(arr);
    calcList.add(String.valueOf(tencds.size()));
    calcList.add(String.valueOf(tensukei));

    return calcList;
  }

  /**
   * 入力値から現在のDBに変更があるか確認
   *
   * @param moyskbn 催し区分
   * @param moysstdt 催し開始日
   * @param moysrban 催し連番
   * @param bmncd 部門コード
   * @param kanrino 管理番号
   * @param kanrieno 枝番
   * @param tenrankarr 店ランク配列
   * @param syukbn 実績率パタン数値
   * @param jskptnznenmkbn 実績率パタン前年同月
   * @param jskptnznenwkbn 実績率パタン前年同週
   * @param tenkaikbn 展開区分
   * @param nndt 納入日
   * @param ptnno パターン№
   * @param htasu 発注総数
   * @return JSONArray dbDatas
   */
  public JSONArray getST021UpdChk(String moyskbn, String moysstdt, String moysrban, String bmncd, String kanrino, String kanrieno, String tenrankarr, // 全特(ア有/無)_商品.店ランク配列
      String tenkaikbn, // 全特(ア有/無)_商品.展開区分
      String syukbn, // 全特(ア有/無)_商品.実績率パタン数値
      String jskptnznenmkbn, // 全特(ア有/無)_商品.実績率パタン前年同月
      String jskptnznenwkbn, // 全特(ア有/無)_商品.実績率パタン前年同週
      String nndt, // 全特(ア有/無)_納入日.納入日
      String ptnno, // 全特(ア有/無)_納入日.パターン№
      String htasu // 全特(ア有/無)_納入日.発注総数
  ) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlWhereNndt = "";
    String sqlFrom = " INATK.TOKSP_SHN T1, INATK.TOKSP_NNDT T2 "; // アン無

    // アン有
    if (Integer.valueOf(moysrban) >= 50) {
      sqlFrom = " INATK.TOKTG_SHN T1, INATK.TOKTG_NNDT T2 ";
    }

    ArrayList<String> paramData = new ArrayList<>();

    bmncd = String.valueOf(Integer.valueOf(bmncd));

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

    // 部門コード
    if (StringUtils.isEmpty(bmncd)) {
      sqlWhere += "T1.BMNCD=null AND ";
    } else {
      sqlWhere += "T1.BMNCD=? AND ";
      paramData.add(bmncd);
    }

    // 管理№
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "T1.KANRINO=null AND ";
    } else {
      sqlWhere += "T1.KANRINO=? AND ";
      paramData.add(kanrino);
    }

    // 枝番
    if (StringUtils.isEmpty(kanrieno)) {
      sqlWhere += "T1.KANRIENO=null AND ";
    } else {
      sqlWhere += "T1.KANRIENO=? AND ";
      paramData.add(kanrieno);
    }

    // 展開区分
    if (StringUtils.isEmpty(tenkaikbn)) {
      sqlWhere += "T1.TENKAIKBN=null AND ";
    } else {
      sqlWhere += "T1.TENKAIKBN=? AND ";
      paramData.add(tenkaikbn);
    }

    if (tenkaikbn.equals("3")) {
      // 実績率パタン数値
      if (StringUtils.isEmpty(syukbn)) {
        sqlWhere += "T1.JSKPTNSYUKBN=null AND ";
      } else {
        sqlWhere += "T1.JSKPTNSYUKBN=? AND ";
        paramData.add(syukbn);
      }

      // 実績率パタン前年同月
      if (StringUtils.isEmpty(jskptnznenmkbn)) {
        sqlWhere += "T1.JSKPTNZNENMKBN=null AND ";
      } else {
        sqlWhere += "T1.JSKPTNZNENMKBN=? AND ";
        paramData.add(jskptnznenmkbn);
      }

      // 実績率パタン前年同週
      if (StringUtils.isEmpty(jskptnznenwkbn)) {
        sqlWhere += "T1.JSKPTNZNENWKBN=null AND ";
      } else {
        sqlWhere += "T1.JSKPTNZNENWKBN=? AND ";
        paramData.add(jskptnznenwkbn);
      }
    }

    // 店ランク配列
    if (StringUtils.isEmpty(tenrankarr)) {
      sqlWhere += "T1.TENRANK_ARR=null AND ";
    } else {
      sqlWhere += "T1.TENRANK_ARR=? AND ";
      paramData.add(tenrankarr);
    }

    // 納入日
    if (StringUtils.isEmpty(nndt)) {
      sqlWhereNndt += "T2.NNDT=null AND ";
    } else {
      sqlWhereNndt += "T2.NNDT=? AND ";
      paramData.add(nndt);
    }

    // 発注総数
    if (StringUtils.isEmpty(htasu)) {
      sqlWhereNndt += "T2.HTASU IS NULL AND ";
    } else {
      sqlWhereNndt += "IFNULL(T2.HTASU,0)=? AND ";
      paramData.add(htasu);
    }

    // パターン№
    if (StringUtils.isEmpty(ptnno)) {
      sqlWhereNndt += "IFNULL(T2.PTNNO,0)=null ";
    } else {
      sqlWhereNndt += "IFNULL(T2.PTNNO,0)=? ";
      paramData.add(ptnno);
    }

    sbSQL.append(" SELECT ");
    sbSQL.append(" T2.TENHTSU_ARR ");
    sbSQL.append(" FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append(" WHERE " + sqlWhere);
    sbSQL.append(" T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" AND T1.MOYSKBN=T2.MOYSKBN ");
    sbSQL.append(" AND T1.MOYSSTDT=T2.MOYSSTDT ");
    sbSQL.append(" AND T1.MOYSRBAN=T2.MOYSRBAN ");
    sbSQL.append(" AND T1.BMNCD=T2.BMNCD ");
    sbSQL.append(" AND T1.KANRINO=T2.KANRINO ");
    sbSQL.append(" AND T1.KANRIENO=T2.KANRIENO ");
    sbSQL.append(" AND " + sqlWhereNndt);

    return ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
  }

  /**
   * 入力値から現在のDBに変更があるか確認
   *
   * @param moyskbn 催し区分
   * @param moysstdt 催し開始日
   * @param moysrban 催し連番
   * @param bmncd 部門コード
   * @param kanrino 管理番号
   * @param nndt 納入日
   * @return JSONArray dbDatas
   */
  public int getTjten(String moyskbn, String moysstdt, String moysrban, String bmncd, String kanrino, String tencd, String tjFlg) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = " INATK.TOKSP_TJTEN T1 "; // アン無

    // アン有
    if (Integer.valueOf(moysrban) >= 50) {
      sqlFrom = " INATK.TOKTG_TJTEN T1 ";
    }

    ArrayList<String> paramData = new ArrayList<>();

    bmncd = String.valueOf(Integer.valueOf(bmncd));

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

    // 部門コード
    if (StringUtils.isEmpty(bmncd)) {
      sqlWhere += "T1.BMNCD=null AND ";
    } else {
      sqlWhere += "T1.BMNCD=? AND ";
      paramData.add(bmncd);
    }

    // 管理№
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "T1.KANRINO=null AND ";
    } else {
      sqlWhere += "T1.KANRINO=? AND ";
      paramData.add(kanrino);
    }

    // 店舗
    if (StringUtils.isEmpty(tencd)) {
      sqlWhere += "T1.TENCD=null ";
    } else {
      sqlWhere += "T1.TENCD=? ";
      paramData.add(tencd);
    }

    // 対象除外フラグ 1:対象 2:除外
    if (StringUtils.isEmpty(tjFlg)) {
      sqlWhere += "T1.TJFLG=null ";
    } else {
      sqlWhere += "T1.TJFLG=? ";
      paramData.add(tjFlg);
    }

    sbSQL.append(" SELECT ");
    sbSQL.append(" T1.TENCD ");
    sbSQL.append(" FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append(" WHERE " + sqlWhere);

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return dbDatas.size();
  }

  /**
   * 入力値から現在のDBに変更があるか確認
   *
   * @param moyskbn 催し区分
   * @param moysstdt 催し開始日
   * @param moysrban 催し連番
   * @param bmncd 部門コード
   * @param kanrino 管理番号
   * @param nndt 納入日
   * @return JSONArray dbDatas
   */
  public ArrayList<String> getTG016NndtChgIni(String moyskbn, String moysstdt, String moysrban, String bmncd, String kanrino, String nndt) {

    JSONArray dbDatas = getTG016NndtChgBefore(moyskbn, moysstdt, moysrban, bmncd, kanrino, nndt);

    return getCalcList(dbDatas);
  }

  /**
   * 入力値から現在のDBに変更があるか確認
   *
   * @param moyskbn 催し区分
   * @param moysstdt 催し開始日
   * @param moysrban 催し連番
   * @param bmncd 部門コード
   * @param kanrino 管理番号
   * @param nndt 納入日
   * @return JSONArray dbDatas
   */
  public JSONArray getTG016NndtChgBefore(String moyskbn, String moysstdt, String moysrban, String bmncd, String kanrino, String nndt) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = " INATK.TOKSP_NNDT T1 "; // アン無

    // アン有
    if (Integer.valueOf(moysrban) >= 50) {
      sqlFrom = " INATK.TOKTG_NNDT T1 ";
    }

    ArrayList<String> paramData = new ArrayList<>();

    bmncd = String.valueOf(Integer.valueOf(bmncd));

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

    // 部門コード
    if (StringUtils.isEmpty(bmncd)) {
      sqlWhere += "T1.BMNCD=null AND ";
    } else {
      sqlWhere += "T1.BMNCD=? AND ";
      paramData.add(bmncd);
    }

    // 管理№
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "T1.KANRINO=null AND ";
    } else {
      sqlWhere += "T1.KANRINO=? AND ";
      paramData.add(kanrino);
    }

    // 納入日
    if (StringUtils.isEmpty(nndt)) {
      sqlWhere += "T1.NNDT=null ";
    } else {
      sqlWhere += "T1.NNDT=? ";
      paramData.add(nndt);
    }

    sbSQL.append(" SELECT ");
    sbSQL.append(" T1.TENHTSU_ARR ");
    sbSQL.append(" FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append(" WHERE " + sqlWhere);

    return ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
  }


  /**
   * 配列をkye,valueの形で返却(key:店、value:数量)
   *
   * @param moyskbn 催し区分
   * @param moysstdt 催し開始日
   * @param moysrban 催し連番
   * @param bmncd 部門コード
   * @param kanrino 管理番号
   * @param kanrieno 枝番
   * @param rankNoAdd 対象ランク
   * @param rankNoDel 除外ランク
   * @param tenCdAdds 追加店
   * @param rankAdds 追加ランク
   * @param tenCdDels 除外店
   * @param tenrankarr 店ランク配列
   * @param tenkaikbn 展開区分
   * @param syukbn 実績率パタン数値
   * @param jskptnznenmkbn 実績率パタン前年同月
   * @param jskptnznenwkbn 実績率パタン前年同週
   * @param nndt 納入日
   * @param ptnno パターン№
   * @param htasu 発注総数
   * @return htsuMap
   */
  public ArrayList<String> getST021UpdArr(String moyskbn, String moysstdt, String moysrban, String bmncd, String kanrino, String kanrieno, String rankNoAdd, String rankNoDel, JSONArray tenCdAdds,
      JSONArray rankAdds, JSONArray tenCdDels, String tenkaikbn, // 全特(ア有/無)_商品.展開区分
      String syukbn, // 全特(ア有/無)_商品.実績率パタン数値
      String jskptnznenmkbn, // 全特(ア有/無)_商品.実績率パタン前年同月
      String jskptnznenwkbn, // 全特(ア有/無)_商品.実績率パタン前年同週
      String nndt, // 全特(ア有/無)_納入日.納入日
      String ptnno, // 全特(ア有/無)_納入日.パターン№
      String htasu // 全特(ア有/無)_納入日.発注総数
  ) {

    // 店別数量画面での変更があったか確認
    ArrayList<String> tenranks = getTenrankArray(bmncd, moyskbn, moysstdt, moysrban, rankNoAdd, rankNoDel, tenCdAdds, rankAdds, tenCdDels, "");

    String arr = "";
    for (String rank : tenranks) {
      if (StringUtils.isEmpty(rank)) {
        arr += String.format("%1s", "");
      } else {
        arr += rank;
      }
    }

    arr = new ReportJU012Dao(JNDIname).spaceArr(arr, 1);

    JSONArray dbDatas = getST021UpdChk(moyskbn, moysstdt, moysrban, bmncd, kanrino, kanrieno, arr, tenkaikbn, syukbn, jskptnznenmkbn, jskptnznenwkbn, nndt, ptnno, htasu);

    return getCalcList(dbDatas);
  }

  public ArrayList<String> getCalcList(JSONArray dbDatas) {
    // データが存在する場合、配列の展開を実施
    String arr = "";
    HashMap<String, String> htsuMap = new HashMap<>();
    ArrayList<String> calcList = new ArrayList<>();
    int digit = 5;

    if (dbDatas.size() != 0) {
      arr = dbDatas.getJSONObject(0).optString("TENHTSU_ARR");
      htsuMap = new ReportJU012Dao(JNDIname).getDigitMap(arr, digit, "1");
    } else {
      return calcList;
    }

    int tensukei = 0;

    // ①展開結果の総数を求める
    for (HashMap.Entry<String, String> htsu : htsuMap.entrySet()) {
      // 発注数
      int val = Integer.valueOf(htsu.getValue());

      // 発注数 > 0 の店舗数を求める
      tensukei += val;
    }

    // 発注数配列、店舗数、展開数を返却
    calcList.add(arr);
    calcList.add(String.valueOf(htsuMap.size()));
    calcList.add(String.valueOf(tensukei));

    return calcList;
  }

  /***
   *
   * 本体売価と部門を返却
   *
   * @param shnCd
   * @return ArrayList<String>
   */
  public JSONArray getShnInfo(String shnCd, String shoriDt) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    sbSQL.append("SELECT ");
    sbSQL.append("T2.ZEIRT ");
    sbSQL.append(",T1.DAICD ");
    sbSQL.append(",T1.CHUCD ");
    sbSQL.append("FROM ");
    sbSQL.append("(SELECT ");
    sbSQL.append("T1.DAICD ");
    sbSQL.append(",T1.CHUCD ");
    sbSQL.append(",CASE ");
    sbSQL.append("WHEN T1.ZEIKBN = '0' AND T1.ZEIRTHENKODT <= ? THEN T1.ZEIRTKBN ");
    sbSQL.append("WHEN T1.ZEIKBN = '0' AND T1.ZEIRTHENKODT > ? THEN T1.ZEIRTKBN_OLD ");
    sbSQL.append("WHEN T1.ZEIKBN = '3' AND T2.ZEIKBN = '0' AND T2.ZEIRTHENKODT <= ? THEN T2.ZEIRTKBN ");
    sbSQL.append("WHEN T1.ZEIKBN = '3' AND T2.ZEIKBN = '0' AND T2.ZEIRTHENKODT > ? THEN T2.ZEIRTKBN_OLD ELSE null END ZEIRTKBN");
    paramData.add(shoriDt);
    paramData.add(shoriDt);
    paramData.add(shoriDt);
    paramData.add(shoriDt);
    sbSQL.append(" FROM ");
    sbSQL.append("INAMS.MSTSHN T1 ");
    sbSQL.append(",INAMS.MSTBMN T2 ");
    sbSQL.append("WHERE ");

    // 商品コード
    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "T1.SHNCD=null AND ";
    } else {
      sqlWhere += "T1.SHNCD=? AND ";
      paramData.add(shnCd);
    }

    sbSQL.append(sqlWhere);
    sbSQL.append("T1.BMNCD = T2.BMNCD ");
    sbSQL.append(") T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN = T2.ZEIRTKBN");

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return dbDatas;
  }

  public String getArrMap(HashMap<String, String> map) {

    ArrayList<String> calcList = new ArrayList<>();
    ArrayList<String> nndtList = new ArrayList<>();
    String sqlcommand = "SELECT '' AS F1,'' AS F2,'' AS F3 ";
    int col = 0;

    // 検索条件などの情報を設定
    super.setMap(map);

    int htasu = 0;
    String ptnNo = "";
    String ptn = getMap().get("PTN"); // 1:通常率パターン 2:数量パターン 3:実績率パターン

    // 発注総数、パターンの入力が存在するか
    if (!StringUtils.isEmpty(getMap().get("HTASU"))) {
      htasu = Integer.valueOf(getMap().get("HTASU")); // 発注総数
    }

    if (!StringUtils.isEmpty(getMap().get("PTNNO"))) {
      ptnNo = getMap().get("PTNNO"); // パターン
    }

    if (!StringUtils.isEmpty(ptnNo)) {

      // 商品コードをもとに本体売価(部門)を求める
      String shncd = getMap().get("SHNCD"); // 商品コード
      String bmncd = getMap().get("BMNCD"); // 部門コード
      String daicd = ""; // 大分類
      String chucd = ""; // 中分類
      String shoridt = getMap().get("SHORIDT"); // 処理日付
      String syukbn = getMap().get("SYUKBN"); // 数値区分
      JSONArray getShnInf = getShnInfo(shncd, shoridt);

      if (getShnInf.size() != 0) {
        daicd = getShnInf.getJSONObject(0).optString("DAICD");
        chucd = getShnInf.getJSONObject(0).optString("CHUCD");
      }

      /* 対象店作成用情報 */
      String moysKbn = getMap().get("MOYSKBN"); // 催し区分
      String moysStDt = getMap().get("MOYSSTDT"); // 催し開始日
      String moysRban = getMap().get("MOYSRBAN"); // 催し連番

      String rankNoAdd = getMap().get("RANKNOADD"); // ランク№(追加)
      String rankNoDel = getMap().get("RANKNODEL"); // ランク№(除外)
      JSONArray tenCdAdds = JSONArray.fromObject(getMap().get("TENCDADDS")); // 対象店
      JSONArray rankAdds = JSONArray.fromObject(getMap().get("RANKADDS")); // 対象ランク
      JSONArray tenCdDels = JSONArray.fromObject(getMap().get("TENCDDELS")); // 除外店

      String kanrino = getMap().get("KANRINO"); // 管理番号
      String nndt = getMap().get("NNDT"); // 納入日

      if (!getMap().get("UPD").equals("2")) {
        nndtList = getTG016NndtChgIni(moysKbn, moysStDt, moysRban, bmncd, kanrino, nndt);
        if (getMap().get("UPD").equals("1")) {
          calcList = nndtList;
        }
      }

      if (calcList.size() == 0) {
        String saveTenrankArr = getMap().get("SAVETENRANKARR"); // 店ランク配列
        if (!ptn.equals("2")) {

          String wwmm = getMap().get("WWMM"); // 週月フラグ
          String shuno = ptnNo; // 週№

          // 対象店を取得
          Set<Integer> tencds = new TreeSet<>();
          if (StringUtils.isEmpty(saveTenrankArr)) {
            tencds = getTenCdAdd(bmncd // 部門コード
                , moysKbn // 催し区分
                , moysStDt // 催し開始日
                , moysRban // 催し連番
                , rankNoAdd // 対象ランク№
                , rankNoDel // 除外ランク№
                , tenCdAdds // 対象店
                , tenCdDels // 除外店
            );
          } else {
            tencds = getTenCds(saveTenrankArr.split(""), new String[] {}, tenCdAdds, tenCdDels);
          }

          // 店舗数・展開数をListで返却
          calcList = getRtPtArr(bmncd, ptnNo, wwmm, syukbn, shuno, daicd, chucd, htasu, tencds, ptn);
        } else if (ptn.equals("2")) {
          // 店舗数・展開数をListで返却
          calcList = getSuryoArr(bmncd, moysKbn, moysStDt, moysRban, rankNoAdd, rankNoDel, tenCdAdds, rankAdds, tenCdDels, saveTenrankArr, ptnNo);
        }
      }

      if (calcList.size() != 0 && nndtList.size() != 0) {

        HashMap<String, String> nndtListMap = new HashMap<>();
        HashMap<String, String> calcListMap = new HashMap<>();
        HashMap<String, String> mergeMap = new HashMap<>();

        // ①納入日テーブルの発注数配列を元に作成したもの
        nndtListMap = new ReportJU012Dao(JNDIname).getDigitMap(nndtList.get(0), 5, "1");

        // ②最新のマスタを元に作成したもの
        calcListMap = new ReportJU012Dao(JNDIname).getDigitMap(calcList.get(0), 5, "1");

        // ①にあって②にないものが除外店の場合①から削除
        // ①にも②にもあるものは①のを保持
        for (HashMap.Entry<String, String> getMap_1 : nndtListMap.entrySet()) {
          String key = getMap_1.getKey();
          String value = getMap_1.getValue().trim();
          // ①にあって②にないものが除外店の場合①から削除
          // ①にあって②になくて今回除外店にも入力のないものは追加店から削除された場合を加味しテーブルを参照
          if (!calcListMap.containsKey(key)) {
            if (!tenCdDels.contains(key) && getTjten(moysKbn, moysStDt, moysRban, bmncd, kanrino, key, "1") != 0) {
              mergeMap.put(key, value);
            }

            // ①にも②にもあるものは①のを保持
          } else if (calcListMap.containsKey(key)) {
            mergeMap.put(key, value);
          }
        }

        // ①になくて②にあるものが追加店の場合①に0で追加(TODO:マスタの値見る？)
        for (HashMap.Entry<String, String> getMap_2 : calcListMap.entrySet()) {
          String key = getMap_2.getKey();
          // ①になくて②にあるものが追加店の場合①に0で追加(TODO:マスタの値見る？)
          if (!nndtListMap.containsKey(key)) {
            if (tenCdAdds.contains(key) || getTjten(moysKbn, moysStDt, moysRban, bmncd, kanrino, key, "2") != 0) {
              mergeMap.put(key, "0");
            }
          }
        }

        String arr = "";
        int tennum = 0;
        int tensu = 0;
        int tenhtsukei = 0;
        for (int j = 1; j <= 400; j++) {

          String key = String.valueOf(j);
          String val = "";

          if (mergeMap.containsKey(key)) {
            val = mergeMap.get(key);

            for (int jj = tennum + 1; jj < Integer.valueOf(key); jj++) {
              arr += String.format("%5s", "");
            }
            arr += String.format("%05d", Integer.valueOf(val));
            tennum = Integer.valueOf(key);
            tensu++;
            tenhtsukei += Integer.valueOf(val);
          }
        }
        calcList.set(0, arr);
        calcList.set(1, String.valueOf(tensu));
        calcList.set(2, String.valueOf(tenhtsukei));
      }
    }

    for (int i = 0; i < calcList.size(); i++) {

      if (col == 0) {
        sqlcommand = "SELECT ";
      }

      col = i + 1;

      sqlcommand += "'" + calcList.get(i) + "' AS F" + col;

      if (calcList.size() != col) {
        sqlcommand += ",";
      }
    }

    return sqlcommand += " FROM (SELECT 1 AS DUMMY) DUMMY";
  }
}
