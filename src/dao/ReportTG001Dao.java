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
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.DefineReport.ValKbn10002;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportTM002Dao.TOKMOYCDCMNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTG001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG001Dao(String JNDIname) {
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
   * 検索実行
   *
   * @return
   */
  @Override
  public boolean selectForDL() {
    // 検索コマンド生成（基本情報取得）
    String command = createCommandForDl();

    return super.selectBySQL(command, false);
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
      option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E30007", new String[] {}));
    }
    return option;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }
    ArrayList<String> paramData = new ArrayList<String>();

    String szStym = getMap().get("STYM"); // 表示年月From
    String szEnym = getMap().get("ENYM"); // 表示年月To

    // パラメータ確認
    /*
     * // 必須チェック if ((btnId == null)) { System.out.println(super.getConditionLog()); return ""; }
     */

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    // 3.1.2.1．催しコードテーブル：
    // 催しコード.販売開始日 >= 【画面】.「表示年月From」 AND
    // 催しコード.販売開始日 <= 【画面】.「表示年月To」 AND
    // 催しコード.催し区分 = 1(全店特売) AND
    // 催しコード.催し連番 >= 50
    // ソート順：催しコード（催し区分、催し開始日、催し連番）の昇順
    sbSQL.append(",WKCD as ( ");
    sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.MOYKN, T1.HBSTDT, T1.HBEDDT ");
    sbSQL.append("  from INATK.TOKMOYCD T1");
    sbSQL.append("  where T1.UPDKBN = 0");
    if (!StringUtils.isEmpty(szStym)) {
      sbSQL.append("  and T1.HBSTDT >= ? ");
      paramData.add(szStym + "00");
    }
    if (!StringUtils.isEmpty(szEnym)) {
      sbSQL.append("  and T1.HBSTDT <= ? ");
      paramData.add(szEnym + "99");
    }
    sbSQL.append("  and T1.MOYSKBN = " + ValKbn10002.VAL1.getVal() + "");
    sbSQL.append("  and T1.MOYSRBAN >= 50");
    sbSQL.append(") ");
    sbSQL.append(" select");
    sbSQL.append("    COALESCE(T3.CNT, 0) as F1");
    sbSQL.append("  , case when T4.CNT > 0 then 1 else 0 end as F2");
    sbSQL.append("  , T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F3");
    sbSQL.append("  , T1.MOYKN as F4");
    sbSQL.append("  , T2.HBOKUREFLG as F5");
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as F6");
    sbSQL.append("  , RIGHT(T2.GTSIMEDT, 6) as F7");
    sbSQL.append("  , (select JWEEK2 from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.GTSIMEDT, '%Y%m%d'))) as F8 ");
    sbSQL.append("  , T2.GTSIMEFLG as F9");
    sbSQL.append("  , RIGHT(T2.LSIMEDT, 6) as F10");
    sbSQL.append("  , (select JWEEK2 from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.LSIMEDT, '%Y%m%d'))) as F11");
    sbSQL.append("  , RIGHT(T2.QAYYYYMM, 4)  as F12");
    sbSQL.append("  , COALESCE(T2.QAENO, 0)  as F13");
    sbSQL.append("  , RIGHT(T2.QACREDT, 6)  as F14");
    sbSQL.append("  , (select JWEEK2 from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QACREDT, '%Y%m%d'))) as F15");
    sbSQL.append("  , CASE WHEN T2.QARCREDT = '0' THEN null ELSE RIGHT(T2.QARCREDT, 6) END as F16");
    sbSQL.append("  , CASE WHEN T2.QARCREDT = '0' THEN null ELSE (select JWEEK2 from WEEK where T2.QARCREDT <> 0 and CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QARCREDT, '%Y%m%d'))) END as F17");
    sbSQL.append("  , RIGHT(T2.QADEVSTDT, 6)  as F18");
    sbSQL.append("  , (select JWEEK2 from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QADEVSTDT, '%Y%m%d'))) as F19");
    sbSQL.append("  , T2.JLSTCREFLG as F20");
    sbSQL.append("  , T2.HNCTLFLG as F21");
    sbSQL.append("  , T2.TPNG1FLG as F22");
    sbSQL.append("  , T2.TPNG2FLG as F23");
    sbSQL.append("  , T2.TPNG3FLG as F24");
    sbSQL.append("  , COALESCE(T5.CNT, 0) as F25");
    sbSQL.append("  , T1.MOYSKBN as F26");
    sbSQL.append("  , T1.MOYSSTDT as F27");
    sbSQL.append("  , T1.MOYSRBAN as F28");
    sbSQL.append("  , T1.HBSTDT as F29");
    sbSQL.append("  , T1.HBEDDT as F30");
    sbSQL.append("  , DATE_FORMAT(T2.UPDDT, '%Y%m%d%H%i%s%f') as F31");
    sbSQL.append("  , T2.UPDKBN as F32");
    sbSQL.append("  , 0 as F33"); // CHNAGE_IDX
    sbSQL.append(" from");
    sbSQL.append("  WKCD T1 ");
    sbSQL.append("  left outer join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");
    sbSQL.append("  left outer join ( ");
    sbSQL.append("    select");
    sbSQL.append("     MOYSKBN, MOYSSTDT, MOYSRBAN");
    sbSQL.append("    ,SUM(case when KANRIENO = KANRIENO_MAX then 1 end) as CNT ");
    sbSQL.append("    from");
    sbSQL.append("      ( ");
    sbSQL.append("        select");
    sbSQL.append("            MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO");
    sbSQL.append("          , MAX(KANRIENO) over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO) as KANRIENO_MAX"); // 枝番Max確保
    sbSQL.append("        from INATK.TOKTG_SHN T3");
    sbSQL.append("        where");
    sbSQL.append("          exists (select 'X' from WKCD T1 where T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN)");
    sbSQL.append("          and COALESCE(GTSIMECHGKBN, 0) <> 0 "); // 月締変更理由<>0
    sbSQL.append("          and COALESCE(GTSIMEOKFLG, 0) <> 1 "); // 月締変更許可フラグ<>1:許可
    sbSQL.append("          and UPDKBN = 0");
    sbSQL.append("      ) T ");
    sbSQL.append("    group by MOYSKBN, MOYSSTDT, MOYSRBAN ");
    sbSQL.append("  ) T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
    sbSQL.append("  left outer join ( ");
    sbSQL.append("    select MOYSKBN, MOYSSTDT, MOYSRBAN, COUNT(T4.TENGPCD) as CNT ");
    sbSQL.append("    from INATK.TOKTG_TENGP T4");
    sbSQL.append("    where");
    sbSQL.append("      exists (select 'X' from WKCD T1 where T1.MOYSKBN = T4.MOYSKBN and T1.MOYSSTDT = T4.MOYSSTDT and T1.MOYSRBAN = T4.MOYSRBAN) ");
    sbSQL.append("      and UPDKBN = 0 ");
    sbSQL.append("    group by MOYSKBN, MOYSSTDT, MOYSRBAN");
    sbSQL.append("  ) T4 on T1.MOYSKBN = T4.MOYSKBN and T1.MOYSSTDT = T4.MOYSSTDT and T1.MOYSRBAN = T4.MOYSRBAN");
    sbSQL.append("  left outer join ( ");
    sbSQL.append("    select MOYSKBN, MOYSSTDT, MOYSRBAN");
    sbSQL.append("      ,SUM(case when KANRIENO = KANRIENO_MAX then 1 end) as CNT ");
    sbSQL.append("    from");
    sbSQL.append("      ( select");
    sbSQL.append("           MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO");
    sbSQL.append("          ,MAX(KANRIENO) over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO) as KANRIENO_MAX");
    sbSQL.append("        from INATK.TOKTG_SHN T3 ");
    sbSQL.append("        where");
    sbSQL.append("          exists (select 'X' from WKCD T1 where T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN)");
    sbSQL.append("          and UPDKBN = 0");
    sbSQL.append("      ) T ");
    sbSQL.append("    group by MOYSKBN, MOYSSTDT, MOYSRBAN");
    sbSQL.append("  ) T5 on T1.MOYSKBN = T5.MOYSKBN and T1.MOYSSTDT = T5.MOYSSTDT and T1.MOYSRBAN = T5.MOYSRBAN");
    sbSQL.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
    setParamData(paramData);

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  private String createCommandForDl() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String btnId = getMap().get("BTN"); // 実行ボタン
    JSONArray dataArray = JSONArray.fromObject(getMap().get("DATA")); // 選択情報

    // パラメータ確認
    // 必須チェック
    if ((btnId == null) || (dataArray.size() == 0)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    CsvFileInfo cfi = null;
    if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj() + "1")) {
      cfi = CsvFileInfo.CSV1;
    } else if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj() + "2")) {
      cfi = CsvFileInfo.CSV2;
    } else if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj() + "3")) {
      cfi = CsvFileInfo.CSV3;
    } else if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj() + "4")) {
      cfi = CsvFileInfo.CSV4;
    }

    String values = "", names = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (TOKMOYCDCMNLayout targetCol : TOKMOYCDCMNLayout.values()) {
        values += ", '" + dataArray.optJSONObject(j).optString(targetCol.getId()) + "'";
        names += ", " + targetCol.getCol();
      }
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("with WK as (");
    sbSQL.append(" select " + names);
    sbSQL.append(" from (values" + rows + ") as T1(" + names + ")");
    sbSQL.append(")");
    sbSQL.append("select REC from (");
    sbSQL.append(" " + super.createCmnSqlFTPH1(cfi.geFnm(), userInfo.getId(), cfi.getLen(), true));
    sbSQL.append(" union all ");
    if (cfi.equals(CsvFileInfo.CSV1) || cfi.equals(CsvFileInfo.CSV2) || cfi.equals(CsvFileInfo.CSV3)) {
      // sbSQL.append(" select distinct 2 as RNO, rpad('D1'||T1.MOYSKBN||lpad(T1.MOYSSTDT,
      // 6,'0')||lpad(T1.MOYSRBAN,3, '0'), "+cfi.getDataLen()+", ' ') as REC ");
      // sbSQL.append(" from INATK.TOKMOYCD T1");
      // sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN= WK.MOYSKBN and T1.MOYSSTDT =
      // WK.MOYSSTDT and T1.MOYSRBAN= WK.MOYSRBAN");

      sbSQL.append(" select distinct 1 as RNO, rpad('D1'||T1.MOYSKBN||lpad(T1.MOYSSTDT, 6,'0')||lpad(T1.MOYSRBAN,3, '0'), " + cfi.getDataLen() + ", ' ') as REC ");
      sbSQL.append(" from INATK.TOKTG_SHN T1");
      sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
      sbSQL.append(" union all ");
      sbSQL.append(" select distinct 1 as RNO, rpad('D1'||T1.MOYSKBN||lpad(T1.MOYSSTDT, 6,'0')||lpad(T1.MOYSRBAN,3, '0'), " + cfi.getDataLen() + ", ' ') as REC ");
      sbSQL.append(" from INATK.TOKTG_TEN T1");
      sbSQL.append(" inner join WK on T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");

    } else if (cfi.equals(CsvFileInfo.CSV4)) {
      sbSQL.append(" select distinct 1 as RNO");
      sbSQL.append(" ,rpad('D1'");
      sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
      sbSQL.append(" ||lpad(COALESCE(CAST(T2.HBSTDT AS CHAR),''),8,'0')"); // 販売開始日 YYYYMMDDで設定 NULLの時ALL0/桁数に満たない時-
      sbSQL.append(" ||lpad(COALESCE(CAST(T2.HBEDDT AS CHAR),''),8,'0')"); // 販売終了日 YYYYMMDDで設定 NULLの時ALL0/桁数に満たない時-
      sbSQL.append(" ||left(rpad(COALESCE(T2.MOYKN,''), 40, '　'), 40)");
      sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
      sbSQL.append(" from INATK.TOKTG_SHN T1");
      sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
      sbSQL.append(" inner join INATK.TOKMOYCD T2 on T2.UPDKBN = 0 and T2.MOYSKBN = WK.MOYSKBN and T2.MOYSSTDT = WK.MOYSSTDT and T2.MOYSRBAN = WK.MOYSRBAN");
      sbSQL.append(" union all ");
      sbSQL.append(" select distinct 2 as RNO");
      sbSQL.append(" ,rpad('D2'");
      sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
      sbSQL.append(" ||lpad(T1.BMNCD,3,'0')"); // 部門 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(T1.KANRINO,4,'0')"); // 管理番号 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||left(rpad(COALESCE(CAST(T1.HIGAWRFLG AS CHAR),''),1,' '),1)"); // 日替フラグ 0：通し 1：日替 NULLの時ALL半SP/桁数に満たない時-
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTDT AS CHAR),''),8,'0')"); // 販売開始日 YYYYMMDDで設定 NULLの時ALL0/桁数に満たない時-
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBEDDT AS CHAR),''),8,'0')"); // 販売終了日 YYYYMMDDで設定 NULLの時ALL0/桁数に満たない時-
      sbSQL.append(" ||left(rpad(COALESCE(T1.SHNCD,''),14,' '),14)"); // 商品コード NULLの時ALL半SP/桁数に満たない時半SPACE
      sbSQL.append(" ||left(rpad(COALESCE(T1.SANCHIKN,''),40,'　'),40)"); // 産地 NULLの時ALL全SP/桁数に満たない時全SPACE
      sbSQL.append(" ||left(rpad(COALESCE(T1.MAKERKN,''),28,'　'),28)"); // メーカー名 NULLの時ALL全SP/桁数に満たない時全SPACE
      sbSQL.append(" ||left(rpad(COALESCE(T1.POPKN,''),40,'　'),40)"); // POP名称 NULLの時ALL全SP/桁数に満たない時全SPACE
      sbSQL.append(" ||left(rpad(COALESCE(T1.KIKKN,''),46,'　'),46)"); // 規格名称 NULLの時ALL全SP/桁数に満たない時全SPACE
      sbSQL.append(" ||left(rpad(COALESCE(CAST(T1.NAMANETUKBN AS CHAR),''),1,' '),1)"); // 生食加熱区分 1：生食 2：加熱 NULLの時ALL半SP/桁数に満たない時-
      sbSQL.append(" ||left(rpad(COALESCE(CAST(T1.KAITOFLG AS CHAR),''),1,' '),1)"); // 解凍フラグ 0：なし 1：解凍 NULLの時ALL半SP/桁数に満たない時-
      sbSQL.append(" ||left(rpad(COALESCE(CAST(T1.YOSHOKUFLG AS CHAR),''),1,' '),1)"); // 養殖フラグ 0：なし 1：養殖 NULLの時ALL半SP/桁数に満たない時-
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_MAE AS CHAR),''),8,'0')"); // 事前原価 99999999で設定不定貫のときは1㎏原価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_ATO AS CHAR),''),8,'0')"); // 追加原価 99999999で設定不定貫のときは1㎏原価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.IRISU AS CHAR),''),3,'0')"); // 入数 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.TKANPLUKBN AS CHAR),''),1,'0')"); // 定貫PLU・不定貫区分 1：定貫・PLU 2：不定貫 （鮮魚、精肉以外は0をセット） NULLの時ALL0/桁数に満たない時-
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM AS CHAR),''),6,'0')"); // A総額売価 定貫：999999 不定貫：999999 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM AS CHAR),''),6,'0')"); // B総額売価 定貫：999999 不定貫：999999 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM AS CHAR),''),6,'0')"); // C総額売価 定貫：999999 不定貫：999999 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||left(rpad(COALESCE(CAST(T1.YORIFLG AS CHAR),''),1,' '),1)"); // よりどりフラグ 0：無 1：有 NULLの時ALL半SP/桁数に満たない時-
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_TENSU AS CHAR),''),3,'0')"); // バンドル1_点数 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_A_BAIKAAN AS CHAR),''),6,'0')"); // バンドル1_A総額売価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_B_BAIKAAN AS CHAR),''),6,'0')"); // バンドル1_B総額売価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_C_BAIKAAN AS CHAR),''),6,'0')"); // バンドル1_C総額売価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_TENSU AS CHAR),''),3,'0')"); // バンドル2_点数 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_A_BAIKAAN AS CHAR),''),6,'0')"); // バンドル2_A総額売価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_B_BAIKAAN AS CHAR),''),6,'0')"); // バンドル2_B総額売価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_C_BAIKAAN AS CHAR),''),6,'0')"); // バンドル2_C総額売価 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.MEDAMAKBN AS CHAR),''),1,'0')"); // 目玉区分 1：大目玉 2：目玉 3：他 NULLの時ALL0/桁数に満たない時前0
      sbSQL.append(" ||left(rpad(COALESCE('先着'||lpad(CAST(T1.SEGN_NINZU AS CHAR),5,'　')||'名様'||'　', '')||COALESCE('お一人様'||lpad(CAST(T1.SEGN_GENTEI AS CHAR),3,'　')||'点限り', ''),58,'　'),58)"); // 制限
                                                                                                                                                                                              // 先着ＺＺＺＺ９名様
      // お一人様ＺＺ９点限り
      sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_TB,''),60,'　'),60)"); // 特売コメント
      sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_POP,''),100,'　'),100)"); // POPコメント
      sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_HGW,''),100,'　'),100)"); // その他日替コメント
      sbSQL.append(" ||lpad(T2.DAICD,2,'0')"); // 大分類 前0埋め
      sbSQL.append(" ||lpad(T2.CHUCD,2,'0')"); // 中分類 前0埋め
      sbSQL.append(" ||lpad(T2.SHOCD,2,'0')"); // 小分類 前0埋め
      sbSQL.append(" ||left(rpad(COALESCE(CAST(T2.ZEIKBN AS CHAR),''),1,' '),1)"); // 税区分 0：外税 1：内税 2：非課税 3：部門に準拠
      sbSQL.append(" ||lpad(COALESCE(CAST(T2.ZEIRTKBN AS CHAR),''),3,'0')"); // 税率区分
      sbSQL.append(" ||lpad(COALESCE(CAST(T2.ZEIRTKBN_OLD AS CHAR),''),3,'0')"); // 旧税率区分
      sbSQL.append(" ||lpad(COALESCE(CAST(T2.ZEIRTHENKODT AS CHAR),''),8,'0')"); // 税率変更日 YYYYMMDDで設定
      sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
      sbSQL.append(" from INATK.TOKTG_SHN T1");
      sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
      sbSQL.append(" inner join INAMS.MSTSHN T2 on T1.SHNCD = T2.SHNCD and COALESCE(T2.UPDKBN, 0) = 0");
    }
    sbSQL.append(") order by RNO, REC");


    // オプション情報設定
    JSONObject option = new JSONObject();
    option.put("FILE_NAME", cfi.geFnm());
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
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 催し基本情報

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

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    this.createSqlTOKTG_KHN(userId, data);

    // 最新情報取得
    String sqlNewWhere = " and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?";
    ArrayList<String> sqlNewParam = new ArrayList<String>();
    sqlNewParam.add(data.optString(TOKTG_KHNLayout.MOYSKBN.getId()));
    sqlNewParam.add(data.optString(TOKTG_KHNLayout.MOYSSTDT.getId()));
    sqlNewParam.add(data.optString(TOKTG_KHNLayout.MOYSRBAN.getId()));
    JSONArray newArray = this.getJSONArrayTOKTG_KHN(sqlNewWhere, sqlNewParam);
    if (newArray.size() == 0 || !StringUtils.equals(data.optString(TOKTG_KHNLayout.QARCREDT.getId()), newArray.optJSONObject(0).optString(TOKTG_KHNLayout.QARCREDT.getCol()))) {
      this.createSqlTOKTG_KHN_RE(userId, data);
    }

    // 排他チェック実行
    String targetTable = "INATK.TOKTG_KHN";
    String targetWhere = " MOYSKBN= ? and MOYSSTDT= ? and MOYSRBAN= ? and UPDKBN = 0";
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(data.optString(TOKTG_KHNLayout.MOYSKBN.getId()));
    targetParam.add(data.optString(TOKTG_KHNLayout.MOYSSTDT.getId()));
    targetParam.add(data.optString(TOKTG_KHNLayout.MOYSRBAN.getId()));
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString(TOKTG_KHNLayout.UPDDT.getId()))) {
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
        // 更新情報取得
        String sqlUpdWhere = " and QAYYYYMM = ? and QAENO = ?";
        ArrayList<String> sqlUpdParam = new ArrayList<String>();
        sqlUpdParam.add(data.optString(TOKTG_KHNLayout.QAYYYYMM.getId()));
        sqlUpdParam.add(data.optString(TOKTG_KHNLayout.QAENO.getId()));
        JSONArray updArray = this.getJSONArrayTOKTG_KHN(sqlUpdWhere, sqlUpdParam);
        option.put("rows_upd", updArray);
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
    map.get("SHUNO");
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = new JSONArray();// JSONArray.fromObject(map.get("DATA")); // 催し基本情報


    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

    MessageUtility mu = new MessageUtility();

    List<JSONObject> msgList = this.checkData(isNew, isChange, map, userInfo, sysdate, mu, dataArray);


    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public List<JSONObject> checkData(boolean isNew, boolean isChange, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 催し基本情報
  ) {

    JSONArray msg = new JSONArray();


    dataArray.optJSONObject(0);


    CmnDate.dbDateFormat(sysdate1);



    String login_dt = sysdate1; // 処理日付
    login_dt.substring(2, 6);

    return msg;
  }

  /**
   * 催し基本情報INSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKTG_KHN(String userId, JSONObject data) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String[] notTarget = new String[] {TOKTG_KHNLayout.UPDKBN.getId(), TOKTG_KHNLayout.SENDFLG.getId(), TOKTG_KHNLayout.OPERATOR.getId(), TOKTG_KHNLayout.ADDDT.getId(), TOKTG_KHNLayout.UPDDT.getId()};

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKTG_KHN ( ");
    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,HBOKUREFLG"); // F4 : 販売日1日遅許可フラグ
    sbSQL.append(" ,GTSIMEDT"); // F5 : 月締日
    sbSQL.append(" ,GTSIMEFLG"); // F6 : 月締フラグ
    sbSQL.append(" ,LSIMEDT"); // F7 : 最終締日
    sbSQL.append(" ,QAYYYYMM"); // F8 : アンケート月度
    sbSQL.append(" ,QAENO"); // F9 : アンケート月度枝番
    sbSQL.append(" ,QACREDT"); // F10: アンケート作成日
    sbSQL.append(" ,QARCREDT"); // F11: アンケート再作成日
    sbSQL.append(" ,JLSTCREFLG"); // F12: 事前発注リスト作成済フラグ
    sbSQL.append(" ,HNCTLFLG"); // F13: 本部コントロールフラグ
    sbSQL.append(" ,TPNG1FLG"); // F14: 店不採用禁止フラグ
    sbSQL.append(" ,TPNG2FLG"); // F15: 店売価選択禁止フラグ
    sbSQL.append(" ,TPNG3FLG"); // F16: 店商品選択禁止フラグ
    sbSQL.append(" ,SIMEFLG1_LD"); // F17: 仮締フラグ_リーダー店
    sbSQL.append(" ,SIMEFLG2_LD"); // F18: 本締フラグ_リーダー店
    sbSQL.append(" ,SIMEFLG_MB"); // F19: 本締フラグ_各店
    sbSQL.append(" ,QADEVSTDT"); // F20: アンケート取込開始日
    sbSQL.append(" ,UPDKBN"); // F21: 更新区分
    sbSQL.append(" ,SENDFLG"); // F22: 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F23: オペレータ
    sbSQL.append(" ,ADDDT"); // F24: 登録日
    sbSQL.append(" ,UPDDT"); // F25: 更新日
    sbSQL.append(")values(");
    for (TOKTG_KHNLayout itm : TOKTG_KHNLayout.values()) {
      String TYP;
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      if (data.containsKey(itm.getId())) {
        String value = StringUtils.strip(data.optString(itm.getId()));
        if (StringUtils.equals(itm.getId(), TOKTG_KHNLayout.QADEVSTDT.getCol())) {
          value = StringUtils.defaultIfEmpty(value, "0");
        }
        if (itm.getTyp() == "SMALLINT" || itm.getTyp() == "INTEGER") {
          TYP = "SIGNED";
        } else if (itm.getTyp() == "TIMESTAMP") {
          TYP = "DATETIME";
        } else {
          TYP = "CHAR(20)";
        }
        if (StringUtils.isNotEmpty(value)) {
          prmData.add(value);
          sbSQL.append("cast(? as " + TYP + ") ");
        } else {
          sbSQL.append("cast(null as " + TYP + ") ");
        }
      } else {
        sbSQL.append("null ");
      }
    }
    sbSQL.append(" ," + DefineReport.ValUpdkbn.NML.getVal()); // F21: 更新区分
    sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal()); // F22: 送信フラグ
    sbSQL.append(" ,'" + userId + "'"); // F23: オペレータ
    sbSQL.append(" ,current_timestamp"); // F24: 登録日
    sbSQL.append(" ,current_timestamp"); // F25: 更新日
    sbSQL.append(" )");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("  MOYSKBN=VALUES(MOYSKBN) "); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT=VALUES(MOYSSTDT) "); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN=VALUES(MOYSRBAN) "); // F3 : 催し連番
    sbSQL.append(" ,HBOKUREFLG=VALUES(HBOKUREFLG) "); // F4 : 販売日1日遅許可フラグ
    sbSQL.append(" ,GTSIMEDT=VALUES(GTSIMEDT) "); // F5 : 月締日
    sbSQL.append(" ,GTSIMEFLG=VALUES(GTSIMEFLG) "); // F6 : 月締フラグ
    sbSQL.append(" ,LSIMEDT=VALUES(LSIMEDT) "); // F7 : 最終締日
    sbSQL.append(" ,QAYYYYMM=VALUES(QAYYYYMM) "); // F8 : アンケート月度
    sbSQL.append(" ,QAENO=VALUES(QAENO) "); // F9 : アンケート月度枝番
    sbSQL.append(" ,QACREDT=VALUES(QACREDT) "); // F10: アンケート作成日
    sbSQL.append(" ,QARCREDT=VALUES(QARCREDT) "); // F11: アンケート再作成日
    sbSQL.append(" ,JLSTCREFLG=VALUES(JLSTCREFLG) "); // F12: 事前発注リスト作成済フラグ
    sbSQL.append(" ,HNCTLFLG=VALUES(HNCTLFLG) "); // F13: 本部コントロールフラグ
    sbSQL.append(" ,TPNG1FLG=VALUES(TPNG1FLG) "); // F14: 店不採用禁止フラグ
    sbSQL.append(" ,TPNG2FLG=VALUES(TPNG2FLG) "); // F15: 店売価選択禁止フラグ
    sbSQL.append(" ,TPNG3FLG=VALUES(TPNG3FLG) "); // F16: 店商品選択禁止フラグ
    sbSQL.append(" ,SIMEFLG1_LD=VALUES(SIMEFLG1_LD) "); // F17: 仮締フラグ_リーダー店
    sbSQL.append(" ,SIMEFLG2_LD=VALUES(SIMEFLG2_LD) "); // F18: 本締フラグ_リーダー店
    sbSQL.append(" ,SIMEFLG_MB=VALUES(SIMEFLG_MB) "); // F19: 本締フラグ_各店
    sbSQL.append(" ,QADEVSTDT=VALUES(QADEVSTDT) "); // F20: アンケート取込開始日
    sbSQL.append(" ,UPDKBN=VALUES(UPDKBN) "); // F21: 更新区分
    sbSQL.append(" ,SENDFLG=VALUES(SENDFLG) "); // F22: 送信フラグ
    sbSQL.append(" ,OPERATOR=VALUES(OPERATOR) "); // F23: オペレータ
    // sbSQL.append(" ,ADDDT "); // F24: 登録日
    sbSQL.append(" ,UPDDT=VALUES(UPDDT) "); // F25: 更新日


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催し基本情報");
    return result;
  }


  /**
   * 関連催し基本情報UPDATE SQL作成処理 再作成日の入力値は、同一アンケート月度、月度枝番のデータに対してUPDATEする。
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKTG_KHN_RE(String userId, JSONObject data) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String[] target = new String[] {TOKTG_KHNLayout.MOYSKBN.getId(), TOKTG_KHNLayout.MOYSSTDT.getId(), TOKTG_KHNLayout.MOYSRBAN.getId(), TOKTG_KHNLayout.QAYYYYMM.getId(),
        TOKTG_KHNLayout.QAENO.getId(), TOKTG_KHNLayout.QARCREDT.getId()};

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKTG_KHN ( ");
    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,QAYYYYMM"); // F8 : アンケート月度
    sbSQL.append(" ,QAENO"); // F9 : アンケート月度枝番
    sbSQL.append(" ,QARCREDT"); // F11: アンケート再作成日
    sbSQL.append(" ,UPDKBN"); // F21: 更新区分
    sbSQL.append(" ,SENDFLG"); // F22: 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F23: オペレータ
    sbSQL.append(" ,UPDDT"); // F25: 更新日
    sbSQL.append(")values(");
    for (TOKTG_KHNLayout itm : TOKTG_KHNLayout.values()) {
      String TYP2;
      if (!ArrayUtils.contains(target, itm.getId())) {
        continue;
      } // 対象外
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      if (itm.getTyp() == "SMALLINT" || itm.getTyp() == "INTEGER") {
        TYP2 = "SIGNED";
      } else if (itm.getTyp() == "TIMESTAMP") {
        TYP2 = "DATETIME";
      } else {
        TYP2 = "CHAR(20)";
      }
      if (data.containsKey(itm.getId())) {
        String value = StringUtils.strip(data.optString(itm.getId()));
        if (StringUtils.isNotEmpty(value)) {
          prmData.add(value);
          sbSQL.append("cast(? as " + TYP2 + ") ");
        } else {
          sbSQL.append("cast(null as " + TYP2 + ") ");
        }
      } else {
        sbSQL.append("null ");
      }
    }
    sbSQL.append(" ," + DefineReport.ValUpdkbn.NML.getVal()); // F21: 更新区分
    sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal()); // F22: 送信フラグ
    sbSQL.append(" ,'" + userId + "'"); // F23: オペレータ
    sbSQL.append(" ,current_timestamp"); // F25: 更新日

    sbSQL.append(" )");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("  MOYSKBN=VALUES(MOYSKBN) "); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT=VALUES(MOYSSTDT) "); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN=VALUES(MOYSRBAN) "); // F3 : 催し連番
    sbSQL.append(" ,QAYYYYMM=VALUES(QAYYYYMM) "); // F8 : アンケート月度
    sbSQL.append(" ,QAENO=VALUES(QAENO) "); // F9 : アンケート月度枝番
    sbSQL.append(" ,QARCREDT=VALUES(QARCREDT) "); // F11: アンケート再作成日
    sbSQL.append(" ,UPDKBN=VALUES(UPDKBN) "); // F21: 更新区分
    sbSQL.append(" ,SENDFLG=VALUES(SENDFLG) "); // F22: 送信フラグ
    sbSQL.append(" ,OPERATOR=VALUES(OPERATOR) "); // F23: オペレータ
    sbSQL.append(" ,UPDDT=VALUES(UPDDT) "); // F25: 更新日

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催し基本情報(関連情報)");
    return result;
  }

  /**
   * 最新情報取得(全店特売（アンケート有）_基本)
   *
   * @throws Exception
   */
  public static JSONArray getJSONArrayTOKTG_KHN(String sqlWhere, ArrayList<String> paramData) {
    // 関連情報取得
    StringBuffer sbSQL = new StringBuffer();

    sbSQL = new StringBuffer();
    sbSQL.append("select *");
    sbSQL.append(" ,DATE_FORMAT(T1.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT");
    sbSQL.append(" from INATK.TOKTG_KHN T1");
    sbSQL.append(" where COALESCE(T1.UPDKBN, 0) <> 1" + sqlWhere);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
    return array;
  }

  /**
   * 3.「アンケート月度枝番」と「アンケート作成日」チェック<br>
   *
   * @throws Exception
   */
  public static JSONObject checkQACREDTAndQAENO(User userInfo, JSONObject cond) {
    ArrayList<String> paramData = new ArrayList<String>();
    paramData.add(cond.optString(TOKTG_KHNLayout.QAYYYYMM.getCol()));
    paramData.add(cond.optString(TOKTG_KHNLayout.QAENO.getCol()));
    paramData.add(cond.optString(TOKTG_KHNLayout.QACREDT.getCol()));

    // 3.1.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート月度枝番」を持つレコードがある場合、【画面】.当行の「アンケート作成日」と異なったら、エラー。
    String sqlWhere = " and QAYYYYMM = ? and QAENO = ? and QACREDT <> ?";
    JSONArray res1 = getJSONArrayTOKTG_KHN(sqlWhere, paramData);
    if (res1.size() > 0) {
      // E20192 同じ月度枝番の催しでは、アンケート作成日を同じにしてください。 0 E
      return MessageUtility.getDbMessageIdObj("E20192", new String[] {});
    }

    // 3.2.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート作成日」を持つレコードがある場合、【画面】.当行の「アンケート月度枝番」と異なったら、エラー。
    sqlWhere = " and not(QAYYYYMM = ? and QAENO = ?) and QACREDT = ?";
    JSONArray res2 = getJSONArrayTOKTG_KHN(sqlWhere, paramData);
    if (res2.size() > 0) {
      // E20196 同じアンケートが作成日ある場合、月度枝番が異なると更新できません。 0 E
      return MessageUtility.getDbMessageIdObj("E20196", new String[] {});
    }

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("CNT", 0);
    return data;
  }


  /**
   * 4.「アンケート月度枝番」と「アンケート取込開始日」チェック<br>
   *
   * @throws Exception
   */
  public static JSONObject checkQADEVSTDTAndQAENO(User userInfo, JSONObject cond) {
    ArrayList<String> paramData = new ArrayList<String>();
    paramData.add(cond.optString(TOKTG_KHNLayout.QAYYYYMM.getCol()));
    paramData.add(cond.optString(TOKTG_KHNLayout.QAENO.getCol()));
    paramData.add(cond.optString(TOKTG_KHNLayout.QADEVSTDT.getCol()));

    // 4.1.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート月度枝番」を持つレコードがある場合、【画面】.当行の「アンケート取込開始日」と異なったら、エラー。
    String sqlWhere = " and QAYYYYMM = ? and QAENO = ? and QADEVSTDT <> ?";
    JSONArray res1 = getJSONArrayTOKTG_KHN(sqlWhere, paramData);
    if (res1.size() > 0) {
      // E20195 同じ月度枝番がある場合、アンケート再作成日が異なると更新できません。 0 E
      return MessageUtility.getDbMessageIdObj("E20197", new String[] {});
    }

    // 4.2.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート取込開始日」を持つレコードがある場合、【画面】.当行の「アンケート月度枝番」と異なったら、エラー。
    sqlWhere = " and not(QAYYYYMM = ? and QAENO = ?) and QADEVSTDT = ?";
    JSONArray res2 = getJSONArrayTOKTG_KHN(sqlWhere, paramData);
    if (res2.size() > 0) {
      // E20199 同じアンケート取込開始日がある場合、月度枝番が異なると更新できません。 0 E
      return MessageUtility.getDbMessageIdObj("E20199", new String[] {});
    }

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("CNT", 0);
    return data;
  }

  /** 全店特売（アンケート有）_基本レイアウト() */
  public enum TOKTG_KHNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 販売日1日遅許可フラグ */
    HBOKUREFLG(4, "HBOKUREFLG", "SMALLINT", "販売日1日遅許可フラグ"),
    /** 月締日 */
    GTSIMEDT(5, "GTSIMEDT", "INTEGER", "月締日"),
    /** 月締フラグ */
    GTSIMEFLG(6, "GTSIMEFLG", "SMALLINT", "月締フラグ"),
    /** 最終締日 */
    LSIMEDT(7, "LSIMEDT", "INTEGER", "最終締日"),
    /** アンケート月度 */
    QAYYYYMM(8, "QAYYYYMM", "INTEGER", "アンケート月度"),
    /** アンケート月度枝番 */
    QAENO(9, "QAENO", "SMALLINT", "アンケート月度枝番"),
    /** アンケート作成日 */
    QACREDT(10, "QACREDT", "INTEGER", "アンケート作成日"),
    /** アンケート再作成日 */
    QARCREDT(11, "QARCREDT", "INTEGER", "アンケート再作成日"),
    /** 事前発注リスト作成済フラグ */
    JLSTCREFLG(12, "JLSTCREFLG", "SMALLINT", "事前発注リスト作成済フラグ"),
    /** 本部コントロールフラグ */
    HNCTLFLG(13, "HNCTLFLG", "SMALLINT", "本部コントロールフラグ"),
    /** 店不採用禁止フラグ */
    TPNG1FLG(14, "TPNG1FLG", "SMALLINT", "店不採用禁止フラグ"),
    /** 店売価選択禁止フラグ */
    TPNG2FLG(15, "TPNG2FLG", "SMALLINT", "店売価選択禁止フラグ"),
    /** 店商品選択禁止フラグ */
    TPNG3FLG(16, "TPNG3FLG", "SMALLINT", "店商品選択禁止フラグ"),
    /** 仮締フラグ_リーダー店 */
    SIMEFLG1_LD(17, "SIMEFLG1_LD", "SMALLINT", "仮締フラグ_リーダー店"),
    /** 本締フラグ_リーダー店 */
    SIMEFLG2_LD(18, "SIMEFLG2_LD", "SMALLINT", "本締フラグ_リーダー店"),
    /** 本締フラグ_各店 */
    SIMEFLG_MB(19, "SIMEFLG_MB", "SMALLINT", "本締フラグ_各店"),
    /** アンケート取込開始日 */
    QADEVSTDT(20, "QADEVSTDT", "INTEGER", "アンケート取込開始日"),
    /** 更新区分 */
    UPDKBN(21, "UPDKBN", "SMALLINT", "更新区分"),
    /** 送信フラグ */
    SENDFLG(22, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(23, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(24, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(25, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKTG_KHNLayout(Integer no, String col, String typ, String txt) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.txt = txt;
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

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    public String getTxt() {
      return txt;
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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

  /** CSV出力ファイル情報() */
  public enum CsvFileInfo {
    /** チラシ原稿CSV出力 */
    CSV1(1, "btn_csv1", "チラシCSV", "MDCR014", 46),
    /** POP原稿CSV出力 */
    CSV2(2, "btn_csv2", "POP原稿CSV", "MDCR015", 46),
    /** アンケートCSV出力 */
    CSV3(3, "btn_csv3", "アンケートCSV", "MDCR016", 46),
    /** 特売商品一覧CSV出力 */
    CSV4(4, "btn_csv4", "特売商品一覧CSV", "MDCR008", 630);

    private final Integer no;
    private final String bid;
    private final String bnm;
    private final String fnm;
    private final Integer len;

    /** 初期化 */
    private CsvFileInfo(Integer no, String bid, String bnm, String fnm, Integer len) {
      this.no = no;
      this.bid = bid;
      this.bnm = bnm;
      this.fnm = fnm;
      this.len = len;
    }

    /** @return no 連番 */
    public Integer getNo() {
      return no;
    }

    /** @return bid ボタンID */
    public String getBid() {
      return bid;
    }

    /** @return bnm ボタン名 */
    public String geBnm() {
      return bnm;
    }

    /** @return bnm ファイル名 */
    public String geFnm() {
      return fnm;
    }

    /** @return len レコード長 */
    public Integer getLen() {
      return len;
    }

    /** @return len レコード長(改行コード桁除外) */
    public Integer getDataLen() {
      return len - LEN_NEW_LINE_CODE;
    }

  }
}
