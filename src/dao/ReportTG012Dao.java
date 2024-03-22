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
public class ReportTG012Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG012Dao(String JNDIname) {
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

    String szqayyyymm = getMap().get("QAYYYYMM"); // 月度（左）
    String szqaend = getMap().get("QAEND"); // 月度（右）
    String szhnctlflg = getMap().get("HNCT"); // アンケート本部ctl

    if (szhnctlflg == null) {
      szhnctlflg = "0";
    }

    if (szqayyyymm.length() == 0 || szqaend.length() == 0) {
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    paramData.add(szqayyyymm);
    paramData.add(szqaend);
    paramData.add(szhnctlflg);

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" ,ZEN AS ( ");
    sbSQL.append(" SELECT ");
    sbSQL.append(" T1.MOYSKBN ");
    sbSQL.append(" , T1.MOYSSTDT ");
    sbSQL.append(" , T1.MOYSRBAN ");
    sbSQL.append(" , SUM(CASE WHEN T2.MBSYFLG <> '1' THEN 1 ELSE 0 END) AS ZEN ");
    sbSQL.append(" from ");
    sbSQL.append(" INATK.TOKTG_QAGP T1 ");
    sbSQL.append(" , INATK.TOKTG_QATEN T2 ");
    sbSQL.append(" WHERE ");
    sbSQL.append(" T1.ITMANSFLG = 0 ");
    sbSQL.append(" AND T1.SCHANSFLG = 0 ");
    sbSQL.append(" AND T1.MOYSKBN = T2.MOYSKBN ");
    sbSQL.append(" AND T1.MOYSSTDT = T2.MOYSSTDT ");
    sbSQL.append(" AND T1.MOYSRBAN = T2.MOYSRBAN ");
    sbSQL.append(" AND T2.LDTENKBN = 1 ");
    sbSQL.append(" GROUP BY ");
    sbSQL.append(" T1.MOYSKBN ");
    sbSQL.append(" , T1.MOYSSTDT ");
    sbSQL.append(" , T1.MOYSRBAN ");
    sbSQL.append(" ) ");
    sbSQL.append(" SELECT ");
    sbSQL.append(" CONCAT(CONCAT(TTKH.MOYSKBN,'-'),CONCAT(TTKH.MOYSSTDT,CONCAT('-',right ('000' || RTRIM(CHAR (TTKH.MOYSRBAN)), 3)))) as MOYOOSI ");
    sbSQL.append(" ,TMYD.MOYKN ");
    sbSQL.append(" , DATE_FORMAT(DATE_FORMAT(TMYD.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYD.HBSTDT, '%Y%m%d'))) ");
    sbSQL.append(" ||'～'|| ");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT(TMYD.HBEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(TMYD.HBEDDT, '%Y%m%d'))) as KIKAN ");
    sbSQL.append(" , CASE ");
    sbSQL.append(" WHEN T1.ZEN = 0 ");
    sbSQL.append(" THEN '○' ");
    sbSQL.append(" ELSE '×' ");
    sbSQL.append(" END AS ZEN ");
    sbSQL.append(" , COUNT(TTQT.TENCD) AS CNT ");
    sbSQL.append(" , sum(case when TTQT.MBSYFLG = 1 then 1 else 0 end) as TCNT ");
    sbSQL.append(" , sum(case when TTQT.MBSYFLG = 2 then 1 else 0 end) as NCNT ");
    sbSQL.append(" , TTKH.SIMEFLG1_LD ");
    sbSQL.append(" , TTKH.SIMEFLG2_LD ");
    sbSQL.append(" , TTKH.SIMEFLG_MB ");
    sbSQL.append(" , TTKH.MOYSKBN ");
    sbSQL.append(" , TTKH.MOYSSTDT ");
    sbSQL.append(" , TTKH.MOYSRBAN ");
    sbSQL.append(" , DATE_FORMAT(TTKH.UPDDT, '%Y%m%d%H%i%s%f') ");
    sbSQL.append(" , TTKH.JLSTCREFLG ");
    sbSQL.append(" from ");
    sbSQL.append(" INATK.TOKTG_KHN TTKH ");
    sbSQL.append(" LEFT JOIN INATK.TOKMOYCD TMYD ");
    sbSQL.append(" ON TTKH.MOYSKBN = TMYD.MOYSKBN ");
    sbSQL.append(" AND TTKH.MOYSSTDT = TMYD.MOYSSTDT ");
    sbSQL.append(" AND TTKH.MOYSRBAN = TMYD.MOYSRBAN ");
    sbSQL.append(" , INATK.TOKTG_QATEN TTQT ");
    sbSQL.append(" LEFT JOIN ZEN AS T1 ");
    sbSQL.append(" ON TTQT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append(" AND TTQT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append(" AND TTQT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append(" WHERE ");
    sbSQL.append(" TTKH.QAYYYYMM = ? ");
    sbSQL.append(" AND TTKH.QAENO = ? ");
    sbSQL.append(" AND TTKH.HNCTLFLG = ? ");
    sbSQL.append(" AND TTKH.MOYSKBN = TTQT.MOYSKBN ");
    sbSQL.append(" AND TTKH.MOYSSTDT = TTQT.MOYSSTDT ");
    sbSQL.append(" AND TTKH.MOYSRBAN = TTQT.MOYSRBAN ");
    sbSQL.append(" GROUP BY  ");
    sbSQL.append(" TTKH.MOYSKBN ");
    sbSQL.append(" , TTKH.MOYSSTDT ");
    sbSQL.append(" , TTKH.MOYSRBAN ");
    sbSQL.append(" , TTKH.SIMEFLG1_LD ");
    sbSQL.append(" , TTKH.SIMEFLG2_LD ");
    sbSQL.append(" , TTKH.SIMEFLG_MB ");
    sbSQL.append(" , TMYD.HBSTDT ");
    sbSQL.append(" , TMYD.HBEDDT ");
    sbSQL.append(" , TMYD.MOYKN ");
    sbSQL.append(" , TTKH.UPDDT ");
    sbSQL.append(" , TTKH.JLSTCREFLG ");
    sbSQL.append(" , ZEN ");
    sbSQL.append(" ORDER BY MOYOOSI ");

    setParamData(paramData);

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
    // JSONArray msg = this.check(map, userInfo, sysdate);

    // if(msg.size() > 0){
    // msgObj.put(MsgKey.E.getKey(), msg);
    // return msgObj;
    // }
    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    // msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
    return msgObj;
  }

  boolean isTest = false;

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

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";
    int kryoColNum = 6; // テーブル列数
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONObject msgObj = new JSONObject();
    JSONArray msg = new JSONArray();
    JSONObject option = new JSONObject();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    map.get("SENDBTNID");
    map.get("SEL_BMONCD");
    String updateRows = ""; // 更新データ


    // 更新情報
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (int i = 1; i <= kryoColNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (isTest) {
          if (StringUtils.isEmpty(val)) {
            values += ", null";
          } else {
            if (i == 1) {
              values += " '" + val + "'";
            } else {
              values += ", '" + val + "'";
            }
          }
        } else {
          if (StringUtils.isEmpty(val)) {
            values += ", null";
          } else {
            if (i == 1) {
              prmData.add(val);
              values += " ?";
            } else {
              prmData.add(val);
              values += ", ?";
            }
          }

        }
      }
      values += ", " + DefineReport.ValUpdkbn.NML.getVal();
      values += ", " + DefineReport.Values.SENDFLG_UN.getVal();
      values += ", '" + userId + "'";
      values += ", CURRENT_TIMESTAMP ";
      values += ", CURRENT_TIMESTAMP ";
      updateRows += ",(" + values + ")";
    }
    updateRows = StringUtils.removeStart(updateRows, ",");

    if (values.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return msgObj;
    }
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL = new StringBuffer();;

    sbSQL.append("REPLACE INTO INATK.TOKTG_KHN ( ");
    sbSQL.append("  MOYSKBN"); // 配送グループ：
    sbSQL.append(", MOYSSTDT"); // 配送グループ名称（カナ）：
    sbSQL.append(", MOYSRBAN"); // 配送グループ名称（漢字）：
    sbSQL.append(", SIMEFLG1_LD"); // エリア区分：
    sbSQL.append(", SIMEFLG2_LD"); // エリア区分：
    sbSQL.append(", SIMEFLG_MB"); // エリア区分：
    sbSQL.append(", UPDKBN"); // 更新区分：
    sbSQL.append(", SENDFLG");// 送信区分：
    sbSQL.append(", OPERATOR "); // オペレーター：
    sbSQL.append(", ADDDT "); // 登録日：
    sbSQL.append(", UPDDT "); // 更新日：
    sbSQL.append(") VALUES ");
    sbSQL.append(updateRows);

    if (msg.size() > 0) {
      // 重複チェック_エラー時
      msgObj.put(MsgKey.E.getKey(), msg);
    } else {
      // 更新処理実行
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(sbSQL);

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

          targetTable = "INATK.TOKTG_KHN";
          targetWhere = "MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? ";
          targetParam = new ArrayList<String>();
          targetParam.add(data.optString("F1"));
          targetParam.add(data.optString("F2"));
          targetParam.add(data.optString("F3"));

          if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F7"))) {
            // rownum = data.optString("idx");
            rownum = String.valueOf(i + 1);
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[] {rownum}));
            option.put(MsgKey.E.getKey(), msg);
            return option;
          }
        }
      }

      int count = super.executeSQL(sbSQL.toString(), prmData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("全店特売（アンケート有）基本を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }
    }
    return msgObj;
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
    BMNCD(3, "BMNCD", "SMALLINT");


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

    List<JSONObject> msgList = this.checkData(isNew, map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }



  public List<JSONObject> checkData(boolean isNew, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）

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

}

