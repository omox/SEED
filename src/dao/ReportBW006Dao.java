package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class ReportBW006Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportBW006Dao(String JNDIname) {
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

    String szSEQ = getMap().get("SEQ"); // 催し区分

    ArrayList<String> paramData = new ArrayList<>();
    paramData.add(szSEQ);
    paramData.add(szSEQ);
    paramData.add(szSEQ);
    paramData.add(szSEQ);
    paramData.add(szSEQ);
    setParamData(paramData);

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());
    sbSQL.append("  select");
    sbSQL.append("   CTRS.INPUTEDANO");
    sbSQL.append(" , CONCAT(CONCAT(left(CTRS.SHNCD,4),'-'),SUBSTRING(CTRS.SHNCD,5,4))");
    sbSQL.append(" , CTRS.SHNKN");
    sbSQL.append(" , CTRS.ERRFLD");
    sbSQL.append(" , CTRS.ERRTBLNM");
    sbSQL.append(" , CTRS.ERRVL");
    sbSQL.append(" from INATK.CSVTOKHEAD CTHD");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.CSVTOK_RSKKK CTRK");
    sbSQL.append(" on CTRK.SEQ = ? ");
    sbSQL.append(" and CTRK.UPDKBN = 0");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.CSVTOK_RSSHN CTRS");
    sbSQL.append(" on CTRS.SEQ = ? ");
    sbSQL.append(" and CTRS.INPUTNO = 1");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH1");
    sbSQL.append(" on MMSH1.MEISHOCD = CTRK.WRITUKBN ");
    sbSQL.append(" and MMSH1.MEISHOKBN = 10302 ");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH2 ");
    sbSQL.append(" on MMSH1.MEISHOCD = CTRK.SEICUTKBN ");
    sbSQL.append(" and MMSH1.MEISHOKBN = 10303 ");
    sbSQL.append(" left join INAMS.MSTSHN MTSH ");
    sbSQL.append(" on MTSH.SHNCD = CTRK.DUMMYCD ");
    sbSQL.append(" where ");
    sbSQL.append(" CTHD.SEQ = ? ");
    sbSQL.append(" and CTRK.SEQ = ? ");
    sbSQL.append(" and CTRK.UPDKBN = 0 ");
    sbSQL.append(" and CTRS.SEQ = ? ");
    sbSQL.append(" and CTRS.INPUTNO = 1 ");
    sbSQL.append(" order by CTRS.INPUTEDANO ");

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
    String szSEQ = map.get("SEQ"); // 催し区分

    ArrayList<String> paramData = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("  select");
    sbSQL.append("   CTHD.OPERATOR as F1");
    sbSQL.append(" , DATE_FORMAT(CTHD.INPUT_DATE, '%y/%m/%d') as F2");
    sbSQL.append(" , DATE_FORMAT(CTHD.INPUT_DATE, '%H:%i') as F3");
    sbSQL.append(" , RIGHT(CAST(CTRK.HBSTDT AS CHAR), 6) as F4");
    sbSQL.append(" , CTRK.MEISHOKN as F5");
    sbSQL.append(" , MMSH1.NMKN as F6");
    sbSQL.append(" , MMSH2.NMKN as F7");
    sbSQL.append(" , CTRK.DUMMYCD as F8");
    sbSQL.append(" , MTSH.POPKN as F9");
    sbSQL.append(" , (select COUNT(CTRS.ERRCD) from INATK.CSVTOK_RSSHN CTRS where CTRS.SEQ =  ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRS.INPUTNO = 1) as F10");
    sbSQL.append(" , CTHD.COMMENTKN as F11");
    sbSQL.append(" from INATK.CSVTOKHEAD CTHD");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.CSVTOK_RSKKK CTRK");
    sbSQL.append(" on CTRK.SEQ = ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRK.UPDKBN = 0");
    sbSQL.append(" left join");
    sbSQL.append(" INATK.CSVTOK_RSSHN CTRS");
    sbSQL.append(" on CTRS.SEQ = ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRS.INPUTNO = 1");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH1");
    sbSQL.append(" on MMSH1.MEISHOCD = CTRK.WRITUKBN ");
    sbSQL.append(" and MMSH1.MEISHOKBN = 10302 ");
    sbSQL.append(" left join INAMS.MSTMEISHO MMSH2 ");
    sbSQL.append(" on MMSH2.MEISHOCD = CTRK.SEICUTKBN ");
    sbSQL.append(" and MMSH2.MEISHOKBN = 10303 ");
    sbSQL.append(" left join INAMS.MSTSHN MTSH ");
    sbSQL.append(" on MTSH.SHNCD = CTRK.DUMMYCD ");
    sbSQL.append(" where ");
    sbSQL.append(" CTHD.SEQ =  ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRK.SEQ = ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRK.UPDKBN = 0 ");
    sbSQL.append(" and CTRS.SEQ = ");
    sbSQL.append(szSEQ);
    sbSQL.append(" and CTRS.INPUTNO = 1 ");
    sbSQL.append(" order by CTRS.INPUTEDANO ");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
