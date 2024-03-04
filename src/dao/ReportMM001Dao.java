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
public class ReportMM001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportMM001Dao(String JNDIname) {
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
    String szTencd = getMap().get("TENCD"); // 店コード
    String szHbstdt = getMap().get("HBSTDT"); // 販売開始日
    String szHbeddt = getMap().get("HBEDDT"); // 販売終了日
    String szNnstdt = getMap().get("NNSTDT"); // 納入開始日
    String szNneddt = getMap().get("NNEDDT"); // 納入終了日
    String szMoysKbn = getMap().get("MOYSKBN"); // 催し区分
    String szBumon = getMap().get("BUMON"); // 部門

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlWhere2 = " where 1=1 ";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    if (!"-1".equals(szMoysKbn)) {
      sqlWhere2 = " where T1.MOYSKBN = ? ";
      paramData.add(szMoysKbn);
    }

    if (!StringUtils.isEmpty(szHbstdt)) {
      if (!StringUtils.isEmpty(szHbeddt)) {
        sqlWhere2 += " AND DATE_FORMAT(T1.HBSTDT,'%y%m%d') <= ? ";
        paramData.add(szHbeddt);
        sqlWhere2 += " AND DATE_FORMAT(T1.HBEDDT,'%y%m%d') >= ? ";
        paramData.add(szHbstdt);
      } else {
        sqlWhere2 += " AND DATE_FORMAT(T1.HBSTDT,'%y%m%d') >= ? ";
        paramData.add(szHbstdt);
      }
    }
    if (!StringUtils.isEmpty(szNnstdt)) {
      if (!StringUtils.isEmpty(szNneddt)) {
        sqlWhere2 += " AND DATE_FORMAT(T1.NNSTDT,'%y%m%d') <= ? ";
        paramData.add(szNneddt);
        sqlWhere2 += " AND DATE_FORMAT(T1.NNEDDT,'%y%m%d') >= ? ";
        paramData.add(szNnstdt);
      } else {
        sqlWhere2 += " AND DATE_FORMAT(T1.NNSTDT,'%y%m%d') >= ? ";
        paramData.add(szNnstdt);
      }
    }

    if (!StringUtils.isEmpty(szShncd) && StringUtils.isEmpty(szTencd)) {
      sqlWhere += " AND T3.SHNCD = ? ";
      paramData.add(szShncd);
    } else if (!StringUtils.isEmpty(szShncd) && !StringUtils.isEmpty(szTencd)) {
      // b. 商品コード、店コード
      sqlWhere += " AND T3.SHNCD = ? ";
      paramData.add(szShncd);

      sqlFrom = ",INATK.TOKMM_TEN T4 ";
      sqlWhere += " AND T3.MOYSKBN = T4.MOYSKBN";
      sqlWhere += " AND T3.MOYSSTDT = T4.MOYSSTDT";
      sqlWhere += " AND T3.MOYSRBAN = T4.MOYSRBAN";
      sqlWhere += " AND T3.BMFLG = T4.BMFLG";
      sqlWhere += " AND T3.BMNO = T4.BMNO";
      sqlWhere += " AND T3.BMNCD = T4.BMNCD";
      sqlWhere += " AND T3.KANRINO = T4.KANRINO";
      sqlWhere += " AND T4.TENCD = ? ";
      paramData.add(szTencd);
    } else if (StringUtils.isEmpty(szShncd) && !StringUtils.isEmpty(szTencd) && !StringUtils.isEmpty(szBumon)) {
      // c. 店コード、部門
      sqlWhere += " AND T3.BMNCD = ? ";
      paramData.add(szBumon);

      sqlFrom = ",INATK.TOKMM_TEN T4 ";
      sqlWhere += " AND T3.MOYSKBN = T4.MOYSKBN";
      sqlWhere += " AND T3.MOYSSTDT = T4.MOYSSTDT";
      sqlWhere += " AND T3.MOYSRBAN = T4.MOYSRBAN";
      sqlWhere += " AND T3.BMFLG = T4.BMFLG";
      sqlWhere += " AND T3.BMNO = T4.BMNO";
      sqlWhere += " AND T3.BMNCD = T4.BMNCD";
      sqlWhere += " AND T3.KANRINO = T4.KANRINO";
      sqlWhere += " AND T4.TENCD = ? ";
      paramData.add(szTencd);
    } else {
      // エラー
      System.out.println(super.getConditionLog());
      return "";
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append("SELECT  ");
    sbSQL.append("MOYSKBN || RIGHT('000000' || MOYSSTDT,6) || RIGHT('000' || MOYSRBAN,3) AS F1 "); // F1 催しコード
    sbSQL.append(",MOYKN AS F2 "); // F2 催し名称
    sbSQL.append(",CASE ");
    sbSQL.append("WHEN (HBSTDTD IS NULL OR HBSTDTD='') AND (HBEDDTD IS NULL OR HBEDDTD='') THEN '' ");
    sbSQL.append("WHEN (HBSTDTD IS NOT NULL AND HBSTDTD <> '') AND (HBEDDTD IS NULL OR HBEDDTD='') THEN HBSTDTD || HBSTDTW ");
    sbSQL.append("WHEN (HBSTDTD IS NULL OR HBSTDTD='') AND (HBEDDTD IS NOT NULL AND HBEDDTD <> '') THEN HBEDDTD || HBEDDTW ");
    sbSQL.append("ELSE HBSTDTD || HBSTDTW || '～' || HBEDDTD || HBEDDTW END AS F3 "); // F3 販売期間
    sbSQL.append(",CASE ");
    sbSQL.append("WHEN (NNSTDTD IS NULL OR NNSTDTD='') AND (NNEDDTD IS NULL OR NNEDDTD='') THEN '' ");
    sbSQL.append("WHEN (NNSTDTD IS NOT NULL AND NNSTDTD <> '') AND (NNEDDTD IS NULL OR NNEDDTD='') THEN NNSTDTD || NNSTDTW ");
    sbSQL.append("WHEN (NNSTDTD IS NULL OR NNSTDTD='') AND (NNEDDTD IS NOT NULL AND NNEDDTD <> '') THEN NNEDDTD || NNEDDTW ");
    sbSQL.append("ELSE NNSTDTD || NNSTDTW || '～' || NNEDDTD || NNEDDTW END AS F4 "); // F4 納入期間
    sbSQL.append(",KANRINO AS F5 "); // F5 管理番号
    sbSQL.append(",CASE ");
    sbSQL.append("WHEN BMFLG = 0 THEN '' ");
    sbSQL.append("WHEN BMFLG = 1 THEN '●' ");
    sbSQL.append("ELSE NULL END AS F6 "); // F6 B/M
    sbSQL.append(",BMNO AS F7 "); // F7 BM番号
    sbSQL.append(",ADDSHUKBN AS F8 "); // F8 登録種別
    sbSQL.append(",MOYSKBN AS F9 "); // F9 催し区分
    sbSQL.append(",MOYSSTDT AS F10 "); // F10 催し開始日
    sbSQL.append(",MOYSRBAN AS F11 "); // F11 催し連番
    sbSQL.append(",BMNCD AS F12 "); // F12 部門
    sbSQL.append("FROM( ");
    sbSQL.append("SELECT ");
    sbSQL.append("T1.MOYSKBN ");
    sbSQL.append(",T1.MOYSSTDT ");
    sbSQL.append(",T1.MOYSRBAN ");
    sbSQL.append(",T2.MOYKN ");
    sbSQL.append(",DATE_FORMAT(T1.HBSTDT,'%y/%m/%d') AS HBSTDTD ");
    sbSQL.append(",DATE_FORMAT(T1.HBEDDT,'%y/%m/%d') AS HBEDDTD ");
    sbSQL.append(",DATE_FORMAT(T1.NNSTDT,'%y/%m/%d') AS NNSTDTD ");
    sbSQL.append(",DATE_FORMAT(T1.NNEDDT,'%y/%m/%d') AS NNEDDTD ");
    sbSQL.append(",(SELECT JWEEK FROM WEEK WHERE CWEEK=DAYOFWEEK(DATE_FORMAT(T1.HBSTDT,'%Y%m%d'))) HBSTDTW ");
    sbSQL.append(",(SELECT JWEEK FROM WEEK WHERE CWEEK=DAYOFWEEK(DATE_FORMAT(T1.HBEDDT,'%Y%m%d'))) HBEDDTW ");
    sbSQL.append(",(SELECT JWEEK FROM WEEK WHERE CWEEK=DAYOFWEEK(DATE_FORMAT(T1.NNSTDT,'%Y%m%d'))) NNSTDTW ");
    sbSQL.append(",(SELECT JWEEK FROM WEEK WHERE CWEEK=DAYOFWEEK(DATE_FORMAT(T1.NNEDDT,'%Y%m%d'))) NNEDDTW ");
    sbSQL.append(",T3.KANRINO ");
    sbSQL.append(",T3.BMFLG ");
    sbSQL.append(",T3.BMNO ");
    sbSQL.append(",T3.ADDSHUKBN ");
    sbSQL.append(",T3.BMNCD ");
    sbSQL.append("FROM INATK.TOKMM T1 ");
    sbSQL.append("LEFT JOIN INATK.TOKMOYCD T2 ");
    sbSQL.append("ON T1.MOYSKBN = T2.MOYSKBN ");
    sbSQL.append("AND T1.MOYSSTDT = T2.MOYSSTDT ");
    sbSQL.append("AND T1.MOYSRBAN = T2.MOYSRBAN ");
    sbSQL.append(",INATK.TOKMM_SHN T3 ");
    sbSQL.append(sqlFrom);
    sbSQL.append(sqlWhere2);
    sbSQL.append("AND T1.MOYSKBN = T3.MOYSKBN ");
    sbSQL.append("AND T1.MOYSSTDT = T3.MOYSSTDT ");
    sbSQL.append("AND T1.MOYSRBAN = T3.MOYSRBAN ");
    sbSQL.append("AND T1.BMFLG = T3.BMFLG ");
    sbSQL.append(sqlWhere);
    sbSQL.append(") S1 ");
    sbSQL.append("ORDER BY F1, F6, F5 ");

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
