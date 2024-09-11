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
public class ReportBW004Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBW004Dao(String JNDIname) {
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

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

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
    sbSQL.append(" from INATK.TOKRS_SHN TRSH");
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
    sbSQL.append(" left join INAMS.MSTSHN MTSH");
    sbSQL.append(" on TRSH.SHNCD = MTSH.SHNCD");
    sbSQL.append(" where ");
    sbSQL.append(" TRSH.HBSTDT = ");
    sbSQL.append(szHbstdt);
    sbSQL.append(" and TRSH.BMNCD = ");
    sbSQL.append(szBmncd);
    sbSQL.append(" and TRSH.WRITUKBN = ");
    sbSQL.append(szWaribiki);
    sbSQL.append(" and TRSH.SEICUTKBN = ");
    sbSQL.append(szSeisi);
    sbSQL.append(" and TRSH.DUMMYCD = ");
    sbSQL.append(szDummycd);
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

    String szSEQ = getMap().get("SEQ"); // 催し区分

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());
    sbSQL.append(" WITH RECURSIVE T1(IDX) as ( select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < 500) ");
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
    sbSQL.append(", T2.F13 as INPUTNO");
    sbSQL.append(" from T1");
    sbSQL.append(" left join (");
    sbSQL.append(" select");
    sbSQL.append("  ROW_NUMBER() over (order by CTRH.INPUTNO) as IDX");
    sbSQL.append(", CTRH.SHNCD as F1"); // F1 ：販売開始日
    sbSQL.append(", CTRH.MAKERKN as F2"); // F2 ：名称（漢字）
    sbSQL.append(", CTRH.SHNKN as F3"); // F3 ：割引率区分
    sbSQL.append(", CTRH.KIKKN as F4"); // F4 ：正規・カット
    sbSQL.append(", CTRH.IRISU as F5"); // F5 ：ダミーコード（商品コード）
    sbSQL.append("," + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@BAIKA", "MTSH.RG_BAIKAAM").replaceAll("@DT", "CTRH.HBSTDT") + " as F6");
    sbSQL.append(", MTSH.RG_BAIKAAM as F7"); // F7 ：販売開始日(パラメータ)
    sbSQL.append(", MTSH.RG_GENKAAM as F8"); // F8 ：割引率区分(パラメータ)
    sbSQL.append(", CTRH.BAIKAAM as F9"); // F9 ：正規・カット(パラメータ)
    sbSQL.append("," + DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "CTRH.BAIKAAM").replaceAll("@DT", "CTRH.HBSTDT") + " as F10"); // F10 ：デフォルト_数展開
    sbSQL.append(", CTRH.GENKAAM as F11"); // F11 ：ダミーコード(パラメータ)
    sbSQL.append(", MSBR.SHOBRUIKN  as F12"); // F12 ：ダミーコード(パラメータ)
    sbSQL.append(", CTRH.INPUTNO as F13"); // F12 ：ダミーコード(パラメータ)
    sbSQL.append(" from INATK.CSVTOK_RSKKK CTRK");
    sbSQL.append(" left join INATK.CSVTOK_RSSHN CTRH");
    sbSQL.append(" on CTRK.SEQ = CTRH.SEQ");
    sbSQL.append(" and CTRK.INPUTNO = CTRH.INPUTNO");
    sbSQL.append(" and CTRH.UPDKBN = ");
    sbSQL.append(DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" left join INAMS.MSTSHN MTSH");
    sbSQL.append(" on CTRH.SHNCD = MTSH.SHNCD");
    sbSQL.append(" left join INAMS.MSTSHOBRUI MSBR");
    sbSQL.append(" on MTSH.DAICD = MSBR.DAICD");
    sbSQL.append(" and MTSH.CHUCD = MSBR.CHUCD");
    sbSQL.append(" and MTSH.SHOCD = MSBR.SHOCD");
    sbSQL.append(" and MTSH.BMNCD = MSBR.BMNCD");
    sbSQL.append(" left outer join INAMS.MSTSHN M0 on M0.SHNCD = CTRH.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTBMN M1 on M1.BMNCD = CTRH.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1");
    sbSQL.append(" where");
    sbSQL.append(" CTRK.SEQ = ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRK.UPDKBN = 0 ");
    sbSQL.append(" order by CTRH.INPUTEDANO IS NULL ASC,CTRH.INPUTEDANO ) T2");
    sbSQL.append(" on T1.IDX = T2.IDX");
    sbSQL.append(" order by T1.IDX IS NULL ASC, T1.IDX");
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
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
    sbSQL.append(DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", obj.optString("value")).replaceAll("@DT", "TRSH.HBSTDT") + " as F11 ");
    sbSQL.append(" from INATK.TOKRS_SHN TRSH left join INAMS.MSTSHN MTSH on ");
    sbSQL.append(" TRSH.SHNCD = MTSH.SHNCD ");
    sbSQL.append(" left join INAMS.MSTSHOBRUI MSBR on ");
    sbSQL.append(" MTSH.DAICD = MSBR.DAICD and  ");
    sbSQL.append(" MTSH.CHUCD = MSBR.CHUCD and ");
    sbSQL.append(" MTSH.SHOCD = MSBR.SHOCD and ");
    sbSQL.append(" MSBR.BMNCD = MTSH.BMNCD ");
    sbSQL.append(" left outer join INAMS.MSTSHN M0 on  ");
    sbSQL.append(" M0.SHNCD = TRSH.SHNCD and  ");
    sbSQL.append(" COALESCE(M0.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTBMN M1 on  ");
    sbSQL.append(" M1.BMNCD = TRSH.BMNCD and ");
    sbSQL.append(" COALESCE(M1.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M2 on ");
    sbSQL.append(" M2.ZEIRTKBN = M0.ZEIRTKBN and ");
    sbSQL.append(" COALESCE(M2.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M3 on  ");
    sbSQL.append(" M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and  ");
    sbSQL.append(" COALESCE(M3.UPDKBN, 0) <> 1  ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M4 on  ");
    sbSQL.append(" M4.ZEIRTKBN = M1.ZEIRTKBN and ");
    sbSQL.append(" COALESCE(M4.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left outer join INAMS.MSTZEIRT M5 on  ");
    sbSQL.append(" M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and ");
    sbSQL.append(" COALESCE(M5.UPDKBN, 0) <> 1 ");
    sbSQL.append(" where TRSH.HBSTDT = ? and ");
    sbSQL.append(" TRSH.BMNCD = ? and  ");
    sbSQL.append(" TRSH.WRITUKBN = ? and  ");
    sbSQL.append(" TRSH.SEICUTKBN = ? and ");
    sbSQL.append(" TRSH.DUMMYCD = ? and ");
    sbSQL.append(" TRSH.UPDKBN <> 1 and ");
    sbSQL.append(" TRSH.SHNCD = ? ");

    return sbSQL.toString();
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szSEQ = map.get("SEQ"); // 催し区分

    String sqlcommand = "";
    String inputFlg = "0";

    new ItemList();
    StringBuffer sbSQL = new StringBuffer();

    ArrayList<String> paramData = new ArrayList<>();
    // 部門コードの存在チェック
    paramData = new ArrayList<>();
    paramData.add(szSEQ);
    sqlcommand = "select COUNT(KKK.HBSTDT) as VALUE" + " from INATK.TOKRS_KKK KKK" + " inner join (select distinct HBSTDT, BMNCD, WRITUKBN, SEICUTKBN, DUMMYCD, MEISHOKN"
        + " from INATK.CSVTOK_RSKKK where COALESCE(UPDKBN, 0) <> 1 and SEQ = ? ) CSV on CSV.HBSTDT = KKK.HBSTDT and CSV.BMNCD = KKK.BMNCD and CSV.WRITUKBN = KKK.WRITUKBN and CSV.SEICUTKBN = KKK.SEICUTKBN and CSV.DUMMYCD = KKK.DUMMYCD and CSV.MEISHOKN = KKK.MEISHOKN"
        + " where COALESCE(KKK.UPDKBN, 0) <> 1";

    @SuppressWarnings("static-access")
    JSONArray result = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (result.size() > 0) {
      int count = result.getJSONObject(0).getInt("VALUE");
      if (count == 0) {
        inputFlg = "1";
      }
    }

    sbSQL = new StringBuffer();
    paramData = new ArrayList<>();
    sbSQL.append("  select");
    sbSQL.append(" RIGHT(CAST(CTRK.HBSTDT AS CHAR), 6) as F1");
    sbSQL.append(" , right('0'|| CTRK.BMNCD,2) as F2");
    sbSQL.append(" , CTRK.MEISHOKN as F3");
    sbSQL.append(" , CTRK.WRITUKBN as F4");
    sbSQL.append(" , CTRK.SEICUTKBN as F5");
    sbSQL.append(" , CTRK.DUMMYCD as F6");
    sbSQL.append(" , MTSH.POPKN as F7");
    sbSQL.append(" , CTRK.WRITUKBN as F8");
    sbSQL.append(" , CTRK.SEICUTKBN as F9");
    sbSQL.append(" , " + inputFlg + " as F10");
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
    sbSQL.append(" where CTRK.SEQ = ");
    sbSQL.append(szSEQ);

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

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
    return msg;
  }

  /**
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public boolean checkMstExist(String outobj, String value) {
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";

    String tbl = "";
    String col = "";
    // 商品コード
    tbl = "INAMS.MSTSHN";
    col = "SHNCD";

    paramData.add(value);
    sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);

    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
      return false;
    }
    return true;
  }

  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    JSONObject msgObj = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報

    String userId = userInfo.getId(); // ログインユーザー
    String szBmncd = map.get("BMNCD"); // 部門コード
    String szHbstdt = map.get("HBSTDT"); // 販売開始日
    String szDummycd = map.get("DUMMYCD"); // ダミーコード
    String szWaribiki = map.get("WARIBIKI"); // 割引率区分
    String szSeisi = map.get("SEISI"); // 正視・カット
    String szMeishokn = map.get("MEISHOKN"); // 正視・カット
    ArrayList<String> paramData = new ArrayList<>();
    // タイトル情報(任意)設定

    StringBuffer sbSQL = new StringBuffer();
    String values = "";
    String shnValues = "";

    paramData.add(szHbstdt);
    paramData.add(szBmncd);
    paramData.add(szWaribiki);
    paramData.add(szSeisi);
    paramData.add(szDummycd);

    sbSQL.append(" select ");
    sbSQL.append(" (1) ");
    sbSQL.append(" from INATK.TOKRS_KKK ");
    sbSQL.append(" where ");
    sbSQL.append(" HBSTDT = ?");
    sbSQL.append(" and BMNCD = ?");
    sbSQL.append(" and WRITUKBN = ?");
    sbSQL.append(" and SEICUTKBN = ?");
    sbSQL.append(" and DUMMYCD = ?");
    sbSQL.append(" and COALESCE(UPDKBN, 0) <> 1");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // 企画に新しい冷凍食品レコードの場合
    paramData = new ArrayList<>();
    if (array.size() == 0) {
      // ① 冷凍食品_企画テーブルに更新登録する
      for (int j = 0; j < 6; j++) {
        shnValues = "";
        if (j == 0) {
          shnValues = szHbstdt;
        } else if (j == 1) {
          shnValues = szBmncd;
        } else if (j == 2) {
          shnValues = szWaribiki;
        } else if (j == 3) {
          shnValues = szSeisi;
        } else if (j == 4) {
          shnValues = szDummycd;
        } else if (j == 5) {
          shnValues = szMeishokn;
        }
        if (shnValues != null) {
          paramData.add(shnValues);
          values += ", ?";
        } else {
          values += ", null";
        }
      }
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKRS_KKK  ( ");
      sbSQL.append(" HBSTDT"); // 販売開始日
      sbSQL.append(",BMNCD"); // 部門
      sbSQL.append(",WRITUKBN"); // 割引率区分
      sbSQL.append(",SEICUTKBN"); // 正規・カット
      sbSQL.append(",DUMMYCD"); // ダミーコード
      sbSQL.append(",MEISHOKN"); // 管理番号
      sbSQL.append(",UPDKBN"); // 更新区分
      sbSQL.append(",SENDFLG"); // 送信フラグ
      sbSQL.append(",OPERATOR"); // オペレーターコード
      sbSQL.append(",ADDDT"); // 登録日
      sbSQL.append(",UPDDT"); // 更新日
      sbSQL.append(" )SELECT");
      sbSQL.append(" HBSTDT"); // 販売開始日 F1
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",WRITUKBN"); // 割引率区分 F3
      sbSQL.append(",SEICUTKBN"); // 正規・カット F4
      sbSQL.append(",DUMMYCD"); // ダミーコード F5
      sbSQL.append(",MEISHOKN"); // 管理番号 F6
      sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
      sbSQL.append(",0 as SENDFLG"); // 送信フラグ
      sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
      sbSQL.append(", current_timestamp AS ADDDT "); // 登録日：
      sbSQL.append(", current_timestamp AS UPDDT "); // 更新日：
      sbSQL.append(" FROM (values ROW( " + values + ") ) as RE(");
      sbSQL.append(" HBSTDT"); // 販売開始日
      sbSQL.append(",BMNCD"); // 部門
      sbSQL.append(",WRITUKBN"); // 割引率区分
      sbSQL.append(",SEICUTKBN"); // 正規・カット
      sbSQL.append(",DUMMYCD"); // ダミーコード
      sbSQL.append(",MEISHOKN"); // メーカー名
      sbSQL.append(" )");
      sbSQL.append(" ON DUPLICATE KEY UPDATE");
      sbSQL.append(" HBSTDT = RE.HBSTDT");
      sbSQL.append(", BMNCD = RE.BMNCD");
      sbSQL.append(", WRITUKBN = RE.WRITUKBN");
      sbSQL.append(", SEICUTKBN = RE.SEICUTKBN");
      sbSQL.append(", DUMMYCD = RE.DUMMYCD");
      sbSQL.append(", MEISHOKN = RE.MEISHOKN");
      sbSQL.append(", OPERATOR =  '" + userId + "'");
      sbSQL.append(", UPDDT = current_timestamp");
      sbSQL.append(", UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + "");
      sbSQL.append(", ADDDT = current_timestamp");


      int count = super.executeSQL(sbSQL.toString(), paramData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("冷凍食品_企画を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
        return msgObj;
      }

      paramData = new ArrayList<>();
      paramData.add(szHbstdt);
      paramData.add(szBmncd);
      paramData.add(szWaribiki);
      paramData.add(szSeisi);
      paramData.add(szDummycd);

      sbSQL = new StringBuffer();
      sbSQL.append("  select");
      sbSQL.append("   SYSR.SUMI_KANRINO+1 as F1 ");
      sbSQL.append(" from");
      sbSQL.append(" INATK.SYSRS SYSR");
      sbSQL.append(" where");
      sbSQL.append(" SYSR.HBSTDT = ? ");
      sbSQL.append(" and SYSR.BMNCD = ? ");
      sbSQL.append(" and SYSR.WRITUKBN = ? ");
      sbSQL.append(" and SYSR.SEICUTKBN = ? ");
      sbSQL.append(" and SYSR.DUMMYCD = ? ");

      new ItemList();
      @SuppressWarnings("static-access")
      JSONArray array2 = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
      int kanriNo;
      if (array2.size() == 0) {
        kanriNo = 1;
      } else {
        kanriNo = Integer.parseInt(array2.optJSONObject(0).optString("F1"));
      }

      int newColNum = 13;
      paramData = new ArrayList<>();
      values = "";
      for (int i = 0; i < dataArray.size(); i++) {
        shnValues = "";
        for (int k = 1; k <= newColNum; k++) {

          if (k == 1) {
            values += ",ROW(";
            shnValues = szHbstdt;// F1
          } else if (k == 2) {
            shnValues = szBmncd;// F2
          } else if (k == 3) {
            shnValues = szWaribiki;// F3
          } else if (k == 4) {
            shnValues = szSeisi;// F4
          } else if (k == 5) {
            shnValues = szDummycd;// F5
          } else if (k == 6) {
            shnValues = String.valueOf(kanriNo);;// F6
            kanriNo++;
          } else if (k == 7) {
            shnValues = dataArray.optJSONObject(i).optString("F1");
          } else if (k == 8) {
            shnValues = dataArray.optJSONObject(i).optString("F2");
          } else if (k == 9) {
            shnValues = dataArray.optJSONObject(i).optString("F3");
          } else if (k == 10) {
            shnValues = dataArray.optJSONObject(i).optString("F4");
          } else if (k == 11) {
            shnValues = dataArray.optJSONObject(i).optString("F5");
          } else if (k == 12) {
            shnValues = dataArray.optJSONObject(i).optString("F6");
          } else if (k == 13) {
            shnValues = dataArray.optJSONObject(i).optString("F7");
          }
          if (k == 13) {
            if (shnValues != null) {
              paramData.add(shnValues);
              values += "?)";
            } else {
              values += "null)";
            }
          } else {

            if (shnValues != null) {
              paramData.add(shnValues);
              values += "?,";
            } else {
              values += "null,";
            }
          }
        }
      }
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKRS_SHN  ( ");
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
      sbSQL.append(",UPDKBN"); // 更新区分
      sbSQL.append(",SENDFLG"); // 送信フラグ
      sbSQL.append(",OPERATOR"); // オペレーターコード
      sbSQL.append(",ADDDT"); // 登録日
      sbSQL.append(",UPDDT"); // 更新日
      sbSQL.append(" )SELECT * FROM (");
      sbSQL.append(" SELECT");
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
      sbSQL.append(",CAST(COALESCE(NULLIF(BAIKAAM, 0), 0) AS DECIMAL) AS BAIKAAM"); // 売価
      sbSQL.append(",CAST(COALESCE(NULLIF(GENKAAM, 0), 0) AS DECIMAL) AS GENKAAM"); // 原価
      sbSQL.append(",MAKERKN"); // メーカ名
      sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
      sbSQL.append(",0 as SENDFLG"); // 送信フラグ
      sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
      sbSQL.append(", current_timestamp AS ADDDT "); // 登録日：
      sbSQL.append(", current_timestamp AS UPDDT "); // 更新日：
      sbSQL.append(" FROM (values " + values + ") as T1(");
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
      sbSQL.append(" ))AS RE");
      sbSQL.append(" ON DUPLICATE KEY UPDATE");
      sbSQL.append(" HBSTDT = RE.HBSTDT");
      sbSQL.append(", BMNCD = RE.BMNCD");
      sbSQL.append(", WRITUKBN = RE.WRITUKBN");
      sbSQL.append(", SEICUTKBN = RE.SEICUTKBN");
      sbSQL.append(", DUMMYCD = RE.DUMMYCD");
      sbSQL.append(", KANRINO = RE.KANRINO");
      sbSQL.append(", OPERATOR = '" + userId + "'");
      sbSQL.append(", UPDDT = current_timestamp");
      sbSQL.append(", UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + "");

      count = super.executeSQL(sbSQL.toString(), paramData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("冷凍食品_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
        return msgObj;
      }

      paramData = new ArrayList<>();
      values = "";
      for (int j = 0; j < 6; j++) {
        shnValues = "";
        if (j == 0) {
          shnValues = szHbstdt;
        } else if (j == 1) {
          shnValues = szBmncd;
        } else if (j == 2) {
          shnValues = szWaribiki;
        } else if (j == 3) {
          shnValues = szSeisi;
        } else if (j == 4) {
          shnValues = szDummycd;
        } else if (j == 5) {
          shnValues = String.valueOf(kanriNo);// F6
        }
        values += ", ?";
        paramData.add(shnValues);
      }

      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.SYSRS ( ");
      sbSQL.append(" HBSTDT"); // 販売開始日
      sbSQL.append(",BMNCD"); // 部門
      sbSQL.append(",WRITUKBN"); // 割引率区分
      sbSQL.append(",SEICUTKBN"); // 正規・カット
      sbSQL.append(",DUMMYCD"); // ダミーコード
      sbSQL.append(",SUMI_KANRINO"); // 管理番号
      sbSQL.append(",OPERATOR"); // オペレーターコード
      sbSQL.append(",ADDDT"); // 登録日
      sbSQL.append(",UPDDT"); // 更新日

      sbSQL.append(" )SELECT");
      sbSQL.append(" HBSTDT"); // 販売開始日 F1
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",WRITUKBN"); // 割引率区分 F3
      sbSQL.append(",SEICUTKBN"); // 正規・カット F4
      sbSQL.append(",DUMMYCD"); // ダミーコード F5
      sbSQL.append(",SUMI_KANRINO"); // 管理番号 F6
      sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
      sbSQL.append(", current_timestamp AS ADDDT "); // 登録日：
      sbSQL.append(", current_timestamp AS UPDDT "); // 更新日：
      sbSQL.append(" FROM (values ");
      sbSQL.append(" ROW(" + values + "");
      sbSQL.append(") ) as RE(");
      sbSQL.append(" HBSTDT"); // 販売開始日
      sbSQL.append(",BMNCD"); // 部門
      sbSQL.append(",WRITUKBN"); // 割引率区分
      sbSQL.append(",SEICUTKBN"); // 正規・カット
      sbSQL.append(",DUMMYCD"); // ダミーコード
      sbSQL.append(",SUMI_KANRINO"); // 管理番号
      sbSQL.append(" ) ");
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" HBSTDT = RE.HBSTDT");
      sbSQL.append(", BMNCD = RE.BMNCD");
      sbSQL.append(", WRITUKBN = RE.WRITUKBN");
      sbSQL.append(", SEICUTKBN = RE.SEICUTKBN");
      sbSQL.append(", DUMMYCD = RE.DUMMYCD");
      sbSQL.append(", SUMI_KANRINO = RE.SUMI_KANRINO");
      sbSQL.append(", OPERATOR = '" + userId + "'");
      sbSQL.append(", UPDDT = current_timestamp");

      count = super.executeSQL(sbSQL.toString(), paramData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("冷凍食品内部管理テーブルを " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
        return msgObj;
      }

      paramData = new ArrayList<>();
      paramData.add(map.get("SEQ"));

      sbSQL = new StringBuffer();
      sbSQL.append(" UPDATE ");
      sbSQL.append(" INATK.CSVTOK_RSKKK ");
      sbSQL.append(" SET ");
      sbSQL.append(" UPDKBN = ");
      sbSQL.append(DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(" where ");
      sbSQL.append(" SEQ = ? ");
      sbSQL.append(" and INPUTNO = 1 ");

      count = super.executeSQL(sbSQL.toString(), paramData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("CSV取込トラン_冷凍食品_企画を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
        return msgObj;
      }
    } else {
      // 既に企画が存在している場合
      paramData = new ArrayList<>();

      paramData.add(szHbstdt);
      paramData.add(szBmncd);
      paramData.add(szWaribiki);
      paramData.add(szSeisi);
      paramData.add(szDummycd);

      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append(" UPDATE ");
      sbSQL.append(" INATK.TOKRS_KKK ");
      sbSQL.append(" SET ");
      sbSQL.append(" UPDKBN = ");
      sbSQL.append(DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(", UPDDT = current_timestamp ");
      sbSQL.append(" where ");
      sbSQL.append(" HBSTDT = ? ");
      sbSQL.append(" and BMNCD = ? ");
      sbSQL.append(" and WRITUKBN = ? ");
      sbSQL.append(" and SEICUTKBN = ? ");
      sbSQL.append(" and DUMMYCD = ? ");
      sbSQL.append(" and COALESCE(UPDKBN, 0) <> 1 ");

      int count = super.executeSQL(sbSQL.toString(), paramData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("冷凍食品_企画を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
        return msgObj;
      }

      for (int i = 0; i < dataArray.size(); i++) {
        paramData = new ArrayList<>();
        for (int j = 0; j < 6; j++) {
          shnValues = "";
          if (j == 0) {
            shnValues = szHbstdt;
          } else if (j == 1) {
            shnValues = szBmncd;
          } else if (j == 2) {
            shnValues = szWaribiki;
          } else if (j == 3) {
            shnValues = szSeisi;
          } else if (j == 4) {
            shnValues = szDummycd;
          } else if (j == 5) {
            shnValues = dataArray.optJSONObject(i).optString("F1");
          }
          paramData.add(shnValues);
        }

        sbSQL = new StringBuffer();
        sbSQL.append("  select");
        sbSQL.append(" KANRINO ");
        sbSQL.append(" from");
        sbSQL.append(" INATK.TOKRS_SHN");
        sbSQL.append(" where");
        sbSQL.append(" HBSTDT = ? ");
        sbSQL.append(" and BMNCD = ? ");
        sbSQL.append(" and WRITUKBN = ? ");
        sbSQL.append(" and SEICUTKBN = ? ");
        sbSQL.append(" and DUMMYCD = ? ");
        sbSQL.append(" and SHNCD = ? ");
        sbSQL.append(" and UPDKBN = ");
        sbSQL.append(DefineReport.ValUpdkbn.NML.getVal());

        new ItemList();
        @SuppressWarnings("static-access")
        JSONArray array2 = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        // 同一商品データが存在する場合
        if (array2.size() > 0) {
          paramData = new ArrayList<>();
          for (int k = 1; k <= 13; k++) {

            if (k == 1) {
              values = ",(";
              shnValues = szHbstdt;// F1
            } else if (k == 2) {
              shnValues = szBmncd;// F2
            } else if (k == 3) {
              shnValues = szWaribiki;// F3
            } else if (k == 4) {
              shnValues = szSeisi;// F4
            } else if (k == 5) {
              shnValues = szDummycd;// F5
            } else if (k == 6) {
              shnValues = array2.optJSONObject(0).optString("KANRINO");
            } else if (k == 7) {
              shnValues = dataArray.optJSONObject(i).optString("F1");
            } else if (k == 8) {
              shnValues = dataArray.optJSONObject(i).optString("F2");
            } else if (k == 9) {
              shnValues = dataArray.optJSONObject(i).optString("F3");
            } else if (k == 10) {
              shnValues = dataArray.optJSONObject(i).optString("F4");
            } else if (k == 11) {
              shnValues = dataArray.optJSONObject(i).optString("F5");
            } else if (k == 12) {
              shnValues = dataArray.optJSONObject(i).optString("F6");
            } else if (k == 13) {
              shnValues = dataArray.optJSONObject(i).optString("F7");
            }
            if (k == 13) {
              if (shnValues != null) {
                paramData.add(shnValues);
                values += "?)";
              } else {
                values += "null)";
              }
            } else {

              if (shnValues != null) {
                paramData.add(shnValues);
                values += "?,";
              } else {
                values += "null,";
              }
            }
          }
          values = StringUtils.removeStart(values, ",");

          sbSQL = new StringBuffer();
          sbSQL.append("UPDATE SET INATK.TOKRS_SHN  , ");
          sbSQL.append("( SELECT");
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
          sbSQL.append(",CAST(COALESCE(NULLIF(BAIKAAM, 0), 0) AS DECIMAL) AS BAIKAAM"); // 売価
          sbSQL.append(",CAST(COALESCE(NULLIF(GENKAAM, 0), 0) AS DECIMAL) AS GENKAAM"); // 原価
          sbSQL.append(",MAKERKN"); // メーカ名
          sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
          sbSQL.append(",0 as SENDFLG"); // 送信フラグ
          sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
          sbSQL.append(", current_timestamp AS ADDDT "); // 登録日：
          sbSQL.append(", current_timestamp AS UPDDT "); // 更新日：
          sbSQL.append(" FROM (values ROW" + values + ") as T1(");
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
          sbSQL.append(" )) AS RE ");

          sbSQL.append("  SET");
          sbSQL.append(" SHNCD = RE.SHNCD");
          sbSQL.append(", SHNKN = RE.SHNKN");
          sbSQL.append(", KIKKN = RE.KIKKN");
          sbSQL.append(", IRISU = RE.IRISU");
          sbSQL.append(", BAIKAAM = RE.BAIKAAM");
          sbSQL.append(", GENKAAM = RE.GENKAAM");
          sbSQL.append(", MAKERKN = RE.MAKERKN");
          sbSQL.append(", OPERATOR = '" + userId + "'");
          sbSQL.append(", UPDDT = current_timestamp");
          sbSQL.append(", UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " ");

          count = super.executeSQL(sbSQL.toString(), paramData);
          if (StringUtils.isEmpty(getMessage())) {
            if (DefineReport.ID_DEBUG_MODE)
              System.out.println("冷凍食品_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
            msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
          } else {
            msgObj.put(MsgKey.E.getKey(), getMessage());
            return msgObj;
          }
          // 同一商品データが存在しない場合
        } else {
          paramData = new ArrayList<>();
          paramData.add(szHbstdt);
          paramData.add(szBmncd);
          paramData.add(szWaribiki);
          paramData.add(szSeisi);
          paramData.add(szDummycd);

          sbSQL = new StringBuffer();
          sbSQL.append("  select");
          sbSQL.append("   SYSR.SUMI_KANRINO+1 as F1 ");
          sbSQL.append(" from");
          sbSQL.append(" INATK.SYSRS SYSR");
          sbSQL.append(" where");
          sbSQL.append(" SYSR.HBSTDT = ?");
          sbSQL.append(" and SYSR.BMNCD = ?");
          sbSQL.append(" and SYSR.WRITUKBN = ?");
          sbSQL.append(" and SYSR.SEICUTKBN = ?");
          sbSQL.append(" and SYSR.DUMMYCD = ?");

          new ItemList();
          @SuppressWarnings("static-access")
          JSONArray array3 = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
          int kanriNo;
          if (array3.size() == 0) {
            kanriNo = 1;
          } else {
            kanriNo = Integer.parseInt(array3.optJSONObject(0).optString("F1"));
          }
          paramData = new ArrayList<>();
          for (int k = 1; k <= 13; k++) {

            if (k == 1) {
              values = ",(";
              shnValues = szHbstdt;// F1
            } else if (k == 2) {
              shnValues = szBmncd;// F2
            } else if (k == 3) {
              shnValues = szWaribiki;// F3
            } else if (k == 4) {
              shnValues = szSeisi;// F4
            } else if (k == 5) {
              shnValues = szDummycd;// F5
            } else if (k == 6) {
              shnValues = String.valueOf(kanriNo);;
            } else if (k == 7) {
              shnValues = dataArray.optJSONObject(i).optString("F1");
            } else if (k == 8) {
              shnValues = dataArray.optJSONObject(i).optString("F2");
            } else if (k == 9) {
              shnValues = dataArray.optJSONObject(i).optString("F3");
            } else if (k == 10) {
              shnValues = dataArray.optJSONObject(i).optString("F4");
            } else if (k == 11) {
              shnValues = dataArray.optJSONObject(i).optString("F5");
            } else if (k == 12) {
              shnValues = dataArray.optJSONObject(i).optString("F6");
            } else if (k == 13) {
              shnValues = dataArray.optJSONObject(i).optString("F7");
            }
            if (k == 13) {
              if (shnValues != null) {
                paramData.add(shnValues);
                values += "?)";
              } else {
                values += "null)";
              }
            } else {

              if (shnValues != null) {
                System.out.print("shnValues:" + shnValues + k + "\n");
                paramData.add(shnValues);
                values += "?,";
              } else {
                values += "null,";
              }
            }
          }
          values = StringUtils.removeStart(values, ",");
          sbSQL = new StringBuffer();
          sbSQL.append("INSERT INTO INATK.TOKRS_SHN ( ");
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
          sbSQL.append(",UPDKBN"); // 更新区分
          sbSQL.append(",SENDFLG"); // 送信フラグ
          sbSQL.append(",OPERATOR"); // オペレーターコード
          sbSQL.append(",ADDDT"); // 登録日
          sbSQL.append(",UPDDT"); // 更新日
          sbSQL.append(" )SELECT * FROM (");
          sbSQL.append(" SELECT");
          sbSQL.append(" HBSTDT"); // 販売開始日 F1
          sbSQL.append(",BMNCD"); // 部門 F2
          sbSQL.append(",WRITUKBN"); // 割引率区分 F3
          sbSQL.append(",SEICUTKBN"); // 正規・カット F4
          sbSQL.append(",DUMMYCD"); // ダミーコード F5
          sbSQL.append(",KANRINO"); // 管理番号 F6
          sbSQL.append(",SHNCD"); // 商品コード F7
          sbSQL.append(",SHNKN"); // 商品名称
          sbSQL.append(",KIKKN"); // 規格名称
          sbSQL.append(",CAST(COALESCE(NULLIF(IRISU, 0), NULL) AS CHAR) AS IRISU"); // 入数
          sbSQL.append(",CAST(COALESCE(NULLIF(BAIKAAM, 0), 0) AS DECIMAL) AS BAIKAAM"); // 売価
          sbSQL.append(",CAST(COALESCE(NULLIF(GENKAAM, 0), 0) AS DECIMAL) AS GENKAAM"); // 原価
          sbSQL.append(",MAKERKN"); // メーカ名
          sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN"); // 更新区分：
          sbSQL.append(",0 as SENDFLG"); // 送信フラグ
          sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
          sbSQL.append(", current_timestamp AS ADDDT "); // 登録日：
          sbSQL.append(", current_timestamp AS UPDDT "); // 更新日：
          sbSQL.append(" FROM (values ROW" + values + ") as T1(");
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
          sbSQL.append(" ) )AS RE ON DUPLICATE KEY UPDATE ");
          sbSQL.append(" SHNCD = RE.SHNCD");
          sbSQL.append(", SHNKN = RE.SHNKN");
          sbSQL.append(", KIKKN = RE.KIKKN");
          sbSQL.append(", IRISU = RE.IRISU");
          sbSQL.append(", BAIKAAM = RE.BAIKAAM");
          sbSQL.append(", GENKAAM = RE.GENKAAM");
          sbSQL.append(", MAKERKN = RE.MAKERKN");
          sbSQL.append(", OPERATOR = '" + userId + "'");
          sbSQL.append(", UPDDT =  current_timestamp ");
          sbSQL.append(", UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + "");

          count = super.executeSQL(sbSQL.toString(), paramData);
          if (StringUtils.isEmpty(getMessage())) {
            if (DefineReport.ID_DEBUG_MODE)
              System.out.println("冷凍食品_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
            msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
          } else {
            msgObj.put(MsgKey.E.getKey(), getMessage());
            return msgObj;
          }
          paramData = new ArrayList<>();
          values = "";
          for (int j = 0; j < 6; j++) {
            shnValues = "";
            if (j == 0) {
              shnValues = szHbstdt;
            } else if (j == 1) {
              shnValues = szBmncd;
            } else if (j == 2) {
              shnValues = szWaribiki;
            } else if (j == 3) {
              shnValues = szSeisi;
            } else if (j == 4) {
              shnValues = szDummycd;
            } else if (j == 5) {
              shnValues = String.valueOf(kanriNo);// F6
            }
            values += ", ?";
            paramData.add(shnValues);
          }

          values = StringUtils.removeStart(values, ",");
          sbSQL = new StringBuffer();
          sbSQL.append("INSERT INTO INATK.SYSRS  ( ");
          sbSQL.append(" HBSTDT"); // 販売開始日
          sbSQL.append(",BMNCD"); // 部門
          sbSQL.append(",WRITUKBN"); // 割引率区分
          sbSQL.append(",SEICUTKBN"); // 正規・カット
          sbSQL.append(",DUMMYCD"); // ダミーコード
          sbSQL.append(",SUMI_KANRINO"); // 管理番号
          sbSQL.append(",OPERATOR"); // オペレーターコード
          sbSQL.append(",ADDDT"); // 登録日
          sbSQL.append(",UPDDT"); // 更新日
          sbSQL.append(" )SELECT"); // 販売開始日 F1
          sbSQL.append(" HBSTDT"); // 販売開始日 F1
          sbSQL.append(",BMNCD"); // 部門 F2
          sbSQL.append(",WRITUKBN"); // 割引率区分 F3
          sbSQL.append(",SEICUTKBN"); // 正規・カット F4
          sbSQL.append(",DUMMYCD"); // ダミーコード F5
          sbSQL.append(",SUMI_KANRINO"); // 管理番号 F6
          sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
          sbSQL.append(", current_timestamp AS ADDDT "); // 登録日：
          sbSQL.append(", current_timestamp AS UPDDT "); // 更新日：
          sbSQL.append(" FROM  ");
          sbSQL.append(" (values ROW(" + values + "");
          sbSQL.append(" ) ) as RE(");
          sbSQL.append(" HBSTDT"); // 販売開始日
          sbSQL.append(",BMNCD"); // 部門
          sbSQL.append(",WRITUKBN"); // 割引率区分
          sbSQL.append(",SEICUTKBN"); // 正規・カット
          sbSQL.append(",DUMMYCD"); // ダミーコード
          sbSQL.append(",SUMI_KANRINO"); // 管理番号
          sbSQL.append(" )");
          sbSQL.append(" ON DUPLICATE KEY UPDATE ");
          sbSQL.append(" HBSTDT = RE.HBSTDT");
          sbSQL.append(", BMNCD = RE.BMNCD");
          sbSQL.append(", WRITUKBN = RE.WRITUKBN");
          sbSQL.append(", SEICUTKBN = RE.SEICUTKBN");
          sbSQL.append(", DUMMYCD = RE.DUMMYCD");
          sbSQL.append(", SUMI_KANRINO = RE.SUMI_KANRINO");
          sbSQL.append(", OPERATOR = '" + userId + "'");
          sbSQL.append(", UPDDT = current_timestamp");

          count = super.executeSQL(sbSQL.toString(), paramData);
          if (StringUtils.isEmpty(getMessage())) {
            if (DefineReport.ID_DEBUG_MODE)
              System.out.println("冷凍食品内部管理テーブルを " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
            msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
          } else {
            msgObj.put(MsgKey.E.getKey(), getMessage());
            return msgObj;
          }
        }
        String szSEQ = map.get("SEQ"); // 正視・カット
        sbSQL = new StringBuffer();
        sbSQL.append(" UPDATE ");
        sbSQL.append(" INATK.CSVTOK_RSKKK ");
        sbSQL.append(" SET ");
        sbSQL.append(" UPDKBN = ");
        sbSQL.append(DefineReport.ValUpdkbn.DEL.getVal());
        sbSQL.append(" where ");
        sbSQL.append(" SEQ =  ");
        sbSQL.append(szSEQ);
        sbSQL.append(" and INPUTNO = 1 ");
        paramData = new ArrayList<>();
        count = super.executeSQL(sbSQL.toString(), paramData);

        if (StringUtils.isEmpty(getMessage())) {
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("CSV取込トラン_冷凍食品_企画を " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
          msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
        } else {
          msgObj.put(MsgKey.E.getKey(), getMessage());
          return msgObj;
        }
      }
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
    String userId = userInfo.getId(); // ログインユーザー
    String szSEQ = map.get("SEQ"); // 催し区分
    String szInpuno = map.get("INPUTNO"); // 部門コード
    ArrayList<String> paramData = new ArrayList<>();

    paramData.add(userId);
    paramData.add(szSEQ);
    paramData.add(szInpuno);

    JSONObject msgObj = new JSONObject();

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    new ArrayList<String>();

    // 削除処理：更新区分に"1"（削除）を登録
    sbSQL = new StringBuffer();
    sbSQL.append("  UPDATE INATK.CSVTOK_RSSHN SET ");
    sbSQL.append("  UPDKBN = "); // 販売開始日 F1
    sbSQL.append(DefineReport.ValUpdkbn.DEL.getVal()); // 更新区分：
    sbSQL.append(", OPERATOR = ? "); // オペレーター：
    sbSQL.append(", UPDDT = current_timestamp   "); // 更新日：
    sbSQL.append("  where");
    sbSQL.append(" SEQ = ? ");
    sbSQL.append(" and INPUTNO = ?");

    int count = super.executeSQL(sbSQL.toString(), paramData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("CSV取込トラン_冷凍食品_商品マスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
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
