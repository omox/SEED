package dao;

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
import dao.ReportTG016Dao.TOK_CMN_MySQL_Layout;
import dao.ReportTM002Dao.TOKMOYCDCMNLayout;
import dao.Reportx002Dao.MSTSHNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSO003Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /** CSV取込トラン特殊情報保持用 */
  String[] csvtokso_add_data = new String[CSVTOK_SOLayout.values().length];

  /** 登録後の画面への戻り値検索に使用する管理番号保持用 */
  String selectKanriNo = new String();

  /** 画面での新規登録を判断する */
  boolean isNew = false;

  /** 生活応援部門 */
  public enum TOKSO_BMNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門");

    private final Integer no;
    private final String col;
    private final String typ;
    private final String text;

    /** 初期化 */
    private TOKSO_BMNLayout(Integer no, String col, String typ, String text) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.text = text;
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

    /** @return tbl 列名(論理名称) */
    public String getText() {
      return text;
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

  /** 生活応援商品 */
  public enum TOKSO_SHNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SMALLINT", "管理番号"),
    /** 商品コード */
    SHNCD(6, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** メーカー名称 */
    MAKERKN(7, "MAKERKN", "VARCHAR(28)", "メーカー名称"),
    /** 商品名称 */
    SHNKN(8, "SHNKN", "VARCHAR(20)", "商品名称"),
    /** 規格名称 */
    KIKKN(9, "KIKKN", "VARCHAR(20)", "規格名称"),
    /** 入数 */
    IRISU(10, "IRISU", "SMALLINT", "入数"),
    /** 最低発注数 */
    MINSU(11, "MINSU", "SMALLINT", "最低発注数"),
    /** 原価 */
    GENKAAM(12, "GENKAAM", "DECIMAL", "原価"),
    /** A売価 */
    A_BAIKAAM(13, "A_BAIKAAM", "INTEGER", "A売価"),
    /** B売価 */
    B_BAIKAAM(14, "B_BAIKAAM", "INTEGER", "B売価"),
    /** C売価 */
    C_BAIKAAM(15, "C_BAIKAAM", "INTEGER", "C売価"),
    /** Aランク */
    A_RANKNO(16, "A_RANKNO", "SMALLINT", "Aランク"),
    /** Bランク */
    B_RANKNO(17, "B_RANKNO", "SMALLINT", "Bランク"),
    /** Cランク */
    C_RANKNO(18, "C_RANKNO", "SMALLINT", "Cランク"),
    /** POPコード */
    POPCD(19, "POPCD", "INTEGER ", "POPコード"),
    /** POPサイズ */
    POPSZ(20, "POPSZ", "VARCHAR(3) ", "POPサイズ"),
    /** 枚数 */
    POPSU(21, "POPSU", "SMALLINT", "枚数"),
    /** 店扱いフラグ配列 */
    TENATSUK_ARR(22, "TENATSUK_ARR", "VARCHAR(400)", "店扱いフラグ配列");

    private final Integer no;
    private final String col;
    private final String typ;
    private final String text;

    /** 初期化 */
    private TOKSO_SHNLayout(Integer no, String col, String typ, String text) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.text = text;

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

    /** @return tbl 列名(論理名称) */
    public String getText() {
      return text;
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

  /** CSV取込トラン_生活応援レイアウト(正マスタとの差分) */
  public enum CSVTOK_SOLayout implements MSTLayout {
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
    /** 商品コード */
    SHNCD(11, "SHNCD", "CHARACTER(14)"),
    /** 部門 */
    BMNCD(12, "BMNCD", "SMALLINT"),
    /** メーカー名称 */
    MAKERKN(13, "MAKERKN", "VARCHAR(28)"),
    /** 商品名称 */
    SHNKN(14, "SHNKN", "VARCHAR(20)"),
    /** 規格名称 */
    KIKKN(15, "KIKKN", "VARCHAR(20)"),
    /** 入数 */
    IRISU(16, "IRISU", "SMALLINT"),
    /** 最低発注数 */
    MINSU(17, "MINSU", "SMALLINT"),
    /** 原価 */
    GENKAAM(18, "GENKAAM", "DECIMAL"),
    /** A売価 */
    A_BAIKAAM(19, "A_BAIKAAM", "INTEGER"),
    /** B売価 */
    B_BAIKAAM(20, "B_BAIKAAM", "INTEGER"),
    /** C売価 */
    C_BAIKAAM(21, "C_BAIKAAM", "INTEGER"),
    /** Aランク */
    A_RANKNO(22, "A_RANKNO", "SMALLINT"),
    /** Bランク */
    B_RANKNO(23, "B_RANKNO", "SMALLINT"),
    /** Cランク */
    C_RANKNO(24, "C_RANKNO", "SMALLINT"),
    /** POPコード */
    POPCD(25, "POPCD", "INTEGER"),
    /** POPサイズ */
    POPSZ(26, "POPSZ", "VARCHAR(3)"),
    /** 枚数 */
    POPSU(27, "POPSU", "SMALLINT"),
    /** 店扱いフラグ配列 */
    TENATSUK_ARR(28, "TENATSUK_ARR", "VARCHAR");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private CSVTOK_SOLayout(Integer no, String col, String typ) {
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

  // 名称検索
  public final static String ID_SQL_SHNKN_SO003 = "" + "select" + " case when SHN.POPKN is null or SHN.POPKN = '' then '　' else SHN.POPKN end as F1" + ", SHN.KIKKN as F2" + ", SHN.RG_IRISU as F3"

      + ", case when COALESCE(SHN.HS_SPOTMINSU, 0) = 0 then 1 else SHN.HS_SPOTMINSU end as F4" + ", SHN.RG_GENKAAM as F5" + ", SHN.SANCHIKN as F6"

      + ", case" + "  when COALESCE(SHN.HS_GENKAAM, 0) <> 0 and COALESCE(SHN.HS_BAIKAAM, 0) <> 0 and COALESCE(SHN.HS_IRISU, 0) <> 0 then SHN.HS_BAIKAAM" + "  else SHN.RG_BAIKAAM end as F7"
      + ", SHN.RG_IRISU as F8" + ", case" + "  when COALESCE(SHN.HS_GENKAAM, 0) <> 0 and COALESCE(SHN.HS_BAIKAAM, 0) <> 0 and COALESCE(SHN.HS_IRISU, 0) <> 0 then SHN.HS_GENKAAM"
      + "  else SHN.RG_GENKAAM end as F9" + ", '' as F10" + ", '' as F11" + ", '' as F12" + ", '' as F13" + ", '' as F14" + ", '' as F15" + ", '' as F16" + ", '' as F17" + ", SHN.RG_BAIKAAM as F18" // エラーチェック用：レギュラー標準売価
      + " from INAMS.MSTSHN SHN" + " left join INAMS.MSTMAKER MAK on MAK.MAKERCD = SHN.MAKERCD" + " where COALESCE(SHN.UPDKBN, 0) <> 1" + " and SHN.SHNCD = ?";

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportSO003Dao(String JNDIname) {
    super(JNDIname);
  }

  /**
   * 検索実行
   *
   * @return
   */
  @Override
  public boolean selectForDL() {

    // 検索コマンド生成
    String command = createCommandForDl();

    // 出力用検索条件生成
    outputQueryList();

    // 検索実行
    return super.selectBySQL(command);
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
      // option = this.deleteDataSub(map, userInfo, sysdate);
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

    String szBumon = getMap().get("BMNCD"); // 部門
    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = getMap().get("MOYSRBAN"); // 催し連番
    String szSeq = getMap().get("SEQ"); // SEQ
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン
    String countRows = "";
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    if (StringUtils.equals(DefineReport.Button.ERR_CHANGE.getObj(), sendBtnid)) {

      // パラメータ確認
      // 必須チェック
      if (szSeq == null) {
        System.out.println(super.getConditionLog());
        return "";
      }

      sbSQL.append("select");
      sbSQL.append(" trim(T2.SHNCD)"); // F1: 商品コード
      sbSQL.append(", T2.MAKERKN"); // F2: メーカー名
      sbSQL.append(", T2.SHNKN"); // F3: 商品名称
      sbSQL.append(", T2.KIKKN"); // F4: 規格
      sbSQL.append(", T2.RG_IRISU"); // F5: レギュラー入数
      sbSQL.append(", T2.IRISU"); // F6: 生活応援入数
      sbSQL.append(", T2.MINSU"); // F7: 最低発注数
      sbSQL.append(", T2.RG_GENKAAM"); // F8: 通常原価
      sbSQL.append(", T2.GENKAAM"); // F9: 生活応援原価
      sbSQL.append(", T2.A_BAIKAAM"); // F10: A総売価
      sbSQL.append(", T2.A_HONBAIK"); // F11: A本売価
      sbSQL.append(", T2.A_RANKNO"); // F12: Aランク
      sbSQL.append(", truncate((T2.A_HONBAIK - T2.GENKAAM) * 100 / T2.A_HONBAIK, 2)"); // F13: A値入率
      sbSQL.append(", T2.B_BAIKAAM"); // F14: B総売価
      sbSQL.append(", T2.B_HONBAIK"); // F15: B本売価
      sbSQL.append(", T2.B_RANKNO"); // F16: Bランク
      sbSQL.append(", case when T2.B_HONBAIK is null then null else truncate((T2.B_HONBAIK - T2.GENKAAM) * 100 / T2.B_HONBAIK, 2) end "); // F17: B値入率
      sbSQL.append(", T2.C_BAIKAAM"); // F18: C総売価
      sbSQL.append(", T2.C_HONBAIK"); // F19: C本売価
      sbSQL.append(", T2.C_RANKNO"); // F20: Cランク
      sbSQL.append(", case when T2.C_HONBAIK is null then null else truncate((T2.C_HONBAIK - T2.GENKAAM) * 100 / T2.C_HONBAIK, 2) end "); // F21: C値入率
      sbSQL.append(", T2.POPCD"); // F22: POPコード
      sbSQL.append(", T2.POPSZ"); // F23: POPサイズ
      sbSQL.append(", T2.POPSU"); // F24: 枚数
      sbSQL.append(", T2.ADDDT"); // F25: 登録
      sbSQL.append(", T2.UPDDT"); // F26: 更新
      sbSQL.append(", T2.OPERATOR"); // F27: オペレーター
      sbSQL.append(", T2.KANRINO"); // F28: 管理番号
      sbSQL.append(", T2.PLUSFLG"); // F29: PLG配信済フラグ
      sbSQL.append(", T2.UPDKBN"); // F30: 更新区分
      sbSQL.append(", null"); // F31: 売価
      sbSQL.append(", T2.SEQ"); // F32: SEQ
      sbSQL.append(", T2.INPUTNO"); // F33: 入力番号
      sbSQL.append(", T2.CSV_UPDKBN"); // F34: CSV更新区分
      sbSQL.append(", T2.RG_BAIKAAM"); // F35: レギュラー標準売価
      sbSQL.append(" from (");
      sbSQL.append("select");
      sbSQL.append(" CSO.INPUTNO");
      sbSQL.append(", CSO.SHNCD");
      sbSQL.append(", CSO.MAKERKN");
      sbSQL.append(", CSO.SHNKN");
      sbSQL.append(", CSO.KIKKN");
      sbSQL.append(", M0.RG_IRISU");
      sbSQL.append(", CSO.IRISU");
      sbSQL.append(", CSO.MINSU");
      sbSQL.append(", M0.RG_GENKAAM");
      sbSQL.append(", CSO.GENKAAM");
      sbSQL.append(", M0.RG_BAIKAAM");
      sbSQL.append(", CSO.A_BAIKAAM");
      sbSQL.append("," + DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "CSO.A_BAIKAAM").replaceAll("@DT", "MYCD.MOYSSTDT") + " as A_HONBAIK");
      sbSQL.append(", CSO.A_RANKNO");
      sbSQL.append(", null as '13'");
      sbSQL.append(", CSO.B_BAIKAAM");
      sbSQL.append(
          ",case when CSO.B_BAIKAAM is null then null else " + DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "CSO.B_BAIKAAM").replaceAll("@DT", "MYCD.MOYSSTDT") + " end as B_HONBAIK");
      sbSQL.append(", CSO.B_RANKNO");
      sbSQL.append(", null as '17'");
      sbSQL.append(", CSO.C_BAIKAAM");
      sbSQL.append(
          ",case when CSO.C_BAIKAAM is null then null else " + DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "CSO.C_BAIKAAM").replaceAll("@DT", "MYCD.MOYSSTDT") + " end as C_HONBAIK");
      sbSQL.append(", CSO.C_RANKNO");
      sbSQL.append(", null as '21'");
      sbSQL.append(", CSO.POPCD");
      sbSQL.append(", CSO.POPSZ");
      sbSQL.append(", CSO.POPSU");
      sbSQL.append(", MYCD.PLUSFLG");
      sbSQL.append(", CSO.SEQ");
      sbSQL.append(", COALESCE(CSO.UPDKBN, 0) as UPDKBN");
      sbSQL.append(", DATE_FORMAT(CSO.ADDDT, '%y/%m/%d') as ADDDT");
      sbSQL.append(", DATE_FORMAT(CSO.UPDDT, '%y/%m/%d') as UPDDT");
      sbSQL.append(", CSO.OPERATOR");
      sbSQL.append(", CSO.CSV_UPDKBN");
      sbSQL.append(", TKS.KANRINO");
      sbSQL.append(" from INATK.TOKMOYCD MYCD");
      sbSQL.append(" left join INATK.CSVTOK_SO CSO on CSO.MOYSKBN = MYCD.MOYSKBN and CSO.MOYSSTDT = MYCD.MOYSSTDT and CSO.MOYSRBAN = MYCD.MOYSRBAN");
      sbSQL.append(" left join (");
      sbSQL.append("  select");
      sbSQL.append("   MOYSKBN");
      sbSQL.append(" , MOYSSTDT");
      sbSQL.append(" , MOYSRBAN");
      sbSQL.append(" , BMNCD");
      sbSQL.append(" , SHNCD");
      sbSQL.append(" , MAX(KANRINO) as KANRINO");
      sbSQL.append("  from INATK.TOKSO_SHN");
      sbSQL.append("  where COALESCE(UPDKBN, 0) <> 1");
      sbSQL.append("  group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, SHNCD");
      sbSQL.append(") TKS on TKS.MOYSKBN = MYCD.MOYSKBN and TKS.MOYSSTDT = MYCD.MOYSSTDT and TKS.MOYSRBAN = MYCD.MOYSRBAN and TKS.BMNCD = CSO.BMNCD and TKS.SHNCD = CSO.SHNCD");
      sbSQL.append(" inner join INATK.CSVTOKHEAD CHEAD on CHEAD.SEQ = CSO.SEQ");
      sbSQL.append(" left outer join INAMS.MSTSHN M0 on M0.SHNCD = CSO.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTBMN M1 on M1.BMNCD = CSO.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1");
      sbSQL.append(" where CSO.SEQ = " + szSeq);
      sbSQL.append(" and COALESCE(CSO.ERRCD, 0) <> 4");
      sbSQL.append(" and COALESCE(CSO.UPDKBN , 0) <> 1");
      sbSQL.append(" ) T2 ");
      sbSQL.append(" order by T2.SHNCD");

    } else {

      // パラメータ確認
      // 必須チェック
      if (szBumon == null && szMoyskbn == null && szMoysstdt == null && szMoysrban == null) {
        System.out.println(super.getConditionLog());
        return "";
      }

      String kijundt = common.CmnDate.dateFormat(common.CmnDate.convYYMMDD(szMoysstdt));

      if (array.size() > 0) {
        countRows = array.getJSONObject(0).optString("F5");
      } else {
        countRows = "0";
      }


      sbSQL.append("with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 DUMMY) AS DUMMY union all select IDX + 1 from T1 where IDX < (" + countRows + " + 1))");
      sbSQL.append("select");
      sbSQL.append(" /*+ SET_VAR(cte_max_recursion_depth = 10000) */ ");
      sbSQL.append(" trim(T2.SHNCD)"); // F1: 商品コード
      sbSQL.append(", T2.MAKERKN"); // F2: メーカー名
      sbSQL.append(", T2.SHNKN"); // F3: 商品名称
      sbSQL.append(", T2.KIKKN"); // F4: 規格
      sbSQL.append(", T2.RG_IRISU"); // F5: レギュラー入数
      sbSQL.append(", T2.IRISU"); // F6: 生活応援入数
      sbSQL.append(", T2.MINSU"); // F7: 最低発注数
      sbSQL.append(", T2.RG_GENKAAM"); // F8: 通常原価
      sbSQL.append(", T2.GENKAAM"); // F9: 生活応援原価
      sbSQL.append(", T2.A_BAIKAAM"); // F10: A総売価
      sbSQL.append(", T2.A_HONBAIK"); // F11: A本売価
      sbSQL.append(", T2.A_RANKNO"); // F12: Aランク
      sbSQL.append(", TRUNCATE(CAST(T2.A_HONBAIK - T2.GENKAAM AS DECIMAL) * 100 / T2.A_HONBAIK, 2)"); // F13: A値入率
      sbSQL.append(", T2.B_BAIKAAM"); // F14: B総売価
      sbSQL.append(", T2.B_HONBAIK"); // F15: B本売価
      sbSQL.append(", T2.B_RANKNO"); // F16: Bランク
      sbSQL.append(", null "); // F17: B値入率
      sbSQL.append(", T2.C_BAIKAAM"); // F18: C総売価
      sbSQL.append(", T2.C_HONBAIK"); // F19: C本売価
      sbSQL.append(", T2.C_RANKNO"); // F20: Cランク
      sbSQL.append(", null "); // F21: C値入率
      sbSQL.append(", T2.POPCD"); // F22: POPコード
      sbSQL.append(", T2.POPSZ"); // F23: POPサイズ
      sbSQL.append(", T2.POPSU"); // F24: 枚数
      sbSQL.append(", DATE_FORMAT(T2.ADDDT, '%y%m%d')"); // F25: 登録
      sbSQL.append(", DATE_FORMAT(T2.UPDDT, '%y%m%d')"); // F26: 更新
      sbSQL.append(", T2.OPERATOR"); // F27: オペレーター
      sbSQL.append(", T2.KANRINO"); // F28: 管理番号
      sbSQL.append(", T2.PLUSFLG"); // F29: PLG配信済フラグ
      sbSQL.append(", T2.UPDKBN"); // F30: 更新区分
      sbSQL.append(", null"); // F31: 売価
      sbSQL.append(", null"); // F32: SEQ
      sbSQL.append(", null"); // F33: 入力番号
      sbSQL.append(", null"); // F34: CSV更新区分
      sbSQL.append(", T2.RG_BAIKAAM"); // F35: レギュラー標準売価
      sbSQL.append(" from T1 left join (");
      sbSQL.append("select");
      sbSQL.append(" ROW_NUMBER() over (order by SHN.SHNCD) as IDX");
      sbSQL.append(", SHN.SHNCD");
      sbSQL.append(", SHN.MAKERKN");
      sbSQL.append(", SHN.SHNKN");
      sbSQL.append(", SHN.KIKKN");
      sbSQL.append(", M0.RG_IRISU");
      sbSQL.append(", SHN.IRISU");
      sbSQL.append(", SHN.MINSU");
      sbSQL.append(", M0.RG_GENKAAM");
      sbSQL.append(", SHN.GENKAAM");
      sbSQL.append(", M0.RG_BAIKAAM");
      sbSQL.append(", SHN.A_BAIKAAM");
      sbSQL.append(", SHN.A_RANKNO");
      sbSQL.append(", SHN.B_BAIKAAM");
      sbSQL.append(", SHN.B_RANKNO");
      sbSQL.append(", SHN.C_BAIKAAM");
      sbSQL.append(", SHN.C_RANKNO");
      sbSQL.append("," + DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "SHN.A_BAIKAAM").replaceAll("@DT", kijundt) + " as A_HONBAIK");
      sbSQL.append(",null as B_HONBAIK");
      sbSQL.append(",null as C_HONBAIK");
      sbSQL.append(", SHN.POPCD");
      sbSQL.append(", SHN.POPSZ");
      sbSQL.append(", SHN.POPSU");
      sbSQL.append(", SHN.ADDDT");
      sbSQL.append(", SHN.UPDDT");
      sbSQL.append(", SHN.OPERATOR");
      sbSQL.append(", SHN.KANRINO ");
      sbSQL.append(", MYCD.PLUSFLG");
      sbSQL.append(", COALESCE(SHN.UPDKBN, 0) as UPDKBN");
      sbSQL.append(" from INATK.TOKMOYCD MYCD");
      sbSQL.append(" left join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN");
      sbSQL.append(" left join INATK.TOKSO_SHN SHN on SHN.MOYSKBN = MYCD.MOYSKBN and SHN.MOYSSTDT = MYCD.MOYSSTDT and SHN.MOYSRBAN = MYCD.MOYSRBAN and SHN.BMNCD = BMN.BMNCD");
      sbSQL.append(" left outer join INAMS.MSTSHN M0 on M0.SHNCD = SHN.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTBMN M1 on M1.BMNCD = SHN.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1");
      sbSQL.append(" left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1");
      sbSQL.append(" where COALESCE(SHN.UPDKBN, 0) <> 1 and MYCD.MOYSKBN = " + szMoyskbn + " and MYCD.MOYSSTDT = " + szMoysstdt + " and MYCD.MOYSRBAN = " + szMoysrban + " and SHN.BMNCD = " + szBumon);
      sbSQL.append(") AS T2 on T1.IDX = T2.IDX");
      sbSQL.append(" order by T2.KANRINO IS NULL ASC ,T2.KANRINO ");
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }

  private String createCommandForDl() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<>();

    JSONArray dataArray = JSONArray.fromObject(getMap().get("NEW_DATA")); // 新規登録情報

    String szBumon = getMap().get("BMNCD"); // 部門
    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = getMap().get("MOYSRBAN"); // 催し連番

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append(" 'U' as RCKBN");
    sbSQL.append(", SHN.MOYSKBN || SHN.MOYSSTDT || right ('000' || SHN.MOYSRBAN, 3) as MOYSCD");
    sbSQL.append(", SHN.BMNCD");
    sbSQL.append(", SHN.KANRINO");
    sbSQL.append(", SHN.SHNCD");
    sbSQL.append(", SHN.MAKERKN");
    sbSQL.append(", SHN.SHNKN");
    sbSQL.append(", SHN.KIKKN");
    sbSQL.append(", M0.DAICD");
    sbSQL.append(", M0.CHUCD");
    sbSQL.append(", M0.SHOCD");
    sbSQL.append(", M0.RG_IRISU");
    sbSQL.append(", SHN.IRISU");
    sbSQL.append(", SHN.MINSU");
    sbSQL.append(", M0.RG_GENKAAM");
    sbSQL.append(", SHN.GENKAAM");
    sbSQL.append(", M0.RG_BAIKAAM");
    sbSQL.append(", SHN.A_BAIKAAM");
    sbSQL.append(", SHN.B_BAIKAAM");
    sbSQL.append(", SHN.C_BAIKAAM");
    sbSQL.append(", SHN.A_RANKNO");
    sbSQL.append(", SHN.B_RANKNO");
    sbSQL.append(", SHN.C_RANKNO");
    sbSQL.append(", SHN.POPCD");
    sbSQL.append(", SHN.POPSU");
    sbSQL.append(", SHN.POPSZ");
    sbSQL.append(", M0.ZEIKBN");
    sbSQL.append(", M0.ZEIRTKBN");
    sbSQL.append(", M0.ZEIRTKBN_OLD");
    sbSQL.append(", M0.ZEIRTHENKODT");
    sbSQL.append(", '0D0A' as KGCD");
    sbSQL.append(" from INATK.TOKMOYCD MYCD");
    sbSQL.append(" inner join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN");
    sbSQL.append(" inner join INATK.TOKSO_SHN SHN on SHN.MOYSKBN = BMN.MOYSKBN and SHN.MOYSSTDT = BMN.MOYSSTDT and SHN.MOYSRBAN = BMN.MOYSRBAN and SHN.BMNCD = BMN.BMNCD");
    sbSQL.append(" inner join INAMS.MSTSHN M0 on M0.SHNCD = SHN.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1");
    sbSQL.append(" where COALESCE(SHN.UPDKBN, 0) <> 1");
    sbSQL.append(" and BMN.MOYSKBN = " + szMoyskbn);
    sbSQL.append(" and BMN.MOYSSTDT = " + szMoysstdt);
    sbSQL.append(" and BMN.MOYSRBAN = " + szMoysrban);
    sbSQL.append(" and BMN.BMNCD = " + szBumon);

    // 新規登録データ用SQL
    if (dataArray.size() > 0) {

      Object[] valueData = new Object[] {};
      String values = "";

      int maxField = 19; // Fxxの最大値
      for (int j = 0; j < dataArray.size(); j++) {
        JSONObject data = dataArray.getJSONObject(j);
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          String val = data.optString(key);
          if (StringUtils.isEmpty(val)) {
            values += ", null";
          } else {
            if (ArrayUtils.contains(new String[] {"F1", "F4", "F5", "F6", "F7", "F19"}, key)) {
              val = "'" + val + "'";
            }

            values += ", " + val;
          }
          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, "(" + StringUtils.removeStart(values, ",") + ")");
            values = "";
          }
        }
      }

      sbSQL.append(" union all select");
      sbSQL.append(" 'A' as RCKBN");
      sbSQL.append(", T1.MOYSCD");
      sbSQL.append(", T1.BMNCD");
      sbSQL.append(", T1.KANRINO");
      sbSQL.append(", T1.SHNCD");
      sbSQL.append(", T1.MAKERKN");
      sbSQL.append(", T1.SHNKN");
      sbSQL.append(", T1.KIKKN");
      sbSQL.append(", SHN.DAICD");
      sbSQL.append(", SHN.CHUCD");
      sbSQL.append(", SHN.SHOCD");
      sbSQL.append(", SHN.RG_IRISU");
      sbSQL.append(", T1.IRISU");
      sbSQL.append(", T1.MINSU");
      sbSQL.append(", SHN.RG_GENKAAM");
      sbSQL.append(", T1.GENKAAM");
      sbSQL.append(", SHN.RG_BAIKAAM");
      sbSQL.append(", T1.A_BAIKAAM");
      sbSQL.append(", T1.B_BAIKAAM");
      sbSQL.append(", T1.C_BAIKAAM");
      sbSQL.append(", T1.A_RANKNO");
      sbSQL.append(", T1.B_RANKNO");
      sbSQL.append(", T1.C_RANKNO");
      sbSQL.append(", T1.POPCD");
      sbSQL.append(", T1.POPSU");
      sbSQL.append(", T1.POPSZ");
      sbSQL.append(", SHN.ZEIKBN");
      sbSQL.append(", SHN.ZEIRTKBN");
      sbSQL.append(", SHN.ZEIRTKBN_OLD");
      sbSQL.append(", SHN.ZEIRTHENKODT");
      sbSQL.append(", '0D0A' as KGCD");
      sbSQL.append(" from (values " + StringUtils.join(valueData, ",") + ") as T1(");
      sbSQL.append(" MOYSCD");
      sbSQL.append(", BMNCD");
      sbSQL.append(", KANRINO");
      sbSQL.append(", SHNCD");
      sbSQL.append(", MAKERKN");
      sbSQL.append(", SHNKN");
      sbSQL.append(", KIKKN");
      sbSQL.append(", IRISU");
      sbSQL.append(", MINSU");
      sbSQL.append(", GENKAAM");
      sbSQL.append(", A_BAIKAAM");
      sbSQL.append(", B_BAIKAAM");
      sbSQL.append(", C_BAIKAAM");
      sbSQL.append(", A_RANKNO");
      sbSQL.append(", B_RANKNO");
      sbSQL.append(", C_RANKNO");
      sbSQL.append(", POPCD");
      sbSQL.append(", POPSU");
      sbSQL.append(", POPSZ)");
      sbSQL.append(" left join INAMS.MSTSHN SHN on SHN.SHNCD = T1.SHNCD");
    }

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
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szBumon = map.get("BMNCD"); // 部門
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番
    String szSeq = map.get("SEQ"); // 催し連番
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    ArrayList<String> paramData = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    if (StringUtils.equals(DefineReport.Button.ERR_CHANGE.getObj(), sendBtnid)) {

      sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
      sbSQL.append("select");
      sbSQL.append(" T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F1");
      sbSQL.append(", T1.MOYKN as F2");
      sbSQL.append(
          ", DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'))) || '～' || DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as F3");
      sbSQL.append(", right('00' || T2.BMNCD, 2) || '-' || BMN.BMNKN as F4");
      sbSQL.append(", T2.COUNT as F5");
      sbSQL.append(", T1.PLUSFLG as F7");
      sbSQL.append(" from INATK.TOKMOYCD T1");
      sbSQL.append(" inner join (select");
      sbSQL.append(" SEQ, MAX(MOYSKBN) as MOYSKBN");
      sbSQL.append(", MAX(MOYSSTDT) as MOYSSTDT");
      sbSQL.append(", MAX(MOYSRBAN) as MOYSRBAN");
      sbSQL.append(", MAX(BMNCD) as BMNCD");
      sbSQL.append(", COUNT(INPUTNO) as COUNT");
      sbSQL.append(" from INATK.CSVTOK_SO");
      sbSQL.append(" where COALESCE(UPDKBN, 0) <> 1 and SEQ = " + szSeq);
      sbSQL.append(" group by SEQ) T2 on T2.MOYSKBN = T1.MOYSKBN and T2.MOYSSTDT = T1.MOYSSTDT and T2.MOYSRBAN = T1.MOYSRBAN");
      sbSQL.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = T2.BMNCD");

    } else {
      sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
      sbSQL.append("select");
      sbSQL.append("  T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
      sbSQL.append(", T1.MOYKN as F2");
      sbSQL.append(", DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
      sbSQL.append("  ||'～'||");
      sbSQL.append("  DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as F3");
      sbSQL.append(", CASE WHEN T2.BMNCD IS NULL THEN ");
      sbSQL.append(" (SELECT right('00' || BMNCD, 2) || '-' || BMNKN FROM INAMS.MSTBMN WHERE UPDKBN <> 1 and BMNCD = " + szBumon + ") ELSE ");
      sbSQL.append(" right('00' || T2.BMNCD, 2) || '-' || BMN.BMNKN END as F4");
      sbSQL.append(", (select");
      sbSQL.append("   COUNT(SHNCD)");
      sbSQL.append("   from INATK.TOKSO_SHN");
      sbSQL.append("   where UPDKBN = 0");
      sbSQL.append("   and BMNCD = T2.BMNCD");
      sbSQL.append("   and MOYSKBN = T1.MOYSKBN");
      sbSQL.append("   and MOYSSTDT = T1.MOYSSTDT");
      sbSQL.append("   and MOYSRBAN = T1.MOYSRBAN) as F5");
      sbSQL.append(", DATE_FORMAT(T2.UPDDT, '%Y%m%d%H%i%s%f') as F6");
      sbSQL.append(", T1.PLUSFLG as F7");
      sbSQL.append(" from INATK.TOKMOYCD T1");
      sbSQL.append(" left join INATK.TOKSO_BMN T2 on COALESCE(T2.UPDKBN, 0) <> 1 and T2.MOYSKBN = T1.MOYSKBN and T2.MOYSSTDT = T1.MOYSSTDT and T2.MOYSRBAN = T1.MOYSRBAN and T2.BMNCD = " + szBumon);
      sbSQL.append(" left join INAMS.MSTBMN BMN on COALESCE(BMN.UPDKBN, 0) <> 1 and BMN.BMNCD = T2.BMNCD");
      sbSQL.append(" where T1.UPDKBN = 0");
      sbSQL.append(" and T1.MOYSKBN = " + szMoyskbn);
      sbSQL.append(" and T1.MOYSKBN = " + szMoyskbn);
      sbSQL.append(" and T1.MOYSSTDT = " + szMoysstdt);
      sbSQL.append(" and T1.MOYSRBAN = " + szMoysrban);
    }

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());

    return array;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(配送グループ)
    JSONArray dataArraySHN = JSONArray.fromObject(map.get("DATA_SHN")); // 更新情報(配送店グループ)
    map.get("SENDBTNID");
    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<>();

    new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    MessageUtility mu = new MessageUtility();
    new JSONArray();

    String kanriNo = "";

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    for (int i = 0; i < dataArraySHN.size(); i++) {
      data = dataArraySHN.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      // 重複チェック：商品コード
      if (StringUtils.isEmpty(data.optString("F5")) || (!StringUtils.isEmpty(data.optString("F24")))) {
        // 管理番号が空の時、エラー修正にてCSV登録区分が更新('U')の時
        paramData = new ArrayList<>();
        paramData.add(data.getString("F1"));
        paramData.add(data.getString("F2"));
        paramData.add(data.getString("F3"));
        paramData.add(data.getString("F4"));
        paramData.add(data.getString("F6"));
        sqlcommand = "select COUNT(SHNCD) as VALUE from INATK.TOKSO_SHN where COALESCE(UPDKBN, 0) <> 1 and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ? and SHNCD = ? ";

        @SuppressWarnings("static-access")
        JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

        if (StringUtils.equals("U", data.optString("F24"))) {
          if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
            JSONObject o = mu.getDbMessageObj("E20582", new String[] {});
            msg.add(o);
            return msg;
          }
        } else {
          if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
            JSONObject o = mu.getDbMessageObj("E00004", "商品コード");
            msg.add(o);
            return msg;
          }
        }
      }

      if (StringUtils.isEmpty(data.optString("F5"))) {
        if (StringUtils.isEmpty(kanriNo)) {
          kanriNo = "" + this.getKANRINO(data.optString("F1"), data.optString("F2"), data.optString("F3"), data.optString("F4"));
          if (StringUtils.isEmpty(kanriNo)) {
            kanriNo = "1";
          } else {
            kanriNo = "" + (Integer.parseInt(kanriNo) + 1);
          }
        } else {
          kanriNo = "" + (Integer.parseInt(kanriNo) + 1);
        }
      }

      if (!StringUtils.isEmpty(kanriNo) && Integer.valueOf(kanriNo) > 9999) {
        JSONObject o = mu.getDbMessageObj("E11234", new String[] {});
        msg.add(o);
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

    JSONArray.fromObject(map.get("DATA"));
    map.get("TENGPCD");

    new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    new JSONArray();
    new MessageUtility();


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
    JSONArray dataArrayCSV_Del = JSONArray.fromObject(map.get("DATA_CSV_DEL")); // 対象情報（削除データ）

    // 排他チェック用
    JSONArray msg = new JSONArray();

    String dispType = map.get("DISPTYPE");
    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    if (dataArrayCSV_Del.size() > 0) {
      // CSVエラー修正データの削除
      this.createSqlDelCSVSO(userInfo, dataArrayCSV_Del.getJSONObject(0));

    } else {
      // 生活応援_部門、生活応援_商品INSERT/UPDATE処理
      this.createSqlTOKSO(data, map, userInfo);
    }


    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<>();
    if (StringUtils.equals("SO005", dispType)) {

    } else {
      if (dataArray.size() > 0) {
        targetTable = "INATK.TOKSO_BMN";
        targetWhere = "MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ?";
        targetParam.add(data.optString("F1"));
        targetParam.add(data.optString("F2"));
        targetParam.add(data.optString("F3"));
        targetParam.add(data.optString("F4"));
        if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F5"))) {
          msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
          option.put(MsgKey.E.getKey(), msg);
          return option;
        }
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

        if (!StringUtils.equals("SO005", dispType)) {
          // CSV取込 エラー修正画面以外の時に、更新日、登録件数を取得
          this.setReturnData(data.optString("F1"), data.optString("F2"), data.optString("F3"), data.optString("F4"), option);
        }
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

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 生活応援削除
    this.createSqlDelTOKSO(userInfo, data, true);

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
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * 生活応援_部門、生活応援_商品INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTOKSO(JSONObject data, HashMap<String, String> map, User userInfo){

      StringBuffer sbSQL      = new StringBuffer();
      JSONArray dataArrayT    = JSONArray.fromObject(map.get("DATA_SHN"));        // 更新情報(予約発注_納品日)
      JSONArray dataArrayDel  = JSONArray.fromObject(map.get("DATA_SHN_DEL"));    // 対象情報（主要な更新情報）


      // ログインユーザー情報取得
      String userId   = userInfo.getId();                         // ログインユーザー
      String kanriNo  = "";                                       // 管理No
      String dispType = map.get("DISPTYPE");                      // 画面状態

      ArrayList<String> prmData = new ArrayList<String>();
      Object[] valueData = new Object[]{};
      String values = "";

      int maxField = 5;       // Fxxの最大値
      for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if(k == 1){
              values += String.valueOf(0 + 1);

          }

          if(! ArrayUtils.contains(new String[]{"F5"}, key)){
              String val = data.optString(key);
              if(StringUtils.isEmpty(val)){
                  values += ", null";
              }else{
                  values += ", ?";
                  prmData.add(val);
              }
          }

          if(k == maxField){
              valueData = ArrayUtils.add(valueData, "ROW("+values+")");
              values = "";
          }
      }

      // 生活応援_部門の登録・更新
      sbSQL = new StringBuffer();
      sbSQL.append(" INSERT INTO INATK.TOKSO_BMN ( ");
      sbSQL.append(" MOYSKBN");
      sbSQL.append(", MOYSSTDT");
      sbSQL.append(", MOYSRBAN");
      sbSQL.append(", BMNCD");
      sbSQL.append(", UPDKBN");
      sbSQL.append(", SENDFLG");
      sbSQL.append(", OPERATOR");
      sbSQL.append(", ADDDT");
      sbSQL.append(", UPDDT");
      sbSQL.append(") SELECT ");
      sbSQL.append(" MOYSKBN");
      sbSQL.append(", MOYSSTDT");
      sbSQL.append(", MOYSRBAN");
      sbSQL.append(", BMNCD");
      sbSQL.append(", UPDKBN");
      sbSQL.append(", SENDFLG");
      sbSQL.append(", OPERATOR");
      sbSQL.append(", ADDDT");
      sbSQL.append(", UPDDT");
      sbSQL.append(" FROM ( ");
      sbSQL.append("SELECT ");
      sbSQL.append(" TMP.MOYSKBN");                                                   // 催し区分
      sbSQL.append(", TMP.MOYSSTDT");                                                 // 催し開始日
      sbSQL.append(", TMP.MOYSRBAN");                                                 // 催し連番
      sbSQL.append(", TMP.BMNCD");                                                    // 部門
      sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");        // 更新区分：
      sbSQL.append(", 0 as SENDFLG");                                             // 送信フラグ
      sbSQL.append(", '"+userId+"' AS OPERATOR ");                                // オペレーター：
      sbSQL.append(", CURRENT_TIMESTAMP AS ADDDT ");                              // 登録日：
      sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT ");                              // 更新日：
      sbSQL.append("FROM ( VALUES " + StringUtils.join(valueData, ",") + ") ");
      sbSQL.append("AS TMP(NUM, MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD)");
      sbSQL.append(") AS T1 ");
      sbSQL.append("ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" MOYSKBN = VALUES(MOYSKBN) ");
      sbSQL.append(", MOYSSTDT = VALUES(MOYSSTDT) ");
      sbSQL.append(", MOYSRBAN = VALUES(MOYSRBAN) ");
      sbSQL.append(", BMNCD = VALUES(BMNCD) ");
      sbSQL.append(", UPDKBN = VALUES(UPDKBN) ");
      sbSQL.append(", SENDFLG = VALUES(SENDFLG) ");
      sbSQL.append(", OPERATOR = VALUES(OPERATOR) ");
      sbSQL.append(", UPDDT = VALUES(UPDDT) ");
     
      if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("生活応援_部門");

    // クリア
    prmData = new ArrayList<>();
    valueData = new Object[] {};
    values = "";

    maxField = 22; // Fxxの最大値
    int len = dataArrayT.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);



          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);

            // 新規登録時は管理番号を使用する。
            if (StringUtils.equals("F5", key)) {
              if (StringUtils.isEmpty(val)) {

                if (StringUtils.isNotEmpty(kanriNo)) {
                  kanriNo = "" + (Integer.parseInt(kanriNo) + 1);
                } else {
                  isNew = true; // 管理番号未入力の場合は新規登録
                  kanriNo = "" + this.getKANRINO(dataT.optString("F1"), dataT.optString("F2"), dataT.optString("F3"), dataT.optString("F4"));
                  if (StringUtils.isEmpty(kanriNo)) {
                    kanriNo = "1";
                  } else {
                    kanriNo = "" + (Integer.parseInt(kanriNo) + 1);
                  }
                }
                val = kanriNo;

              }
              selectKanriNo = val.toString(); // 登録成功時の検索処理で使用するため、管理番号を保持する。
            } else if (StringUtils.equals("F22", key)) {
              val = this.getTenCdAddABC(dataT.optString("F4"), dataT.optString("F1"), dataT.optString("F2"), dataT.optString("F3"), dataT.optString("F16"), dataT.optString("F17"),
                  dataT.optString("F18"));
            }

            if (StringUtils.isEmpty(val)) {
              values += "null, ";
            } else {
              values += "?, ";
              prmData.add(val);
            }
          }

          if (k == maxField) {

            values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
            values += ", 0";
            values += ", '" + userId + "'";
            values += ", CURRENT_TIMESTAMP";
            values += ", CURRENT_TIMESTAMP";

            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 生活応援_商品の登録・更新

        sbSQL = new StringBuffer();
        sbSQL.append(" INSERT into INATK.TOKSO_SHN  (");
        sbSQL.append(" MOYSKBN"); // 催し区分
        sbSQL.append(", MOYSSTDT"); // 催し開始日
        sbSQL.append(", MOYSRBAN"); // 催し連番
        sbSQL.append(", BMNCD"); // 部門
        sbSQL.append(", KANRINO"); // 管理番号
        sbSQL.append(", SHNCD"); // 商品コード
        sbSQL.append(", MAKERKN"); // メーカー名称
        sbSQL.append(", SHNKN"); // 商品名称
        sbSQL.append(", KIKKN"); // 規格名称
        sbSQL.append(", IRISU"); // 入数
        sbSQL.append(", MINSU"); // 最低発注数
        sbSQL.append(", GENKAAM"); // 原価
        sbSQL.append(", A_BAIKAAM"); // A売価
        sbSQL.append(", B_BAIKAAM"); // B売価
        sbSQL.append(", C_BAIKAAM"); // C売価
        sbSQL.append(", A_RANKNO"); // Aランク
        sbSQL.append(", B_RANKNO"); // Bランク
        sbSQL.append(", C_RANKNO"); // Cランク
        sbSQL.append(", POPCD"); // POPコード
        sbSQL.append(", POPSZ"); // POPサイズ
        sbSQL.append(", POPSU"); // 枚数
        sbSQL.append(", TENATSUK_ARR"); // 店扱いフラグ配列
        sbSQL.append(", UPDKBN"); // 更新区分：
        sbSQL.append(", SENDFLG"); // 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター：
        sbSQL.append(", ADDDT");
        sbSQL.append(", UPDDT "); // 更新日：
        sbSQL.append(") ");
        sbSQL.append("VALUES ");
        sbSQL.append(StringUtils.join(valueData, ",") + "AS NEW ");
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("MOYSKBN = NEW.MOYSKBN ");
        sbSQL.append(", MOYSSTDT = NEW.MOYSSTDT ");
        sbSQL.append(", BMNCD = NEW.BMNCD ");
        sbSQL.append(", KANRINO = NEW.KANRINO ");
        sbSQL.append(", SHNCD = NEW.SHNCD ");
        sbSQL.append(", MAKERKN = NEW. MAKERKN ");
        sbSQL.append(", SHNKN = NEW.SHNKN ");
        sbSQL.append(", KIKKN = NEW.KIKKN ");
        sbSQL.append(", SHNKN = NEW.SHNKN ");
        sbSQL.append(", KIKKN = NEW.KIKKN ");
        sbSQL.append(", IRISU = NEW.IRISU ");
        sbSQL.append(", MINSU = NEW.MINSU ");
        sbSQL.append(", GENKAAM = NEW.GENKAAM ");
        sbSQL.append(", A_BAIKAAM = NEW.A_BAIKAAM ");
        sbSQL.append(", B_BAIKAAM = NEW.B_BAIKAAM ");
        sbSQL.append(", C_BAIKAAM = NEW.C_BAIKAAM ");
        sbSQL.append(", A_RANKNO = NEW.A_RANKNO ");
        sbSQL.append(", B_RANKNO = NEW.B_RANKNO ");
        sbSQL.append(", C_RANKNO = NEW.C_RANKNO ");
        sbSQL.append(", POPCD = NEW.POPCD ");
        sbSQL.append(", POPSZ = NEW.POPSZ ");
        sbSQL.append(", POPSU = NEW.POPSU ");
        sbSQL.append(", TENATSUK_ARR = NEW.TENATSUK_ARR ");
        sbSQL.append(", UPDKBN = NEW.UPDKBN ");
        sbSQL.append(", SENDFLG = NEW.SENDFLG ");
        sbSQL.append(", OPERATOR = NEW.OPERATOR ");
        // sbSQL.append(", ADDDT = NEW.ADDDT ");
        sbSQL.append(", UPDDT = NEW.UPDDT ");

        /*
         * sbSQL.append(", MOYSSTDT"); // 催し開始日 sbSQL.append(", MOYSRBAN"); // 催し連番 sbSQL.append(", BMNCD");
         * // 部門 sbSQL.append(", KANRINO"); // 管理番号 sbSQL.append(", SHNCD"); // 商品コード
         * sbSQL.append(", MAKERKN"); // メーカー名称 sbSQL.append(", SHNKN"); // 商品名称 sbSQL.append(", KIKKN"); //
         * 規格名称 sbSQL.append(", IRISU"); // 入数 sbSQL.append(", MINSU"); // 最低発注数 sbSQL.append(", GENKAAM");
         * // 原価 sbSQL.append(", A_BAIKAAM"); // A売価 sbSQL.append(", B_BAIKAAM"); // B売価
         * sbSQL.append(", C_BAIKAAM"); // C売価 sbSQL.append(", A_RANKNO"); // Aランク
         * sbSQL.append(", B_RANKNO"); // Bランク sbSQL.append(", C_RANKNO"); // Cランク sbSQL.append(", POPCD");
         * // POPコード sbSQL.append(", POPSZ"); // POPサイズ sbSQL.append(", POPSU"); // 枚数
         * sbSQL.append(", TENATSUK_ARR"); // 店扱いフラグ配列 sbSQL.append(", UPDKBN"); // 更新区分：
         * sbSQL.append(", SENDFLG"); // 送信フラグ sbSQL.append(", OPERATOR "); // オペレーター：
         * sbSQL.append(", ADDDT"); sbSQL.append(", UPDDT "); // 更新日： sbSQL.append(") VALUES");
         */

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("生活応援_商品");

        // クリア
        prmData = new ArrayList<>();
        valueData = new Object[] {};
        values = "";
      }
    }

    // 生活応援_商品の削除
    if (dataArrayDel.size() > 0) {
      // 削除チェック有データ
      JSONObject dataDel = dataArrayDel.getJSONObject(0);
      this.createSqlDelTOKSO(userInfo, dataDel, false);

    } else if (StringUtils.equals("SO005", dispType)) {
      // 修正登録データ
      JSONObject dataDel = new JSONObject();
      dataDel.put("F1", dataArrayT.getJSONObject(0).optString("F22"));
      dataDel.put("F2", dataArrayT.getJSONObject(0).optString("F23"));

      this.createSqlDelCSVSO(userInfo, dataDel);
    }

    // 管理番号更新
    if (StringUtils.isNotEmpty(kanriNo)) {
      this.createSqlSYSMOYBMN(userId, map, data, SqlType.MRG, kanriNo);
    }


    return sbSQL.toString();
  }

  // 対象店取得処理
  public String getTenCdAddABC(String bmnCd, String moysKbn, String moysStDt, String moysRban, String rankNo_A, String rankNo_B, String rankNo_C) {

    ArrayList<String> paramData = new ArrayList<>();
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKRANK ";

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    JSONObject data = new JSONObject();

    ArrayList<String> rankNoList = new ArrayList<>();
    ArrayList<String> LankArray = new ArrayList<>();
    String rankdata = new String();

    String[] rankARR_A = new String[] {};
    String[] rankARR_B = new String[] {};
    String[] rankARR_C = new String[] {};

    rankNoList.add(rankNo_A);
    rankNoList.add(rankNo_B);
    rankNoList.add(rankNo_C);

    for (int i = 0; i < rankNoList.size(); i++) {
      String rankNo = rankNoList.get(i);
      if (StringUtils.isNotEmpty(rankNo)) {

        sbSQL = new StringBuffer();
        paramData = new ArrayList<>();
        sqlWhere = "";

        sbSQL.append("SELECT ");
        sbSQL.append("TENRANK_ARR ");
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");

        // 部門コード
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmnCd);

        // ランクNo.が900未満の場合参照テーブルを変更
        if (Integer.valueOf(rankNo) >= 900) {
          sqlFrom = "INATK.TOKRANKEX ";

          // 催し区分
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moysKbn);

          // 催し開始日
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysStDt);

          // 催し連番
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysRban);
        }

        // ランクNo.
        sqlWhere += "RANKNO=? AND ";
        paramData.add(rankNo);

        sbSQL.append(sqlWhere);
        sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

        dbDatas = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
        if (dbDatas.size() > 0) {
          data = dbDatas.getJSONObject(0);
          if (i == 0) {
            rankARR_A = data.optString("TENRANK_ARR").split("");
          } else if (i == 1) {
            rankARR_B = data.optString("TENRANK_ARR").split("");
          } else if (i == 2) {
            rankARR_C = data.optString("TENRANK_ARR").split("");

          }
        }
      }
    }

    // Aランクの値よりベースとなる配列を作成
    for (String element : rankARR_A) {
      if (rankARR_A.length > 0) {
        if (StringUtils.isNotEmpty(element.trim())) {
          LankArray.add("A");
        } else {
          LankArray.add(" ");
        }
      }
    }

    // Bランクの値を設定
    if (LankArray.size() > 0) {
      for (int i = 0; i < rankARR_B.length; i++) {
        if (StringUtils.isNotEmpty(rankARR_B[i].trim())) {
          if (StringUtils.isNotEmpty(LankArray.get(i).trim())) {
            LankArray.set(i, "B");
          }
        }
      }
    }

    // Cランクの値を設定
    if (LankArray.size() > 0) {
      for (int i = 0; i < rankARR_C.length; i++) {
        if (StringUtils.isNotEmpty(rankARR_C[i].trim())) {
          if (StringUtils.isNotEmpty(LankArray.get(i).trim())) {
            LankArray.set(i, "C");
          }
        }
      }
    }

    // 文字列に変換
    for (String element : LankArray) {
      rankdata += element;
    }
    return rankdata;
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
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * 管理番号取得処理(催し部門内部管理)
   *
   * @throws Exception
   */
  public String getKANRINO(String MOYSKBN, String MOYSSTDT, String MOYSRBAN, String BMNCD) {
    new ItemList();
    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> paramData = new ArrayList<>();

    paramData.add(MOYSKBN);
    paramData.add(MOYSSTDT);
    paramData.add(MOYSRBAN);
    paramData.add(BMNCD);

    sbSQL = new StringBuffer();
    sbSQL.append("select");
    sbSQL.append(" MAX(SUMI_KANRINO) as KANRINO");
    sbSQL.append(" from INATK.SYSMOYBMN");
    sbSQL.append(" where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ?");


    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("KANRINO");
    }
    return value;
  }

  /**
   * 管理番号取得保存(催し部門内部管理)
   *
   * @throws Exception
   */
  public String createSqlKANRINO(String MOYSKBN, String MOYSSTDT, String MOYSRBAN, String BMNCD) {
    new ItemList();
    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> paramData = new ArrayList<>();

    paramData.add(MOYSKBN);
    paramData.add(MOYSSTDT);
    paramData.add(MOYSRBAN);
    paramData.add(BMNCD);

    sbSQL = new StringBuffer();
    sbSQL.append("select");
    sbSQL.append(" MAX(SUMI_KANRINO) as KANRINO");
    sbSQL.append(" from INATK.SYSMOYBMN");
    sbSQL.append(" where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ?");


    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("KANRINO");
    }
    return value;
  }

  /**
   * 催し部門内部管理 SQL作成処理(INSERT/UPDATE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlSYSMOYBMN(String userId, HashMap<String, String> map, JSONObject data, SqlType sql, String kanriNo) {
    JSONObject result = new JSONObject();

    String moyskbn = data.getString("F1");
    String moysstdt = data.getString("F2");
    String moysrban = data.getString("F3");
    String bmncd = data.getString("F4");

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    // 基本INSERT文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.SYSMOYBMN ");
    sbSQL.append("( MOYSKBN");
    sbSQL.append(", MOYSSTDT");
    sbSQL.append(", MOYSRBAN");
    sbSQL.append(", BMNCD");
    sbSQL.append(", SUMI_KANRINO");
    sbSQL.append(", SUMI_HYOSEQNO");
    sbSQL.append(", OPERATOR");
    sbSQL.append(", ADDDT");
    sbSQL.append(", UPDDT");
    sbSQL.append(") VALUES");
    sbSQL.append("( ");
    // sbSQL.append("SELECT ( ");
    // キー情報はロックのため後で追加する
    for (TOK_CMN_MySQL_Layout itm : TOK_CMN_MySQL_Layout.values()) {
      if (itm.getNo() > 1 && itm.getCol() != TOK_CMN_MySQL_Layout.KANRIENO.getCol()) {
        sbSQL.append(",");
      }

      if (itm.getCol() == TOK_CMN_MySQL_Layout.KANRIENO.getCol()) {
        // 枝番の設定は行わない
        ;
      } else {
        sbSQL.append("CAST(? as " + itm.getTyp() + ") ");
      }
    }
    sbSQL.append(" ,NULL "); // F6 : 付番済表示順番
    sbSQL.append(" ,'" + userId + "' "); // オペレータ
    sbSQL.append(" ,CURRENT_TIMESTAMP "); // 登録日（更新）
    sbSQL.append(" ,CURRENT_TIMESTAMP "); // 更新日
    sbSQL.append(")");
    sbSQL.append(" ON DUPLICATE KEY UPDATE ");// 重複した時のUPDATE処理
    sbSQL.append(" SUMI_KANRINO = VALUES(SUMI_KANRINO) ");
    sbSQL.append(" ,SUMI_HYOSEQNO = VALUES(SUMI_HYOSEQNO) ");
    sbSQL.append(" ,OPERATOR = VALUES(OPERATOR)");
    sbSQL.append(" ,UPDDT = VALUES(UPDDT) ");


    // パラメータの設定
    prmData.add(moyskbn);
    prmData.add(moysstdt);
    prmData.add(moysrban);
    prmData.add(bmncd);
    prmData.add(kanriNo);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + this.getClass().getName() + "[sql]*/" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催し部門内部管理");
    return result;
  }


  /**
   * 更新日付再取得(生活応援_商品)
   *
   * @throws Exception
   */
  @SuppressWarnings("static-access")
  public void setReturnData(String MOYSKBN, String MOYSSTDT, String MOYSRBAN, String BMNCD, JSONObject option) {
    new ItemList();
    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> paramData = new ArrayList<>();
    new JSONObject();

    paramData.add(MOYSKBN);
    paramData.add(MOYSSTDT);
    paramData.add(MOYSRBAN);
    paramData.add(BMNCD);

    sbSQL = new StringBuffer();
    sbSQL.append("select");
    sbSQL.append(" DATE_FORMAT(SOB.UPDDT, '%Y%m%d%H%i%s%f') as UPDDT");
    sbSQL.append(", SOS.COUNT as COUNT_ROW");
    sbSQL.append(" from INATK.TOKMOYCD MYCD");
    sbSQL.append(" inner join INATK.TOKSO_BMN SOB on SOB.MOYSKBN = MYCD.MOYSKBN and SOB.MOYSSTDT = MYCD.MOYSSTDT and SOB.MOYSRBAN = MYCD.MOYSRBAN and MYCD.UPDKBN = 0 and SOB.UPDKBN = 0");
    sbSQL.append(" inner join (select");
    sbSQL.append("   MOYSKBN");
    sbSQL.append("  , MOYSSTDT");
    sbSQL.append("  , MOYSRBAN");
    sbSQL.append("  , BMNCD");
    sbSQL.append("  , COUNT(SHNCD) as COUNT");
    sbSQL.append("  from INATK.TOKSO_SHN");
    sbSQL.append("  where COALESCE(UPDKBN, 0) <> 1");
    sbSQL.append("  group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD");
    sbSQL.append(") SOS on SOS.MOYSKBN = SOB.MOYSKBN and SOS.MOYSSTDT = SOB.MOYSSTDT and SOS.MOYSRBAN = SOB.MOYSRBAN and SOS.BMNCD = SOB.BMNCD");
    sbSQL.append(" where COALESCE(SOB.UPDKBN, 0) <> 1");
    sbSQL.append(" and SOB.MOYSKBN = ?");
    sbSQL.append(" and SOB.MOYSSTDT = ?");
    sbSQL.append(" and SOB.MOYSRBAN = ?");
    sbSQL.append(" and SOB.BMNCD = ?");

    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
    String VALUE = ""; // 生活応援_部門 更新日
    String count = ""; // 登録商品数

    if (array.size() > 0) {
      VALUE = array.optJSONObject(0).optString("UPDDT");
      count = array.optJSONObject(0).optString("COUNT_ROW");

      option.put("UPDDT", VALUE);
      option.put("COUNT_ROWS", count);
    }

    // 登録後に画面に反映させるデータを取得する。
    if (StringUtils.isNotEmpty(selectKanriNo)) {

      paramData = new ArrayList<>();
      paramData.add(MOYSKBN);
      paramData.add(MOYSSTDT);
      paramData.add(MOYSRBAN);
      paramData.add(BMNCD);
      paramData.add(selectKanriNo);

      sbSQL = new StringBuffer();
      sbSQL.append(" select");
      sbSQL.append(" DATE_FORMAT(SOS.ADDDT, '%y%m%d') as F25"); // 登録日
      sbSQL.append(", DATE_FORMAT(SOS.UPDDT, '%y%m%d') as F26"); // 更新日
      sbSQL.append(", SOS.OPERATOR as F27"); // オペレーター
      sbSQL.append(" from INATK.TOKMOYCD MYCD");
      sbSQL.append(" inner join INATK.TOKSO_BMN SOB on SOB.MOYSKBN = MYCD.MOYSKBN and SOB.MOYSSTDT = MYCD.MOYSSTDT and SOB.MOYSRBAN = MYCD.MOYSRBAN and MYCD.UPDKBN = 0 and SOB.UPDKBN = 0");
      sbSQL.append(" inner join INATK.TOKSO_SHN SOS on SOS.MOYSKBN = SOB.MOYSKBN and SOS.MOYSSTDT = SOB.MOYSSTDT and SOS.MOYSRBAN = SOB.MOYSRBAN and SOS.BMNCD = SOB.BMNCD");
      sbSQL.append(" where COALESCE(SOS.UPDKBN, 0) <> 1");
      sbSQL.append(" and SOS.MOYSKBN = ?");
      sbSQL.append(" and SOS.MOYSSTDT = ?");
      sbSQL.append(" and SOS.MOYSRBAN = ?");
      sbSQL.append(" and SOS.BMNCD = ?");
      sbSQL.append(" and SOS.KANRINO = ?");

      array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
      option.put("NEWDATA", array);

      if (isNew) {
        option.put("KANRINO", selectKanriNo);
      }
    }

  }

  String kanriNoCsv = "";

  public List<JSONObject> checkData(HashMap<String, String> map, User userInfo, MessageUtility mu, JSONArray dataArray, // 対象情報（主要な更新情報）
      JSONArray dataArrayShn // 対象情報（追加更新情報）
  ) {
    JSONArray msg = new JSONArray();
    JSONObject data = new JSONObject();
    new ItemList();

    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<>();

    String errCd = "";

    // 1.生活応援_部門
    for (int i = 0; i < dataArray.size(); i++) {
      data = dataArray.optJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      // 必須入力チェック
      errCd = "001";


      TOKSO_BMNLayout[] targetCol = null;
      targetCol = new TOKSO_BMNLayout[] {TOKSO_BMNLayout.MOYSKBN, TOKSO_BMNLayout.MOYSSTDT, TOKSO_BMNLayout.MOYSRBAN, TOKSO_BMNLayout.BMNCD};
      for (TOKSO_BMNLayout colinf : targetCol) {

        String val = StringUtils.trim(data.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, val, colinf.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }


      // 基本データチェック:入力値がテーブル定義と矛盾してないか確認
      errCd = "002";
      for (TOKSO_BMNLayout colinf : TOKSO_BMNLayout.values()) {
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
            o = this.setCsvSoErrinfo(o, errCd, val, colinf.getText(), String.valueOf(i + 1), data);
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            o = this.setCsvSoErrinfo(o, errCd, val, colinf.getText(), String.valueOf(i + 1), data);
            msg.add(o);
            return msg;
          }
        }
      }

    }

    // 2.生活応援_商品
    for (int i = 0; i < dataArrayShn.size(); i++) {
      data = dataArrayShn.optJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      // 必須入力チェック
      errCd = "001";
      TOKSO_SHNLayout[] targetCol = null;
      targetCol = new TOKSO_SHNLayout[] {TOKSO_SHNLayout.MOYSKBN // 催し区分
          , TOKSO_SHNLayout.MOYSSTDT // 催し開始日
          , TOKSO_SHNLayout.MOYSRBAN // 催し連番
          , TOKSO_SHNLayout.BMNCD // 部門コード
          , TOKSO_SHNLayout.SHNCD // 商品コード
          , TOKSO_SHNLayout.SHNKN // 商品名称
          , TOKSO_SHNLayout.IRISU // 生活応援入数
          , TOKSO_SHNLayout.MINSU // 最低発注数
          , TOKSO_SHNLayout.GENKAAM // 生活応援原価
          , TOKSO_SHNLayout.A_BAIKAAM // A総売価
          , TOKSO_SHNLayout.A_RANKNO // Aランク
      };
      for (TOKSO_SHNLayout colinf : targetCol) {

        String val = StringUtils.trim(data.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, val, colinf.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
      for (TOKSO_SHNLayout colinf : TOKSO_SHNLayout.values()) {
        String val = StringUtils.trim(data.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            if (StringUtils.equals("POPSZ", colinf.getCol())) {
              // POPサイズ項目
              // DefineReportの設定を優先しない。
              dtype = colinf.getDataType();
              digit = colinf.getDigit();

            } else if (StringUtils.equals("MAKERKN", colinf.getCol())) {
              // メーカー名項目
              // DefineReportの設定を優先しない。
              dtype = DefineReport.DataType.ZEN;
              digit = colinf.getDigit();

            }
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {colinf.getText() + "は"});
            data.put(colinf.getId(), "");
            o = this.setCsvSoErrinfo(o, errCd, val, colinf.getText(), String.valueOf(i + 1), data);
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {colinf.getText() + "は"});
            data.put(colinf.getId(), "");
            o = this.setCsvSoErrinfo(o, errCd, val, colinf.getText(), String.valueOf(i + 1), data);
            msg.add(o);
            return msg;
          }
        }
      }

      // 重複チェック：商品コード
      errCd = "003";
      if (StringUtils.isEmpty(data.optString("F5"))) {
        // 管理番号が空の時
        paramData = new ArrayList<>();
        paramData.add(data.getString("F1"));
        paramData.add(data.getString("F2"));
        paramData.add(data.getString("F3"));
        paramData.add(data.getString("F4"));
        paramData.add(data.getString("F6"));
        sqlcommand = "select COUNT(SHNCD) as VALUE from INATK.TOKSO_SHN where COALESCE(UPDKBN, 0) <> 1 and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ? and SHNCD = ? ";

        @SuppressWarnings("static-access")
        JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
        if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
          JSONObject o = mu.getDbMessageObj("E00004", "商品コード");
          o = this.setCsvSoErrinfo(o, errCd, data.getString("F6"), TOKSO_SHNLayout.SHNCD.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }

        if (StringUtils.isEmpty(kanriNoCsv)) {
          kanriNoCsv = "" + this.getKANRINO(data.optString("F1"), data.optString("F2"), data.optString("F3"), data.optString("F4"));
          if (StringUtils.isEmpty(kanriNoCsv)) {
            kanriNoCsv = "1";
          } else {
            kanriNoCsv = "" + (Integer.parseInt(kanriNoCsv) + 1);
          }
        } else {
          kanriNoCsv = "" + (Integer.parseInt(kanriNoCsv) + 1);
        }

        if (!StringUtils.isEmpty(kanriNoCsv) && Integer.valueOf(kanriNoCsv) > 9999) {
          JSONObject o = mu.getDbMessageObj("E11234", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, kanriNoCsv, TOKSO_SHNLayout.KANRINO.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // 存在チェック：商品コード
      if (StringUtils.isNotEmpty(data.optString("F6"))) {
        paramData = new ArrayList<>();
        paramData.add(data.getString("F6"));
        sqlcommand = "select COUNT(SHNCD) as VALUE from INAMS.MSTSHN where COALESCE(UPDKBN, 0) <> 1 and SHNCD = ? ";

        JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
        if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
          JSONObject o = mu.getDbMessageObj("E11046", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, data.getString("F6"), TOKSO_SHNLayout.SHNCD.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // 存在チェック：部門コード
      if (StringUtils.isNotEmpty(data.optString("F4"))) {
        paramData = new ArrayList<>();
        paramData.add(data.getString("F4"));
        sqlcommand = "select COUNT(BMNCD) as VALUE from INAMS.MSTBMN where COALESCE(UPDKBN, 0) <> 1 and BMNCD = ? ";

        JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
        if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
          JSONObject o = mu.getDbMessageObj("E11097", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, data.getString("F4"), TOKSO_SHNLayout.SHNCD.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // 存在チェック：ランクNo
      ArrayList<String> rankNo = new ArrayList<>();
      rankNo.add(data.optString("F16"));
      rankNo.add(data.optString("F17"));
      rankNo.add(data.optString("F18"));

      for (int j = 0; j < rankNo.size(); j++) {
        String val = rankNo.get(j);
        if (StringUtils.isNotEmpty(val)) {

          String text = "";
          if (j == 0) {
            text = TOKSO_SHNLayout.A_RANKNO.getText();
          } else if (j == 1) {
            text = TOKSO_SHNLayout.B_RANKNO.getText();
          } else if (j == 2) {
            text = TOKSO_SHNLayout.C_RANKNO.getText();
          }

          String emsgId = "";
          if (Integer.parseInt(val) > 900) {
            emsgId = "E20466";
            paramData = new ArrayList<>();
            sqlcommand = "select COUNT(BMNCD) as VALUE from INATK.TOKRANKEX where COALESCE(UPDKBN, 0) <> 1 and BMNCD = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and RANKNO = ? ";
            paramData.add(data.getString("F4"));
            paramData.add(data.getString("F1"));
            paramData.add(data.getString("F2"));
            paramData.add(data.getString("F3"));
            paramData.add(val);

          } else {
            emsgId = "E20057";
            paramData = new ArrayList<>();
            sqlcommand = "select COUNT(BMNCD) as VALUE from INATK.TOKRANK where COALESCE(UPDKBN, 0) <> 1 and BMNCD = ? and RANKNO = ? ";
            paramData.add(data.getString("F4"));
            paramData.add(val);
          }



          @SuppressWarnings("static-access")
          JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
          if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
            JSONObject o = mu.getDbMessageObj(emsgId, new String[] {});
            o = this.setCsvSoErrinfo(o, errCd, val, text, String.valueOf(i + 1), data);
            msg.add(o);
            return msg;
          }
        }
      }

      // 相互チェック：部門コード
      if (StringUtils.isNotEmpty(data.optString("F6")) && data.optString("F6").length() > 2) {
        String bmncd = data.getString("F4");
        String bmncd_ = data.getString("F6").substring(0, 2);
        if (Integer.parseInt(bmncd) != Integer.parseInt(bmncd_)) {
          JSONObject o = mu.getDbMessageObj("E11162", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, data.getString("F6"), TOKSO_SHNLayout.SHNCD.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // 既存データチェック:商品
      if (StringUtils.isNotEmpty(data.optString("F24"))) {

        // 管理番号の設定がある場合(更新データ)
        paramData = new ArrayList<>();
        paramData.add(data.getString("F1"));
        paramData.add(data.getString("F2"));
        paramData.add(data.getString("F3"));
        paramData.add(data.getString("F4"));
        paramData.add(data.getString("F6"));
        sqlcommand = "select COUNT(SHNCD) as VALUE from INATK.TOKSO_SHN where COALESCE(UPDKBN, 0) <> 1 and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ? and SHNCD = ? ";

        @SuppressWarnings("static-access")
        JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

        if (StringUtils.equals("U", data.optString("F24"))) {
          // 更新時は既存データ無しの場合エラー
          if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
            JSONObject o = mu.getDbMessageObj("E20582", new String[] {});
            o = this.setCsvSoErrinfo(o, errCd, data.getString("F6"), TOKSO_SHNLayout.SHNCD.getText(), String.valueOf(i + 1), data);
            msg.add(o);
            return msg;
          }
        }
      }

      // 相互入力チェック
      String baika_A = data.getString("F13");
      String baika_B = data.getString("F14");
      String baika_C = data.getString("F15");
      data.getString("F16");
      String rank_B = data.getString("F17");
      String rank_C = data.getString("F18");
      String popcd = data.getString("F19");
      String popsize = data.getString("F20");
      String popsu = data.getString("F21");

      // B総売価
      if (StringUtils.isNotEmpty(baika_B)) {
        if (StringUtils.isEmpty(rank_B)) {
          JSONObject o = mu.getDbMessageObj("E20049", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, rank_B, TOKSO_SHNLayout.B_BAIKAAM.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // Bランク
      if (StringUtils.isNotEmpty(rank_B)) {
        if (StringUtils.isEmpty(baika_B)) {
          JSONObject o = mu.getDbMessageObj("E20049", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, baika_B, TOKSO_SHNLayout.B_RANKNO.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // C総売価
      if (StringUtils.isNotEmpty(baika_C)) {
        if (StringUtils.isEmpty(rank_C)) {
          JSONObject o = mu.getDbMessageObj("E20054", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, rank_C, TOKSO_SHNLayout.C_BAIKAAM.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      // Cランク
      if (StringUtils.isNotEmpty(rank_C)) {
        if (StringUtils.isEmpty(baika_C)) {
          JSONObject o = mu.getDbMessageObj("E20054", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, baika_C, TOKSO_SHNLayout.C_RANKNO.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }

        if (StringUtils.isEmpty(rank_B)) {
          JSONObject o = mu.getDbMessageObj("E20045", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, rank_B, TOKSO_SHNLayout.B_RANKNO.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }

      baika_A = StringUtils.isEmpty(baika_A) ? "0" : baika_A;
      baika_B = StringUtils.isEmpty(baika_B) ? "0" : baika_B;
      baika_C = StringUtils.isEmpty(baika_C) ? "0" : baika_C;

      if (Integer.parseInt(baika_A) < Integer.parseInt(baika_B)) {
        JSONObject o = mu.getDbMessageObj("E20052", new String[] {});
        o = this.setCsvSoErrinfo(o, errCd, baika_A, TOKSO_SHNLayout.A_BAIKAAM.getText(), String.valueOf(i + 1), data);
        msg.add(o);
        return msg;
      }

      if (Integer.parseInt(baika_B) < Integer.parseInt(baika_C)) {
        JSONObject o = mu.getDbMessageObj("E20052", new String[] {});
        o = this.setCsvSoErrinfo(o, errCd, baika_B, TOKSO_SHNLayout.B_BAIKAAM.getText(), String.valueOf(i + 1), data);
        msg.add(o);
        return msg;
      }

      if (Integer.parseInt(baika_A) == Integer.parseInt(baika_B) && Integer.parseInt(baika_B) == Integer.parseInt(baika_C)) {
        JSONObject o = mu.getDbMessageObj("E20053", new String[] {});
        o = this.setCsvSoErrinfo(o, errCd, baika_A, TOKSO_SHNLayout.A_BAIKAAM.getText(), String.valueOf(i + 1), data);
        msg.add(o);
        return msg;
      }

      // POP系項目
      if (StringUtils.isNotEmpty(popcd) || StringUtils.isNotEmpty(popsize)) {
        if (StringUtils.isEmpty(popsu) || Integer.parseInt(popsu) < 1) {
          JSONObject o = mu.getDbMessageObj("E20531", new String[] {});
          o = this.setCsvSoErrinfo(o, errCd, popsu, TOKSO_SHNLayout.POPSU.getText(), String.valueOf(i + 1), data);
          msg.add(o);
          return msg;
        }
      }


    }
    return msg;
  }

  public JSONObject setCsvSoErrinfo(JSONObject o, String errCd, String errvalue, String errfld, String inputno, JSONObject tokSo) {

    // SQLエラーとなるデータを取り除く
    tokSo = this.removeErrData(tokSo);

    String csvUpdkbn = DefineReport.ValCsvUpdkbn.NEW.getVal();
    String kanriNo = tokSo.optString(TOKSO_SHNLayout.KANRINO.getId());

    if (StringUtils.isNotEmpty(kanriNo)) {
      csvUpdkbn = DefineReport.ValCsvUpdkbn.UPD.getVal();
    }

    o.put(CSVTOK_SOLayout.ERRCD.getCol(), errCd);
    o.put(CSVTOK_SOLayout.ERRTBLNM.getCol(), o.optString(MessageUtility.MSG));
    o.put(CSVTOK_SOLayout.ERRFLD.getCol(), errfld);
    o.put(CSVTOK_SOLayout.ERRVL.getCol(), errvalue);
    o.put(CSVTOK_SOLayout.CSV_UPDKBN.getCol(), csvUpdkbn);
    o.put(CSVTOK_SOLayout.INPUTNO.getCol(), inputno);
    o.put(CSVTOK_SOLayout.MOYSKBN.getCol(), tokSo.optString(TOKSO_BMNLayout.MOYSKBN.getId()));
    o.put(CSVTOK_SOLayout.MOYSSTDT.getCol(), tokSo.optString(TOKSO_BMNLayout.MOYSSTDT.getId()));
    o.put(CSVTOK_SOLayout.MOYSRBAN.getCol(), tokSo.optString(TOKSO_BMNLayout.MOYSRBAN.getId()));
    o.put(CSVTOK_SOLayout.BMNCD.getCol(), tokSo.optString(TOKSO_BMNLayout.BMNCD.getId()));
    o.put(CSVTOK_SOLayout.SHNCD.getCol(), tokSo.optString(TOKSO_SHNLayout.SHNCD.getId()));
    o.put(CSVTOK_SOLayout.MAKERKN.getCol(), tokSo.optString(TOKSO_SHNLayout.MAKERKN.getId()));
    o.put(CSVTOK_SOLayout.SHNKN.getCol(), tokSo.optString(TOKSO_SHNLayout.SHNKN.getId()));
    o.put(CSVTOK_SOLayout.A_BAIKAAM.getCol(), tokSo.optString(TOKSO_SHNLayout.A_BAIKAAM.getId()));
    o.put(CSVTOK_SOLayout.B_BAIKAAM.getCol(), tokSo.optString(TOKSO_SHNLayout.B_BAIKAAM.getId()));
    o.put(CSVTOK_SOLayout.C_BAIKAAM.getCol(), tokSo.optString(TOKSO_SHNLayout.C_BAIKAAM.getId()));
    o.put(CSVTOK_SOLayout.A_RANKNO.getCol(), tokSo.optString(TOKSO_SHNLayout.A_RANKNO.getId()));
    o.put(CSVTOK_SOLayout.B_RANKNO.getCol(), tokSo.optString(TOKSO_SHNLayout.B_RANKNO.getId()));
    o.put(CSVTOK_SOLayout.C_RANKNO.getCol(), tokSo.optString(TOKSO_SHNLayout.C_RANKNO.getId()));
    o.put(CSVTOK_SOLayout.GENKAAM.getCol(), tokSo.optString(TOKSO_SHNLayout.GENKAAM.getId()));
    o.put(CSVTOK_SOLayout.IRISU.getCol(), tokSo.optString(TOKSO_SHNLayout.IRISU.getId()));
    o.put(CSVTOK_SOLayout.KIKKN.getCol(), tokSo.optString(TOKSO_SHNLayout.KIKKN.getId()));
    o.put(CSVTOK_SOLayout.MINSU.getCol(), tokSo.optString(TOKSO_SHNLayout.MINSU.getId()));
    o.put(CSVTOK_SOLayout.POPCD.getCol(), tokSo.optString(TOKSO_SHNLayout.POPCD.getId()));
    o.put(CSVTOK_SOLayout.POPSZ.getCol(), tokSo.optString(TOKSO_SHNLayout.POPSZ.getId()));
    o.put(CSVTOK_SOLayout.POPSU.getCol(), tokSo.optString(TOKSO_SHNLayout.POPSU.getId()));
    o.put(CSVTOK_SOLayout.TENATSUK_ARR.getCol(), tokSo.optString(TOKSO_SHNLayout.TENATSUK_ARR.getId()));


    return o;
  }

  // CSYエラー登録時に、レイアウトに対してエラーとなる項目を空に置き換える。
  public JSONObject removeErrData(JSONObject row) {
    JSONObject updateRow = new JSONObject();

    for (TOKSO_SHNLayout colinf : TOKSO_SHNLayout.values()) {
      String val = StringUtils.trim(row.optString(colinf.getId()));
      if (StringUtils.isNotEmpty(val)) {
        DataType dtype = null;
        int[] digit = null;
        try {
          DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
          dtype = inpsetting.getType();
          digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          if (StringUtils.equals("POPSZ", colinf.getCol())) {
            // DefineReportの設定を優先しない。
            dtype = colinf.getDataType();
            digit = colinf.getDigit();

          } else if (StringUtils.equals("MAKERKN", colinf.getCol())) {
            // DefineReportの設定を優先しない。
            dtype = DefineReport.DataType.ZEN;
            digit = colinf.getDigit();
          }
        } catch (IllegalArgumentException e) {
          dtype = colinf.getDataType();
          digit = colinf.getDigit();
        }
        // ①データ型による文字種チェック
        if (!InputChecker.checkDataType(dtype, val)) {
          val = "";
        }
        // ②データ桁チェック
        if (!InputChecker.checkDataLen(dtype, val, digit)) {
          val = "";
        }
      }
      updateRow.put(colinf.getId(), val);
    }
    return updateRow;
  }

  /**
   * 生活応援DELETE SQL作成処理
   *
   * @param userInfo
   * @param Sqlprm 入力No
   * @throws Exception
   */
  public JSONObject createSqlDelTOKSO(User userInfo, JSONObject data, boolean fullDelFlg) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    String userId = userInfo.getId(); // ログインユーザー
    String myoskbn = data.optString("F1");
    String myosstdt = data.optString("F2");
    String myosrbn = data.optString("F3");
    String bmncd = data.optString("F4");
    String kanriNo = data.optString("F5");

    StringBuffer sbSQL;

    if (fullDelFlg) {
      // 全削除ボタン押下時
      sbSQL = new StringBuffer();
      prmData.add(myoskbn);
      prmData.add(myosstdt);
      prmData.add(myosrbn);
      prmData.add(bmncd);

      sbSQL.append("UPDATE INATK.TOKSO_BMN");
      sbSQL.append(" SET ");
      sbSQL.append(" UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());

      sbSQL.append(",SENDFLG = 0 ");

      sbSQL.append(",OPERATOR = '" + userId + "'");
      sbSQL.append(",UPDDT= CURRENT_TIMESTAMP ");
      sbSQL.append(" WHERE MOYSKBN = ?");
      sbSQL.append(" and MOYSSTDT = ?");
      sbSQL.append(" and MOYSRBAN = ?");
      sbSQL.append(" and BMNCD = ?");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("生活応援_部門");

      // 子要素の削除
      sbSQL = new StringBuffer();
      sbSQL.append("UPDATE INATK.TOKSO_SHN");
      sbSQL.append(" SET ");
      sbSQL.append(" UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());

      sbSQL.append(",SENDFLG = 0 ");

      sbSQL.append(",OPERATOR = '" + userId + "'");
      sbSQL.append(",UPDDT = CURRENT_TIMESTAMP ");
      sbSQL.append(" WHERE MOYSKBN = ?");
      sbSQL.append(" and  MOYSSTDT = ?");
      sbSQL.append(" and MOYSRBAN = ?");
      sbSQL.append(" and BMNCD = ?");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("生活応援_商品");

    } else {

      // 削除チェックによる商品データ削除
      prmData = new ArrayList<>();
      prmData.add(myoskbn);
      prmData.add(myosstdt);
      prmData.add(myosrbn);
      prmData.add(bmncd);
      prmData.add(kanriNo);

      sbSQL = new StringBuffer();
      sbSQL.append("UPDATE INATK.TOKSO_SHN");
      sbSQL.append(" SET ");
      sbSQL.append(" UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(",OPERATOR = '" + userId + "'");
      sbSQL.append(",UPDDT = CURRENT_TIMESTAMP ");
      sbSQL.append(" WHERE MOYSKBN = ?");
      sbSQL.append(" and  MOYSSTDT = ?");
      sbSQL.append(" and MOYSRBAN = ?");
      sbSQL.append(" and BMNCD = ?");
      sbSQL.append(" and KANRINO = ?");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("生活応援_商品");
    }

    return result;
  }

  /**
   * 生活応援DELETE SQL作成処理
   *
   * @param userInfo
   * @param Sqlprm 入力No
   * @throws Exception
   */
  public JSONObject createSqlDelCSVSO(User userInfo, JSONObject data) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    String userId = userInfo.getId(); // ログインユーザー
    String seg = data.optString("F1");
    String inputNo = data.optString("F2");

    StringBuffer sbSQL;

    sbSQL = new StringBuffer();
    prmData.add(seg);
    prmData.add(inputNo);

    sbSQL.append("UPDATE INATK.CSVTOK_SO");
    sbSQL.append(" SET ");
    sbSQL.append(" UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(",OPERATOR = '" + userId + "'");
    sbSQL.append(",UPDDT = CURRENT_TIMESTAMP ");
    sbSQL.append(" WHERE SEQ = ?");
    sbSQL.append(" and INPUTNO = ?");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("CSV取込トラン_生活応援");


    return result;
  }

  public String createCommandFTP(String userId, JSONObject obj, String outpage) {
    obj.optString("BTN");

    obj.optString("MOYSSTDT");
    obj.optString("BMNCD");
    obj.optInt("REQLEN");

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("with RECURSIVE WK as (");
    sbSQL.append("select ");
    sbSQL.append(" cast(? as " + MSTSHNLayout.BMNCD.getTyp() + ") as " + MSTSHNLayout.BMNCD.getCol());
    for (TOKMOYCDCMNLayout itm : TOKMOYCDCMNLayout.values()) {
      sbSQL.append(",cast(? as " + itm.getTyp() + ") as " + itm.getCol());
    }
    sbSQL.append(" from  (SELECT 1 AS DUMMY) DUMMY");
    sbSQL.append(")");
    sbSQL.append("select");
    sbSQL.append(" REC");
    sbSQL.append(" from (");
    sbSQL.append("select distinct");
    sbSQL.append(" 1 as RNO");
    sbSQL.append(", T1.MOYSKBN || LPAD(T1.MOYSSTDT, 6, '0') || LPAD(T1.MOYSRBAN, 3, '0') as MOYCD");
    sbSQL.append(", T2.BMNCD");
    sbSQL.append(", null as KANRINO");
    sbSQL.append(", RPAD('D1'");
    sbSQL.append(" || T1.MOYSKBN");
    sbSQL.append(" || LPAD(T1.MOYSSTDT, 6, '0')");
    sbSQL.append(" || LPAD(T1.MOYSRBAN, 3, '0')");
    sbSQL.append(" || left (RPAD(COALESCE(T1.MOYKN, ''), 40, '　'), 40)");
    sbSQL.append(" || LPAD(COALESCEL(T1.MOYSSTDT, ''), 8, '0')");
    sbSQL.append(" || LPAD(COALESCE(T1.HBEDDT, ''), 8, '0'), 241, ' ') as REC");
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(
        " inner join INATK.TOKSO_BMN T2 on COALESCE(T1.UPDKBN, 0) <> 1 and COALESCE(T2.UPDKBN, 0) <> 1 and T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
    sbSQL.append(" inner join WK on T2.MOYSKBN = WK.MOYSKBN and T2.MOYSSTDT = WK.MOYSSTDT and T2.MOYSRBAN = WK.MOYSRBAN and T2.BMNCD = WK.BMNCD");

    if (DefineReport.ID_PAGE_SO003.equals(outpage)) {

      sbSQL.append(" union all");
      sbSQL.append(" select distinct");
      sbSQL.append(" 2 as RNO");
      sbSQL.append(", T1.MOYSKBN || LPAD(T1.MOYSSTDT, 6, '0') || LPAD(T1.MOYSRBAN, 3, '0') as MOYCD");
      sbSQL.append(", T2.BMNCD");
      sbSQL.append(", T3.KANRINO");
      sbSQL.append(", RPAD('D2'");
      sbSQL.append(" || T1.MOYSKBN");
      sbSQL.append(" || LPAD(T1.MOYSSTDT, 6, '0')");
      sbSQL.append(" || LPAD(T1.MOYSRBAN, 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T2.BMNCD, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.KANRINO, ''), 4, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.SHNCD, ''), 14, '0')");
      sbSQL.append(" || left (RPAD(COALESCE(T3.MAKERKN, ''), 14, '　'), 14)");
      sbSQL.append(" || left (RPAD(COALESCE(T3.SHNKN, ''), 40, '　'), 40)");
      sbSQL.append(" || left (RPAD(COALESCE(T3.KIKKN, ''), 46, '　'), 46)");
      sbSQL.append(" || LPAD(COALESCE(M1.DAICD, ''), 2, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.CHUCD, ''), 2, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.SHOCD, ''), 2, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.RG_IRISU, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.IRISU, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.MINSU, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.RG_GENKAAM, ''), 11, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.GENKAAM, ''), 11, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.RG_BAIKAAM, ''), 6, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.A_BAIKAAM, ''), 6, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.B_BAIKAAM, ''), 6, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.C_BAIKAAM, ''), 6, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.A_RANKNO, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.B_RANKNO, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.C_RANKNO, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.POPCD, ''), 10, '0')");
      sbSQL.append(" || LPAD(COALESCE(T3.POPSU, ''), 2, '0')");
      sbSQL.append(" || left (RPAD(COALESCE(T3.POPSZ, ''), 3, '　'), 3)");
      sbSQL.append(" || left (RPAD(COALESCE(M1.ZEIKBN, ''), 1, '　'), 1)");
      sbSQL.append(" || LPAD(COALESCE(M1.ZEIRTKBN, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.ZEIRTKBN_OLD, ''), 3, '0')");
      sbSQL.append(" || LPAD(COALESCE(M1.ZEIRTHENKODT, ''), 8, '0'), 241, ' ') as REC");
      sbSQL.append(" from INATK.TOKMOYCD T1");
      sbSQL.append(
          " inner join INATK.TOKSO_BMN T2 on COALESCE(T1.UPDKBN, 0) <> 1 and COALESCE(T2.UPDKBN, 0) <> 1 and T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
      sbSQL.append(" inner join WK on T2.MOYSKBN = WK.MOYSKBN and T2.MOYSSTDT = WK.MOYSSTDT and T2.MOYSRBAN = WK.MOYSRBAN and T2.BMNCD = WK.BMNCD");
      sbSQL.append(" inner join INATK.TOKSO_SHN T3 on T2.MOYSKBN = T3.MOYSKBN and T2.MOYSSTDT = T3.MOYSSTDT and T2.MOYSRBAN = T3.MOYSRBAN and T2.BMNCD = T3.BMNCD");
      sbSQL.append(" inner join INAMS.MSTSHN M1 on M1.SHNCD = T3.SHNCD and COALESCE(M1.UPDKBN, 0) <> 1");
    }
    sbSQL.append(") order by RNO, MOYCD, BMNCD, KANRINO");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }
}
