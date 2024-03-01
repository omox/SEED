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
public class ReportMI001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportMI001Dao(String JNDIname) {
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
    String szSrccd = getMap().get("SRCCD"); // JANコード

    ArrayList<String> paramData = new ArrayList<>();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlWhere2 = "";
    // 検索条件
    if (!StringUtils.isEmpty(szShncd)) {
      sqlWhere += ", (select SRCCD from (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SRCCD from INAMS.MSTSRCCD T2 where T2.SHNCD = ?) AS SRCCD1FROM where ROWID=1) as SRCCD1";
      paramData.add(szShncd);
      sqlWhere += ", (select SRCCD from (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SRCCD from INAMS.MSTSRCCD T2 where T2.SHNCD = ?) AS SRCCD2FROM where ROWID=2) as SRCCD2";
      paramData.add(szShncd);
      sqlWhere2 += " where T1.SHNCD = ? and T1.UPDKBN = 0 ";
      paramData.add(szShncd);
    } else {
      sqlWhere += ",(select SRCCD from";
      sqlWhere += " (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SRCCD from INAMS.MSTSRCCD T2";
      sqlWhere += " where T2.SHNCD = (select SHNCD from (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SHNCD from INAMS.MSTSRCCD T2 where T2.SRCCD = ? )AS SRCCD3FROM where ROWID=1)";
      paramData.add(szSrccd);
      sqlWhere += " )AS SRCCD1FROM where ROWID=1) as SRCCD1";
      sqlWhere += ",(select SRCCD from";
      sqlWhere += " (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SRCCD from INAMS.MSTSRCCD T2";
      sqlWhere += " where T2.SHNCD = (select SHNCD from (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SHNCD from INAMS.MSTSRCCD T2 where T2.SRCCD = ? ) AS SRCCD4FROM where ROWID=1)";
      paramData.add(szSrccd);
      sqlWhere += " )AS SRCCD2FROM where ROWID=2) as SRCCD2";
      sqlWhere2 +=
          " where T1.SHNCD = (select SHNCD from (select ROW_NUMBER() OVER (ORDER BY T2.SEQNO) as ROWID, T2.SHNCD from INAMS.MSTSRCCD T2 where T2.SRCCD = ? ) AS GETSHNCDFROM where ROWID=1)  and T1.UPDKBN = 0 ";
      paramData.add(szSrccd);
    }
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();
    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("select");
    sbSQL.append(" substr(SHNCD,1,4)||'-'||substr(SHNCD,5,4) as F1"); // F1 : 商品コード
    sbSQL.append(", SHNKN as F2"); // F2 : 商品名(漢字)
    sbSQL.append(", KIKKN as F3"); // F3 : 規格名(漢字)
    sbSQL.append(", SHNAN as F4"); // F4 : 商品名(カナ)
    sbSQL.append(", NULL as F5"); // F5 : 規格名(カナ)
    sbSQL.append(", SRCCD1 as F6"); // F6 : JANコード1
    sbSQL.append(", SRCCD2 as F7"); // F7 : JANコード2
    sbSQL.append(", right('00'||DAICD, 2) as F8"); // F8 : 大分類
    sbSQL.append(", right('00'||CHUCD, 2) as F9"); // F9 : 中分類
    sbSQL.append(", right('00'||SHOCD, 2) as F10"); // F10 : 小分類
    sbSQL.append(", MAKERCD as F11"); // F11 : メーカー
    sbSQL.append(", right('000000'||SSIRCD, 6) as F12"); // F12 : 仕入先
    sbSQL.append(", SIRKN as F13"); // F13 : 仕入先名漢字
    sbSQL.append(", RG_GENKAAM as F14"); // F14 : 原価(レギュラー)
    sbSQL.append(", RG_BAIKAAM as F15"); // F15 : 売価(レギュラー)
    sbSQL.append(", RG_IRISU as F16"); // F16 : 入数(レギュラー)
    sbSQL.append(", AVGPTANKAAM as F17"); // F17 : 平均パック単価(レギュラー)
    sbSQL.append(", HS_GENKAAM as F18"); // F18 : 原価(山積・特売)
    sbSQL.append(", HS_BAIKAAM as F19"); // F19 : 売価(山積・特売)
    sbSQL.append(", HS_IRISU as F20"); // F20 : 入数(山積・特売)
    sbSQL.append(", NULL as F21"); // F21 : 平均パック単価(山積・特売)
    sbSQL.append(", READTM_MON as F22"); // F22 : 月
    sbSQL.append(", READTM_TUE as F23"); // F23 : 火
    sbSQL.append(", READTM_WED as F24"); // F24 : 水
    sbSQL.append(", READTM_THU as F25"); // F25 : 木
    sbSQL.append(", READTM_FRI as F26"); // F26 : 金
    sbSQL.append(", READTM_SAT as F27"); // F27 : 土
    sbSQL.append(", READTM_SUN as F28"); // F28 : 日
    sbSQL.append(" from");
    sbSQL.append(" (select");
    sbSQL.append(" T1.SHNCD");
    sbSQL.append(", T1.SHNKN");
    sbSQL.append(", T1.KIKKN");
    sbSQL.append(", T1.SHNAN");
    sbSQL.append(sqlWhere);
    sbSQL.append(", T1.DAICD");
    sbSQL.append(", T1.CHUCD");
    sbSQL.append(", T1.SHOCD");
    sbSQL.append(", T1.MAKERCD");
    sbSQL.append(", T1.SSIRCD");
    sbSQL.append(", T2.SIRKN");
    sbSQL.append(", T1.RG_GENKAAM");
    sbSQL.append(", T1.RG_BAIKAAM");
    sbSQL.append(", T1.RG_IRISU");
    sbSQL.append(", (select T3.AVGPTANKAAM from INAMS.MSTAVGPTANKA T3 where T3.SHNCD=T1.SHNCD) AS AVGPTANKAAM");
    sbSQL.append(", T1.HS_GENKAAM");
    sbSQL.append(", T1.HS_BAIKAAM");
    sbSQL.append(", T1.HS_IRISU");
    sbSQL.append(", (select READTM_MON from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_MON");
    sbSQL.append(", (select READTM_TUE from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_TUE");
    sbSQL.append(", (select READTM_WED from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_WED");
    sbSQL.append(", (select READTM_THU from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_THU");
    sbSQL.append(", (select READTM_FRI from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_FRI");
    sbSQL.append(", (select READTM_SAT from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_SAT");
    sbSQL.append(", (select READTM_SUN from INAMS.MSTREADTM T4 where T4.READTMPTN=T1.READTMPTN) AS READTM_SUN");
    sbSQL.append("  from INAMS.MSTSHN T1");
    sbSQL.append("  left join INAMS.MSTSIR T2");
    sbSQL.append("  on T1.SSIRCD = T2.SIRCD");
    sbSQL.append("  and T2.UPDKBN <> 1");
    sbSQL.append(sqlWhere2);
    sbSQL.append("  ) AS M1");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/*" + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
