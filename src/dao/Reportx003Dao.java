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
public class Reportx003Dao extends ItemDao {

  // private static String DECI_DIGITS = ",20,10";

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx003Dao(String JNDIname) {
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

    String szRadCode = getMap().get("RAD_CODE"); // コードタイプ
    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null) || (szRadCode == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();
    ArrayList<String> paramData = new ArrayList<>();

    // コード
    String rows = "";
    String szCodes = "";
    for (int j = 1; j <= 8; j++) {
      String id = "TXT_CODE" + j;
      String val = getMap().get(id);
      if (!StringUtils.isEmpty(val)) {
        rows += ",row( " + j + " , ? )";
        paramData.add(val);
      }
    }
    for (int j = 1; j <= 8; j++) {
      String id = "TXT_CODE" + j;
      String val = getMap().get(id);
      if (!StringUtils.isEmpty(val)) {
        szCodes += ",?";
        if (!DefineReport.RadCode.ID2.getVal().equals(szRadCode)) {
          paramData.add(val);
        }
      }
    }
    if (DefineReport.RadCode.ID2.getVal().equals(szRadCode)) {
      for (int j = 1; j <= 8; j++) {
        String id = "TXT_CODE" + j;
        String val = getMap().get(id);
        if (!StringUtils.isEmpty(val)) {
          paramData.add(val);
        }
      }
    }
    rows = StringUtils.removeStart(rows, ",");
    szCodes = StringUtils.removeStart(szCodes, ",");

    // 必須チェック{
    if (StringUtils.isEmpty(szCodes)) {
      // コードはいずれか一つ入力必須
      System.out.println(super.getConditionLog());
      return "";
    }

    String szWhereShnCd = "";
    String szWhereSrcCd = "";
    String szWhereUriCd = "";

    if (DefineReport.RadCode.ID1.getVal().equals(szRadCode)) {
      szWhereShnCd = " and trim(T1.SHNCD) in (" + szCodes + " )";
    } else if (DefineReport.RadCode.ID2.getVal().equals(szRadCode)) {
      szWhereSrcCd = " and trim(T1.SRCCD) in (" + szCodes + ")";
    } else if (DefineReport.RadCode.ID3.getVal().equals(szRadCode)) {
      szWhereUriCd = " and T1.URICD in (" + szCodes + ")";
    }


    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("with INP as (");
    if (DefineReport.RadCode.ID1.getVal().equals(szRadCode)) {
      sbSQL.append(" select X.RNO,X.CD as SHNCD");
      sbSQL.append(" from (values " + rows + ") as X(RNO, CD)");
    } else if (DefineReport.RadCode.ID2.getVal().equals(szRadCode)) {
      sbSQL.append(" select");
      sbSQL.append(" ROW_NUMBER() over (order by T.RNO) as RNO");
      sbSQL.append(", T.SHNCD from (");
      sbSQL.append("  select T1.SHNCD,MIN(X.RNO) RNO");
      sbSQL.append("  from (values " + rows + ") as X(RNO, CD)");
      sbSQL.append("  , INAMS.MSTSRCCD T1");
      sbSQL.append("  where X.CD = trim(T1.SRCCD)" + szWhereSrcCd);
      sbSQL.append(" group by T1.SHNCD) T");

    } else if (DefineReport.RadCode.ID3.getVal().equals(szRadCode)) {
      sbSQL.append(" select X.RNO,trim(T1.SHNCD) as SHNCD");
      sbSQL.append(" from (values " + rows + ") as X(RNO, CD)");
      sbSQL.append(" ,INAMS.MSTSHN T1");
      sbSQL.append(" where X.CD = T1.URICD and COALESCE(T1.UPDKBN, 0) <> 1" + szWhereUriCd);
    }
    sbSQL.append("),WKSRC as (");
    sbSQL.append(" select T1.SHNCD, T1.SRCCD, T1.SEQNO");
    sbSQL.append(" from INAMS.MSTSRCCD T1");
    sbSQL.append(" where T1.SEQNO <=2 and exists( select 'X' from INP T0 where T1.SHNCD = T0.SHNCD)" + szWhereShnCd);
    sbSQL.append(")");
    sbSQL.append(" select ");
    sbSQL.append("   case IDX when 1 then row_number() over (partition BY IDX order by T0.RNO,T1.SHNCD) end as NO"); // No
    sbSQL.append("  ,case IDX when 1 then trim(left(T1.SHNCD, 4)||'-'||substr(T1.SHNCD, 5)) end as SHNCD"); // 商品コード
    sbSQL.append("  ,SRCCD"); // ソースコード1/ソースコード2
    sbSQL.append("  ,case IDX when 1 then URICD end"); // 販売コード
    sbSQL.append("  ,case IDX when 1 then case when trim(T1.PARENTCD) = '00000000' then '' else trim(left(T1.PARENTCD, 4) || '-' || SUBSTR(T1.PARENTCD, 5)) end end as PARENTCD"); // 親コード
    sbSQL.append("  ,case IDX when 1 then SHNKN end"); // 商品名
    sbSQL.append("  ,T2.KTXT"); // 扱区
    sbSQL.append("  ,case IDX when 1 then RG_ATSUKFLG when 2 then HS_ATSUKFLG end as ATSUKFLG"); //
    sbSQL.append("  ,case IDX when 1 then RG_GENKAAM when 2 then HS_GENKAAM end as GENKAAM"); // 原価
    sbSQL.append("  ,case IDX when 1 then RG_BAIKAAM when 2 then HS_BAIKAAM end as BAIKAAM"); // 本体売価
    sbSQL.append("  ,case IDX when 1 then " + DefineReport.ID_SQL_MD03111301_COL_RG + " " + "when 2 then " + DefineReport.ID_SQL_MD03111301_COL_HS + " end"); // 総額売価
    sbSQL.append("  ,case IDX when 1 then RG_IRISU when 2 then HS_IRISU end as IRISU"); // 店入数
    sbSQL.append("  ,case IDX when 1 then RG_WAPNFLG when 2 then HS_WAPNFLG end as WAPNFLG"); // ワッペン
    sbSQL.append("  ,case IDX when 1 then RG_IDENFLG end IDENFLG"); // 一括
    sbSQL.append("  ,case IDX when 1 then lpad(SSIRCD, 6, '0') end"); // 標準仕入先
    sbSQL.append("  ,case IDX when 1 then right('0'||COALESCE(T1.BMNCD,0),2)||right('0'||COALESCE(T1.DAICD,0),2)||right('0'||COALESCE(T1.CHUCD,0),2)||right('0'||COALESCE(T1.SHOCD,0),2) end"); // 分類コード
    sbSQL.append("  ,case IDX when 1 then COALESCE(DATE_FORMAT(DATE_FORMAT(T1.UPDDT, '%Y%m%d'), '%Y/%m/%d'), '__/__/__')  end as UPDDT "); // 更新日
    sbSQL.append(" from INAMS.MSTSHN T1");
    sbSQL.append(" inner join INP T0 on T1.SHNCD = T0.SHNCD and COALESCE(T1.UPDKBN, 0) <> 1");
    sbSQL.append(" inner join (values row(1, 'レギュラー'),ROW(2, '販促')) as T2(IDX, KTXT) on 1=1");
    sbSQL.append(" left outer join WKSRC T3 on T1.SHNCD = T3.SHNCD and T2.IDX = T3.SEQNO");
    sbSQL.append(DefineReport.ID_SQL_MD03111301_JOIN);
    sbSQL.append(" order by T0.RNO,T1.SHNCD, T2.IDX ");
    setParamData(paramData);
    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<>();

    // タイトル名称
    cells.add(jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<>();
    cells.add("");
    getWhere().add(cells);

    cells = new ArrayList<>();
    cells.add(jad.getJSONText(DefineReport.RadCode.NAME.getTxt()));
    getWhere().add(cells);
    cells = new ArrayList<>();
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "1"));
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "2"));
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "3"));
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "4"));
    getWhere().add(cells);
    cells = new ArrayList<>();
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "5"));
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "6"));
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "7"));
    cells.add(jad.getJSONValue(DefineReport.Text.CODE.getObj() + "8"));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<>();
    cells.add("");
    getWhere().add(cells);
  }
}
