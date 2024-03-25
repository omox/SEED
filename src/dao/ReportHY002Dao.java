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
public class ReportHY002Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportHY002Dao(String JNDIname) {
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

    String kkkcd = getMap().get("KKKCD"); // 企画N0
    String tencd = userInfo.getTenpo(); // 担当店舗
    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(" select");
    sbSQL.append(" T1.CATALGNO"); // F1 : カタログNo
    sbSQL.append(", TRIM(left (T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5))"); // F2 : 商品コード
    sbSQL.append(", T1.SHNKN"); // F3 : 商品名
    sbSQL.append(", T1.HTSU_T"); // F4 : 発注数累計
    sbSQL.append(", T1.UKESTDT || W1.JWEEK"); // F5 : 受付期間－開始日
    sbSQL.append(", T1.UKEEDDT || W2.JWEEK"); // F6 : 受付期間ー終了日
    sbSQL.append(", T1.TENISTDT || W3.JWEEK"); // F7 : 店舗入力期間－開始日
    sbSQL.append(", T1.TENIEDDT || W4.JWEEK"); // F7 : 店舗入力期間－終了日
    sbSQL.append(", T1.HTDT || W7.JWEEK"); // F8 : 発注日
    sbSQL.append(", T1.MIN_NNDT || W5.JWEEK"); // F9 : 納入期間－開始日
    sbSQL.append(", T1.MAX_NNDT || W6.JWEEK"); // F10 : 納入期間－終了日
    sbSQL.append(", T1.YOTEISU"); // F11 : 全店－予定数
    sbSQL.append(", T1.HTSU"); // F12 : 全店－累計
    sbSQL.append(" from (select");
    sbSQL.append(" YHS.CATALGNO");
    sbSQL.append(", YHS.SHNCD");
    sbSQL.append(", SHN.SHNKN");
    sbSQL.append(", case when YHT.HTSU_T = 0 then null else YHT.HTSU_T END as HTSU_T");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHS.UKESTDT, 6), '%y/%m/%d'), '%y/%m/%d') as UKESTDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHS.UKESTDT, 6), '%Y%m%d')) as UKESTDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHS.UKEEDDT, 6), '%y/%m/%d'), '%y/%m/%d') as UKEEDDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHS.UKEEDDT, 6), '%Y%m%d')) as UKEEDDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHS.TENISTDT, 6), '%y/%m/%d'), '%y/%m/%d') as TENISTDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHS.TENISTDT, 6), '%Y%m%d')) as TENISTDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHS.TENIEDDT, 6), '%y/%m/%d'), '%y/%m/%d') as TENIEDDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHS.TENIEDDT, 6), '%Y%m%d')) as TENIEDDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHN.MIN_NNDT, 6), '%y/%m/%d'), '%y/%m/%d') as MIN_NNDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHN.MIN_NNDT, 6), '%Y%m%d')) as MIN_NNDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHN.MAX_NNDT, 6), '%y/%m/%d'), '%y/%m/%d') as MAX_NNDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHN.MAX_NNDT, 6), '%Y%m%d')) as MAX_NNDT_WNUM");
    sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHS.HTDT, 6), '%y/%m/%d'), '%y/%m/%d') as HTDT");
    sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHS.HTDT, 6), '%Y%m%d')) as HTDT_WNUM");
    sbSQL.append(", YHS.YOTEISU, YHT.HTSU from INATK.HATYH_KKK YHK left join INATK.HATYH_SHN YHS on YHS.KKKCD = YHK.KKKCD");
    sbSQL.append(
        " left join (select KKKCD, SHNCD, SUM(HTSU) as HTSU, SUM(case when TENCD = ? then HTSU else 0 end) as HTSU_T from INATK.HATYH_TEN group by KKKCD, SHNCD) YHT on YHT.KKKCD = YHS.KKKCD and YHT.SHNCD = YHS.SHNCD");
    paramData.add(tencd);
    sbSQL.append(" left join (select KKKCD, SHNCD, MAX(NNDT) as MAX_NNDT, MIN(NNDT) as MIN_NNDT from INATK.HATYH_NNDT group by KKKCD, SHNCD) YHN on YHN.KKKCD = YHS.KKKCD and YHN.SHNCD = YHS.SHNCD");
    sbSQL.append(" left join INAMS.MSTSHN SHN on SHN.SHNCD = YHS.SHNCD");
    sbSQL.append(" where YHS.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and YHS.KKKCD = ?");
    paramData.add(kkkcd);
    sbSQL.append(") T1");
    sbSQL.append(" left outer join WEEK W1 on T1.UKESTDT_WNUM = W1.CWEEK");
    sbSQL.append(" left outer join WEEK W2 on T1.UKEEDDT_WNUM = W2.CWEEK");
    sbSQL.append(" left outer join WEEK W3 on T1.TENISTDT_WNUM = W3.CWEEK");
    sbSQL.append(" left outer join WEEK W4 on T1.TENIEDDT_WNUM = W4.CWEEK");
    sbSQL.append(" left outer join WEEK W5 on T1.MIN_NNDT_WNUM = W5.CWEEK");
    sbSQL.append(" left outer join WEEK W6 on T1.MAX_NNDT_WNUM = W6.CWEEK");
    sbSQL.append(" left outer join WEEK W7 on T1.HTDT_WNUM = W7.CWEEK");
    sbSQL.append(" order by T1.CATALGNO, T1.SHNCD");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String kkkcd = map.get("KKKCD"); // 催し区分

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" ");

    sbSQL.append("select");
    sbSQL.append(" right ('0000' || KKKCD, 4) as F1");
    sbSQL.append(", KKKKM as F2");
    sbSQL.append(" from INATK.HATYH_KKK");
    sbSQL.append(" where KKKCD = " + kkkcd);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
