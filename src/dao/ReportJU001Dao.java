package dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportJU001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU001Dao(String JNDIname) {
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
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 催し開始日
    if (StringUtils.isEmpty(szMoysstdt)) {
      sqlWhere += "T1.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "T1.MOYSSTDT >=? AND ";
      paramData.add(szMoysstdt);
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select ");
    sbSQL.append(" CONCAT(CONCAT(T1.MOYSKBN,'-'),CONCAT(T1.MOYSSTDT,CONCAT('-',right ('000' || RTRIM(CAST(T1.MOYSRBAN as CHAR)), 3)))) ");
    sbSQL.append(" ,T1.MOYKN");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T1.NNSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNSTDT, '%Y%m%d')))");
    sbSQL.append(" ||'～'||");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT(T1.NNEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNEDDT, '%Y%m%d'))) as KIKAN");
    sbSQL.append(" ,Count(T2.KANRINO) as 商品数");
    sbSQL.append(",CONCAT(CONCAT(T1.MOYSKBN, right(T1.MOYSSTDT, 6)), right('000'||T1.MOYSRBAN, 3)) as 催しコード");// F5催しコード
    sbSQL.append(" from INATK.TOKMOYCD T1 LEFT JOIN INATK.TOKJU_SHN T2 ON ");
    sbSQL.append(" T1.MOYSKBN=T2.MOYSKBN");
    sbSQL.append(" and T1.MOYSSTDT=T2.MOYSSTDT");
    sbSQL.append(" and T1.MOYSRBAN=T2.MOYSRBAN");
    sbSQL.append(" AND T2.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" where ");
    sbSQL.append(sqlWhere);
    sbSQL.append("T1.MOYSKBN=9 AND ");
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" group by T1.MOYSKBN");
    sbSQL.append(",T1.MOYSSTDT");
    sbSQL.append(",T1.MOYSRBAN");
    sbSQL.append(",T1.MOYKN");
    sbSQL.append(",T1.NNSTDT");
    sbSQL.append(",T1.NNEDDT");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
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
}
