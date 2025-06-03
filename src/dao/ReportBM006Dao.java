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
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.Reportx002Dao.MSTSHNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBM006Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /** BM番号保持 */
  String bmno = "";

  /** CSV取込トラン特殊情報保持用 */
  String[] csvtok_add_data = new String[CSVTOKLayout.values().length];

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBM006Dao(String JNDIname) {
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

    String tenAtuk_Arr = map.get("TENATSUK_ARR");

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    MessageUtility mu = new MessageUtility();
    JSONArray dbDatas = new JSONArray();
    ArrayList<JSONObject> shncds = new ArrayList<>();
    HashSet<String> shncds_ = new HashSet<>();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String rankNoAdd = "";
    String rankNoDel = "";
    String hbStDt = "";
    String hbEdDt = "";
    String pluFlg = "";
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
      msg.add(mu.getDbMessageObj("EX1047", new String[] {"商品コード"}));
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
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN = null AND ";
    } else {
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN = ? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT = null AND ";
    } else {
      sqlWhere += "MOYSSTDT = ? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN = null";
    } else {
      sqlWhere += "MOYSRBAN = ?";
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
      msg.add(mu.getDbMessageObj("E20005", new String[] {}));
      return msg;
    } else {
      // 催しコードのPLU配信済みフラグチェック
      if (dbDatas.getJSONObject(0).optString("PLUSFLG").equals("1")) {
        pluFlg = "1";
        if (StringUtils.isEmpty(data.optString("F6"))) {
          // PLU配信済みフラグ=1の場合はエラー
          msg.add(mu.getDbMessageObj("E20272", new String[] {}));
          return msg;
        }
      }

      // 販売開始日の入力をチェック
      if ((!StringUtils.isEmpty(data.optString("F15")) && StringUtils.isEmpty(data.optString("F16"))) || (!StringUtils.isEmpty(data.optString("F16")) && StringUtils.isEmpty(data.optString("F15")))) {

        // 販売期間Fromと販売期間Toの両方入力または両方未入力としてください。
        msg.add(mu.getDbMessageObj("E20297", new String[] {}));
        return msg;
      } else if (!StringUtils.isEmpty(data.optString("F15")) && !StringUtils.isEmpty(data.optString("F16"))) {

        if (!StringUtils.isEmpty(data.optString("F3")) && (data.optInt("F3") > data.optInt("F15"))) {

          // 販売期間(開始日) ≧ 催し期間(開始日)の条件で入力してください。
          msg.add(mu.getDbMessageObj("E20007", new String[] {}));
          return msg;
        } else if (!StringUtils.isEmpty(data.optString("F4")) && (data.optInt("F4") < data.optInt("F16"))) {

          // 販売期間(終了日) ≦ 催し期間(終了日)の条件で入力してください。
          msg.add(mu.getDbMessageObj("E20008", new String[] {}));
          return msg;
        } else if (data.optInt("F15") > data.optInt("F16")) {

          // 販売期間(開始日) ≦ 販売期間(終了日)の条件で入力してください。
          msg.add(mu.getDbMessageObj("E20006", new String[] {}));
          return msg;
        } else {
          hbStDt = data.optString("F15");
          hbEdDt = data.optString("F16");
        }
      } else {
        hbStDt = data.optString("F3");
        hbEdDt = data.optString("F4");
      }
    }

    // 1個売り総売価、個数総売価(1)に入力がある場合
    if (!StringUtils.isEmpty(data.optString("F10")) && !StringUtils.isEmpty(data.optString("F11")) && !StringUtils.isEmpty(data.optString("F12"))) {
      int baika = data.optInt("F10");
      int kosu1 = data.optInt("F11");
      int baika1 = data.optInt("F12");
      double ave1 = baika1 / kosu1;

      // (1)の平均売価(=円/個)<=1個売り総売価
      if (!(ave1 <= baika)) {
        // (1)の平均売価（＝円/個）が1個売り売価以上になっています。
        msg.add(mu.getDbMessageObj("E20089", new String[] {"(1)"}));
        return msg;
        // (1)総売価 > 1個売り総売価
      } else if (!(baika1 > baika)) {
        // (1)総売価 > 1個売り総売価を入力してください。
        msg.add(mu.getDbMessageObj("E30012", new String[] {"(1)総売価 > 1個売り総売価"}));
        return msg;
      }

      // 個数総売価(2)に入力がある場合
      if (!StringUtils.isEmpty(data.optString("F13")) && !StringUtils.isEmpty(data.optString("F14"))) {
        int kosu2 = data.optInt("F13");
        int baika2 = data.optInt("F14");
        double ave2 = baika2 / kosu2;

        // (2)の平均売価(=円/個)<=1個売り総売価
        if (!(ave2 <= baika)) {
          // （2）の平均売価＜（1）の平均売価の範囲で入力してください。
          msg.add(mu.getDbMessageObj("E20089", new String[] {"(2)"}));
          return msg;
          // (2)の平均売価(=円/個)<=(1)の平均売価(=円/個)
        } else if (!(ave2 <= ave1)) {
          // (2)の平均売価（＝円/個）が1個売り売価以上になっています。
          msg.add(mu.getDbMessageObj("E20090", new String[] {}));
          return msg;
          // (2)個数 > (1)個数
        } else if (!(kosu2 > kosu1)) {
          // （2）の個数＞（1）の個数の範囲で入力してください。
          msg.add(mu.getDbMessageObj("E20091", new String[] {}));
          return msg;
          // (2)総売価 > (1)総売価
        } else if (!(baika2 > baika1)) {
          // (2)総売価 > (1)総売価を入力してください。
          msg.add(mu.getDbMessageObj("E30012", new String[] {"(2)総売価 > (1)総売価"}));
          return msg;
        }
      }
    }

    // 対象店、除外店ランク№に入力がある場合、同一の値はエラー
    if (!StringUtils.isEmpty(data.optString("F18")) || !StringUtils.isEmpty(data.optString("F19"))) {
      rankNoAdd = data.optString("F18");
      rankNoDel = data.optString("F19");

      if (rankNoAdd.equals(rankNoDel)) {
        // 対象店・除外店ランク№が同一
        msg.add(mu.getDbMessageObj("E20016", new String[] {}));
        return msg;
      }
    }

    // 対象店、除外店ランク№に入力がある場合ランクマスタに存在してるかチェック
    if (StringUtils.isEmpty(tenAtuk_Arr)) {
      if (!StringUtils.isEmpty(rankNoAdd)) {
        // 変数を初期化
        sbSQL = new StringBuffer();
        new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<>();

        sqlFrom = "INATK.TOKRANK ";

        // 部門コードが入力されてたら
        if (!StringUtils.isEmpty(data.optString("F17"))) {
          sqlWhere += "BMNCD=? AND ";
          paramData.add(data.optString("F17"));
        }

        if (Integer.valueOf(rankNoAdd) >= 900) {

          // 催し区分
          if (StringUtils.isEmpty(moyskbn)) {
            sqlWhere += "MOYSKBN = null AND ";
          } else {
            sqlWhere += "MOYSKBN = ? AND ";
            paramData.add(moyskbn);
          }

          // 催し開始日
          if (StringUtils.isEmpty(moysstdt)) {
            sqlWhere += "MOYSSTDT = null AND ";
          } else {
            sqlWhere += "MOYSSTDT = ? AND ";
            paramData.add(moysstdt);
          }

          // 催し連番
          if (StringUtils.isEmpty(moysrban)) {
            sqlWhere += "MOYSRBAN = null AND ";
          } else {
            sqlWhere += "MOYSRBAN = ? AND ";
            paramData.add(moysrban);
          }

          sqlFrom = "INATK.TOKRANKEX ";
        }

        // ランクNo.
        sqlWhere += "RANKNO = ? AND ";
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
        if (!StringUtils.isEmpty(data.optString("F17"))) {
          sqlWhere += "BMNCD = ? AND ";
          paramData.add(data.optString("F17"));
        }

        if (Integer.valueOf(rankNoDel) >= 900) {

          // 催し区分
          if (StringUtils.isEmpty(moyskbn)) {
            sqlWhere += "MOYSKBN = null AND ";
          } else {
            sqlWhere += "MOYSKBN = ? AND ";
            paramData.add(moyskbn);
          }

          // 催し開始日
          if (StringUtils.isEmpty(moysstdt)) {
            sqlWhere += "MOYSSTDT = null AND ";
          } else {
            sqlWhere += "MOYSSTDT = ? AND ";
            paramData.add(moysstdt);
          }

          // 催し連番
          if (StringUtils.isEmpty(moysrban)) {
            sqlWhere += "MOYSRBAN = null AND ";
          } else {
            sqlWhere += "MOYSRBAN = ? AND ";
            paramData.add(moysrban);
          }

          sqlFrom = "INATK.TOKRANKEX ";
        }

        // ランクNo.
        sqlWhere += "RANKNO = ? AND ";
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
    }

    int addCnt = 37;
    int delCnt = 27;

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
    String checkTencd = "";
    ReportBM015Dao dao = new ReportBM015Dao(JNDIname);
    if (!StringUtils.isEmpty(tenAtuk_Arr)) {
      checkTencd = dao.checkTenCdAddArr(tenAtuk_Arr, tenCdAdds, tenCdDels);
    } else {
      checkTencd = dao.checkTenCdAdd(data.optString("F17") // 部門コード
          , moyskbn // 催し区分
          , moysstdt // 催し開始日
          , moysrban // 催し連番
          , rankNoAdd // 対象ランク№
          , rankNoDel // 除外ランク№
          , tenCdAdds // 対象店
          , tenCdDels // 除外店
      );
    }

    if (!StringUtils.isEmpty(checkTencd)) {
      msg.add(mu.getDbMessageObj(checkTencd, new String[] {}));
      return msg;
    }

    int delCntChk = 0;

    // 入力値の重複チェック（商品コード）、PLUFLG=1の場合削除は不可
    for (int i = 0; i < dataArrayG.size(); i++) {
      String val = dataArrayG.optJSONObject(i).optString("F2");
      if (StringUtils.isNotEmpty(val)) {
        shncds.add(dataArrayG.optJSONObject(i));
        shncds_.add(val);
      }

      if (dataArrayG.optJSONObject(i).optString("F1").equals("1")) {
        delCntChk++;
      }
    }

    // 削除不可エラー(全て消えてしまう場合)
    if (pluFlg.equals("1") && dataArrayG.size() == delCntChk) {
      // 催しの店舗配信済みのため、削除はできません。
      msg.add(mu.getDbMessageObj("E20510", new String[] {}));
      return msg;
    }

    if (shncds.size() != shncds_.size()) {
      msg.add(mu.getDbMessageObj("EX1022", new String[] {}));
      return msg;
    }

    // 商品チェック
    for (int i = 0; i < dataArrayG.size(); i++) {
      JSONObject dataG = new JSONObject();
      dataG = dataArrayG.getJSONObject(i);
      if (dataG.isEmpty()) {
        continue;
      }

      // 変数を初期化
      sbSQL = new StringBuffer();
      new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<>();

      if (StringUtils.isEmpty(dataG.optString("F2"))) {
        sqlWhere += "SHNCD=null";
      } else {
        sqlWhere += "SHNCD=?";
        paramData.add(dataG.optString("F2"));
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
        msg.add(mu.getDbMessageObj("E11046", new String[] {}));
        return msg;
      }

      // 対象店を取得
      Set<Integer> tencds = new TreeSet<>();
      if (!StringUtils.isEmpty(tenAtuk_Arr)) {
        tencds = dao.getTenCdAddArr(tenAtuk_Arr, tenCdAdds, tenCdDels);
      } else {
        tencds = dao.getTenCdAdd(data.optString("F17") // 部門コード
            , moyskbn // 催し区分
            , moysstdt // 催し開始日
            , moysrban // 催し連番
            , rankNoAdd // 対象ランク№
            , rankNoDel // 除外ランク№
            , tenCdAdds // 対象店
            , tenCdDels // 除外店
        );
      }

      // 販売期間の日数を取得
      int days = getHbDays(hbStDt, hbEdDt);
      String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

      for (int amount = 0; amount <= days; amount++) {

        String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
        HashMap<String, String> eachMoysCdArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "0");
        HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "1");
        HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "2");

        HashMap<String, String> moyCdArrMap = new HashMap<>();

        for (HashMap.Entry<String, String> getMoysCd : eachMoysCdArrMap.entrySet()) {
          String getTen = getMoysCd.getKey();
          String getMoys = getMoysCd.getValue();

          boolean remFlg = false;

          if (getMoys.equals(moyscd)) {
            // 催しが一致した場合さらにBM番号と管理番号も一致するかを確認
            if (bmnNoArrMap.containsKey(getTen) && bmnNoArrMap.get(getTen).trim().equals(bmno)) {
              if (!StringUtils.isEmpty(dataG.optString("F9"))) {
                if (kanriNoArrMap.containsKey(getTen) && kanriNoArrMap.get(getTen).trim().equals(dataG.optString("F9"))) {

                  bmnNoArrMap.remove(getTen);
                  kanriNoArrMap.remove(getTen);

                  remFlg = true;
                }
              }
            }
          }

          if (!remFlg && !moyCdArrMap.containsKey(getTen)) {
            moyCdArrMap.put(getTen, moyscd);
          }
        }

        // BM番号の入力があれば更新、なければ新規
        Iterator<Integer> ten = tencds.iterator();
        for (int j = 0; j < tencds.size(); j++) {
          String tenCd = String.valueOf(ten.next());
          if (!StringUtils.isEmpty(data.optString("F6"))) {
            if (moyCdArrMap.containsKey(tenCd) && bmnNoArrMap.containsKey(tenCd)) {

              String getMoysCd = moyCdArrMap.get(tenCd);
              String getBmnNo = bmnNoArrMap.get(tenCd).trim();

              if (!getMoysCd.equals(moyscd) || !getBmnNo.equals(data.optString("F6"))) {
                // 同一販売日の重複チェックエラー
                msg.add(mu.getDbMessageObj("E20449", new String[] {}));
                return msg;
              }
            }
          } else {
            if (moyCdArrMap.containsKey(tenCd)) {
              // 同一販売日の重複チェックエラー
              msg.add(mu.getDbMessageObj("E20449", new String[] {}));
              return msg;
            }
          }
        }
      }
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
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN = null AND ";
    } else {
      sqlWhere += "MOYSKBN IN(1,2,3) AND MOYSKBN = ? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT = null AND ";
    } else {
      sqlWhere += "MOYSSTDT = ? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN = null";
    } else {
      sqlWhere += "MOYSRBAN = ?";
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

  public List<JSONObject> checkData(MessageUtility mu, JSONArray dataArray, // BM催し送信
      JSONArray dataArrayShn, // BM催し送信_商品
      JSONArray dataArrayTj, // BM催し送信_対象除外店
      JSONObject dataOther // その他情報
  ) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    ArrayList<JSONObject> shncds = new ArrayList<>();
    HashSet<String> shncds_ = new HashSet<>();
    ArrayList<JSONObject> cTenCdAdds = new ArrayList<>();
    HashSet<String> tenCdAdds_ = new HashSet<>();
    ArrayList<JSONObject> cTenCdDels = new ArrayList<>();
    HashSet<String> tenCdDels_ = new HashSet<>();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String rankNoAdd = "";
    String rankNoDel = "";
    String hbStDt = "";
    String hbEdDt = "";
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();
    JSONObject tokBmInfo = new JSONObject();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<>();

    String JNDIname = Defines.STR_JNDI_DS;

    // 基本的には全て同じ情報になるため催しコードを頭で保存
    tokBmInfo = dataArray.optJSONObject(0);

    // 新規(正) 1.1 必須入力項目チェックを行う。
    // 変更(正) 1.1 必須入力項目チェックを行う。
    String errCd = "001";
    TOKBMLayout[] targetCol = null;
    targetCol = new TOKBMLayout[] {TOKBMLayout.MOYSCD, TOKBMLayout.MOYSSTDT, TOKBMLayout.MOYSRBAN, TOKBMLayout.BMNCD_RANK, TOKBMLayout.RANKNO_ADD};
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);
      for (TOKBMLayout colinf : targetCol) {

        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
          msg.add(o);
          return msg;
        }
      }
    }

    TOKBMSHNLayout[] targetColShn = null;
    targetColShn = new TOKBMSHNLayout[] {TOKBMSHNLayout.SHNCD};
    for (int i = 0; i < dataArrayShn.size(); i++) {
      JSONObject jo = dataArrayShn.optJSONObject(i);
      for (TOKBMSHNLayout colinf : targetColShn) {

        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
          msg.add(o);
          return msg;
        }
      }
    }

    errCd = "002";

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.BM催し送信
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);
      for (TOKBMLayout colinf : TOKBMLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            // エラー発生箇所を保存
            tokBmInfo = jo;
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            tokBmInfo = jo;
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
            msg.add(o);
            return msg;
          }

          // チェックしている項目が催しコードでかつ入力がない場合は必須チェックでひっかからない為ここでチェック
          if (colinf.getId().equals(TOKBMLayout.MOYSCD.getId()) && !StringUtils.isEmpty(val)) {
            if (!tokBmInfo.optString(colinf.getId()).equals(jo.optString(colinf.getId()))) {
              // エラー発生箇所を保存
              tokBmInfo = jo;
              JSONObject o = mu.getDbMessageObj("E40004", new String[] {});
              o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
              msg.add(o);
              return msg;
            }
          }
        }
      }
    }

    // 2.BM催し送信_商品
    for (int i = 0; i < dataArrayShn.size(); i++) {
      JSONObject jo = dataArrayShn.optJSONObject(i);
      for (TOKBMSHNLayout colinf : TOKBMSHNLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 3.BM催し送信_対象除外店
    for (int i = 0; i < dataArrayTj.size(); i++) {
      JSONObject jo = dataArrayTj.optJSONObject(i);
      for (TOKBMTJTENLayout colinf : TOKBMTJTENLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, val, colinf.getTxt(), String.valueOf(i + 1), jo, tokBmInfo);
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // チェック処理
    // 対象件数チェック
    errCd = "003";
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty() || dataArrayShn.size() == 0 || dataArrayShn.getJSONObject(0).isEmpty()) {
      JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
      o = this.setCsvbmErrinfo(o, errCd, "", "", "1", dataArray.getJSONObject(0), tokBmInfo);
      msg.add(o);
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

    if (!StringUtils.isEmpty(data.optString(TOKBMLayout.MOYSCD.getId())) && data.optString(TOKBMLayout.MOYSCD.getId()).length() >= 8) {
      moyskbn = data.optString(TOKBMLayout.MOYSCD.getId()).substring(0, 1);
      moysstdt = data.optString(TOKBMLayout.MOYSCD.getId()).substring(1, 7);
      moysrban = data.optString(TOKBMLayout.MOYSCD.getId()).substring(7);
    } else {
      JSONObject o = mu.getDbMessageObj("E20005", new String[] {});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.MOYSCD.getId()), TOKBMLayout.MOYSCD.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    }

    if (!moyskbn.equals("1") && !moyskbn.equals("2") && !moyskbn.equals("3")) {
      JSONObject o = mu.getDbMessageObj("E20004", new String[] {});
      o = this.setCsvbmErrinfo(o, errCd, moyskbn, TOKBMLayout.MOYSCD.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
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
    sbSQL.append(",HBSTDT "); // PLU配信済みフラグ
    sbSQL.append(",HBEDDT "); // PLU配信済みフラグ
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // 登録のない催しコード
      JSONObject o = mu.getDbMessageObj("E20005", new String[] {});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.MOYSCD.getId()), TOKBMLayout.MOYSCD.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    } else {
      // 催しコードのPLU配信済みフラグチェック
      if (dbDatas.getJSONObject(0).optString("PLUSFLG").equals("1")) {
        if (StringUtils.isEmpty(data.optString(TOKBMLayout.BMNNO.getId()))) {
          // PLU配信済みフラグ=1の場合はエラー
          JSONObject o = mu.getDbMessageObj("E20272", new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.MOYSCD.getId()), TOKBMLayout.MOYSCD.getTxt(), "1", data, tokBmInfo);
          msg.add(o);
          return msg;
        }
      }

      // 販売開始日の入力をチェック
      if ((!StringUtils.isEmpty(data.optString(TOKBMLayout.HBSTDT.getId())) && StringUtils.isEmpty(data.optString(TOKBMLayout.HBEDDT.getId())))
          || (!StringUtils.isEmpty(data.optString(TOKBMLayout.HBEDDT.getId())) && StringUtils.isEmpty(data.optString(TOKBMLayout.HBSTDT.getId())))) {

        // 販売期間Fromと販売期間Toの両方入力または両方未入力としてください。
        JSONObject o = mu.getDbMessageObj("E20297", new String[] {});
        o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.HBSTDT.getId()), TOKBMLayout.HBSTDT.getTxt(), "1", data, tokBmInfo);
        msg.add(o);
        return msg;
      } else if (!StringUtils.isEmpty(data.optString(TOKBMLayout.HBSTDT.getId())) && !StringUtils.isEmpty(data.optString(TOKBMLayout.HBEDDT.getId()))) {

        if (!StringUtils.isEmpty(dbDatas.getJSONObject(0).optString("HBSTDT")) && (dbDatas.getJSONObject(0).optInt("HBSTDT") > data.optInt(TOKBMLayout.HBSTDT.getId()))) {

          // 販売期間(開始日) ≧ 催し期間(開始日)の条件で入力してください。
          JSONObject o = mu.getDbMessageObj("E20007", new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.HBSTDT.getId()), TOKBMLayout.HBSTDT.getTxt(), "1", data, tokBmInfo);
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(dbDatas.getJSONObject(0).optString("HBEDDT")) && (dbDatas.getJSONObject(0).optInt("HBEDDT") < data.optInt(TOKBMLayout.HBEDDT.getId()))) {

          // 販売期間(終了日) ≦ 催し期間(終了日)の条件で入力してください。
          JSONObject o = mu.getDbMessageObj("E20008", new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.HBEDDT.getId()), TOKBMLayout.HBSTDT.getTxt(), "1", data, tokBmInfo);
          msg.add(o);
          return msg;
        } else if (data.optInt(TOKBMLayout.HBSTDT.getId()) > data.optInt(TOKBMLayout.HBEDDT.getId())) {

          // 販売期間(開始日) ≦ 販売期間(終了日)の条件で入力してください。
          JSONObject o = mu.getDbMessageObj("E20006", new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.HBSTDT.getId()), TOKBMLayout.HBSTDT.getTxt(), "1", data, tokBmInfo);
          msg.add(o);
          return msg;
        } else {
          hbStDt = data.optString(TOKBMLayout.HBSTDT.getId());
          hbEdDt = data.optString(TOKBMLayout.HBEDDT.getId());
        }
      } else {
        hbStDt = dbDatas.getJSONObject(0).optString("HBSTDT");
        hbEdDt = dbDatas.getJSONObject(0).optString("HBEDDT");
      }
    }

    // マイナス値チェック
    if (data.optInt(TOKBMLayout.BAIKAAM.getId()) < 0) {
      // 0以上の値を入力してください
      JSONObject o = mu.getDbMessageObj("EX1047", new String[] {TOKBMLayout.BAIKAAM.getTxt() + " ≧ 0の値"});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.BAIKAAM.getId()), TOKBMLayout.BAIKAAM.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    }

    if (data.optInt(TOKBMLayout.BD_KOSU1.getId()) < 0) {
      // 0以上の値を入力してください
      JSONObject o = mu.getDbMessageObj("EX1047", new String[] {TOKBMLayout.BD_KOSU1.getTxt() + " ≧ 0の値"});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.BD_KOSU1.getId()), TOKBMLayout.BD_KOSU1.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    }

    if (data.optInt(TOKBMLayout.BD_BAIKAAN1.getId()) < 0) {
      // 0以上の値を入力してください
      JSONObject o = mu.getDbMessageObj("EX1047", new String[] {TOKBMLayout.BD_BAIKAAN1.getTxt() + " ≧ 0の値"});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.BD_BAIKAAN1.getId()), TOKBMLayout.BD_BAIKAAN1.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    }

    if (data.optInt(TOKBMLayout.BD_KOSU2.getId()) < 0) {
      // 0以上の値を入力してください
      JSONObject o = mu.getDbMessageObj("EX1047", new String[] {TOKBMLayout.BD_KOSU2.getTxt() + " ≧ 0の値"});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.BD_KOSU2.getId()), TOKBMLayout.BD_KOSU2.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    }

    if (data.optInt(TOKBMLayout.BD_BAIKAAN2.getId()) < 0) {
      // 0以上の値を入力してください
      JSONObject o = mu.getDbMessageObj("EX1047", new String[] {TOKBMLayout.BD_BAIKAAN2.getTxt() + " ≧ 0の値"});
      o = this.setCsvbmErrinfo(o, errCd, data.optString(TOKBMLayout.BD_BAIKAAN2.getId()), TOKBMLayout.BD_BAIKAAN2.getTxt(), "1", data, tokBmInfo);
      msg.add(o);
      return msg;
    }

    // 対象店、除外店ランク№に入力がある場合、同一の値はエラー
    if (!StringUtils.isEmpty(data.optString(TOKBMLayout.RANKNO_ADD.getId())) || !StringUtils.isEmpty(data.optString(TOKBMLayout.RANKNO_DEL.getId()))) {
      rankNoAdd = data.optString(TOKBMLayout.RANKNO_ADD.getId());
      rankNoDel = data.optString(TOKBMLayout.RANKNO_DEL.getId());

      if (rankNoAdd.equals(rankNoDel)) {
        // 対象店・除外店ランク№が同一
        JSONObject o = mu.getDbMessageObj("E20016", new String[] {});
        o = this.setCsvbmErrinfo(o, errCd, rankNoAdd, TOKBMLayout.RANKNO_ADD.getTxt(), "1", data, tokBmInfo);
        msg.add(o);
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
      if (!StringUtils.isEmpty(data.optString(TOKBMLayout.BMNCD_RANK.getId()))) {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(data.optString(TOKBMLayout.BMNCD_RANK.getId()));
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
        JSONObject o = mu.getDbMessageObj("E20014", new String[] {});
        o = this.setCsvbmErrinfo(o, errCd, rankNoAdd, TOKBMLayout.RANKNO_ADD.getTxt(), "1", data, tokBmInfo);
        msg.add(o);
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
      if (!StringUtils.isEmpty(data.optString(TOKBMLayout.BMNCD_RANK.getId()))) {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(data.optString(TOKBMLayout.BMNCD_RANK.getId()));
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
        JSONObject o = mu.getDbMessageObj("E20015", new String[] {});
        o = this.setCsvbmErrinfo(o, errCd, rankNoDel, TOKBMLayout.RANKNO_DEL.getTxt(), "1", data, tokBmInfo);
        msg.add(o);
        return msg;
      }
    }

    int addCnt = 1;
    int delCnt = 11;

    for (int i = 0; i < 10; i++) {

      String val = dataArrayTj.optJSONObject(0).optString("F" + addCnt);
      if (StringUtils.isNotEmpty(val)) {
        String msgCd = checkTenCd(val);
        if (!StringUtils.isEmpty(msgCd)) {
          JSONObject o = mu.getDbMessageObj(msgCd, new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, val, TOKBMTJTENLayout.TENCD_ADD1.getTxt(), "1", dataArrayTj.optJSONObject(0), tokBmInfo);
          msg.add(o);
          return msg;
        }
        cTenCdAdds.add(dataArrayTj.optJSONObject(i));
        tenCdAdds_.add(val);
      }
      tenCdAdds.add(i, dataArrayTj.optJSONObject(0).optString("F" + addCnt));

      val = dataArrayTj.optJSONObject(0).optString("F" + delCnt);
      if (StringUtils.isNotEmpty(val)) {
        String msgCd = checkTenCd(val);
        if (!StringUtils.isEmpty(msgCd)) {
          JSONObject o = mu.getDbMessageObj(msgCd, new String[] {});
          o = this.setCsvbmErrinfo(o, errCd, val, TOKBMTJTENLayout.TENCD_DEL1.getTxt(), "1", dataArrayTj.optJSONObject(0), tokBmInfo);
          msg.add(o);
          return msg;
        }
        cTenCdDels.add(dataArrayTj.optJSONObject(i));
        tenCdDels_.add(val);
      }
      tenCdDels.add(i, dataArrayTj.optJSONObject(0).optString("F" + delCnt));

      addCnt++;
      delCnt++;
    }

    // 追加店重複
    if (cTenCdAdds.size() != tenCdAdds_.size()) {
      JSONObject o = mu.getDbMessageObj("E11040", new String[] {"対象店"});
      o = this.setCsvbmErrinfo(o, errCd, tenCdAdds_.toString(), TOKBMTJTENLayout.TENCD_ADD1.getTxt(), "1", dataArrayTj.optJSONObject(0), tokBmInfo);
      msg.add(o);
      return msg;
    }

    // 除外店重複
    if (cTenCdDels.size() != tenCdDels_.size()) {
      JSONObject o = mu.getDbMessageObj("E11040", new String[] {"除外店"});
      o = this.setCsvbmErrinfo(o, errCd, tenCdDels_.toString(), TOKBMTJTENLayout.TENCD_DEL1.getTxt(), "1", dataArrayTj.optJSONObject(0), tokBmInfo);
      msg.add(o);
      return msg;
    }

    // 対象店を取得
    String checkTencd = new ReportBM015Dao(JNDIname).checkTenCdAdd(data.optString(TOKBMLayout.BMNCD_RANK.getId()) // 部門コード
        , moyskbn // 催し区分
        , moysstdt // 催し開始日
        , moysrban // 催し連番
        , rankNoAdd // 対象ランク№
        , rankNoDel // 除外ランク№
        , tenCdAdds // 対象店
        , tenCdDels // 除外店
    );

    if (!StringUtils.isEmpty(checkTencd)) {
      JSONObject o = mu.getDbMessageObj(checkTencd, new String[] {});
      o = this.setCsvbmErrinfo(o, errCd, rankNoAdd, TOKBMTJTENLayout.TENCD_ADD1.getTxt(), "1", dataArrayTj.optJSONObject(0), tokBmInfo);
      msg.add(o);
      return msg;
    }

    // 入力値の重複チェック（商品コード）
    for (int i = 0; i < dataArrayShn.size(); i++) {
      String val = dataArrayShn.optJSONObject(i).optString(TOKBMSHNLayout.SHNCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        shncds.add(dataArrayShn.optJSONObject(i));
        shncds_.add(val);
      }
    }

    if (shncds.size() != shncds_.size()) {
      JSONObject o = mu.getDbMessageObj("EX1022", new String[] {});
      o = this.setCsvbmErrinfo(o, errCd, shncds_.toString(), TOKBMSHNLayout.SHNCD.getTxt(), "1", dataArrayShn.optJSONObject(0), tokBmInfo);
      msg.add(o);
      return msg;
    }

    // 商品チェック
    for (int i = 0; i < dataArrayShn.size(); i++) {
      JSONObject dataG = new JSONObject();
      dataG = dataArrayShn.getJSONObject(i);
      if (dataG.isEmpty()) {
        continue;
      }

      // 変数を初期化
      sbSQL = new StringBuffer();
      new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<>();

      if (StringUtils.isEmpty(dataG.optString(TOKBMSHNLayout.SHNCD.getId()))) {
        sqlWhere += "SHNCD=null";
      } else {
        sqlWhere += "SHNCD=?";
        paramData.add(dataG.optString(TOKBMSHNLayout.SHNCD.getId()));
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
        JSONObject o = mu.getDbMessageObj("E11046", new String[] {});
        o = this.setCsvbmErrinfo(o, errCd, dataG.optString(TOKBMSHNLayout.SHNCD.getId()), TOKBMSHNLayout.SHNCD.getTxt(), String.valueOf(i + 1), dataG, tokBmInfo);
        msg.add(o);
        return msg;
      }

      // 販売期間の日数を取得
      int days = getHbDays(hbStDt, hbEdDt);

      // 対象店を取得
      Set<Integer> tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(data.optString(TOKBMLayout.BMNCD_RANK.getId()) // 部門コード
          , moyskbn // 催し区分
          , moysstdt // 催し開始日
          , moysrban // 催し連番
          , rankNoAdd // 対象ランク№
          , rankNoDel // 除外ランク№
          , tenCdAdds // 対象店
          , tenCdDels // 除外店
      );

      for (int amount = 0; amount <= days; amount++) {

        String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
        HashMap<String, String> moyCdArrMap = getArrMap(moyskbn, dataG.optString(TOKBMSHNLayout.SHNCD.getId()), hbDt, "0");

        Iterator<Integer> ten = tencds.iterator();
        for (int j = 0; j < tencds.size(); j++) {

          String getTen = String.valueOf(ten.next());

          if (moyCdArrMap.containsKey(getTen)) {
            // 既に催しコードが登録されている為エラー
            JSONObject o = mu.getDbMessageObj("E40007", new String[] {});
            o = this.setCsvbmErrinfo(o, errCd, dataG.optString(TOKBMSHNLayout.SHNCD.getId()), TOKBMSHNLayout.SHNCD.getTxt(), String.valueOf(i + 1), dataG, tokBmInfo);
            msg.add(o);
            return msg;
          }
        }
      }
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

  public JSONObject setCsvbmErrinfo(JSONObject o, String errCd, String errvl, String errfld, String inputno, JSONObject jo, JSONObject tokBm) {
    o.put(CSVTOKLayout.ERRCD.getCol(), errCd);
    o.put(CSVTOKLayout.ERRTBLNM.getCol(), o.optString(MessageUtility.MSG));
    o.put(CSVTOKLayout.ERRFLD.getCol(), errfld);
    o.put(CSVTOKLayout.ERRVL.getCol(), errvl);
    o.put(CSVTOKLayout.INPUTNO.getCol(), inputno);

    String moyskbn = tokBm.optString(TOKBMLayout.MOYSKBN.getId());
    String moysstdt = tokBm.optString(TOKBMLayout.MOYSSTDT.getId());
    String moysrban = tokBm.optString(TOKBMLayout.MOYSRBAN.getId());
    String bmnno = tokBm.optString(TOKBMLayout.BMNNO.getId());
    String bmnman = tokBm.optString(TOKBMLayout.BMNMAN.getId());
    String hbstdt = tokBm.optString(TOKBMLayout.HBSTDT.getId());
    String hbeddt = tokBm.optString(TOKBMLayout.HBEDDT.getId());

    // 催し区分
    if (InputChecker.isInteger(moyskbn) && moyskbn.length() == 1) {
      o.put(CSVTOKLayout.MOYSKBN.getCol(), moyskbn);
    } else {
      o.put(CSVTOKLayout.MOYSKBN.getCol(), "");
    }

    // 催し開始日
    if (InputChecker.isInteger(moysstdt) && moysstdt.length() == 6) {
      o.put(CSVTOKLayout.MOYSSTDT.getCol(), moysstdt);
    } else {
      o.put(CSVTOKLayout.MOYSSTDT.getCol(), "");
    }

    // 催し連番
    if (InputChecker.isInteger(moysrban) && moysrban.length() >= 1 && moysrban.length() <= 3) {
      o.put(CSVTOKLayout.MOYSRBAN.getCol(), moysrban);
    } else {
      o.put(CSVTOKLayout.MOYSRBAN.getCol(), "");
    }

    // BM番号
    if (InputChecker.isInteger(bmnno) && bmnno.length() >= 1 && bmnno.length() <= 3) {
      o.put(CSVTOKLayout.BMNNO.getCol(), bmnno);
    } else {
      o.put(CSVTOKLayout.BMNNO.getCol(), "");
    }

    // BM名称（ｶﾅ）
    if (bmnman.length() >= 1 && bmnman.length() <= 20) {
      o.put(CSVTOKLayout.BMNMAN.getCol(), bmnman);
    } else {
      o.put(CSVTOKLayout.BMNMAN.getCol(), "");
    }

    // 販売開始日
    if (InputChecker.isValidDate(hbstdt)) {
      o.put(CSVTOKLayout.HBSTDT.getCol(), hbstdt);
    } else {
      o.put(CSVTOKLayout.HBSTDT.getCol(), "");
    }

    // 販売終了日
    if (InputChecker.isValidDate(hbstdt)) {
      o.put(CSVTOKLayout.HBEDDT.getCol(), hbeddt);
    } else {
      o.put(CSVTOKLayout.HBEDDT.getCol(), "");
    }

    // エラー項目判定
    if (errfld.equals(TOKBMSHNLayout.SHNCD.getTxt())) {

      String shncd = jo.optString(TOKBMSHNLayout.SHNCD.getId());

      if (shncd.length() == 8) {
        o.put(CSVTOKLayout.SHNCD.getCol(), shncd);
      } else {
        o.put(CSVTOKLayout.SHNCD.getCol(), "");
      }
    }

    return o;
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

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // BM催し送信情報INSERT/UPDATE処理
    createSqlBm(data, dataArrayG, map.get("TENATSUK_ARR"), userInfo);

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

      // BM番号
      if (StringUtils.isEmpty(bmno)) {
        targetWhere += "BMNNO=null ";
      } else {
        targetWhere += "BMNNO=? ";
        targetParam.add(bmno);
      }

      targetTable = "INATK.TOKBM";


      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F26"))) {
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

      // BM番号
      if (StringUtils.isEmpty(data.optString("F6"))) {
        targetWhere += "BMNNO=null ";
      } else {
        targetWhere += "BMNNO=? ";
        targetParam.add(data.optString("F6"));
      }

      targetTable = "INATK.TOKBM";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F26"))) {
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
  public String createSqlBm(JSONObject data, JSONArray dataArrayG, String tenAtuk_Arr, User userInfo) {

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
    String moyCd_Arr = "";
    String kanriNo_Arr = "";
    String bmnNo_Arr = "";
    String hbShn = "";
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();

    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    // 新規の場合BM番号を取得
    if (StringUtils.isEmpty(data.optString("F6"))) {

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
          bmno = String.valueOf(1);
          bmnoShn = String.valueOf(1);
        } else {
          bmno = getData.optString("SUMI_BMNO");
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
      sbSQL.append(") VALUES ");
      sbSQL.append(" (?,?,?,? ");
      sbSQL.append(", '" + userId + "'  "); // オペレーター：
      sbSQL.append(",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.SYSBMCD WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt
          + " AND MOYSRBAN = " + moysrban + " LIMIT 1 ) T2 ) "); // 登録日：
      sbSQL.append(", CURRENT_TIMESTAMP ) "); // 更新日：



      // 催し区分
      paramData.add(moyskbn);
      // 催し開始日
      paramData.add(moysstdt);
      // 催し連番
      paramData.add(moysrban);
      // 付番済BM番号
      paramData.add(bmno);

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("BM番号内部管理");
    } else {
      bmno = data.optString("F6");
      bmnoShn = data.optString("F6");
    }

    // 店扱いフラグ配列作成

    int addCnt = 37;
    int delCnt = 27;

    String addValues = "";
    String delValues = "";

    for (int i = 0; i < 10; i++) {
      String addTen = data.optString("F" + addCnt);
      String delTen = data.optString("F" + delCnt);
      tenCdAdds.add(i, addTen);
      tenCdDels.add(i, delTen);

      if (!StringUtils.isEmpty(addTen)) {
        addValues += StringUtils.isEmpty(addValues) ? addTen : "," + addTen;
      }
      if (!StringUtils.isEmpty(addTen)) {
        delValues += StringUtils.isEmpty(delValues) ? delTen : "," + delTen;
      }

      addCnt++;
      delCnt++;
    }

    // 新規の場合BM番号を取得
    if (!StringUtils.isEmpty(data.optString("F6"))) {
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

      // バンドル番号
      if (StringUtils.isEmpty(bmno)) {
        sqlWhere += "BMNNO=null ";
      } else {
        sqlWhere += "BMNNO=? ";
        paramData.add(bmno);
      }

      sbSQL.append(" SELECT * FROM ");
      sbSQL.append(" (SELECT ");
      sbSQL.append(" MOYSKBN ");
      sbSQL.append(" ,MOYSSTDT ");
      sbSQL.append(" ,MOYSRBAN ");
      sbSQL.append(" ,BMNNO ");
      sbSQL.append(" ,TENCD ");
      sbSQL.append(" ,TJFLG ");
      sbSQL.append(" FROM ");
      sbSQL.append(" INATK.TOKBM_TJTEN ");
      sbSQL.append(" WHERE ");
      sbSQL.append(" TJFLG = 1  ");
      if (!StringUtils.isEmpty(addValues)) {
        sbSQL.append(" AND TENCD NOT IN (" + addValues + ") ");
      }
      sbSQL.append(" UNION ALL ");
      sbSQL.append(" SELECT ");
      sbSQL.append(" MOYSKBN ");
      sbSQL.append(" ,MOYSSTDT ");
      sbSQL.append(" ,MOYSRBAN ");
      sbSQL.append(" ,BMNNO ");
      sbSQL.append(" ,TENCD ");
      sbSQL.append(" ,TJFLG ");
      sbSQL.append(" FROM ");
      sbSQL.append(" INATK.TOKBM_TJTEN ");
      sbSQL.append(" WHERE ");
      sbSQL.append(" TJFLG = 0  ");
      if (!StringUtils.isEmpty(delValues)) {
        sbSQL.append(" AND TENCD NOT IN (" + delValues + ")) ");
      }
      sbSQL.append(" )T1 WHERE " + sqlWhere);

      dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      for (int i = 0; i < dbDatas.size(); i++) {

        String tencd = dbDatas.getJSONObject(i).optString("TENCD");
        String tjflg = dbDatas.getJSONObject(i).optString("TENCD");

        // 元々対象店だったものが今回の画面入力で除外されたケース
        if (tjflg.equals("0")) {
          tenCdDels.add(tenCdDels.size(), tencd);

          // 元々除外店だったものが今回の画面入力で除外されたケース
        } else {
          tenCdAdds.add(tenCdAdds.size(), tencd);
        }
      }
    }

    // 対象店を取得
    ReportBM015Dao dao = new ReportBM015Dao(JNDIname);
    Set<Integer> tencds = new TreeSet<>();
    if (!StringUtils.isEmpty(tenAtuk_Arr)) {
      tencds = dao.getTenCdAddArr(tenAtuk_Arr, tenCdAdds, tenCdDels);
      tenAtuk_Arr = "";
    } else {
      tencds = dao.getTenCdAdd(data.optString("F17") // 部門コード
          , moyskbn // 催し区分
          , moysstdt // 催し開始日
          , moysrban // 催し連番
          , data.optString("F18") // 対象ランク№
          , data.optString("F19") // 除外ランク№
          , tenCdAdds // 対象店
          , tenCdDels // 除外店
      );
    }

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
    tenAtuk_Arr = new ReportJU012Dao(JNDIname).spaceArr(tenAtuk_Arr, 1);

    int maxField = 27; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // values += String.valueOf(0 + 1);
        // 催しコードを追加
        values += " ?, ?, ?,";
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
      }

      if (!ArrayUtils.contains(new String[] {"F1", "F2", "F3", "F4", "F5", "F20", "F21", "F22", "F23", "F24", "F25", "F26"}, key)) {
        String val = data.optString(key);
        if (key.equals("F6")) {
          values += "? ,";
          prmData.add(bmno);
        } else if (key.equals("F27")) {
          values += "? ,";
          prmData.add(tenAtuk_Arr);

          // 数量、金額には0をセット
        } else if (StringUtils.isEmpty(val) && (key.equals("F10") || key.equals("F11") || key.equals("F12") || key.equals("F13") || key.equals("F14"))) {
          values += "0 ,";
          // 販売期間Fromが空の場合は催し期間Fromを使用
        } else if (key.equals("F15")) {
          values += "? ,";
          if (StringUtils.isEmpty(val)) {
            prmData.add(data.optString("F3"));
          } else {
            prmData.add(val);
          }

          // 販売期間Toが空の場合は催し期間Toを使用
        } else if (key.equals("F16")) {
          values += "? ,";
          if (StringUtils.isEmpty(val)) {
            prmData.add(data.optString("F4"));
          } else {
            prmData.add(val);
          }
        } else if (StringUtils.isEmpty(val)) {
          values += "null ,";
        } else {
          values += "? ,";
          prmData.add(val);
        }
      }

      if (k == maxField) {
        System.out.print("prm:" + prmData + "\n");
        values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + " ";
        values += ", '" + userId + "'";
        values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values +=
            "FROM INATK.TOKBM WHERE MOYSKBN = " + prmData.get(0) + " AND MOYSSTDT = " + prmData.get(1) + " AND MOYSRBAN = " + prmData.get(2) + " AND BMNNO = " + prmData.get(3) + " LIMIT 1 ) T2 ) ";
        values += ", CURRENT_TIMESTAMP";
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKBM  ( ");
    sbSQL.append(" MOYSKBN"); // F1 催し区分
    sbSQL.append(",MOYSSTDT"); // F1 催し開始日
    sbSQL.append(",MOYSRBAN"); // F1 催し連番
    sbSQL.append(",BMNNO"); // F6 BM番号
    sbSQL.append(",BMNMAN"); // F7 BM名称（カナ）
    sbSQL.append(",BMTYP"); // F8 BMタイプ
    sbSQL.append(",BMNMKN"); // F9 BM名称（漢字）
    sbSQL.append(",BAIKAAM"); // F10 1個売価
    sbSQL.append(",BD_KOSU1"); // F11 バンドル個数1
    sbSQL.append(",BD_BAIKAAN1"); // F12 バンドル売価1
    sbSQL.append(",BD_KOSU2"); // F13 バンドル個数2
    sbSQL.append(",BD_BAIKAAN2"); // F14 バンドル売価2
    sbSQL.append(",HBSTDT"); // F15 販売開始日
    sbSQL.append(",HBEDDT"); // F16 販売終了日
    sbSQL.append(",BMNCD_RANK"); // F17 ランク用部門コード
    sbSQL.append(",RANKNO_ADD"); // F18 対象ランク
    sbSQL.append(",RANKNO_DEL"); // F19 除外ランク
    sbSQL.append(",TENATSUK_ARR"); // F27-F36 店扱いフラグ配列
    sbSQL.append(",UPDKBN"); // 更新区分：
    sbSQL.append(",SENDFLG");// 送信区分：
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES");
    sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");


    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    }

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
    if (StringUtils.isEmpty(bmno)) {
      sqlWhere += "BMNNO=null ";
    } else {
      sqlWhere += "BMNNO=? ";
      paramData.add(bmno);
    }


    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.TOKBM_TJTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    }

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
        prmData.add(bmno);
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
        prmData.add(bmno);
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

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

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

    maxField = 9; // Fxxの最大値
    int len = dataArrayG.size();
    int delRow = 0;
    for (int i = 0; i < len; i++) {
      JSONObject dataG = dataArrayG.getJSONObject(i);
      if (!dataG.isEmpty() && !dataG.optString("F1").equals("1")) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (k == 1) {
            values += "? ,? ,?,"; // 催しコードをパラメーターに入れる
            prmData.add(moyskbn);
            prmData.add(moysstdt);
            prmData.add(moysrban);
          }

          int days = 0;
          String hbStDt = "";

          if (!ArrayUtils.contains(new String[] {"F1", "F3", "F5", "F6", "F7"}, key)) {
            String val = dataG.optString(key);

            // 新規の場合管理番号を取得
            if (StringUtils.isEmpty(kanrino) && StringUtils.isEmpty(dataG.optString("F9"))) {

              sbSQL = new StringBuffer();
              new ItemList();
              dbDatas = new JSONArray();
              sqlWhere = "";
              paramData = new ArrayList<>();
              getData = new JSONObject();

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

              // BM番号
              if (!StringUtils.isEmpty(dataG.optString("F8"))) {
                sqlWhere += "AND BMNO=? ";
                paramData.add(dataG.optString("F8"));
                bmnoShn = dataG.optString("F8");
              }

              // 一覧表情報
              sbSQL.append("SELECT ");
              sbSQL.append("MAX(SUMI_KANRINO) AS SUMI_KANRINO ");
              sbSQL.append("FROM INATK.SYSBM_KANRINO ");
              sbSQL.append("WHERE ");
              sbSQL.append(sqlWhere);

              dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

              if (dbDatas.size() > 0) {
                getData = dbDatas.getJSONObject(0);
                if (StringUtils.isEmpty(getData.optString("SUMI_KANRINO"))) {
                  kanrino = "0";
                } else {
                  kanrino = getData.optString("SUMI_KANRINO");
                }
              }
            }

            if (key.equals("F8")) {
              values += "? ,";
              prmData.add(bmno);
            } else if (key.equals("F9")) {
              if (StringUtils.isEmpty(val)) {
                kanrino = String.valueOf(Integer.valueOf(kanrino) + 1);
                values += "? ,";
                prmData.add(kanrino);
              } else {
                values += "? ,";
                prmData.add(String.valueOf(val));
              }
            } else if (StringUtils.isEmpty(val)) {
              values += "null ,";
            } else {
              values += "?,";
              prmData.add(val);
            }


            if (k == maxField) {
              values += " " + DefineReport.Values.SENDFLG_UN.getVal() + " ";// 送信区分：
              values += ", '" + userId + "'  "; // オペレーター：
              /*values += ",  (SELECT * FROM (SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHN WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = "
                  + moysstdt + " AND MOYSRBAN = " + moysrban + " AND BMNNO = " + bmno + " AND KANRINO = " + prmData.get(((i + 1) * 7) - 1) + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
              */
              values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
              values +=
                  "FROM INATK.TOKBM_SHN WHERE MOYSKBN = " + prmData.get(0) + " AND MOYSSTDT = " + prmData.get(1) + " AND MOYSRBAN = " + prmData.get(2) + " AND SHNCD = " + prmData.get(3) + " LIMIT 1 ) T2 ) ";
              
              values += ", CURRENT_TIMESTAMP  "; // 更新日：
              valueData = ArrayUtils.add(valueData, "(" + values + ")");
              values = "";
            }

            // BM商品販売日更新用
            if (key.equals("F2")) {
              hbShn = val;
            }

            // BM商品販売日用配列作成
            if (key.equals("F9")) {
              if (!StringUtils.isEmpty(data.optString("F15")) && !StringUtils.isEmpty(data.optString("F16"))) {
                hbStDt = data.optString("F15");
                days = getHbDays(hbStDt, data.optString("F16"));
              } else {
                hbStDt = data.optString("F3");
                days = getHbDays(hbStDt, data.optString("F4"));
              }
            } else {
              days = -1;
            }

            String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

            for (int amount = 0; amount <= days; amount++) {

              String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
              HashMap<String, String> eachMoysCdArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "0");
              HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "1");
              HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "2");

              HashMap<String, String> moyCdArrMap = new HashMap<>();

              for (HashMap.Entry<String, String> getMoysCd : eachMoysCdArrMap.entrySet()) {
                String getTen = getMoysCd.getKey();
                String getMoys = getMoysCd.getValue();

                boolean remFlg = false;

                if (getMoys.equals(moyscd)) {
                  // 催しが一致した場合さらにBM番号と管理番号も一致するかを確認
                  if (bmnNoArrMap.containsKey(getTen) && bmnNoArrMap.get(getTen).trim().equals(bmno)) {
                    if (!StringUtils.isEmpty(dataG.optString("F9"))) {
                      if (kanriNoArrMap.containsKey(getTen) && kanriNoArrMap.get(getTen).trim().equals(dataG.optString("F9"))) {

                        bmnNoArrMap.remove(getTen);
                        kanriNoArrMap.remove(getTen);

                        remFlg = true;
                      }
                    }
                  }
                }

                if (!remFlg && !moyCdArrMap.containsKey(getTen)) {
                  moyCdArrMap.put(getTen, moyscd);
                }
              }

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
                  for (int digit = 0; digit < (3 - bmno.length()); digit++) {
                    space += " ";
                  }
                  bmnNoArrMap.put(tenCd, space + bmno);
                }

                // 管理番号を追加
                if (!kanriNoArrMap.containsKey(tenCd)) {

                  String no = "";
                  // 管理番号はその都度作成する
                  if (key.equals("F9") && StringUtils.isEmpty(val)) {
                    no = kanrino;
                  } else if (key.equals("F9") && !StringUtils.isEmpty(val)) {
                    no = val;
                  }

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

              moyCd_Arr = new ReportJU012Dao(JNDIname).spaceArr(moyCd_Arr, 10);

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

              bmnNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(bmnNo_Arr, 3);

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

              kanriNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(kanriNo_Arr, 4);

              // 販売日、催しコード配列、管理番号配列、BM番号配列
              // valuesHb += String.valueOf(amount + 1);
              if (StringUtils.isEmpty(hbShn)) {
                valuesHb += "null, ";
              } else {
                valuesHb += "?, ";
              }
              valuesHb += "?, ?, ?, ?, ";
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
                    + dataArrayG.optJSONObject(i).optString("F2") + " AND HBDT = " + hbDt + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
                // 催し区分=3ならBM商品販売日_本部個特
              } else {
                valuesHb += ",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHNHBDT_HTK WHERE SHNCD = "
                    + dataArrayG.optJSONObject(i).optString("F2") + " AND HBDT = " + hbDt + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
              }

              valuesHb += ", CURRENT_TIMESTAMP  "; // 更新日：
              valueDataHb = ArrayUtils.add(valueDataHb, "(" + valuesHb + ")");
              valuesHb = "";
            }


            // BM商品販売日用配列作成
            String hidHbStDt = data.optString("F47");
            String hidHbEdDt = data.optString("F48");
            hbStDt = data.optString("F15");
            String hbEdDt = data.optString("F16");

            if ((key.equals("F9")) && (!StringUtils.isEmpty(hidHbStDt) && !StringUtils.isEmpty(hidHbEdDt)) && (!hidHbStDt.equals(hbStDt) || !hidHbEdDt.equals(hbEdDt))) {

              days = 0;

              if ((Integer.valueOf(hidHbStDt) < Integer.valueOf(hbStDt) && Integer.valueOf(hidHbEdDt) < Integer.valueOf(hbStDt))
                  || (Integer.valueOf(hidHbStDt) > Integer.valueOf(hbEdDt) && Integer.valueOf(hidHbEdDt) > Integer.valueOf(hbEdDt))) {
                hbStDt = hidHbStDt;
                hbEdDt = hidHbEdDt;
              } else if ((Integer.valueOf(hidHbStDt) < Integer.valueOf(hbStDt) && Integer.valueOf(hidHbEdDt) > Integer.valueOf(hbStDt))
                  || (Integer.valueOf(hidHbStDt) < Integer.valueOf(hbStDt) && Integer.valueOf(hidHbEdDt) <= Integer.valueOf(hbEdDt))) {
                hbEdDt = String.valueOf(Integer.valueOf(hbStDt) - 1);
                hbStDt = hidHbStDt;
              } else if ((Integer.valueOf(hidHbStDt) < Integer.valueOf(hbEdDt) && Integer.valueOf(hidHbEdDt) > Integer.valueOf(hbEdDt))
                  || (Integer.valueOf(hidHbStDt) >= Integer.valueOf(hbStDt) && Integer.valueOf(hidHbEdDt) > Integer.valueOf(hbEdDt))) {
                hbStDt = String.valueOf(Integer.valueOf(hbEdDt) + 1);
                hbEdDt = hidHbEdDt;
              } else {
                days = -1;
              }

              // 非対象になったデータがあった場合期間の取得
              if (days == 0) {
                days = getHbDays(hbStDt, hbEdDt);
              }

              for (int amount = 0; amount <= days; amount++) {

                String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
                HashMap<String, String> moyCdArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "0");
                HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "1");
                HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "2");

                ten = tencds.iterator();

                for (int j = 0; j < tencds.size(); j++) {

                  String tenCd = String.valueOf(ten.next());

                  // 催しコードを追加
                  if (moyCdArrMap.containsKey(tenCd)) {
                    moyCdArrMap.put(tenCd, "          ");
                  }

                  // BM番号を追加
                  if (bmnNoArrMap.containsKey(tenCd)) {
                    bmnNoArrMap.put(tenCd, "   ");
                  }

                  // 管理番号を追加
                  if (kanriNoArrMap.containsKey(tenCd)) {
                    kanriNoArrMap.put(tenCd, "    ");
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

                moyCd_Arr = new ReportJU012Dao(JNDIname).spaceArr(moyCd_Arr, 10);

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

                bmnNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(bmnNo_Arr, 3);

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

                kanriNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(kanriNo_Arr, 4);

                // 初期化
                sbSQL = new StringBuffer();
                sqlWhere = "";
                paramData = new ArrayList<>();

                // 催しコード配列が空だったらDELETE
                if (StringUtils.isEmpty(moyCd_Arr.trim())) {
                  sbSQL.append("DELETE FROM ");
                } else {
                  sbSQL.append("UPDATE ");
                }

                // 催し区分=1or2ならBM商品販売日
                if (!moyskbn.equals("3")) {
                  sbSQL.append("INATK.TOKBM_SHNHBDT ");
                  // 催し区分=3ならBM商品販売日_本部個特
                } else {
                  sbSQL.append("INATK.TOKBM_SHNHBDT_HTK ");
                }

                if (!StringUtils.isEmpty(moyCd_Arr.trim())) {
                  sbSQL.append("SET ");
                  sbSQL.append("MOYCD_ARR=? ");
                  sbSQL.append(",BMNNO_ARR=? ");
                  sbSQL.append(",KANRINO_ARR=? ");
                  sbSQL.append(",OPERATOR='" + userId + "' ");
                  sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
                  paramData.add(moyCd_Arr);
                  paramData.add(bmnNo_Arr);
                  paramData.add(kanriNo_Arr);
                }

                // 商品コード
                if (StringUtils.isEmpty(dataG.optString("F2"))) {
                  sqlWhere += "SHNCD=null AND ";
                } else {
                  sqlWhere += "SHNCD=? AND ";
                  paramData.add(dataG.optString("F2"));
                }

                // 販売日
                sqlWhere += "HBDT=? ";
                paramData.add(hbDt);

                sbSQL.append("WHERE ");
                sbSQL.append(sqlWhere);

                if (DefineReport.ID_DEBUG_MODE) {
                  System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
                }
                  
                sqlList.add(sbSQL.toString());
                prmList.add(paramData);
                lblList.add("BM商品販売日");
              }
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



          if (DefineReport.ID_DEBUG_MODE) {
            System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
          }
            
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
          sbSQL.append(",BMNNO_ARR"); // BM番号配列
          sbSQL.append(",KANRINO_ARR"); // 管理番号配列
          sbSQL.append(", OPERATOR "); // オペレーター：
          sbSQL.append(", ADDDT "); // 登録日：
          sbSQL.append(",  UPDDT "); // 更新日：
          sbSQL.append(") VALUES");
          sbSQL.append("  " + StringUtils.join(valueDataHb, ",") + " ");


          if (DefineReport.ID_DEBUG_MODE) {
            System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
          }

          sqlList.add(sbSQL.toString());
          prmList.add(prmDataHb);
          lblList.add("BM商品販売日");

          // クリア
          prmDataHb = new ArrayList<>();
          valueDataHb = new Object[] {};
          valuesHb = "";
        }
      } else {

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
        if (StringUtils.isEmpty(dataG.optString("F8"))) {
          sqlWhere += "BMNNO=null AND ";
        } else {
          sqlWhere += "BMNNO=? AND ";
          paramData.add(dataG.optString("F8"));
        }

        // 管理番号
        if (StringUtils.isEmpty(dataG.optString("F9"))) {
          sqlWhere += "KANRINO=null AND ";
        } else {
          sqlWhere += "KANRINO=? AND ";
          paramData.add(dataG.optString("F9"));
        }

        // 商品コード
        if (StringUtils.isEmpty(dataG.optString("F2"))) {
          sqlWhere += "SHNCD=null ";
        } else {
          sqlWhere += "SHNCD=? ";
          paramData.add(dataG.optString("F2"));
        }

        sbSQL = new StringBuffer();
        sbSQL.append("DELETE FROM INATK.TOKBM_SHN ");
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere);

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(paramData);
        lblList.add("BM催し送信_商品");

        // BM商品販売日用配列作成
        String hbStDt = data.optString("F47");
        int days = getHbDays(hbStDt, data.optString("F48"));

        for (int amount = 0; amount <= days; amount++) {

          String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
          HashMap<String, String> moyCdArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "0");
          HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "1");
          HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "2");

          ten = tencds.iterator();

          for (int j = 0; j < tencds.size(); j++) {

            String tenCd = String.valueOf(ten.next());

            // 催しコードを追加
            if (moyCdArrMap.containsKey(tenCd)) {
              moyCdArrMap.put(tenCd, "          ");
            }

            // BM番号を追加
            if (bmnNoArrMap.containsKey(tenCd)) {
              bmnNoArrMap.put(tenCd, "   ");
            }

            // 管理番号を追加
            if (kanriNoArrMap.containsKey(tenCd)) {
              kanriNoArrMap.put(tenCd, "    ");
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

          moyCd_Arr = new ReportJU012Dao(JNDIname).spaceArr(moyCd_Arr, 10);

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

          bmnNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(bmnNo_Arr, 3);

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

          kanriNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(kanriNo_Arr, 4);

          // 初期化
          sbSQL = new StringBuffer();
          sqlWhere = "";
          paramData = new ArrayList<>();

          // 催しコード配列が空だったらDELETE
          if (StringUtils.isEmpty(moyCd_Arr.trim())) {
            sbSQL.append("DELETE FROM ");
          } else {
            sbSQL.append("UPDATE ");
          }

          // 催し区分=1or2ならBM商品販売日
          if (!moyskbn.equals("3")) {
            sbSQL.append("INATK.TOKBM_SHNHBDT ");
            // 催し区分=3ならBM商品販売日_本部個特
          } else {
            sbSQL.append("INATK.TOKBM_SHNHBDT_HTK ");
          }

          if (!StringUtils.isEmpty(moyCd_Arr.trim())) {
            sbSQL.append("SET ");
            sbSQL.append("MOYCD_ARR=? ");
            sbSQL.append(",BMNNO_ARR=? ");
            sbSQL.append(",KANRINO_ARR=? ");
            sbSQL.append(",OPERATOR='" + userId + "' ");
            sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
            paramData.add(moyCd_Arr);
            paramData.add(bmnNo_Arr);
            paramData.add(kanriNo_Arr);
          }

          // 商品コード
          if (StringUtils.isEmpty(dataG.optString("F2"))) {
            sqlWhere += "SHNCD=null AND ";
          } else {
            sqlWhere += "SHNCD=? AND ";
            paramData.add(dataG.optString("F2"));
          }

          // 販売日
          sqlWhere += "HBDT=? ";
          paramData.add(hbDt);

          sbSQL.append("WHERE ");
          sbSQL.append(sqlWhere);

          if (DefineReport.ID_DEBUG_MODE) {
            System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
          }

          sqlList.add(sbSQL.toString());
          prmList.add(paramData);
          lblList.add("BM商品販売日");
        }
        delRow++;
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
      paramData.add(bmno);

      // 番号の更新
      sbSQL = new StringBuffer();
      sbSQL.append("UPDATE INATK.TOKBM ");
      sbSQL.append("SET ");
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
      sbSQL.append(" WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

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
          + moysstdt + " AND MOYSRBAN = " + moysrban + " AND BMNO = " + bmno + " LIMIT 1 ) T2 ) "); // 登録日：
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

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("BM管理番号内部管理");
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

    if (data.optString(TOKBMLayout.MOYSCD.getId()).length() >= 8) {
      moyskbn = data.optString(TOKBMLayout.MOYSCD.getId()).substring(0, 1);
      moysstdt = data.optString(TOKBMLayout.MOYSCD.getId()).substring(1, 7);
      moysrban = data.optString(TOKBMLayout.MOYSCD.getId()).substring(7);
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
        bmno = String.valueOf(1);
        bmnoShn = String.valueOf(1);
      } else {
        bmno = getData.optString("SUMI_BMNO");
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
    sbSQL.append(" )VALUES (?,?,?,?");
    sbSQL.append(", '" + userId + "'  "); // オペレーター：
    sbSQL.append(",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.SYSBMCD WHERE MOYSKBN = " + paramData.get(0) + " AND MOYSSTDT = "
        + paramData.get(1) + " AND MOYSRBAN = " + paramData.get(2) + " LIMIT 1 ) T2 ) "); // 登録日：
    sbSQL.append(", CURRENT_TIMESTAMP ) "); // 更新日：


    // 催し区分
    paramData.add(moyskbn);
    // 催し開始日
    paramData.add(moysstdt);
    // 催し連番
    paramData.add(moysrban);
    // 付番済BM番号
    paramData.add(bmno);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    }

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
    Set<Integer> tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(data.optString(TOKBMLayout.BMNCD_RANK.getId()) // 部門コード
        , moyskbn // 催し区分
        , moysstdt // 催し開始日
        , moysrban // 催し連番
        , data.optString(TOKBMLayout.RANKNO_ADD.getId()) // 対象ランク№
        , data.optString(TOKBMLayout.RANKNO_DEL.getId()) // 除外ランク№
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

    tenAtuk_Arr = new ReportJU012Dao(JNDIname).spaceArr(tenAtuk_Arr, 1);

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
        if (key.equals(TOKBMLayout.BMNNO.getId())) {
          values += " ?, ";
          prmData.add(bmno);
        } else if (key.equals("F19")) {
          values += "?, ";
          prmData.add(tenAtuk_Arr);

          // 販売期間Fromが空の場合は催し期間Fromを使用
        } else if (key.equals(TOKBMLayout.HBSTDT.getId())) {
          values += "?, ";
          if (StringUtils.isEmpty(val)) {
            prmData.add(hbStDt);
          } else {
            prmData.add(val);
          }

          // 販売期間Toが空の場合は催し期間Toを使用
        } else if (key.equals(TOKBMLayout.HBEDDT.getId())) {
          values += "?, ";
          if (StringUtils.isEmpty(val)) {
            prmData.add(hbEdDt);
          } else {
            prmData.add(val);
          }
        } else if (StringUtils.isEmpty(val)) {
          values += "null, ";
        } else {
          values += "?, ";
          prmData.add(val);
        }
      }

      if (k == maxField) {
        values += " " + DefineReport.ValUpdkbn.NML.getVal() + " "; // 更新区分：
        values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + " ";// 送信区分：
        values += ", '" + userId + "'  "; // オペレーター：
        values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values +=
            "FROM INATK.TOKBM WHERE MOYSKBN = " + prmData.get(0) + " AND MOYSSTDT = " + prmData.get(1) + " AND MOYSRBAN = " + prmData.get(2) + " AND BMNNO = " + prmData.get(3) + " LIMIT 1 ) T2 ) ";
        values += ", CURRENT_TIMESTAMP  "; // 更新日：
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
    sbSQL.append(" )values ( ");
    sbSQL.append(StringUtils.join(valueData, ",") + " ");


    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    }

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
    if (StringUtils.isEmpty(bmno)) {
      sqlWhere += "BMNNO=null ";
    } else {
      sqlWhere += "BMNNO=? ";
      paramData.add(bmno);
    }


    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.TOKBM_TJTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    }

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
        prmData.add(bmno);
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
        prmData.add(bmno);
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


      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

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
            values += " ?, ";
            prmData.add(bmno);
          } else if (key.equals("F4")) {
            kanrino = String.valueOf(Integer.valueOf(kanrino) + 1);
            values += "?, ";
            prmData.add(kanrino);
          } else if (StringUtils.isEmpty(val)) {
            values += "null, ";
          } else {
            values += "?, ";
            prmData.add(val);
          }

          if (k == maxField) {
            values += " " + DefineReport.Values.SENDFLG_UN.getVal() + " ";// 送信区分：
            values += ", '" + userId + "'  "; // オペレーター：
            values += ",  (SELECT * FROM (SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHN WHERE MOYSKBN = " + moyskbn + " AND MOYSSTDT = " + moysstdt
                + " AND MOYSRBAN = " + moysrban + " AND BMNNO = " + bmno + " AND KANRINO = " + prmData.get(((i + 1) * 7) - 1) + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
            values += "T" + i + "    ) "; // 登録日：
            values += ", CURRENT_TIMESTAMP  "; // 更新日：
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }


          // BM商品販売日更新用
          if (key.equals(TOKBMSHNLayout.SHNCD.getId())) {
            hbShn = val;
          }

          // BM商品販売日用配列作成
          if (key.equals("F4")) {
            if (!StringUtils.isEmpty(data.optString(TOKBMLayout.HBSTDT.getId())) && !StringUtils.isEmpty(data.optString(TOKBMLayout.HBEDDT.getId()))) {
              hbStDt = data.optString(TOKBMLayout.HBSTDT.getId());
              days = getHbDays(hbStDt, data.optString(TOKBMLayout.HBEDDT.getId()));
            } else {
              days = getHbDays(hbStDt, hbEdDt);
            }
          } else {
            days = -1;
          }

          String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

          for (int amount = 0; amount <= days; amount++) {

            String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
            HashMap<String, String> moyCdArrMap = getArrMap(moyskbn, dataG.optString(TOKBMSHNLayout.SHNCD.getId()), hbDt, "0");
            HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString(TOKBMSHNLayout.SHNCD.getId()), hbDt, "1");
            HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString(TOKBMSHNLayout.SHNCD.getId()), hbDt, "2");

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
                for (int digit = 0; digit < (3 - bmno.length()); digit++) {
                  space += " ";
                }
                bmnNoArrMap.put(tenCd, space + bmno);
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

            moyCd_Arr = new ReportJU012Dao(JNDIname).spaceArr(moyCd_Arr, 10);

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

            bmnNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(bmnNo_Arr, 3);

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

            kanriNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(kanriNo_Arr, 4);

            // 販売日、催しコード配列、管理番号配列、BM番号配列
            // valuesHb += String.valueOf(amount + 1);
            if (StringUtils.isEmpty(hbShn)) {
              valuesHb += " null, ";
            } else {
              valuesHb += " ?, ";
            }
            valuesHb += " ?, ?, ?, ?, ";
            if (!StringUtils.isEmpty(hbShn)) {
              prmDataHb.add(hbShn);
            }
            prmDataHb.add(CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount)));
            prmDataHb.add(moyCd_Arr);
            prmDataHb.add(bmnNo_Arr);
            prmDataHb.add(kanriNo_Arr);
            valuesHb += " '" + userId + "'  "; // オペレーター：

            if (!moyskbn.equals("3")) {
              valuesHb += ",  ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.TOKBM_SHNHBDT WHERE SHNCD = "
                  + datashn.optJSONObject(i).optString("F2") + " AND HBDT = " + hbDt + " LIMIT 1 ) T" + i + "    ) "; // 登録日：
              // 催し区分=3ならBM商品販売日_本部個特
            } else {
              valuesHb += ",  ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT INATK.TOKBM_SHNHBDT_HTK WHERE SHNCD = "
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
        sbSQL.append(",SENDFLG");// 送信区分：
        sbSQL.append(",OPERATOR "); // オペレーター：
        sbSQL.append(",ADDDT "); // 登録日：
        sbSQL.append(",UPDDT "); // 更新日：
        sbSQL.append(") VALUES");
        sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");


        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
        }

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


        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

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
      paramData.add(bmno);

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

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

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
      sbSQL.append(" ) VALUES(?,?,?,?,? ");
      sbSQL.append(", '" + userId + "'  "); // オペレーター：
      sbSQL.append(",  (SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0  THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT FROM INATK.SYSBM_KANRINO WHERE MOYSKBN = " + paramData.get(0) + " AND MOYSSTDT = "
          + paramData.get(1) + " AND MOYSRBAN = " + paramData.get(2) + " AND BMNO = " + paramData.get(3) + " LIMIT 1 ) T2 ) "); // 登録日：
      sbSQL.append(", CURRENT_TIMESTAMP  "); // 更新日：

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

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
      }

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

    // BM番号
    if (StringUtils.isEmpty(data.optString("F6"))) {
      sqlWhere += "BMNNO=null ";
    } else {
      sqlWhere += "BMNNO=? ";
      paramData.add(data.optString("F6"));
    }

    // BM催し送信DELETE(論理削除)
    sbSQL.append("UPDATE INATK.TOKBM ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());// 送信区分：
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("BM催し送信");

    // BM商品販売日用配列作成
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();
    String moyCd_Arr = "";
    String kanriNo_Arr = "";
    String bmnNo_Arr = "";
    int addCnt = 37;
    int delCnt = 27;

    for (int i = 0; i < 10; i++) {
      tenCdAdds.add(i, data.optString("F" + addCnt));
      tenCdDels.add(i, data.optString("F" + delCnt));
      addCnt++;
      delCnt++;
    }

    // 対象店を取得
    Set<Integer> tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(data.optString("F17") // 部門コード
        , moyskbn // 催し区分
        , moysstdt // 催し開始日
        , moysrban // 催し連番
        , data.optString("F18") // 対象ランク№
        , data.optString("F19") // 除外ランク№
        , tenCdAdds // 対象店
        , tenCdDels // 除外店
    );

    String hbStDt = data.optString("F47");
    int days = getHbDays(hbStDt, data.optString("F48"));

    for (int i = 0; i < dataArrayG.size(); i++) {
      JSONObject dataG = dataArrayG.getJSONObject(i);

      for (int amount = 0; amount <= days; amount++) {

        String hbDt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
        HashMap<String, String> moyCdArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "0");
        HashMap<String, String> bmnNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "1");
        HashMap<String, String> kanriNoArrMap = getArrMap(moyskbn, dataG.optString("F2"), hbDt, "2");

        Iterator<Integer> ten = tencds.iterator();

        for (int j = 0; j < tencds.size(); j++) {

          String tenCd = String.valueOf(ten.next());

          // 催しコードを追加
          if (moyCdArrMap.containsKey(tenCd)) {
            moyCdArrMap.put(tenCd, "          ");
          }

          // BM番号を追加
          if (bmnNoArrMap.containsKey(tenCd)) {
            bmnNoArrMap.put(tenCd, "   ");
          }

          // 管理番号を追加
          if (kanriNoArrMap.containsKey(tenCd)) {
            kanriNoArrMap.put(tenCd, "    ");
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

        moyCd_Arr = new ReportJU012Dao(JNDIname).spaceArr(moyCd_Arr, 10);

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

        bmnNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(bmnNo_Arr, 3);

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

        kanriNo_Arr = new ReportJU012Dao(JNDIname).spaceArr(kanriNo_Arr, 4);

        // 初期化
        sbSQL = new StringBuffer();
        sqlWhere = "";
        paramData = new ArrayList<>();

        // 催しコード配列が空だったらDELETE
        if (StringUtils.isEmpty(moyCd_Arr.trim())) {
          sbSQL.append("DELETE FROM ");
        } else {
          sbSQL.append("UPDATE ");
        }

        // 催し区分=1or2ならBM商品販売日
        if (!moyskbn.equals("3")) {
          sbSQL.append("INATK.TOKBM_SHNHBDT ");
          // 催し区分=3ならBM商品販売日_本部個特
        } else {
          sbSQL.append("INATK.TOKBM_SHNHBDT_HTK ");
        }

        if (!StringUtils.isEmpty(moyCd_Arr.trim())) {
          sbSQL.append("SET ");
          sbSQL.append("MOYCD_ARR=? ");
          sbSQL.append(",BMNNO_ARR=? ");
          sbSQL.append(",KANRINO_ARR=? ");
          sbSQL.append(",OPERATOR='" + userId + "' ");
          sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
          paramData.add(moyCd_Arr);
          paramData.add(bmnNo_Arr);
          paramData.add(kanriNo_Arr);
        }

        // 商品コード
        if (StringUtils.isEmpty(dataG.optString("F2"))) {
          sqlWhere += "SHNCD=null AND ";
        } else {
          sqlWhere += "SHNCD=? AND ";
          paramData.add(dataG.optString("F2"));
        }

        // 販売日
        sqlWhere += "HBDT=? ";
        paramData.add(hbDt);

        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere);

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(paramData);
        lblList.add("BM商品販売日");
      }
    }
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
    String szBMNNO = getMap().get("BMNNO"); // B/M番号

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
      sqlWhere += "T1.MOYSKBN=null AND ";
    } else {
      sqlWhere += "T1.MOYSKBN=? AND ";
      paramData.add(szMoysKbn);
    }

    if (StringUtils.isEmpty(szMoysStDt)) {
      sqlWhere += "T1.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "T1.MOYSSTDT=? AND ";
      paramData.add(szMoysStDt);
    }

    if (StringUtils.isEmpty(szMoysRban)) {
      sqlWhere += "T1.MOYSRBAN=null AND ";
    } else {
      sqlWhere += "T1.MOYSRBAN=? AND ";
      paramData.add(szMoysRban);
    }

    if (StringUtils.isEmpty(szBMNNO)) {
      sqlWhere += "T1.BMNNO=null AND ";
    } else {
      sqlWhere += "T1.BMNNO=? AND ";
      paramData.add(szBMNNO);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("T1.TENATSUK_ARR ");
    sbSQL.append(",T1.BMNCD_RANK ");
    sbSQL.append(",T2.TJFLG ");
    sbSQL.append(",T2.TENCD ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKBM T1 LEFT JOIN INATK.TOKBM_TJTEN T2 ON T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN AND T1.BMNNO=T2.BMNNO ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append(" T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append(" ORDER BY T2.TJFLG,T2.TENCD");

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    String tenAtuk_Arr = "";
    String bmncdRank = "";
    JSONArray tenCdAdds = new JSONArray();
    JSONArray tenCdDels = new JSONArray();
    Set<Integer> tencds = new TreeSet<>();

    for (int i = 0; i < dbDatas.size(); i++) {
      data = dbDatas.getJSONObject(i);

      String tjflg = data.containsKey("TJFLG") ? data.optString("TJFLG") : "";
      String tencd = data.containsKey("TENCD") ? data.optString("TENCD") : "";

      // 除外店
      if (tjflg.equals("0")) {

        tenDelCnt++;
        sqlTjTen += "," + tencd + " AS TENCD_DEL_" + tenDelCnt;
        tenCdDels.add(tencd);

        // 対象店
      } else if (tjflg.equals("1")) {

        // 除外店を10個出力する
        if (tenDelCnt < 10) {

          for (int j = tenDelCnt; j < 10; j++) {

            tenDelCnt++;
            sqlTjTen += ",'' AS TENCD_DEL_" + (tenDelCnt + 1);
          }

        }

        tenAddCnt++;
        sqlTjTen += "," + tencd + " AS TENCD_ADD_" + tenAddCnt;
        tenCdAdds.add(tencd);
      }

      if (i + 1 == dbDatas.size()) {
        tenAtuk_Arr = bmncdRank + "_" + data.optString("TENATSUK_ARR");
        bmncdRank = data.optString("BMNCD_RANK");

        if (!StringUtils.isEmpty(tenAtuk_Arr)) {
          tencds = new ReportBM015Dao(JNDIname).getTenCdAddArr(tenAtuk_Arr, tenCdDels, tenCdAdds);
          tenAtuk_Arr = "";
        }
      }
    }

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
    tenAtuk_Arr = bmncdRank + "_" + new ReportJU012Dao(JNDIname).spaceArr(tenAtuk_Arr, 1);

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
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(CD.HBSTDT,'%Y%m%d'),'%y/%m/%d') MOYSSTDT "); // F3 : 催し開始日
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(CD.HBEDDT,'%Y%m%d'),'%y/%m/%d') MOYSEDDT "); // F4 : 催し終了日
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(CD.PLUSDDT,'%Y%m%d'),'%y/%m/%d') PLUSDDT "); // F5 : PLU配信日
    sbSQL.append(",BM.BMNNO "); // F6 : B/M番号
    sbSQL.append(",BM.BMNMAN "); // F7 : B/M名称（ｶﾅ）
    sbSQL.append(",BM.BMTYP "); // F8 : B/Mタイプ
    sbSQL.append(",BM.BMNMKN "); // F9 : B/M名称（漢字）
    sbSQL.append(",BM.BAIKAAM "); // F10 : 1個売り総売価
    sbSQL.append(",BM.BD_KOSU1 "); // F11 : 個数総売価（１）
    sbSQL.append(",BM.BD_BAIKAAN1 "); // F12 : 金額
    sbSQL.append(",BM.BD_KOSU2 "); // F13 : 個数総売価（２）
    sbSQL.append(",BM.BD_BAIKAAN2 "); // F14 : 金額
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(BM.HBSTDT,'%Y%m%d'),'%y/%m/%d') HBSTDT "); // F15 : 販売開始日
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(BM.HBEDDT,'%Y%m%d'),'%y/%m/%d') HBEDDT "); // F16 : 販売終了日
    sbSQL.append(",BM.BMNCD_RANK "); // F17 : 部門コード
    sbSQL.append(",BM.RANKNO_ADD "); // F18 : 対象店ランク№
    sbSQL.append(",BM.RANKNO_DEL "); // F19 : 除外店ランク№
    sbSQL.append(",BM.MOYSKBN "); // F20 : 催し区分
    sbSQL.append(",BM.MOYSSTDT "); // F21 : 催し開始日
    sbSQL.append(",BM.MOYSRBAN "); // F22 : 催し連番
    sbSQL.append(",BM.OPERATOR "); // F23 : オペレーター
    sbSQL.append(",DATE_FORMAT(BM.ADDDT,'%y/%m/%d') AS ADDDT "); // F24 : 登録日
    sbSQL.append(",DATE_FORMAT(BM.UPDDT,'%y/%m/%d') AS UPDDT "); // F25 : 更新日
    sbSQL.append(",DATE_FORMAT(BM.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F26 : 更新日時
    sbSQL.append(sqlTjTen); // F27～F46 : 対象店・除外店
    sbSQL.append(",BM.HBSTDT AS HDN_HBSTDT "); // F47 : 元販売開始日
    sbSQL.append(",BM.HBEDDT AS HND_HBEDDT "); // F48 : 元販売終了日
    sbSQL.append(",'" + tenAtuk_Arr + "' AS TENATSUK_ARR "); // F49 : 店扱いフラグ配列
    sbSQL.append(" FROM ");
    sbSQL.append("INATK.TOKMOYCD CD LEFT JOIN INATK.TOKBM BM ON ");
    sbSQL.append("BM.MOYSKBN=CD.MOYSKBN AND ");
    sbSQL.append("BM.MOYSSTDT=CD.MOYSSTDT AND ");
    sbSQL.append("BM.MOYSRBAN=CD.MOYSRBAN AND ");
    if (StringUtils.isEmpty(szBMNNO)) {
      sbSQL.append("BM.BMNNO=null ");
    } else {
      sbSQL.append("BM.BMNNO=? ");
      paramData.add(szBMNNO);
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
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
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

  /** BM催し送信 */
  public enum TOKBMLayout implements MSTLayout {

    /** 催しコード */
    MOYSCD(1, "MOYSCD", "VARCHAR(10)", "催しコード"),
    /** 催し区分 */
    MOYSKBN(2, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(3, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(4, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** BM番号 */
    BMNNO(5, "BMNNO", "SMALLINT", "BM番号"),
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
    private TOKBMLayout(Integer no, String col, String typ, String txt) {
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

  /** BM催し送信_対象除外店 */
  public enum TOKBMTJTENLayout implements MSTLayout {

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
    private TOKBMTJTENLayout(Integer no, String col, String typ, String txt) {
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
  public enum TOKBMSHNLayout implements MSTLayout {

    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 原価 */
    GENKAAM(2, "GENKAAM", "DECIMAL(8,2)", "原価");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKBMSHNLayout(Integer no, String col, String typ, String txt) {
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

  /** CSV取込トラン_特売ヘッダレイアウト(正マスタとの差分) */
  public enum CSVTOKLayout implements MSTLayout {
    /** SEQ */
    SEQ(1, "SEQ", "INTEGER"),
    /** 入力番号 */
    INPUTNO(2, "INPUTNO", "INTEGER"),
    /** エラーコード */
    ERRCD(3, "ERRCD", "SMALLINT"),
    /** エラー箇所 */
    ERRFLD(4, "ERRFLD", "VARCHAR(100)"),
    /** エラー値 */
    ERRVL(5, "ERRVL", "VARCHAR(100)"),
    /** エラーテーブル名 */
    ERRTBLNM(6, "ERRTBLNM", "VARCHAR(100)"),
    /** CSV登録区分 */
    CSV_UPDKBN(7, "CSV_UPDKBN", "CHARACTER(1)"),
    /** 催し区分 */
    MOYSKBN(8, "MOYSKBN", "SMALLINT"),
    /** 催し開始日 */
    MOYSSTDT(9, "MOYSSTDT", "INTEGER"),
    /** 催し連番 */
    MOYSRBAN(10, "MOYSRBAN", "SMALLINT"),
    /** BM番号 */
    BMNNO(11, "BMNNO", "SMALLINT"),
    /** BM名称（ｶﾅ） */
    BMNMAN(12, "BMNMAN", "VARCHAR(20)"),
    /** 販売開始日 */
    HBSTDT(13, "HBSTDT", "INTEGER"),
    /** 販売終了日 */
    HBEDDT(14, "HBEDDT", "INTEGER"),
    /** 商品コード */
    SHNCD(15, "SHNCD", "CHARACTER(14)");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private CSVTOKLayout(Integer no, String col, String typ) {
      this.no = no;
      this.col = col;
      this.typ = typ;
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

    /** @return tbl 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return col Id */
    public String getId2() {
      return "F" + Integer.toString(no + MSTSHNLayout.values().length);
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
      if (typ.indexOf("DATE") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }
  }
}
