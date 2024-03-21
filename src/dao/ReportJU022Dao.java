package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
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
public class ReportJU022Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  int shoriDt = 0;

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU022Dao(String JNDIname) {
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

    JSONObject option = new JSONObject();

    shoriDt = Integer.valueOf(map.get("SHORIDATE")); // 処理日付

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
   * 店舗アンケート付き送り付けINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTokQju(JSONObject data, HashMap<String, String> map, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    String moyskbn = map.get("MOYSKBN");
    String moysstdt = map.get("MOYSSTDT");
    String moysrban = map.get("MOYSRBAN");

    int maxField = 2; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);



      if (!ArrayUtils.contains(new String[] {}, key)) {
        String val = data.optString(key);

        if (key.equals("F1")) {
          values += " ?, ?, ?";
          prmData.add(moyskbn);
          prmData.add(moysstdt);
          prmData.add(moysrban);
        } else if (StringUtils.isEmpty(val)) {
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

    // 配送グループマスタの登録・更新
    sbSQL.append("REPLACE INTO INATK.TOKQJU_MOY ( ");
    sbSQL.append("  MOYSKBN"); // 催し区分：
    sbSQL.append(", MOYSSTDT"); // 催し開始日：
    sbSQL.append(", MOYSRBAN"); // 催し連番：
    sbSQL.append(", QASMDT"); // アンケート締め切り日：
    sbSQL.append(", UPDKBN"); // 更新区分：
    sbSQL.append(", SENDFLG"); // 送信フラグ：
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(") VALUES ( " + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分：
    sbSQL.append(", 0 "); // 送信フラグ：
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("店舗アンケート付き送り付け_催し");

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

    // クリア
    prmData = new ArrayList<String>();

    // 一覧表情報
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("SUMI_KANRINO+1 AS SUMI_KANRINO ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.SYSMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    int kanrino = 0;
    if (dbDatas.size() > 0) {
      JSONObject getData = dbDatas.getJSONObject(0);
      if (!StringUtils.isEmpty(getData.optString("SUMI_KANRINO"))) {
        kanrino = Integer.valueOf(getData.optString("SUMI_KANRINO"));
      } else {
        kanrino = 0;
      }
    }

    // 管理番号が登録されていれば更新
    if (kanrino != 0) {
      sbSQL = new StringBuffer();
      sbSQL.append("UPDATE ");
      sbSQL.append("INATK.SYSMOYCD ");
      sbSQL.append("SET ");
      sbSQL.append("SUMI_KANRINO=" + kanrino);
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=current_timestamp ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("催しコード内部管理");
    }
    return sbSQL.toString();
  }

  /**
   * 店舗アンケート付き送り付けUPDATE(倫理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlTokQju(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String moyskbn = map.get("MOYSKBN");
    String moysstdt = map.get("MOYSSTDT");
    String moysrban = map.get("MOYSRBAN");

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

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

    sbSQL.append("UPDATE ");
    sbSQL.append("INATK.TOKQJU_MOY ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal() + ",");
    sbSQL.append("OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=current_timestamp ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("店舗アンケート付き送り付け_催し");

    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INATK.SYSMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("催しコード内部管理");

    return sbSQL.toString();
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
    JSONArray msg = this.check(map, userInfo, sysdate);

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

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";

    String moyskbn = getMap().get("MOYSKBN");
    String moysstdt = getMap().get("MOYSSTDT");
    String moysrban = getMap().get("MOYSRBAN");

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
      sqlWhere += "T1.MOYSRBAN=null ";
    } else {
      sqlWhere += "T1.MOYSRBAN=? ";
      paramData.add(moysrban);
    }

    // 一覧表情報
    sbSQL.append("SELECT ");
    sbSQL.append("T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) AS MOYSCD"); // F1 : 催しコード
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(T2.QASMDT,'%Y%m%d'),'%y/%m/%d') AS QASMDT "); // F2 : アンケート締切日
    sbSQL.append(",T2.OPERATOR "); // F3 : オペレーター
    sbSQL.append(",DATE_FORMAT(T2.ADDDT,'%y/%m/%d') AS ADDDT "); // F4 : 登録日
    sbSQL.append(",DATE_FORMAT(T2.UPDDT,'%y/%m/%d') AS UPDDT "); // F5 : 更新日
    sbSQL.append(",DATE_FORMAT(T2.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F6 : 更新日時
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKMOYCD T1 LEFT JOIN INATK.TOKQJU_MOY T2 ON ");
    sbSQL.append("T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append(" AND T1.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" AND T2.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());

    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  boolean isTest = true;

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<String>();

    // 期間系
    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.KIKAN.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
    getWhere().add(cells);

    // 店舗系
    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.KIGYO.getTxt());
    cells.add(DefineReport.Select.TENPO.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.TENPO.getObj()));
    getWhere().add(cells);

    // 分類系
    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.BUMON.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.BUMON.getObj()));
    getWhere().add(cells);

    // 共通箇所設定
    createCmnOutput(jad);
  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray msg = new JSONArray();

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 店舗アンケート付き送り付けINSERT/UPDATE処理
    createSqlTokQju(data, map, userInfo);

    // 排他チェック実行
    String moyskbn = map.get("MOYSKBN");
    String moysstdt = map.get("MOYSSTDT");
    String moysrban = map.get("MOYSRBAN");

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
      targetWhere += "MOYSRBAN=null ";
    } else {
      targetWhere += "MOYSRBAN=? ";
      targetParam.add(moysrban);
    }

    targetTable = "INATK.TOKQJU_MOY";
    targetWhere += " AND UPDKBN=?";
    targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F6"))) {
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

  /** マスタレイアウト */
  public interface MSTLayout {
    public Integer getNo();

    public String getCol();

    public String getTyp();

    public String getId();

    public DataType getDataType();

    public boolean isText();
  }

  /** 店舗アンケート付き送り付け_催し */
  public enum TOKQJULayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT"),
    /** アンケート締切日 */
    QASMDT(4, "QASMDT", "INTEGER");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TOKQJULayout(Integer no, String col, String typ) {
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
    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray msg = new JSONArray();

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 店舗アンケート付き送り付けUPDATE(論理削除)処理
    createDelSqlTokQju(data, map, userInfo);

    // 排他チェック実行
    String moyskbn = map.get("MOYSKBN");
    String moysstdt = map.get("MOYSSTDT");
    String moysrban = map.get("MOYSRBAN");

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
      targetWhere += "MOYSRBAN=null ";
    } else {
      targetWhere += "MOYSRBAN=? ";
      targetParam.add(moysrban);
    }

    targetTable = "INATK.TOKQJU_MOY";
    targetWhere += " AND UPDKBN=?";
    targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F6"))) {
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

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    MessageUtility mu = new MessageUtility();

    // ①正 .新規
    boolean isNew = false;
    // ②正 .変更
    boolean isChange = true;
    // 削除
    boolean isDel = false;

    if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
      isNew = true;
      isChange = false;
    } else if (StringUtils.equals(DefineReport.Button.DEL.getObj(), sendBtnid)) {
      isDel = true;
    }

    List<JSONObject> msgList = this.checkData(isNew, isChange, map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
      return msgArray;
    }

    JSONObject data = dataArray.optJSONObject(0);

    String moyskbn = map.get("MOYSKBN");
    String moysstdt = map.get("MOYSSTDT");
    String moysrban = map.get("MOYSRBAN");
    int qasmdt = data.optInt("F2");

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

    if (isNew) {

      // 一覧表情報
      sbSQL.append("SELECT ");
      sbSQL.append("MOYSKBN ");
      sbSQL.append("FROM INATK.TOKQJU_MOY ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append(" AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() > 0) {
        // 既に催しコードが登録されている為エラー
        msgArray.add(mu.getDbMessageObj("E20275", new String[] {}));
        return msgArray;
      }
    }

    if (isDel) {

      // 一覧表情報
      sbSQL = new StringBuffer();
      sbSQL.append("SELECT ");
      sbSQL.append("SHNCD ");
      sbSQL.append("FROM INATK.TOKQJU_SHN ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append(" AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() > 0) {
        // 既に催しコードが登録されている為エラー
        msgArray.add(mu.getDbMessageObj("E20239", new String[] {}));
        return msgArray;
      }
    }

    if (!isDel) {
      // 一覧表情報
      sbSQL = new StringBuffer();
      sbSQL.append("SELECT ");
      sbSQL.append("DATE_FORMAT(DATE_FORMAT(NNSTDT,'%Y%m%d') - 2 ,'%Y%m%d') AS NNSTDT ");
      sbSQL.append("FROM INATK.TOKMOYCD ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      int nnstdt = 0;

      if (dbDatas.size() > 0) {
        nnstdt = dbDatas.getJSONObject(0).optInt("NNSTDT");
      }

      // アンケート締め日範囲チェック
      if (!(shoriDt <= qasmdt && qasmdt <= nnstdt)) {
        // 既に催しコードが登録されている為エラー
        msgArray.add(mu.getDbMessageObj("E20262", new String[] {}));
        return msgArray;
      }
    }
    return msgArray;
  }

  public List<JSONObject> checkData(boolean isNew, boolean isChange, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）
  ) {
    JSONArray msg = new JSONArray();
    JSONObject data = dataArray.optJSONObject(0);

    String moyskbn = map.get("MOYSKBN");
    String moysstdt = map.get("MOYSSTDT");
    String moysrban = map.get("MOYSRBAN");

    // 必須チェック
    if (StringUtils.isEmpty(moyskbn)) {
      JSONObject o = mu.getDbMessageObj("E30012", new String[] {"催しコード"});
      msg.add(o);
      return msg;
    }

    if (StringUtils.isEmpty(moysstdt)) {
      JSONObject o = mu.getDbMessageObj("E30012", new String[] {"催しコード"});
      msg.add(o);
      return msg;
    }

    if (StringUtils.isEmpty(moysrban)) {
      JSONObject o = mu.getDbMessageObj("E30012", new String[] {"催しコード"});
      msg.add(o);
      return msg;
    }

    if (StringUtils.isEmpty(data.optString("F2"))) {
      JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
      msg.add(o);
      return msg;
    }

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.計量器マスタ
    for (TOKQJULayout colinf : TOKQJULayout.values()) {
      String val = "";

      if (colinf.getId().equals("F1")) {
        val = moyskbn;
        if (val.length() >= 8) {
          val = val.substring(0, 1);
        }
      } else if (colinf.getId().equals("F2")) {
        val = moysstdt;
        if (val.length() >= 8) {
          val = val.substring(1, 7);
        }
      } else if (colinf.getId().equals("F3")) {
        val = moysrban;
        if (val.length() >= 8) {
          val = val.substring(8);
        }
      } else {
        val = StringUtils.trim(data.optString("F2"));
      }

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

    return msg;
  }
}
