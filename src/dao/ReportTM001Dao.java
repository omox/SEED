package dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.DefineReport.ValKbn10002;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTM001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTM001Dao(String JNDIname) {
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
    ArrayList<String> paramData = new ArrayList<String>();
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード

    // パラメータ確認
    /*
     * // 必須チェック if ((btnId == null)) { System.out.println(super.getConditionLog()); return ""; }
     */

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(", WK as (");
    sbSQL.append(" select SHUNO,CNTR,CNTS,CNTT from (");
    sbSQL.append("  select");
    sbSQL.append("   T1.SHUNO");
    sbSQL.append("  ,sum(case when T1.MOYSKBN = " + ValKbn10002.VAL0.getVal() + " then 1 else 0 end) as CNTR"); // レギュラーデータ件数
    sbSQL.append("  ,sum(case when T1.MOYSKBN = " + ValKbn10002.VAL2.getVal() + " and not(T1.NNSTDT is null and T1.NNEDDT is null) then 1 else 0 end) as CNTS"); // スポットデータ件数
    sbSQL.append("  ,sum(case when T1.MOYSKBN = " + ValKbn10002.VAL1.getVal() + " then 1 else 0 end) as CNTT"); // 特売データ件数
    sbSQL.append("  from INATK.TOKMOYCD T1");
    sbSQL.append("  where T1.MOYSKBN in (" + ValKbn10002.VAL0.getVal() + "," + ValKbn10002.VAL2.getVal() + ", " + ValKbn10002.VAL1.getVal() + ")");
    sbSQL.append("  and T1.UPDKBN = 0");
    if (!StringUtils.isEmpty(szMoysstdt)) {
      sbSQL.append("  and T1.MOYSSTDT >= ? ");
      paramData.add(szMoysstdt);
    }
    sbSQL.append("  group by T1.SHUNO");
    sbSQL.append(" ) AS T1 where not(CNTR=0 and CNTS=0 and CNTT=0)");
    sbSQL.append(")");
    sbSQL.append(" select T1.SHUNO,CNTR,CNTS,CNTT, STARTDT||W1.JWEEK||'～'||ENDDT||W2.JWEEK");
    sbSQL.append(" from WK T1");
    sbSQL.append(" left outer join (");
    sbSQL.append("  select");
    sbSQL.append("   T2.SHUNO");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T2.STARTDT, '%y/%m/%d'), '%y/%m/%d') as STARTDT");
    sbSQL.append("  ,dayofweek(DATE_FORMAT(T2.STARTDT, '%Y%m%d')) as STARTDTW");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T2.ENDDT, '%y/%m/%d'), '%y/%m/%d') as ENDDT");
    sbSQL.append("  ,dayofweek(DATE_FORMAT(T2.ENDDT, '%Y%m%d')) as ENDDTW");
    sbSQL.append("  from INAAD.SYSSHUNO T2");
    sbSQL.append("  where exists (select 'X' from WK T1 where T1.SHUNO = T2.SHUNO)");
    sbSQL.append(" ) T2 on T1.SHUNO = T2.SHUNO");
    sbSQL.append(" left outer join WEEK W1 on T2.STARTDTW = W1.CWEEK");
    sbSQL.append(" left outer join WEEK W2 on T2.ENDDTW = W2.CWEEK");
    sbSQL.append(" order by T1.SHUNO,T2.STARTDT, T2.ENDDT");
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

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
  }
}
