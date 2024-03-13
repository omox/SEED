package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportTM002Dao.TOKMOYCDLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTM004Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTM004Dao(String JNDIname) {
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
   * 更新処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);


    // 更新情報チェック(基本JS側で制御)
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

  /**
   * 削除処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    List<JSONObject> msgList = this.checkDel(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), JSONArray.fromObject(msgList));
      return option;
    }


    // 削除処理
    try {
      option = this.deleteData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
    }
    return option;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String txt_moyskbn = getMap().get("MOYSKBN"); // 催し区分
    String txt_moysstdt = getMap().get("MOYSSTDT"); // 催し開始日
    String txt_moysrban = getMap().get("MOYSRBAN"); // 催し連番
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid);

    // 変更/参照
    if (!isNew) {
      sbSQL.append(" select ");
      sbSQL.append("  T1.MOYSKBN as MOYSKBN"); // F1 : 催し区分
      sbSQL.append(" ,T1.MOYSSTDT as MOYSSTDT"); // F2 : 催し開始日
      sbSQL.append(" ,T1.MOYSRBAN as MOYSRBAN"); // F3 : 催し連番
      sbSQL.append(" ,T1.SHUNO as SHUNO"); // F4 : 週№
      sbSQL.append(" ,T1.MOYKN as MOYKN"); // F5 : 催し名称（漢字）
      sbSQL.append(" ,T1.MOYAN as MOYAN"); // F6 : 催し名称（カナ）
      sbSQL.append(" ,T1.NENMATKBN as NENMATKBN"); // F7 : 年末区分
      sbSQL.append(" ,RIGHT(T1.HBSTDT, 6) as HBSTDT"); // F8 : 販売開始日
      sbSQL.append(" ,RIGHT(T1.HBEDDT, 6) as HBEDDT"); // F9 : 販売終了日
      sbSQL.append(" ,RIGHT(T1.NNSTDT, 6) as NNSTDT"); // F10: 納入開始日
      sbSQL.append(" ,RIGHT(T1.NNEDDT, 6) as NNEDDT"); // F11: 納入終了日
      sbSQL.append(" ,RIGHT(T1.NNSTDT_TGF, 6) as NNSTDT_TGF"); // F12: 納入開始日_全特生鮮
      sbSQL.append(" ,RIGHT(T1.NNEDDT_TGF, 6) as NNEDDT_TGF"); // F13: 納入終了日_全特生鮮
      sbSQL.append(" ,RIGHT(T1.PLUSDDT, 6) as PLUSDDT"); // F14: PLU配信日
      sbSQL.append(" ,T1.PLUSFLG as PLUSFLG"); // F15: PLU配信済フラグ
      sbSQL.append(" ,T1.UPDKBN as UPDKBN"); // F16: 更新区分
      sbSQL.append(" ,T1.SENDFLG as SENDFLG"); // F17: 送信フラグ
      sbSQL.append(" ,T1.OPERATOR as OPERATOR"); // F18: オペレータ
      sbSQL.append(" ,COALESCE(DATE_FORMAT(T1.ADDDT, '%y/%m/%d'),'__/__/__') as ADDDT"); // F19: 登録日
      sbSQL.append(" ,COALESCE(DATE_FORMAT(T1.UPDDT, '%y/%m/%d'),'__/__/__') as UPDDT"); // F20: 更新日
      sbSQL.append(" ,DATE_FORMAT(T1.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F21:更新日時
      sbSQL.append(" from INATK.TOKMOYCD T1 ");
      sbSQL.append(" where T1.MOYSKBN = " + txt_moyskbn + " and T1.MOYSSTDT = " + txt_moysstdt + " and T1.MOYSRBAN = " + txt_moysrban + " and COALESCE(UPDKBN, 0) <> 1 ");
    }

    // オプション情報設定
    JSONObject option = new JSONObject();
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
    new ArrayList<String>();


    // 共通箇所設定
    createCmnOutput(jad);

  }


  boolean isTest = false;

  /**
   * 固定値定義（SQLタイプ）<br>
   */
  public enum SqlType {
    /** INSERT */
    INS(1, "INSERT"),
    /** UPDATE */
    UPD(2, "UPDATE"),
    /** DELETE */
    DEL(3, "DELETE"),
    /** MERGE */
    MRG(4, "MERGE");

    private final Integer val;
    private final String txt;

    /** 初期化 */
    private SqlType(Integer val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    public Integer getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }
  }


  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ取得
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー


    // SQLパターン
    // ①正 .新規 → 催し週：Insert処理／催しコード：Insert処理
    // ②正 .変更 → 週 ：Update処理

    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);


    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // ①正 .新規
    if (isNew) {
      this.createSqlTOKMOYCD(userId, dataArray, SqlType.MRG);

      // ②正 .変更
    } else if (isChange) {
      this.createSqlTOKMOYCD(userId, dataArray, SqlType.MRG);

    }


    // 排他チェック実行
    String targetTable = "INATK.TOKMOYCD";
    String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and UPDKBN = 0";
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(data.optString(TOKMOYCDLayout.MOYSKBN.getId()));
    targetParam.add(data.optString(TOKMOYCDLayout.MOYSSTDT.getId()));
    targetParam.add(data.optString(TOKMOYCDLayout.MOYSRBAN.getId()));
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F21"))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

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

  @Override
  public JSONObject createJSONObject(String[] keys, String[] values) {
    JSONObject obj = new JSONObject();
    for (int i = 0; i < keys.length; i++) {
      obj.put(keys[i], values[i]);
    }
    return obj;
  }

  /**
   * 削除処理実行
   *
   * @param sysdate
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ取得
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);
    DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid);
    DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
    DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);


    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);
    // 変更処理（正）
    if (isChange) {
      this.createSqlTOKMOYCD(userId, dataArray, SqlType.DEL);


      // String moyskbn = data.optString(TOKMOYCDLayout.MOYSKBN.getId());
      // // --- 全店特売（アンケート有）系情報削除
      // if(StringUtils.equals(moyskbn, "5")){
      // JSONObject resultD1 = this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKCP_BMN"); //
      // 催し送信
      // JSONObject resultD2 = this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKSO_SHN"); //
      // 生活応援_商品
      // }
      //
      // if(StringUtils.equals(moyskbn, "7")){
      // JSONObject resultD1 = this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKGY_SHN"); //
      // 月間山積_商品
      // }
      //
      // if(StringUtils.equals(moyskbn, "8")){
      // JSONObject resultD1 = this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKQJU_MOY"); //
      // 店舗アンケート付き送付け_催し
      // }
      //
      // if(StringUtils.equals(moyskbn, "9")){
      // JSONObject resultD1 = this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKJU_SHN"); //
      // 事前打出し商品
      // }

      // this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKRANKEX");
      // this.createSqlTOKTG_CMN(userId, dataArray, SqlType.DEL, "TOKSRPTNEX");

    }


    // 排他チェック実行
    String targetTable = "INATK.TOKMOYCD";
    String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and UPDKBN = 0";
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(data.optString(TOKMOYCDLayout.MOYSKBN.getId()));
    targetParam.add(data.optString(TOKMOYCDLayout.MOYSSTDT.getId()));
    targetParam.add(data.optString(TOKMOYCDLayout.MOYSRBAN.getId()));
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F21"))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

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
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
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
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    DefineReport.Button.NEW.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);


    List<JSONObject> msgList = new ArrayList<JSONObject>();

    dataArray.optJSONObject(0);



    // // 3.催しコード：スポット
    // ErrTblNm = "MSTSIRGPSHN";
    // for (int i = 0; i < dataArrayMOYCD.size(); i++) {
    // JSONObject jo = dataArrayMOYCD.optJSONObject(i);
    // for(MSTSIRGPSHNLayout colinf: MSTSIRGPSHNLayout.values()){
    // String val = StringUtils.trim(jo.optString(colinf.getId()));
    // if(StringUtils.isNotEmpty(val)){
    // DataType dtype = null;
    // int[] digit = null;
    // try {
    // DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
    // dtype = inpsetting.getType();
    // digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
    // }catch (IllegalArgumentException e){
    // dtype = colinf.getDataType();
    // digit = colinf.getDigit();
    // }
    // // ①データ型による文字種チェック
    // if(!InputChecker.checkDataType(dtype, val)){
    // JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
    // msg.add(o);
    // if(!colinf.isText()){
    // jo.element(colinf.getId(), ""); // CSVトラン用に空
    // }
    // }
    // // ②データ桁チェック
    // if(!InputChecker.checkDataLen(dtype, val, digit)){
    // JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
    // msg.add(o);
    // jo.element(colinf.getId(), ""); // CSVトラン用に空
    // }
    // }
    // }
    // }



    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public String createCommandcheck(String kbn) {
    StringBuffer sqlcommand = new StringBuffer();


    if ("MOYOKBN".equals(kbn)) {
      sqlcommand.append(" SELECT COUNT(*) AS VALUE");
      sqlcommand.append(" FROM INATK.TOKMOYCD as T1");
      sqlcommand.append(" WHERE");
      sqlcommand.append(" MOYSKBN = ?");
      sqlcommand.append(" and MOYSSTDT = ?");
      sqlcommand.append(" and MOYSRBAN = ?");
      sqlcommand.append(" and UPDKBN = 0");
    } else {
      sqlcommand.append(" with WK as (select");
      sqlcommand.append(" ? as MOYSKBN");
      sqlcommand.append(" ,? as MOYSSTDT");
      sqlcommand.append(" ,?as MOYSRBAN");
      sqlcommand.append(" from (SELECT 1 AS DUMMY) DUMMY)");
      sqlcommand.append(" SELECT SUM(CNT) as VALUE FROM (");

      if ("MOYOKBN3".equals(kbn) || "MOYOKBN5".equals(kbn) || "MOYOKBN2".equals(kbn)) {
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKCP_BMN as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" UNION ALL");
      }


      if ("MOYOKBN3".equals(kbn) || "MOYOKBN2".equals(kbn)) {
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKBM as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
      }

      if ("MOYOKBN3".equals(kbn)) {
        sqlcommand.append(" UNION ALL");
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKTG_SHN as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");

        sqlcommand.append(" UNION ALL");
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKSP_SHN as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");

        sqlcommand.append(" UNION ALL");
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKBT_KKK as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
      }

      if ("MOYOKBN5".equals(kbn)) {
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKSO_SHN as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
      }

      if ("MOYOKBN7".equals(kbn)) {
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKGY_SHN as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
      }

      if ("MOYOKBN8".equals(kbn)) {
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKQJU_MOY as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
      }
      if ("MOYOKBN9".equals(kbn)) {
        sqlcommand.append(" SELECT COUNT(*) AS CNT");
        sqlcommand.append(" FROM INATK.TOKJU_SHN as T1");
        sqlcommand.append(" inner join WK");
        sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
        sqlcommand.append(" WHERE");
        sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
        sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
        sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
        sqlcommand.append(" and T1.UPDKBN = 0");
      }
      sqlcommand.append(" ) AS T1");
    }
    return sqlcommand.toString();

  }



  /**
   * チェック処理(削除時)
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    DefineReport.Button.NEW.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);


    List<JSONObject> msgList = new ArrayList<JSONObject>();

    dataArray.optJSONObject(0);
    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  /**
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getMstData(String sqlcommand, ArrayList<String> paramData) {
    // 関連情報取得
    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    return array;
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
    if (outobj.equals(DefineReport.Select.SHUNO.getObj())) {
      tbl = "INAMS.MSTSHN";
      col = "SHUNO";
    }
    // メーカーコード
    if (outobj.equals(DefineReport.InpText.MAKERCD.getObj())) {
      tbl = "INAMS.MSTMAKER";
      col = "MAKERCD";
    }
    // 部門コード
    if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
      tbl = "INAMS.MSTBMN";
      col = "BMNCD";
    }

    // 配送パターン仕入先
    if (outobj.equals("MSTHSPTNSIR") && value.length() > 1) {
      tbl = "INAMS.MSTHSPTNSIR";
      col = "right('00000'||SIRCD, 6)||right('00'||HSPTN, 3)";

      String[] vals = StringUtils.split(value, ",");
      for (String val : vals) {
        rep += ", ?";
        String cd = StringUtils.leftPad(val.split("-")[0], 6, "0") + StringUtils.leftPad(val.split("-")[1], 3, "0");
        paramData.add(cd);
      }
      rep = StringUtils.removeStart(rep, ",");
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

  /**
   * 催しコードINSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKMOYCD(String userId, JSONArray dataArray, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    // 更新列設定
    TOKMOYCDLayout[] cols = new TOKMOYCDLayout[] {};
    if (SqlType.DEL.getVal() == sql.getVal()) {
      cols = new TOKMOYCDLayout[] {TOKMOYCDLayout.MOYSKBN, TOKMOYCDLayout.MOYSSTDT, TOKMOYCDLayout.MOYSRBAN};
    } else {
      for (TOKMOYCDLayout col : TOKMOYCDLayout.values()) {
        if (!ArrayUtils.contains(new TOKMOYCDLayout[] {TOKMOYCDLayout.SENDFLG, TOKMOYCDLayout.UPDKBN, TOKMOYCDLayout.OPERATOR, TOKMOYCDLayout.ADDDT, TOKMOYCDLayout.UPDDT}, col)) {
          cols = (TOKMOYCDLayout[]) ArrayUtils.add(cols, col);
        }
      }
    }

    ItemList iL = new ItemList();

    String sqlcommand = DefineReport.ID_SQL_SHUNO_M;
    ArrayList<String> paramData = new ArrayList<String>();
    paramData.add(dataArray.optJSONObject(0).optString("F8"));
    paramData.add(dataArray.optJSONObject(0).optString("F8"));

    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);


    String values = "", names = "", rows = "";
    // 列別名設定
    for (TOKMOYCDLayout col : cols) {
      names += "," + col.getId();
    }
    names = StringUtils.removeStart(names, ",");
    // 値設定
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (TOKMOYCDLayout col : cols) {
        String val = dataArray.optJSONObject(j).optString(col.getId());

        if (col.equals(TOKMOYCDLayout.NNSTDT_TGF)) {
          String nnstdt = dataArray.optJSONObject(j).optString(TOKMOYCDLayout.NNSTDT.getId());
          if (StringUtils.isNotEmpty(nnstdt)) {
            val = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(nnstdt), 1));
          }
        }
        if (col.equals(TOKMOYCDLayout.NNEDDT_TGF)) {
          String nneddt = dataArray.optJSONObject(j).optString(TOKMOYCDLayout.NNEDDT.getId());
          if (StringUtils.isNotEmpty(nneddt)) {
            val = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(nneddt), 1));
          }
        }

        if (StringUtils.isEmpty(val)) {

          if (col.equals(TOKMOYCDLayout.SHUNO)) {
            val = array.optJSONObject(0).optString("F1");
            if (isTest) {
              values += ",'" + val + "'";
            } else {
              prmData.add(val);
              values += ",?";
            }
          } else {
            values += ", null";
          }
        } else {
          if (isTest) {
            values += ",'" + val + "'";
          } else {
            prmData.add(val);
            values += ",?";
          }
        }
      }
      values += ", " + DefineReport.ValUpdkbn.NML.getVal();
      values += ", " + DefineReport.Values.SENDFLG_UN.getVal();
      values += ", '" + userId + "' ";
      values += ", CURRENT_TIMESTAMP ";
      values += ", CURRENT_TIMESTAMP ";
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");



    DefineReport.Values.SENDFLG_UN.getVal();
    DefineReport.ValUpdkbn.NML.getVal();
    if (SqlType.DEL.getVal() == sql.getVal()) {
      DefineReport.ValUpdkbn.DEL.getVal();
    }


    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKMOYCD (");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append("  MOYSKBN"); // F1 : 催し区分
      sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
      sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
      sbSQL.append(" ,UPDKBN"); // F16: 更新区分
      sbSQL.append(" ,SENDFLG"); // F17: 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F18: オペレータ
      sbSQL.append(" ,UPDDT"); // F20: 更新日
      sbSQL.append(" ,ADDDT"); // F19: 登録日
    } else {
      sbSQL.append("  MOYSKBN"); // F1 : 催し区分
      sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
      sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
      sbSQL.append(" ,SHUNO"); // F4 : 週№
      sbSQL.append(" ,MOYKN"); // F5 : 催し名称（漢字）
      sbSQL.append(" ,MOYAN"); // F6 : 催し名称（カナ）
      sbSQL.append(" ,NENMATKBN"); // F7 : 年末区分
      sbSQL.append(" ,HBSTDT"); // F8 : 販売開始日
      sbSQL.append(" ,HBEDDT"); // F9 : 販売終了日
      sbSQL.append(" ,NNSTDT"); // F10: 納入開始日
      sbSQL.append(" ,NNEDDT"); // F11: 納入終了日
      sbSQL.append(" ,NNSTDT_TGF"); // F12: 納入開始日_全特生鮮
      sbSQL.append(" ,NNEDDT_TGF"); // F13: 納入終了日_全特生鮮
      sbSQL.append(" ,PLUSDDT"); // F14: PLU配信日
      sbSQL.append(" ,PLUSFLG"); // F15: PLU配信済フラグ
      sbSQL.append(" ,UPDKBN"); // F16: 更新区分
      sbSQL.append(" ,SENDFLG"); // F17: 送信フラグ
      sbSQL.append(" ,OPERATOR");// F18: オペレータ
      sbSQL.append(" ,ADDDT"); // F19: 登録日
      sbSQL.append(" ,UPDDT"); // F20: 更新日
    }
    sbSQL.append(") VALUES ");
    sbSQL.append(rows);
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(",UPDDT = VALUES(UPDDT)");
    } else {
      sbSQL.append("MOYSKBN = VALUES(MOYSKBN)");
      sbSQL.append(",MOYSSTDT = VALUES(MOYSSTDT)");
      sbSQL.append(",MOYSRBAN = VALUES(MOYSRBAN)");
      sbSQL.append(",SHUNO = VALUES(SHUNO)");
      sbSQL.append(",MOYKN = VALUES(MOYKN)");
      sbSQL.append(",NENMATKBN = VALUES(NENMATKBN)");
      sbSQL.append(",HBSTDT = VALUES(HBSTDT)");
      sbSQL.append(",HBEDDT = VALUES(HBEDDT)");
      sbSQL.append(",NNSTDT = VALUES(NNSTDT)");
      sbSQL.append(",NNEDDT = VALUES(NNEDDT)");
      sbSQL.append(",NNSTDT_TGF = VALUES(NNSTDT_TGF)");
      sbSQL.append(",NNEDDT_TGF = VALUES(NNEDDT_TGF)");
      sbSQL.append(",PLUSDDT = VALUES(PLUSDDT)");
      sbSQL.append(",PLUSFLG = VALUES(PLUSFLG)");
      sbSQL.append(",UPDKBN = VALUES(UPDKBN)");
      sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
      sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
      sbSQL.append(",UPDDT = VALUES(UPDDT)");
    }



    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催しコード");
    return result;
  }
}
