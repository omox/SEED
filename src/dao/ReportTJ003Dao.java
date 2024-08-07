package dao;

import java.math.BigDecimal;
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
public class ReportTJ003Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTJ003Dao(String JNDIname) {
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

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    ArrayList<String> paramData = new ArrayList<>();

    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String listno = szLstno.replace("-", "");
    String bmncd = szBmncd.substring(0, 2);
    int cnt = 0;

    if (getMap().get("callpage").equals(DefineReport.ID_PAGE_TJ005)) {
      // オプション情報（タイトル）設定
      cnt = getMstDaiBrui(bmncd);
    }

    paramData.add(listno);
    paramData.add(bmncd);
    paramData.add(tenpo);
    paramData.add(listno);

    sbSQL.append("WITH WEEK as ( select CWEEK , JWEEK as JWEEK from ( values ROW(1, '日') , ROW(2, '月') , ROW(3, '火') , ROW(4, '水') , ROW(5, '木') , ROW(6, '金') , ROW(7, '土') ) as TMP(CWEEK, JWEEK) )");
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
    sbSQL.append(" ,T3.JTDT_01 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_01, '%Y%m%d'))) as X1");
    sbSQL.append(" ,T3.JTDT_02 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_02, '%Y%m%d'))) as X2");
    sbSQL.append(" ,T3.JTDT_03 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_03, '%Y%m%d'))) as X3");
    sbSQL.append(" ,T3.JTDT_04 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_04, '%Y%m%d'))) as X4");
    sbSQL.append(" ,T3.JTDT_05 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_05, '%Y%m%d'))) as X5");
    sbSQL.append(" ,T3.JTDT_06 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_06, '%Y%m%d'))) as X6");
    sbSQL.append(" ,T3.JTDT_07 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_07, '%Y%m%d'))) as X7");
    sbSQL.append(" ,T3.JTDT_08 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_08, '%Y%m%d'))) as X8");
    sbSQL.append(" ,T3.JTDT_09 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_09, '%Y%m%d'))) as X9");
    sbSQL.append(" ,T3.JTDT_10 || '-' || (select CWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T3.JTDT_10, '%Y%m%d'))) as X10");
    sbSQL.append(" , " + cnt + " as CNT");
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
    sbSQL.append(" where ");
    sbSQL.append("  T1.LSTNO = ? ");
    sbSQL.append("  order by T3.HYOSEQNO");

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  public int getMstDaiBrui(String bmnCd) {

    ArrayList<String> paramData = new ArrayList<>();
    paramData.add(bmnCd);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append("  COUNT(DAICD) AS CNT ");
    sbSQL.append(" from");
    sbSQL.append(" INAMS.MSTDAIBRUI ");
    sbSQL.append(" where");
    sbSQL.append(" BMNCD = ?");
    sbSQL.append(" AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (array.size() != 0) {
      return array.optJSONObject(0).optInt("CNT");
    }
    return 0;
  }

  private String createCommand2() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    // メインデータ作成
    JSONArray wkArray = getTOKTJSHNDTWKData(getMap()); // 発注明細(ワーク)
    // ワークにデータが存在しない場合空を返却
    if (wkArray.size() == 0) {
      return "";
    }

    JSONArray bmnysanArray = getBMNYSANData(getMap()); // 部門予算
    JSONArray cmprtArray = getCMPRTData(getMap()); // 構成比

    ArrayList<String> paramData = new ArrayList<>();

    // 部門予算のデータを key:日付、value:部門予算 に変更
    HashMap<String, String> bmnysanMap = new HashMap<>();
    for (int i = 0; i < bmnysanArray.size(); i++) {
      String key = bmnysanArray.optJSONObject(i).optString("TJDT");
      String val = StringUtils.isEmpty(bmnysanArray.optJSONObject(i).optString("BMNYSANAM")) ? "0" : bmnysanArray.optJSONObject(i).optString("BMNYSANAM");
      bmnysanMap.put(key, val);
    }

    // 構成比のデータを key:大分類+-+日付、value:部門予算 に変更
    HashMap<String, String> cmprtMap = new HashMap<>();
    for (int i = 0; i < cmprtArray.size(); i++) {
      String key = cmprtArray.optJSONObject(i).optString("DAICD") + "-" + cmprtArray.optJSONObject(i).optString("TJDT");
      String val = StringUtils.isEmpty(cmprtArray.optJSONObject(i).optString("URICMPRT")) ? "0.0" : cmprtArray.optJSONObject(i).optString("URICMPRT");
      cmprtMap.put(key, val);
    }

    // 全ての情報を集約
    String values = "(values";

    for (int i = 0; i < wkArray.size(); i++) {

      JSONObject data = wkArray.getJSONObject(i);

      // 発注売価、売上予算、荒利をソート番号、期間計で保持
      HashMap<Integer, BigDecimal> kikanKei = new HashMap<>();
      // 10日分の発注売価、売上予算、荒利を kye:ソート番号-日付、value:金額 で保持
      HashMap<String, BigDecimal> bmnKei = new HashMap<>();

      for (int sortNum = 1; sortNum <= 4; sortNum++) {

        // ソート番号と大分類
        String getDaiCd = data.optString("DAICD");
        values += "(CAST(? AS SMALLINT), CAST(? AS INTEGER), CAST(? AS VARCHAR(30)),";
        paramData.add(String.valueOf(getDaiCd));
        paramData.add(String.valueOf(sortNum));
        paramData.add(data.optString("DAIBRUIKN"));

        // 項目名
        values += "CAST(? AS VARCHAR(10)),";
        if (sortNum == 1) {
          paramData.add("売上予算");
        } else if (sortNum == 2) {
          paramData.add("発注売価");
        } else if (sortNum == 3) {
          paramData.add("予算比");
        } else if (sortNum == 4) {
          paramData.add("値入率");
        }

        for (int jtDt = 1; jtDt <= 10; jtDt++) {
          String getJtDt = jtDt == 10 ? data.optString("JTDT_10") : data.optString("JTDT_0" + String.valueOf(jtDt));
          String key = String.valueOf(jtDt);

          // 売上予算作成
          if (sortNum == 1) {
            BigDecimal bmnyosan = bmnysanMap.containsKey(getJtDt) ? new BigDecimal(bmnysanMap.get(getJtDt)) : new BigDecimal("0.0");
            BigDecimal uricmprt = cmprtMap.containsKey(getDaiCd + "-" + getJtDt) ? new BigDecimal(cmprtMap.get(getDaiCd + "-" + getJtDt)) : new BigDecimal("0.0");
            BigDecimal setVal = bmnyosan.multiply(uricmprt).divide(new BigDecimal("100"), 0, BigDecimal.ROUND_HALF_UP);

            // 小数点第一位四捨五入
            values += "CAST(? AS INTEGER),";
            paramData.add(String.valueOf(setVal));

            // 売上予算を日付別に保持
            bmnKei.put(sortNum + "-" + key, setVal);

            // 期間計(予算費用)
            if (kikanKei.containsKey(sortNum)) {
              BigDecimal val = kikanKei.get(sortNum);
              kikanKei.replace(sortNum, val.add(setVal));
            } else {
              kikanKei.put(sortNum, setVal);
            }
            // 発注売価作成
          } else if (sortNum == 2) {
            values += "CAST(? AS INTEGER),";

            BigDecimal setVal = new BigDecimal(jtDt == 10 ? data.optString("NNBAIKA_10") : data.optString("NNBAIKA_0" + String.valueOf(jtDt)));

            // 期間計(値入率用)
            if (kikanKei.containsKey(sortNum + 4)) {
              BigDecimal val = kikanKei.get(sortNum + 4);
              kikanKei.replace(sortNum + 4, val.add(setVal));
            } else {
              kikanKei.put(sortNum + 4, setVal);
            }

            setVal = setVal.setScale(0, BigDecimal.ROUND_HALF_UP);
            paramData.add(String.valueOf(setVal));

            // 発注売価を日付別に保持
            bmnKei.put(sortNum + "-" + key, setVal);

            // 期間計(予算費用)
            if (kikanKei.containsKey(sortNum)) {
              BigDecimal val = kikanKei.get(sortNum);
              kikanKei.replace(sortNum, val.add(setVal));
            } else {
              kikanKei.put(sortNum, setVal);
            }
            // 予算比作成
          } else if (sortNum == 3) {

            BigDecimal setVal = new BigDecimal(jtDt == 10 ? data.optString("NNBAIKA_10") : data.optString("NNBAIKA_0" + String.valueOf(jtDt)));
            // 予算比の部分に値入率計算で使用する売価を保管
            bmnKei.put(sortNum + "-" + key, setVal);

            values += "CAST(? AS VARCHAR(10)),";
            BigDecimal getValA = bmnKei.containsKey("2-" + key) ? bmnKei.get("2-" + key) : new BigDecimal("0");
            BigDecimal getValB = bmnKei.containsKey("1-" + key) ? bmnKei.get("1-" + key) : new BigDecimal("0");

            // 小数点第二位で四捨五入
            if (getValB.signum() == 0) {
              paramData.add(String.valueOf(getValB));
            } else {
              paramData.add(String.valueOf(getValA.divide(getValB, 5, BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_HALF_UP)));
            }
            // 値入率作成
          } else if (sortNum == 4) {
            values += "CAST(? AS VARCHAR(10)),";

            BigDecimal getValA = new BigDecimal(jtDt == 10 ? data.optString("ARARI_10") : data.optString("ARARI_0" + String.valueOf(jtDt)));
            BigDecimal getValB = new BigDecimal(jtDt == 10 ? data.optString("NNBAIKA_10") : data.optString("NNBAIKA_0" + String.valueOf(jtDt)));

            if (getValB.signum() == 0) {
              paramData.add(String.valueOf(getValB));
            } else {
              paramData.add(String.valueOf(getValA.divide(getValB, 5, BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_HALF_UP)));
            }

            // 荒利を日付別に保持
            bmnKei.put(sortNum + "-" + key, getValA);

            // 期間計
            if (kikanKei.containsKey(sortNum)) {
              BigDecimal val = kikanKei.get(sortNum);
              kikanKei.replace(sortNum, val.add(getValA));
            } else {
              kikanKei.put(sortNum, getValA);
            }

          }
        }

        if (sortNum == 1 || sortNum == 2) {
          values += "CAST(? AS INTEGER),";
          paramData.add(String.valueOf(kikanKei.get(sortNum)));
        } else {
          values += "CAST(? AS VARCHAR(10)),";
          BigDecimal getHtBaika = kikanKei.containsKey(2) ? kikanKei.get(2) : new BigDecimal("0"); // 発注売価の期間計
          // 予算比
          if (sortNum == 3) {
            BigDecimal getUriYosan = kikanKei.containsKey(1) ? kikanKei.get(1) : new BigDecimal("0"); // 売上予算の期間計

            // 小数点第二位で四捨五入
            if (getUriYosan.signum() == 0) {
              paramData.add(String.valueOf(getUriYosan));
            } else {
              paramData.add(String.valueOf(getHtBaika.divide(getUriYosan, 5, BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_HALF_UP)));
            }
            // 値入率
          } else {
            BigDecimal getArari = kikanKei.containsKey(sortNum) ? kikanKei.get(sortNum) : new BigDecimal("0"); // 荒利の期間計
            getHtBaika = kikanKei.containsKey(2 + sortNum) ? kikanKei.get(2 + sortNum) : new BigDecimal("0"); // 発注売価の期間計

            if (getHtBaika.signum() == 0) {
              paramData.add(String.valueOf(getHtBaika));
            } else {
              paramData.add(String.valueOf(getArari.divide(getHtBaika, 5, BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_HALF_UP)));
            }
          }
        }

        for (int jtDt = 1; jtDt <= 10; jtDt++) {
          String key = sortNum + "-" + String.valueOf(jtDt);
          // 部門計用
          if (sortNum == 4 || sortNum == 3) {
            values += "CAST(? AS VARCHAR(10))";
          } else {
            values += "CAST(? AS INTEGER)";
          }
          paramData.add(String.valueOf(bmnKei.get(key)));
          // 一番最後のレコード以外カンマをつける
          if (jtDt == 10) {
            values += ")";
          } else {
            values += ",";
          }
        }

        // 一番最後のレコード以外カンマをつける
        if (sortNum == 4 && i + 1 == wkArray.size()) {
          values += ")";
        } else {
          values += ",";
        }
      }
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("select * from " + values + " as ");
    sbSQL.append("(F1 ,F2 ,F3 ,F4 ");
    sbSQL.append(",F5 ,F6 ,F7 ,F8 ,F9 ,F10 ,F11 ,F12 ,F13 ,F14 ,F15 "); // 日付別+期間
    sbSQL.append(",F16 ,F17 ,F18 ,F19 ,F20 ,F21 ,F22 ,F23 ,F24 ,F25 ) "); // 日付別売上予算、発注売価、ダミー、荒利
    sbSQL.append("order by F1,F2 ");

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
    new ArrayList<String>();

    // 共通箇所設定
    createCmnOutput(jad);

  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getBMNYSANData(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    String listno = szLstno.replace("-", "");
    String bmncd = szBmncd.substring(0, 2);

    paramData.add(listno);
    paramData.add(tenpo);
    paramData.add(bmncd);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("  select");
    sbSQL.append("  T1.BMNYSANAM ");
    sbSQL.append(" ,T1.TJDT ");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ_BMNYSAN T1");
    sbSQL.append(" where");
    sbSQL.append("  T1.LSTNO = ?");
    sbSQL.append("  and T1.TENCD = ?");
    sbSQL.append("  and T1.BMNCD = ?");
    sbSQL.append("  order by T1.TJDT");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getCMPRTData(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    JSONArray arr = JSONArray.fromObject(getMap().get("INPDAYARR"));
    String listno = szLstno.replace("-", "");
    String bmncd = szBmncd.substring(0, 2);

    paramData.add(tenpo);
    paramData.add(bmncd);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("  select DAICD ");
    sbSQL.append(" ,URICMPRT_MON ");
    sbSQL.append(" ,URICMPRT_TUE ");
    sbSQL.append(" ,URICMPRT_WED ");
    sbSQL.append(" ,URICMPRT_THU ");
    sbSQL.append(" ,URICMPRT_FRI ");
    sbSQL.append(" ,URICMPRT_SAT ");
    sbSQL.append(" ,URICMPRT_SUN ");
    sbSQL.append(" from");
    sbSQL.append(" INATK.TOKTJ_DFCMPRT ");
    sbSQL.append(" where");
    sbSQL.append("  TENCD = ?");
    sbSQL.append("  and BMNCD = ?");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // 全ての情報を集約
    String values = "(values ROW(null,null,null))";
    paramData = new ArrayList<>();

    if (array.size() != 0) {
      values = "(values ROW";
    }

    for (int i = 0; i < array.size(); i++) {
      // 日付は固定値
      for (int j = 0; j < arr.size(); j++) {

        String tjDt = arr.getString(j).split("-")[0];
        String wk = arr.getString(j).split("-")[1];
        String daiCd = array.getJSONObject(i).containsKey("DAICD") ? array.getJSONObject(i).getString("DAICD") : "";

        values += "(CAST(? AS SMALLINT)";
        paramData.add(String.valueOf(daiCd));
        values += ",CAST(? AS INTEGER)";
        paramData.add(tjDt);
        values += ",CAST(? AS DECIMAL(5,2))) ";

        // 日曜日
        String uricmprt = "";
        if (wk.equals("1")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_SUN") ? array.getJSONObject(i).getString("URICMPRT_SUN") : "";

          // 月曜日
        } else if (wk.equals("2")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_MON") ? array.getJSONObject(i).getString("URICMPRT_MON") : "";

          // 火曜日
        } else if (wk.equals("3")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_TUE") ? array.getJSONObject(i).getString("URICMPRT_TUE") : "";

          // 水曜日
        } else if (wk.equals("4")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_WED") ? array.getJSONObject(i).getString("URICMPRT_WED") : "";

          // 木曜日
        } else if (wk.equals("5")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_THU") ? array.getJSONObject(i).getString("URICMPRT_THU") : "";

          // 金曜日
        } else if (wk.equals("6")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_FRI") ? array.getJSONObject(i).getString("URICMPRT_FRI") : "";

          // 土曜日
        } else if (wk.equals("7")) {
          uricmprt = array.getJSONObject(i).containsKey("URICMPRT_SAT") ? array.getJSONObject(i).getString("URICMPRT_SAT") : "";
        }
        paramData.add(uricmprt);

        if (arr.size() != (j + 1) || (arr.size() == (j + 1) && array.size() != (i + 1))) {
          values += ",";
        } else {
          values += ")";
        }
      }
    }

    sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append(" CASE WHEN T2.DAICD IS NULL THEN T1.DAICD ELSE T2.DAICD END AS DAICD");
    sbSQL.append(" ,CASE WHEN T2.URICMPRT IS NULL THEN T1.URICMPRT ELSE T2.URICMPRT END AS URICMPRT");
    sbSQL.append(" ,CASE WHEN T2.TJDT IS NULL THEN T1.TJDT ELSE T2.TJDT END AS TJDT");
    sbSQL.append(" from");
    sbSQL.append("(select DAICD, TJDT, URICMPRT from " + values + " as VT ");
    sbSQL.append("(DAICD,TJDT,URICMPRT)) T1 LEFT JOIN");
    sbSQL.append(" (select DAICD, TJDT, URICMPRT from INATK.TOKTJ_CMPRT ");
    sbSQL.append(" where");
    sbSQL.append("  LSTNO = ?");
    sbSQL.append("  and TENCD = ?");
    sbSQL.append("  and BMNCD = ?) AS T2 ON T1.DAICD=T2.DAICD AND T1.TJDT = T2.TJDT");
    paramData.add(listno);
    paramData.add(tenpo);
    paramData.add(bmncd);

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array2 = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array2;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKTJSHNDTWKData(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<>();

    User userInfo = getUserInfo();
    String tenpo = userInfo.getTenpo();
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szLstno = getMap().get("LSTNO"); // リスト№
    JSONArray arr = JSONArray.fromObject(getMap().get("INPDAYARR"));
    String bmncd = szBmncd.substring(0, 2);
    String listno = szLstno.replace("-", "");

    paramData.add(listno);
    paramData.add(tenpo);
    paramData.add(bmncd);

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" WITH WK AS(SELECT");
    sbSQL.append(" T1.LSTNO");
    sbSQL.append(" ,T1.TENCD");
    sbSQL.append(" ,T1.BMNCD");
    sbSQL.append(" ,T1.DAICD");
    sbSQL.append(" ,T1.IRISU_TB AS IRISU_TB ");
    sbSQL.append(" ,CASE WHEN T2.BAIKAAM_PACK IS NOT NULL AND T2.BAIKAAM_PACK <> 0 THEN T2.BAIKAAM_PACK ELSE T1.BAIKAAM_TB END AS BAIKAAM_TB");
    sbSQL.append(" ,CASE WHEN T2.GENKAAM_PACK IS NOT NULL AND T2.GENKAAM_PACK <> 0 THEN T2.GENKAAM_PACK ELSE T1.GENKAAM_MAE END AS GENKAAM_MAE");
    sbSQL.append(" ,CASE WHEN T1.HTSU_01=99999 THEN 0 ELSE T1.HTSU_01 END AS HTSU_01 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_02=99999 THEN 0 ELSE T1.HTSU_02 END AS HTSU_02 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_03=99999 THEN 0 ELSE T1.HTSU_03 END AS HTSU_03 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_04=99999 THEN 0 ELSE T1.HTSU_04 END AS HTSU_04 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_05=99999 THEN 0 ELSE T1.HTSU_05 END AS HTSU_05 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_06=99999 THEN 0 ELSE T1.HTSU_06 END AS HTSU_06 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_07=99999 THEN 0 ELSE T1.HTSU_07 END AS HTSU_07 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_08=99999 THEN 0 ELSE T1.HTSU_08 END AS HTSU_08 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_09=99999 THEN 0 ELSE T1.HTSU_09 END AS HTSU_09 ");
    sbSQL.append(" ,CASE WHEN T1.HTSU_10=99999 THEN 0 ELSE T1.HTSU_10 END AS HTSU_10 ");
    // 日付は固定値
    for (int i = 0; i < arr.size(); i++) {
      String col = (i + 1) == 10 ? "JTDT_10" : "JTDT_0" + String.valueOf(i + 1);
      sbSQL.append(" , '" + arr.getString(i).split("-")[0] + "' AS " + col);
    }
    if (arr.size() == 0) {
      sbSQL.append(" , T2.JTDT_01");
      sbSQL.append(" , T2.JTDT_02");
      sbSQL.append(" , T2.JTDT_03");
      sbSQL.append(" , T2.JTDT_04");
      sbSQL.append(" , T2.JTDT_05");
      sbSQL.append(" , T2.JTDT_06");
      sbSQL.append(" , T2.JTDT_07");
      sbSQL.append(" , T2.JTDT_08");
      sbSQL.append(" , T2.JTDT_09");
      sbSQL.append(" , T2.JTDT_10");
    } else if (arr.size() != 10) {
      for (int i = arr.size() + 1; i <= 10; i++) {
        String col = i == 10 ? "JTDT_10" : "JTDT_0" + (i + 1);
        sbSQL.append(" , null AS " + col);
      }
    }
    sbSQL.append(" FROM");
    sbSQL.append(" INATK.TOKTJ_SHNDT_WK T1 LEFT JOIN INATK.TOKTJ_TEN T2");
    sbSQL.append(" ON T1.LSTNO=T2.LSTNO AND T1.TENCD=T2.TENCD AND T1.BMNCD=T2.BMNCD AND T1.HYOSEQNO=T2.HYOSEQNO)");
    sbSQL.append(" SELECT T0.DAICD");
    sbSQL.append(" ,T0.DAIBRUIKN");
    sbSQL.append(" ,T1.LSTNO");
    sbSQL.append(" ,T1.TENCD");
    sbSQL.append(" ,T1.BMNCD");
    sbSQL.append(" ,T1.JTDT_01");
    sbSQL.append(" ,T1.JTDT_02");
    sbSQL.append(" ,T1.JTDT_03");
    sbSQL.append(" ,T1.JTDT_04");
    sbSQL.append(" ,T1.JTDT_05");
    sbSQL.append(" ,T1.JTDT_06");
    sbSQL.append(" ,T1.JTDT_07");
    sbSQL.append(" ,T1.JTDT_08");
    sbSQL.append(" ,T1.JTDT_09");
    sbSQL.append(" ,T1.JTDT_10");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_01*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_01");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_02*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_02");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_03*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_03");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_04*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_04");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_05*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_05");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_06*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_06");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_07*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_07");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_08*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_08");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_09*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_09");
    sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_10*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_10");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_01*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_01");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_02*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_02");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_03*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_03");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_04*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_04");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_05*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_05");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_06*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_06");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_07*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_07");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_08*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_08");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_09*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_09");
    sbSQL.append(" ,NVL(SUM(((T1.BAIKAAM_TB-T1.GENKAAM_MAE)*T1.HTSU_10*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS ARARI_10");
    sbSQL.append(" FROM");
    sbSQL.append(" INAMS.MSTDAIBRUI T0 LEFT JOIN WK T1");
    sbSQL.append(" ON T0.BMNCD=T1.BMNCD and T0.DAICD=T1.DAICD and T1.LSTNO = ? and T1.TENCD = ?");
    sbSQL.append(" WHERE T0.BMNCD = ? ");
    sbSQL.append(" GROUP BY T0.DAICD");
    sbSQL.append(" ,T0.DAIBRUIKN");
    sbSQL.append(" ,T1.LSTNO");
    sbSQL.append(" ,T1.TENCD");
    sbSQL.append(" ,T1.BMNCD");
    sbSQL.append(" ,T1.JTDT_01");
    sbSQL.append(" ,T1.JTDT_02");
    sbSQL.append(" ,T1.JTDT_03");
    sbSQL.append(" ,T1.JTDT_04");
    sbSQL.append(" ,T1.JTDT_05");
    sbSQL.append(" ,T1.JTDT_06");
    sbSQL.append(" ,T1.JTDT_07");
    sbSQL.append(" ,T1.JTDT_08");
    sbSQL.append(" ,T1.JTDT_09");
    sbSQL.append(" ,T1.JTDT_10");
    sbSQL.append(" ORDER BY T0.DAICD");

    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }
}
