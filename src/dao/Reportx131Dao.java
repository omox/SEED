package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import common.DefineReport.DataType;
import common.Defines;
import common.InputChecker;
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
public class Reportx131Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx131Dao(String JNDIname) {
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
    ArrayList<String> paramData = new ArrayList<String>();
    String szTencd = getMap().get("TENCD"); // 店コード
    getMap().get("TENKN");
    getMap().get("SENDBTNID");

    if (!StringUtils.isEmpty(szTencd)) {
      paramData.add(szTencd);
      paramData.add(szTencd);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("with RECURSIVE T1(IDX) as ( ");
    sbSQL.append("  select");
    sbSQL.append("    1");
    sbSQL.append("  from");
    sbSQL.append("    (SELECT 1 AS DUMMY) AS DUMMY");
    sbSQL.append("  union all");
    sbSQL.append("  select");
    sbSQL.append("    IDX + 1");
    sbSQL.append("  from");
    sbSQL.append("    T1 ");
    sbSQL.append("  where");
    sbSQL.append("    IDX < 99");
    sbSQL.append(") ");
    sbSQL.append("select T1.*,T2.*");
    sbSQL.append("from T1");
    sbSQL.append(" left outer join (");
    sbSQL.append("select");
    sbSQL.append("  ROW_NUMBER() over (order by TYHB.TENCD, TYHB.BMNCD) as IDX");
    sbSQL.append("  , right('0'||TYHB.BMNCD,2)");
    sbSQL.append("  , BMN.BMNKN");
    sbSQL.append("  , TYHB.HATFLG_MON");
    sbSQL.append("  , TYHB.HATFLG_TUE");
    sbSQL.append("  , TYHB.HATFLG_WED");
    sbSQL.append("  , TYHB.HATFLG_THU");
    sbSQL.append("  , TYHB.HATFLG_FRI");
    sbSQL.append("  , TYHB.HATFLG_SAT");
    sbSQL.append("  , TYHB.HATFLG_SUN");
    sbSQL.append("  , T2.OPERATOR");
    sbSQL.append("  , DATE_FORMAT(T1.ADDDT, '%Y%m%d')");
    sbSQL.append("  , DATE_FORMAT(T1.UPDDT, '%Y%m%d')");
    sbSQL.append("  , 0 ");
    sbSQL.append(" , DATE_FORMAT(TYHB.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // :更新日時
    sbSQL.append(" from");
    sbSQL.append("  INAMS.MSTTENYOBIHTBMN TYHB ");
    sbSQL.append("  left join (select TENCD, MIN(ADDDT) as ADDDT, MAX(UPDDT) as UPDDT from INAMS.MSTTENYOBIHTBMN where COALESCE(UPDKBN, 0) <> 1 group by TENCD) T1 on T1.TENCD = TYHB.TENCD");
    sbSQL.append(
        "  left join (select distinct TENCD, OPERATOR from INAMS.MSTTENYOBIHTBMN where COALESCE(UPDKBN, 0) <> 1 and  TENCD = ? and UPDDT = (select MAX(UPDDT) from INAMS.MSTTENYOBIHTBMN where COALESCE(UPDKBN, 0) <> 1 and TENCD = ? group by TENCD)) T2 on T2.TENCD = TYHB.TENCD ");
    sbSQL.append("  left join INAMS.MSTBMN BMN ");
    sbSQL.append("    on BMN.BMNCD = TYHB.BMNCD  ");
    sbSQL.append("    where   ");
    sbSQL.append("    TYHB.UPDKBN =  ");
    sbSQL.append(DefineReport.ValUpdkbn.NML.getVal());

    if (szTencd.length() > 0) {
      sbSQL.append(" and TYHB.TENCD = ? ");
      paramData.add(szTencd);
    }
    sbSQL.append(" ) T2 on T1.IDX = T2.IDX");
    sbSQL.append(" order by T1.IDX");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

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
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map, userInfo, sysdate);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }
    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
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
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.checkDel(map, userInfo, sysdate);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 削除処理
    try {
      msgObj = this.deleteData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
    }
    return msgObj;
  }

  boolean isTest = true;

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

    CmnDate.dbDateFormat(sysdate);

    new ArrayList<String>();
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    new JSONObject();
    JSONArray msg = new JSONArray();
    JSONObject option = new JSONObject();

    userInfo.getId();

    map.get("SENDBTNID");
    map.get("SEL_BMONCD");
    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 店舗曜日別発注部門マスタINSERT/UPDATE処理
    this.createSqlTENYOBIHTBMN(map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    // ⑨CSVエラー修正

    // 排他チェック：大分類グリッド
    if (dataArray.size() > 0) {
      String rownum = ""; // エラー行数

      for (int i = 0; i < dataArray.size(); i++) {
        JSONObject data = dataArray.getJSONObject(i);
        if (data.isEmpty()) {
          continue;
        }

        targetTable = "INAMS.MSTTENYOBIHTBMN";
        targetWhere = "TENCD = ? and BMNCD = ?";
        targetParam = new ArrayList<String>();
        targetParam.add(data.optString("F1"));
        targetParam.add(data.optString("F2"));

        if (StringUtils.isEmpty(data.optString("F1")) && StringUtils.isEmpty(data.optString("F2"))) {
          if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F10"))) {
            rownum = data.optString("idx");
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[] {rownum}));
            option.put(MsgKey.E.getKey(), msg);
            return option;
          }
        }
      }
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

  /** マスタレイアウト */
  public interface MSTLayout {
    public Integer getNo();

    public String getCol();

    public String getTyp();

    public String getId();

    public DataType getDataType();

    public boolean isText();
  }

  /** 商品マスタレイアウト */
  public enum TYHBLayout implements MSTLayout {
    /** 店コード */
    TENCD(1, "TENCD", "SMALLINT"),
    /** 部門コード */
    BMNCD(2, "BMNCD", "SMALLINT");


    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TYHBLayout(Integer no, String col, String typ) {
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

  private enum dbNumericTypeInfo {
    /** SMALLINT */
    SMALLINT(5, -32768, 32768),
    /** INT */
    INT(10, -2147483648, 2147483648l),
    /** INTEGER */
    INTEGER(10, -2147483648, 2147483648l);

    private final int digit;

    /** 初期化 */
    private dbNumericTypeInfo(int digit, long min, long max) {
      this.digit = digit;
    }

    /** @return digit */
    public int getDigit() {
      return digit;
    }
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONObject msgObj = new JSONObject();

    String userId = userInfo.getId(); // ログインユーザー
    String tencd = map.get("TENCD"); // 店舗コード
    String bmncd = ""; // 部門コード

    JSONObject data = dataArray.getJSONObject(0);
    bmncd = data.optString("F3");

    if (tencd.length() == 0 || bmncd.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    ArrayList<String> prmData = new ArrayList<String>();

    // 削除処理：更新区分に"1"（削除）を登録
    sbSQL = new StringBuffer();

    sbSQL.append("UPDATE INAMS.MSTTENYOBIHTBMN");
    sbSQL.append(" SET ");
    sbSQL.append(" SENDFLG = 0");
    sbSQL.append(",UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT= UPDDT ");
    sbSQL.append(" WHERE TENCD = ?");
    sbSQL.append(" and BMNCD = ?");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(tencd);
    prmData.add(tencd);
    prmData.add(bmncd);
    int count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("店舗曜日別発注部門マスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
    }
    return msgObj;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    MessageUtility mu = new MessageUtility();
    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

    List<JSONObject> msgList = this.checkData(isNew, isChange, map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  /**
   * チェック処理(削除用)
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {
    JSONArray.fromObject(map.get("DATA"));

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    new MessageUtility();
    DefineReport.Button.NEW.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

    // List<JSONObject> msgList = this.checkData(isNew,isChange,map,userInfo,sysdate,mu,dataArray);

    JSONArray msgArray = new JSONArray();

    /*
     * if(msgList.size() > 0){ msgArray.add(msgList.get(0)); }
     */
    return msgArray;
  }



  public List<JSONObject> checkData(boolean isNew, boolean isChange, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）

  ) {
    JSONArray msg = new JSONArray();
    JSONObject data = dataArray.optJSONObject(0);

    // ソースコード取得
    ArrayList<JSONObject> srccds = new ArrayList<JSONObject>();
    HashSet<String> srccds_ = new HashSet<String>();
    for (int i = 0; i < dataArray.size(); i++) {
      String val = dataArray.optJSONObject(i).optString(TYHBLayout.BMNCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        srccds.add(dataArray.optJSONObject(i));
        srccds_.add(val);
      }
    }

    // 重複チェック
    if (srccds.size() != srccds_.size()) {
      JSONObject o = MessageUtility.getDbMessageIdObj("E11040", new String[] {"部門コード"});
      msg.add(o);
      return msg;
    }

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.店舗曜日別発注部門マスタ
    for (TYHBLayout colinf : TYHBLayout.values()) {
      String val = StringUtils.trim(data.optString(colinf.getId()));
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
          JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
          msg.add(o);
          return msg;
        }
        // ②データ桁チェック
        if (!InputChecker.checkDataLen(dtype, val, digit)) {
          JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
          msg.add(o);
          return msg;
        }
      }
    }
    /*
     * if(this.checkMstExist(isNew,data.optString("F1"),data.optString("F2"))){ JSONObject o; o =
     * mu.getDbMessageObj("E00004", new String[]{});
     *
     * msg.add(o); return msg; }
     *
     * if(!isChange&&this.checkMstExist(isNew,data.optString("F1"),data.optString("F3"))){ JSONObject o;
     * if(isNew){ o = mu.getDbMessageObj("E00004", new String[]{}); }else{ o =
     * mu.getDbMessageObj("E00006", new String[]{}); } msg.add(o); return msg; }
     */

    TYHBLayout[] targetCol = null;

    targetCol = new TYHBLayout[] {TYHBLayout.TENCD, TYHBLayout.BMNCD};

    for (TYHBLayout colinf : targetCol) {
      if (StringUtils.isEmpty(data.optString(colinf.getId()))) {
        JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
        msg.add(o);
        return msg;
      }
    }
    return msg;
  }

  /**
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public boolean checkMstExist(boolean isNew, String outobj, String value1, String value2) {
    // 関連情報取得
    ItemList iL = new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";
    String tbl = "";
    String sqlwhere = "";

    // 店舗コード
    if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
      tbl = "INAMS.MSTTEN";
    }
    // 部門コード
    if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
      tbl = "INAMS.MSTBMN";
    }

    paramData.add(value1);
    paramData.add(value2);
    sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl) + sqlwhere;

    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
      return true;
    }

    return false;
  }

  /**
   * 店舗曜日別発注部門マスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTENYOBIHTBMN(HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(予約発注_納品日)


    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    map.get("SENDBTNID");

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 9; // Fxxの最大値
    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (!data.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);


          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = data.optString(key);

            if (StringUtils.isEmpty(val)) {
              values += ", null";
            } else {
              values += ", ?";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, values);
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        sbSQL.append(" REPLACE INTO INAMS.MSTTENYOBIHTBMN (");
        sbSQL.append(" TENCD"); // 店コード
        sbSQL.append(", BMNCD"); // 部門
        sbSQL.append(", HATFLG_MON"); // 発注可能フラグ_月
        sbSQL.append(", HATFLG_TUE"); // 発注可能フラグ_火
        sbSQL.append(", HATFLG_WED"); // 発注可能フラグ_水
        sbSQL.append(", HATFLG_THU"); // 発注可能フラグ_木
        sbSQL.append(", HATFLG_FRI"); // 発注可能フラグ_金
        sbSQL.append(", HATFLG_SAT"); // 発注可能フラグ_土
        sbSQL.append(", HATFLG_SUN"); // 発注可能フラグ_日
        sbSQL.append(", UPDKBN "); // 更新区分：
        sbSQL.append(", SENDFLG "); // 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター：
        sbSQL.append(", ADDDT ");// 登録日
        sbSQL.append(", UPDDT "); // 更新日：
        sbSQL.append(") ");
        sbSQL.append("VALUES (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal()); // 更新区分
        sbSQL.append(", 0"); // 送信フラグ
        sbSQL.append(", '" + userId + "' "); // オペレーター
        sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日
        sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + " */ " + sbSQL.toString());
        }
        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("店舗曜日別発注部門マスタ");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }
    return sbSQL.toString();
  }

}

