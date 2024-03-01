package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTM005Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTM005Dao(String JNDIname) {
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

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 最大行数
    String idx = DefineReport.SubGridRowNumber.MOYDEF.getVal();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) AS DUMMY union all select IDX + 1 from T1 where IDX < " + idx + ") ");
    sbSQL.append(" select");
    sbSQL.append("  T1.IDX");
    sbSQL.append(", T2.F1 as BMNCD");
    sbSQL.append(", T2.F2 as BMNKN");
    sbSQL.append(", T2.F3 as SZKCD");
    sbSQL.append(", T2.F4 as SEISEN");
    sbSQL.append(", T2.F5 as NNINFO");
    sbSQL.append(", T2.F6 as HBINFO");
    sbSQL.append(", T2.F7 as KSDAIBRUI");
    sbSQL.append(", T2.F8 as KSCHUBRUI");
    sbSQL.append(", T2.F9 as DSUEXRTPTN");
    sbSQL.append(", T2.F10 as DSUEXSUPTN");
    sbSQL.append(", T2.F11 as DSUEXJRTPTN");
    sbSQL.append(", T2.F12 as DRTEXURI");
    sbSQL.append(", T2.F13 as DRTEXTEN");
    sbSQL.append(", T2.F14 as DZNENDSDAI");
    sbSQL.append(", T2.F15 as DZNENDSCHU");
    sbSQL.append(", T2.F16 as DDNENDSDAI");
    sbSQL.append(", T2.F17 as DDNENDSCHU");
    sbSQL.append(", T2.F18 as DCUTEX");
    sbSQL.append(", T2.F19 as DCHIRAS");
    sbSQL.append(", T2.F20 as DEL");
    sbSQL.append(", T2.F21 as FLG");
    sbSQL.append(" from T1");
    sbSQL.append(" left join (");
    sbSQL.append(" select");
    sbSQL.append(" ROW_NUMBER() over (order by TMYD.BMNCD) as IDX");
    sbSQL.append(", right('0'|| TMYD.BMNCD,2) as F1"); // F1 ：部門コード
    sbSQL.append(", MTBN.BMNKN as F2"); // F2 ：部門名称（漢字）
    sbSQL.append(", right('00'|| TMYD.SZKCD,3) as F3"); // F3 ：所属コード
    sbSQL.append(", TMYD.DBMNATRKBN as F4"); // F4 ：デフォルト_部門属性
    sbSQL.append(", TMYD.NNSLIDEKBN as F5"); // F5 ：1遅スライド_納品
    sbSQL.append(", TMYD.HBSLIDEKBN as F6"); // F6 ：1遅スライド_販売
    sbSQL.append(", (case when TMYD.KSGRPKBN = 1 then 1 else 0 END) as F7"); // F7 ：検証の括り
    sbSQL.append(", (case when TMYD.KSGRPKBN = 2 then 1 else 0 END) as F8"); // F8 ：検証の括り
    sbSQL.append(", (case when TMYD.DSUEXKBN = 1 then 1 else 0 END) as F9"); // F9 ：デフォルト_数展開
    sbSQL.append(", (case when TMYD.DSUEXKBN = 2 then 1 else 0 END) as F10"); // F10 ：デフォルト_数展開
    sbSQL.append(", (case when TMYD.DSUEXKBN = 3 then 1 else 0 END) as F11"); // F11 ：デフォルト_数展開
    sbSQL.append(", (case when TMYD.DRTEXKBN = 1 then 1 else 0 END) as F12"); // F12 ：デフォルト_実績率パタン数値
    sbSQL.append(", (case when TMYD.DRTEXKBN = 2 then 1 else 0 END) as F13"); // F13 ：デフォルト_実績率パタン数値
    sbSQL.append(", (case when TMYD.DZNENDSKBN = 1 then 1 else 0 END) as F14"); // F14 ：デフォルト_前年同週
    sbSQL.append(", (case when TMYD.DZNENDSKBN = 2 then 1 else 0 END) as F15"); // F15 ：デフォルト_前年同週
    sbSQL.append(", (case when TMYD.DDNENDSKBN = 1 then 1 else 0 END) as F16"); // F16 ：デフォルト_同年同月
    sbSQL.append(", (case when TMYD.DDNENDSKBN = 2 then 1 else 0 END) as F17"); // F17 ：デフォルト_同年同月
    sbSQL.append(", TMYD.DCUTEXKBN as F18"); // F18 ：デフォルト_カット店展開
    sbSQL.append(", TMYD.DCHIRASKBN as F19"); // F19 ：デフォルト_ちらしのみ
    sbSQL.append(", '0' as F20"); // F20 ：削除区分
    sbSQL.append(", '1' as F21"); // F21 ：存在フラグ
    sbSQL.append(" from INATK.TOKMOYDEF TMYD");
    sbSQL.append(" left join INAMS.MSTBMN MTBN");
    sbSQL.append(" on TMYD.BMNCD = MTBN.BMNCD");
    sbSQL.append(" where TMYD.UPDKBN = 0");
    sbSQL.append(" order by TMYD.BMNCD");
    sbSQL.append(" ) T2");
    sbSQL.append(" on T1.IDX = T2.IDX");
    sbSQL.append(" order by T1.IDX");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
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
      option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return option;
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
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    JSONObject option = new JSONObject();
    new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null || dataArray.isEmpty() || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    int userId = userInfo.getCD_user(); // ログインユーザー


    this.createSqlTOKMOYDEF(userId, dataArray, sysdate);

    // // 排他チェック実行
    // String targetTable = "INATK.TOKMOYSYU";
    // String targetWhere = " SHUNO= ? and UPDKBN = 0";
    // ArrayList<String> targetParam = new ArrayList<String>();
    // targetParam.add(data.optString(TOKMOYSYULayout.SHUNO.getId()));
    // if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))){
    // msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
    // option.put(MsgKey.E.getKey(), msg);
    // return option;
    // }

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
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray msg = new JSONArray();
    new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }
    // // 重複チェック：部門
    // Map<String,String> mapBmn = new HashMap<>();
    // for (int j = 0; j < dataArray.size(); j++) {
    // mapBmn.put("F" + j, dataArray.getJSONObject(j).optString("F2"));
    // }
    // String target = null;
    // for (int k = 0; k < dataArray.size(); k++) {
    // target = dataArray.getJSONObject(k).optString("F2");
    // for (String val : mapBmn.values()) {
    // if (StringUtils.equals(target, val)) {
    // JSONObject o = mu.getDbMessageObj("E11040", "部門");
    // msg.add(o);
    // return msg;
    // }
    // }
    // }

    return msg;
  }

  /**
   * 催し週INSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private String createSqlTOKMOYDEF(int userId, JSONArray dataArray, String sysdate) {
    CmnDate.dbDateFormat(sysdate);
    new JSONObject();
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";

    new JSONObject();
    new JSONArray();

    // パラメータ確認
    int kryoColNum = 15; // テーブル列数
    // ログインユーザー情報取得
    values = "";
    // 更新情報
    for (int j = 0; j < dataArray.size(); j++) {
      for (int i = 1; i <= kryoColNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(0).optString(col);
        if (i == 1) { // F1 : 部門
          int intBmn = NumberUtils.toInt(dataArray.optJSONObject(j).optString("F2"));
          val = Integer.toString(intBmn);
        } else if (i == 2) { // F2 : 所属コード
          val = dataArray.optJSONObject(j).optString("F4");
        } else if (i == 3) { // F3 : 1遅スライド_販売
          val = dataArray.optJSONObject(j).optString("F7");
        } else if (i == 4) { // F4 : 1遅スライド_納品
          val = dataArray.optJSONObject(j).optString("F6");
        } else if (i == 5) { // F5 : 検証の括り
          String dai = dataArray.optJSONObject(j).optString("F8");
          if (StringUtils.equals("1", dai)) {
            val = "1";
          } else {
            val = "2";
          }
        } else if (i == 6) { // F6 : デフォルト_数展開
          String rtptn = dataArray.optJSONObject(j).optString("F10");
          String suptn = dataArray.optJSONObject(j).optString("F11");
          if (StringUtils.equals("1", rtptn)) {
            val = "1";
          } else if (StringUtils.equals("1", suptn)) {
            val = "2";
          } else {
            val = "3";
          }
        } else if (i == 7) { // F7 : デフォルト_実績率パタン数値
          String uri = dataArray.optJSONObject(j).optString("F13");
          if (StringUtils.equals("1", uri)) {
            val = "1";
          } else {
            val = "2";
          }
        } else if (i == 8) { // F8 : デフォルト_前年同週
          String dai = dataArray.optJSONObject(j).optString("F15");
          String chu = dataArray.optJSONObject(j).optString("F16");
          if (StringUtils.equals("0", dai) && StringUtils.equals("0", chu)) {
            val = "0";
          } else if (StringUtils.equals("1", dai)) {
            val = "1";
          } else {
            val = "2";
          }
        } else if (i == 9) { // F9 : デフォルト_同年同月
          String dai = dataArray.optJSONObject(j).optString("F17");
          String chu = dataArray.optJSONObject(j).optString("F18");
          if (StringUtils.equals("0", dai) && StringUtils.equals("0", chu)) {
            val = "0";
          } else if (StringUtils.equals("1", dai)) {
            val = "1";
          } else {
            val = "2";
          }
        } else if (i == 10) { // F10 : デフォルト_カット店展開
          val = dataArray.optJSONObject(j).optString("F19");
        } else if (i == 11) { // F11 : デフォルト_ちらしのみ
          val = dataArray.optJSONObject(j).optString("F20");
        } else if (i == 12) { // F12 : デフォルト_部門属性
          val = dataArray.optJSONObject(j).optString("F5");
        } else if (i == 13) { // F13 : 更新区分：
          String del = dataArray.optJSONObject(j).optString("F21");
          if (StringUtils.equals("1", del)) {
            val = DefineReport.ValUpdkbn.DEL.getVal();
          } else {
            val = DefineReport.ValUpdkbn.NML.getVal();
          }
        } else if (i == 14) { // F14 : 送信フラグ：
          val = "0";
        } else if (i == 15) { // F15 : オペレーター：
          val = "" + userId;
        }
        if (isTest) {
          if (i == 1) {
            values += val; // F1 : 部門：
          } else if (i == 15) {
            values += ", '" + val; // F17 : 更新日：
          } else {
            values += ", '" + val + "'";
          }
        } else {
          prmData.add(val);
          if (i == 1) {
            values += " ?  "; // F1 : 部門：
          } else if (i == 15) {
            values += ", ?"; // F17 : 更新日：
          } else {
            values += ", ?";
          }
        }
      }
      if (j != dataArray.size() - 1) {
        values += ", ";
      }
    }

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    // 更新SQL
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKMOYDEF ( ");
    sbSQL.append("  BMNCD"); // F1 : 部門
    sbSQL.append(", SZKCD"); // F2 : 所属コード
    sbSQL.append(", HBSLIDEKBN"); // F3 : 1遅スライド_販売
    sbSQL.append(", NNSLIDEKBN"); // F4 : 1遅スライド_納品
    sbSQL.append(", KSGRPKBN"); // F5 : 検証の括り
    sbSQL.append(", DSUEXKBN"); // F6 : デフォルト_数展開
    sbSQL.append(", DRTEXKBN"); // F7 : デフォルト_実績率パタン数値
    sbSQL.append(", DZNENDSKBN"); // F8 : デフォルト_前年同週
    sbSQL.append(", DDNENDSKBN"); // F9 : デフォルト_同年同月
    sbSQL.append(", DCUTEXKBN"); // F10 : デフォルト_カット店展開
    sbSQL.append(", DCHIRASKBN"); // F11 : デフォルト_ちらしのみ
    sbSQL.append(", DBMNATRKBN"); // F12 : デフォルト_部門属性
    sbSQL.append(", UPDKBN"); // F13 : 更新区分
    sbSQL.append(", SENDFLG"); // F14 : 送信フラグ：
    sbSQL.append(", OPERATOR"); // F15 : オペレータ
    sbSQL.append(", ADDDT"); // F16 : 登録日
    sbSQL.append(", UPDDT"); // F17 : 更新日
    sbSQL.append(") VALUES (");
    sbSQL.append(values);
    sbSQL.append(", CURRENT_TIMESTAMP ");
    sbSQL.append(", CURRENT_TIMESTAMP ");
    sbSQL.append(") ");



    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催し_デフォルト設定");

    // // クリア
    // prmData = new ArrayList<String>();
    // valueData = new Object[]{};
    // values = "";

    return sbSQL.toString();
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


  /** 催し週レイアウト */
  public enum TOKMOYDEFLayout implements MSTLayout {
    /** 部門 */
    BMNCD(1, "BMNCD", "SMALLINT"),
    /** 所属コード */
    SZKCD(2, "SZKCD", "SMALLINT"),
    /** 1遅スライド_販売 */
    HBSLIDEKBN(3, "HBSLIDEKBN", "SMALLINT"),
    /** 1遅スライド_納品 */
    NNSLIDEKBN(4, "NNSLIDEKBN", "SMALLINT"),
    /** 検証の括り */
    KSGRPKBN(5, "KSGRPKBN", "SMALLINT"),
    /** デフォルト_数展開 */
    DSUEXKBN(6, "DSUEXKBN", "SMALLINT"),
    /** デフォルト_実績率パタン数値 */
    DRTEXKBN(7, "DRTEXKBN", "SMALLINT"),
    /** デフォルト_前年同週 */
    DZNENDSKBN(8, "DZNENDSKBN", "SMALLINT"),
    /** デフォルト_同年同月 */
    DDNENDSKBN(9, "DDNENDSKBN", "SMALLINT"),
    /** デフォルト_カット店展開 */
    DCUTEXKBN(10, "DCUTEXKBN", "SMALLINT"),
    /** デフォルト_ちらしのみ */
    DCHIRASKBN(11, "DCHIRASKBN", "SMALLINT"),
    /** デフォルト_部門属性 */
    DBMNATRKBN(12, "DBMNATRKBN", "SMALLINT"),
    /** 更新区分 */
    UPDKBN(13, "UPDKBN", "SMALLINT"),
    /** 送信フラグ */
    SENDFLG(14, "SENDFLG", "SMALLINT"),
    /** オペレータ */
    OPERATOR(15, "OPERATOR", "VARCHAR(20)"),
    /** 登録日 */
    ADDDT(16, "ADDDT", "TIMESTMP"),
    /** 更新日 */
    UPDDT(17, "UPDDT", "TIMESTMP");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TOKMOYDEFLayout(Integer no, String col, String typ) {
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
}
