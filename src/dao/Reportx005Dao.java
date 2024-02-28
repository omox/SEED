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
public class Reportx005Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx005Dao(String JNDIname) {
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

    String szSeq = getMap().get("SEQ"); // SEQ
    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getBaseData(getMap());

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append("  trim(left(T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) SHNCD"); // 商品コード
    sbSQL.append(" ,T3.SRCCD as SRCCD"); // ソースコード1
    sbSQL.append(" ,T1.URICD"); // 販売コード
    sbSQL.append(" ,trim(left(T1.PARENTCD, 4) || '-' || SUBSTR(T1.PARENTCD, 5)) as PARENTCD"); // 親コード
    sbSQL.append(" ,T1.SHNKN"); // 商品名
    sbSQL.append(" ,T1.RG_ATSUKFLG"); // 扱区分
    sbSQL.append(" ,T1.RG_GENKAAM"); // 原価
    sbSQL.append(" ,T1.RG_BAIKAAM"); // 本体売価
    sbSQL.append(" ," + DefineReport.ID_SQL_MD03111301_COL_RG); // 総額売価
    sbSQL.append(" ,T1.RG_IRISU"); // 店入数
    sbSQL.append(" ,T1.RG_WAPNFLG"); // ワッペン区分
    sbSQL.append(" ,T1.RG_IDENFLG"); // 一括区分
    sbSQL.append(" ,T1.SSIRCD"); // 標準仕入先
    sbSQL.append(" ,right('0'||nvl(T1.BMNCD,0),2)||right('0'||nvl(T1.DAICD,0),2)||right('0'||nvl(T1.CHUCD,0),2)||right('0'||nvl(T1.SHOCD,0),2) as BUNCD"); // 分類コード
    sbSQL.append(" ,T1.SEQ"); // SEQ
    sbSQL.append(" ,T1.INPUTNO"); // 入力番号
    sbSQL.append(" ,T1.CSV_UPDKBN"); // CSV登録区分
    sbSQL.append(" ,T1.YOYAKUDT"); // マスタ変更予定日
    sbSQL.append(" ,T1.TENBAIKADT"); // 店売価実施日
    sbSQL.append(" ,trim(T1.SHNCD)");
    sbSQL.append(" ,case when T5.SHNCD is not null then 1 else 0 end"); // 商品コード更新可
    sbSQL.append(" ,case when T6.SHNCD is null and nvl(T7.CNT,0) = 0 then 1 else 0 end"); // 予約商品新規可
    sbSQL.append(" ,case when T6.SHNCD is not null and nvl(T7.CNT,0) = 1 then 1 else 0 end"); // 予約商品更新可
    sbSQL.append(" from INAMS.CSVSHNHEAD T0");
    sbSQL.append(" inner join INAMS.CSVSHN T1 on T1.SEQ = T0.SEQ and T0.SEQ = '" + szSeq + "' and nvl(T1.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.CSVSRCCD T3 on T3.SEQ = T0.SEQ and T1.INPUTNO=T3.INPUTNO and T1.SHNCD = T3.SHNCD and T3.SEQNO = 1");
    sbSQL.append(" " + DefineReport.ID_SQL_MD03111301_JOIN + "");
    sbSQL.append(" left outer join INAMS.MSTSHN T5 on T1.SHNCD = T5.SHNCD and nvl(T5.UPDKBN, 0) <> 1");
    sbSQL.append(" left outer join INAMS.MSTSHN_Y T6 on T1.SHNCD = T6.SHNCD and T1.YOYAKUDT = T6.YOYAKUDT and T1.TENBAIKADT = T6.TENBAIKADT and NVL(T6.UPDKBN, 0) <> 1");
    sbSQL.append(
        " left outer join (select T7.SHNCD,count(T7.SHNCD) over(partition by T7.SHNCD) as CNT from INAMS.MSTSHN_Y T7 where NVL(T7.UPDKBN, 0) <> 1 group by T7.SHNCD) T7 on T1.SHNCD = T7.SHNCD");
    sbSQL.append(" order by ");
    sbSQL.append("  T1.SHNCD ");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  /**
   * 基本情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getBaseData(HashMap<String, String> map) {

    String szSeq = getMap().get("SEQ"); // SEQ
    getMap().get("BTN");

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append("  T1.SEQ as F1"); // F1 : 2.入力番号
    sbSQL.append(" , T1.OPERATOR as F2");
    sbSQL.append(" , DATE_FORMAT(T1.INPUT_DATE, '%Y%m%d') as F3");
    sbSQL.append(" , DATE_FORMAT(T1.INPUT_DATE, '%H%i%S') as F4");
    sbSQL.append(" , trim(COMMENTKN) as F5");
    sbSQL.append(" , DATE_FORMAT(T1.INPUT_DATE, '%Y%m%d%H%i%s%f') as F6");
    sbSQL.append(" from INAMS.CSVSHNHEAD T1");
    sbSQL.append(" where T1.SEQ = '" + szSeq + "'");
    sbSQL.append(" order by ");
    sbSQL.append("  T1.SEQ ");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<String>();

    // タイトル名称
    cells.add("商品マスタ");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);

    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.KIKAN.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
    cells.add(DefineReport.Select.TENPO.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.TENPO.getObj()));
    cells.add(DefineReport.Select.BUMON.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.BUMON.getObj()));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);
  }
}
