package dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
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
public class ReportJU031Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU031Dao(String JNDIname) {
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

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";

    String szMoyscd = getMap().get("MOYSCD"); // 催しコード

    String szMoyskbn = "";
    String szHbstdt = "";
    String szMoysrban = "";

    if (!StringUtils.isEmpty(szMoyscd)) {
      szMoyskbn = szMoyscd.substring(0, 1);
      szHbstdt = szMoyscd.substring(1, 7);
      szMoysrban = szMoyscd.substring(7, 10);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    // 基本情報取得
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("SELECT ");
    sbSQL.append("trim(left(T2.SHNCD, 4) || '-' || SUBSTR(T2.SHNCD, 5)) ");
    sbSQL.append(",T2.SHNKN ");
    sbSQL.append(",T1.IRISU ");
    sbSQL.append(",CAST(T1.GENKAAM AS CHAR) AS GENKAAM ");
    sbSQL.append(",CASE WHEN T2.ZEIRT IS NULL THEN T1.BAIKAAM  ");
    sbSQL.append("ELSE CEILING(CAST(T1.BAIKAAM AS DECIMAL(8,2)) / NULLIF(1 + CAST(T2.ZEIRT AS DECIMAL(4,2)) / 100, 0)) END HONBAIKA ");
    sbSQL.append(",T1.BAIKAAM ");
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(T1.HTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HTDT, '%Y%m%d'))) ");
    sbSQL.append(",DATE_FORMAT(DATE_FORMAT(T1.NNDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNDT, '%Y%m%d'))) ");
    sbSQL.append(",right('0000'||T1.KANRINO,4) AS KANRINO ");
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKQJU_SHN T1 LEFT JOIN ");
    sbSQL.append("(SELECT ");
    sbSQL.append("T1.SHNCD ");
    sbSQL.append(",T1.SHNKN ");
    sbSQL.append(",T2.ZEIRT ");
    sbSQL.append("FROM ");
    sbSQL.append("(SELECT ");
    sbSQL.append("T1.SHNCD ");
    sbSQL.append(",T1.SHNKN ");
    sbSQL.append(",CASE WHEN T1.ZEIKBN <> '3' THEN ");
    sbSQL.append("CASE WHEN T1.ZEIKBN <> '0' THEN NULL ");
    sbSQL.append("WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) <= ? THEN T1.ZEIRTKBN ");
    sbSQL.append("WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) > ? THEN T1.ZEIRTKBN_OLD END ");
    sbSQL.append("ELSE CASE WHEN T2.ZEIKBN <> '0' THEN NULL ");
    sbSQL.append("WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) <= ? THEN T2.ZEIRTKBN ");
    sbSQL.append("WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) > ? THEN T2.ZEIRTKBN_OLD END END ZEIRTKBN ");
    sbSQL.append("FROM INAMS.MSTSHN T1 LEFT JOIN INAMS.MSTBMN T2 ON SUBSTR(T1.SHNCD,1,2) = T2.BMNCD ");
    paramData.add(szHbstdt);
    paramData.add(szHbstdt);
    paramData.add(szHbstdt);
    paramData.add(szHbstdt);
    sbSQL.append(") T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN = T2.ZEIRTKBN) AS T2 ON T1.SHNCD=T2.SHNCD ");
    sbSQL.append("WHERE ");

    // 催し区分
    if (StringUtils.isEmpty(szMoyskbn)) {
      sqlWhere += "T1.MOYSKBN=null AND ";
    } else {
      sqlWhere += "T1.MOYSKBN=? AND ";
      paramData.add(szMoyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(szHbstdt)) {
      sqlWhere += "T1.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "T1.MOYSSTDT=? AND ";
      paramData.add(szHbstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(szMoysrban)) {
      sqlWhere += "T1.MOYSRBAN=null AND ";
    } else {
      sqlWhere += "T1.MOYSRBAN=? AND ";
      paramData.add(szMoysrban);
    }

    sbSQL.append(sqlWhere);
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" ORDER BY T1.KANRINO,LEFT(T2.SHNCD,2) ");

    // 基本情報取得
    JSONArray array1 = getTOKMOYCDData(szMoyskbn, szHbstdt, szMoysrban);

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array1);
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  /**
   * ベース情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(String szMoyskbn, String szMoysstdt, String szMoysrban) {

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append(" T1.NNSTDT"); // F1: 納入開始日
    sbSQL.append(" ,T1.NNEDDT"); // F2: 納入終了日
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" where T1.UPDKBN = 0");
    sbSQL.append("   and T1.MOYSKBN = " + szMoyskbn + "");
    sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
  }



}
