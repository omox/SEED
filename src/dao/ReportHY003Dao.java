package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportHY003Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportHY003Dao(String JNDIname) {
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
      // option = this.updateDataTest(map, userInfo, sysdate);

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

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szKkkcd = getMap().get("KKKCD"); // 企画No
    String szShncd = getMap().get("SHNCD"); // 商品No
    String szShoridt = getMap().get("SHORIDT"); // 処理日付
    getMap().get("NNDT");
    String tencd = userInfo.getTenpo(); // 担当店舗

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    // パラメータ確認
    // 必須チェック
    if (szKkkcd == null || szShncd == null || tencd == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT('20' || right ('0' || T3.NNDT, 6), '%y/%m/%d'), '%y/%m/%d') || W1.JWEEK");
    sbSQL.append(",YHT.HTSU");
    sbSQL.append(",YHT_.HTSU2");
    sbSQL.append(",COALESCE(YHT.HTSU, 0) + COALESCE(YHT_.HTSU2, 0)");
    sbSQL.append(",case when T2.NGFLG = 1 or T2.TENIEDDT < ? then 1 else 0 end as INPUTFLG");
    paramData.add(szShoridt);
    sbSQL.append(",DATE_FORMAT(T3.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT");
    sbSQL.append(",T3.NNDT as HDN_NNDT");
    sbSQL.append(" from");
    sbSQL.append(" INATK.HATYH_KKK T1");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.HATYH_SHN T2");
    sbSQL.append(" on T1.KKKCD = T2.KKKCD");
    sbSQL.append(" and T2.UPDKBN = 0");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.HATYH_NNDT T3");
    sbSQL.append(" on T3.KKKCD = T2.KKKCD");
    sbSQL.append(" and T3.SHNCD = T2.SHNCD");
    sbSQL.append(" left join");
    sbSQL.append(" (select");
    sbSQL.append(" KKKCD");
    sbSQL.append(", SHNCD");
    sbSQL.append(", NNDT");
    sbSQL.append(", SUM(HTSU) as HTSU");
    sbSQL.append(" from INATK.HATYH_TEN");
    sbSQL.append(" where INPUTDT < ?");
    paramData.add(szShoridt);
    sbSQL.append(" and TENCD = ?");
    paramData.add(tencd);
    sbSQL.append(" group by KKKCD, SHNCD, NNDT) YHT");
    sbSQL.append(" on YHT.KKKCD = T3.KKKCD");
    sbSQL.append(" and YHT.SHNCD = T3.SHNCD");
    sbSQL.append(" and YHT.NNDT = T3.NNDT");
    sbSQL.append(" left join");
    sbSQL.append(" (select");
    sbSQL.append(" KKKCD");
    sbSQL.append(" , SHNCD");
    sbSQL.append(" , NNDT");
    sbSQL.append(" , HTSU as HTSU2");
    sbSQL.append(" from INATK.HATYH_TEN");
    sbSQL.append(" where");
    sbSQL.append(" INPUTDT = ?");
    paramData.add(szShoridt);
    sbSQL.append(" and TENCD = ?");
    paramData.add(tencd);
    sbSQL.append(" ) YHT_ on YHT_.KKKCD = T3.KKKCD");
    sbSQL.append(" and YHT_.SHNCD = T3.SHNCD");
    sbSQL.append(" and YHT_.NNDT = T3.NNDT");
    sbSQL.append(" left outer join WEEK W1");
    sbSQL.append(" on  DAYOFWEEK(DATE_FORMAT('20' || right ('0' || T3.NNDT, 6), '%Y%m%d'))  = W1.CWEEK");
    sbSQL.append(" where");
    sbSQL.append(" T3.KKKCD = ?");
    paramData.add(szKkkcd);
    sbSQL.append(" and T3.SHNCD = ?");
    paramData.add(szShncd);
    sbSQL.append(" and T1.UPDKBN = 0");
    sbSQL.append(" order by T3.NNDT");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szKkkcd = map.get("KKKCD"); // 企画No
    String szShncd = map.get("SHNCD"); // 商品No
    String szShoridt = map.get("SHORIDT"); // 処理日付

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" select");
    sbSQL.append(" YHS.CATALGNO as F1");
    sbSQL.append(", left (TRIM(YHS.SHNCD), 4) || '-' || SUBSTR(TRIM(YHS.SHNCD), 5) as F2");
    sbSQL.append(", SHN.SHNKN as F3");
    sbSQL.append(", SHN.RG_GENKAAM as F4");
    sbSQL.append(", SHN.RG_BAIKAAM as F5");
    sbSQL.append(", CAST(case");
    sbSQL.append("  when SHN.ZEIKBN = 3 then (");
    sbSQL.append("   case");
    sbSQL.append("   when BMN.ZEIRTHENKODT <= " + szShoridt + " and BMN.ZEIKBN = 0 then ROUND(CAST(SHN.RG_BAIKAAM AS DECIMAL) + (CAST(SHN.RG_BAIKAAM AS DECIMAL) * (ZRTB.ZEIRT / 100)), 0)");
    sbSQL.append("   when BMN.ZEIRTHENKODT > " + szShoridt + " and BMN.ZEIKBN = 0 then ROUND(CAST(SHN.RG_BAIKAAM AS DECIMAL) + (CAST(SHN.RG_BAIKAAM AS DECIMAL) * (ZRTB_OLD.ZEIRT / 100)), 0) ");
    sbSQL.append("   when BMN.ZEIKBN = 1 or BMN.ZEIKBN = 2 then SHN.RG_BAIKAAM end)");
    sbSQL.append("  when SHN.ZEIKBN = 1 or SHN.ZEIKBN = 2 then SHN.RG_BAIKAAM");
    sbSQL.append("  when SHN.ZEIKBN = 0 then (");
    sbSQL.append("   case");
    sbSQL.append("   when SHN.ZEIRTHENKODT <= " + szShoridt + " then ROUND(CAST(SHN.RG_BAIKAAM AS DECIMAL) + (CAST(SHN.RG_BAIKAAM AS DECIMAL) * (ZRTS.ZEIRT / 100)), 0)");
    sbSQL.append("   when SHN.ZEIRTHENKODT > " + szShoridt + " then ROUND(CAST(SHN.RG_BAIKAAM AS DECIMAL) + (CAST(SHN.RG_BAIKAAM AS DECIMAL) * (ZRTS_OLD.ZEIRT / 100)), 0) end) end AS SIGNED ) as F6");
    sbSQL.append(" from INATK.HATYH_KKK YHK");
    sbSQL.append(" left join INATK.HATYH_SHN YHS on YHS.KKKCD = YHK.KKKCD");
    sbSQL.append(" left join INAMS.MSTSHN SHN on SHN.SHNCD = YHS.SHNCD");
    sbSQL.append(" left join INAMS.MSTBMN BMN on SHN.BMNCD = BMN.BMNCD");
    sbSQL.append(" left join INAMS.MSTZEIRT ZRTS on ZRTS.ZEIRTKBN = SHN.ZEIRTKBN");
    sbSQL.append(" left join INAMS.MSTZEIRT ZRTS_OLD on ZRTS_OLD.ZEIRTKBN = SHN.ZEIRTKBN_OLD");
    sbSQL.append(" left join INAMS.MSTZEIRT ZRTB on ZRTB.ZEIRTKBN = BMN.ZEIRTKBN");
    sbSQL.append(" left join INAMS.MSTZEIRT ZRTB_OLD on ZRTB_OLD.ZEIRTKBN = BMN.ZEIRTKBN_OLD");
    sbSQL.append(" where YHS.KKKCD = " + szKkkcd + " and YHS.SHNCD = " + szShncd);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

    JSONArray.fromObject(map.get("DATA"));
    JSONArray.fromObject(map.get("DATA_TEN"));
    map.get("SENDBTNID");

    new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    new MessageUtility();
    new JSONArray();

    // チェック処理
    // 対象件数チェック
    /*
     * if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
     * msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal())); return msg; }
     *
     * // 基本登録情報 JSONObject data = dataArray.getJSONObject(0);
     *
     * // 標準-商品コード // ①商品マスタに無い場合エラー String shncd = data.optString("F2");
     * if(!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), shncd)){ JSONObject o =
     * mu.getDbMessageObj("E11046", new String[]{}); msg.add(o); return msg; }
     *
     * // グリッドエラーチェック：店舗一覧 ArrayList<JSONObject> record = new ArrayList<JSONObject>(); HashSet<String>
     * parameter_ = new HashSet<String>(); for (int i = 0; i < dataArrayTEN.size(); i++) { data =
     * dataArray.getJSONObject(0);
     *
     * // 店舗取得 String val = data.optString("F1"); if(StringUtils.isNotEmpty(val)){ record.add(data); //
     * レコード数 parameter_.add(val); // 店コードード(HashSetにより重複なし状態にする。) }
     *
     * // 標準-店舗コード // ①店舗基本マスタに無い場合エラー String tencd = data.optString("F1");
     * if(!this.checkMstExist(DefineReport.InpText.TENCD.getObj(), tencd)){ JSONObject o =
     * mu.getDbMessageObj("E11046", new String[]{}); msg.add(o); return msg; } } // 重複チェック
     * if(record.size() != parameter_.size()){ JSONObject o = MessageUtility.getDbMessageIdObj("E11141",
     * new String[]{}); msg.add(o); return msg; }
     */

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
    String tengpcd = map.get("TENGPCD"); // 配送店グループコード

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    if (tengpcd.isEmpty() || tengpcd == null) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // エリア別配送パターンテーブルにレコードが存在する場合エラー
    sbSQL.append("SELECT ");
    sbSQL.append("	 HSGPCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("	INAMS.MSTAREAHSPTN ");
    sbSQL.append("WHERE ");
    sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND "); // 入力された配送グループで検索
    sbSQL.append("	TENGPCD	= '" + tengpcd + "' "); // 入力された配送店グループで検索

    dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    new JSONArray();

    // パラメータ確認
    // 必須チェック
    /*
     * if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
     * option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal())); return option; }
     */

    dataArray.getJSONObject(0);

    // 予約発注_納品日、予約発注_店舗INSERT/UPDATE処理
    this.createSqlHAT(map, userInfo);

    // 排他チェック実行
    /*
     * String targetTable = null; String targetWhere = null; ArrayList<String> targetParam = new
     * ArrayList<String>(); if(dataArray.size() > 0){ targetTable = "INATK.HATYH_NNDT"; targetWhere =
     * "KKKCD = ? and SHNCD = ? and NNDT = ?"; targetParam.add(data.optString("F1"));
     * targetParam.add(data.optString("F2")); targetParam.add(data.optString("F3")); if(!
     * super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F4"))){
     * msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
     * option.put(MsgKey.E.getKey(), msg); return option; } }
     */

    ArrayList<Integer> countList = new ArrayList<Integer>();
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    dataArray.getJSONObject(0);

    ArrayList<Integer> countList = new ArrayList<Integer>();
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
   * 予約発注_店舗INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlYHT_TEN(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_TEN")); // 更新情報(予約発注_納品日)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 6; // Fxxの最大値
    int len = dataArrayT.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += "null ,";
            } else {
              values += "? ,";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += "0";
            values += "," + DefineReport.ValUpdkbn.NML.getVal();
            values += ", '" + userId + "'";
            values += ",CURRENT_TIMESTAMP";
            values += ",CURRENT_TIMESTAMP";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 予約発注_店舗の登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.HATYH_TEN ( ");
        sbSQL.append("KKKCD"); // F1 : 企画コード
        sbSQL.append(",SHNCD"); // F2 : 商品コード
        sbSQL.append(",NNDT"); // F3 : 納入日
        sbSQL.append(",INPUTDT"); // F4 : 入力日付
        sbSQL.append(",TENCD"); // F5 : 店コード
        sbSQL.append(",HTSU"); // F6 : 発注数
        sbSQL.append(",UPDKBN");
        sbSQL.append(",SENDFLG");
        sbSQL.append(",OPERATOR");
        sbSQL.append(",ADDDT");
        sbSQL.append(",UPDDT");
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("KKKCD = VALUES(KKKCD)"); // F1 : 企画コード
        sbSQL.append(",SHNCD = VALUES(SHNCD)"); // F2 : 商品コード
        sbSQL.append(",NNDT = VALUES(NNDT)"); // F3 : 納入日
        sbSQL.append(",INPUTDT = VALUES(INPUTDT)"); // F4 : 入力日付
        sbSQL.append(",TENCD = VALUES(TENCD)"); // F5 : 店コード
        sbSQL.append(",HTSU = VALUES(HTSU)"); // F6 : 発注数
        sbSQL.append(",UPDKBN = VALUES(UPDKBN)");
        sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
        sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
        sbSQL.append(",UPDDT = VALUES(UPDDT)");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }
        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("予約発注_店舗");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }
    return sbSQL.toString();
  }

  /**
   * 予約発注_納品日、予約発注_店舗INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlHAT(HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_TEN")); // 更新情報(予約発注_納品日)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String userTenpo = userInfo.getTenpo(); // 担当店舗

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 3; // Fxxの最大値
    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (!data.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = data.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += "null ,";
            } else {
              values += "? ,";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += "0";
            values += ", '" + userId + "'";
            values += ", CURRENT_TIMESTAMP";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 予約発注_納入日の登録・更新
        sbSQL = new StringBuffer();

        sbSQL.append("INSERT INTO INATK.HATYH_NNDT ( ");
        sbSQL.append(" KKKCD"); // 企画コード
        sbSQL.append(", SHNCD"); // 商品コード
        sbSQL.append(", NNDT"); // 納入日
        sbSQL.append(", SENDFLG"); // 送信フラグ
        sbSQL.append(", OPERATOR");// オペレーター
        sbSQL.append(", UPDDT");// 更新日
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" KKKCD = VALUES(KKKCD)"); // 企画コード
        sbSQL.append(", SHNCD = VALUES(SHNCD)"); // 商品コード
        sbSQL.append(", NNDT = VALUES(NNDT)"); // 納入日
        sbSQL.append(", SENDFLG = VALUES(SENDFLG)"); // 送信フラグ
        sbSQL.append(", OPERATOR = VALUES(OPERATOR)");// オペレーター
        sbSQL.append(", UPDDT = VALUES(UPDDT)");// 更新日

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }
        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("予約発注_納入日");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    maxField = 6; // Fxxの最大値
    len = dataArrayT.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);

            // 店コード(担当店舗)を設定
            if (StringUtils.equals("F5", key)) {
              val = userTenpo;
            }

            if (StringUtils.isEmpty(val)) {
              values += "null ,";
            } else {
              values += "? ,";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += "0";
            values += ", '" + userId + "'";
            values += ", CURRENT_TIMESTAMP";
            values += ", CURRENT_TIMESTAMP";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 予約発注_店舗の登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.HATYH_TEN ( ");
        sbSQL.append(" KKKCD");// 企画コード
        sbSQL.append(", SHNCD");// 商品コード
        sbSQL.append(", NNDT");// 納入日
        sbSQL.append(", INPUTDT");// 入力日
        sbSQL.append(", TENCD");// 店舗コード
        sbSQL.append(", HTSU");// 発注数
        sbSQL.append(", SENDFLG");// 送信フラグ
        sbSQL.append(", OPERATOR");// オペレーター
        sbSQL.append(", ADDDT");// 登録日
        sbSQL.append(", UPDDT");// 更新日
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" KKKCD = VALUES(KKKCD)");// 企画コード
        sbSQL.append(", SHNCD = VALUES(SHNCD)");// 商品コード
        sbSQL.append(", NNDT = VALUES(NNDT)");// 納入日
        sbSQL.append(", INPUTDT = VALUES(INPUTDT)");// 入力日
        sbSQL.append(", TENCD = VALUES(TENCD)");// 店舗コード
        sbSQL.append(", HTSU = VALUES(HTSU)");// 発注数
        sbSQL.append(", SENDFLG = VALUES(SENDFLG)");// 送信フラグ
        sbSQL.append(", OPERATOR = VALUES(OPERATOR)");// オペレーター
        sbSQL.append(", UPDDT = VALUES(UPDDT)");// 更新日

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }
        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("予約発注_店舗");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }
    return sbSQL.toString();
  }

  /**
   * 予約発注_店舗DELETE SQL作成処理
   *
   * @param userInfo
   * @param Sqlprm 入力No
   * @throws Exception
   */
  public JSONObject createSqlDelHATYH_TEN(User userInfo, HashMap<String, String> map) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    userInfo.getId();
    String userTenpo = userInfo.getTenpo(); // 担当店舗

    String kikakuNo = map.get("KIKAKUNO"); // 企画No
    String shncd = map.get("SHNCD"); // 商品コード
    String nndt = map.get("NNDT"); // 納入日
    String shoridt = map.get("SHORIDT"); // 処理日付

    if (StringUtils.isEmpty(userTenpo)) {
      prmData.add(kikakuNo);
      prmData.add(shncd);
      prmData.add(userTenpo);
      prmData.add(nndt);
      prmData.add(shoridt);

      StringBuffer sbSQL;
      sbSQL = new StringBuffer();
      sbSQL.append("delete from INATK.HATYH_TEN where KKKCD = ? and SHNCD = ? and TENCD = ? and NNDT = ? and INPUTDT = ? ");

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
      }
      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("予約発注_店舗テーブル");
    }
    return result;
  }

  /**
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public boolean checkMstExist(String outobj, String value) {
    // 関連情報取得
    ItemList iL = new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";

    String tbl = "";
    String col = "";
    String rep = "";
    // 商品コード
    if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
      tbl = "INAMS.MSTSHN";
      col = "SHNCD";
    }

    // 店コード
    if (outobj.equals(DefineReport.InpText.TENCD.getObj())) {
      tbl = "INAMS.MSTTEN";
      col = "TENCD";
    }


    if (tbl.length() > 0 && col.length() > 0) {
      if (paramData.size() > 0 && rep.length() > 0) {
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep);
      } else {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);
      }

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }


}
