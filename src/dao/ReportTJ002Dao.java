package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTJ002Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTJ002Dao(String JNDIname) {
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
   * 他画面からの呼び出し検索実行
   *
   * @return
   */
  public String createCommandSub(HashMap<String, String> map, User userInfo) {

    // ユーザー情報を設定
    super.setUserInfo(userInfo);

    // 検索条件などの情報を設定
    super.setMap(map);

    // 検索コマンド生成
    String command = createCommand();

    // 出力用検索条件生成
    outputQueryList();

    // 検索実行
    return command;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    ArrayList<String> paramData = new ArrayList<>();

    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String bmncd = szBmncd.substring(0, 2);
    paramData.add(szLstno);
    paramData.add(bmncd);
    paramData.add(tenpo);
    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);
    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" WITH WK as ");
    sbSQL.append(" ( select");
    sbSQL.append("   DAYOFWEEK(DATE_FORMAT(T3.JTDT_01, '%Y%m%d')) as DOW");
    sbSQL.append("   ,T3.JTDT_01 as D1");
    sbSQL.append("   ,T3.JTDT_02 as D2");
    sbSQL.append("   ,T3.JTDT_03 as D3");
    sbSQL.append("   ,T3.JTDT_04 as D4");
    sbSQL.append("   ,T3.JTDT_05 as D5");
    sbSQL.append("   ,T3.JTDT_06 as D6");
    sbSQL.append("   ,T3.JTDT_07 as D7");
    sbSQL.append("   ,T3.JTDT_08 as D8");
    sbSQL.append("   ,T3.JTDT_09 as D9");
    sbSQL.append("   ,T3.JTDT_10 as D10");
    sbSQL.append("   from");
    sbSQL.append("   INATK.TOKTJ T1");
    sbSQL.append("   left join INATK.TOKTJ_SHN T2");
    sbSQL.append("   on T2.LSTNO = ?");
    sbSQL.append("   and T2.BMNCD = ?");
    sbSQL.append("   left join INATK.TOKTJ_TEN T3");
    sbSQL.append("   on T3.BMNCD = T2.BMNCD");
    sbSQL.append("   and T3.LSTNO = T2.LSTNO");
    sbSQL.append("   and T3.TENCD = ?");
    sbSQL.append("   and T3.HYOSEQNO = T2.HYOSEQNO");
    sbSQL.append("   where");
    sbSQL.append("   T1.LSTNO = ?");
    sbSQL.append("   ORDER BY DOW ");
    sbSQL.append("   FETCH FIRST 1 ROWS ONLY");
    sbSQL.append(" ),");
    sbSQL.append(" WK2 as ");
    sbSQL.append(" ( select");
    sbSQL.append("  T2.DAIBRUIKN as DAI");
    sbSQL.append("  ,T2.DAICD as DAICD");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_MON IS NULL THEN null ELSE V1.URICMPRT_MON END as MON");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_TUE IS NULL THEN null ELSE V1.URICMPRT_TUE END as TUE");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_WED IS NULL THEN null ELSE V1.URICMPRT_WED END as WED");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_THU IS NULL THEN null ELSE V1.URICMPRT_THU END as THU");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_FRI IS NULL THEN null ELSE V1.URICMPRT_FRI END as FRI");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_SAT IS NULL THEN null ELSE V1.URICMPRT_SAT END as SAT");
    sbSQL.append("  ,CASE WHEN V1.URICMPRT_SUN IS NULL THEN null ELSE V1.URICMPRT_SUN END as SUN");
    sbSQL.append("  from");
    sbSQL.append("  ( select * from INATK.TOKTJ_DFCMPRT T1");
    sbSQL.append("    where T1.TENCD = ?");
    sbSQL.append("  ) as V1");
    sbSQL.append("  right join INAMS.MSTDAIBRUI T2");
    sbSQL.append("   on T2.BMNCD = V1.BMNCD");
    sbSQL.append("   and T2.DAICD = V1.DAICD");
    sbSQL.append("  WHERE T2.BMNCD = ?");
    sbSQL.append("  order by T2.DAICD");
    sbSQL.append("  ),");
    sbSQL.append(" WK3 as ");
    sbSQL.append(" ( select ");
    sbSQL.append(" T1.DAICD as DAICD");
    sbSQL.append(" ,T3.DAIBRUIKN as DAIKN");
    sbSQL.append(" ,T1.TJDT as DT");
    sbSQL.append(" ,T1.URICMPRT as URI");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ_CMPRT T1");
    sbSQL.append(" left join INATK.TOKTJ_DFCMPRT T2");
    sbSQL.append(" on T2.TENCD = T1.TENCD");
    sbSQL.append(" and T2.BMNCD = T1.BMNCD");
    sbSQL.append(" and T2.DAICD = T1.DAICD");
    sbSQL.append(" left join INAMS.MSTDAIBRUI T3");
    sbSQL.append(" on T3.BMNCD = T1.BMNCD");
    sbSQL.append(" and T3.DAICD = T1.DAICD");
    sbSQL.append(" where");
    sbSQL.append(" T1.LSTNO = ?");
    sbSQL.append(" and T1.TENCD = ?");
    sbSQL.append(" and T1.BMNCD = ?");
    sbSQL.append(" order by T1.DAICD,T1.TJDT");
    sbSQL.append(" ),");
    sbSQL.append(" DEF as ");
    sbSQL.append(" ( select ");
    sbSQL.append("  WK2.DAICD DAICD");
    sbSQL.append("  ,WK2.DAI F1");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.SUN when 2 then WK2.MON when 3 then WK2.TUE when 4 then WK2.WED when 5 then WK2.THU when 6 then WK2.FRI when 7 then WK2.SAT END F2");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.MON when 2 then WK2.TUE when 3 then WK2.WED when 4 then WK2.THU when 5 then WK2.FRI when 6 then WK2.SAT when 7 then WK2.SUN END F3");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.TUE when 2 then WK2.WED when 3 then WK2.THU when 4 then WK2.FRI when 5 then WK2.SAT when 6 then WK2.SUN when 7 then WK2.MON END F4");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.WED when 2 then WK2.THU when 3 then WK2.FRI when 4 then WK2.SAT when 5 then WK2.SUN when 6 then WK2.MON when 7 then WK2.TUE END F5");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.THU when 2 then WK2.FRI when 3 then WK2.SAT when 4 then WK2.SUN when 5 then WK2.MON when 6 then WK2.TUE when 7 then WK2.WED END F6");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.FRI when 2 then WK2.SAT when 3 then WK2.SUN when 4 then WK2.MON when 5 then WK2.TUE when 6 then WK2.WED when 7 then WK2.THU END F7");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.SAT when 2 then WK2.SUN when 3 then WK2.MON when 4 then WK2.TUE when 5 then WK2.WED when 6 then WK2.THU when 7 then WK2.FRI END F8");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.SUN when 2 then WK2.MON when 3 then WK2.TUE when 4 then WK2.WED when 5 then WK2.THU when 6 then WK2.FRI when 7 then WK2.SAT END F9");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.MON when 2 then WK2.TUE when 3 then WK2.WED when 4 then WK2.THU when 5 then WK2.FRI when 6 then WK2.SAT when 7 then WK2.SUN END F10");
    sbSQL.append("  ,case WK.DOW when 1 then WK2.TUE when 2 then WK2.WED when 3 then WK2.THU when 4 then WK2.FRI when 5 then WK2.SAT when 6 then WK2.SUN when 7 then WK2.MON END F11");
    sbSQL.append("  from");
    sbSQL.append("  WK, WK2");
    sbSQL.append(" ),");
    sbSQL.append(" DATA as ");
    sbSQL.append(" ( select ");
    sbSQL.append(" A1 as F1, B2 as F2, C2 as F3, D2 as F4, E2 as F5, F2 as F6, G2 as F7, H2 as F8, I2 as F9, J2 as F10, K2 as F11");
    sbSQL.append(" from");
    sbSQL.append(" ( select distinct DAIKN as A1 from WK3) as A");
    sbSQL.append(" left join (select WK3.DAIKN as B1, WK3.URI as B2 from WK3,WK where WK3.DT=WK.D1) as B on A1=B1");
    sbSQL.append(" left join (select WK3.DAIKN as C1, WK3.URI as C2 from WK3,WK where WK3.DT=WK.D2) as C on A1=C1");
    sbSQL.append(" left join (select WK3.DAIKN as D1, WK3.URI as D2 from WK3,WK where WK3.DT=WK.D3) as D on A1=D1");
    sbSQL.append(" left join (select WK3.DAIKN as E1, WK3.URI as E2 from WK3,WK where WK3.DT=WK.D4) as E on A1=E1");
    sbSQL.append(" left join (select WK3.DAIKN as F1, WK3.URI as F2 from WK3,WK where WK3.DT=WK.D5) as F on A1=F1");
    sbSQL.append(" left join (select WK3.DAIKN as G1, WK3.URI as G2 from WK3,WK where WK3.DT=WK.D6) as G on A1=G1");
    sbSQL.append(" left join (select WK3.DAIKN as H1, WK3.URI as H2 from WK3,WK where WK3.DT=WK.D7) as H on A1=H1");
    sbSQL.append(" left join (select WK3.DAIKN as I1, WK3.URI as I2 from WK3,WK where WK3.DT=WK.D8) as I on A1=I1");
    sbSQL.append(" left join (select WK3.DAIKN as J1, WK3.URI as J2 from WK3,WK where WK3.DT=WK.D9) as J on A1=J1");
    sbSQL.append(" left join (select WK3.DAIKN as K1, WK3.URI as K2 from WK3,WK where WK3.DT=WK.D10) as K on A1=K1");
    sbSQL.append(" )");
    sbSQL.append(" select ");
    sbSQL.append(" A.F1 as F1");
    sbSQL.append(" ,case when B.F2  is null then A.F2  else B.F2 END  as F2");
    sbSQL.append(" ,case when B.F3  is null then A.F3  else B.F3 END  as F3");
    sbSQL.append(" ,case when B.F4  is null then A.F4  else B.F4 END  as F4");
    sbSQL.append(" ,case when B.F5  is null then A.F5  else B.F5 END  as F5");
    sbSQL.append(" ,case when B.F6  is null then A.F6  else B.F6 END  as F6");
    sbSQL.append(" ,case when B.F7  is null then A.F7  else B.F7 END  as F7");
    sbSQL.append(" ,case when B.F8  is null then A.F8  else B.F8 END  as F8");
    sbSQL.append(" ,case when B.F9  is null then A.F9  else B.F9 END  as F9");
    sbSQL.append(" ,case when B.F10 is null then A.F10 else B.F10 END as F10");
    sbSQL.append(" ,case when B.F11 is null then A.F11 else B.F11 END as F11");
    sbSQL.append(" ,A.DAICD as F12");
    sbSQL.append(" from");
    sbSQL.append(" ( select * from DEF) as A");
    sbSQL.append(" left join (select * from DATA) as B on A.F1=B.F1");
    sbSQL.append(" order by A.DAICD");

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

  }
}
