package dao;

import java.util.ArrayList;
import java.util.HashMap;
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
public class ReportJU011Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU011Dao(String JNDIname) {
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

    String moyscd = getMap().get("MOYSCD"); // 催しコード
    String szMoyskbn = ""; // 催し区分
    String szHbstdt = ""; // 催し開始日
    String szMoysrban = ""; // 催し連番

    if (!StringUtils.isEmpty(moyscd) && moyscd.length() >= 8) {
      szMoyskbn = moyscd.substring(0, 1);
      szHbstdt = moyscd.substring(1, 7);
      szMoysrban = moyscd.substring(7);
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
    sbSQL.append("INATK.TOKJU_SHN T1 LEFT JOIN ");
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
    sbSQL.append(" ORDER BY LEFT(T2.SHNCD,2),T1.KANRINO");

    // 基本情報取得
    JSONArray array1 = new ReportJU031Dao(JNDIname).getTOKMOYCDData(szMoyskbn, szHbstdt, szMoysrban);

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
    String szHbstdt = map.get("HBSTDT"); // 催し区分
    String szMoysrban = map.get("MOYSRBAN"); // 催し区分

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("  select");
    sbSQL.append("  CONCAT(T1.MOYSKBN,CONCAT(T1.MOYSSTDT,right ('000' || RTRIM(CHAR (T1.MOYSRBAN)), 3))) as F1");
    sbSQL.append("  ,T1.MOYKN  as F2");
    sbSQL.append("  ,TO_CHAR(TO_DATE(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNSTDT, 'YYYYMMDD')))");
    sbSQL.append("  ||'～'||");
    sbSQL.append("  TO_CHAR(TO_DATE(T1.NNEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNEDDT, 'YYYYMMDD'))) as F3");
    sbSQL.append("  from");
    sbSQL.append("  INATK.TOKMOYCD T1");
    sbSQL.append("  where");
    sbSQL.append("  T1.MOYSKBN = ");
    sbSQL.append(szMoyskbn);
    sbSQL.append("  and T1.MOYSSTDT = ");
    sbSQL.append(szHbstdt);
    sbSQL.append("  and T1.MOYSRBAN = ");
    sbSQL.append(szMoysrban);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
