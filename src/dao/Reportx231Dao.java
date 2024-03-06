package dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx231Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx231Dao(String JNDIname) {
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
    String szHtdt = getMap().get("HTDT"); // 商品コード
    String szErrordiv = getMap().get("ERRORDIV"); // エラー区分
    String szTenkn = getMap().get("TENKN"); // 店コード
    String szSsrccd = getMap().get("SRCCD"); // 取引先コード
    String szCentercd = getMap().get("CENTERCD"); // センターコード
    String szShnkn = getMap().get("SHNKN"); // 商品区分
    JSONArray bumonArray = JSONArray.fromObject(getMap().get("BUMON")); // 部門
    JSONArray daiBunArray = JSONArray.fromObject(getMap().get("DAI_BUN")); // 大分類
    JSONArray chuBunArray = JSONArray.fromObject(getMap().get("CHU_BUN")); // 中分類
    String szSelBumon = StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonArray.join(","), "\"0", "\""), "\"", ""), ",");// 部門
    String szSelDaiBun = StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(daiBunArray.join(","), "\"0", "\""), "\"", ""), ",");// 大分類
    String szSelChuBun = StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(chuBunArray.join(","), "\"0", "\""), "\"", ""), ","); // 中分類
    String btnId = getMap().get("BTN"); // 実行ボタン
    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<>();

    // 一覧表情報
    // TODO:天気・気温情報今適当
    StringBuffer sb = new StringBuffer();
    sb.append(DefineReport.ID_SQL_CMN_WEEK);
    sb.append(" select ");
    sb.append(" SUBSTRING(right ('0000000' || RTRIM(T1.PRODUCTSCD), 8), 1, 4) || SUBSTRING(right ('0000000' || RTRIM(T1.PRODUCTSCD), 8), 5, 4) "); // 取引先コード
    sb.append(" ,T1.PRODUCTSKANJI "); // 商品名
    sb.append(" ,T2.NMKN "); // 商品区分
    sb.append(" ,FLOOR(T1.ORDERUNITQTY) "); // 入数
    sb.append(" ,FLOOR(T1.QTYPERUNIT) "); // 発注数
    sb.append(" ,FLOOR(T1.QTYPERUNIT) * FLOOR(T1.ORDERUNITQTY) "); // 発注バラ数
    sb.append(" ,DATE_FORMAT(DATE_FORMAT(T1.DELIVERYDATE, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.DELIVERYDATE, '%Y%m%d'))) "); // 納品日
    sb.append(" ,case when  ERRORDIV = 0 then ' ' when ERRORDIV <> 0 then ERRORDIV END "); // エラー区分
    sb.append(" ,case when  ERRORDIV = 0 then ' ' when ERRORDIV <> 0 then ERRORMESSAGE END "); // エラーメッセージ
    sb.append(" ,right ('000' || RTRIM(T1.CENTERCD), 3) "); // センターコード
    sb.append(" ,right ('000000' || RTRIM(T1.SUPPLIERCD), 6) "); // 取引先コード
    sb.append(" from INAAD.HATKEKKA T1");
    sb.append(" left join INAMS.MSTMEISHO T2");
    sb.append(" on T2.MEISHOCD = T1.PRODUCTDIV");
    sb.append(" and T2.MEISHOKBN = 910010");
    sb.append(" where ");
    sb.append(" T1.ORDERSTORECD = ? "); // 発注企業店舗コード
    paramData.add(String.format("%3s", szTenkn).replace(" ", "0"));
    if (!StringUtils.isEmpty(szHtdt)) {
      sb.append(" and  T1.ORDERDATE = ? "); // 発注年月日
      paramData.add(szHtdt);
    }

    if (!szErrordiv.equals("-1")) {
      if (szErrordiv.equals("1")) {
        sb.append(" and  T1.ERRORDIV <> 0 "); // エラー区分
      } else {
        sb.append(" and  T1.ERRORDIV = 0 "); // エラー区分
      }
    }
    if (!szShnkn.equals("-1")) {
      sb.append(" and  T1.PRODUCTDIV = ? "); // 商品区分
      paramData.add(szShnkn);
    }
    if (!StringUtils.isEmpty(szCentercd)) {
      sb.append(" and  T1.CENTERCD = ? "); // センターコード
      paramData.add(szCentercd);
    }
    if (!StringUtils.isEmpty(szSsrccd)) {
      sb.append(" and  T1.SUPPLIERCD = ? "); // 取引先コード
      paramData.add(szSsrccd);
    }
    if (!szSelBumon.equals("-1")) {
      sb.append(" and  T1.CATEGORY1CD = ? "); // 部門
      paramData.add(szSelBumon);
    }
    if (!szSelDaiBun.equals("-1")) {
      sb.append(" and  T1.CATEGORY2CD = ? "); // 大分類
      paramData.add(szSelDaiBun);
    }
    if (!szSelChuBun.equals("-1")) {
      sb.append(" and  T1.CATEGORY3CD = ? "); // 中分類
      paramData.add(szSelChuBun);
    }

    sb.append(" order by ");
    sb.append("  T1.CENTERCD,T1.SUPPLIERCD,T1.PRODUCTDIV,T1.ERRORDIV ");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sb.toString());
    return sb.toString();
  }



  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<>();
    LocalDateTime d = LocalDateTime.now();
    DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    String s = df1.format(d); // format(d)のdは、LocalDateTime dのd
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(s); // 出力結果：2018/02/11 13:02:49 日
    // タイトル名称
    cells.add("発注結果");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    cells.add("");
    cells.add("");
    cells.add("");
    cells.add("");
    cells.add("");
    cells.add("");
    cells.add("");
    cells.add("出力日時 :" + s);
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<>();
    cells.add("");
    getWhere().add(cells);

    cells = new ArrayList<>();
    cells.add(DefineReport.InpText.HTDT.getTxt() + " :" + jad.getJSONText(DefineReport.InpText.HTDT.getObj()));
    cells.add("");
    cells.add(DefineReport.MeisyoSelect.KBN910009.getTxt() + " :" + jad.getJSONText(DefineReport.MeisyoSelect.KBN910009.getObj()));
    cells.add("店舗 :" + jad.getJSONText(DefineReport.Select.TENKN.getObj()));
    cells.add(DefineReport.MeisyoSelect.KBN10002.getTxt() + " :" + jad.getJSONText(DefineReport.MeisyoSelect.KBN10002.getObj()));
    cells.add("");
    cells.add(DefineReport.InpText.CENTERCD.getTxt() + " :" + jad.getJSONText(DefineReport.InpText.CENTERCD.getObj()));
    getWhere().add(cells);
    cells = new ArrayList<>();
    cells.add(DefineReport.Select.BUMON.getTxt() + " :" + jad.getJSONText(DefineReport.Select.BUMON.getObj()));
    cells.add("");
    cells.add(DefineReport.Select.DAI_BUN.getTxt() + " :" + jad.getJSONText(DefineReport.Select.DAI_BUN.getObj()));
    cells.add("");
    cells.add(DefineReport.Select.CHU_BUN.getTxt() + " :" + jad.getJSONText(DefineReport.Select.CHU_BUN.getObj()));
    cells.add("");
    cells.add(DefineReport.InpText.SSIRCD.getTxt() + " :" + jad.getJSONText(DefineReport.InpText.SSIRCD.getObj()));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<>();
    cells.add("");
    getWhere().add(cells);
  }
}
