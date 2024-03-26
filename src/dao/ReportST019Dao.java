package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportST019Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportST019Dao(String JNDIname) {
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

    String szMoyscd = getMap().get("MOYSCD"); // 催しコード
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    getMap().get("PUSHBTNID");
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出ボタン

    // パラメータ確認
    // 必須チェック
    if ((szMoyscd == null) || (sendBtnid == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    ArrayList<String> paramData = new ArrayList<String>();
    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(StringUtils.left(szMoyscd, 1), StringUtils.right(szMoyscd, 3));

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());


    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11");
    sbSQL.append("  ,T1.MOYSKBN");
    sbSQL.append("  ,T1.MOYSSTDT");
    sbSQL.append("  ,T1.MOYSRBAN");
    sbSQL.append("  ,T1.BMNCD");
    sbSQL.append("  ,T1.KANRINO");
    sbSQL.append("  ,T1.KANRIENO");
    sbSQL.append("  ,T1.ADDSHUKBN");
    sbSQL.append(" from (");
    sbSQL.append("  select");
    sbSQL.append("   T1.PARNO as F1");
    sbSQL.append("  ,T1.CHLDNO as F2");
    sbSQL.append("  ,trim(left(T1.SHNCD, 4)||'-'||SUBSTR(T1.SHNCD, 5)) as F3");
    sbSQL.append("  ,M1.SHNKN as F4");
    sbSQL.append("  ,case when M1.SHNKBN = 1 then '" + DefineReport.Values.ON.getVal() + "' end as F5");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("   ||'～'||");
    sbSQL.append("   DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as F6");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T1.NNSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNSTDT, '%Y%m%d')))");
    sbSQL.append("   ||'～'||");
    sbSQL.append("   DATE_FORMAT(DATE_FORMAT(T1.NNEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNEDDT, '%Y%m%d'))) as F7");
    sbSQL.append("  ,case when T1.BD1_TENSU is not null then '" + DefineReport.Values.ON.getVal() + "' end as F8");
    sbSQL.append("  ,case when T1.ADDSHUKBN = " + DefineReport.ValAddShuKbn.VAL1.getVal());
    if (isTOKTG) {
      sbSQL.append("  and T1.B_WRITUKBN is not null");
    } else {
      sbSQL.append("  and T1.B_BAIKAAM is not null");
    }
    sbSQL.append("   then '" + DefineReport.Values.ON.getVal() + "' end as F9");
    if (isTOKTG) {
      sbSQL.append("  ,T1.RANKNO_ADD as F10");
      sbSQL.append("  ,null as F11");
    } else {
      sbSQL.append("  ,T1.RANKNO_ADD_A as F10");
      sbSQL.append("  ,T1.RANKNO_DEL as F11");
    }
    sbSQL.append("  ,T1.MOYSKBN");
    sbSQL.append("  ,T1.MOYSSTDT");
    sbSQL.append("  ,T1.MOYSRBAN");
    sbSQL.append("  ,T1.BMNCD");
    sbSQL.append("  ,T1.KANRINO");
    sbSQL.append("  ,T1.KANRIENO");
    sbSQL.append("  ,T1.ADDSHUKBN");

    sbSQL.append("  ,row_number() over(partition by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD,T1.KANRINO order by T1.KANRIENO desc) as RNO");
    sbSQL.append("  from " + szTableSHN + " T1");
    sbSQL.append("  left outer join INAMS.MSTSHN M1 on M1.SHNCD = T1.SHNCD and COALESCE(M1.UPDKBN, 0) <> 1");
    sbSQL.append("  where T1.UPDKBN = 0");
    if (StringUtils.isNotEmpty(szMoyscd)) {
      if (StringUtils.length(szMoyscd) >= 1) {
        sbSQL.append("  and T1.MOYSKBN = ? ");
        paramData.add(StringUtils.left(szMoyscd, 1));
      }
      if (StringUtils.length(szMoyscd) >= 7) {
        sbSQL.append("  and T1.MOYSSTDT = ? ");
        paramData.add(StringUtils.mid(szMoyscd, 1, 6));
      }
      if (StringUtils.length(szMoyscd) == DefineReport.InpText.MOYSCD.getLen()) {
        sbSQL.append("  and T1.MOYSRBAN = ? ");
        paramData.add(Integer.toString(NumberUtils.toInt(StringUtils.right(szMoyscd, 3))));
      }
    }

    if (StringUtils.isNotEmpty(szBmncd) && !StringUtils.equals(szBmncd, DefineReport.Values.NONE.getVal())) {
      sbSQL.append("  and T1.BMNCD = " + szBmncd + "");
    }
    sbSQL.append(" ) T1");
    sbSQL.append(" where RNO = 1");
    sbSQL.append(" order by");
    sbSQL.append("   T1.MOYSKBN");
    sbSQL.append("  ,T1.MOYSSTDT");
    sbSQL.append("  ,T1.MOYSRBAN");
    sbSQL.append("  ,T1.BMNCD");
    sbSQL.append("  ,T1.KANRINO");
    sbSQL.append(" LIMIT 1000 ");
    setParamData(paramData);
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
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番

    ArrayList<String> paramData = new ArrayList<String>();


    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append("    T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
    sbSQL.append("  , T1.MOYKN as F2");
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("    ||'～'||");
    sbSQL.append("    DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as F3");
    sbSQL.append("  , case when T2.HBOKUREFLG = " + DefineReport.Values.ON.getVal() + " then '" + DefineReport.Values.ON.getVal() + "' end as F4");
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(T2.GTSIMEDT,'%Y%m%d'),'%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.GTSIMEDT, '%Y%m%d'))) as F5 ");
    sbSQL.append("  , case when T2.GTSIMEFLG = " + DefineReport.Values.ON.getVal() + " then '" + DefineReport.Values.ON.getVal() + "' end as F6");
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(T2.LSIMEDT, '%Y%m%d'),'%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.LSIMEDT, '%Y%m%d'))) as F7");
    sbSQL.append("  , left(right(T2.QAYYYYMM, 4), 2)||'/'||right(T2.QAYYYYMM, 2) as F8");
    sbSQL.append("  , right('00'||COALESCE(T2.QAENO, 0), 2)  as F9");
    sbSQL.append("  , DATE_FORMAT(DATE_FORMAT(T2.QACREDT, '%Y%m%d'),'%Y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QACREDT, '%Y%m%d'))) as F10");
    sbSQL.append("  , case when T2.HNCTLFLG = " + DefineReport.Values.ON.getVal() + " then '" + DefineReport.Values.ON.getVal() + "' end as F11");
    sbSQL.append("  , T1.OPERATOR as F12");
    sbSQL.append("  , COALESCE(DATE_FORMAT(T1.ADDDT, '%y/%m/%d'),'__/__/__') as F13");
    sbSQL.append("  , COALESCE(DATE_FORMAT(T1.UPDDT, '%y/%m/%d'),'__/__/__') as F14");
    sbSQL.append("  , DATE_FORMAT(T1.UPDDT, '%Y%m%d%H%i%s%f') as F15");
    sbSQL.append("  from INATK.TOKMOYCD T1");
    sbSQL.append("  inner join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");
    sbSQL.append("  and T1.UPDKBN = 0");
    sbSQL.append("  and T1.MOYSKBN = ? ");
    paramData.add(szMoyskbn);
    sbSQL.append("  and T1.MOYSSTDT = ? ");
    paramData.add(szMoysstdt);
    sbSQL.append("  and T1.MOYSRBAN = ? ");
    paramData.add(szMoysrban);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
