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
public class ReportSR003Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportSR003Dao(String JNDIname) {
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

    String szShncd = getMap().get("SHNCD"); // 商品コード
    String szSyoridt = getMap().get("SYORIDT"); // 処理日付

    // パラメータ確認
    // 必須チェック
    if (szShncd == null && szSyoridt == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sb = new StringBuffer();

    sb.append(DefineReport.ID_SQL_CMN_WEEK);
    sb.append(" select");
    sb.append(" T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F1"); // F1 : 催しコード
    sb.append(", DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y/%m/%d'), '%y/%m/%d')"); // F2 : 販売期間
    sb.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y/%m/%d')))");
    sb.append("  || '～' || DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y/%m/%d'), '%y/%m/%d')");
    sb.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y/%m/%d'))) as F3");
    sb.append(", DATE_FORMAT(DATE_FORMAT(T1.NNSTDT, '%Y/%m/%d'), '%y/%m/%d')"); // F3 : 納入期間
    sb.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNSTDT, '%Y/%m/%d')))");
    sb.append("  || '～' || DATE_FORMAT(DATE_FORMAT(T1.NNEDDT, '%Y/%m/%d'), '%y/%m/%d')");
    sb.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.NNEDDT, '%Y/%m/%d'))) as F3");
    sb.append(", T2.IRISU"); // F4 : 生活応援入数
    sb.append(", T2.MINSU"); // F5 : 最低発注数
    sb.append(", T2.GENKAAM"); // F6 : 生活応援原価
    sb.append(", T2.A_BAIKAAM");
    sb.append(", T2.B_BAIKAAM");
    sb.append(", T2.B_RANKNO"); // F9 : Bランク
    sb.append(", T2.C_BAIKAAM");
    sb.append(", T2.C_RANKNO"); // F11 : Cランク
    sb.append(" from INATK.TOKMOYCD T1");
    sb.append(" inner join INATK.TOKSO_SHN T2 on T2.MOYSKBN = T1.MOYSKBN and T2.MOYSSTDT = T1.MOYSSTDT and T2.MOYSRBAN = T1.MOYSRBAN");
    sb.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = T2.BMNCD");
    sb.append(" left join INAMS.MSTBMN MSBMN on MSBMN.BMNCD = T2.BMNCD");
    sb.append(" left join INAMS.MSTSHN MSSHN on MSSHN.SHNCD = T2.SHNCD");
    sb.append(" left join INAMS.MSTZEIRT ZRT on ZRT.ZEIRTKBN = MSSHN.ZEIRTKBN");
    sb.append(" left join INAMS.MSTZEIRT ZRT_OLD on ZRT_OLD.ZEIRTKBN = MSSHN.ZEIRTKBN_OLD");
    sb.append(" left join INAMS.MSTZEIRT ZRT_BMN on ZRT_BMN.ZEIRTKBN = MSBMN.ZEIRTKBN");
    sb.append(" left join INAMS.MSTZEIRT ZRT_OLD_BMN on ZRT_OLD_BMN.ZEIRTKBN = MSBMN.ZEIRTKBN_OLD");
    sb.append(" where T2.SHNCD = " + szShncd);
    sb.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sb.toString());
    return sb.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

    String szShncd = map.get("SHNCD"); // 商品コード
    String szSyoridt = map.get("SYORIDT"); // 処理日付

    ArrayList<String> paramData = new ArrayList<>();

    StringBuffer sb = new StringBuffer();

    sb.append(" select");
    sb.append(" SUBSTRING(right ('0000000' || RTRIM(T1.SHNCD), 8), 1, 4) || '-' || SUBSTRING(right ('0000000' || RTRIM( T1.SHNCD), 8), 5, 4) as F1");
    sb.append(", T1.SHNKN as F2");
    sb.append(", right ('00' || T1.BMNCD, 2) as F3");
    sb.append(", T1.RG_GENKAAM as F4");
    sb.append(", T1.SOBAIKA as F5");
    sb.append(", T1.RG_IRISU as F6");
    sb.append(", truncate(((T1.HTBAIKA) - T1.RG_GENKAAM) / T1.HTBAIKA * 100, 2) as F7");
    sb.append(" from (select");
    sb.append(" SHN.SHNCD");
    sb.append(", SHN.SHNKN");
    sb.append(", SHN.BMNCD");
    sb.append(", SHN.RG_GENKAAM");
    sb.append(", SHN.RG_IRISU");
    sb.append(", case");
    sb.append("  when SHN.ZEIKBN = 3 then (");
    sb.append("   case");
    sb.append("   when BMN.ZEIRTHENKODT <= " + szSyoridt + " then truncate( (SHN.RG_BAIKAAM) + ( (SHN.RG_BAIKAAM) * (ZRT_BMN.ZEIRT / 100)), 0)");
    sb.append("   when BMN.ZEIRTHENKODT > " + szSyoridt + " then truncate( (SHN.RG_BAIKAAM) + ( (SHN.RG_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100)), 0) end)");
    sb.append("  when SHN.ZEIKBN = 1 or SHN.ZEIKBN = 2 then SHN.RG_BAIKAAM when SHN.ZEIKBN = 0 then (");
    sb.append("   case");
    sb.append("   when SHN.ZEIRTHENKODT <= " + szSyoridt + " then truncate(( (SHN.RG_BAIKAAM) + ((SHN.RG_BAIKAAM) * (ZRT.ZEIRT / 100))), 0)");
    sb.append("   when SHN.ZEIRTHENKODT > " + szSyoridt + " then truncate( (SHN.RG_BAIKAAM) + ( (SHN.RG_BAIKAAM) * (ZRT_OLD.ZEIRT / 100)), 0) end) end as SOBAIKA");
    sb.append(", case");
    sb.append("  when SHN.ZEIKBN = 3 then (");
    sb.append("   case");
    sb.append("   when BMN.ZEIRTHENKODT <= " + szSyoridt + " then truncate(( (SHN.RG_BAIKAAM) + ( (SHN.RG_BAIKAAM) * (ZRT_BMN.ZEIRT / 100))) / NULLIF(1 + (COALESCE(ZRT_BMN.ZEIRT, 0) / 100), 0), 0)");
    sb.append("   when BMN.ZEIRTHENKODT > " + szSyoridt
        + " then truncate(( (SHN.RG_BAIKAAM) + ( (SHN.RG_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100))) / NULLIF(1 + (COALESCE(ZRT_OLD_BMN.ZEIRT, 0) / 100), 0), 0) end)");
    sb.append("  when SHN.ZEIKBN = 1 or SHN.ZEIKBN = 2 then SHN.RG_BAIKAAM when SHN.ZEIKBN = 0 then (");
    sb.append("   case");
    sb.append("   when SHN.ZEIRTHENKODT <= " + szSyoridt + " then truncate(( (SHN.RG_BAIKAAM) + ( (SHN.RG_BAIKAAM) * (ZRT.ZEIRT / 100))) / NULLIF(1 + (COALESCE(ZRT.ZEIRT, 0) / 100), 0), 0)");
    sb.append("   when SHN.ZEIRTHENKODT > " + szSyoridt
        + " then truncate(( (SHN.RG_BAIKAAM) + ((SHN.RG_BAIKAAM) * (ZRT_OLD.ZEIRT / 100))) / NULLIF(1 + (COALESCE(ZRT_OLD.ZEIRT, 0) / 100), 0), 0) end) end as HTBAIKA");
    sb.append(" from INAMS.MSTSHN SHN");
    sb.append(" left join INAMS.MSTZEIRT ZRT on ZRT.ZEIRTKBN = SHN.ZEIRTKBN");
    sb.append(" left join INAMS.MSTZEIRT ZRT_OLD on ZRT_OLD.ZEIRTKBN = SHN.ZEIRTKBN_OLD");
    sb.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = SHN.BMNCD");
    sb.append(" left join INAMS.MSTZEIRT ZRT_BMN on ZRT_BMN.ZEIRTKBN = BMN.ZEIRTKBN");
    sb.append(" left join INAMS.MSTZEIRT ZRT_OLD_BMN on ZRT_OLD_BMN.ZEIRTKBN = BMN.ZEIRTKBN_OLD");
    sb.append(" where COALESCE(SHN.UPDKBN, 0) <> 1 and SHN.SHNCD = " + szShncd);
    sb.append(") T1");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sb.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
