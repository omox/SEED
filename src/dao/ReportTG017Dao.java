package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.DefineReport.ValKbn10002;
import common.JsonArrayData;
import dao.ReportTM002Dao.TOKMOYCDCMNLayout;
import dao.Reportx002Dao.MSTSHNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTG017Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG017Dao(String JNDIname) {
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

    FtpFileInfo fio = this.getFtpFileInfo(getMap());
    JSONArray dataArray = JSONArray.fromObject(getMap().get("DATA")); // 選択情報

    // 検索コマンド生成（基本情報取得）
    String command = createCommandForDl(getUserInfo().getId(), dataArray, fio, true);

    // オプション情報設定
    JSONObject option = new JSONObject();
    option.put("FILE_NAME", fio.getFnm());
    setOption(option);

    return super.selectBySQL(command, false);
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szShuno = getMap().get("SHUNO"); // 入力商品コード
    String szStym = getMap().get("STYM"); // 表示年月From
    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    JSONArray moyskbnAllArray = JSONArray.fromObject(getMap().get("MOYSKBN_DATA")); // 全催し区分

    // パラメータ確認
    /*
     * // 必須チェック if ((btnId == null)) { System.out.println(super.getConditionLog()); return ""; }
     */

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();
    ArrayList<String> prmData = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(",WKCD as ( ");
    sbSQL.append(" select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.MOYKN, T1.HBSTDT, T1.HBEDDT ");
    sbSQL.append(" ,case when T1.MOYSKBN = " + ValKbn10002.VAL1.getVal() + " and T1.MOYSRBAN >= 50 then 1 else 2 end as KBN"); // アンケート有無区分
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" where T1.UPDKBN = 0");
    if (!DefineReport.Values.NONE.getVal().equals(szShuno)) {
      sbSQL.append(" and T1.SHUNO = " + szShuno);
    }
    if (!StringUtils.isEmpty(szStym) && DefineReport.Values.NONE.getVal().equals(szShuno)) {
      sbSQL.append(" and T1.MOYSSTDT between ? and ? ");
      prmData.add(szStym + "00");
      prmData.add(szStym + "99");
      setParamData(prmData);
    }
    if (!StringUtils.isEmpty(szMoyskbn) && !DefineReport.Values.NONE.getVal().equals(szMoyskbn)) {
      sbSQL.append(" and T1.MOYSKBN = " + szMoyskbn);
    } else {
      sbSQL.append(" and T1.MOYSKBN IN (" + StringUtils.removeEnd(StringUtils.replace(moyskbnAllArray.join(","), "\"", "'"), ",") + ")");
    }
    // 但し、催し区分=2 AND納入開始日・終了日がNULLのデータは表示しない。＝ 「送信情報」非表示
    sbSQL.append(" and not (T1.MOYSKBN = " + ValKbn10002.VAL2.getVal() + " and T1.NNSTDT is null and T1.NNSTDT is null)");
    sbSQL.append(") ");
    sbSQL.append(" select");
    sbSQL.append("   T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F1");
    sbSQL.append("  ,T1.MOYKN as F2");
    sbSQL.append("  ,case when T1.KBN = 1 then T2.HBOKUREFLG end as F3");
    sbSQL.append(
        "  ,case when T1.HBSTDT = T1.HBEDDT then DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'))) ELSE ");
    sbSQL.append("  DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("   ||'～'||");
    sbSQL.append("   DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%Y%m%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) END as F4");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T2.GTSIMEDT, '%Y%m%d'), '%m/%d')  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.GTSIMEDT, '%Y%m%d'))) as F5");
    sbSQL.append("  ,case when T1.KBN = 1 then T2.GTSIMEFLG end as F6");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T2.LSIMEDT, '%Y%m%d'), '%m/%d')   || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.LSIMEDT, '%Y%m%d'))) as F7");
    sbSQL.append("  ,COALESCE(T3.CNT, 0) as F8");
    sbSQL.append("  ,COALESCE(T4.CNT, 0) as F9");
    sbSQL.append("  ,T1.MOYSKBN as F10");
    sbSQL.append("  ,T1.MOYSSTDT as F11");
    sbSQL.append("  ,T1.MOYSRBAN as F12");
    sbSQL.append(" from");
    sbSQL.append("  WKCD T1 ");
    sbSQL.append("  left outer join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");
    sbSQL.append("  left outer join ( ");
    sbSQL.append("    select");
    sbSQL.append("     MOYSKBN, MOYSSTDT, MOYSRBAN");
    sbSQL.append("    ,SUM(case when KANRIENO = KANRIENO_MAX then 1 end) as CNT ");
    sbSQL.append("    from");
    sbSQL.append("      ( ");
    // アンケート有
    sbSQL.append("        select");
    sbSQL.append("            MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO");
    sbSQL.append("          , MAX(KANRIENO) over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO) as KANRIENO_MAX"); // 枝番Max確保
    sbSQL.append("        from INATK.TOKTG_SHN T3");
    sbSQL.append("        where");
    sbSQL.append("          exists (select 'X' from WKCD T1 where T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN and T1.KBN = 1)");
    sbSQL.append("          and UPDKBN = 0");
    sbSQL.append("        union all");
    // アンケート無
    sbSQL.append("        select");
    sbSQL.append("            MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO");
    sbSQL.append("          , MAX(KANRIENO) over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO) as KANRIENO_MAX"); // 枝番Max確保
    sbSQL.append("        from INATK.TOKSP_SHN T3");
    sbSQL.append("        where");
    sbSQL.append("          exists (select 'X' from WKCD T1 where T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN and T1.KBN = 2)");
    sbSQL.append("          and UPDKBN = 0");
    sbSQL.append("      ) T ");
    sbSQL.append("    group by MOYSKBN, MOYSSTDT, MOYSRBAN ");
    sbSQL.append("  ) T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
    sbSQL.append("  left outer join ( ");
    sbSQL.append("    select");
    sbSQL.append("     MOYSKBN, MOYSSTDT, MOYSRBAN");
    sbSQL.append("    ,SUM(case when KANRIENO = KANRIENO_MAX then 1 end) as CNT ");
    sbSQL.append("    from");
    sbSQL.append("      ( ");
    // アンケート有
    sbSQL.append("        select");
    sbSQL.append("            MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO");
    sbSQL.append("          , MAX(KANRIENO) over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO) as KANRIENO_MAX"); // 枝番Max確保
    sbSQL.append("        from INATK.TOKTG_SHN T3");
    sbSQL.append("        where");
    sbSQL.append("          exists (select 'X' from WKCD T1 where T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN and T1.KBN = 1)");
    sbSQL.append("          and COALESCE(GTSIMECHGKBN, 0) <> 0 "); // 月締変更理由<>0
    sbSQL.append("          and COALESCE(GTSIMEOKFLG, 0) <> 1 "); // 月締変更許可フラグ<>1:許可
    sbSQL.append("          and UPDKBN = 0");
    sbSQL.append("      ) T ");
    sbSQL.append("    group by MOYSKBN, MOYSSTDT, MOYSRBAN ");
    sbSQL.append("  ) T4 on T1.MOYSKBN = T4.MOYSKBN and T1.MOYSSTDT = T4.MOYSSTDT and T1.MOYSRBAN = T4.MOYSRBAN");
    sbSQL.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");


    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  public FtpFileInfo getFtpFileInfo(HashMap<String, String> map) {
    String btnId = map.get("BTN"); // 実行ボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 選択情報

    JSONObject data = dataArray.optJSONObject(0);
    String szMoyskbn = data.optString(TOKMOYCDCMNLayout.MOYSKBN.getId()); // 催し区分
    data.optString(TOKMOYCDCMNLayout.MOYSSTDT.getId());
    String szMoysrban = data.optString(TOKMOYCDCMNLayout.MOYSRBAN.getId()); // 催し連番

    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    // ファイル情報
    FtpFileInfo fi = null;
    if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj() + "1")) {
      if (isTOKTG) {
        fi = FtpFileInfo.CSV1_TG;
      } else {
        fi = FtpFileInfo.CSV1_SP;
      }
    } else if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj() + "2")) {
      fi = FtpFileInfo.CSV2;
    }
    return fi;
  }

  public String createCommandForDl(String userId, JSONArray dataArray, FtpFileInfo cfi, boolean containHeadSql) {
    // パラメータ確認
    // 必須チェック
    if ((dataArray.size() == 0)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    if (cfi.equals(FtpFileInfo.CSV2)) {
      sbSQL.append(this.createCommandCSV2(userId, dataArray, containHeadSql));
      // アンケート有
    } else if (cfi.equals(FtpFileInfo.CSV1_TG)) {
      sbSQL.append(this.createCommandCSV1_TG(userId, dataArray, containHeadSql));

      // アンケート無
    } else if (cfi.equals(FtpFileInfo.CSV1_SP)) {
      sbSQL.append(this.createCommandCSV1_SP(userId, dataArray, containHeadSql));
    }

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  public String createCommandCheckList(String userId, JSONObject obj) {
    String btnId = obj.optString("BTN"); // 実行ボタン

    String moyskbn = obj.optString("MOYSKBN");
    obj.optString("MOYSSTDT");
    String moysrban = obj.optString("MOYSRBAN");
    obj.optString("BMNCD");
    int datalen = obj.optInt("REQLEN") - LEN_NEW_LINE_CODE; // 1レコードのbyte数

    boolean isTOKTG = super.isTOKTG(moyskbn, moysrban);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("with WK as (");
    sbSQL.append("select ");
    sbSQL.append(" cast(? as " + MSTSHNLayout.BMNCD.getTyp() + ") as " + MSTSHNLayout.BMNCD.getCol());
    for (TOKMOYCDCMNLayout itm : TOKMOYCDCMNLayout.values()) {
      sbSQL.append(",cast(? as " + itm.getTyp() + ") as " + itm.getCol());
    }
    sbSQL.append(" from SYSIBM.SYSDUMMY1");
    sbSQL.append(")");

    String sort = "0";
    if (StringUtils.equals(btnId, DefineReport.Button.CHECKLIST.getObj() + "1")) {
      // 管理NO順
      sort = "1";
      sbSQL.append(this.createCommandCHK(userId, datalen, isTOKTG, sort));
      sbSQL.append(" order by MOYCD, RNO, KANRINO");
    } else if (StringUtils.equals(btnId, DefineReport.Button.CHECKLIST.getObj() + "2")) {
      // 商品コード順
      sort = "3";
      sbSQL.append(this.createCommandCHK(userId, datalen, isTOKTG, sort));
      sbSQL.append(" order by MOYCD, RNO, SHNCD");
    } else if (StringUtils.equals(btnId, DefineReport.Button.CHECKLIST.getObj() + "3")) {
      // 販促用チェックリスト
      sbSQL.append(this.createCommandCHK(userId, datalen, isTOKTG, sort));
      sbSQL.append(" order by MOYCD, RNO, REC");
    } else if (StringUtils.equals(btnId, DefineReport.Button.CHECKLIST.getObj() + "4")) {
      // 納入期間順
      sort = "2";
      sbSQL.append(this.createCommandCHK(userId, datalen, isTOKTG, sort));
      sbSQL.append(" order by MOYCD, RNO, NNSTDT");
    } else if (StringUtils.equals(btnId, DefineReport.Button.CHECKLIST.getObj() + "5")) {
      // 分類順
      sort = "4";
      sbSQL.append(this.createCommandCHK(userId, datalen, isTOKTG, sort));
      sbSQL.append(" order by MOYCD, RNO, DAICD, CHUCD");
    } else if (StringUtils.equals(btnId, DefineReport.Button.CHECKLIST.getObj() + "6")) {
      // 週間特売原稿
      sbSQL.append(this.createCommandCHK(userId, datalen, isTOKTG, sort));
      sbSQL.append(" order by MOYCD, RNO, REC");
    }
    // TODO 並び順変更
    // sbSQL.append(" order by MOYCD, RNO, REC");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  public String createCommandCHK(String userId, int datalen, boolean isTOKTG, String sort) {
    String szTableSHN = ""; // 全店特売_商品
    String szTableTJTEN = ""; // 全店特売_対象除外店
    String szTableNNDT = ""; // 全店特売_納品日

    if (isTOKTG) {
      // アンケート有
      szTableSHN = "INATK.TOKTG_SHN";
      szTableTJTEN = "INATK.TOKTG_TJTEN";
      szTableNNDT = "INATK.TOKTG_NNDT";
    } else {
      // アンケート無
      szTableSHN = "INATK.TOKSP_SHN";
      szTableTJTEN = "INATK.TOKSP_TJTEN";
      szTableNNDT = "INATK.TOKSP_NNDT";
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(", WK2 as (select");
    sbSQL.append(" MOYSKBN");
    sbSQL.append(", MOYSSTDT");
    sbSQL.append(", MOYSRBAN");
    sbSQL.append(", BMNCD");
    sbSQL.append(", KANRINO");
    sbSQL.append(", KANRIENO");
    sbSQL.append(", MAX(case when IDX = 1 then (case when TJFLG = 1 then TENCD end) end) as ADT1");
    sbSQL.append(", MAX(case when IDX = 2 then (case when TJFLG = 1 then TENCD end) end) as ADT2");
    sbSQL.append(", MAX(case when IDX = 3 then (case when TJFLG = 1 then TENCD end) end) as ADT3");
    sbSQL.append(", MAX(case when IDX = 4 then (case when TJFLG = 1 then TENCD end) end) as ADT4");
    sbSQL.append(", MAX(case when IDX = 5 then (case when TJFLG = 1 then TENCD end) end) as ADT5");
    sbSQL.append(", MAX(case when IDX = 6 then (case when TJFLG = 1 then TENCD end) end) as ADT6");
    sbSQL.append(", MAX(case when IDX = 7 then (case when TJFLG = 1 then TENCD end) end) as ADT7");
    sbSQL.append(", MAX(case when IDX = 8 then (case when TJFLG = 1 then TENCD end) end) as ADT8");
    sbSQL.append(", MAX(case when IDX = 9 then (case when TJFLG = 1 then TENCD end) end) as ADT9");
    sbSQL.append(", MAX(case when IDX = 10 then (case when TJFLG = 1 then TENCD end) end) as ADT10");
    sbSQL.append(", MAX(case when IDX = 1 then (case when TJFLG = 1 then TENRANK end) end) as RNK1");
    sbSQL.append(", MAX(case when IDX = 2 then (case when TJFLG = 1 then TENRANK end) end) as RNK2");
    sbSQL.append(", MAX(case when IDX = 3 then (case when TJFLG = 1 then TENRANK end) end) as RNK3");
    sbSQL.append(", MAX(case when IDX = 4 then (case when TJFLG = 1 then TENRANK end) end) as RNK4");
    sbSQL.append(", MAX(case when IDX = 5 then (case when TJFLG = 1 then TENRANK end) end) as RNK5");
    sbSQL.append(", MAX(case when IDX = 6 then (case when TJFLG = 1 then TENRANK end) end) as RNK6");
    sbSQL.append(", MAX(case when IDX = 7 then (case when TJFLG = 1 then TENRANK end) end) as RNK7");
    sbSQL.append(", MAX(case when IDX = 8 then (case when TJFLG = 1 then TENRANK end) end) as RNK8");
    sbSQL.append(", MAX(case when IDX = 9 then (case when TJFLG = 1 then TENRANK end) end) as RNK9");
    sbSQL.append(", MAX(case when IDX = 10 then (case when TJFLG = 1 then TENRANK end) end) as RNK10");
    sbSQL.append(", MAX(case when IDX = 1 then (case when TJFLG = 2 then TENCD end) end) as DLT1");
    sbSQL.append(", MAX(case when IDX = 2 then (case when TJFLG = 2 then TENCD end) end) as DLT2");
    sbSQL.append(", MAX(case when IDX = 3 then (case when TJFLG = 2 then TENCD end) end) as DLT3");
    sbSQL.append(", MAX(case when IDX = 4 then (case when TJFLG = 2 then TENCD end) end) as DLT4");
    sbSQL.append(", MAX(case when IDX = 5 then (case when TJFLG = 2 then TENCD end) end) as DLT5");
    sbSQL.append(", MAX(case when IDX = 6 then (case when TJFLG = 2 then TENCD end) end) as DLT6");
    sbSQL.append(", MAX(case when IDX = 7 then (case when TJFLG = 2 then TENCD end) end) as DLT7");
    sbSQL.append(", MAX(case when IDX = 8 then (case when TJFLG = 2 then TENCD end) end) as DLT8");
    sbSQL.append(", MAX(case when IDX = 9 then (case when TJFLG = 2 then TENCD end) end) as DLT9");
    sbSQL.append(", MAX(case when IDX = 10 then (case when TJFLG = 2 then TENCD end) end) as DLT10");
    sbSQL.append(" from (select");
    sbSQL.append(" ROW_NUMBER() over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO order by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO, TENCD) as IDX");
    sbSQL.append(", MOYSKBN");
    sbSQL.append(", MOYSSTDT");
    sbSQL.append(", MOYSRBAN");
    sbSQL.append(", BMNCD");
    sbSQL.append(", KANRINO");
    sbSQL.append(", KANRIENO");
    sbSQL.append(", TENCD");
    sbSQL.append(", TJFLG");
    sbSQL.append(", TENRANK");
    sbSQL.append(" from " + szTableTJTEN + ")");
    sbSQL.append(" group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO)");

    sbSQL.append(", CAL as (");
    sbSQL.append(" select");
    sbSQL.append(" T3.MOYSKBN");
    sbSQL.append(", T3.MOYSSTDT");
    sbSQL.append(", T3.MOYSRBAN");
    sbSQL.append(", T3.BMNCD");
    sbSQL.append(", T3.KANRINO");
    sbSQL.append(", T3.KANRIENO");
    for (int i = 2; i <= 10; i++) {
      sbSQL.append(", case");
      sbSQL.append("  when int (DATE_FORMAT(DATE_FORMAT(T3.NNSTDT, '%Y%m%d') + " + (i - 1) + " , '%Y%m%d')) <= COALESCE(");
      sbSQL.append("   case");
      sbSQL.append("   when T3.HBEDDT > T3.NNEDDT then T3.HBEDDT else T3.NNEDDT end");
      sbSQL.append(" , 0) then DATE_FORMAT(DATE_FORMAT(T3.NNSTDT, '%Y%m%d') + " + (i - 1) + " , '%Y%m%d') end as DT" + i);
    }
    sbSQL.append(" from " + szTableSHN + " T3");
    sbSQL.append(")");

    if (isTOKTG) {
      // 月締後登録データの出力有無を判断するワークテーブルの追加
      /*
       * 月締変更理由:GTSIMECHGKBN 0：通常 1：追加 2：変更 3：削除 4：差替
       * 
       * アンケート有りの月締後変更が行われている商品において、 ・未承認の場合（(月締変更許可フラグ[GTSIMEOKFLG] = 0:未許可 or NULL) AND
       * 月締変更理由[GTSIMECHGKBN]<>0） 削除商品：出力する 新規商品：出力しない 差替商品：旧商品（枝番-1）で出力する 変更商品：旧商品（枝番-1）で出力する
       * 
       * ・承認済みの場合（(月締変更許可フラグ[GTSIMEOKFLG] = 1:許可) AND 月締変更理由[GTSIMECHGKBN]<>0）（修正無し） 削除商品：出力しない 新規商品：出力する
       * 差替商品：新商品（MAX枝番）で出力する 変更商品：新商品（MAX枝番）で出力する
       */

      sbSQL.append(", SENDKANRINO as (");
      sbSQL.append(" select");
      sbSQL.append(" T1.MOYSKBN");
      sbSQL.append(", T1.MOYSSTDT");
      sbSQL.append(", T1.MOYSRBAN");
      sbSQL.append(", T1.BMNCD");
      sbSQL.append(", T1.KANRINO");
      sbSQL.append(", case");
      sbSQL.append("  when COALESCE(T1.GTSIMEOKFLG, 0) = 0 then (");
      sbSQL.append("    case");
      sbSQL.append("    when T1.GTSIMECHGKBN = 2 then (WK1.KANRIENO - 1)");
      sbSQL.append("    when T1.GTSIMECHGKBN = 3 then WK1.KANRIENO");
      sbSQL.append("    when T1.GTSIMECHGKBN = 4 then (WK1.KANRIENO - 1) end)");
      sbSQL.append("  when COALESCE(T1.GTSIMEOKFLG, 0) = 1 then (");
      sbSQL.append("    case");
      sbSQL.append("    when T1.GTSIMECHGKBN = 1 then WK1.KANRIENO");
      sbSQL.append("    when T1.GTSIMECHGKBN = 2 then WK1.KANRIENO");
      sbSQL.append("    when T1.GTSIMECHGKBN = 4 then WK1.KANRIENO end) end as KANRIENO"); // 管理No(送信使用用)
      sbSQL.append(", case");
      sbSQL.append("  when COALESCE(T1.GTSIMEOKFLG, 0) = 0 then (");
      sbSQL.append("    case");
      sbSQL.append("    when T1.GTSIMECHGKBN = 1 then 1 else 0 end)");
      sbSQL.append("  when COALESCE(T1.GTSIMEOKFLG, 0) = 1 then (");
      sbSQL.append("    case when T1.GTSIMECHGKBN = 3 then 1 else 0 end) end as NOTOUTPUTFLG"); // 非出力フラグ 0:出力、1:非出力
      sbSQL.append(" from INATK.TOKTG_SHN T1");
      sbSQL.append(" inner join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.BMNCD, T3.KANRINO, MAX(T1.GTSIMEDT), MAX(T1.GTSIMEFLG), MAX(T3.KANRIENO) as KANRIENO");
      sbSQL.append("  from INATK.TOKTG_KHN T1");
      sbSQL.append("  inner join INATK.TOKTG_BMN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
      sbSQL.append("  inner join INATK.TOKTG_SHN T3 on T2.MOYSKBN = T3.MOYSKBN and T2.MOYSSTDT = T3.MOYSSTDT and T2.MOYSRBAN = T3.MOYSRBAN and T2.BMNCD = T3.BMNCD");
      sbSQL.append("  where T1.GTSIMEDT < CAST(DATE_FORMAT(T3.UPDDT, '%Y%m%d') AS signed) and T1.GTSIMEFLG = 1 and T3.GTSIMECHGKBN <> 0");
      sbSQL.append(
          "  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.BMNCD, T3.KANRINO) WK1 on T1.MOYSKBN = WK1.MOYSKBN and T1.MOYSSTDT = WK1.MOYSSTDT and T1.MOYSRBAN = WK1.MOYSRBAN and T1.BMNCD = WK1.BMNCD and T1.KANRINO = WK1.KANRINO and T1.KANRIENO = WK1.KANRIENO ");
      sbSQL.append(")");
    }

    sbSQL.append("select REC from (");
    sbSQL.append(" select distinct 1 as RNO");
    sbSQL.append(" ,T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0') as MOYCD");
    sbSQL.append(" ,null as KANRINO");
    sbSQL.append(" ,null as SHNCD");
    sbSQL.append(" ,null as DAICD");
    sbSQL.append(" ,null as CHUCD");
    sbSQL.append(" ,null as NNSTDT");
    sbSQL.append(" ,rpad('D1'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||left(rpad(COALESCE(T1.MOYKN,''),40,'　'),40)"); // 催し名称(漢字) 催しコード.催し名称（漢字） NULLの時ALL全SP/桁数に満たない時全SPACE
    sbSQL.append(" ||left(lpad(COALESCE(CAST(T1.SHUNO AS CHAR),''),6,'0'),4)"); // 週№_年 催しコード.週№(前2桁) NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||right(lpad(COALESCE(CAST(T1.SHUNO AS CHAR),''),6,'0'),2)"); // 週№_週 催しコード.週№(後2桁) NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTDT AS CHAR),''),8,'0')"); // 催し販売開始日 催しコード.販売開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBEDDT AS CHAR),''),8,'0')"); // 催し販売終了日 催しコード.販売終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNSTDT AS CHAR),''),8,'0')"); // 催し納入開始日 催しコード.納入開始日生鮮時の値取得ロジックを考慮TG016の機能概要説明書5.3.2.2.を参照 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNEDDT AS CHAR),''),8,'0')"); // 催し納入終了日 催しコード.納入終了日生鮮時の値取得ロジックを考慮TG016の機能概要説明書5.3.2.2.を参照 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T2.HBOKUREFLG AS CHAR),''),1,'0')"); // 催し１日遅れフラグ 全店特売（アンケート有）_基本.販売日1日遅許可フラグ０：遅れ無し １：遅れ有り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NENMATKBN AS CHAR),''),1,'0')"); // 年末区分 催しコード.年末区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + datalen + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" left join INATK.TOKTG_KHN T2 on T2.UPDKBN = 0 and T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 2 as RNO");
    sbSQL.append(" ,T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0') as MOYCD");
    sbSQL.append(" ,T1.KANRINO");
    sbSQL.append(" ,T1.SHNCD");
    sbSQL.append(" ,T1.DAICD");
    sbSQL.append(" ,T1.CHUCD");
    sbSQL.append(" ,T1.NNSTDT");
    sbSQL.append(" ,rpad('D2'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||" + sort); // ソート順 画面指定１：管理No順 ２：納入期間順 ３：商品コード順 ４：分類順ただし、MDCR001とMDCRF006は0をセットする。 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.BMNCD,''),3,'0')"); // 部門 全店特売(アンケート有)_商品.部門全店特売(アンケート無)_商品.部門 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(CAST(T1.BYCD AS CHAR),''),30,'　'),30)"); // バイヤー氏名 全店特売(アンケート有)_商品.BYコード全店特売(アンケート無)_商品.BYコードログイン管理.氏名全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.KANRINO,''),4,'0')"); // 管理番号 全店特売(アンケート有)_商品.管理番号全店特売(アンケート無)_商品.管理番号 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.PARNO,''),3,' '),3)"); // グループ№(親№) 全店特売(アンケート有)_商品.親No全店特売(アンケート無)_商品.親No NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CHLDNO AS CHAR),''),2,'0')"); // 子№ 全店特売(アンケート有)_商品.子No全店特売(アンケート無)_商品.子No NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SHNCD,''),14,' '),14)"); // 商品コード 全店特売(アンケート有)_商品.商品コード全店特売(アンケート無)_商品.商品コード左詰でセット NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.POPKN,''),40,'　'),40)"); // ＰＯＰ名称(漢字) 全店特売(アンケート有)_商品.POP名称全店特売(アンケート無)_商品.POP名称全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.MAKERKN,''),28,'　'),28)"); // メーカー名(漢字) 全店特売(アンケート有)_商品.メーカー名全店特売(アンケート無)_商品.メーカー名全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.KIKKN,''),46,'　'),46)"); // 規格名称 全店特売(アンケート有)_商品.規格名称全店特売(アンケート無)_商品.規格名称全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SANCHIKN,''),40,'　'),40)"); // 産地 全店特売(アンケート有)_商品.産地全店特売(アンケート無)_商品.産地全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NAMANETUKBN AS CHAR),''),1,'0')"); // 生食加熱区分 全店特売(アンケート有)_商品.生食加熱区分全店特売(アンケート無)_商品.生食加熱区分１：生食 ２：加熱 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KAITOFLG AS CHAR),''),1,'0')"); // 解凍フラグ 全店特売(アンケート有)_商品.解凍フラグ全店特売(アンケート無)_商品.解凍フラグ０：通常 １：解凍 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.YOSHOKUFLG AS CHAR),''),1,'0')"); // 養殖フラグ 全店特売(アンケート有)_商品.養殖フラグ全店特売(アンケート無)_商品.養殖フラグ０：通常 １：養殖 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HIGAWRFLG AS CHAR),''),1,'0')"); // 日替区分 全店特売(アンケート有)_商品.日替フラグ全店特売(アンケート無)_商品.日替フラグ０：通し １：日替 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTDT AS CHAR),''),8,'0')"); // 販売期間_開始日 全店特売(アンケート有)_商品.販売期間_開始日全店特売(アンケート無)_商品.販売期間_開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBEDDT AS CHAR),''),8,'0')"); // 販売期間_終了日 全店特売(アンケート有)_商品.販売期間_終了日全店特売(アンケート無)_商品.販売期間_終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNSTDT AS CHAR),''),8,'0')"); // 納入期間_開始日 全店特売(アンケート有)_商品.納入期間_開始日全店特売(アンケート無)_商品.納入期間_開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNEDDT AS CHAR),''),8,'0')"); // 納入期間_終了日 全店特売(アンケート有)_商品.納入期間_終了日全店特売(アンケート無)_商品.納入期間_終了日 NULLの時ALL0/桁数に満たない時
    if (isTOKTG) {
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSLIDEFLG AS CHAR),''),1,'0')"); // 販売スライドフラグ 全店特売(アンケート有)_商品.一日遅スライド_販売０：スライドする １：スライドしないアンケート無は0をセット NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.NHSLIDEFLG AS CHAR),''),1,'0')"); // 納品スライドフラグ 全店特売(アンケート有)_商品.一日遅スライド_納入０：スライドする １：スライドしないアンケート無は0をセット NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD AS CHAR),''),3,'0')"); // 特売Ａ売価店グループ№ 全店特売(アンケート有)_商品.対象店ランク全店特売(アンケート無)_商品.対象店ランク_A売価対象店グループ№ NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad('',3,'0')"); // 特売Ｂ売価店グループ№ 全店特売(アンケート無)_商品.対象店ランク_B売価 NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad('',3,'0')"); // 特売Ｃ売価店グループ№ 全店特売(アンケート無)_商品.対象店ランク_C売価 NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad('',3,'0')"); // 特売除外店グループ№ 全店特売(アンケート無)_商品.除外店ランク NULLの時ALL0/桁数に満たない時

    } else {
      sbSQL.append(" ||'0'"); // 販売スライドフラグ 全店特売(アンケート有)_商品.一日遅スライド_販売０：スライドする １：スライドしないアンケート無は0をセット NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||'0'"); // 納品スライドフラグ 全店特売(アンケート有)_商品.一日遅スライド_納入０：スライドする １：スライドしないアンケート無は0をセット NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD_A AS CHAR),''),3,'0')"); // 特売Ａ売価店グループ№ 全店特売(アンケート有)_商品.対象店ランク全店特売(アンケート無)_商品.対象店ランク_A売価対象店グループ№ NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD_B AS CHAR),''),3,'0')"); // 特売Ｂ売価店グループ№ 全店特売(アンケート無)_商品.対象店ランク_B売価 NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD_C AS CHAR),''),3,'0')"); // 特売Ｃ売価店グループ№ 全店特売(アンケート無)_商品.対象店ランク_C売価 NULLの時ALL0/桁数に満たない時
      sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_DEL AS CHAR),''),3,'0')"); // 特売除外店グループ№ 全店特売(アンケート無)_商品.除外店ランク NULLの時ALL0/桁数に満たない時
    }
    sbSQL.append(" ||lpad(case when T1.TKANPLUKBN = " + DefineReport.ValKbn10414.VAL2.getVal() + " then '' else COALESCE(CAST(T1.GENKAAM_MAE AS CHAR),'') end,8,'0')"); // 特売事前原価
                                                                                                                                                                        // 全店特売(アンケート有)_商品.原価_特売事前全店特売(アンケート無)_商品.原価_特売事前
                                                                                                                                                                        // 不定貫時はALL0をセット
                                                                                                                                                                        // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(case when T1.TKANPLUKBN = " + DefineReport.ValKbn10414.VAL2.getVal() + " then '' else COALESCE(CAST(T1.GENKAAM_ATO AS CHAR),'') end,8,'0')"); // 特売追加原価
                                                                                                                                                                        // 全店特売(アンケート有)_商品.原価_特売追加全店特売(アンケート無)_商品.原価_特売追加
                                                                                                                                                                        // 不定貫時はALL0をセット
                                                                                                                                                                        // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.IRISU AS CHAR),''),3,'0')"); // 特売事前入数 全店特売(アンケート有)_商品.入数全店特売(アンケート無)_商品.入数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM AS CHAR),''),6,'0')"); // 特売Ａ売価 全店特売(アンケート有)_商品.A売価（100ｇ）全店特売(アンケート無)_商品.A売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM AS CHAR),''),6,'0')"); // 特売Ｂ売価 全店特売(アンケート有)_商品.B売価（100ｇ）全店特売(アンケート無)_商品.B売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM AS CHAR),''),6,'0')"); // 特売Ｃ売価 全店特売(アンケート有)_商品.C売価（100ｇ）全店特売(アンケート無)_商品.C売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TKANPLUKBN AS CHAR),''),1,'0')"); // 定貫ＰＬＵ・不定貫区分 全店特売(アンケート有)_商品.C定貫PLU・不定貫区分全店特売(アンケート無)_商品.C定貫PLU・不定貫区分１：定貫 ２：不定貫 （鮮魚、精肉以外は1をセット）
                                                                             // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM AS CHAR),''),6,'0')"); // 特売Ａ100ｇ売価 全店特売(アンケート有)_商品.A売価（100ｇ）全店特売(アンケート無)_商品.A売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_GENKAAM_1KG AS CHAR),''),6,'0')"); // 特売Ａ1㎏売価 全店特売(アンケート有)_商品.A売価_1㎏全店特売(アンケート無)_商品.A売価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM_PACK AS CHAR),''),6,'0')"); // 特売Ａﾊﾟｯｸ売価 全店特売(アンケート有)_商品.A売価_パック全店特売(アンケート無)_商品.A売価_パック NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM AS CHAR),''),6,'0')"); // 特売Ｂ100ｇ売価 全店特売(アンケート有)_商品.B売価（100ｇ）全店特売(アンケート無)_商品.B売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_GENKAAM_1KG AS CHAR),''),6,'0')"); // 特売Ｂ1㎏売価 全店特売(アンケート有)_商品.B売価_1㎏全店特売(アンケート無)_商品.B売価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM_PACK AS CHAR),''),6,'0')"); // 特売Ｂﾊﾟｯｸ売価 全店特売(アンケート有)_商品.B売価_パック全店特売(アンケート無)_商品.B売価_パック NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM AS CHAR),''),6,'0')"); // 特売Ｃ100ｇ売価 全店特売(アンケート有)_商品.C売価（100ｇ）全店特売(アンケート無)_商品.C売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_GENKAAM_1KG AS CHAR),''),6,'0')"); // 特売Ｃ1㎏売価 全店特売(アンケート有)_商品.C売価_1㎏全店特売(アンケート無)_商品.C売価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM_PACK AS CHAR),''),6,'0')"); // 特売Ｃﾊﾟｯｸ売価 全店特売(アンケート有)_商品.C売価_パック全店特売(アンケート無)_商品.C売価_パック NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM_100G AS CHAR),''),6,'0')"); // Ａ売価_100ｇ相当 全店特売(アンケート有)_商品.A売価_100ｇ相当全店特売(アンケート無)_商品.A売価_100ｇ相当 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM_100G AS CHAR),''),6,'0')"); // Ｂ売価_100ｇ相当 全店特売(アンケート有)_商品.B売価_100ｇ相当全店特売(アンケート無)_商品.B売価_100ｇ相当 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM_100G AS CHAR),''),6,'0')"); // Ｃ売価_100ｇ相当 全店特売(アンケート有)_商品.C売価_100ｇ相当全店特売(アンケート無)_商品.C売価_100ｇ相当 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_1KG AS CHAR),''),8,'0')"); // 1㎏原価 全店特売(アンケート有)_商品.原価_1㎏全店特売(アンケート無)_商品.原価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_PACK AS CHAR),''),8,'0')"); // P原価 全店特売(アンケート有)_商品.パック原価全店特売(アンケート無)_商品.パック原価 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_WRITUKBN AS CHAR),''),1,'0')"); // Ａ割引率 全店特売(アンケート有)_商品.A売価_割引率区分全店特売(アンケート無)_商品.A売価_割引率区分１：１割引 ２：2割引･･･ ９：9割引 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_WRITUKBN AS CHAR),''),1,'0')"); // Ｂ割引率 全店特売(アンケート有)_商品.B売価_割引率区分全店特売(アンケート無)_商品.B売価_割引率区分１：１割引 ２：2割引･･･ ９：9割引 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_WRITUKBN AS CHAR),''),1,'0')"); // Ｃ割引率 全店特売(アンケート有)_商品.C売価_割引率区分全店特売(アンケート無)_商品.C売価_割引率区分１：１割引 ２：2割引･･･ ９：9割引 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBYOTEISU AS CHAR),''),6,'0')"); // 予定数 全店特売(アンケート有)_商品.販売予定数全店特売(アンケート無)_商品.販売予定数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.YORIFLG AS CHAR),''),1,'0')"); // よりどりフラグ 全店特売(アンケート有)_商品.よりどりフラグ全店特売(アンケート無)_商品.よりどりフラグ０：通常 １：よりどり NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_A_BAIKAAN AS CHAR),''),6,'0')"); // ＢＭ１個売売価Ａ 全店特売(アンケート有)_商品.A売価_1個売り全店特売(アンケート無)_商品.A売価_1個売り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_B_BAIKAAN AS CHAR),''),6,'0')"); // ＢＭ１個売売価Ｂ 全店特売(アンケート有)_商品.B売価_1個売り全店特売(アンケート無)_商品.B売価_1個売り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_C_BAIKAAN AS CHAR),''),6,'0')"); // ＢＭ１個売売価Ｃ 全店特売(アンケート有)_商品.C売価_1個売り全店特売(アンケート無)_商品.C売価_1個売り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_TENSU AS CHAR),''),3,'0')"); // バンドル１点数 全店特売(アンケート有)_商品.点数_バンドル1全店特売(アンケート無)_商品.点数_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_A_BAIKAAN AS CHAR),''),6,'0')"); // バンドル１売価Ａ 全店特売(アンケート有)_商品.A売価_バンドル1全店特売(アンケート無)_商品.A売価_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_B_BAIKAAN AS CHAR),''),6,'0')"); // バンドル１売価Ｂ 全店特売(アンケート有)_商品.B売価_バンドル1全店特売(アンケート無)_商品.B売価_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_C_BAIKAAN AS CHAR),''),6,'0')"); // バンドル１売価Ｃ 全店特売(アンケート有)_商品.C売価_バンドル1全店特売(アンケート無)_商品.C売価_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_TENSU AS CHAR),''),3,'0')"); // バンドル２点数 全店特売(アンケート有)_商品.点数_バンドル2全店特売(アンケート無)_商品.点数_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_A_BAIKAAN AS CHAR),''),6,'0')"); // バンドル２売価Ａ 全店特売(アンケート有)_商品.A売価_バンドル2全店特売(アンケート無)_商品.A売価_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_B_BAIKAAN AS CHAR),''),6,'0')"); // バンドル２売価Ｂ 全店特売(アンケート有)_商品.B売価_バンドル2全店特売(アンケート無)_商品.B売価_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_C_BAIKAAN AS CHAR),''),6,'0')"); // バンドル２売価Ｃ 全店特売(アンケート有)_商品.C売価_バンドル2全店特売(アンケート無)_商品.C売価_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MEDAMAKBN AS CHAR),''),1,'0')"); // 目玉 全店特売(アンケート有)_商品.目玉区分全店特売(アンケート無)_商品.目玉区分１：大目玉 ２：目玉 ３：その他 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_POP,''),100,'　'),100)"); // ＰＯＰコメント 全店特売(アンケート有)_商品.POPコメント全店特売(アンケート無)_商品.POPコメント全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_TB,''),60,'　'),60)"); // 特売コメント 全店特売(アンケート有)_商品.特売コメント全店特売(アンケート無)_商品.特売コメント全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_HGW,''),100,'　'),100)"); // その他日替コメント 全店特売(アンケート有)_商品.その他日替わりコメント全店特売(アンケート無)_商品.その他日替わりコメント全角 NULLの時ALL全SP/桁数に満たない時

    // 制限
    // 全店特売(アンケート有)_商品.制限_先着人数(5桁)全店特売(アンケート有)_商品.制限_限定表現(20桁)全店特売(アンケート有)_商品.制限_一人当たり個数(3桁)全店特売(アンケート有)_商品.制限_一人当たり個数単位(10桁)
    // 全店特売(アンケート無)_商品.制限_先着人数(5桁)全店特売(アンケート無)_商品.制限_限定表現(20桁)全店特売(アンケート無)_商品.制限_一人当たり個数(3桁)全店特売(アンケート無)_商品.制限_一人当たり個数単位(10桁)
    // 先着人数部分のロジック
    // ◇制限_先着人数がNULLの場合 "先着"、制限_先着人数、"名様"を表示しない
    // ◇制限_先着人数がNOTNULLの場合 "先着"+制限_先着人数（前ゼロ、前スペースを省く）+"名様"
    // 制限一人当り部分のロジック
    // ◇制限_限定表現、制限_一人当り個数、制限_一人当り個数単位の全てがNULLの場合 制限_限定表現、制限_一人当り個数、制限_一人当り個数単位、"限り"を表示しない
    // ◇上記以外 制限_限定表現（右スペースカット）+制限_一人当り個数（前ゼロ前スペースカット）+制限_一人当り個数単位（右スペースカット）+"限り"を表示
    // NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(" + "COALESCE('先着'||trim(CAST(T1.SEGN_NINZU AS CHAR))||'名様', '')||"
        + "trim(COALESCE(T1.SEGN_GENTEI,''))||trim(COALESCE(CAST(T1.SEGN_1KOSU AS CHAR),''))||trim(COALESCE(T1.SEGN_1KOSUTNI,''))"
        + "|| case when length(trim(COALESCE(T1.SEGN_GENTEI,''))||trim(COALESCE(CAST(T1.SEGN_1KOSU AS CHAR),''))||trim(COALESCE(T1.SEGN_1KOSUTNI,''))) = 0 then '' else '限り' end" + ",58,'　'),58)");
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CUTTENFLG AS CHAR),''),1,'0')"); // カット店展開フラグ 全店特売(アンケート有)_商品.カット店展開フラグ全店特売(アンケート無)_商品.カット店展開フラグ０：展開する １：展開しない NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CHIRASFLG AS CHAR),''),1,'0')"); // チラシ未掲載フラグ 全店特売(アンケート有)_商品.チラシ未掲載全店特売(アンケート無)_商品.チラシ未掲載０：掲載(D) １：未掲載 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.PLUSNDFLG AS CHAR),''),1,'0')"); // ＰＬＵ未配信フラグ 全店特売(アンケート有)_商品.PLU配信フラグ全店特売(アンケート無)_商品.PLU配信フラグ０：配信(D) １：未配信 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HTGENBAIKAFLG AS CHAR),''),1,'0')"); // 発注原売価適用フラグ 全店特売(アンケート有)_商品発注原売価適用フラグ全店特売(アンケート無)_商品発注原売価適用フラグ０：適用する(D) １：適用しない
                                                                                // NULLの時ALL0/桁数に満たない時
    // 全品割引区分 全店特売(アンケート有)_商品.登録種別全店特売(アンケート無)_商品.登録種別を取得し、
    // ◇登録種別=01 1
    // ◇登録種別<>01 0
    // ０：通常 １：全割ダミー商品 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||case when T1.ADDSHUKBN = " + DefineReport.ValAddShuKbn.VAL1.getVal() + " then '1' else '0' end");
    sbSQL.append(" || LPAD(COALESCE(CAST(T1.NNSTDT AS CHAR), ''), 8, '0')	"); // 納品情報日付1 全店特売(アンケート有)_商品.催し納入期間_開始日全店特売(アンケート無)_商品.催し納入期間_開始日YYYYMMDD 初期値は'00000000'
                                                                                // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT2 AS CHAR), ''), 8, '0')	"); // 納品情報日付2
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT3 AS CHAR), ''), 8, '0')	"); // 納品情報日付3
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT4 AS CHAR), ''), 8, '0')	"); // 納品情報日付4
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT5 AS CHAR), ''), 8, '0')	"); // 納品情報日付5
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT6 AS CHAR), ''), 8, '0')	"); // 納品情報日付6
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT7 AS CHAR), ''), 8, '0')	"); // 納品情報日付7
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT8 AS CHAR), ''), 8, '0')	"); // 納品情報日付8
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT9 AS CHAR), ''), 8, '0')	"); // 納品情報日付9
                                                                            // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                            // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(CAL.DT10 AS CHAR), ''), 8, '0')	"); // 納品情報日付10
                                                                                // 全店特売(アンケート有)_商品.催し販売期間_終了日全店特売(アンケート有)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）全店特売(アンケート無)_商品.催し販売期間_終了日全店特売(アンケート無)_商品.催し納入期間_終了日のいずれか大きい方（ただし10日以上はカット）
                                                                                // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(T1.NNSTDT AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ1 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出０：販売なし
                                                                                                                // １：画面上、販売日にチェックがついている日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT2 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ2 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT3 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ3 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT4 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ4 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT5 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ5 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT6 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ6 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT7 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ7 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT8 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ8 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT9 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ9 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || case when CAST(CAL.DT10 AS signed) between T1.HBSTDT and T1.HBEDDT then 1 else 0 end	"); // 納品情報販売フラグ10 全店特売(アンケート有)_商品.販売期間より、算出全店特売(アンケート無)_商品.販売期間より、算出〃 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when T1.NNSTDT is not null then WK2.ADT1 end AS CHAR), ''), 3, '0')"); // 追加店1 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                     // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT2 is not null then WK2.ADT2 end AS CHAR), ''), 3, '0')"); // 追加店2 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT3 is not null then WK2.ADT3 end AS CHAR), ''), 3, '0')"); // 追加店3 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT4 is not null then WK2.ADT4 end AS CHAR), ''), 3, '0')"); // 追加店4 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT5 is not null then WK2.ADT5 end AS CHAR), ''), 3, '0')"); // 追加店5 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT6 is not null then WK2.ADT6 end AS CHAR), ''), 3, '0')"); // 追加店6 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT7 is not null then WK2.ADT7 end AS CHAR), ''), 3, '0')"); // 追加店7 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT8 is not null then WK2.ADT8 end AS CHAR), ''), 3, '0')"); // 追加店8 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT9 is not null then WK2.ADT9 end AS CHAR), ''), 3, '0')"); // 追加店9 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT10 is not null then WK2.ADT10 end AS CHAR), ''), 3, '0')"); // 追加店10 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=1)
                                                                                                                     // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || COALESCE(case when T1.NNSTDT is not null then WK2.RNK1 end, ' ')"); // 追加店ランク1 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                          // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT2 is not null then WK2.RNK2 end, ' ')"); // 追加店ランク2 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT3 is not null then WK2.RNK3 end, ' ')"); // 追加店ランク3 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT4 is not null then WK2.RNK4 end, ' ')"); // 追加店ランク4 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT5 is not null then WK2.RNK5 end, ' ')"); // 追加店ランク5 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT6 is not null then WK2.RNK6 end, ' ')"); // 追加店ランク6 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT7 is not null then WK2.RNK7 end, ' ')"); // 追加店ランク7 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT8 is not null then WK2.RNK8 end, ' ')"); // 追加店ランク8 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT9 is not null then WK2.RNK9 end, ' ')"); // 追加店ランク9 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                         // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || COALESCE(case when  CAL.DT10 is not null then WK2.RNK10 end, ' ')"); // 追加店ランク10 全店特売(アンケート有)_対象除外店.店ランク(対象除外フラグ=1)全店特売(アンケート無)_対象除外店.店ランク(対象除外フラグ=1)
                                                                                           // NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when T1.NNSTDT is not null then WK2.DLT1 end AS CHAR), ''), 3, '0')"); // 除外店1 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                     // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT2 is not null then WK2.DLT2 end AS CHAR), ''), 3, '0')"); // 除外店2 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT3 is not null then WK2.DLT3 end AS CHAR), ''), 3, '0')"); // 除外店3 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT4 is not null then WK2.DLT4 end AS CHAR), ''), 3, '0')"); // 除外店4 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT5 is not null then WK2.DLT5 end AS CHAR), ''), 3, '0')"); // 除外店5 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT6 is not null then WK2.DLT6 end AS CHAR), ''), 3, '0')"); // 除外店6 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT7 is not null then WK2.DLT7 end AS CHAR), ''), 3, '0')"); // 除外店7 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT8 is not null then WK2.DLT8 end AS CHAR), ''), 3, '0')"); // 除外店8 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT9 is not null then WK2.DLT9 end AS CHAR), ''), 3, '0')"); // 除外店9 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                   // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" || LPAD(COALESCE(CAST(case when CAL.DT10 is not null then WK2.DLT10 end AS CHAR), ''), 3, '0')"); // 除外店10 全店特売(アンケート有)_対象除外店.店コード(対象除外フラグ=2)全店特売(アンケート無)_対象除外店.店コード(対象除外フラグ=2)
                                                                                                                     // NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BINKBN AS CHAR),''),1,'0')"); // 便区分 全店特売(アンケート有)_商品.便区分全店特売(アンケート無)_商品.便区分１：１便 ２：２便 ３：３便 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BDENKBN AS CHAR),''),1,'0')"); // 別伝区分 全店特売(アンケート有)_商品.別伝区分全店特売(アンケート無)_商品.別伝区分０：通常(D) １～８：任意 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.WAPPNKBN AS CHAR),''),1,'0')"); // ワッペン区分 全店特売(アンケート有)_商品.ワッペン区分全店特売(アンケート無)_商品.ワッペン区分０：出さない １：発注数分 ３：１枚 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JUFLG AS CHAR),''),1,'0')"); // 事前打出区分 全店特売(アンケート有)_商品.事前打出フラグ全店特売(アンケート無)_商品.事前打出フラグ０：通常(D) ２：事前発注 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JUHTDT AS CHAR),''),8,'0')"); // 事前打出発注日 全店特売(アンケート有)_商品.事前打出日付全店特売(アンケート無)_商品.事前打出日付YYYYMMDD 初期値は'00000000' NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SHUDENFLG AS CHAR),''),1,'0')"); // 週次伝送区分 全店特売(アンケート有)_商品.週次仕入先伝送フラグ全店特売(アンケート無)_商品.週次仕入先伝送フラグ０：通常(D) １：週次伝送 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.POPCD AS CHAR),''),10,'0')"); // ＰＯＰコード 全店特売(アンケート有)_商品.POPコード全店特売(アンケート無)_商品.POPコード NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.POPSU AS CHAR),''),2,'0')"); // ＰＯＰ枚数 全店特売(アンケート有)_商品.POP枚数全店特売(アンケート無)_商品.POP枚数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.POPSZ,''),3,'0')"); // ＰＯＰサイズ 全店特売(アンケート有)_商品.POPサイズ全店特売(アンケート無)_商品.POPサイズSS△、S△△、M△△、L△△、LL△ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SHNSIZE,''),40,'　'),40)"); // 商品サイズ 全店特売(アンケート有)_商品.商品サイズ全店特売(アンケート無)_商品.商品サイズ NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SHNCOLOR,''),20,'　'),20)"); // 商品色 全店特売(アンケート有)_商品.商品色全店特売(アンケート無)_商品.商品色 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TENKAIKBN AS CHAR),''),1,'0')"); // ﾊﾟﾀｰﾝ種類 全店特売(アンケート有)_商品.展開方法全店特売(アンケート無)_商品.展開方法 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JSKPTNSYUKBN AS CHAR),''),1,'0')"); // 実績率PT数値 全店特売(アンケート有)_商品.実績率パタン数値全店特売(アンケート無)_商品.実績率パタン数値 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JSKPTNZNENWKBN AS CHAR),''),1,'0')"); // 実績率PT前年同週 全店特売(アンケート有)_商品.実績率パタン前年同週全店特売(アンケート無)_商品.実績率パタン前年同週 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JSKPTNZNENMKBN AS CHAR),''),1,'0')"); // 実績率PT前年同月 全店特売(アンケート有)_商品.実績率パタン前年同月全店特売(アンケート無)_商品.実績率パタン前年同月 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||LPAD(COALESCE(CAST(M1.ZEIKBN AS CHAR), ''), 1, '0')"); // 税区分 商品マスタ.税区分商品マスタ項目 ０：外税 １：内税 ２：非課税 ３：部門マスタに準拠 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||LPAD(COALESCE(CAST(M1.ZEIRTKBN AS CHAR), ''), 3, '0')"); // 税率区分 商品マスタ.税率区分商品マスタ項目 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||LPAD(COALESCE(CAST(M1.ZEIRTKBN_OLD AS CHAR), ''), 3, '0')"); // 旧税率区分 商品マスタ.旧税率区分商品マスタ項目 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||LPAD(COALESCE(CAST(M1.ZEIRTHENKODT AS CHAR), ''), 8, '0')"); // 税率変更日 商品マスタ.税率変更日商品マスタ項目 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + datalen + ", ' ') as REC ");
    sbSQL.append(" from " + szTableSHN + " T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN and T1.BMNCD = WK.BMNCD");
    // 本レコードは、バンドルが組まれている商品（A売価_バンドル1>0）のみ作成を行う。
    sbSQL.append(" and T1.BD1_A_BAIKAAN > 0");
    sbSQL.append(
        " inner join CAL on T1.MOYSKBN = CAL.MOYSKBN and T1.MOYSSTDT = CAL.MOYSSTDT and T1.MOYSRBAN = CAL.MOYSRBAN and T1.BMNCD = CAL.BMNCD and T1.KANRINO = CAL.KANRINO and T1.KANRIENO = CAL.KANRIENO and T1.KANRIENO = CAL.KANRIENO");
    sbSQL.append(
        " left join WK2 on T1.MOYSKBN = WK2.MOYSKBN and T1.MOYSSTDT = WK2.MOYSSTDT and T1.MOYSRBAN = WK2.MOYSRBAN and T1.BMNCD = WK2.BMNCD and T1.KANRINO = WK2.KANRINO and T1.KANRIENO = WK2.KANRIENO");
    sbSQL.append(" inner join INAMS.MSTSHN M1 on M1.SHNCD = T1.SHNCD and COALESCE(M1.UPDKBN, 0) <> 1");
    if (isTOKTG) {
      // 月締後登録データの絞り込みを行う
      sbSQL.append(" left join SENDKANRINO SKNO on T1.MOYSKBN = SKNO.MOYSKBN and T1.MOYSSTDT = SKNO.MOYSSTDT and T1.MOYSRBAN = SKNO.MOYSRBAN and T1.BMNCD = SKNO.BMNCD and T1.KANRINO = SKNO.KANRINO");
      sbSQL.append(" where SKNO.KANRINO is null or (SKNO.KANRINO is not null and SKNO.NOTOUTPUTFLG = 0 and T1.KANRIENO = SKNO.KANRIENO)");
    }
    sbSQL.append(" union all");
    sbSQL.append(" select distinct");
    sbSQL.append(" 3 as RNO");
    sbSQL.append(", T1.MOYSKBN || LPAD(T1.MOYSSTDT, 6, '0') || LPAD(T1.MOYSRBAN, 3, '0') as MOYCD");
    sbSQL.append(", T1.KANRINO");
    sbSQL.append(", T1.SHNCD");
    sbSQL.append(", T1.DAICD");
    sbSQL.append(", T1.CHUCD");
    sbSQL.append(", T1.NNSTDT");
    sbSQL.append(", RPAD('D3'");
    sbSQL.append(" || COALESCE(T1.MOYSKBN, 0)");
    sbSQL.append(" || LPAD(COALESCE(T1.MOYSSTDT, 0), 6, '0')");
    sbSQL.append(" || LPAD(COALESCE(T1.MOYSRBAN, 0), 3, '0')");
    sbSQL.append(" || LPAD(COALESCE(T1.KANRINO, 0), 4, '0')");
    sbSQL.append(" || LPAD(COALESCE(T1.KANRINO, 0), 4, '0')");
    sbSQL.append(" || LPAD(COALESCE(T2.NNDT, 0), 8, '0')");
    sbSQL.append(" || LPAD(COALESCE(T2.HTASU, 0), 6, '0')");
    sbSQL.append(" || LPAD(COALESCE(T2.PTNNO, 0), 9, '0')");
    sbSQL.append(" || LPAD(COALESCE(T2.TSEIKBN, 0), 1, '0'), 1060, ' ') as REC");
    sbSQL.append(" from " + szTableSHN + " T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN and T1.BMNCD = WK.BMNCD");
    // 本レコードは、バンドルが組まれている商品（A売価_バンドル1>0）のみ作成を行う。
    sbSQL.append(" and T1.BD1_A_BAIKAAN > 0");
    sbSQL.append(" inner join " + szTableNNDT
        + " T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T1.BMNCD = T2.BMNCD and T1.KANRINO = T2.KANRINO and T1.KANRIENO = T2.KANRIENO");
    if (isTOKTG) {
      // 月締後登録データの絞り込みを行う
      sbSQL.append(" left join SENDKANRINO SKNO on T1.MOYSKBN = SKNO.MOYSKBN and T1.MOYSSTDT = SKNO.MOYSSTDT and T1.MOYSRBAN = SKNO.MOYSRBAN and T1.BMNCD = SKNO.BMNCD and T1.KANRINO = SKNO.KANRINO");
      sbSQL.append(" where SKNO.KANRINO is null or (SKNO.KANRINO is not null and SKNO.NOTOUTPUTFLG = 0 and T1.KANRIENO = SKNO.KANRIENO)");
    }
    sbSQL.append(")");
    return sbSQL.toString();
  }


  public String createCommandCSV1_TG(String userId, JSONArray dataArray, boolean containHeadSql) {
    FtpFileInfo cfi = FtpFileInfo.CSV1_TG;

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
    if (containHeadSql) {
      sbSQL.append(" " + super.createCmnSqlFTPH1(cfi.getFnm(), userId, cfi.getLen(), true));
      sbSQL.append(" union all ");
    }
    sbSQL.append(" select distinct 1 as RNO");
    sbSQL.append(" ,rpad('D1'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SHUNO AS CHAR),''),4,'0')"); // 週№ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.MOYKN,''),40,'　'),40)"); // 催し名称（漢字） NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.MOYAN,''),20,' '),20)"); // 催し名称（カナ） NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NENMATKBN AS CHAR),''),1,'0')"); // 年末区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTDT AS CHAR),''),8,'0')"); // 販売開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBEDDT AS CHAR),''),8,'0')"); // 販売終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNSTDT AS CHAR),''),8,'0')"); // 納入開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNEDDT AS CHAR),''),8,'0')"); // 納入終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNSTDT_TGF AS CHAR),''),8,'0')"); // 納入開始日_全特生鮮 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNEDDT_TGF AS CHAR),''),8,'0')"); // 納入終了日_全特生鮮 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.PLUSDDT AS CHAR),''),8,'0')"); // PLU配信日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.PLUSFLG AS CHAR),''),1,'0')"); // PLU配信済フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 2 as RNO");
    sbSQL.append(" ,rpad('D2'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBOKUREFLG AS CHAR),''),1,'0')"); // 販売日1日遅許可フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GTSIMEDT AS CHAR),''),8,'0')"); // 月締日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GTSIMEFLG AS CHAR),''),1,'0')"); // 月締フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.LSIMEDT AS CHAR),''),8,'0')"); // 最終締日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QAYYYYMM AS CHAR),''),6,'0')"); // アンケート月度 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QAENO AS CHAR),''),2,'0')"); // アンケート月度枝番 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QACREDT AS CHAR),''),8,'0')"); // アンケート作成日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QARCREDT AS CHAR),''),8,'0')"); // アンケート再作成日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JLSTCREFLG AS CHAR),''),1,'0')"); // 事前発注リスト作成済フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HNCTLFLG AS CHAR),''),1,'0')"); // 本部コントロールフラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TPNG1FLG AS CHAR),''),1,'0')"); // 店不採用禁止フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TPNG2FLG AS CHAR),''),1,'0')"); // 店売価選択禁止フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TPNG3FLG AS CHAR),''),1,'0')"); // 店商品選択禁止フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SIMEFLG1_LD AS CHAR),''),1,'0')"); // 仮締フラグ_リーダー店 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SIMEFLG2_LD AS CHAR),''),1,'0')"); // 本締フラグ_リーダー店 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SIMEFLG_MB AS CHAR),''),1,'0')"); // 本締フラグ_各店 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QADEVSTDT AS CHAR),''),8,'0')"); // アンケート取込開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_KHN T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 3 as RNO");
    sbSQL.append(" ,rpad('D3'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||left(rpad(COALESCE(T1.BMNCD,''),3,'　'),3)"); // 部門 NULLの時－/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.KANRINO,''),4,'　'),4)"); // 管理番号 NULLの時－/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.KANRIENO,''),2,'　'),2)"); // 枝番 NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.ADDSHUKBN AS CHAR),''),2,'0')"); // 登録種別 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSLIDEFLG AS CHAR),''),1,'0')"); // 1日遅スライド_販売 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NHSLIDEFLG AS CHAR),''),1,'0')"); // 1日遅スライド_納品 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BYCD AS CHAR),''),7,'0')"); // BYコード NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.SHNCD,''),14,'0')"); // 商品コード NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.PARNO,''),3,' '),3)"); // 親No NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CHLDNO AS CHAR),''),2,'0')"); // 子No NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HIGAWRFLG AS CHAR),''),1,'0')"); // 日替フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTDT AS CHAR),''),8,'0')"); // 販売期間_開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBEDDT AS CHAR),''),8,'0')"); // 販売期間_終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNSTDT AS CHAR),''),8,'0')"); // 納入期間_開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NNEDDT AS CHAR),''),8,'0')"); // 納入期間_終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CHIRASFLG AS CHAR),''),1,'0')"); // チラシ未掲載 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD AS CHAR),''),3,'0')"); // 対象店ランク NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBYOTEISU AS CHAR),''),6,'0')"); // 販売予定数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_MAE AS CHAR),''),8,'0')"); // 原価_特売事前 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_ATO AS CHAR),''),8,'0')"); // 原価_特売追加 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM AS CHAR),''),6,'0')"); // A売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM AS CHAR),''),6,'0')"); // B売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM AS CHAR),''),6,'0')"); // C売価（100ｇ） NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.IRISU AS CHAR),''),3,'0')"); // 入数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HTGENBAIKAFLG AS CHAR),''),1,'0')"); // 発注原売価適用フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_WRITUKBN AS CHAR),''),1,'0')"); // A売価_割引率区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_WRITUKBN AS CHAR),''),1,'0')"); // B売価_割引率区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_WRITUKBN AS CHAR),''),1,'0')"); // C売価_割引率区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TKANPLUKBN AS CHAR),''),1,'0')"); // 定貫PLU・不定貫区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_GENKAAM_1KG AS CHAR),''),6,'0')"); // A売価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_GENKAAM_1KG AS CHAR),''),6,'0')"); // B売価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_GENKAAM_1KG AS CHAR),''),6,'0')"); // C売価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_PACK AS CHAR),''),8,'0')"); // パック原価 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM_PACK AS CHAR),''),6,'0')"); // A売価_パック NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM_PACK AS CHAR),''),6,'0')"); // B売価_パック NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM_PACK AS CHAR),''),6,'0')"); // C売価_パック NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.A_BAIKAAM_100G AS CHAR),''),6,'0')"); // A売価_100ｇ相当 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.B_BAIKAAM_100G AS CHAR),''),6,'0')"); // B売価_100ｇ相当 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.C_BAIKAAM_100G AS CHAR),''),6,'0')"); // C売価_100ｇ相当 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_1KG AS CHAR),''),8,'0')"); // 原価_1㎏ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.PLUSNDFLG AS CHAR),''),1,'0')"); // PLU配信フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TENKAIKBN AS CHAR),''),1,'0')"); // 展開方法 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JSKPTNSYUKBN AS CHAR),''),1,'0')"); // 実績率パタン数値 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JSKPTNZNENMKBN AS CHAR),''),1,'0')"); // 実績率パタン前年同月 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JSKPTNZNENWKBN AS CHAR),''),1,'0')"); // 実績率パタン前年同週 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.DAICD AS CHAR),''),2,'0')"); // 大分類 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CHUCD AS CHAR),''),2,'0')"); // 中分類 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SANCHIKN,''),40,'　'),40)"); // 産地 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.MAKERKN,''),28,'　'),28)"); // メーカー名 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.POPKN,''),40,'　'),40)"); // POP名称 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.KIKKN,''),46,'　'),46)"); // 規格名称 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SEGN_NINZU AS CHAR),''),5,'0')"); // 制限_先着人数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SEGN_GENTEI,''),20,'　'),20)"); // 制限_限定表現 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SEGN_1KOSU AS CHAR),''),3,'0')"); // 制限_一人当たり個数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SEGN_1KOSUTNI,''),10,'　'),10)"); // 制限_一人当たり個数単位 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.YORIFLG AS CHAR),''),1,'0')"); // よりどりフラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_TENSU AS CHAR),''),3,'0')"); // 点数_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_TENSU AS CHAR),''),3,'0')"); // 点数_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_A_BAIKAAN AS CHAR),''),6,'0')"); // A売価_1個売り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_A_BAIKAAN AS CHAR),''),6,'0')"); // A売価_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_A_BAIKAAN AS CHAR),''),6,'0')"); // A売価_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_B_BAIKAAN AS CHAR),''),6,'0')"); // B売価_1個売り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_B_BAIKAAN AS CHAR),''),6,'0')"); // B売価_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_B_BAIKAAN AS CHAR),''),6,'0')"); // B売価_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_C_BAIKAAN AS CHAR),''),6,'0')"); // C売価_1個売り NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_C_BAIKAAN AS CHAR),''),6,'0')"); // C売価_バンドル1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_C_BAIKAAN AS CHAR),''),6,'0')"); // C売価_バンドル2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MEDAMAKBN AS CHAR),''),1,'0')"); // 目玉区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.POPCD AS CHAR),''),10,'0')"); // POPコード NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.POPSZ,''),3,' '),3)"); // POPサイズ NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.POPSU AS CHAR),''),2,'0')"); // POP枚数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SHNSIZE,''),40,'　'),40)"); // 商品サイズ NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SHNCOLOR,''),20,'　'),20)"); // 商品色 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_HGW,''),100,'　'),100)"); // その他日替わりコメント NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_POP,''),100,'　'),100)"); // POPコメント NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.NAMANETUKBN AS CHAR),''),1,'0')"); // 生食加熱区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KAITOFLG AS CHAR),''),1,'0')"); // 解凍フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.YOSHOKUFLG AS CHAR),''),1,'0')"); // 養殖フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JUFLG AS CHAR),''),1,'0')"); // 事前打出フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JUHTDT AS CHAR),''),8,'0')"); // 事前打出日付 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.COMMENT_TB,''),60,'　'),60)"); // 特売コメント NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CUTTENFLG AS CHAR),''),1,'0')"); // カット店展開フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BINKBN AS CHAR),''),1,'0')"); // 便区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BDENKBN AS CHAR),''),1,'0')"); // 別伝区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.WAPPNKBN AS CHAR),''),1,'0')"); // ワッペン区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SHUDENFLG AS CHAR),''),1,'0')"); // 週次仕入先伝送フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.TENRANK_ARR,''),400,' '),400)"); // 店ランク配列 NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GTSIMECHGKBN AS CHAR),''),1,'0')"); // 月締変更理由 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GTSIMEOKFLG AS CHAR),''),1,'0')"); // 月締変更許可フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JLSTCREDT AS CHAR),''),8,'0')"); // 事前発注リスト出力日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.JHTSUINDT AS CHAR),''),8,'0')"); // 事前発注数量取込日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.WEEKHTDT AS CHAR),''),8,'0')"); // 週間発注処理日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MYOSHBSTDT AS CHAR),''),8,'0')"); // 催し販売開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MYOSHBEDDT AS CHAR),''),8,'0')"); // 催し販売終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MYOSNNSTDT AS CHAR),''),8,'0')"); // 催し納入開始日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MYOSNNEDDT AS CHAR),''),8,'0')"); // 催し納入終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_SHN T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    // .D3、D8は月締め後新規商品（全特（ア有）_商品.月締変更理由=1）で未許可（月締変更許可フラグ=0）の商品は対象外
    sbSQL.append(" and not(T1.GTSIMECHGKBN=1 and T1.GTSIMEOKFLG=0)");
    // .D3、D8はA売価_バンドル1>0の商品
    sbSQL.append(" and T1.BD1_A_BAIKAAN > 0");
    sbSQL.append(" inner join INAMS.MSTSHN T2 on T1.SHNCD = T2.SHNCD and COALESCE(T2.UPDKBN, 0) = 0");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 4 as RNO");
    sbSQL.append(" ,rpad('D4'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(T1.TENGPCD,''),3,'0')"); // 店グループ NULLの時－/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.TENGPKN,''),40,'　'),40)"); // 店グループ名称 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KYOSEIFLG AS CHAR),''),1,'0')"); // 強制グループフラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QASYUKBN AS CHAR),''),1,'0')"); // アンケート種類 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QACREDT_K AS CHAR),''),8,'0')"); // アンケート作成日_強制 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QARCREDT_K AS CHAR),''),8,'0')"); // アンケート再作成日_強制 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_TENGP T1");
    sbSQL.append(" inner join WK on T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN"); // d.D4のみ更新区分=1のレコードも対象とする。
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 5 as RNO");
    sbSQL.append(" ,rpad('D5'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(T1.TENCD,''),3,'0')"); // 店コード NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.KYOSEIFLG,''),1,'0')"); // 強制グループフラグ NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TENGPCD AS CHAR),''),3,'0')"); // 店グループ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.LDTENKBN AS CHAR),''),1,'0')"); // リーダー店区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_TEN T1");
    sbSQL.append(" inner join WK on T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 6 as RNO");
    sbSQL.append(" ,rpad('D6'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(T1.TENGPCD,''),3,'0')"); // 店グループ NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KYOSEIFLG AS CHAR),''),1,'0')"); // 強制グループフラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.QASYUKBN AS CHAR),''),1,'0')"); // アンケート種類 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.LDSYFLG AS CHAR),''),1,'0')"); // リーダー店採用フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.URIASELKBN AS CHAR),''),1,'0')"); // 売価一括選択 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.SCHANSFLG AS CHAR),''),1,'0')"); // スケジュール回答フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.ITMANSFLG AS CHAR),''),1,'0')"); // アイテム回答フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CSVSEQNO AS CHAR),''),12,'0')"); // CSV連番 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_QAGP T1");
    sbSQL.append(" inner join WK on T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 7 as RNO");
    sbSQL.append(" ,rpad('D7'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(T1.TENCD,''),3,'0')"); // 店コード NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.KYOSEIFLG,''),1,'0')"); // 強制グループフラグ NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.TENGPCD AS CHAR),''),3,'0')"); // 店グループ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.LDTENKBN AS CHAR),''),1,'0')"); // リーダー店区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MBSYFLG AS CHAR),''),1,'0')"); // 各店採用フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTRTFLG AS CHAR),''),1,'0')"); // 販売開始フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.MBANSFLG AS CHAR),''),1,'0')"); // 各店回答フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CSVSEQNO AS CHAR),''),12,'0')"); // CSV連番 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_QATEN T1");
    sbSQL.append(" inner join WK on T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 8 as RNO");
    sbSQL.append(" ,rpad('D8'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(T1.TENGPCD,''),3,'0')"); // 店グループ NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.BMNCD,''),3,'0')"); // 部門 NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.KANRINO,''),4,'0')"); // 管理番号 NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(T1.KANRIENO,''),2,'0')"); // 枝番 NULLの時－/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.URISELKBN AS CHAR),''),1,'0')"); // 売価選択 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.ITMSELKBN AS CHAR),''),1,'0')"); // 商品選択 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.URICHGAM1 AS CHAR),''),6,'0')"); // 売価差替1 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.URICHGAM2 AS CHAR),''),6,'0')"); // 売価差替2 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.URICHGAM3 AS CHAR),''),6,'0')"); // 売価差替3 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.CSVSEQNO AS CHAR),''),12,'0')"); // CSV連番 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKTG_QASHN T1");
    sbSQL.append(" inner join WK on T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(
        " inner join INATK.TOKTG_SHN T2 on T2.UPDKBN = 0 and T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T1.BMNCD = T2.BMNCD and T1.KANRINO = T2.KANRINO and T1.KANRIENO = T2.KANRIENO");
    // .D3、D8は月締め後新規商品（全特（ア有）_商品.月締変更理由=1）で未許可（月締変更許可フラグ=0）の商品は対象外
    sbSQL.append(" and not(T2.GTSIMECHGKBN=1 and T2.GTSIMEOKFLG=0)");
    // .D3、D8はA売価_バンドル1>0の商品
    sbSQL.append(" and T2.BD1_A_BAIKAAN > 0");
    sbSQL.append(") order by RNO, REC");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  public String createCommandCSV1_SP(String userId, JSONArray dataArray, boolean containHeadSql) {
    FtpFileInfo cfi = FtpFileInfo.CSV1_SP;

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
    if (containHeadSql) {
      sbSQL.append(" select ");
      sbSQL.append(" 0 as RNO,0 as MOYCD,");
      sbSQL.append(
          " rpad('H1'||left(rpad('" + cfi.getFnm() + "', 8, ' '),8)||left(rpad('" + userId + "', 20, ' '),20)||to_char(current timestamp,'YYYYMMDDHH24MISS'), " + cfi.getDataLen() + ", ' ')  as REC");
      sbSQL.append(" from sysibm.sysdummy1");
      sbSQL.append(" union all ");
    }
    sbSQL.append(" select distinct 1 as RNO,T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0') as MOYCD");
    sbSQL.append(" ,rpad('D1'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad('',3,'0')"); // 店グループ№ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||rpad('',40,'　')"); // 店グループ№名称 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 強制フラグ ０：通常 １：強制 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 販売開始フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||rpad('',400,' ')"); // 対象店舗フラグ NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 一日遅許可フラグ NULLの時/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    sbSQL.append(" union all ");
    sbSQL.append(" select distinct 2 as RNO,T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0') as MOYCD");
    sbSQL.append(" ,rpad('D2'");
    sbSQL.append(" ||T1.MOYSKBN||lpad(T1.MOYSSTDT,6,'0')||lpad(T1.MOYSRBAN,3,'0')"); // 催しコード
    sbSQL.append(" ||lpad(COALESCE(T1.BMNCD,''),3,'0')"); // 部門 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',3,'0')"); // 店グループ№ ALL0 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 強制フラグ ０：通常 １：強制 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 販売開始フラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 日替区分 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBSTDT AS CHAR),''),8,'0')"); // 販売期間_開始日 ０：通常 １：強制 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.HBEDDT AS CHAR),''),8,'0')"); // 販売期間_終了日 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_TENSU AS CHAR),''),3,'0')"); // バンドル１点数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_TENSU AS CHAR),''),3,'0')"); // バンドル２点数 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_A_BAIKAAN AS CHAR),''),6,'0')"); // １個売売価Ａ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_B_BAIKAAN AS CHAR),''),6,'0')"); // １個売売価Ｂ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.KO_C_BAIKAAN AS CHAR),''),6,'0')"); // １個売売価Ｃ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_A_BAIKAAN AS CHAR),''),6,'0')"); // バンドル１売価Ａ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_B_BAIKAAN AS CHAR),''),6,'0')"); // バンドル１売価Ｂ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD1_C_BAIKAAN AS CHAR),''),6,'0')"); // バンドル１売価Ｃ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',6,'0')"); // バンドル１差替売価 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_A_BAIKAAN AS CHAR),''),6,'0')"); // バンドル２売価Ａ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_B_BAIKAAN AS CHAR),''),6,'0')"); // バンドル２売価Ｂ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.BD2_C_BAIKAAN AS CHAR),''),6,'0')"); // バンドル２売価Ｃ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad('',6,'0')"); // バンドル２差替売価 NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.YORIFLG AS CHAR),''),1,'0')"); // よりどりフラグ NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.POPKN,''),40,'　'),40)"); // ＰＯＰ名称(漢字) 全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.MAKERKN,''),28,'　'),28)"); // メーカー名(漢字) 全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.KIKKN,''),46,'　'),46)"); // 規格名称 全角 NULLの時ALL全SP/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.SHNCD,''),14,' '),14)"); // 商品コード NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.GENKAAM_MAE AS CHAR),''),8,'0')"); // 特売事前原価 不定貫は１kg原価(03/31 JCC) NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD_A AS CHAR),''),3,'0')"); // 対象店Ａ売価ランク NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD_B AS CHAR),''),3,'0')"); // 対象店Ｂ売価ランク NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||lpad(COALESCE(CAST(T1.RANKNO_ADD_C AS CHAR),''),3,'0')"); // 対象店Ｃ売価ランク NULLの時ALL0/桁数に満たない時
    sbSQL.append(" ||left(rpad(COALESCE(T1.TENRANK_ARR,''),400,' '),400)"); // 対象店舗 店400のｲﾝﾃﾞｯｸｽ 扱い店にＡ～Ｄがセットされている。 ブランクは非対象 NULLの時ALL半SP/桁数に満たない時
    sbSQL.append(" ||lpad('',1,'0')"); // 一日遅スライド_販売 NULLの時/桁数に満たない時
    sbSQL.append(" , " + cfi.getDataLen() + ", ' ') as REC ");
    sbSQL.append(" from INATK.TOKSP_SHN T1");
    sbSQL.append(" inner join WK on T1.UPDKBN = 0 and T1.MOYSKBN = WK.MOYSKBN and T1.MOYSSTDT = WK.MOYSSTDT and T1.MOYSRBAN = WK.MOYSRBAN");
    // 本レコードは、バンドルが組まれている商品（A売価_バンドル1>0）のみ作成を行う。
    sbSQL.append(" and T1.BD1_A_BAIKAAN > 0");
    sbSQL.append(") order by MOYCD, RNO, REC");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  public String createCommandCSV2(String userId, JSONArray dataArray, boolean containHeadSql) {
    FtpFileInfo cfi = FtpFileInfo.CSV2;

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
    if (containHeadSql) {
      sbSQL.append(" " + super.createCmnSqlFTPH1(cfi.getFnm(), userId, cfi.getLen(), true));
      sbSQL.append(" union all ");
    }
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
    sbSQL.append(") order by RNO, REC");

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

    // 共通箇所設定
    createCmnOutput(jad);
  }

  /** FTPファイル情報() */
  public enum FtpFileInfo implements FtpFileInfoInterface {
    /** B/M CSV出力 */
    CSV1_TG(1, "btn_csv1", "B/M CSV出力", "MDCR018", 1281),
    /** B/M CSV出力 */
    CSV1_SP(2, "btn_csv1", "B/M CSV出力", "MDCR018", 658),
    /** POP原稿 CSV出力 */
    CSV2(3, "btn_csv2", "POP原稿 CSV出力", "MDCR017", 46),
    /** チェックリスト 管理NO順 */
    CHK1(4, "btn_checklist1", "管理NO順", "MDCR002", 1062),
    /** チェックリスト 商品コード順 */
    CHK2(5, "btn_checklist2", "商品コード順", "MDCR004", 1062),
    /** チェックリスト 販促用 */
    CHK3(6, "btn_checklist3", "販促用チェックリスト", "MDCR006", 187),
    /** チェックリスト 納入期間 */
    CHK4(7, "btn_checklist4", "納入期間順", "MDCR003", 1062),
    /** チェックリスト 分類順 */
    CHK5(8, "btn_checklist5", "分類順", "MDCR005", 1062),
    /** チェックリスト 週間特売原稿 */
    CHK6(9, "btn_checklist6", "週間特売原稿", "MDCR001", 1062),;

    private final Integer no;
    private final String bid;
    private final String bnm;
    private final String fnm;
    private final Integer len;

    /** 初期化 */
    private FtpFileInfo(Integer no, String bid, String bnm, String fnm, Integer len) {
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
    public String getBnm() {
      return bnm;
    }

    /** @return bnm ファイル名 */
    @Override
    public String getFnm() {
      return fnm;
    }

    /** @return len レコード長 */
    @Override
    public Integer getLen() {
      return len;
    }

    /** @return len レコード長(改行コード桁除外) */
    public Integer getDataLen() {
      return len - LEN_NEW_LINE_CODE;
    }

  }
}
