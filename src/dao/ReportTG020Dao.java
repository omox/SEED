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
public class ReportTG020Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG020Dao(String JNDIname) {
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

    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = getMap().get("MOYSRBAN"); // 催し連番
    String szShncd = getMap().get("SHNCD"); // 商品コード
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szNndt = getMap().get("NNDT"); // 納入日
    String szShnkbn = getMap().get("SHNKBN"); // 商品区分
    String pushBtnid = getMap().get("PUSHBTNID"); // 実行ボタン
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出ボタン

    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();
    JSONArray array_ten = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null) || (sendBtnid == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    String table = " INATK.TOKTG_SHN T1, INATK.TOKTG_NNDT T2 "; // 全店特売(アン有)_商品、納入日
    if (!isTOKTG) {
      table = " INATK.TOKSP_SHN T1, INATK.TOKSP_NNDT T2 "; // 全店特売(アン無)_商品、納入日
    }
    if ("".equals(szShnkbn)) {
      szShnkbn = "null";
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    if (StringUtils.isNotEmpty(pushBtnid)) {

      sbSQL.append(" SELECT");
      sbSQL.append(" T1.KIKKN AS F5");
      sbSQL.append(" ,T1.IRISU AS F6");
      sbSQL.append(" ," + szShnkbn + " AS F7");
      sbSQL.append(" ,T1.BINKBN AS F8");
      sbSQL.append(" ,case when T2.TENHTSU_ARR is null then '     ' else T2.TENHTSU_ARR end as F9");
      sbSQL.append(" ,T2.KANRINO F10");
      sbSQL.append(" ,T2.KANRIENO F11");
      if (isTOKTG) {
        sbSQL.append(" ,T2.TENCHGFLG_ARR F12");
      } else {
        sbSQL.append(" ,null AS F12");
      }
      sbSQL.append(" ,T2.HTASU F13");
      sbSQL.append(" ,T2.PTNNO F14");
      sbSQL.append(" ,T2.TSEIKBN F15");
      sbSQL.append(" ,T2.TPSU F16");
      sbSQL.append(" ,T2.TENKAISU F17");
      sbSQL.append(" ,T2.ZJSKFLG F18");
      sbSQL.append(" ,T2.WEEKHTDT F19");
      sbSQL.append(" ,T1.SHUDENFLG F20");
      sbSQL.append(" ,DATE_FORMAT(T2.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT ");
      sbSQL.append(" FROM");
      sbSQL.append(table);
      sbSQL.append(" WHERE");
      if (StringUtils.isEmpty(szMoyskbn)) {
        sqlWhere += " T1.MOYSKBN=null ";
      } else {
        sqlWhere += " T1.MOYSKBN=? ";
        paramData.add(szMoyskbn);
      }

      if (StringUtils.isEmpty(szMoysstdt)) {
        sqlWhere += " AND T1.MOYSSTDT=null ";
      } else {
        sqlWhere += " AND T1.MOYSSTDT=?";
        paramData.add(szMoysstdt);
      }

      if (StringUtils.isEmpty(szMoysrban)) {
        sqlWhere += " AND T1.MOYSRBAN=null ";
      } else {
        sqlWhere += " AND T1.MOYSRBAN=?";
        paramData.add(szMoysrban);
      }

      if (StringUtils.isEmpty(szBmncd)) {
        sqlWhere += " AND T1.BMNCD=null ";
      } else {
        sqlWhere += " AND T1.BMNCD=?";
        paramData.add(szBmncd);
      }

      if (StringUtils.isEmpty(szShncd)) {
        sqlWhere += " AND T1.SHNCD=null ";
      } else {
        sqlWhere += " AND T1.SHNCD=?";
        paramData.add(szShncd);
      }
      sbSQL.append(sqlWhere);
      sbSQL.append(" AND T1.UPDKBN<>'1'");
      sbSQL.append(" AND T1.MOYSKBN=T2.MOYSKBN");
      sbSQL.append(" AND T1.MOYSSTDT=T2.MOYSSTDT");
      sbSQL.append(" AND T1.MOYSRBAN=T2.MOYSRBAN");
      sbSQL.append(" AND T1.BMNCD=T2.BMNCD");
      sbSQL.append(" AND T1.KANRINO=T2.KANRINO");
      sbSQL.append(" AND T1.KANRIENO=T2.KANRIENO");
      if (StringUtils.isEmpty(szNndt)) {
        sbSQL.append(" AND T2.NNDT=null");
      } else {
        sbSQL.append(" AND T2.NNDT=?");
        paramData.add(szNndt);
      }

      // 店運用区分
      array_ten = getTenCount(getMap());
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    option.put("rows_ten", array_ten);
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
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(",WKCD as (");
    sbSQL.append(" select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.MOYKN, T1.HBSTDT, T1.HBEDDT, T1.SHUNO ");
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" where T1.UPDKBN = 0");
    sbSQL.append("   and T1.MOYSKBN = " + szMoyskbn + "");
    sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append(")");
    sbSQL.append(" select");
    sbSQL.append("   T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
    sbSQL.append(" , T1.MOYKN as F2");
    sbSQL.append(
        " , case when T1.HBSTDT=T1.HBEDDT then DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'))) else ");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("   ||'～'||");
    sbSQL.append("   DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) end as F3");
    sbSQL.append(" , T1.SHUNO as F4");
    sbSQL.append(" , DATE_FORMAT(DATE_FORMAT(T2.GTSIMEDT,'%Y%m%d'),'%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.GTSIMEDT, '%Y%m%d'))) as F5 ");
    sbSQL.append("  ,T2.GTSIMEFLG as F6"); // 月締フラグ
    sbSQL.append(" from WKCD T1");
    sbSQL.append(" left outer join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTenCount(HashMap<String, String> map) {
    String szTenCd = map.get("TENCD"); // 店コード

    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";

    // 店コード
    if (StringUtils.isEmpty(szTenCd)) {
      sqlWhere += " TENCD=null ";
    } else {
      sqlWhere += " TENCD=? ";
      paramData.add(szTenCd);
    }

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("COUNT(1) AS CNT ");
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("AND TENCD <= 400 ");
    sbSQL.append("AND MISEUNYOKBN <> 9 ");
    sbSQL.append("AND UPDKBN <> 1 ");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  // 商品コード入力時呼び出し
  public String createSqlSelMSTSHN(JSONObject obj) {

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT");
    sbSQL.append(" SHNKN AS F1"); // F1:商品名
    sbSQL.append(" ,BMNCD AS F2"); // F2:部門コード
    sbSQL.append(" ,SHNKBN AS F3"); // F3:商品区分
    sbSQL.append(" FROM");
    sbSQL.append(" INAMS.MSTSHN");
    sbSQL.append(" WHERE");
    sbSQL.append(" SHNCD=?");

    return sbSQL.toString();
  }

  // 商品コード入力時呼び出し
  public String createSqlGetHatsu(JSONObject obj) {

    String arr = obj.optString("value");
    String tenCd = obj.optString("tencd");
    HashMap<String, String> getRitsuMap = new ReportJU012Dao(JNDIname).getDigitMap(arr, 5, "0");

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT ");
    if (getRitsuMap.containsKey(tenCd)) {
      sbSQL.append(getRitsuMap.get(tenCd) + " AS F1");
    } else {
      sbSQL.append(" '     ' AS F1");
    }
    sbSQL.append(" FROM (SELECT 1 FROM (SLECT 1 AS DUMMY) DUMMY)");

    return sbSQL.toString();
  }
}
