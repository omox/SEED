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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTM002Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTM002Dao(String JNDIname) {
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

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szShuno = getMap().get("SHUNO"); // 入力商品コード
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

    // 新規
    if (isNew) {
      sbSQL.append(" select ");
      sbSQL.append("  T2.SHUNO  as SHUNO"); // F1 : 週№
      sbSQL.append(" ,null as TSHUFLG"); // F2 : 特別週フラグ
      sbSQL.append(" ,null as UPDKBN"); // F3 : 更新区分
      sbSQL.append(" ,null as SENDFLG"); // F4 : 送信フラグ
      sbSQL.append(" ,null as OPERATOR"); // F5 : オペレータ
      sbSQL.append(" ,null as ADDDT"); // F6 : 登録日
      sbSQL.append(" ,null as UPDDT"); // F7 : 更新日
      sbSQL.append(" ,null as HDN_UPDDT"); // F8:更新日時
      sbSQL.append(" from INAAD.SYSSHORIDT T1 ");
      sbSQL.append(" left outer join INAAD.SYSSHUNO T2 on DATE_FORMAT(DATE_FORMAT(T1.SHORIDT, '%Y%m%d') + INTERVAL 1 YEAR, '%Y%m%d') between T2.STARTDT and T2.ENDDT");
      sbSQL.append(" where T1.UPDKBN = 0");
      // 変更/参照
    } else {
      sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
      sbSQL.append(" select ");
      sbSQL.append("  T1.SHUNO as SHUNO"); // F1 : 週№
      sbSQL.append(" ,T1.TSHUFLG as TSHUFLG"); // F2 : 特別週フラグ
      sbSQL.append(" ,T1.UPDKBN as UPDKBN"); // F3 : 更新区分
      sbSQL.append(" ,T1.SENDFLG as SENDFLG"); // F4 : 送信フラグ
      sbSQL.append(" ,T1.OPERATOR as OPERATOR"); // F5 : オペレータ
      sbSQL.append(" ,COALESCE(DATE_FORMAT(T1.ADDDT, '%y/%m/%d'),'__/__/__') as ADDDT"); // F6 : 登録日
      sbSQL.append(" ,COALESCE(DATE_FORMAT(T1.UPDDT, '%y/%m/%d'),'__/__/__') as UPDDT"); // F7 : 更新日
      sbSQL.append(" ,DATE_FORMAT(T1.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F8:更新日時
      sbSQL.append(" ,CASE WHEN T1.TSHUFLG <> '1' AND T2.SHUNO IS NOT NULL THEN");
      sbSQL
          .append(" RIGHT('0000'||T2.SHUNO,4)||'-'||DATE_FORMAT(DATE_FORMAT(T2.STARTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.STARTDT, '%Y%m%d')))");
      sbSQL.append("||'～'||DATE_FORMAT(DATE_FORMAT(T2.ENDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.ENDDT, '%Y%m%d')))");
      sbSQL.append(" ELSE RIGHT ('0000' || T1.SHUNO, 4) END TXT "); // F9:TEXT
      sbSQL.append(" from INATK.TOKMOYSYU T1 left join INAAD.SYSSHUNO T2 ON T1.SHUNO=T2.SHUNO ");
      sbSQL.append(" where T1.SHUNO = " + szShuno + " and COALESCE(T1.UPDKBN, 0) <> 1 ");
    }

    // オプション情報設定
    JSONObject option = new JSONObject();
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  public String createCommandcheck(int count) {
    StringBuffer sqlcommand = new StringBuffer();



    sqlcommand.append(" with WK as (select");
    sqlcommand.append(" cast(? as SIGNED) as MOYSKBN");
    sqlcommand.append(" , cast(? as SIGNED) as MOYSSTDT");
    sqlcommand.append(" , cast(? as SIGNED) as MOYSRBAN");
    for (int i = 1; i <= count; i++) {
      sqlcommand.append(" , cast(? as SIGNED) as BMNCD" + i);
    }
    sqlcommand.append(" from (SELECT 1 AS DUMMY) DUMMY) ");
    sqlcommand.append(" SELECT SUM(CNT) as VALUE FROM (");
    sqlcommand.append(" SELECT COUNT(*) AS CNT");
    sqlcommand.append(" FROM INATK.TOKTG_SHN as T1");
    sqlcommand.append(" inner join WK");
    sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") WHERE");
    sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD  IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") UNION ALL");

    sqlcommand.append(" SELECT COUNT(*) AS CNT");
    sqlcommand.append(" FROM INATK.TOKSP_SHN as T1");
    sqlcommand.append(" inner join WK");
    sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") WHERE");
    sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD  IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") UNION ALL");

    sqlcommand.append(" SELECT COUNT(*) AS CNT");
    sqlcommand.append(" FROM INATK.TOKMM_SHN as T1");
    sqlcommand.append(" inner join WK");
    sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") WHERE");
    sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD  IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") UNION ALL");

    sqlcommand.append(" SELECT COUNT(*) AS CNT");
    sqlcommand.append(" FROM INATK.TOKBT_KKK as T1");
    sqlcommand.append(" inner join WK");
    sqlcommand.append(" on T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") WHERE");
    sqlcommand.append(" T1.MOYSKBN = WK.MOYSKBN");
    sqlcommand.append(" and T1.MOYSSTDT = WK.MOYSSTDT");
    sqlcommand.append(" and T1.MOYSRBAN = WK.MOYSRBAN");
    sqlcommand.append(" and T1.BMNCD  IN (WK.BMNCD1");
    for (int i = 2; i <= count; i++) {
      sqlcommand.append(" ,WK.BMNCD" + i);
    }
    sqlcommand.append(") )MT1");



    return sqlcommand.toString();

  }


  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONObject createSqlSelTOKMOYCD(HashMap<String, String> map) {
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szSelShuno = map.get("SEL_SHUNO"); // 検索週№
    String szMoysstdt = map.get("MOYSSTDT"); // 催し開始日

    String key = map.get("KEY"); // SQLタイプ
    String reportno = map.get("REPORT"); // ﾚﾎﾟｰﾄ


    boolean isOtherM = DefineReport.ID_PAGE_TM003.equals(reportno) || DefineReport.ID_PAGE_TM004.equals(reportno);

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> param = new ArrayList<>();

    // 共通SQL取得:他テーブル使用状況
    String col_chk_use = "";
    ArrayList<String> chk_use_param = new ArrayList<>();
    if (ValKbn10002.VAL0.getVal().equals(szMoyskbn) || ValKbn10002.VAL1.getVal().equals(szMoyskbn) || (!isOtherM && ValKbn10002.VAL2.getVal().equals(szMoyskbn))
        || ValKbn10002.VAL3.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T2.CNT_FLG, 0)+COALESCE(T4.CNT_FLG, 0)";

      // 特売アンケート有商品
      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKTG_SHN T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
      // 特売アンケート無商品
      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKSP_SHN T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T4 on T1.MOYSKBN = T4.MOYSKBN and T1.MOYSSTDT = T4.MOYSSTDT and T1.MOYSRBAN = T4.MOYSRBAN");
    }
    // 催し送信
    if (ValKbn10002.VAL1.getVal().equals(szMoyskbn) || ValKbn10002.VAL2.getVal().equals(szMoyskbn) || ValKbn10002.VAL3.getVal().equals(szMoyskbn) || ValKbn10002.VAL5.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T5.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKCP_BMN T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T5 on T1.MOYSKBN = T5.MOYSKBN and T1.MOYSSTDT = T5.MOYSSTDT and T1.MOYSRBAN = T5.MOYSRBAN");
    }
    // BM催し送信
    if (ValKbn10002.VAL1.getVal().equals(szMoyskbn) || ValKbn10002.VAL2.getVal().equals(szMoyskbn) || ValKbn10002.VAL3.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T6.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKBM T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T6 on T1.MOYSKBN = T6.MOYSKBN and T1.MOYSSTDT = T6.MOYSSTDT and T1.MOYSRBAN = T6.MOYSRBAN");
    }
    // 分類割引_企画
    if (ValKbn10002.VAL1.getVal().equals(szMoyskbn) || (!isOtherM && ValKbn10002.VAL2.getVal().equals(szMoyskbn)) || ValKbn10002.VAL3.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T7.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKBT_KKK T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T7 on T1.MOYSKBN = T7.MOYSKBN and T1.MOYSSTDT = T7.MOYSSTDT and T1.MOYSRBAN = T7.MOYSRBAN");
    }
    // 生活応援商品
    if (ValKbn10002.VAL5.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T8.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKSO_SHN T1");
      sbSQL.append("  where nvl(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T8 on T1.MOYSKBN = T8.MOYSKBN and T1.MOYSSTDT = T8.MOYSSTDT and T1.MOYSRBAN = T8.MOYSRBAN");
    }
    // 月間山積商品
    if (ValKbn10002.VAL7.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T9.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKGY_SHN T1");
      sbSQL.append("  where nvl(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T9 on T1.MOYSKBN = T9.MOYSKBN and T1.MOYSSTDT = T9.MOYSSTDT and T1.MOYSRBAN = T9.MOYSRBAN");
    }
    // 店舗アンケート付き送付け_催し
    if (ValKbn10002.VAL8.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T10.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKQJU_MOY T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T10 on T1.MOYSKBN = T10.MOYSKBN and T1.MOYSSTDT = T10.MOYSSTDT and T1.MOYSRBAN = T10.MOYSRBAN");
    }
    // 事前打出し商品
    if (ValKbn10002.VAL9.getVal().equals(szMoyskbn)) {
      col_chk_use += "+COALESCE(T11.CNT_FLG, 0)";

      sbSQL.append(" left outer join (");
      sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN,count(MOYSKBN) as CNT_FLG");
      sbSQL.append("  from INATK.TOKJU_SHN T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szMoysstdt)) {
        chk_use_param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
      sbSQL.append(") T11 on T1.MOYSKBN = T11.MOYSKBN and T1.MOYSSTDT = T11.MOYSSTDT and T1.MOYSRBAN = T11.MOYSRBAN");
    }
    col_chk_use = StringUtils.removeStart(col_chk_use, "+");
    String check_use_sql = sbSQL.toString();


    sbSQL = new StringBuffer();
    if (StringUtils.equals(key, "CNT")) {
      param.add(szMoysstdt);

      sbSQL.append(" select");
      if (StringUtils.isNotEmpty(col_chk_use)) {
        sbSQL.append(" case when " + col_chk_use + " > 0 then 1 else 0 end  as VALUE ");
      } else {
        sbSQL.append(" 0 as VALUE ");
      }
      sbSQL.append(" from (select " + szMoyskbn + " as MOYSKBN, cast(? as varchar(100)) as MOYSSTDT from SYSIBM.SYSDUMMY1) T1");
      sbSQL.append(check_use_sql);
    } else {
      sbSQL.append(" select");
      sbSQL.append("  T1.MOYSKBN as MOYSKBN"); // F1 : 催し区分
      sbSQL.append(" ,T1.MOYSSTDT as MOYSSTDT"); // F2 : 催し開始日
      sbSQL.append(" ,T1.MOYSRBAN as MOYSRBAN"); // F3 : 催し連番
      sbSQL.append(" ,T1.SHUNO as SHUNO"); // F4 : 週№
      sbSQL.append(" ,T1.MOYKN as MOYKN"); // F5 : 催し名称（漢字）
      sbSQL.append(" ,T1.MOYAN as MOYAN"); // F6 : 催し名称（カナ）
      sbSQL.append(" ,T1.NENMATKBN as NENMATKBN"); // F7 : 年末区分
      sbSQL.append(" ,RIGHT(T1.HBSTDT,6) as HBSTDT"); // F8 : 販売開始日
      sbSQL.append(" ,RIGHT(T1.HBEDDT,6) as HBEDDT"); // F9 : 販売終了日
      sbSQL.append(" ,RIGHT(T1.NNSTDT,6) as NNSTDT"); // F10: 納入開始日
      sbSQL.append(" ,RIGHT(T1.NNEDDT,6) as NNEDDT"); // F11: 納入終了日
      sbSQL.append(" ,RIGHT(T1.NNSTDT_TGF,6) as NNSTDT_TGF"); // F12: 納入開始日_全特生鮮
      sbSQL.append(" ,RIGHT(T1.NNEDDT_TGF,6) as NNEDDT_TGF"); // F13: 納入終了日_全特生鮮
      sbSQL.append(" ,RIGHT(T1.PLUSDDT,6) as PLUSDDT"); // F14: PLU配信日
      sbSQL.append(" ,T1.PLUSFLG as PLUSFLG"); // F15: PLU配信済フラグ
      sbSQL.append(" ,T1.UPDKBN as UPDKBN"); // F16: 更新区分
      sbSQL.append(" ,T1.SENDFLG as SENDFLG"); // F17: 送信フラグ
      sbSQL.append(" ,T1.OPERATOR as OPERATOR"); // F18: オペレータ
      sbSQL.append(" ,T1.ADDDT as ADDDT"); // F19: 登録日
      sbSQL.append(" ,T1.UPDDT as UPDDT"); // F20: 更新日
      sbSQL.append(" ,row_number() over (partition by T1.MOYSKBN order by T1.MOYSSTDT,T1.MOYSRBAN) as IDX ");
      if (StringUtils.isNotEmpty(col_chk_use)) {
        sbSQL.append(" ,case when " + col_chk_use + " > 0 then 1 else 0 end as USEF ");
      } else {
        sbSQL.append(" ,0 ");
      }
      if (ValKbn10002.VAL1.getVal().equals(szMoyskbn)) {
        for (int i = 1; i <= 20; i++) {
          sbSQL.append(" ,T3.BMNCD_" + i);
        }
      }
      sbSQL.append(" from (");
      sbSQL.append("  select ");
      sbSQL.append("   T1.MOYSKBN as MOYSKBN"); // F1 : 催し区分
      sbSQL.append("  ,T1.MOYSSTDT as MOYSSTDT"); // F2 : 催し開始日
      sbSQL.append("  ,T1.MOYSRBAN as MOYSRBAN"); // F3 : 催し連番
      sbSQL.append("  ,T1.SHUNO as SHUNO"); // F4 : 週№
      sbSQL.append("  ,T1.MOYKN as MOYKN"); // F5 : 催し名称（漢字）
      sbSQL.append("  ,T1.MOYAN as MOYAN"); // F6 : 催し名称（カナ）
      sbSQL.append("  ,T1.NENMATKBN as NENMATKBN"); // F7 : 年末区分
      sbSQL.append("  ,T1.HBSTDT as HBSTDT"); // F8 : 販売開始日
      sbSQL.append("  ,T1.HBEDDT as HBEDDT"); // F9 : 販売終了日
      sbSQL.append("  ,T1.NNSTDT as NNSTDT"); // F10: 納入開始日
      sbSQL.append("  ,T1.NNEDDT as NNEDDT"); // F11: 納入終了日
      sbSQL.append("  ,T1.NNSTDT_TGF as NNSTDT_TGF"); // F12: 納入開始日_全特生鮮
      sbSQL.append("  ,T1.NNEDDT_TGF as NNEDDT_TGF"); // F13: 納入終了日_全特生鮮
      sbSQL.append("  ,T1.PLUSDDT as PLUSDDT"); // F14: PLU配信日
      sbSQL.append("  ,T1.PLUSFLG as PLUSFLG"); // F15: PLU配信済フラグ
      sbSQL.append("  ,T1.UPDKBN as UPDKBN"); // F16: 更新区分
      sbSQL.append("  ,T1.SENDFLG as SENDFLG"); // F17: 送信フラグ
      sbSQL.append("  ,T1.OPERATOR as OPERATOR"); // F18: オペレータ
      sbSQL.append("  ,T1.ADDDT as ADDDT"); // F19: 登録日
      sbSQL.append("  ,T1.UPDDT as UPDDT"); // F20: 更新日
      sbSQL.append("  from INATK.TOKMOYCD T1");
      sbSQL.append("  where COALESCE(T1.UPDKBN, 0) = 0");
      sbSQL.append("  and not(T1.MOYSKBN = " + ValKbn10002.VAL2.getVal() + " and T1.NNSTDT is null and T1.NNEDDT is null)");
      sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn);
      if (!StringUtils.isEmpty(szSelShuno)) {
        param.add(szSelShuno);
        sbSQL.append("  and T1.SHUNO = ?");
      }
      if (!StringUtils.isEmpty(szMoysstdt)) {
        param.add(szMoysstdt);
        sbSQL.append("  and T1.MOYSSTDT = ?");
      }
      sbSQL.append(" ) T1 ");
      // 登録ちらしのみ部門を表示
      if (ValKbn10002.VAL1.getVal().equals(szMoyskbn)) {
        sbSQL.append(" left outer join (");
        sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
        for (int i = 1; i <= 20; i++) {
          sbSQL.append(" , max(case T1.RNO when " + i + " then T1.BMNCD end) as BMNCD_" + i);
        }
        sbSQL.append("  from (");
        sbSQL.append("    select ROW_NUMBER() over (PARTITION BY T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN ORDER BY T3.BMNCD) as RNO, T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T3.BMNCD");
        sbSQL.append("    from INATK.TOKMOYCD T1");
        sbSQL.append("    inner join INATK.TOKCHIRASBMN T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
        sbSQL.append("    and COALESCE(T1.UPDKBN, 0) = 0");
        sbSQL.append("    and T1.MOYSKBN = " + szMoyskbn);
        if (!StringUtils.isEmpty(szSelShuno)) {
          param.add(szSelShuno);
          sbSQL.append("    and T1.SHUNO = ?");
        }
        if (!StringUtils.isEmpty(szMoysstdt)) {
          param.add(szMoysstdt);
          sbSQL.append("    and T1.MOYSSTDT = ?");
        }
        sbSQL.append("  ) T1");
        sbSQL.append("  group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");
        sbSQL.append(") T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
      }
      sbSQL.append(check_use_sql);
      sbSQL.append(" order by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSSTDT ");
    }
    if (chk_use_param.size() > 0) {
      param.addAll(chk_use_param);
    }


    JSONObject rtn = new JSONObject();
    rtn.put("SQL", sbSQL.toString());
    rtn.put("PRM", param);
    return rtn;
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
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 催し週
    JSONArray dataArrayMOYCDR = JSONArray.fromObject(map.get("DATA_MOYCD_R")); // 催しコード：登録-レギュラー
    JSONArray dataArrayMOYCDS = JSONArray.fromObject(map.get("DATA_MOYCD_S")); // 催しコード：登録-スポット
    JSONArray dataArrayMOYCDT = JSONArray.fromObject(map.get("DATA_MOYCD_T")); // 催しコード：登録-特売
    JSONArray dataArrayMOYCDD = JSONArray.fromObject(map.get("DATA_MOYCD_D")); // 催しコード：削除
    JSONArray dataArrayTOKCHIRASBMN = JSONArray.fromObject(map.get("DATA_TOKCHIRASBMN")); // ちらしのみ部門：登録
    JSONArray dataArrayTOKTG = JSONArray.fromObject(map.get("DATA_TOKTG")); // 全店特売（アンケート有）：登録
    JSONArray dataArrayTOKTGD = JSONArray.fromObject(map.get("DATA_TOKTG_D")); // 全店特売（アンケート有）：削除
    String delTokMoySyu = map.get("DEL_TOKMOYSYU");

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
      this.createSqlTOKMOYSYU(userId, data, SqlType.INS);

      // ②正 .変更
    } else if (isChange) {
      // --- 02.催しコード削除
      if (dataArrayMOYCDD.size() > 0) {
        this.createSqlTOKMOYCD(userId, dataArrayMOYCDD, SqlType.DEL);
      }

      if (dataArrayTOKTGD.size() > 0) {
        this.createSqlTOKTG_CMN(userId, dataArrayTOKTGD, SqlType.DEL, "TOKTG_KHN");
        this.createSqlTOKTG_CMN(userId, dataArrayTOKTGD, SqlType.DEL, "TOKTG_TENGP");

        this.createSqlTOKTG_CMN(userId, dataArrayTOKTGD, SqlType.DEL, "TOKRANKEX");
        this.createSqlTOKTG_CMN(userId, dataArrayTOKTGD, SqlType.DEL, "TOKSRPTNEX");
      }

      if (delTokMoySyu.equals("1")) {
        this.createSqlTOKMOYSYU(userId, data, SqlType.DEL);
      }
    }

    // ************ 子テーブル処理 ***********
    // --- 03.催しコード登録-レギュラー
    if (dataArrayMOYCDR.size() > 0) {
      this.createSqlTOKMOYCD(userId, dataArrayMOYCDR, SqlType.MRG);
    }
    // --- 04.催しコード登録-スポット
    if (dataArrayMOYCDS.size() > 0) {
      this.createSqlTOKMOYCD(userId, dataArrayMOYCDS, SqlType.MRG);
    }
    // --- 05.催しコード登録-特売
    if (dataArrayMOYCDT.size() > 0) {
      this.createSqlTOKMOYCD(userId, dataArrayMOYCDT, SqlType.MRG);
    }

    // --- 06.ちらしのみ部門
    if (dataArrayTOKCHIRASBMN.size() > 0) {
      this.createSqlTOKCHIRASBMN(userId, dataArrayTOKCHIRASBMN, SqlType.MRG);
    }

    // --- 07.全店特売（アンケート有）_基本
    if (dataArrayTOKTG.size() > 0) {
      this.createSqlTOKTG_CMN(userId, dataArrayTOKTG, SqlType.MRG, "TOKTG_KHN");
    }


    // 排他チェック実行
    String targetTable = "INATK.TOKMOYSYU";
    String targetWhere = " SHUNO= ? and UPDKBN = 0";
    ArrayList<String> targetParam = new ArrayList<>();
    targetParam.add(data.optString(TOKMOYSYULayout.SHUNO.getId()));
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
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

    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();

    map.get("SHUNO");
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 催し週
    JSONArray dataArrayMOYCD = JSONArray.fromObject(map.get("DATA_MOYCD")); // 催しコード：登録
    JSONArray dataArrayMOYCDDel = JSONArray.fromObject(map.get("DATA_MOYCDT_DEL")); // 催しコード；削除

    JSONArray dataArrayMOYCDR = JSONArray.fromObject(map.get("DATA_MOYCD_R")); // 催しコード：登録-レギュラー
    JSONArray dataArrayMOYCDS = JSONArray.fromObject(map.get("DATA_MOYCD_S")); // 催しコード：登録-スポット
    JSONArray dataArrayMOYCDT = JSONArray.fromObject(map.get("DATA_MOYCD_T")); // 催しコード：登録-特売
    JSONArray dataArrayMOYCDD = JSONArray.fromObject(map.get("DATA_MOYCD_D")); // 催しコード：削除
    JSONArray dataArrayTOKCHIRASBMN = JSONArray.fromObject(map.get("DATA_TOKCHIRASBMN")); // ちらしのみ部門：登録
    JSONArray dataArrayTOKTG = JSONArray.fromObject(map.get("DATA_TOKTG")); // 全店特売（アンケート有）：登録
    JSONArray dataArrayTOKTGD = JSONArray.fromObject(map.get("DATA_TOKTG_D")); // 全店特売（アンケート有）：削除

    // チェック処理
    // 対象件数チェック
    if ((dataArrayMOYCDR.size() == 0 || dataArrayMOYCDR.getJSONObject(0).isEmpty()) && (dataArrayMOYCDS.size() == 0 || dataArrayMOYCDS.getJSONObject(0).isEmpty())
        && (dataArrayMOYCDT.size() == 0 || dataArrayMOYCDT.getJSONObject(0).isEmpty()) && (dataArrayMOYCDD.size() == 0 || dataArrayMOYCDD.getJSONObject(0).isEmpty())
        && (dataArrayTOKCHIRASBMN.size() == 0 || dataArrayTOKCHIRASBMN.getJSONObject(0).isEmpty()) && (dataArrayTOKTG.size() == 0 || dataArrayTOKTG.getJSONObject(0).isEmpty())
        && (dataArrayTOKTGD.size() == 0 || dataArrayTOKTGD.getJSONObject(0).isEmpty())) {
      msg.add(mu.getDbMessageObj("E20118", new String[] {}));
      return msg;
    }

    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

    List<JSONObject> msgList = this.checkData(isNew, isChange, map, userInfo, sysdate, mu, dataArray, dataArrayMOYCD, dataArrayMOYCDDel);

    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msg.add(msgList.get(0));
    }
    return msg;
  }

  public List<JSONObject> checkData(boolean isNew, boolean isChange, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray, // 催し週
      JSONArray dataArrayMOYCD, // 催しコード：更新
      JSONArray dataArrayMOYCDDel // 催しコード：削除
  ) {

    JSONArray msg = new JSONArray();


    dataArray.optJSONObject(0);


    CmnDate.dbDateFormat(sysdate1);



    String login_dt = sysdate1; // 処理日付
    login_dt.substring(2, 6);



    return msg;
  }

  /**
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getMstData(String sqlcommand, ArrayList<String> paramData) {
    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    return array;
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
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * 催し週INSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKMOYSYU(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    for (int i = 1; i <= 2; i++) {
      String col = "F" + i;
      prmData.add(StringUtils.trim(data.optString(col)));
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKMOYSYU (");
    sbSQL.append("  SHUNO"); // F1 : 週№
    sbSQL.append(" ,TSHUFLG"); // F2 : 特別週フラグ
    sbSQL.append(" ,UPDKBN"); // F3 : 更新区分
    sbSQL.append(" ,SENDFLG"); // F4 : 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F5 : オペレータ
    sbSQL.append(" ,ADDDT"); // F6 : 登録日
    sbSQL.append(" ,UPDDT"); // F7 : 更新日
    sbSQL.append(") VALUES (");
    sbSQL.append("? ");
    sbSQL.append(",? ");
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal());
    sbSQL.append(", '" + userId + "' ");
    sbSQL.append(", CURRENT_TIMESTAMP ");
    sbSQL.append(",CURRENT_TIMESTAMP ");
    sbSQL.append(") ");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("SHUNO = VALUES(SHUNO) ");
    sbSQL.append(",TSHUFLG = VALUES(TSHUFLG) ");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append(",UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
    } else {
      sbSQL.append(",UPDKBN = VALUES(UPDKBN) ");
    }
    sbSQL.append(",SENDFLG = VALUES(SENDFLG) ");
    sbSQL.append(",OPERATOR = VALUES(OPERATOR) ");
    sbSQL.append(",UPDDT = VALUES(UPDDT)");


    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催し週");
    return result;
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
    ArrayList<String> prmData = new ArrayList<>();

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
          val = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(nnstdt), 1));
        }
        if (col.equals(TOKMOYCDLayout.NNEDDT_TGF)) {
          String nneddt = dataArray.optJSONObject(j).optString(TOKMOYCDLayout.NNEDDT.getId());
          val = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(nneddt), 1));
        }
        if (col.equals(TOKMOYCDLayout.PLUSFLG)) {
          if (StringUtils.isEmpty(val)) {
            val = "0";
          }
        }

        if (StringUtils.isEmpty(val)) {
          values += ", null";
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
    sbSQL.append("INSERT INTO INATK.TOKMOYCD ( ");
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
      sbSQL.append(" ,OPERATOR"); // F18: オペレータ
      sbSQL.append(" ,UPDDT"); // F20: 更新日
      sbSQL.append(" ,ADDDT"); // F19: 登録日
    }
    sbSQL.append(") VALUES ");
    sbSQL.append(rows);
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
      sbSQL.append(",OPERATOR=  '" + userId + "' ");
      sbSQL.append(",SENDFLG= " + DefineReport.Values.SENDFLG_UN.getVal() + " ");
      sbSQL.append(",UPDDT = VALUES(UPDDT)");
    } else {
      sbSQL.append(" MOYSKBN = VALUES(MOYSKBN)");
      sbSQL.append(",MOYSSTDT = VALUES(MOYSSTDT)");
      sbSQL.append(",MOYSRBAN = VALUES(MOYSRBAN)");
      sbSQL.append(",SHUNO = VALUES(SHUNO)");
      sbSQL.append(",MOYKN = VALUES(MOYKN)");
      sbSQL.append(",MOYAN = VALUES(MOYAN)");
      sbSQL.append(",NENMATKBN = VALUES(NENMATKBN)");
      sbSQL.append(",HBSTDT = VALUES(HBSTDT)");
      sbSQL.append(",HBEDDT = VALUES(HBEDDT)");
      sbSQL.append(",NNSTDT = VALUES(NNSTDT)");
      sbSQL.append(",NNEDDT = VALUES(NNEDDT)");
      sbSQL.append(",NNSTDT_TGF = VALUES(NNSTDT_TGF)");
      sbSQL.append(",NNEDDT_TGF = VALUES(NNEDDT_TGF)");
      sbSQL.append(",PLUSDDT = VALUES(PLUSDDT)");
      sbSQL.append(",PLUSFLG = VALUES(PLUSFLG)");
      sbSQL.append(",UPDKBN = VALUES(UPDKBN) ");
      sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
      sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
      sbSQL.append(",UPDDT = VALUES(UPDDT)");
    }



    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催しコード");
    return result;
  }


  /**
   * ちらしのみ部門INSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKCHIRASBMN(String userId, JSONArray dataArray, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    String values = "", names = "", rows = "";

    int colNum = TOKMOYCDCMNLayout.values().length + 1;
    // 列別名設定
    for (int i = 1; i <= colNum; i++) {
      names += "," + "F" + i;
    }
    names = StringUtils.removeStart(names, ",");
    // 値設定
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);
        if (isTest) {
          values += ",'" + val + "'";
        } else {
          prmData.add(val);
          values += ",?";
        }
      }
      values += ", " + DefineReport.Values.SENDFLG_UN.getVal();
      values += ", '" + userId + "' ";
      values += ", CURRENT_TIMESTAMP ";
      values += ", CURRENT_TIMESTAMP ";
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");


    DefineReport.Values.SENDFLG_UN.getVal();

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKCHIRASBMN ( ");
    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,BMNCD"); // F4 : 部門
    sbSQL.append(" ,SENDFLG"); // F5 : 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F6 : オペレータ
    sbSQL.append(" ,ADDDT"); // F7 : 登録日
    sbSQL.append(" ,UPDDT"); // F8 : 更新日
    sbSQL.append(") VALUES ");
    sbSQL.append(rows);
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("MOYSKBN = VALUES(MOYSKBN)");
    sbSQL.append(",MOYSSTDT = VALUES(MOYSSTDT)");
    sbSQL.append(",MOYSRBAN = VALUES(MOYSRBAN)");
    sbSQL.append(",BMNCD = VALUES(BMNCD)");
    sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
    sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
    sbSQL.append(",UPDDT = VALUES(UPDDT)");

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("ちらしのみ部門");
    return result;
  }

  /**
   * アンケートINSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKTG_CMN(String userId, JSONArray dataArray, SqlType sql, String table) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    String values = "", names = "", rows = "";
    // 列別名設定
    for (TOKMOYCDCMNLayout col : TOKMOYCDCMNLayout.values()) {
      names += "," + col.getId();
    }
    names = StringUtils.removeStart(names, ",");
    // 値設定
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (TOKMOYCDCMNLayout col : TOKMOYCDCMNLayout.values()) {
        String val = dataArray.optJSONObject(j).optString(col.getId());
        if (isTest) {
          values += ",'" + val + "'";
        } else {
          prmData.add(val);
          values += ",?";
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


    if (ArrayUtils.contains(new String[] {"TOKRANKEX", "TOKSRPTNEX"}, table)) {
    }

    DefineReport.Values.SENDFLG_UN.getVal();
    DefineReport.ValUpdkbn.NML.getVal();
    if (SqlType.DEL.getVal() == sql.getVal()) {
      DefineReport.ValUpdkbn.DEL.getVal();
    }


    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK." + table + " ( ");
    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,UPDKBN"); // F21: 更新区分
    sbSQL.append(" ,SENDFLG"); // F22: 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F23: オペレータ
    sbSQL.append(" ,ADDDT"); // F24: 登録日
    sbSQL.append(" ,UPDDT"); // F25: 更新日
    sbSQL.append(") VALUES ");
    sbSQL.append(rows);
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("MOYSKBN = VALUES(MOYSKBN)");
    sbSQL.append(",MOYSSTDT = VALUES(MOYSSTDT)");
    sbSQL.append(",MOYSRBAN = VALUES(MOYSRBAN)");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append(",UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
    } else {
      sbSQL.append(",UPDKBN = VALUES(UPDKBN) ");
    }
    sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
    sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
    sbSQL.append(",UPDDT = VALUES(UPDDT)");

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("アンケート");
    return result;
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
  public enum TOKMOYSYULayout implements MSTLayout {
    /** 週№ */
    SHUNO(1, "SHUNO", "SMALLINT"),
    /** 特別週フラグ */
    TSHUFLG(2, "TSHUFLG", "SMALLINT"),
    /** 更新区分 */
    UPDKBN(3, "UPDKBN", "SMALLINT"),
    /** 送信フラグ */
    SENDFLG(4, "SENDFLG", "SMALLINT"),
    /** オペレータ */
    OPERATOR(5, "OPERATOR", "VARCHAR(20)"),
    /** 登録日 */
    ADDDT(6, "ADDDT", "TIMESTMP"),
    /** 更新日 */
    UPDDT(7, "UPDDT", "TIMESTMP");


    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TOKMOYSYULayout(Integer no, String col, String typ) {
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

  /** 催しコードレイアウト() */
  public enum TOKMOYCDLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT"),
    /** 週№ */
    SHUNO(4, "SHUNO", "SMALLINT"),
    /** 催し名称（漢字） */
    MOYKN(5, "MOYKN", "VARCHAR(40)"),
    /** 催し名称（カナ） */
    MOYAN(6, "MOYAN", "VARCHAR(20)"),
    /** 年末区分 */
    NENMATKBN(7, "NENMATKBN", "SMALLINT"),
    /** 販売開始日 */
    HBSTDT(8, "HBSTDT", "INTEGER"),
    /** 販売終了日 */
    HBEDDT(9, "HBEDDT", "INTEGER"),
    /** 納入開始日 */
    NNSTDT(10, "NNSTDT", "INTEGER"),
    /** 納入終了日 */
    NNEDDT(11, "NNEDDT", "INTEGER"),
    /** 納入開始日_全特生鮮 */
    NNSTDT_TGF(12, "NNSTDT_TGF", "INTEGER"),
    /** 納入終了日_全特生鮮 */
    NNEDDT_TGF(13, "NNEDDT_TGF", "INTEGER"),
    /** PLU配信日 */
    PLUSDDT(14, "PLUSDDT", "INTEGER"),
    /** PLU配信済フラグ */
    PLUSFLG(15, "PLUSFLG", "SMALLINT"),
    /** 更新区分 */
    UPDKBN(16, "UPDKBN", "SMALLINT"),
    /** 送信フラグ */
    SENDFLG(17, "SENDFLG", "SMALLINT"),
    /** オペレータ */
    OPERATOR(18, "OPERATOR", "VARCHAR(20)"),
    /** 登録日 */
    ADDDT(19, "ADDDT", "TIMESTMP"),
    /** 更新日 */
    UPDDT(20, "UPDDT", "TIMESTMP");


    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TOKMOYCDLayout(Integer no, String col, String typ) {
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


  /** 催しコード関連テーブルレイアウト(共通部分のみ) */
  public enum TOKMOYCDCMNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKMOYCDCMNLayout(Integer no, String col, String typ, String txt) {
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
}
