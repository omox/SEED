package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
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
public class ReportHT002Dao extends ItemDao {

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
  public ReportHT002Dao(String JNDIname) {
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
    request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();
    /*
     * JSONArray msgList = this.checkDel(map, userInfo, sysdate);
     *
     * if(msgList.size() > 0){ option.put(MsgKey.E.getKey(), msgList); return option; }
     *
     * // 更新処理 try { option = this.deleteData(map, userInfo, sysdate); } catch (Exception e) { e.printStackTrace(); option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal())); }
     */
    option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    return option;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

    // パラメータ確認
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_TENSU")); // 更新情報
    String week = map.get("WEEK"); // 処理曜日

    // 格納用変数
    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArrayT.size() == 0 || dataArrayT.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 処理曜日が水曜日の場合エラー
    if (week.equals("4")) {
      msg.add(mu.getDbMessageObj("EX1038", new String[] {}));
      return msg;
    }

    // 空白の入力は無効
    for (int i = 0; i < dataArrayT.size(); i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (dataT.isEmpty()) {
        continue;
      }

      if (StringUtils.isEmpty(dataT.optString("F4")) || StringUtils.isEmpty(dataT.optString("F5")) || StringUtils.isEmpty(dataT.optString("F6")) || StringUtils.isEmpty(dataT.optString("F7"))
          || StringUtils.isEmpty(dataT.optString("F8")) || StringUtils.isEmpty(dataT.optString("F9")) || StringUtils.isEmpty(dataT.optString("F10"))) {
        msg.add(mu.getDbMessageObj("E30024", new String[] {"空白"}));
        return msg;
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    String hstengpcd = map.get("HSTENGPCD"); // 配送店グループコード

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty() || hstengpcd.isEmpty() || hstengpcd == null) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    if (StringUtils.isEmpty(data.optString("F1"))) {
      sqlWhere += "HSGPCD=null AND ";
    } else {
      sqlWhere += "HSGPCD=? AND ";
      paramData.add(data.optString("F1"));
    }

    if (StringUtils.isEmpty(hstengpcd)) {
      sqlWhere += "TENGPCD=null";
    } else {
      sqlWhere += "TENGPCD=?";
      paramData.add(hstengpcd);
    }

    // エリア別配送パターンテーブルにレコードが存在する場合エラー
    sbSQL.append("SELECT ");
    sbSQL.append("HSGPCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTAREAHSPTN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() >= 1) {
      // エリア別配送パターンに存在している
      msg.add(mu.getDbMessageObj("E11036", new String[] {}));
      return msg;
    }

    return msg;
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
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_TENSU")); // 更新情報
    String ten = map.get("TEN"); // ログイン店舗

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<>();
    JSONArray msg = new JSONArray();

    // 正規定量_店別数量INSERT/UPDATE処理
    createSqlTenSu(map, userInfo);

    // 排他チェック実行
    for (int i = 0; i < dataArrayT.size(); i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (dataT.isEmpty()) {
        continue;
      }

      /* 正規定量_店別数量チェック */
      targetParam = new ArrayList<>();
      targetTable = "INATK.HATSTR_TEN";
      targetWhere = " SHNCD=? AND BINKBN=? AND TENCD=? AND UPDKBN=?";
      targetParam.add(dataT.optString("F1"));
      targetParam.add(dataT.optString("F3"));
      targetParam.add(ten);
      targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, dataT.optString("F11"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }

      /* 正規定量_商品チェック */
      targetParam = new ArrayList<>();
      targetTable = "INATK.HATSTR_SHN";
      targetWhere = " SHNCD=? AND BINKBN=? AND UPDKBN=?";
      targetParam.add(dataT.optString("F1"));
      targetParam.add(dataT.optString("F3"));
      targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, dataT.optString("F12"))) {
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
   * 配送グループマスタ、配送店グループマスタDELETE(倫理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlHs(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 削除データ検索用コード
    String hstengpcd = map.get("HSTENGPCD"); // 配送店グループコード

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<>();

    if (StringUtils.isEmpty(data.optString("F1"))) {
      sqlWhere += "null";
    } else {
      sqlWhere += "?";
      paramData.add(data.optString("F1"));
    }

    // 配送店グループマスタの件数チェック
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append(" HSGPCD ");
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTHSTENGP ");
    sbSQL.append("WHERE ");
    sbSQL.append("HSGPCD=" + sqlWhere + " AND ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INAMS.MSTHSGP ");
    sbSQL.append("SET ");
    // 1件のみの場合は対象の配送グループを論理削除
    if (dbDatas.size() == 1) {
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal() + ",");
    }
    sbSQL.append("OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append("HSGPCD = " + sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("配送グループマスタ");

    sqlWhere = "";
    paramData = new ArrayList<>();

    if (StringUtils.isEmpty(data.optString("F1"))) {
      sqlWhere += "HSGPCD=null AND ";
    } else {
      sqlWhere += "HSGPCD=? AND ";
      paramData.add(data.optString("F1"));
    }

    if (StringUtils.isEmpty(hstengpcd)) {
      sqlWhere += "TENGPCD=null";
    } else {
      sqlWhere += "TENGPCD=?";
      paramData.add(hstengpcd);
    }

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INAMS.MSTHSTENGP ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("配送店グループマスタ");

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INAMS.MSTHSGPTEN ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("配送グループ店マスタ");

    return sbSQL.toString();
  }

  /**
   * 正規定量_店別数量INSERT/UPDATE処理
   *
   * @param map
   * @param userInfo
   */
  public String createSqlTenSu(HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_TENSU")); // 更新情報
    String ten = map.get("TEN"); // ログイン店舗

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 10; // Fxxの最大値
    int len = dataArrayT.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);



          if (!ArrayUtils.contains(new String[] {"F2"}, key)) {
            String val = dataT.optString(key);
            if (key.equals("F3")) {
              values += " ?, ?,";
              prmData.add(val);
              prmData.add(ten);
            } else if (StringUtils.isEmpty(val)) {
              values += " null,";
            } else {
              values += " ?,";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
            values += ", 0  ";
            values += ", '" + userId + "'  ";
            values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
            values += "FROM INATK.HATSTR_TEN WHERE SHNCD = " + dataArrayT.optJSONObject(i).optString("F1") + " AND BINKBN = " + dataArrayT.optJSONObject(i).optString("F3") + " AND TENCD = " + ten
                + " )T" + i + " )";
            values += ", CURRENT_TIMESTAMP ";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        sbSQL.append("REPLACE INTO INATK.HATSTR_TEN  ( ");
        sbSQL.append("SHNCD"); // 商品コード
        sbSQL.append(",BINKBN"); // 便区分
        sbSQL.append(",TENCD"); // 店コード
        sbSQL.append(",SURYO_MON"); // 店別数量_月
        sbSQL.append(",SURYO_TUE"); // 店別数量_火
        sbSQL.append(",SURYO_WED"); // 店別数量_水
        sbSQL.append(",SURYO_THU"); // 店別数量_木
        sbSQL.append(",SURYO_FRI"); // 店別数量_金
        sbSQL.append(",SURYO_SAT"); // 店別数量_土
        sbSQL.append(",SURYO_SUN"); // 店別数量_日
        sbSQL.append(",UPDKBN"); // 更新区分：
        sbSQL.append(",SENDFLG "); // 送信区分：未送信
        sbSQL.append(",OPERATOR "); // オペレーター：
        sbSQL.append(",ADDDT "); // 登録日：
        sbSQL.append(",UPDDT "); // 更新日：
        sbSQL.append(") VALUES");
        sbSQL.append(" " + StringUtils.join(valueData, ",") + " ");


        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("/*" + this.getClass().getName() + "[sql]*/" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("正規定量_店別数量");

        // クリア
        prmData = new ArrayList<>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szShncd = getMap().get("SHNCD"); // 商品コード
    String szBumon = getMap().get("BUMON"); // 部門
    String szDaibun = getMap().get("DAIBUN"); // 大分類
    String szChubun = getMap().get("CHUBUN"); // 中分類
    String szTencd = getMap().get("TEN"); // 店コード

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlWhere_ks = "";
    ArrayList<String> paramData = new ArrayList<>();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();
    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    // 商品コードが未入力の場合は部門で検索
    if (StringUtils.isEmpty(szShncd)) {

      if (StringUtils.isEmpty(szBumon) || szBumon.equals("-1")) {
        sqlWhere += "BMNCD=null ";
      } else {
        sqlWhere += "SUBSTR(SHNCD,1,2)=? AND BMNCD=? ";
        paramData.add(szBumon);
        paramData.add(String.valueOf(Integer.valueOf(szBumon.substring(0, 2))));
      }

      if (StringUtils.isEmpty(szDaibun) || szDaibun.equals("-1")) {
        sqlWhere += "";
      } else {
        sqlWhere += "AND DAICD=? ";
        paramData.add(String.valueOf(Integer.valueOf(szDaibun.substring(0, 2))));
      }

      if (StringUtils.isEmpty(szChubun) || szChubun.equals("-1")) {
        sqlWhere += "";
      } else {
        sqlWhere += "AND CHUCD=? ";
        paramData.add(String.valueOf(Integer.valueOf(szChubun.substring(0, 2))));
      }
    } else {
      sqlWhere += "SHNCD=? ";
      paramData.add(szShncd);
    }
    
    
 // 商品コードが未入力の場合は部門で検索
    if (StringUtils.isEmpty(szShncd)) {

      if (StringUtils.isEmpty(szBumon) || szBumon.equals("-1")) {
        sqlWhere_ks += "WHERE BMNCD=null ";
      } else {
        sqlWhere_ks += "WHERE BMNCD=? ";
        sqlWhere_ks += "AND TENCD=? ";
        paramData.add(String.valueOf(Integer.valueOf(szBumon.substring(0, 2))));
        paramData.add(szTencd);
      }
    } else {
      sqlWhere_ks += "WHERE SHNCD=? ";
      sqlWhere_ks += "AND TENCD=? ";
      paramData.add(szShncd);
      paramData.add(szTencd);
    }
    
    sbSQL.append("WITH MS AS( ");
    sbSQL.append("SELECT ");
    sbSQL.append(" SHNCD ");
    sbSQL.append(",SHNKN ");
    sbSQL.append(",UPDKBN ");
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTSHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("), KS AS( ");
    sbSQL.append("SELECT DISTINCT ");
    sbSQL.append("SHNCD ");
    sbSQL.append(",TENCD ");
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTKSPAGE ");
    sbSQL.append(sqlWhere_ks);
    sbSQL.append(") ");
    sbSQL.append("SELECT ");
    sbSQL.append("TN.SHNCD "); // F1 : 商品コード
    sbSQL.append(",MS.SHNKN "); // F2 : 商品名（漢字）
    sbSQL.append(",TN.BINKBN "); // F3 : 便区分
    sbSQL.append(",CASE WHEN TN.SURYO_MON IS NULL THEN 0 ELSE TN.SURYO_MON END AS SURYO_MON "); // F4 : 数量＿月
    sbSQL.append(",CASE WHEN TN.SURYO_TUE IS NULL THEN 0 ELSE TN.SURYO_TUE END AS SURYO_TUE "); // F5 : 数量＿火
    sbSQL.append(",CASE WHEN TN.SURYO_WED IS NULL THEN 0 ELSE TN.SURYO_WED END AS SURYO_WED "); // F6 : 数量＿水
    sbSQL.append(",CASE WHEN TN.SURYO_THU IS NULL THEN 0 ELSE TN.SURYO_THU END AS SURYO_THU "); // F7 : 数量＿木
    sbSQL.append(",CASE WHEN TN.SURYO_FRI IS NULL THEN 0 ELSE TN.SURYO_FRI END AS SURYO_FRI "); // F8 : 数量＿金
    sbSQL.append(",CASE WHEN TN.SURYO_SAT IS NULL THEN 0 ELSE TN.SURYO_SAT END AS SURYO_SAT "); // F9 : 数量＿土
    sbSQL.append(",CASE WHEN TN.SURYO_SUN IS NULL THEN 0 ELSE TN.SURYO_SUN END AS SURYO_SUN "); // F10 : 数量＿日
    sbSQL.append(",TN.OPERATOR "); // F11 : オペレーター
    sbSQL.append(",DATE_FORMAT(TN.ADDDT,'%y/%m/%d') AS ADDDT "); // F12 : 登録日
    sbSQL.append(",DATE_FORMAT(TN.UPDDT,'%y/%m/%d') AS UPDDT "); // F13 : 更新日
    sbSQL.append(",DATE_FORMAT(TN.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F14 : 更新日時
    sbSQL.append(",DATE_FORMAT(SN.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT2 "); // F15 : 更新日時（正規定量商品）
    sbSQL.append(", SN.TSKBN_MON "); // F16 : 訂正区分_月
    sbSQL.append(", SN.TSKBN_TUE "); // F17 : 訂正区分_火
    sbSQL.append(", SN.TSKBN_WED "); // F18 : 訂正区分_水
    sbSQL.append(", SN.TSKBN_THU "); // F19 : 訂正区分_木
    sbSQL.append(", SN.TSKBN_FRI "); // F20 : 訂正区分_金
    sbSQL.append(", SN.TSKBN_SAT "); // F21 : 訂正区分_土
    sbSQL.append(", SN.TSKBN_SUN "); // F22 : 訂正区分_日
    sbSQL.append(", ROW_NUMBER() OVER(ORDER BY TN.SHNCD, TN.BINKBN, DATE_FORMAT(TN.UPDDT, '%Y%m%d%H%i%s%f')) "); // F23 : 行番号
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_TEN TN LEFT JOIN INATK.HATSTR_SHN SN ON TN.SHNCD=SN.SHNCD AND TN.BINKBN=SN.BINKBN ");
    // if(StringUtils.isEmpty(szShncd)){
    sbSQL.append(",KS ");
    // }
    sbSQL.append(",MS ");
    sbSQL.append("WHERE ");
    sbSQL.append("TN.TENCD=? AND ");
    sbSQL.append("TN.UPDKBN=0 AND ");
    paramData.add(szTencd);
    sbSQL.append("MS.UPDKBN=0 AND ");
    // if(StringUtils.isEmpty(szShncd)){
    sbSQL.append("TN.SHNCD=KS.SHNCD AND ");
    sbSQL.append("TN.TENCD=KS.TENCD AND ");
    // }
    sbSQL.append("TN.SHNCD=MS.SHNCD ");
    sbSQL.append("ORDER BY TN.SHNCD,TN.BINKBN,HDN_UPDDT");

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
}
