package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTJ006Dao extends ItemDao {

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
  public ReportTJ006Dao(String JNDIname) {
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

  /**
   * 他画面からの呼び出し検索実行
   *
   * @return
   */
  public String createCommandSub2(HashMap<String, String> map, User userInfo) {

    // ユーザー情報を設定
    super.setUserInfo(userInfo);

    // 検索条件などの情報を設定
    super.setMap(map);

    // 検索コマンド生成
    String command = createCommand2();

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

    StringBuffer sbSQL = new StringBuffer();

    ArrayList<String> paramData = new ArrayList<>();

    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String bmncd = szBmncd.substring(0, 2);
    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);
    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);
    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);
    paramData.add(szLstno);
    paramData.add(tenpo);
    paramData.add(bmncd);
    paramData.add(szLstno);
    paramData.add(bmncd);
    paramData.add(tenpo);
    paramData.add(szLstno);

    sbSQL
        .append("WITH WEEK as ( select CWEEK , JWEEK as JWEEK from ( values ROW(1, '日') , ROW(2, '月') , ROW(3, '火') , ROW(4, '水') , ROW(5, '木') , ROW(6, '金') , ROW(7, '土') ) as TMP(CWEEK, JWEEK) ),");
    sbSQL.append(" BMN as (SELECT TJDT,BMNYSANAM FROM INATK.TOKTJ_BMNYSAN WHERE LSTNO=? and TENCD=? and BMNCD=?)");
    sbSQL.append("  select");
    sbSQL.append(" DATE_FORMAT(DATE_FORMAT(T3.JTDT_01, '%Y%m%d'), '%m/%d') as F1");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_02, '%Y%m%d'), '%m/%d') as F2");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_03, '%Y%m%d'), '%m/%d') as F3");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_04, '%Y%m%d'), '%m/%d') as F4");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_05, '%Y%m%d'), '%m/%d') as F5");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_06, '%Y%m%d'), '%m/%d') as F6");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_07, '%Y%m%d'), '%m/%d') as F7");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_08, '%Y%m%d'), '%m/%d') as F8");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_09, '%Y%m%d'), '%m/%d') as F9");
    sbSQL.append(" ,DATE_FORMAT(DATE_FORMAT(T3.JTDT_10, '%Y%m%d'), '%m/%d') as F10");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_01, '%Y%m%d'))) as F11");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_02, '%Y%m%d'))) as F12");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_03, '%Y%m%d'))) as F13");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_04, '%Y%m%d'))) as F14");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_05, '%Y%m%d'))) as F15");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_06, '%Y%m%d'))) as F16");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_07, '%Y%m%d'))) as F17");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_08, '%Y%m%d'))) as F18");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_09, '%Y%m%d'))) as F19");
    sbSQL.append(" ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_10, '%Y%m%d'))) as F20");
    sbSQL.append(" ,(select DATE_FORMAT(ADDDT,'%y/%m/%d') from INATK.TOKTJ_CMPRT where LSTNO = ? and TENCD = ? and BMNCD = ? order by ADDDT asc LIMIT 1 ) as F21");
    sbSQL.append(" ,(select DATE_FORMAT(UPDDT,'%y/%m/%d') from INATK.TOKTJ_CMPRT where LSTNO = ? and TENCD = ? and BMNCD = ? order by UPDDT desc LIMIT 1 ) as F22");
    sbSQL.append(" ,(select OPERATOR from INATK.TOKTJ_CMPRT where LSTNO = ? and TENCD = ? and BMNCD = ? order by UPDDT desc LIMIT 1 ) as F23");
    sbSQL.append(" , B1.BMNYSANAM as F24");
    sbSQL.append(" , B2.BMNYSANAM as F25");
    sbSQL.append(" , B3.BMNYSANAM as F26");
    sbSQL.append(" , B4.BMNYSANAM as F27");
    sbSQL.append(" , B5.BMNYSANAM as F28");
    sbSQL.append(" , B6.BMNYSANAM as F29");
    sbSQL.append(" , B7.BMNYSANAM as F30");
    sbSQL.append(" , B8.BMNYSANAM as F31");
    sbSQL.append(" , B9.BMNYSANAM as F32");
    sbSQL.append(" , B10.BMNYSANAM as F33");
    sbSQL.append(" ,T3.JTDT_01 as X1");
    sbSQL.append(" ,T3.JTDT_02 as X2");
    sbSQL.append(" ,T3.JTDT_03 as X3");
    sbSQL.append(" ,T3.JTDT_04 as X4");
    sbSQL.append(" ,T3.JTDT_05 as X5");
    sbSQL.append(" ,T3.JTDT_06 as X6");
    sbSQL.append(" ,T3.JTDT_07 as X7");
    sbSQL.append(" ,T3.JTDT_08 as X8");
    sbSQL.append(" ,T3.JTDT_09 as X9");
    sbSQL.append(" ,T3.JTDT_10 as X10");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ T1");
    sbSQL.append(" left join INATK.TOKTJ_SHN T2");
    sbSQL.append(" on T2.LSTNO = ? ");
    sbSQL.append(" and T2.BMNCD = ? ");
    sbSQL.append(" left join INATK.TOKTJ_TEN T3");
    sbSQL.append(" on T3.BMNCD = T2.BMNCD");
    sbSQL.append(" and T3.LSTNO = T2.LSTNO");
    sbSQL.append(" and T3.TENCD = ? ");
    sbSQL.append(" and T3.HYOSEQNO = T2.HYOSEQNO ");
    sbSQL.append(" left join BMN B1 on B1.TJDT = T3.JTDT_01");
    sbSQL.append(" left join BMN B2 on B2.TJDT = T3.JTDT_02");
    sbSQL.append(" left join BMN B3 on B3.TJDT = T3.JTDT_03");
    sbSQL.append(" left join BMN B4 on B4.TJDT = T3.JTDT_04");
    sbSQL.append(" left join BMN B5 on B5.TJDT = T3.JTDT_05");
    sbSQL.append(" left join BMN B6 on B6.TJDT = T3.JTDT_06");
    sbSQL.append(" left join BMN B7 on B7.TJDT = T3.JTDT_07");
    sbSQL.append(" left join BMN B8 on B8.TJDT = T3.JTDT_08");
    sbSQL.append(" left join BMN B9 on B9.TJDT = T3.JTDT_09");
    sbSQL.append(" left join BMN B10 on B10.TJDT = T3.JTDT_10");
    sbSQL.append(" where ");
    sbSQL.append("  T1.LSTNO = ? ");
    sbSQL.append("  order by T3.HYOSEQNO  IS NULL ASC,T3.HYOSEQNO");
    sbSQL.append("  LIMIT 1 ");

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  private String createCommand2() {
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
    paramData.add(szLstno);
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
    sbSQL.append("   LIMIT 1 ");
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
    sbSQL.append("  ,T3.AVGPTANKA AVG");
    sbSQL.append("  from");
    sbSQL.append("  ( select * from INATK.TOKTJ_DFCMPRT T1");
    sbSQL.append("    where T1.TENCD = ?");
    sbSQL.append("  ) as V1");
    sbSQL.append("  right join INAMS.MSTDAIBRUI T2");
    sbSQL.append("   on T2.BMNCD = V1.BMNCD");
    sbSQL.append("   and T2.DAICD = V1.DAICD");
    sbSQL.append("  left join INATK.TOKTJ_AVGPTANKA T3");
    sbSQL.append("   on T3.TENCD = V1.TENCD");
    sbSQL.append("   and T3.BMNCD = V1.BMNCD");
    sbSQL.append("   and T3.DAICD = V1.DAICD");
    sbSQL.append("   and T3.LSTNO = ?");
    sbSQL.append("   WHERE T2.BMNCD = ?");
    sbSQL.append("  order by T2.DAICD");
    sbSQL.append("  ),");
    sbSQL.append(" WK3 as ");
    sbSQL.append(" ( select ");
    sbSQL.append(" T1.DAICD as DAICD");
    sbSQL.append(" ,T4.DAIBRUIKN as DAIKN");
    sbSQL.append(" ,T1.TJDT as DT");
    sbSQL.append(" ,T1.URICMPRT as URI");
    sbSQL.append(" ,T3.AVGPTANKA as AVG");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ_CMPRT T1");
    sbSQL.append(" left join INATK.TOKTJ_DFCMPRT T2");
    sbSQL.append(" on T2.TENCD = T1.TENCD");
    sbSQL.append(" and T2.BMNCD = T1.BMNCD");
    sbSQL.append(" and T2.DAICD = T1.DAICD");
    sbSQL.append(" left join INATK.TOKTJ_AVGPTANKA T3");
    sbSQL.append(" on T3.LSTNO = T1.LSTNO");
    sbSQL.append(" and T3.TENCD = T1.TENCD");
    sbSQL.append(" and T3.BMNCD = T1.BMNCD");
    sbSQL.append(" and T3.DAICD = T1.DAICD");
    sbSQL.append(" left join INAMS.MSTDAIBRUI T4");
    sbSQL.append(" on T4.BMNCD = T1.BMNCD");
    sbSQL.append(" and T4.DAICD = T1.DAICD");
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
    sbSQL.append("  ,WK2.AVG F12");
    sbSQL.append("  from");
    sbSQL.append("  WK, WK2");
    sbSQL.append(" ),");
    sbSQL.append(" DATA as ");
    sbSQL.append(" ( select ");
    sbSQL.append(" A1 as F1, B2 as F2, C2 as F3, D2 as F4, E2 as F5, F2 as F6, G2 as F7, H2 as F8, I2 as F9, J2 as F10, K2 as F11, B3 as F12");
    sbSQL.append(" from");
    sbSQL.append(" ( select distinct DAIKN as A1 from WK3) as A");
    sbSQL.append(" left join (select WK3.DAIKN as B1, WK3.URI as B2, WK3.AVG as B3 from WK3,WK where WK3.DT=WK.D1) as B on A1=B1");
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
    sbSQL.append(" ,case when B.F12 is null then (format(A.F12,0) || '円') else (format(B.F12,0) || '円') END as F12");
    sbSQL.append(" ,A.DAICD as F13");
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

  /**
   * 更新処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {

    JSONObject msgObj = new JSONObject();
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONArray dataDefArray = JSONArray.fromObject(map.get("DATADEF")); // 対象情報（デフォルト）
    JSONArray dataDefBmnArray = JSONArray.fromObject(map.get("DATABMN")); // 対象情報（部門予算）

    // 更新処理
    try {
      if (dataArray.size() > 0) {
        this.updateData(map, userInfo);
      }
      if (dataDefArray.size() > 0) {
        this.updateDataDef(map, userInfo);
      }
      if (dataDefBmnArray.size() > 0) {
        this.updateDataBmnYsanAm(map, userInfo);
      }

      // 登録処理
      ArrayList<Integer> countList = new ArrayList<>();
      if (sqlList.size() > 0) {
        countList = super.executeSQLs(sqlList, prmList);
      }

      if (StringUtils.isEmpty(getMessage())) {
        int count = 0;
        for (int i = 0; i < countList.size(); i++) {
          count += countList.get(i);

          if (DefineReport.ID_DEBUG_MODE)
            System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
        }
        if (count == 0) {
          msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
        } else {
          msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
        }
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }

    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
  }

  /**
   * 更新処理
   *
   * @return
   *
   * @throws Exception
   */
  private void updateData(HashMap<String, String> map, User userInfo) throws Exception {

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報

    ArrayList<String> paramData = new ArrayList<>();
    String szLstno = map.get("LSTNO"); // リスト№
    String szBmncd = map.get("BMNCD").substring(0, 2); // 部門コード
    String tenpoCd = userInfo.getTenpo(); // 店コード

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKTJ_CMPRT ( ");
    sbSQL.append(" LSTNO");
    sbSQL.append(",TENCD");
    sbSQL.append(",BMNCD");
    sbSQL.append(",DAICD");
    sbSQL.append(",TJDT");
    sbSQL.append(",URICMPRT");
    sbSQL.append(",TBCMPRT");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT");
    sbSQL.append(" )SELECT * FROM (SELECT");
    sbSQL.append(" LSTNO");
    sbSQL.append(",TENCD");
    sbSQL.append(",BMNCD");
    sbSQL.append(",DAICD");
    sbSQL.append(",TJDT");
    sbSQL.append(",URICMPRT");
    sbSQL.append(",NULL AS TBCMPRT");
    sbSQL.append(", '" + userId + "' AS OPERATOR ");
    sbSQL.append(", CURRENT_TIMESTAMP AS ADDDT ");
    sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT ");
    sbSQL.append(" FROM (values ROW(?,?,?,?,?,?)) as T1(");
    sbSQL.append(" LSTNO");
    sbSQL.append(",TENCD");
    sbSQL.append(",BMNCD");
    sbSQL.append(",DAICD");
    sbSQL.append(",TJDT");
    sbSQL.append(",URICMPRT))RE");
    sbSQL.append(" ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" URICMPRT = RE.URICMPRT");
    sbSQL.append(",OPERATOR = RE.OPERATOR");
    sbSQL.append(",UPDDT = RE.UPDDT");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    for (int i = 0; i < dataArray.size(); i++) {

      paramData = new ArrayList<>();
      paramData.add(szLstno);
      paramData.add(tenpoCd);
      paramData.add(szBmncd);
      paramData.add(dataArray.optJSONObject(i).optString("DAICD"));
      paramData.add(dataArray.optJSONObject(i).optString("TJDT"));
      if (StringUtils.isEmpty(dataArray.optJSONObject(i).optString("URICMPRT"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataArray.optJSONObject(i).optString("URICMPRT"));
      }

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_構成比");
    }
  }

  /**
   * 更新処理
   *
   * @return
   *
   * @throws Exception
   */
  private void updateDataDef(HashMap<String, String> map, User userInfo) throws Exception {

    // パラメータ確認
    JSONArray dataDefArray = JSONArray.fromObject(map.get("DATADEF")); // 対象情報

    ArrayList<String> paramData = new ArrayList<>();
    String szBmncd = map.get("BMNCD").substring(0, 2); // 部門コード
    String tenpoCd = userInfo.getTenpo(); // 店コード

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKTJ_DFCMPRT (");
    sbSQL.append(" TENCD");
    sbSQL.append(",BMNCD");
    sbSQL.append(",DAICD");
    sbSQL.append(",URICMPRT_MON");
    sbSQL.append(",URICMPRT_TUE");
    sbSQL.append(",URICMPRT_WED");
    sbSQL.append(",URICMPRT_THU");
    sbSQL.append(",URICMPRT_FRI");
    sbSQL.append(",URICMPRT_SAT");
    sbSQL.append(",URICMPRT_SUN");
    sbSQL.append(",TBCMPRT_MON");
    sbSQL.append(",TBCMPRT_TUE");
    sbSQL.append(",TBCMPRT_WED");
    sbSQL.append(",TBCMPRT_THU");
    sbSQL.append(",TBCMPRT_FRI");
    sbSQL.append(",TBCMPRT_SAT");
    sbSQL.append(",TBCMPRT_SUN");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT");
    sbSQL.append(" )SELECT * FROM (SELECT");
    sbSQL.append(" TENCD");
    sbSQL.append(",BMNCD");
    sbSQL.append(",DAICD");
    sbSQL.append(",URICMPRT_MON");
    sbSQL.append(",URICMPRT_TUE");
    sbSQL.append(",URICMPRT_WED");
    sbSQL.append(",URICMPRT_THU");
    sbSQL.append(",URICMPRT_FRI");
    sbSQL.append(",URICMPRT_SAT");
    sbSQL.append(",URICMPRT_SUN");
    sbSQL.append(",NULL AS TBCMPRT_MON");
    sbSQL.append(",NULL AS TBCMPRT_TUE");
    sbSQL.append(",NULL AS TBCMPRT_WED");
    sbSQL.append(",NULL AS TBCMPRT_THU");
    sbSQL.append(",NULL AS TBCMPRT_FRI");
    sbSQL.append(",NULL AS TBCMPRT_SAT");
    sbSQL.append(",NULL AS TBCMPRT_SUN");
    sbSQL.append(", '" + userId + "' AS OPERATOR ");
    sbSQL.append(", CURRENT_TIMESTAMP AS ADDDT ");
    sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT ");
    sbSQL.append(" FROM (values ROW(?,?,?,?,?,?,?,?,?,?)) as T1(");
    sbSQL.append(" TENCD");
    sbSQL.append(",BMNCD");
    sbSQL.append(",DAICD");
    sbSQL.append(",URICMPRT_MON");
    sbSQL.append(",URICMPRT_TUE");
    sbSQL.append(",URICMPRT_WED");
    sbSQL.append(",URICMPRT_THU");
    sbSQL.append(",URICMPRT_FRI");
    sbSQL.append(",URICMPRT_SAT");
    sbSQL.append(",URICMPRT_SUN))RE ");
    sbSQL.append(" ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" URICMPRT_MON = RE.URICMPRT_MON");
    sbSQL.append(",URICMPRT_TUE = RE.URICMPRT_TUE");
    sbSQL.append(",URICMPRT_WED = RE.URICMPRT_WED");
    sbSQL.append(",URICMPRT_THU = RE.URICMPRT_THU");
    sbSQL.append(",URICMPRT_FRI = RE.URICMPRT_FRI");
    sbSQL.append(",URICMPRT_SAT = RE.URICMPRT_SAT");
    sbSQL.append(",URICMPRT_SUN = RE.URICMPRT_SUN");
    sbSQL.append(",OPERATOR = RE.OPERATOR");
    sbSQL.append(",UPDDT = RE.UPDDT");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    for (int i = 0; i < dataDefArray.size(); i++) {

      paramData = new ArrayList<>();
      paramData.add(tenpoCd);
      paramData.add(szBmncd);
      paramData.add(dataDefArray.optJSONObject(i).optString("DAICD"));
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_MON"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_MON"));
      }
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_TUE"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_TUE"));
      }
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_WED"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_WED"));
      }
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_THU"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_THU"));
      }
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_FRI"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_FRI"));
      }
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_SAT"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_SAT"));
      }
      if (StringUtils.isEmpty(dataDefArray.optJSONObject(i).optString("URICMPRT_SUN"))) {
        paramData.add("0.00");
      } else {
        paramData.add(dataDefArray.optJSONObject(i).optString("URICMPRT_SUN"));
      }

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_デフォルト構成比");
    }
  }

  private void updateDataBmnYsanAm(HashMap<String, String> map, User userInfo) throws Exception {

    // パラメータ確認
    JSONArray dataArrayYSN = JSONArray.fromObject(map.get("DATABMN")); // 対象情報
    ArrayList<String> paramData = new ArrayList<>();
    String szLstNo = map.get("LSTNO"); // 部門コード
    String szBmncd = map.get("BMNCD").substring(0, 2); // 部門コード
    String tenpoCd = userInfo.getTenpo(); // 店コード

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String values = "";
    StringBuffer sbSQL = new StringBuffer();

    for (int i = 0; i < dataArrayYSN.size(); i++) {

      values += ",ROW(?,?,?,?,?)";

      paramData.add(szLstNo);
      paramData.add(tenpoCd);
      paramData.add(szBmncd);
      paramData.add(dataArrayYSN.getJSONObject(i).getString("TJDT"));
      paramData.add(dataArrayYSN.getJSONObject(i).getString("BMNYSANAM"));
    }
    if (dataArrayYSN.size() > 0) {
      values = StringUtils.removeStart(values, ",");
      sbSQL = new StringBuffer();
      sbSQL.append("INSERT INTO INATK.TOKTJ_BMNYSAN (");
      sbSQL.append(" LSTNO");
      sbSQL.append(",TENCD");
      sbSQL.append(",BMNCD");
      sbSQL.append(",TJDT");
      sbSQL.append(",BMNYSANAM");
      sbSQL.append(",OPERATOR");
      sbSQL.append(",ADDDT");
      sbSQL.append(",UPDDT)");
      sbSQL.append(" SELECT ");
      sbSQL.append(" LSTNO");
      sbSQL.append(",TENCD");
      sbSQL.append(",BMNCD");
      sbSQL.append(",TJDT");
      sbSQL.append(",BMNYSANAM");
      sbSQL.append(",OPERATOR");
      sbSQL.append(",ADDDT");
      sbSQL.append(",UPDDT ");
      sbSQL.append(" FROM (SELECT ");
      sbSQL.append(" BMNYSANAM"); // 発注数_01 F5
      sbSQL.append(",TJDT"); // 表示順番 F3
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",LSTNO"); // リスト番号 F1
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
      sbSQL.append(", CURRENT_TIMESTAMP AS ADDDT "); // 登録日：
      sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT "); // 更新日：
      sbSQL.append(" FROM (values " + values + " ) as T1(");
      sbSQL.append("LSTNO"); // リスト番号 F1
      sbSQL.append(",TENCD"); // 店コード F4
      sbSQL.append(",BMNCD"); // 部門 F2
      sbSQL.append(",TJDT"); // 表示順番 F3
      sbSQL.append(",BMNYSANAM)) RE"); // 発注数_01 F5
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append(" BMNYSANAM = RE.BMNYSANAM");
      sbSQL.append(", OPERATOR = RE.OPERATOR");
      sbSQL.append(", UPDDT = RE.UPDDT");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("事前発注_部門予算");

      // クリア
      paramData = new ArrayList<>();
      values = "";
    }

  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
