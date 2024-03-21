package dao;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportGM003Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /** セット番号保持 */
  String stno = "";

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportGM003Dao(String JNDIname) {
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

  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();

    JSONArray msgList = this.check(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return option;
  }

  // 削除処理
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();

    JSONArray msgList = this.checkDel(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.deleteData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return option;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(BM催し送信)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_BMSHN")); // 更新情報(BM催し送信＿商品一覧)
    JSONArray dataArrayG2 = JSONArray.fromObject(map.get("DATA_BMSHN2")); // 更新情報(BM催し送信＿商品一覧)

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    MessageUtility mu = new MessageUtility();
    JSONArray dbDatas = new JSONArray();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String rankNoAdd = "";
    String rankNoDel = "";
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();
    ArrayList<JSONObject> cTenCdAdds = new ArrayList<>();
    HashSet<String> tenCdAdds_ = new HashSet<>();
    ArrayList<JSONObject> cTenCdDels = new ArrayList<>();
    HashSet<String> tenCdDels_ = new HashSet<>();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<>();

    String JNDIname = Defines.STR_JNDI_DS;

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    if (dataArrayG.size() == 0 || dataArrayG.getJSONObject(0).isEmpty()) {
      msg.add(mu.getDbMessageObj("E20118", new String[] {"セット1に商品コードの"}));
      return msg;
    }

    if (dataArrayG2.size() == 0 || dataArrayG2.getJSONObject(0).isEmpty()) {
      msg.add(mu.getDbMessageObj("E20118", new String[] {"セット2に商品コードの"}));
      return msg;
    }


    // 催しコード存在チェック
    // 変数を初期化
    sbSQL = new StringBuffer();
    new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<>();
    JSONObject data = dataArray.getJSONObject(0);

    if (!StringUtils.isEmpty(data.optString("F1")) && data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    } else {
      msg.add(mu.getDbMessageObj("E20005", new String[] {}));
      return msg;
    }

    if (!moyskbn.equals("1") && !moyskbn.equals("2") && !moyskbn.equals("3")) {
      msg.add(mu.getDbMessageObj("E20004", new String[] {}));
      return msg;
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN=? AND ";
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
      sqlWhere += "MOYSRBAN=null";
    } else {
      sqlWhere += "MOYSRBAN=?";
      paramData.add(moysrban);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("MOYSKBN "); // レコード件数
    sbSQL.append(",PLUSFLG "); // PLU配信済みフラグ

    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // 登録のない催しコード
      msg.add(mu.getDbMessageObj("E20100", new String[] {}));
      return msg;
    } else {
      // 催しコードのPLU配信済みフラグチェック
      if (!StringUtils.equals(DefineReport.Button.SEL_CHANGE.getObj(), sendBtnid) && dbDatas.getJSONObject(0).optString("PLUSFLG").equals("1")) {
        // PLU配信済みフラグ=1の場合はエラー
        msg.add(mu.getDbMessageObj("E20272", new String[] {}));
        return msg;
      }


      if (!StringUtils.isEmpty(data.optString("F3")) && (data.optInt("F3") > data.optInt("F7"))) {

        // 販売期間(開始日) ≧ 催し期間(開始日)の条件で入力してください。
        msg.add(mu.getDbMessageObj("E40016", new String[] {}));
        return msg;
      } else if (!StringUtils.isEmpty(data.optString("F4")) && (data.optInt("F4") < data.optInt("F8"))) {

        // 販売期間(終了日) ≦ 催し期間(終了日)の条件で入力してください。
        msg.add(mu.getDbMessageObj("E40016", new String[] {}));
        return msg;
      } else if (data.optInt("F7") > data.optInt("F8")) {

        // 販売期間(開始日) ≦ 販売期間(終了日)の条件で入力してください。
        msg.add(mu.getDbMessageObj("E20006", new String[] {}));
        return msg;
      }
    }

    // 対象店、除外店ランク№に入力がある場合、同一の値はエラー
    if (!StringUtils.isEmpty(data.optString("F11")) || !StringUtils.isEmpty(data.optString("F12"))) {
      rankNoAdd = data.optString("F11");
      rankNoDel = data.optString("F12");

      if (rankNoAdd.equals(rankNoDel)) {
        // 対象店・除外店ランク№が同一
        msg.add(mu.getDbMessageObj("E20016", new String[] {}));
        return msg;
      }
    }

    // 対象店、除外店ランク№に入力がある場合ランクマスタに存在してるかチェック
    if (!StringUtils.isEmpty(rankNoAdd)) {
      // 変数を初期化
      sbSQL = new StringBuffer();
      new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<>();

      sqlFrom = "INATK.TOKRANK ";

      // 部門コードが入力されてたら
      if (!StringUtils.isEmpty(data.optString("F10"))) {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(data.optString("F10"));
      }

      if (Integer.valueOf(rankNoAdd) >= 900) {

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
      paramData.add(rankNoAdd);

      // 一覧表情報
      sbSQL = new StringBuffer();
      sbSQL.append("SELECT ");
      sbSQL.append("TENRANK_ARR ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // 存在しない対象店ランク№
        msg.add(mu.getDbMessageObj("E20014", new String[] {}));
        return msg;
      }
    }

    if (!StringUtils.isEmpty(rankNoDel)) {
      // 変数を初期化
      sbSQL = new StringBuffer();
      new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<>();

      sqlFrom = "INATK.TOKRANK ";

      // 部門コードが入力されてたら
      if (!StringUtils.isEmpty(data.optString("F10"))) {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(data.optString("F10"));
      }

      if (Integer.valueOf(rankNoDel) >= 900) {

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
      paramData.add(rankNoDel);

      // 一覧表情報
      sbSQL = new StringBuffer();
      sbSQL.append("SELECT ");
      sbSQL.append("TENRANK_ARR ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // 存在しない除外店ランク№
        msg.add(mu.getDbMessageObj("E20015", new String[] {}));
        return msg;
      }
    }

    int addCnt = 30;
    int delCnt = 20;

    Set<String> checkTencds = new TreeSet<>();

    for (int i = 0; i < 10; i++) {

      String val = data.optString("F" + addCnt);

      if (checkTencds.contains(val)) {
        msg.add(mu.getDbMessageObj("E20024", new String[] {}));
        return msg;
      } else if (!StringUtils.isEmpty(val)) {
        checkTencds.add(val);
      }

      if (StringUtils.isNotEmpty(val)) {
        String msgCd = checkTenCd(val);
        if (!StringUtils.isEmpty(msgCd)) {
          msg.add(mu.getDbMessageObj(msgCd, new String[] {}));
          return msg;
        }
        cTenCdAdds.add(data);
        tenCdAdds_.add(val);
      }
      tenCdAdds.add(i, data.optString("F" + addCnt));

      val = data.optString("F" + delCnt);

      if (checkTencds.contains(val)) {
        msg.add(mu.getDbMessageObj("E20024", new String[] {}));
        return msg;
      } else if (!StringUtils.isEmpty(val)) {
        checkTencds.add(val);
      }

      if (StringUtils.isNotEmpty(val)) {
        String msgCd = checkTenCd(val);
        if (!StringUtils.isEmpty(msgCd)) {
          msg.add(mu.getDbMessageObj(msgCd, new String[] {}));
          return msg;
        }
        cTenCdDels.add(data);
        tenCdDels_.add(val);
      }
      tenCdDels.add(i, data.optString("F" + delCnt));

      addCnt++;
      delCnt++;
    }

    // 追加店重複
    if (cTenCdAdds.size() != tenCdAdds_.size()) {
      msg.add(mu.getDbMessageObj("E11040", new String[] {"対象店"}));
      return msg;
    }

    // 除外店重複
    if (cTenCdDels.size() != tenCdDels_.size()) {
      msg.add(mu.getDbMessageObj("E11040", new String[] {"除外店"}));
      return msg;
    }

    // 対象店を取得
    String checkTencd = new ReportBM015Dao(JNDIname).checkTenCdAdd(data.optString("F10") // 部門コード
        , moyskbn // 催し区分
        , moysstdt // 催し開始日
        , moysrban // 催し連番
        , rankNoAdd // 対象ランク№
        , rankNoDel // 除外ランク№
        , tenCdAdds // 対象店
        , tenCdDels // 除外店
    );

    if (!StringUtils.isEmpty(checkTencd)) {
      msg.add(mu.getDbMessageObj(checkTencd, new String[] {}));
      return msg;
    }

    // 入力値の重複チェック（商品コード）、PLUFLG=1の場合削除は不可
    JSONArray dataArrayShn = new JSONArray();
    String shnSet = "";
    for (int j = 0; j < 2; j++) {
      if (j == 0) {
        dataArrayShn = dataArrayG;
        shnSet = "1";
      } else {
        dataArrayShn = dataArrayG2;
        shnSet = "2";
      }

      // 商品チェック
      for (int i = 0; i < dataArrayShn.size(); i++) {
        JSONObject dataG = new JSONObject();
        dataG = dataArrayShn.getJSONObject(i);
        if (dataG.isEmpty()) {
          continue;
        }
        String shncd = dataG.optString("F1");
        // 変数を初期化
        sbSQL = new StringBuffer();
        new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<>();
        if (StringUtils.isEmpty(shncd)) {
          sqlWhere += "SHNCD=null";
        } else {
          sqlWhere += "SHNCD=?";
          paramData.add(shncd);
        }

        sbSQL.append("SELECT ");
        sbSQL.append("SHNCD "); // レコード件数
        sbSQL.append("FROM ");
        sbSQL.append("INAMS.MSTSHN ");
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere); // 入力された商品コードで検索

        dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() == 0) {
          // マスタに登録のない商品
          if ("1".equals(shnSet)) {
            msg.add(mu.getDbMessageObj("E11046", new String[] {"セット１に"}));
            return msg;
          } else if ("2".equals(shnSet)) {
            msg.add(mu.getDbMessageObj("E11046", new String[] {"セット２に"}));
            return msg;
          }
        }
      }



      for (int i = 0; i < dataArrayShn.size(); i++) {
        String val = dataArrayShn.optJSONObject(i).optString("F1");

        for (int k = i + 1; k < dataArrayShn.size(); k++) {
          String val2 = dataArrayShn.optJSONObject(k).optString("F1");
          if (val.equals(val2)) {
            if ("1".equals(shnSet)) {
              String shncd1 = val.substring(0, 4);
              String shncd2 = val.substring(4, 8);
              msg.add(mu.getDbMessageObj("E20572", new String[] {"セット１で商品コード" + shncd1 + "-" + shncd2}));
            } else if ("2".equals(shnSet)) {
              String shncd1 = val.substring(0, 4);
              String shncd2 = val.substring(4, 8);
              msg.add(mu.getDbMessageObj("E20572", new String[] {"セット２で商品コード" + shncd1 + "-" + shncd2}));
            }
            return msg;
          }
        }
      }

      if ("2".equals(shnSet)) {
        for (int i = 0; i < dataArrayG.size(); i++) {
          String val = dataArrayG.optJSONObject(i).optString("F1");

          for (int k = 0; k < dataArrayG2.size(); k++) {
            String val2 = dataArrayG2.optJSONObject(k).optString("F1");
            if (val.equals(val2)) {
              String shncd1 = val2.substring(0, 4);
              String shncd2 = val2.substring(4, 8);
              msg.add(mu.getDbMessageObj("E20572", new String[] {"セット１とセット２で商品コード" + shncd1 + "-" + shncd2}));
              return msg;
            }
          }
        }
      }


    }

    if (data.optString("F6").indexOf("”") != -1 || data.optString("F6").indexOf("、") != -1) {
      msg.add(mu.getDbMessageObj("E11302", new String[] {"セット名称", "", "。(カンマ、ダブルクォーテーションは入力不可)"}));
      return msg;
    }
    return msg;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

    // 削除データ検索用コード
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(プライスカード発行トラン)

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    MessageUtility mu = new MessageUtility();
    JSONArray dbDatas = new JSONArray();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";


    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 催しコード存在チェック
    // 変数を初期化
    sbSQL = new StringBuffer();
    new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<>();
    JSONObject data = dataArray.getJSONObject(0);

    if (!StringUtils.isEmpty(data.optString("F1")) && data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN=? AND ";
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
      sqlWhere += "MOYSRBAN=null";
    } else {
      sqlWhere += "MOYSRBAN=?";
      paramData.add(moysrban);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("MOYSKBN "); // レコード件数
    sbSQL.append(",PLUSFLG "); // PLU配信済みフラグ
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // 催しコードのPLU配信済みフラグチェック
    if (dbDatas.getJSONObject(0).optString("PLUSFLG").equals("1")) {
      // PLU配信済みフラグ=1の場合はエラー
      msg.add(mu.getDbMessageObj("E20510", new String[] {}));
      return msg;
    }

    return msg;
  }


  public String checkTenCd(String tenCd) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    // 店コードが401以上のものはエラー
    if (Integer.valueOf(tenCd) > 400) {
      return "E20520";
    }

    // 店存在チェック
    if (StringUtils.isEmpty(tenCd)) {
      sqlWhere += "TENCD=null AND ";
    } else {
      sqlWhere += "TENCD=? AND ";
      paramData.add(tenCd);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("TENCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      return "E11096";
    }

    return "";
  }


  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(BM催し送信)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_BMSHN")); // 更新情報(BM催し送信＿商品一覧)
    JSONArray dataArrayG2 = JSONArray.fromObject(map.get("DATA_BMSHN2")); // 更新情報(BM催し送信＿商品一覧)

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // BM催し送信情報INSERT/UPDATE処理
    createSqlBm(data, dataArrayG, dataArrayG2, userInfo);

    // 排他チェック実行
    if (!StringUtils.isEmpty(data.optString("F1"))) {

      String moyskbn = "";
      String moysstdt = "";
      String moysrban = "";

      if (data.optString("F1").length() >= 8) {
        moyskbn = data.optString("F1").substring(0, 1);
        moysstdt = data.optString("F1").substring(1, 7);
        moysrban = data.optString("F1").substring(7);
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        targetWhere = "MOYSKBN=null AND ";
      } else {
        targetWhere = "MOYSKBN=? AND ";
        targetParam.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        targetWhere += "MOYSSTDT=null AND ";
      } else {
        targetWhere += "MOYSSTDT=? AND ";
        targetParam.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        targetWhere += "MOYSRBAN=null AND ";
      } else {
        targetWhere += "MOYSRBAN=? AND ";
        targetParam.add(moysrban);
      }

      // セット番号
      if (StringUtils.isEmpty(stno)) {
        targetWhere += "STNO=null ";
      } else {
        targetWhere += "STNO=? ";
        targetParam.add(stno);
      }

      targetTable = "INATK.TOKMM_KKK";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F19"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
    }

    ArrayList<Integer> countList = new ArrayList<>();
    if (sqlList.size() > 0) {
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
      if (count == 0) {
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(BM催し送信)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_BMSHN")); // 更新情報(BM催し送信＿商品一覧)
    JSONArray.fromObject(map.get("DATA_BMSHN2"));

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // BM催し送信情報DELETE(論理削除)処理
    createDelSqlHs(data, dataArrayG, userInfo);

    // 排他チェック実行
    if (!StringUtils.isEmpty(data.optString("F1"))) {

      String moyskbn = "";
      String moysstdt = "";
      String moysrban = "";

      if (data.optString("F1").length() >= 8) {
        moyskbn = data.optString("F1").substring(0, 1);
        moysstdt = data.optString("F1").substring(1, 7);
        moysrban = data.optString("F1").substring(7);
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        targetWhere = "MOYSKBN=null AND ";
      } else {
        targetWhere = "MOYSKBN=? AND ";
        targetParam.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        targetWhere += "MOYSSTDT=null AND ";
      } else {
        targetWhere += "MOYSSTDT=? AND ";
        targetParam.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        targetWhere += "MOYSRBAN=null AND ";
      } else {
        targetWhere += "MOYSRBAN=? AND ";
        targetParam.add(moysrban);
      }

      // セット番号
      targetWhere += "STNO=? ";
      targetParam.add(stno);

      targetTable = "INATK.TOKMM_KKK";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F19"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
    }

    ArrayList<Integer> countList = new ArrayList<>();
    if (sqlList.size() > 0) {
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
      if (count == 0) {
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * BM催し送信INSERT/UPDATE処理
   *
   * @param data
   * @param dataArrayG
   * @param userInfo
   */
  public String createSqlBm(JSONObject data, JSONArray dataArrayG, JSONArray dataArrayG2, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    new JSONObject();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    Object[] valueData = new Object[] {};
    String values = "";
    new ItemList();
    new JSONArray();
    ArrayList<String> prmData = new ArrayList<>();
    new ArrayList<String>();
    stno = data.optString("F5");
    // 入力値格納用変数
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String tenAtuk_Arr = "";
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();

    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }


    // 店扱いフラグ配列作成

    int addCnt = 30;
    int delCnt = 20;

    for (int i = 0; i < 10; i++) {
      tenCdAdds.add(i, data.optString("F" + addCnt));
      tenCdDels.add(i, data.optString("F" + delCnt));
      addCnt++;
      delCnt++;
    }

    // 対象店を取得
    Set<Integer> tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(data.optString("F10") // 部門コード
        , moyskbn // 催し区分
        , moysstdt // 催し開始日
        , moysrban // 催し連番
        , data.optString("F11") // 対象ランク№
        , data.optString("F12") // 除外ランク№
        , tenCdAdds // 対象店
        , tenCdDels // 除外店
    );

    Iterator<Integer> ten = tencds.iterator();
    int min = 0;
    int max = 0;
    for (int i = 0; i < tencds.size(); i++) {
      min = max;
      max = ten.next();
      for (int j = min; j < max; j++) {
        if (j + 1 == max) {
          tenAtuk_Arr += "1";
        } else {
          tenAtuk_Arr += " ";
        }
      }
    }

    int maxField = 20; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // 催しコードを追加
        values += " ?, ?, ?, ";
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
      }

      String val = data.optString(key);
      if (key.equals("F5")) {
        values += " ?, ";
        prmData.add(stno);
      } else if (key.equals("F6")) {
        if (StringUtils.isEmpty(val)) {
          values += "null, ";
        } else {
          values += " ?, ";
          prmData.add(val);
        }
        // 販売期間Fromが空の場合は催し期間Fromを使用
      } else if (key.equals("F7")) {
        values += " ?, ";
        prmData.add(val);
        // 販売期間Toが空の場合は催し期間Toを使用
      } else if (key.equals("F8")) {
        values += " ?, ";
        prmData.add(val);
      } else if (key.equals("F9")) {
        if (StringUtils.isEmpty(val)) {
          values += " null, ";
        } else {
          values += " ?, ";
          prmData.add(val);
        }
      } else if (key.equals("F10")) {
        values += "?, ";
        prmData.add(val);
      } else if (key.equals("F11")) {
        values += "?, ";
        prmData.add(val);
      } else if (key.equals("F12")) {
        if (StringUtils.isEmpty(val)) {
          values += " null, ";
        } else {
          values += "?, ";
          prmData.add(val);
        }
      } else if (key.equals("F20")) {
        values += " ?, ";
        prmData.add(tenAtuk_Arr);
      }
      if (k == maxField) {
        values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + " ";
        values += " ,'" + userId + "' ";
        values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values += "FROM INATK.TOKMM_KKK WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt + " AND MOYSRBAN = " + moysrban + " AND STNO = " + stno + " LIMIT 1 ) T2 ) ";

        values += " ,CURRENT_TIMESTAMP ";
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }



    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKMM_KKK ( ");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",STNO"); // セット番号
    sbSQL.append(",STNM"); // セット名称
    sbSQL.append(",HBSTDT"); // 販売期間_開始日
    sbSQL.append(",HBEDDT"); // 販売期間_終了日
    sbSQL.append(",ESTGK"); // 成立価格
    sbSQL.append(",BMNCD_RANK"); // 部門ランク
    sbSQL.append(",RANKNO_ADD"); // ランク用部門
    sbSQL.append(",RANKNO_DEL"); // 除外店ランク
    sbSQL.append(",TENATSUK_ARR"); // 店扱いフラグ配列
    sbSQL.append(",UPDKBN"); // 更新区分：
    sbSQL.append(",SENDFLG");// 送信区分：
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT"); // 登録日
    sbSQL.append(",UPDDT"); // 更新日：
    sbSQL.append(") VALUES ");
    sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("セット販売企画");

    // 対象除外店を一度全て削除
    sqlWhere = "";
    paramData = new ArrayList<>();

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
    // 明日はここから
    // セット番号
    if (StringUtils.isEmpty(stno)) {
      sqlWhere += "STNO=null ";
    } else {
      sqlWhere += "STNO=? ";
      paramData.add(stno);
    }


    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.TOKMM_ADTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("セット販売_対象除外店");

    // クリア
    prmData = new ArrayList<>();
    values = "";

    // 入力された対象店・除外店のvaluesを作成
    for (int i = 0; i < 10; i++) {
      if (!StringUtils.isEmpty(tenCdAdds.getString(i))) {
        if (!values.equals("")) {
          values += ",";
        }
        values += "(1,?,?,?,?,?," + DefineReport.Values.SENDFLG_UN.getVal() + ",'" + userId + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
        prmData.add(tenCdAdds.getString(i));
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(stno);
      }
      if (!StringUtils.isEmpty(tenCdDels.getString(i))) {
        if (!values.equals("")) {
          values += ",";
        }
        values += "(0,?,?,?,?,?," + DefineReport.Values.SENDFLG_UN.getVal() + ",'" + userId + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
        prmData.add(tenCdDels.getString(i));
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(stno);
      }
    }

    // 対象店・除外店に入力があった場合Insertを実行
    if (!StringUtils.isEmpty(values)) {
      sbSQL = new StringBuffer();
      sbSQL.append("REPLACE INTO INATK.TOKMM_ADTEN ( ");
      sbSQL.append("ADDDELFLG");
      sbSQL.append(",TENCD");
      sbSQL.append(",MOYSKBN");
      sbSQL.append(",MOYSSTDT");
      sbSQL.append(",MOYSRBAN");
      sbSQL.append(",STNO");
      sbSQL.append(",SENDFLG ");
      sbSQL.append(",OPERATOR");
      sbSQL.append(",ADDDT");
      sbSQL.append(",UPDDT");
      sbSQL.append(")VALUES" + values + " ");


      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("セット販売_対象除外店");
    }

    // 対象除外店を一度全て削除
    sqlWhere = "";
    paramData = new ArrayList<>();

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
    // 明日はここから
    // セット番号
    if (StringUtils.isEmpty(stno)) {
      sqlWhere += "STNO=null ";
    } else {
      sqlWhere += "STNO=? ";
      paramData.add(stno);
    }


    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.TOKMM_SHO ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("セット販売_商品");

    // クリア
    prmData = new ArrayList<>();
    new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    maxField = 20; // Fxxの最大値
    int len = 0;

    boolean shnflg;
    JSONArray dataArrayShn = new JSONArray();
    for (int j = 0; j < 2; j++) {
      if (j == 0) {
        dataArrayShn = dataArrayG;
        shnflg = true;
      } else {
        dataArrayShn = dataArrayG2;
        shnflg = false;
      }
      len = dataArrayShn.size();
      // int delRow = 0;
      for (int i = 0; i < len; i++) {
        JSONObject dataG = dataArrayShn.getJSONObject(i);
        if (!dataG.isEmpty() && !dataG.optString("F1").equals("1")) {
          for (int k = 1; k <= maxField; k++) {
            String key = "F" + String.valueOf(k);

            if (k == 1) {

              // 催しコードを追加
              values += " ?, ?, ?,";
              prmData.add(moyskbn);
              prmData.add(moysstdt);
              prmData.add(moysrban);
            }


            data.optString(key);
            if (key.equals("F5")) {
              values += "?,";
              prmData.add(stno);
            }
            if (k == maxField) {
              values += " ?,";
              values += " ?,";
              values += " ?,";
              if (shnflg) {
                prmData.add("1");
              } else {
                prmData.add("2");
              }

              prmData.add(String.valueOf(i + 1));
              prmData.add(dataG.optString("F1"));


              values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
              values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + " ";
              values += ", '" + userId + "'";
              values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
              values += "FROM INATK.TOKMM_SHO WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt + " AND MOYSRBAN = " + moysrban + " AND STNO = " + stno + " ";
              if (shnflg) {
                values += "AND WARIGP = 1";
              } else {
                values += "AND WARIGP = 2";
              }
              values += " AND KANRINO =" + String.valueOf(i + 1) + " LIMIT 1 ) T2 ) ";
              values += ", CURRENT_TIMESTAMP";



              valueData = ArrayUtils.add(valueData, "(" + values + ")");
              values = "";
            }
          }

          if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
            sbSQL = new StringBuffer();
            sbSQL.append("REPLACE INTO INATK.TOKMM_SHO  ( ");
            sbSQL.append(" MOYSKBN"); // 催し区分
            sbSQL.append(",MOYSSTDT"); // 催し開始日
            sbSQL.append(",MOYSRBAN"); // 催し連番
            sbSQL.append(",STNO"); // セット番号
            sbSQL.append(",WARIGP"); // セット番号
            sbSQL.append(",KANRINO"); // 管理番号
            sbSQL.append(",SHNCD"); // 商品コード
            sbSQL.append(",UPDKBN"); // 更新区分：
            sbSQL.append(",SENDFLG");// 送信区分：
            sbSQL.append(",OPERATOR "); // オペレーター：
            sbSQL.append(",ADDDT "); // 登録日：
            sbSQL.append(",UPDDT "); // 更新日：
            sbSQL.append(") VALUES");
            sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");


            if (DefineReport.ID_DEBUG_MODE)
              System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

            sqlList.add(sbSQL.toString());
            prmList.add(prmData);
            lblList.add("セット販売_商品");

            // クリア
            prmData = new ArrayList<>();
            valueData = new Object[] {};
            values = "";
          }

        }
      }
    }


    return sbSQL.toString();
  }

  /**
   * BM催し送信INSERT/UPDATE処理
   *
   * @param data
   * @param datashn
   * @param datatj
   * @param userInfo
   */
  public JSONObject createSqlBmCsv(JSONObject data, JSONArray datashn, JSONObject datatj, User userInfo) {
    JSONObject result = new JSONObject();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    JSONObject getData = new JSONObject();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    Object[] valueData = new Object[] {};
    Object[] valueDataHb = new Object[] {};
    String values = "";
    String valuesHb = "";
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    ArrayList<String> prmData = new ArrayList<>();
    ArrayList<String> prmDataHb = new ArrayList<>();

    // 入力値格納用変数
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String bmnoShn = "";
    String kanrino = "";
    String tenAtuk_Arr = "";
    String moyCd_Arr = "";
    String kanriNo_Arr = "";
    String bmnNo_Arr = "";
    String hbShn = "";
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();

    if (data.optString(TOKMM_KKKLayout.MOYSCD.getId()).length() >= 8) {
      moyskbn = data.optString(TOKMM_KKKLayout.MOYSCD.getId()).substring(0, 1);
      moysstdt = data.optString(TOKMM_KKKLayout.MOYSCD.getId()).substring(1, 7);
      moysrban = data.optString(TOKMM_KKKLayout.MOYSCD.getId()).substring(7);
    }

    // BM番号を取得

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
      sqlWhere += "MOYSRBAN=null ";
    } else {
      sqlWhere += "MOYSRBAN=? ";
      paramData.add(moysrban);
    }

    // 一覧表情報
    sbSQL.append("SELECT ");
    sbSQL.append("MAX(SUMI_BMNO) + 1 AS SUMI_BMNO ");
    sbSQL.append(",MAX(SUMI_BMNO) AS BMNO ");
    sbSQL.append("FROM INATK.SYSBMCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() > 0) {
      getData = dbDatas.getJSONObject(0);
      if (StringUtils.isEmpty(getData.optString("SUMI_BMNO"))) {
        stno = String.valueOf(1);
        bmnoShn = String.valueOf(1);
      } else {
        stno = getData.optString("SUMI_BMNO");
        bmnoShn = getData.optString("BMNO");
      }
    }

    // 初期化
    sbSQL = new StringBuffer();
    sqlWhere = "";
    paramData = new ArrayList<>();

    // 番号の更新
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.SYSBMCD  ( ");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",SUMI_BMNO"); // 付番済BM番号
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(" )VALUES (?,?,?,?,");
    sbSQL.append("'" + userId + "'"); // F1 : 催し区分
    sbSQL.append(",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.SYSBM_KANRINO WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt
        + " AND MOYSRBAN = " + moysrban + " LIMIT 1 ) T2 ) "); // 登録日：
    sbSQL.append(",CURRENT_TIMESTAMP)"); // F1 : 催し連番



    // 催し区分
    paramData.add(moyskbn);
    // 催し開始日
    paramData.add(moysstdt);
    // 催し連番
    paramData.add(moysrban);
    // 付番済BM番号
    paramData.add(stno);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("BM番号内部管理");

    // 店扱いフラグ配列作成
    int addCnt = 1;
    int delCnt = 11;

    for (int i = 0; i < 10; i++) {
      tenCdAdds.add(i, datatj.optString("F" + addCnt));
      tenCdDels.add(i, datatj.optString("F" + delCnt));
      addCnt++;
      delCnt++;
    }

    // 対象店を取得
    Set<Integer> tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(data.optString(TOKMM_KKKLayout.BMNCD_RANK.getId()) // 部門コード
        , moyskbn // 催し区分
        , moysstdt // 催し開始日
        , moysrban // 催し連番
        , data.optString(TOKMM_KKKLayout.RANKNO_ADD.getId()) // 対象ランク№
        , data.optString(TOKMM_KKKLayout.RANKNO_DEL.getId()) // 除外ランク№
        , tenCdAdds // 対象店
        , tenCdDels // 除外店
    );

    Iterator<Integer> ten = tencds.iterator();
    int min = 0;
    int max = 0;
    for (int i = 0; i < tencds.size(); i++) {
      min = max;
      max = ten.next();
      for (int j = min; j < max; j++) {
        if (j + 1 == max) {
          tenAtuk_Arr += "1";
        } else {
          tenAtuk_Arr += " ";
        }
      }
    }

    // 初期化
    sbSQL = new StringBuffer();
    sqlWhere = "";
    paramData = new ArrayList<>();

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
      sqlWhere += "MOYSRBAN=null ";
    } else {
      sqlWhere += "MOYSRBAN=? ";
      paramData.add(moysrban);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("HBSTDT "); // PLU配信済みフラグ
    sbSQL.append(",HBEDDT "); // PLU配信済みフラグ
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    String hbStDt = "";
    String hbEdDt = "";
    if (dbDatas.size() != 0) {
      hbStDt = dbDatas.getJSONObject(0).optString("HBSTDT");
      hbEdDt = dbDatas.getJSONObject(0).optString("HBEDDT");
    }

    int maxField = 19; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (!ArrayUtils.contains(new String[] {"F1"}, key)) {
        String val = data.optString(key);
        if (key.equals(TOKMM_KKKLayout.STNO.getId())) {
          values += " ?,";
          prmData.add(stno);
        } else if (key.equals("F19")) {
          values += " ?,";
          prmData.add(tenAtuk_Arr);

          // 販売期間Fromが空の場合は催し期間Fromを使用
        } else if (key.equals(TOKMM_KKKLayout.HBSTDT.getId())) {
          values += " ?,";
          if (StringUtils.isEmpty(val)) {
            prmData.add(hbStDt);
          } else {
            prmData.add(val);
          }

          // 販売期間Toが空の場合は催し期間Toを使用
        } else if (key.equals(TOKMM_KKKLayout.HBEDDT.getId())) {
          values += "?,";
          if (StringUtils.isEmpty(val)) {
            prmData.add(hbEdDt);
          } else {
            prmData.add(val);
          }
        } else if (StringUtils.isEmpty(val)) {
          values += " null,";
        } else {
          values += " ?,";
          prmData.add(val);
        }
      }

      if (k == maxField) {
        values += " " + DefineReport.ValUpdkbn.NML.getVal() + "";
        values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + "";
        values += ", '" + userId + "'";
        values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values += "FROM INATK.TOKMM_SHO WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt + " AND MOYSRBAN = " + moysrban + " AND BMNNO = " + stno + " ";
        values += ", CURRENT_TIMESTAMP";
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }

    sbSQL = new StringBuffer();

    sbSQL.append("REPLACE INTO INATK.TOKBM  ( ");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",BMNNO"); // BM番号
    sbSQL.append(",BMNMAN"); // BM名称（カナ）
    sbSQL.append(",BMNMKN"); // BM名称（漢字）
    sbSQL.append(",BAIKAAM"); // 1個売価
    sbSQL.append(",BMTYP"); // BMタイプ
    sbSQL.append(",BD_KOSU1"); // バンドル個数1
    sbSQL.append(",BD_BAIKAAN1"); // バンドル売価1
    sbSQL.append(",BD_KOSU2"); // バンドル個数2
    sbSQL.append(",BD_BAIKAAN2"); // バンドル売価2
    sbSQL.append(",HBSTDT"); // 販売開始日
    sbSQL.append(",HBEDDT"); // 販売終了日
    sbSQL.append(",BMNCD_RANK"); // ランク用部門コード
    sbSQL.append(",RANKNO_ADD"); // 対象ランク
    sbSQL.append(",RANKNO_DEL"); // 除外ランク
    sbSQL.append(",TENATSUK_ARR"); // 店扱いフラグ配列
    sbSQL.append(",UPDKBN"); // 更新区分：
    sbSQL.append(",SENDFLG");// 送信区分：
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES");
    sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");



    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("BM催し送信");

    // 対象除外店を一度全て削除
    sqlWhere = "";
    paramData = new ArrayList<>();

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

    // BM番号
    if (StringUtils.isEmpty(stno)) {
      sqlWhere += "BMNNO=null ";
    } else {
      sqlWhere += "BMNNO=? ";
      paramData.add(stno);
    }


    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.TOKBM_TJTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("BM催し送信_対象除外店");

    // クリア
    prmData = new ArrayList<>();
    values = "";

    // 入力された対象店・除外店のvaluesを作成
    for (int i = 0; i < 10; i++) {
      if (!StringUtils.isEmpty(tenCdAdds.getString(i))) {
        if (!values.equals("")) {
          values += ",";
        }
        values += "(1,?,?,?,?,?," + DefineReport.Values.SENDFLG_UN.getVal() + ",'" + userId + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
        prmData.add(tenCdAdds.getString(i));
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(stno);
      }
      if (!StringUtils.isEmpty(tenCdDels.getString(i))) {
        if (!values.equals("")) {
          values += ",";
        }
        values += "(0,?,?,?,?,?," + DefineReport.Values.SENDFLG_UN.getVal() + ",'" + userId + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
        prmData.add(tenCdDels.getString(i));
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(stno);
      }
    }

    // 対象店・除外店に入力があった場合Insertを実行
    if (!StringUtils.isEmpty(values)) {
      sbSQL = new StringBuffer();
      sbSQL.append("REPLACE INTO INATK.TOKBM_TJTEN ( ");
      sbSQL.append("TJFLG");
      sbSQL.append(",TENCD");
      sbSQL.append(",MOYSKBN");
      sbSQL.append(",MOYSSTDT");
      sbSQL.append(",MOYSRBAN");
      sbSQL.append(",BMNNO");
      sbSQL.append(",SENDFLG");
      sbSQL.append(",OPERATOR");
      sbSQL.append(",ADDDT");
      sbSQL.append(",UPDDT");
      sbSQL.append(")VALUES" + values + " ");


      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("BM催し送信_対象除外店");
    }
    // クリア
    prmData = new ArrayList<>();
    prmDataHb = new ArrayList<>();
    valueData = new Object[] {};
    valueDataHb = new Object[] {};
    values = "";

    maxField = 4; // Fxxの最大値
    int len = datashn.size();
    int delRow = 0;
    for (int i = 0; i < len; i++) {
      JSONObject dataG = datashn.getJSONObject(i);
      for (int k = 1; k <= maxField; k++) {
        String key = "F" + String.valueOf(k);

        if (k == 1) {
          values += "? ,? ,?,"; // 催しコードをパラメーターに入れる
          prmData.add(moyskbn);
          prmData.add(moysstdt);
          prmData.add(moysrban);
        }

        int days = 0;
        hbStDt = "";

        if (!ArrayUtils.contains(new String[] {}, key)) {
          String val = dataG.optString(key);

          // 新規の場合管理番号を取得
          if (StringUtils.isEmpty(kanrino)) {
            kanrino = "0";
          }

          if (key.equals("F3")) {
            values += "?,";
            prmData.add(stno);
          } else if (key.equals("F4")) {
            kanrino = String.valueOf(Integer.valueOf(kanrino) + 1);
            values += "?,";
            prmData.add(kanrino);
          } else if (StringUtils.isEmpty(val)) {
            values += " null,";
          } else {
            values += " ?,";
            prmData.add(val);
          }

          if (k == maxField) {
            values += " " + DefineReport.Values.SENDFLG_UN.getVal() + " ";// 送信区分：
            values += ", '" + userId + "'  "; // オペレーター：
            values += ",  (SELECT * FROM (SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHN WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt
                + " AND MOYSRBAN = " + moysrban + " AND BMNNO = " + stno + " AND KANRINO = " + prmData.get(((i + 1) * 7) - 1) + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
            values += ", CURRENT_TIMESTAMP  "; // 更新日：
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }

          // BM商品販売日更新用
          if (key.equals(TOKMMSHOLayout.SHNCD.getId())) {
            hbShn = val;
          }

          // BM商品販売日用配列作成
          if (key.equals("F4")) {
            if (!StringUtils.isEmpty(data.optString(TOKMM_KKKLayout.HBSTDT.getId())) && !StringUtils.isEmpty(data.optString(TOKMM_KKKLayout.HBEDDT.getId()))) {
              hbStDt = data.optString(TOKMM_KKKLayout.HBSTDT.getId());
              days = getHbDays(hbStDt, data.optString(TOKMM_KKKLayout.HBEDDT.getId()));
            } else {
              days = getHbDays(hbStDt, hbEdDt);
            }
          } else {
            days = -1;
          }

          String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

          for (int amount = 0; amount <= days; amount++) {

            String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
            HashMap<String, String> moyCdArrMap = getArrMap(moyskbn, dataG.optString(TOKMMSHOLayout.SHNCD.getId()), hbDt, "0");
            HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString(TOKMMSHOLayout.SHNCD.getId()), hbDt, "1");
            HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString(TOKMMSHOLayout.SHNCD.getId()), hbDt, "2");

            ten = tencds.iterator();

            for (int j = 0; j < tencds.size(); j++) {

              String tenCd = String.valueOf(ten.next());

              // 催しコードを追加
              if (!moyCdArrMap.containsKey(tenCd)) {
                moyCdArrMap.put(tenCd, moyscd);
              }

              // BM番号を追加
              if (!bmnNoArrMap.containsKey(tenCd)) {
                String space = "";
                for (int digit = 0; digit < (3 - stno.length()); digit++) {
                  space += " ";
                }
                bmnNoArrMap.put(tenCd, space + stno);
              }

              // 管理番号を追加
              if (!kanriNoArrMap.containsKey(tenCd)) {

                String no = "";
                // 管理番号はその都度作成する
                no = kanrino;

                String space = "";
                for (int digit = 0; digit < (4 - no.length()); digit++) {
                  space += " ";
                }
                kanriNoArrMap.put(tenCd, space + no);
              }
            }

            // BM番号の入力があれば更新、なければ新規
            kanriNo_Arr = "";
            moyCd_Arr = "";
            bmnNo_Arr = "";

            int tenCd = 1;
            int cnt = 0;
            while (moyCdArrMap.size() != cnt) {

              if (moyCdArrMap.containsKey(String.valueOf(tenCd))) {
                cnt++;
                moyCd_Arr += moyCdArrMap.get(String.valueOf(tenCd));
              } else {
                moyCd_Arr += "          ";
              }
              tenCd++;
            }

            tenCd = 1;
            cnt = 0;
            while (bmnNoArrMap.size() != cnt) {

              if (bmnNoArrMap.containsKey(String.valueOf(tenCd))) {
                cnt++;
                bmnNo_Arr += bmnNoArrMap.get(String.valueOf(tenCd));
              } else {
                bmnNo_Arr += "   ";
              }
              tenCd++;
            }

            tenCd = 1;
            cnt = 0;
            while (kanriNoArrMap.size() != cnt) {

              if (kanriNoArrMap.containsKey(String.valueOf(tenCd))) {
                cnt++;
                kanriNo_Arr += kanriNoArrMap.get(String.valueOf(tenCd));
              } else {
                kanriNo_Arr += "    ";
              }
              tenCd++;
            }

            // 販売日、催しコード配列、管理番号配列、BM番号配列
            valuesHb += String.valueOf(amount + 1);
            if (StringUtils.isEmpty(hbShn)) {
              valuesHb += ", null";
            } else {
              valuesHb += ", ?";
            }
            valuesHb += ", ?, ?, ?, ?";
            if (!StringUtils.isEmpty(hbShn)) {
              prmDataHb.add(hbShn);
            }
            prmDataHb.add(CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount)));
            prmDataHb.add(moyCd_Arr);
            prmDataHb.add(bmnNo_Arr);
            prmDataHb.add(kanriNo_Arr);
            valuesHb += " '" + userId + "'  "; // オペレーター：

            if (!moyskbn.equals("3")) {
              valuesHb += ",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHNHBDT WHERE SHNCD = "
                  + datashn.optJSONObject(i).optString("F2") + " AND HBDT = " + hbDt + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
              // 催し区分=3ならBM商品販売日_本部個特
            } else {
              valuesHb += ",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHNHBDT_HTK WHERE SHNCD = "
                  + datashn.optJSONObject(i).optString("F2") + " AND HBDT = " + hbDt + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
            }

            valuesHb += ", CURRENT_TIMESTAMP  "; // 更新日：
            valueDataHb = ArrayUtils.add(valueDataHb, "(" + valuesHb + ")");
            valuesHb = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        sbSQL.append("REPLACE INTO INATK.TOKBM_SHN  ( ");
        sbSQL.append(" MOYSKBN"); // 催し区分
        sbSQL.append(",MOYSSTDT"); // 催し開始日
        sbSQL.append(",MOYSRBAN"); // 催し連番
        sbSQL.append(",SHNCD"); // 商品コード
        sbSQL.append(",GENKAAM"); // 原価
        sbSQL.append(",BMNNO"); // BM番号
        sbSQL.append(",KANRINO"); // 管理番号
        sbSQL.append(", SENDFLG");// 送信区分：
        sbSQL.append(",  OPERATOR "); // オペレーター：
        sbSQL.append(",  ADDDT "); // 登録日：
        sbSQL.append(",  UPDDT "); // 更新日：
        sbSQL.append(") VALUES");
        sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");



        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("BM催し送信_商品");

        // クリア
        prmData = new ArrayList<>();
        valueData = new Object[] {};
        values = "";
      }

      if (valueDataHb.length >= 100 || (i + 1 == len && valueDataHb.length > 0)) {

        sbSQL = new StringBuffer();
        // 催し区分=1or2ならBM商品販売日
        if (!moyskbn.equals("3")) {
          sbSQL.append("REPLACE INTO INATK.TOKBM_SHNHBDT  ( ");
          // 催し区分=3ならBM商品販売日_本部個特
        } else {
          sbSQL.append("REPLACE INTO INATK.TOKBM_SHNHBDT_HTK  ( ");
        }
        sbSQL.append("SHNCD"); // 商品コード
        sbSQL.append(",HBDT"); // 販売日
        sbSQL.append(",MOYCD_ARR"); // 催しコード配列
        sbSQL.append(",KANRINO_ARR"); // 管理番号配列
        sbSQL.append(",BMNNO_ARR"); // BM番号配列
        sbSQL.append(", OPERATOR "); // オペレーター：
        sbSQL.append(", ADDDT "); // 登録日：
        sbSQL.append(",  UPDDT "); // 更新日：
        sbSQL.append(") VALUES");
        sbSQL.append("  " + StringUtils.join(valueDataHb, ",") + " ");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmDataHb);
        lblList.add("BM商品販売日");

        // クリア
        prmDataHb = new ArrayList<>();
        valueDataHb = new Object[] {};
        valuesHb = "";
      }
    }

    // 子テーブルが全て削除された場合親も論理削除
    if (delRow == len) {

      // 初期化
      sbSQL = new StringBuffer();
      sqlWhere = "";
      paramData = new ArrayList<>();

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

      // BM番号
      sqlWhere += "BMNNO=? ";
      paramData.add(stno);

      // 番号の更新
      sbSQL = new StringBuffer();
      sbSQL.append("UPDATE INATK.TOKBM ");
      sbSQL.append("SET ");
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());// 送信区分：
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
      sbSQL.append(" WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("BM催し送信");
    }

    if (!StringUtils.isEmpty(kanrino)) {
      // 初期化
      sbSQL = new StringBuffer();
      sqlWhere = "";
      paramData = new ArrayList<>();

      // 番号の更新
      sbSQL = new StringBuffer();
      sbSQL.append("REPLACE INTO INATK.SYSBM_KANRINO  ( ");
      sbSQL.append(" MOYSKBN"); // 催し区分
      sbSQL.append(",MOYSSTDT"); // 催し開始日
      sbSQL.append(",MOYSRBAN"); // 催し連番
      sbSQL.append(",BMNO"); // BM番号
      sbSQL.append(",SUMI_KANRINO"); // 付番済管理番号
      sbSQL.append(",OPERATOR "); // オペレーター：
      sbSQL.append(",ADDDT "); // 登録日：
      sbSQL.append(",UPDDT "); // 更新日：
      sbSQL.append(" )VALUES (?,?,?,?,? ");
      sbSQL.append(", '" + userId + "'  "); // オペレーター：
      sbSQL.append(",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.SYSBM_KANRINO WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = "
          + moysstdt + " AND MOYSRBAN = " + moysrban + " AND BMNO = " + stno + " LIMIT 1 ) T2 ) "); // 登録日：
      sbSQL.append(", CURRENT_TIMESTAMP  )"); // 更新日：

      // 催し区分
      paramData.add(moyskbn);
      // 催し開始日
      paramData.add(moysstdt);
      // 催し連番
      paramData.add(moysrban);
      // BM番号
      paramData.add(bmnoShn);
      // 付番済管理番号
      paramData.add(String.valueOf(kanrino));

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("BM管理番号内部管理");
    }

    return result;
  }

  /**
   * BM催し送信情報DELETE(論理削除)処理
   *
   * @param data
   * @param dataArrayG
   * @param userInfo
   */
  public String createDelSqlHs(JSONObject data, JSONArray dataArrayG, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";

    stno = data.optString("F5");


    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere = "MOYSKBN=null AND ";
    } else {
      sqlWhere = "MOYSKBN=? AND ";
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


    sqlWhere += "STNO=? ";
    paramData.add(stno);

    // BM催し送信DELETE(論理削除)
    sbSQL.append("UPDATE INATK.TOKMM_KKK ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());// 送信区分：
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("セット販売_企画");

    sbSQL = new StringBuffer();

    sbSQL.append("UPDATE INATK.TOKMM_SHO ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());// 送信区分：
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("セット販売_商品");

    sbSQL = new StringBuffer();

    sbSQL.append("DELETE FROM INATK.TOKMM_ADTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("セット販売_対象除外店");


    return sbSQL.toString();
  }

  /**
   * SEQ情報取得処理(No)
   *
   * @throws Exception
   */
  public String getInput_SEQ() {
    new ItemList();
    String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ001";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    // DB検索用パラメータ
    String szMoysKbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysStDt = getMap().get("MOYSSTDT"); // 催し販売開始日
    String szMoysRban = getMap().get("MOYSRBAN"); // 催し連番
    String szStno = getMap().get("STNO"); // セット番号

    ArrayList<String> paramData = new ArrayList<>();
    String sqlWhere = "";
    String sqlTjTen = "";
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    JSONObject data = new JSONObject();
    int tenAddCnt = 0;
    int tenDelCnt = 0;

    if (StringUtils.isEmpty(szMoysKbn)) {
      sqlWhere += "TK.MOYSKBN=null AND ";
    } else {
      sqlWhere += "TK.MOYSKBN=? AND ";
      paramData.add(szMoysKbn);
    }

    if (StringUtils.isEmpty(szMoysStDt)) {
      sqlWhere += "TK.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "TK.MOYSSTDT=? AND ";
      paramData.add(szMoysStDt);
    }

    if (StringUtils.isEmpty(szMoysRban)) {
      sqlWhere += "TK.MOYSRBAN=null AND ";
    } else {
      sqlWhere += "TK.MOYSRBAN=? AND ";
      paramData.add(szMoysRban);
    }

    if (StringUtils.isEmpty(szStno)) {
      sqlWhere += "TK.STNO=null AND ";
    } else {
      sqlWhere += "TK.STNO=? AND ";
      paramData.add(szStno);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("TK.ADDDELFLG ");
    sbSQL.append(",TK.TENCD ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMM_ADTEN TK ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append(" 1=1 ORDER BY TK.ADDDELFLG,TK.TENCD");

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    for (int i = 0; i < dbDatas.size(); i++) {
      data = dbDatas.getJSONObject(i);

      // 除外店
      if (data.optString("ADDDELFLG").equals("0")) {

        tenDelCnt++;
        sqlTjTen += "," + data.optString("TENCD") + " AS TENCD_DEL_" + tenDelCnt;

        // 対象店
      } else {

        // 除外店を10個出力する
        if (tenDelCnt < 10) {

          for (int j = tenDelCnt; j < 10; j++) {

            tenDelCnt++;
            sqlTjTen += ",'' AS TENCD_DEL_" + (tenDelCnt + 1);
          }

        }

        tenAddCnt++;
        sqlTjTen += "," + data.optString("TENCD") + " AS TENCD_ADD_" + tenAddCnt;
      }
    }

    if (tenDelCnt <= 10) {
      for (int i = tenDelCnt; i < 10; i++) {

        sqlTjTen += ",'' AS TENCD_DEL_" + (i + 1);
      }
    }

    if (tenAddCnt <= 10) {
      for (int i = tenAddCnt; i < 10; i++) {

        sqlTjTen += ",'' AS TENCD_ADD_" + (i + 1);
      }
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    paramData = new ArrayList<>();
    sqlWhere = "";

    // 前画面から引き継いだ配送グループコードで検索
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append(" CD.MOYSKBN||CD.MOYSSTDT||right('000'||CD.MOYSRBAN, 3) AS MOYSCD "); // F1 : 催しコード
    sbSQL.append(",CD.MOYKN "); // F2 : 催し名称
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(CD.HBSTDT,'%Y/%m/%d'),'%y/%m/%d') MOYSSTDT "); // F3 : 催し開始日
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(CD.HBEDDT,'%Y/%m/%d'),'%y/%m/%d') MOYSEDDT "); // F4 : 催し終了日
    sbSQL.append(",TK.STNO "); // F5 : B/M番号
    sbSQL.append(",TK.STNM "); // F6 : B/M名称（ｶﾅ）
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(TK.HBSTDT, '%Y/%m/%d'), '%y/%m/%d') HBSTDT "); // F7 : B/Mタイプ
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(TK.HBEDDT, '%Y/%m/%d'), '%y/%m/%d') HBEDDT "); // F8 : B/M名称（漢字）
    sbSQL.append(",TK.ESTGK "); // F9 : 1個売り総売価
    sbSQL.append(",TK.BMNCD_RANK "); // F10 : 個数総売価（１）
    sbSQL.append(",TK.RANKNO_ADD "); // F11 : 金額
    sbSQL.append(",TK.RANKNO_DEL "); // F12 : 個数総売価（２）
    sbSQL.append(",TK.MOYSKBN "); // F13 : 金額
    sbSQL.append(",TK.MOYSSTDT "); // F14 : 販売開始日
    sbSQL.append(",TK.MOYSRBAN "); // F15 : 販売終了日
    sbSQL.append(",TK.OPERATOR "); // F16 : 部門コード
    sbSQL.append(",DATE_FORMAT(TK.ADDDT, '%y/%m/%d') AS ADDDT "); // F17 : 対象店ランク№
    sbSQL.append(",DATE_FORMAT(TK.UPDDT, '%y/%m/%d') AS UPDDT "); // F18 : 除外店ランク№
    sbSQL.append(",DATE_FORMAT(TK.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F19 : 催し区分
    sbSQL.append(sqlTjTen); // F20～F39 : 対象店・除外店
    sbSQL.append(",TK.HBSTDT AS HDN_HBSTDT "); // F40 : 元販売開始日
    sbSQL.append(",TK.HBEDDT AS HND_HBEDDT "); // F41 : 元販売終了日
    sbSQL.append(" FROM ");
    sbSQL.append("INATK.TOKMOYCD CD LEFT JOIN INATK.TOKMM_KKK TK ON ");
    sbSQL.append("TK.MOYSKBN = CD.MOYSKBN AND ");
    sbSQL.append("TK.MOYSSTDT = CD.MOYSSTDT AND ");
    sbSQL.append("TK.MOYSRBAN = CD.MOYSRBAN AND ");
    if (StringUtils.isEmpty(szStno)) {
      sbSQL.append("TK.STNO=null ");
    } else {
      sbSQL.append("TK.STNO=? ");
      paramData.add(szStno);
    }

    sbSQL.append("WHERE ");

    if (StringUtils.isEmpty(szMoysKbn)) {
      sqlWhere += "CD.MOYSKBN=null AND ";
    } else {
      sqlWhere += "CD.MOYSKBN=? AND ";
      paramData.add(szMoysKbn);
    }

    if (StringUtils.isEmpty(szMoysStDt)) {
      sqlWhere += "CD.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "CD.MOYSSTDT=? AND ";
      paramData.add(szMoysStDt);
    }

    if (StringUtils.isEmpty(szMoysRban)) {
      sqlWhere += "CD.MOYSRBAN=null ";
    } else {
      sqlWhere += "CD.MOYSRBAN=? ";
      paramData.add(szMoysRban);
    }
    sbSQL.append(sqlWhere);

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

  /**
   * 催しコード配列をkye,valueの形で返却(key:店、value:催しコード)
   *
   * @param moysKbn 催し区分
   * @param shnCd 商品コード
   * @param hbDt 販売日
   * @param getFlg 0:催しコード配列、1:BM番号配列 2:管理番号配列
   * @return
   */
  public HashMap<String, String> getArrMap(String moysKbn, String shnCd, String hbDt, String getFlg) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKBM_SHNHBDT "; // BM商品販売日
    String sqlSelect = "MOYCD_ARR AS ARR ";
    ArrayList<String> paramData = new ArrayList<>();

    String arr = "";
    HashMap<String, String> arrMap = new HashMap<>();
    int st = 0;
    int ed = 0;
    int digit = 10;

    // 商品コード
    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(shnCd);
    }

    // 販売開始日～終了日
    if (StringUtils.isEmpty(hbDt)) {
      sqlWhere += "HBDT=null ";
    } else {
      sqlWhere += "HBDT=? ";
      paramData.add(hbDt);
    }

    // BM商品重複チェック
    // BM商品販売日_本部個特
    if (!StringUtils.isEmpty(moysKbn) && moysKbn.equals("3")) {
      sqlFrom = "INATK.TOKBM_SHNHBDT_HTK ";
    }

    if (getFlg.equals("1")) {
      sqlSelect = "BMNNO_ARR AS ARR ";
    } else if (getFlg.equals("2")) {
      sqlSelect = "KANRINO_ARR AS ARR ";
    }

    sbSQL.append("SELECT ");
    sbSQL.append(sqlSelect); // 配列
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コード、販売開始、終了日で検索

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // データが存在する場合、配列の展開を実施
    if (dbDatas.size() != 0) {

      arr = dbDatas.getJSONObject(0).optString("ARR");

      if (getFlg.equals("1")) {
        digit = 3;
      } else if (getFlg.equals("2")) {
        digit = 4;
      }

      // 配列をdigitで指定された桁ずつで区切りkey、valueを保持(key=店舗、value=取得結果)
      for (int i = 0; i < (arr.length() / digit); i++) {

        ed += digit;

        // valueがスペースのものは登録しない
        if (!StringUtils.isEmpty(arr.substring(st, ed).trim())) {
          arrMap.put(String.valueOf(i + 1), arr.substring(st, ed));
        }

        st += digit;
      }
    }

    return arrMap;
  }

  /**
   * 販売終了日-販売開始日を取得
   *
   * @param hbStDt 販売開始日
   * @param hbEdDt 販売終了日
   * @return hbEdDt-hbStDt
   */
  public int getHbDays(String hbStDt, String hbEdDt) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<>();

    sbSQL.append("SELECT DATEDIFF( DATE_FORMAT(? ,'%Y%m%d') ,DATE_FORMAT(? ,'%Y%m%d')) AS DAYS");
    paramData.add(hbEdDt);
    paramData.add(hbStDt);

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() > 0) {
      return dbDatas.getJSONObject(0).optInt("DAYS");
    }
    return 0;
  }

  /** セット販売企画 */
  public enum TOKMM_KKKLayout implements MSTLayout {

    /** 催しコード */
    MOYSCD(1, "MOYSCD", "VARCHAR(10)", "催しコード"),
    /** 催し区分 */
    MOYSKBN(2, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(3, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(4, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** BM番号 */
    STNO(5, "STNO", "SMALLINT", "セット番号"),
    /** 催し名称（ｶﾅ） */
    BMNMAN(6, "BMNMAN", "VARCHAR(20)", "BM名称（ｶﾅ）"),
    /** 催し名称（漢字） */
    BMNMKN(7, "BMNMKN", "VARCHAR(20)", "BM名称（漢字）"),
    /** 1個売価 */
    BAIKAAM(8, "BAIKAAM", "INTEGER", "1個売価"),
    /** BMタイプ */
    BMTYP(9, "BMTYP", "SMALLINT", "BMタイプ"),
    /** バンドル個数1 */
    BD_KOSU1(10, "BD_KOSU1", "SMALLINT", "バンドル個数1"),
    /** バンドル売価1 */
    BD_BAIKAAN1(11, "BD_BAIKAAN1", "INTEGER", "バンドル売価1"),
    /** バンドル個数2 */
    BD_KOSU2(12, "BD_KOSU2", "SMALLINT", "バンドル個数2"),
    /** バンドル売価2 */
    BD_BAIKAAN2(13, "BD_BAIKAAN2", "INTEGER", "バンドル売価2"),
    /** 販売開始日 */
    HBSTDT(14, "HBSTDT", "INTEGER", "販売開始日"),
    /** 販売終了日 */
    HBEDDT(15, "HBEDDT", "INTEGER", "販売終了日"),
    /** ランク用部門 */
    BMNCD_RANK(16, "BMNCD_RANK", "SMALLINT", "ランク用部門"),
    /** 対象店ランク */
    RANKNO_ADD(17, "RANKNO_ADD", "SMALLINT", "対象ランク"),
    /** 除外店ランク */
    RANKNO_DEL(18, "RANKNO_DEL", "SMALLINT", "除外ランク");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKMM_KKKLayout(Integer no, String col, String typ, String txt) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.txt = txt;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** セット販売_対象除外店 */
  public enum TOKMMTJTENLayout implements MSTLayout {

    /** 店コード */
    TENCD_ADD1(1, "TENCD_ADD1", "SMALLINT", "追加店コード1"),
    /** 店コード */
    TENCD_ADD2(2, "TENCD_ADD2", "SMALLINT", "追加店コード2"),
    /** 店コード */
    TENCD_ADD3(3, "TENCD_ADD3", "SMALLINT", "追加店コード3"),
    /** 店コード */
    TENCD_ADD4(4, "TENCD_ADD4", "SMALLINT", "追加店コード4"),
    /** 店コード */
    TENCD_ADD5(5, "TENCD_ADD5", "SMALLINT", "追加店コード5"),
    /** 店コード */
    TENCD_ADD6(6, "TENCD_ADD6", "SMALLINT", "追加店コード6"),
    /** 店コード */
    TENCD_ADD7(7, "TENCD_ADD7", "SMALLINT", "追加店コード7"),
    /** 店コード */
    TENCD_ADD8(8, "TENCD_ADD8", "SMALLINT", "追加店コード8"),
    /** 店コード */
    TENCD_ADD9(9, "TENCD_ADD9", "SMALLINT", "追加店コード9"),
    /** 店コード */
    TENCD_ADD10(10, "TENCD_ADD10", "SMALLINT", "追加店コード10"),
    /** 店コード */
    TENCD_DEL1(11, "TENCD_DEL1", "SMALLINT", "除外店コード1"),
    /** 店コード */
    TENCD_DEL2(12, "TENCD_DEL2", "SMALLINT", "除外店コード2"),
    /** 店コード */
    TENCD_DEL3(13, "TENCD_DEL3", "SMALLINT", "除外店コード3"),
    /** 店コード */
    TENCD_DEL4(14, "TENCD_DEL4", "SMALLINT", "除外店コード4"),
    /** 店コード */
    TENCD_DEL5(15, "TENCD_DEL5", "SMALLINT", "除外店コード5"),
    /** 店コード */
    TENCD_DEL6(16, "TENCD_DEL6", "SMALLINT", "除外店コード6"),
    /** 店コード */
    TENCD_DEL7(17, "TENCD_DEL7", "SMALLINT", "除外店コード7"),
    /** 店コード */
    TENCD_DEL8(18, "TENCD_DEL8", "SMALLINT", "除外店コード8"),
    /** 店コード */
    TENCD_DEL9(19, "TENCD_DEL9", "SMALLINT", "除外店コード9"),
    /** 店コード */
    TENCD_DEL10(20, "TENCD_DEL10", "SMALLINT", "除外店コード10");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKMMTJTENLayout(Integer no, String col, String typ, String txt) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.txt = txt;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** BM催し送信_商品 */
  public enum TOKMMSHOLayout implements MSTLayout {

    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 原価 */
    GENKAAM(2, "GENKAAM", "DECIMAL(8,2)", "原価");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKMMSHOLayout(Integer no, String col, String typ, String txt) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.txt = txt;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

}
