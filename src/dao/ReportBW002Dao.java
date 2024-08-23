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
import common.Defines;
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.Reportx002Dao.CSVSHNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBW002Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBW002Dao(String JNDIname) {
    super(JNDIname);
  }

  @Override
  public boolean selectForDL() {

    // 検索コマンド生成
    String command = createCommandForDl();

    // 出力用検索条件生成
    outputQueryList();

    // 検索実行
    return super.selectBySQL(command);
  }

  private String createCommandForDl() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szBmncd = getMap().get("BMNCD"); // 催し区分
    String szHbstdt = getMap().get("HBSTDT"); // 催しコード（催し開始日）
    String szDummycd = getMap().get("DUMMYCD"); // 催し連番
    String szWaribiki = getMap().get("WARIBIKI"); // 店コード
    String szSeisi = getMap().get("SEISI"); // ボタンID

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    paramData.add(szHbstdt);
    paramData.add(szBmncd);
    paramData.add(szWaribiki);
    paramData.add(szSeisi);
    paramData.add(szDummycd);
    setParamData(paramData);


    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append(" 'D1' ");
    sbSQL.append(", TRKK.HBSTDT"); // F2
    sbSQL.append(", TRKK.BMNCD");// F3
    sbSQL.append(", MMSH1.NMKN");// F5
    sbSQL.append(", MMSH2.NMKN");// F6
    sbSQL.append(", TRKK.DUMMYCD");
    sbSQL.append(", MTSH.POPKN");
    sbSQL.append(", ''");
    sbSQL.append(", ''");
    sbSQL.append(" from INAMS.MSTSHN MTSH");
    sbSQL.append(" left join INATK.TOKRS_SHN TRSH");
    sbSQL.append(" on MTSH.SHNCD = TRSH.SHNCD");
    sbSQL.append(" left join INATK.TOKRS_KKK TRKK");
    sbSQL.append(" on TRKK.HBSTDT = TRSH.HBSTDT");
    sbSQL.append(" and TRKK.BMNCD = TRSH.BMNCD");
    sbSQL.append(" and TRKK.WRITUKBN = TRSH.WRITUKBN");
    sbSQL.append(" and TRKK.SEICUTKBN = TRSH.SEICUTKBN");
    sbSQL.append(" and TRKK.DUMMYCD = TRSH.DUMMYCD");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH1");
    sbSQL.append(" on MMSH1.MEISHOCD = TRSH.WRITUKBN");
    sbSQL.append(" and MMSH1.MEISHOKBN = 10302");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH2");
    sbSQL.append(" on MMSH2.MEISHOCD = TRSH.SEICUTKBN");
    sbSQL.append(" and MMSH2.MEISHOKBN = 10303");
    sbSQL.append(" where ");
    sbSQL.append(" TRSH.HBSTDT = ? ");
    sbSQL.append(" and TRSH.BMNCD = ? ");
    sbSQL.append(" and TRSH.WRITUKBN = ? ");
    sbSQL.append(" and TRSH.SEICUTKBN = ? ");
    sbSQL.append(" and TRSH.DUMMYCD = ? ");
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
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

    String szBmncd = getMap().get("BMNCD"); // 催し区分
    String szHbstdt = getMap().get("HBSTDT"); // 催しコード（催し開始日）
    String szDummycd = getMap().get("DUMMYCD"); // 催し連番
    String szWaribiki = getMap().get("WARIBIKI"); // 店コード
    String szSeisi = getMap().get("SEISI"); // ボタンID
    getMap().get("SENDBTNID");

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    for (int i = 0; i < 9; i++) {
      paramData.add(szHbstdt);
    }
    paramData.add(szBmncd);
    paramData.add(szWaribiki);
    paramData.add(szSeisi);
    paramData.add(szDummycd);
    setParamData(paramData);

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("WITH RECURSIVE T1(IDX) as ( select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < 500) ");
    sbSQL.append(",WEEK AS (SELECT CWEEK ,'(' || JWEEK || ')' AS JWEEK ,JWEEK AS JWEEK2 FROM ( SELECT 1 AS CWEEK , '日' AS JWEEK "
        + "UNION SELECT 2 , '月' UNION SELECT 3 ,'火' UNION SELECT 4 ,'水' UNION SELECT 5 ,'木' UNION SELECT 6 ,'金' UNION SELECT 7 ,'土' ) T1 )");
    sbSQL.append(" select");
    sbSQL.append("  T1.IDX");
    sbSQL.append(", T2.F1 as SHNCD"); // F2
    sbSQL.append(", T2.F2 as MAKERKN");// F3
    sbSQL.append(", T2.F3 as SHNKN");// F4
    sbSQL.append(", T2.F4 as KIKKN");// F5
    sbSQL.append(", T2.F5 as IRISU");// F6
    sbSQL.append(", T2.F6");
    sbSQL.append(", T2.F7 as RG_BAIKAAM");
    sbSQL.append(", T2.F8 as RG_GENKAAM");
    sbSQL.append(", T2.F9 as BAIKAAM");
    sbSQL.append(", T2.F10 ");// F11
    sbSQL.append(", T2.F11 as GENKAAM");
    sbSQL.append(", T2.F12 as SHOBRUIKN");
    sbSQL.append(", T2.F13 as ADDDT");
    sbSQL.append(", T2.F14 as UPDDT");// F15
    sbSQL.append(", T2.F15 as OPERATOR");
    sbSQL.append(", T2.F16 as UPDKBN");
    sbSQL.append(", T2.F17 as KANRINO");// F18
    sbSQL.append(", T2.F18 ");// F18
    sbSQL.append(" from T1");
    sbSQL.append(" left join (");
    sbSQL.append(" select");
    sbSQL.append(" ROW_NUMBER() over (order by TRSH.SHNCD) as IDX");
    sbSQL.append(", TRSH.SHNCD as F1"); // F1 ：商品コード
    sbSQL.append(", TRSH.MAKERKN as F2"); // F2 ：メーカー名（漢字）
    sbSQL.append(", TRSH.SHNKN as F3"); // F3 ：商品名（漢字）
    sbSQL.append(", TRSH.KIKKN as F4"); // F4 ：規格（漢字）
    sbSQL.append(", TRSH.IRISU as F5"); // F5 ：入り数
    sbSQL.append("," + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@BAIKA", "MTSH.RG_BAIKAAM").replaceAll("@DT", "?") + " as F6");
    sbSQL.append(", MTSH.RG_BAIKAAM as F7"); // F7 ：標準総額売価
    sbSQL.append(", MTSH.RG_GENKAAM as F8"); // F8 ：標準本体売価
    sbSQL.append(", TRSH.BAIKAAM as F9"); // F9 ：標準売価
    sbSQL.append("," + DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "TRSH.BAIKAAM").replaceAll("@DT", "?") + " as F10");
    sbSQL.append(", TRSH.GENKAAM as F11"); // F11 ：割引原価
    sbSQL.append(", MSBR.SHOBRUIKN as F12"); // F12 ：分類
    sbSQL.append(", DATE_FORMAT(TRSH.ADDDT, '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(DATE_FORMAT(TRSH.ADDDT, '%Y%m%d'),'%Y%m%d'))) as F13"); // F13 ：登録日
    sbSQL.append(", DATE_FORMAT(TRSH.UPDDT, '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(DATE_FORMAT(TRSH.UPDDT, '%Y%m%d'),'%Y%m%d'))) as F14"); // F14 ：更新日
    sbSQL.append(", TRSH.OPERATOR  as F15"); // F15 ：オペレーター
    sbSQL.append(", COALESCE(TRSH.UPDKBN,0) as F16"); // F16 ：更新区分
    sbSQL.append(", TRSH.KANRINO as F17"); // F17 管理番号
    sbSQL.append(", '1' as F18");
    sbSQL.append(" from INAMS.MSTSHN MTSH");
    sbSQL.append(" left join INATK.TOKRS_SHN TRSH");
    sbSQL.append(" on TRSH.SHNCD = MTSH.SHNCD");
    sbSQL.append(" left join INAMS.MSTSHOBRUI MSBR");
    sbSQL.append(" on MTSH.DAICD = MSBR.DAICD");
    sbSQL.append(" and MTSH.CHUCD = MSBR.CHUCD");
    sbSQL.append(" and MTSH.SHOCD = MSBR.SHOCD");
    sbSQL.append(" and MTSH.BMNCD = MSBR.BMNCD");
    sbSQL.append(" left outer join INAMS.MSTSHN M0 on M0.SHNCD = TRSH.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTBMN M1 on M1.BMNCD = TRSH.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1");
    sbSQL.append(" where TRSH.HBSTDT = ? ");
    sbSQL.append(" and TRSH.BMNCD = ? ");
    sbSQL.append(" and TRSH.WRITUKBN = ? ");
    sbSQL.append(" and TRSH.SEICUTKBN = ? ");
    sbSQL.append(" and TRSH.DUMMYCD = ? ");
    sbSQL.append(" and TRSH.UPDKBN = ");
    sbSQL.append(DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" order by TRSH.KANRINO) T2");
    sbSQL.append(" on T1.IDX = T2.IDX");
    // sbSQL.append(" order by T1.IDX");
    sbSQL.append(" order by IDX");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
  }

  /**
   * 割引総額売価取得
   *
   * @throws Exception
   */
  public String createSqlSelMSTSHN(JSONObject obj) {

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append(DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "RG_GENKAAM").replaceAll("@DT", "TRKK.HBSTDT") + " as F11 ");
    sbSQL.append(" from INAMS.MSTSHN M0 left join INATK.TOKRS_KKK TRKK  ");
    sbSQL.append(" on TRKK.BMNCD = ? ");
    sbSQL.append(" and TRKK.WRITUKBN = ? ");
    sbSQL.append(" and TRKK.SEICUTKBN = ? ");
    sbSQL.append(" and TRKK.DUMMYCD = ? ");
    sbSQL.append(" and TRKK.HBSTDT = ? ");
    sbSQL.append(" and TRKK.UPDKBN <> 1 ");
    sbSQL.append(" left join INAMS.MSTSHOBRUI MSBR  ");
    sbSQL.append(" on M0.DAICD = MSBR.DAICD  ");
    sbSQL.append(" and M0.CHUCD = MSBR.CHUCD ");
    sbSQL.append(" and M0.SHOCD = MSBR.SHOCD  ");
    sbSQL.append(" and MSBR.BMNCD = M0.BMNCD ");
    sbSQL.append(" left outer join INAMS.MSTBMN M1 ");
    sbSQL.append(" on M1.BMNCD = M0.BMNCD ");
    sbSQL.append(" and COALESCE(M1.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M2 ");
    sbSQL.append(" on M2.ZEIRTKBN = M0.ZEIRTKBN  ");
    sbSQL.append(" and COALESCE(M2.UPDKBN, 0) <> 1  ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M3 ");
    sbSQL.append(" on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD  ");
    sbSQL.append(" and COALESCE(M3.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M4 ");
    sbSQL.append(" on M4.ZEIRTKBN = M1.ZEIRTKBN  ");
    sbSQL.append(" and COALESCE(M4.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M5 ");
    sbSQL.append(" on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD ");
    sbSQL.append(" and COALESCE(M5.UPDKBN, 0) <> 1  ");
    sbSQL.append(" where M0.SHNCD = ?  ");
    sbSQL.append(" and M0.UPDKBN = 0 ");

    return sbSQL.toString();
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szSEQ = map.get("SEQ"); // 催し区分

    ArrayList<String> paramData = new ArrayList<String>();

    paramData.add(szSEQ);
    setParamData(paramData);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append("   DATE_FORMAT(DATE_FORMAT(CTRK.HBSTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(CTRK.HBSTDT, '%Y%m%d'))) as F1");
    sbSQL.append(" , right('0'|| CTRK.BMNCD,2) as F2");
    sbSQL.append(" , CTRK.MEISHOKN as F3");
    sbSQL.append(" , MMSH1.NMKN as F4");
    sbSQL.append(" , MMSH2.NMKN as F5");
    sbSQL.append(" , CTRK.DUMMYCD as F6");
    sbSQL.append(" , MTSH.POPKN as F7");
    sbSQL.append(" from INATK.CSVTOK_RSKKK CTRK");
    sbSQL.append(" left join");
    sbSQL.append(" INAMS.MSTSHN MTSH");
    sbSQL.append(" on MTSH.SHNCD = CTRK.DUMMYCD");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH1");
    sbSQL.append(" on MMSH1.MEISHOCD = CTRK.WRITUKBN");
    sbSQL.append(" and MMSH1.MEISHOKBN = 10302");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH2");
    sbSQL.append(" on MMSH2.MEISHOCD = CTRK.SEICUTKBN");
    sbSQL.append(" and MMSH2.MEISHOKBN = 10303");
    sbSQL.append(" where CTRK.SEQ = ? ");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
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
    // msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
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

  /** 冷凍食品_商品 */
  public enum TOKRSSHNLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "SMALLINT"),
    /** 商品名 */
    SHNKN(2, "SHNKN", "VARCHAR(40)"),
    /** 規格 */
    KIKKN(3, "KIKKN", "VARCHAR(46)"),
    /** 入数 */
    IRISU(4, "IRISU", "SMALLINT"),
    /** 割引総額売価 */
    BAIKAAM(5, "GENKAAM", "DECIMAL(8,2)"),
    /** 割引原価 */
    GENKAAM(6, "GENKAAM", "DECIMAL(8,2)");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TOKRSSHNLayout(Integer no, String col, String typ) {
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

    map.get("SENDBTNID");

    MessageUtility mu = new MessageUtility();

    List<JSONObject> msgList = this.checkData(map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public List<JSONObject> checkData(HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）
  ) {
    JSONArray msg = new JSONArray();
    String ErrTblNm = "TOKRS_SHN";
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.optJSONObject(i);
      if (!data.optString("F8").equals("1")) {
        // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
        // 1.冷凍食品_商品
        for (TOKRSSHNLayout colinf : TOKRSSHNLayout.values()) {
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
              o.put(CSVSHNLayout.ERRCD.getCol(), o.optString(MessageUtility.CD));
              o.put(CSVSHNLayout.ERRTBLNM.getCol(), ErrTblNm);
              o.put(CSVSHNLayout.ERRFLD.getCol(), colinf.getCol());
              o.put(CSVSHNLayout.ERRVL.getCol(), o.optString(MessageUtility.MSG));
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
              o.put(CSVSHNLayout.ERRCD.getCol(), o.optString(MessageUtility.CD));
              o.put(CSVSHNLayout.ERRTBLNM.getCol(), ErrTblNm);
              o.put(CSVSHNLayout.ERRFLD.getCol(), colinf.getCol());
              o.put(CSVSHNLayout.ERRVL.getCol(), o.optString(MessageUtility.MSG));
              msg.add(o);
              return msg;
            }
          }
        }
        if (this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), data.optString(TOKRSSHNLayout.SHNCD.getId()))) {
          JSONObject o = mu.getDbMessageObj("E20160", new String[] {});
          o.put(CSVSHNLayout.ERRCD.getCol(), o.optString(MessageUtility.CD));
          o.put(CSVSHNLayout.ERRTBLNM.getCol(), ErrTblNm);
          o.put(CSVSHNLayout.ERRFLD.getCol(), TOKRSSHNLayout.SHNCD.getCol());
          o.put(CSVSHNLayout.ERRVL.getCol(), o.optString(MessageUtility.MSG));
          msg.add(o);
          return msg;
        }
      }
    }
    return msg;
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
    // 商品コード
    tbl = "INAMS.MSTSHN";
    col = "SHNCD";

    paramData.add(value);
    sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);

    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
      return false;
    }
    return true;
  }

  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    JSONObject msgObj = new JSONObject();
    CmnDate.dbDateFormat(sysdate);
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    // if(userInfo==null){
    // return "";
    // }
    String userId = userInfo.getId(); // ログインユーザー
    String szBmncd = map.get("BMNCD"); // 部門コード
    String szHbstdt = map.get("HBSTDT"); // 販売開始日
    String szDummycd = map.get("DUMMYCD"); // ダミーコード
    String szWaribiki = map.get("WARIBIKI"); // 割引率区分
    String szSeisi = map.get("SEISI"); // 正視・カット

    ArrayList<String> paramData = new ArrayList<String>();
    // タイトル情報(任意)設定

    StringBuffer sbSQL = new StringBuffer();
    boolean isTest = false;
    String updateRows = "";
    String values = "";
    String shnValues = "";

    String shncd2 = "";
    int x = 0;
    for (x = 0; x < dataArray.size(); x++) {
      if (!dataArray.optJSONObject(x).optString("F8").equals("1")) {
        shncd2 = dataArray.optJSONObject(x).optString("F1");
        break;
      }
    }

    // 既存商品データか確認を行う
    if (!shncd2.equals("")) {
      sbSQL.append("  with SHN as ( ");
      if (dataArray.size() > x) {
        String sql = "";
        for (; x < dataArray.size(); x++) {
          if (!dataArray.optJSONObject(x).optString("F8").equals("1")) {
            sql += "union all select DISTINCT  '" + dataArray.optJSONObject(x).optString("F1") + "' as SHNCD from INATK.TOKRS_SHN TRSH ";
          }
        }
        if (StringUtils.isNotEmpty(sql)) {
          sbSQL.append(StringUtils.removeStart(sql, "union all"));
        }
      }
      sbSQL.append("  )select SHNCD from SHN where not exists (");
      sbSQL.append("  select * from INATK.TOKRS_SHN TRSH where SHN.SHNCD = TRSH.SHNCD ");
      sbSQL.append("  and TRSH.HBSTDT = ");
      sbSQL.append(szHbstdt);
      sbSQL.append(" and TRSH.BMNCD = ");
      sbSQL.append(szBmncd);
      sbSQL.append(" and TRSH.WRITUKBN = ");
      sbSQL.append(szWaribiki);
      sbSQL.append(" and TRSH.SEICUTKBN = ");
      sbSQL.append(szSeisi);
      sbSQL.append(" and TRSH.DUMMYCD = ");
      sbSQL.append(szDummycd);
      sbSQL.append(" and COALESCE(TRSH.UPDKBN, 0) <> 1)");

      ItemList iL = new ItemList();
      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      // 企画に新しい冷凍食品レコードの場合
      if (array.size() > 0) {
        sbSQL = new StringBuffer();
        sbSQL.append("  select");
        sbSQL.append("   SYSR.SUMI_KANRINO+1 as F1 ");
        sbSQL.append(" from");
        sbSQL.append(" INATK.SYSRS SYSR");
        sbSQL.append(" where");
        sbSQL.append(" SYSR.HBSTDT = ");
        sbSQL.append(szHbstdt);
        sbSQL.append(" and SYSR.BMNCD = ");
        sbSQL.append(szBmncd);
        sbSQL.append(" and SYSR.WRITUKBN = ");
        sbSQL.append(szWaribiki);
        sbSQL.append(" and SYSR.SEICUTKBN = ");
        sbSQL.append(szSeisi);
        sbSQL.append(" and SYSR.DUMMYCD = ");
        sbSQL.append(szDummycd);

        iL = new ItemList();
        @SuppressWarnings("static-access")
        JSONArray array2 = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
        int kanriNo;
        boolean New;
        if (array2.size() == 0) {
          kanriNo = 1;
          New = true;
        } else {
          kanriNo = Integer.parseInt(array2.optJSONObject(0).optString("F1"));
          New = false;
        }
        String shncd = "";
        int newColNum = 13;
        paramData = new ArrayList<String>();
        for (int i = 0; i < dataArray.size(); i++) {
          shncd = dataArray.optJSONObject(i).optString("F1").trim();;
          for (int j = 0; j < array.size(); j++) {
            String oldShncd = array.getJSONObject(j).optString("SHNCD").trim();
            values = "";
            if (shncd.equals(oldShncd)) {
              for (int k = 1; k <= newColNum; k++) {
                // String col = "F" + k;
                // String val = dataArray.optJSONObject(i).optString(col);
                if (isTest) {
                  if (k == 1) {
                    values += " '" + szHbstdt + "'";// F1
                  } else if (k == 2) {
                    values += ", '" + szBmncd + "'";// F2
                  } else if (k == 3) {
                    values += ", '" + szWaribiki + "'";// F3
                  } else if (k == 4) {
                    values += ", '" + szSeisi + "'";// F4
                  } else if (k == 5) {
                    values += ", '" + szDummycd + "'";// F5
                  } else if (k == 6) {
                    values += ", '" + kanriNo + "'";// F6
                    kanriNo++;
                  } else if (k == 7) {
                    values += ", '" + oldShncd + "'";// F7
                  } else if (k == 8) {
                    values += ", '" + dataArray.optJSONObject(i).optString("F2") + "'";
                  } else if (k == 9) {
                    values += ", '" + dataArray.optJSONObject(i).optString("F3") + "'";
                  } else if (k == 10) {
                    values += ", '" + dataArray.optJSONObject(i).optString("F4") + "'";
                  } else if (k == 11) {
                    values += ", '" + dataArray.optJSONObject(i).optString("F5") + "'";
                  } else if (k == 12) {
                    values += ", '" + dataArray.optJSONObject(i).optString("F6") + "'";
                  } else if (k == 13) {
                    values += ", '" + dataArray.optJSONObject(i).optString("F7") + "'";
                  }
                } else {
                  if (k == 1) {
                    paramData.add(szHbstdt);
                  } else if (k == 2) {
                    paramData.add(szBmncd);
                  } else if (k == 3) {
                    paramData.add(szWaribiki);
                  } else if (k == 4) {
                    paramData.add(szSeisi);
                  } else if (k == 5) {
                    paramData.add(szDummycd);
                  } else if (k == 6) {
                    paramData.add(Integer.toString(kanriNo));
                    kanriNo++;
                  } else if (k == 7) {
                    paramData.add(oldShncd);
                  } else if (k == 8) {
                    paramData.add(dataArray.optJSONObject(i).optString("F2"));
                  } else if (k == 9) {
                    paramData.add(dataArray.optJSONObject(i).optString("F3"));
                  } else if (k == 10) {
                    paramData.add(dataArray.optJSONObject(i).optString("F4"));
                  } else if (k == 11) {
                    paramData.add(dataArray.optJSONObject(i).optString("F5"));
                  } else if (k == 12) {
                    paramData.add(dataArray.optJSONObject(i).optString("F6"));
                  } else if (k == 13) {
                    paramData.add(dataArray.optJSONObject(i).optString("F7"));
                  }
                  // prmData.add(values);
                  values += ", ?";
                }
              }
              // updateRows += ",("+values+")";
              updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";
            }
          }
        }
        updateRows = StringUtils.removeStart(updateRows, ",");
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.TOKRS_SHN ( ");
        sbSQL.append(" HBSTDT "); // 販売開始日
        sbSQL.append(",BMNCD "); // 部門
        sbSQL.append(",WRITUKBN "); // 割引率区分
        sbSQL.append(",SEICUTKBN "); // 正規・カット
        sbSQL.append(",DUMMYCD "); // ダミーコード
        sbSQL.append(",KANRINO "); // 管理番号
        sbSQL.append(",SHNCD "); // 商品コード
        sbSQL.append(",MAKERKN "); // メーカー名
        sbSQL.append(",SHNKN "); // 商品名称
        sbSQL.append(",KIKKN "); // 規格名称
        sbSQL.append(",IRISU "); // 入数
        sbSQL.append(",BAIKAAM "); // 売価
        sbSQL.append(",GENKAAM "); // 原価
        sbSQL.append(",UPDKBN "); // 更新フラグ
        sbSQL.append(",SENDFLG "); // 送信フラグ
        sbSQL.append(",OPERATOR "); // オペレーターコード
        sbSQL.append(",ADDDT "); // 登録日
        sbSQL.append(",UPDDT "); // 更新日
        sbSQL.append(") ");
        sbSQL.append("SELECT ");
        sbSQL.append(" HBSTDT "); // 販売開始日
        sbSQL.append(",BMNCD "); // 部門
        sbSQL.append(",WRITUKBN "); // 割引率区分
        sbSQL.append(",SEICUTKBN "); // 正規・カット
        sbSQL.append(",DUMMYCD "); // ダミーコード
        sbSQL.append(",KANRINO "); // 管理番号
        sbSQL.append(",SHNCD "); // 商品コード
        sbSQL.append(",MAKERKN "); // メーカー名
        sbSQL.append(",SHNKN "); // 商品名称
        sbSQL.append(",KIKKN "); // 規格名称
        sbSQL.append(",IRISU "); // 入数
        sbSQL.append(",BAIKAAM "); // 売価
        sbSQL.append(",GENKAAM "); // 原価
        sbSQL.append(",UPDKBN "); // 更新フラグ
        sbSQL.append(",SENDFLG "); // 送信フラグ
        sbSQL.append(",OPERATOR "); // オペレーターコード
        sbSQL.append(",ADDDT "); // 登録日
        sbSQL.append(",UPDDT "); // 更新日
        sbSQL.append("FROM ( ");
        sbSQL.append("SELECT ");
        sbSQL.append(" HBSTDT"); // 販売開始日 F1
        sbSQL.append(",BMNCD"); // 部門 F2
        sbSQL.append(",WRITUKBN"); // 割引率区分 F3
        sbSQL.append(",SEICUTKBN"); // 正規・カット F4
        sbSQL.append(",DUMMYCD"); // ダミーコード F5
        sbSQL.append(",KANRINO"); // 管理番号 F6
        sbSQL.append(",SHNCD"); // 商品コード F7
        sbSQL.append(",SHNKN"); // 商品名称
        sbSQL.append(",KIKKN"); // 規格名称
        sbSQL.append(",IRISU"); // 入数
        sbSQL.append(",BAIKAAM"); // 売価
        sbSQL.append(",GENKAAM"); // 原価
        sbSQL.append(",MAKERKN"); // メーカ名
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
        sbSQL.append(",0 as SENDFLG"); // 送信フラグ
        sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
        sbSQL.append(", CURRENT_TIMESTAMP AS ADDDT "); // 登録日：
        sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT "); // 更新日：
        sbSQL.append(" FROM (values ROW" + updateRows + ") as T1(");
        sbSQL.append(" HBSTDT"); // 販売開始日
        sbSQL.append(",BMNCD"); // 部門
        sbSQL.append(",WRITUKBN"); // 割引率区分
        sbSQL.append(",SEICUTKBN"); // 正規・カット
        sbSQL.append(",DUMMYCD"); // ダミーコード
        sbSQL.append(",KANRINO"); // 管理番号
        sbSQL.append(",SHNCD"); // 商品コード
        sbSQL.append(",SHNKN"); // 商品名称
        sbSQL.append(",KIKKN"); // 規格名称
        sbSQL.append(",IRISU"); // 入数
        sbSQL.append(",BAIKAAM"); // 売価
        sbSQL.append(",GENKAAM"); // 原価
        sbSQL.append(",MAKERKN"); // メーカー名
        sbSQL.append(")) as T1 ");
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("SHNCD = VALUES(SHNCD) "); // 商品コード
        sbSQL.append(",MAKERKN = VALUES(MAKERKN) "); // メーカー名
        sbSQL.append(",SHNKN = VALUES(SHNKN) "); // 商品名称
        sbSQL.append(",KIKKN = VALUES(KIKKN) "); // 規格名称
        sbSQL.append(",IRISU = VALUES(IRISU) "); // 入数
        sbSQL.append(",BAIKAAM = VALUES(BAIKAAM) "); // 売価
        sbSQL.append(",GENKAAM = VALUES(GENKAAM) "); // 原価
        sbSQL.append(",UPDKBN = VALUES(UPDKBN) "); // 更新フラグ
        sbSQL.append(",SENDFLG = VALUES(SENDFLG) "); // 送信フラグ
        sbSQL.append(",OPERATOR = VALUES(OPERATOR) "); // オペレーターコード
        sbSQL.append(",UPDDT = VALUES(UPDDT) "); // 更新日

        int count = super.executeSQL(sbSQL.toString(), paramData);
        if (StringUtils.isEmpty(getMessage())) {
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("冷凍食品_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
          msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
        } else {
          msgObj.put(MsgKey.E.getKey(), getMessage());
          return msgObj;
        }
        paramData = new ArrayList<String>();
        sbSQL = new StringBuffer();
        if (New) {
          sbSQL.append("REPLACE INTO INATK.SYSRS ( ");
          sbSQL.append(" HBSTDT"); // 販売開始日 F1
          sbSQL.append(",BMNCD"); // 部門 F2
          sbSQL.append(",WRITUKBN"); // 割引率区分 F3
          sbSQL.append(",SEICUTKBN"); // 正規・カット F4
          sbSQL.append(",DUMMYCD"); // ダミーコード F5
          sbSQL.append(",SUMI_KANRINO"); // 管理番号 F6
          sbSQL.append(",OPERATOR "); // オペレーター：
          sbSQL.append(",ADDDT "); // 登録日：
          sbSQL.append(",UPDDT "); // 更新日：
          sbSQL.append(" ) VALUES ( ");
          sbSQL.append(" '" + szHbstdt + "'");// 販売開始日 F1
          sbSQL.append(", '" + szBmncd + "'");// 部門 F2
          sbSQL.append(", '" + szWaribiki + "'");// 割引率区分 F3
          sbSQL.append(", '" + szSeisi + "'");// 正規・カット F4
          sbSQL.append(", '" + szDummycd + "'");// ダミーコード F5
          sbSQL.append(", '" + kanriNo + "'");// 管理番号 F6
          sbSQL.append(", '" + userId + "' "); // オペレーター
          sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日
          sbSQL.append(", CURRENT_TIMESTAMP ");// 更新日
          sbSQL.append(") ");

        } else {
          sbSQL.append("REPLACE INTO INATK.SYSRS (");
          sbSQL.append(" HBSTDT"); // 販売開始日 F1
          sbSQL.append(",BMNCD"); // 部門 F2
          sbSQL.append(",WRITUKBN"); // 割引率区分 F3
          sbSQL.append(",SEICUTKBN"); // 正規・カット F4
          sbSQL.append(",DUMMYCD"); // ダミーコード F5
          sbSQL.append(",SUMI_KANRINO"); // 管理番号 F6
          sbSQL.append(",OPERATOR "); // オペレーター：
          sbSQL.append(",ADDDT "); // 登録日：
          sbSQL.append(",UPDDT "); // 更新日：
          sbSQL.append(" ) VALUES ( ");
          sbSQL.append(" '" + szHbstdt + "'");
          sbSQL.append(", '" + szBmncd + "'");
          sbSQL.append(", '" + szWaribiki + "'");
          sbSQL.append(", '" + szSeisi + "'");
          sbSQL.append(", '" + szDummycd + "'");
          sbSQL.append(", '" + kanriNo + "'");
          sbSQL.append(", '" + userId + "' ");
          sbSQL.append(", CURRENT_TIMESTAMP ");
          sbSQL.append(", CURRENT_TIMESTAMP ");
          sbSQL.append(") ");

        }
        count = super.executeSQL(sbSQL.toString(), paramData);
        if (StringUtils.isEmpty(getMessage())) {
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("冷凍食品_内部管理を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
          msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
        } else {
          msgObj.put(MsgKey.E.getKey(), getMessage());
          return msgObj;
        }
      }

      // 企画に既存の冷凍食品レコードの場合の処理
      updateRows = "";
      paramData = new ArrayList<String>();
      for (int j = 0; j < dataArray.size(); j++) {
        shnValues = "";
        if (isTest) {
          // 商品コード
          if (!dataArray.optJSONObject(j).optString("F8").equals("1")) {
            shnValues = " '" + dataArray.optJSONObject(j).optString("F1") + "'";
            updateRows += "," + shnValues;
          }
        } else {
          // 商品コード
          if (!dataArray.optJSONObject(j).optString("F8").equals("1")) {
            paramData.add(dataArray.optJSONObject(j).optString("F1"));
            updateRows += ", ?";
          }
        }
      }
      if (!updateRows.equals("")) {
        updateRows = StringUtils.removeStart(updateRows, ",");
        sbSQL = new StringBuffer();
        sbSQL.append("  select");
        sbSQL.append("  KANRINO as F1");
        sbSQL.append(" ,SHNCD as F2");
        sbSQL.append(" from");
        sbSQL.append(" INATK.TOKRS_SHN");
        sbSQL.append(" where");
        sbSQL.append(" COALESCE(UPDKBN, 0) <> 1");
        sbSQL.append(" and HBSTDT = ");
        sbSQL.append(szHbstdt);
        sbSQL.append(" and BMNCD = ");
        sbSQL.append(szBmncd);
        sbSQL.append(" and WRITUKBN = ");
        sbSQL.append(szWaribiki);
        sbSQL.append(" and SEICUTKBN = ");
        sbSQL.append(szSeisi);
        sbSQL.append(" and DUMMYCD = ");
        sbSQL.append(szDummycd);
        sbSQL.append(" and SHNCD in ( ");
        sbSQL.append(updateRows);
        sbSQL.append(" ) ");

        iL = new ItemList();
        @SuppressWarnings("static-access")
        JSONArray array2 = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
        paramData = new ArrayList<String>();
        if (array2.size() > 0) {
          // 更新情報
          values = "";
          int kryoColNum = 12; // テーブル列数
          updateRows = "";
          String shncd = "";
          for (int i = 0; i < dataArray.size(); i++) {
            shncd = dataArray.optJSONObject(i).optString("F1");
            for (int j = 0; j < array2.size(); j++) {
              String oldKanrino = array2.getJSONObject(j).optString("F1");
              String oldShncd = array2.getJSONObject(j).optString("F2");
              values = "";
              if (shncd.trim().equals(oldShncd.trim())) {
                for (int k = 1; k <= kryoColNum; k++) {
                  // String col = "F" + k;
                  // String val = dataArray.optJSONObject(i).optString(col);
                  if (isTest) {
                    if (k == 1) {
                      values += " '" + szHbstdt + "'";// F1
                    } else if (k == 2) {
                      values += ", '" + szBmncd + "'";// F2
                    } else if (k == 3) {
                      values += ", '" + szWaribiki + "'";// F3
                    } else if (k == 4) {
                      values += ", '" + szSeisi + "'";// F4
                    } else if (k == 5) {
                      values += ", '" + szDummycd + "'";// F5
                    } else if (k == 6) {
                      values += ", '" + oldKanrino + "'";// F6
                    } else if (k == 7) {
                      values += ", '" + oldShncd + "'";// F7
                    } else if (k == 8) {
                      values += ", '" + dataArray.optJSONObject(i).optString("F2") + "'";
                    } else if (k == 9) {
                      values += ", '" + dataArray.optJSONObject(i).optString("F3") + "'";
                    } else if (k == 10) {
                      values += ", '" + dataArray.optJSONObject(i).optString("F4") + "'";
                    } else if (k == 11) {
                      values += ", '" + dataArray.optJSONObject(i).optString("F5") + "'";
                    } else if (k == 12) {
                      values += ", '" + dataArray.optJSONObject(i).optString("F6") + "'";
                    }
                  } else {
                    if (k == 1) {
                      paramData.add(szHbstdt);
                    } else if (k == 2) {
                      paramData.add(szBmncd);
                    } else if (k == 3) {
                      paramData.add(szWaribiki);
                    } else if (k == 4) {
                      paramData.add(szSeisi);
                    } else if (k == 5) {
                      paramData.add(szDummycd);
                    } else if (k == 6) {
                      paramData.add(oldKanrino);
                    } else if (k == 7) {
                      paramData.add(oldShncd);
                    } else if (k == 8) {
                      paramData.add(dataArray.optJSONObject(i).optString("F2"));
                    } else if (k == 9) {
                      paramData.add(dataArray.optJSONObject(i).optString("F3"));
                    } else if (k == 10) {
                      paramData.add(dataArray.optJSONObject(i).optString("F4"));
                    } else if (k == 11) {
                      paramData.add(dataArray.optJSONObject(i).optString("F5"));
                    } else if (k == 12) {
                      paramData.add(dataArray.optJSONObject(i).optString("F6"));
                    }
                    // prmData.add(values);
                    values += ", ?";
                  }
                }

                updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";
              }
            }
          }
          updateRows = StringUtils.removeStart(updateRows, ",");

          if (updateRows.length() == 0) {
            msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
            return msgObj;
          }
          sbSQL = new StringBuffer();
          new ArrayList<String>();
          sbSQL.append("INSERT INTO INATK.TOKRS_SHN ( ");
          sbSQL.append(" HBSTDT "); // 販売開始日
          sbSQL.append(",BMNCD "); // 部門
          sbSQL.append(",WRITUKBN "); // 割引率区分
          sbSQL.append(",SEICUTKBN "); // 正規・カット
          sbSQL.append(",DUMMYCD "); // ダミーコード
          sbSQL.append(",KANRINO "); // 管理番号
          sbSQL.append(",SHNCD "); // 商品コード
          sbSQL.append(",SHNKN "); // 商品名称
          sbSQL.append(",KIKKN "); // 規格名称
          sbSQL.append(",IRISU "); // 入数
          sbSQL.append(",BAIKAAM "); // 売価
          sbSQL.append(",GENKAAM "); // 原価
          sbSQL.append(",UPDKBN "); // 更新フラグ
          sbSQL.append(",SENDFLG "); // 送信フラグ
          sbSQL.append(",OPERATOR "); // オペレーターコード
          sbSQL.append(",ADDDT "); // 登録日
          sbSQL.append(",UPDDT "); // 更新日
          sbSQL.append(") ");
          sbSQL.append("SELECT ");
          sbSQL.append(" HBSTDT "); // 販売開始日
          sbSQL.append(",BMNCD "); // 部門
          sbSQL.append(",WRITUKBN "); // 割引率区分
          sbSQL.append(",SEICUTKBN "); // 正規・カット
          sbSQL.append(",DUMMYCD "); // ダミーコード
          sbSQL.append(",KANRINO "); // 管理番号
          sbSQL.append(",SHNCD "); // 商品コード
          sbSQL.append(",SHNKN "); // 商品名称
          sbSQL.append(",KIKKN "); // 規格名称
          sbSQL.append(",IRISU "); // 入数
          sbSQL.append(",BAIKAAM "); // 売価
          sbSQL.append(",GENKAAM "); // 原価
          sbSQL.append(",UPDKBN "); // 更新フラグ
          sbSQL.append(",SENDFLG "); // 送信フラグ
          sbSQL.append(",OPERATOR "); // オペレーターコード
          sbSQL.append(",ADDDT "); // 登録日
          sbSQL.append(",UPDDT "); // 更新日
          sbSQL.append("FROM ( ");
          sbSQL.append("SELECT ");
          sbSQL.append(" HBSTDT"); // 販売開始日 F1
          sbSQL.append(",BMNCD"); // 部門 F2
          sbSQL.append(",WRITUKBN"); // 割引率区分 F3
          sbSQL.append(",SEICUTKBN"); // 正規・カット F4
          sbSQL.append(",DUMMYCD"); // ダミーコード F5
          sbSQL.append(",KANRINO"); // 管理番号 F6
          sbSQL.append(",SHNCD"); // 商品コード F7
          sbSQL.append(",SHNKN"); // 商品名称
          sbSQL.append(",KIKKN"); // 規格名称
          sbSQL.append(",IRISU"); // 入数
          sbSQL.append(",BAIKAAM"); // 売価
          sbSQL.append(",GENKAAM"); // 原価
          sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
          sbSQL.append(",0 as SENDFLG"); // 送信フラグ
          sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
          sbSQL.append(", CURRENT_TIMESTAMP AS ADDDT "); // 登録日：
          sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT "); // 更新日：
          sbSQL.append(" FROM (values ROW" + updateRows + ") as T1(");
          sbSQL.append(" HBSTDT"); // 販売開始日
          sbSQL.append(",BMNCD"); // 部門
          sbSQL.append(",WRITUKBN"); // 割引率区分
          sbSQL.append(",SEICUTKBN"); // 正規・カット
          sbSQL.append(",DUMMYCD"); // ダミーコード
          sbSQL.append(",KANRINO"); // 管理番号
          sbSQL.append(",SHNCD"); // 商品コード
          sbSQL.append(",SHNKN"); // 商品名称
          sbSQL.append(",KIKKN"); // 規格名称
          sbSQL.append(",IRISU"); // 入数
          sbSQL.append(",BAIKAAM"); // 売価
          sbSQL.append(",GENKAAM"); // 原価
          sbSQL.append(")) as T1 ");
          sbSQL.append("ON DUPLICATE KEY UPDATE ");
          sbSQL.append("SHNCD = VALUES(SHNCD) "); // 商品コード
          sbSQL.append(",SHNKN = VALUES(SHNKN) "); // 商品名称
          sbSQL.append(",KIKKN = VALUES(KIKKN) "); // 規格名称
          sbSQL.append(",IRISU = VALUES(IRISU) "); // 入数
          sbSQL.append(",BAIKAAM = VALUES(BAIKAAM) "); // 売価
          sbSQL.append(",GENKAAM = VALUES(GENKAAM) "); // 原価
          sbSQL.append(",UPDKBN = VALUES(UPDKBN) "); // 更新フラグ
          sbSQL.append(",SENDFLG = VALUES(SENDFLG) "); // 送信フラグ
          sbSQL.append(",OPERATOR = VALUES(OPERATOR) "); // オペレーターコード
          sbSQL.append(",UPDDT = VALUES(UPDDT) "); // 更新日


          int count = super.executeSQL(sbSQL.toString(), paramData);
          if (StringUtils.isEmpty(getMessage())) {
            if (DefineReport.ID_DEBUG_MODE)
              System.out.println("冷凍食品_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
            msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
          } else {
            msgObj.put(MsgKey.E.getKey(), getMessage());
            return msgObj;
          }
        }
      }
    }


    // 削除処理
    ArrayList<String> prmData = new ArrayList<String>();
    updateRows = "";
    int delColNum = 6;
    for (int i = 0; i < dataArray.size(); i++) {
      String del = dataArray.optJSONObject(i).optString("F8");
      values = "";
      if (del.equals("1")) {
        for (int k = 1; k <= delColNum; k++) {
          // String col = "F" + k;
          // String val = dataArray.optJSONObject(i).optString(col);
          if (isTest) {
            if (k == 1) {
              values += " '" + szHbstdt + "'";// F1
            } else if (k == 2) {
              values += ", '" + szBmncd + "'";// F2
            } else if (k == 3) {
              values += ", '" + szWaribiki + "'";// F3
            } else if (k == 4) {
              values += ", '" + szSeisi + "'";// F4
            } else if (k == 5) {
              values += ", '" + szDummycd + "'";// F5
            } else if (k == 6) {
              values += ", '" + dataArray.optJSONObject(i).optString("F9") + "'";// 管理番号
            }
          } else {
            if (k == 1) {
              prmData.add(szHbstdt);
            } else if (k == 2) {
              prmData.add(szBmncd);
            } else if (k == 3) {
              prmData.add(szWaribiki);
            } else if (k == 4) {
              prmData.add(szSeisi);
            } else if (k == 5) {
              prmData.add(szDummycd);
            } else if (k == 6) {
              prmData.add(dataArray.optJSONObject(i).optString("F9"));
            }
            values += ", ?";
          }
        }
        values += ", " + DefineReport.ValUpdkbn.NML.getVal();
        values += ", 0";
        values += ", '" + userId + "' ";
        values += ", CURRENT_TIMESTAMP ";
        values += ", CURRENT_TIMESTAMP ";
        updateRows += ",(" + StringUtils.removeStart(values, ",") + ")";

      }
    }
    if (updateRows.equals("")) {
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      return msgObj;
    }
    updateRows = StringUtils.removeStart(updateRows, ",");
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKRS_SHN ( ");
    sbSQL.append(" HBSTDT"); // 販売開始日 F1
    sbSQL.append(",BMNCD"); // 部門 F2
    sbSQL.append(",WRITUKBN"); // 割引率区分 F3
    sbSQL.append(",SEICUTKBN"); // 正規・カット F4
    sbSQL.append(",DUMMYCD"); // ダミーコード F5
    sbSQL.append(",KANRINO"); // 管理番号 F6
    sbSQL.append(",UPDKBN"); // 更新区分：
    sbSQL.append(",SENDFLG"); // 送信フラグ
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES ");
    sbSQL.append(updateRows);


    int count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("冷凍食品_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
      return msgObj;
    }

    // 全部削除された場合
    prmData = new ArrayList<String>();
    sbSQL = new StringBuffer();
    sbSQL.append("  UPDATE INATK.TOKRS_KKK as TRKK ");
    sbSQL.append("  SET UPDKBN = '1' "); // 販売開始日 F1
    sbSQL.append("  FROM ( select "); // 部門 F2
    sbSQL.append("  count(1) as CNT"); // 割引率区分 F3
    sbSQL.append("  from INATK.TOKRS_SHN "); // 登録日：
    sbSQL.append("  where "); // 更新日：
    sbSQL.append(" HBSTDT = ");
    sbSQL.append(szHbstdt);
    sbSQL.append(" and BMNCD = ");
    sbSQL.append(szBmncd);
    sbSQL.append(" and WRITUKBN = ");
    sbSQL.append(szWaribiki);
    sbSQL.append(" and SEICUTKBN = ");
    sbSQL.append(szSeisi);
    sbSQL.append(" and DUMMYCD = ");
    sbSQL.append(szDummycd);
    sbSQL.append(" and UPDKBN = 0");
    sbSQL.append(" ) as TRSH where");
    sbSQL.append(" TRKK.HBSTDT = ");
    sbSQL.append(szHbstdt);
    sbSQL.append(" and TRKK.BMNCD = ");
    sbSQL.append(szBmncd);
    sbSQL.append(" and TRKK.WRITUKBN = ");
    sbSQL.append(szWaribiki);
    sbSQL.append(" and TRKK.SEICUTKBN = ");
    sbSQL.append(szSeisi);
    sbSQL.append(" and TRKK.DUMMYCD = ");
    sbSQL.append(szDummycd);
    sbSQL.append(" and TRSH.CNT = 0 ");

    count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("冷凍食品_企画を " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
      return msgObj;
    }

    msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));

    return msgObj;
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
    // if(userInfo==null){
    // return "";
    // }
    int userId = userInfo.getCD_user(); // ログインユーザー
    String szBmncd = map.get("BMNCD"); // 部門コード
    String szHbstdt = map.get("HBSTDT"); // 販売開始日
    String szDummycd = map.get("DUMMYCD"); // ダミーコード
    String szWaribiki = map.get("WARIBIKI"); // 割引率区分
    String szSeisi = map.get("SEISI"); // 正視・カット
    JSONObject msgObj = new JSONObject();

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    ArrayList<String> prmData = new ArrayList<String>();

    // 削除処理：更新区分に"1"（削除）を登録
    sbSQL = new StringBuffer();
    sbSQL.append("  UPDATE INATK.TOKRS_KKK SET ");
    sbSQL.append("  UPDKBN = '1'"); // 販売開始日 F1
    sbSQL.append(", SENDFLG = '0'"); // 送信フラグ
    sbSQL.append(", OPERATOR = '" + userId + "'"); // オペレーター：
    sbSQL.append(", ADDDT = CURRENT_TIMESTAMP  "); // 登録日：
    sbSQL.append(", UPDDT = CURRENT_TIMESTAMP   "); // 更新日：
    sbSQL.append("  where");
    sbSQL.append(" HBSTDT = ");
    sbSQL.append(szHbstdt);
    sbSQL.append(" and BMNCD = ");
    sbSQL.append(szBmncd);
    sbSQL.append(" and WRITUKBN = ");
    sbSQL.append(szWaribiki);
    sbSQL.append(" and SEICUTKBN = ");
    sbSQL.append(szSeisi);
    sbSQL.append(" and DUMMYCD = ");
    sbSQL.append(szDummycd);

    int count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("冷凍食品企画マスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
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
    request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    // JSONArray msg = this.check(map, userInfo, sysdate);
    //
    // if(msg.size() > 0){
    // msgObj.put(MsgKey.E.getKey(), msg);
    // return msgObj;
    // }

    // 削除処理
    try {
      msgObj = this.deleteData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
    }
    return msgObj;
  }

}
