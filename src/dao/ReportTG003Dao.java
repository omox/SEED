package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.DefineReport.DataType;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTG003Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG003Dao(String JNDIname) {
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
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    JSONArray msgList = this.check(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E30007", new String[] {}));
    }
    return option;
  }

  /**
   * 削除処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    List<JSONObject> msgList = this.checkDel(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), JSONArray.fromObject(msgList));
      return option;
    }


    // 削除処理
    try {
      option = this.deleteData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
    }
    return option;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = getMap().get("MOYSRBAN"); // 催し連番
    String szTengpcd = getMap().get("TENGPCD"); // 店グループ

    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // ①正 .新規
    boolean isNew = StringUtils.startsWith(sendBtnid, DefineReport.Button.NEW.getObj());
    DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid);

    String LBL_KYOSEI = "強制グループ";

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(", WKCD as ( ");
    sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.MOYKN");
    sbSQL.append("  ,T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as CD");
    sbSQL.append("  ,DATE_FORMAT(DATE_FORMAT(T1.HBSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBSTDT, '%Y%m%d')))");
    sbSQL.append("   ||'～'||");
    sbSQL.append("   DATE_FORMAT(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.HBEDDT, '%Y%m%d'))) as DT");
    sbSQL.append(
        "  ,COALESCE(DATE_FORMAT(DATE_FORMAT(T2.QACREDT, '%Y%m%d'), '%Y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QACREDT, '%Y%m%d'))), '____/__/__(_)') as QACREDT");
    sbSQL.append(
        "  ,COALESCE(DATE_FORMAT(DATE_FORMAT(T2.QARCREDT, '%Y%m%d'),'%Y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T2.QARCREDT,'%Y%m%d'))), '____/__/__(_)') as QARCREDT");
    sbSQL.append("  ,T2.LSIMEDT"); // 最終締日
    sbSQL.append("  ,T2.JLSTCREFLG"); // 事前発注リスト作成済フラグ
    sbSQL.append("  ,T2.QADEVSTDT"); // アンケート取込開始日
    sbSQL.append("  from INATK.TOKMOYCD T1 ");
    sbSQL.append("  inner join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
    sbSQL.append("  and T1.UPDKBN = 0 and T2.UPDKBN = 0");
    sbSQL.append("  and T1.MOYSKBN = " + szMoyskbn + "");
    sbSQL.append("  and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("  and T1.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append(" ) ");
    if (isNew) {
      sbSQL.append(" select");
      sbSQL.append("   T1.CD as F1"); // 催しコード
      sbSQL.append("  ,T1.MOYKN as F2"); // 催し名称
      sbSQL.append("  ,T1.DT as F3"); // 催し期間
      if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) { // 強制グループ
        sbSQL.append("  ,'' as F4");
      } else {
        sbSQL.append("  ,'" + LBL_KYOSEI + "' as F4");
      }
      sbSQL.append("  ,null as F5"); // グループNo.
      sbSQL.append("  ,null as F6"); // グループ名称
      sbSQL.append("  ,null as F7"); // リーダー店No.
      sbSQL.append("  ,null as F8"); // リーダー店
      sbSQL.append("  ,null as F9"); // 店数
      sbSQL.append("  ,null as F10"); // アンケート種類
      sbSQL.append("  ,T1.QACREDT as F11"); // アンケート作成日:通常
      sbSQL.append("  ,null as F12"); // アンケート作成日:本強制
      sbSQL.append("  ,T1.QARCREDT as F13"); // アンケート再作成日:通常
      sbSQL.append("  ,null as F14"); // アンケート再作成日:本強制
      sbSQL.append("  ,null as OPERATOR"); // F15
      sbSQL.append("  ,null as ADDDT"); // F16
      sbSQL.append("  ,null as UPDDT"); // F17
      sbSQL.append("  ,null as HDN_UPDDT"); // F18
      sbSQL.append("  ,T1.LSIMEDT"); // F19 最終締日
      sbSQL.append("  ,T1.JLSTCREFLG"); // F20 事前発注リスト作成済フラグ
      sbSQL.append("  ,T1.QADEVSTDT"); // F21 アンケート取込開始日
      sbSQL.append(" from");
      sbSQL.append("  WKCD T1 ");
    } else {
      sbSQL.append(" select");
      sbSQL.append("   max(T1.CD) as F1"); // 催しコード
      sbSQL.append("  ,max(T1.MOYKN) as F2"); // 催し名称
      sbSQL.append("  ,max(T1.DT) as F3"); // 催し期間
      sbSQL.append("  ,max(case when T2.KYOSEIFLG = 1 then '" + LBL_KYOSEI + "' else '' end) as F4"); // 強制グループ
      sbSQL.append("  ,right('000'||T2.TENGPCD,3) as F5"); // グループNo.
      sbSQL.append("  ,max(T2.TENGPKN)as F6"); // グループ名称
      sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then right('000'||T3.TENCD,3) end) as F7"); // リーダー店No.
      sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then M1.TENKN end) as F8"); // リーダー店
      sbSQL.append("  ,count(T3.TENCD)  F9"); // 店数
      sbSQL.append("  ,max(T2.QASYUKBN) as F10"); // アンケート種類
      sbSQL.append("  ,max(T1.QACREDT) as F11"); // アンケート作成日:通常
      sbSQL.append("  ,max(T2.QACREDT_K) as F12"); // アンケート作成日:本強制
      sbSQL.append("  ,max(T1.QARCREDT) as F13"); // アンケート再作成日:通常
      sbSQL.append("  ,max(T2.QARCREDT_K) as F14"); // アンケート再作成日:本強制
      sbSQL.append(" , max(T2.OPERATOR)");
      sbSQL.append(" , max(COALESCE(DATE_FORMAT(T2.ADDDT, '%y/%m/%d'),'__/__/__')) as ADDDT");
      sbSQL.append(" , max(COALESCE(DATE_FORMAT(T2.UPDDT, '%y/%m/%d'),'__/__/__')) as UPDDT");
      sbSQL.append("  ,max(DATE_FORMAT(T2.UPDDT, '%Y%m%d%H%i%s%f')) as HDN_UPDDT");
      sbSQL.append("  ,max(T1.LSIMEDT)"); // F19 最終締日
      sbSQL.append("  ,max(T1.JLSTCREFLG)"); // F20 事前発注リスト作成済フラグ
      sbSQL.append("  ,max(T1.QADEVSTDT)"); // F21 アンケート取込開始日
      sbSQL.append(" from");
      sbSQL.append("  WKCD T1 ");
      sbSQL.append("  inner join INATK.TOKTG_TENGP T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0 and T2.TENGPCD = " + szTengpcd + "");
      sbSQL.append(
          "  left outer join INATK.TOKTG_TEN T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN and T2.TENGPCD = T3.TENGPCD and T2.KYOSEIFLG = T3.KYOSEIFLG");
      sbSQL.append("  left outer join INAMS.MSTTEN M1 on T3.TENCD = M1.TENCD");
      sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.TENGPCD");
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  /**
   * 全店特売(アンケート有)_店舗情報取得処理
   *
   * @throws Exception
   */
  public String createSqlSelTOKTG_TEN(JSONObject data, boolean dispEmpty) {
    String szMoyskbn = data.optString("MOYSKBN"); // 催し区分
    String szMoysstdt = data.optString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = data.optString("MOYSRBAN"); // 催し連番
    String szTengpcd = data.optString("TENGPCD"); // 店グループ
    String szKyoseiflg = data.optString("KYOSEIFLG"); // 強制グループフラグ

    // 最大行数
    String max_row = DefineReport.SubGridRowNumber.TENPO_TG003.getVal();

    StringBuffer sbSQL = new StringBuffer();
    if (dispEmpty) {
      sbSQL.append(DefineReport.ID_SQL_GRD_CMN.replaceAll("@M", max_row));
      sbSQL.append(" select");
      sbSQL.append("  T2.MOYSKBN"); // F1 : 催し区分
      sbSQL.append(" ,T2.MOYSSTDT"); // F2 : 催し開始日
      sbSQL.append(" ,T2.MOYSRBAN"); // F3 : 催し連番
      sbSQL.append(" ,T2.TENCD"); // F4 : 店コード
      sbSQL.append(" ,T2.KYOSEIFLG"); // F5 : 強制グループフラグ
      sbSQL.append(" ,T2.TENGPCD"); // F6 : 店グループ
      sbSQL.append(" ,T2.LDTENKBN"); // F7 : リーダー店区分
      sbSQL.append(" ,T2.TENKN"); // F8 : 店名
      sbSQL.append(" ,T1.IDX"); // F9 : IDX
      sbSQL.append(" from T1 left join (");
    }
    sbSQL.append(" select");
    sbSQL.append("  T1.MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,T1.MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,T1.MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,T1.TENCD"); // F4 : 店コード
    sbSQL.append(" ,T1.KYOSEIFLG"); // F5 : 強制グループフラグ
    sbSQL.append(" ,T1.TENGPCD"); // F6 : 店グループ
    sbSQL.append(" ,T1.LDTENKBN"); // F7 : リーダー店区分
    sbSQL.append(" ,M1.TENKN"); // F8 : 店名
    sbSQL.append(" ,ROW_NUMBER() over (order by T1.TENCD) as IDX"); // F9 : IDX
    sbSQL.append(" from INATK.TOKTG_TEN T1");
    sbSQL.append(" left join INAMS.MSTTEN M1 on T1.TENCD = M1.TENCD and COALESCE(M1.UPDKBN,0) <> 1");
    sbSQL.append(" where T1.MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and T1.KYOSEIFLG= " + szKyoseiflg + "");
    sbSQL.append("   and T1.TENGPCD  = " + StringUtils.defaultIfEmpty(szTengpcd, DefineReport.Values.NONE.getVal()) + "");
    if (dispEmpty) {
      sbSQL.append(") T2 on T1.IDX = T2.IDX order by T1.IDX ,T2.TENCD");
    } else {
      sbSQL.append(" order by T1.TENCD");
    }
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


  boolean isTest = true;

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ取得
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 店グループ
    JSONArray dataArrayTENCD = JSONArray.fromObject(map.get("DATA_TENCD")); // 店コード

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー


    // SQLパターン
    // ①正 .新規 → 催し週：Insert処理／催しコード：Insert処理
    // ②正 .変更 → 週 ：Update処理

    StringUtils.startsWith(sendBtnid, DefineReport.Button.NEW.getObj());
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);


    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);
    String szMoyskbn = data.optString(TOKTG_TENGPLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOKTG_TENGPLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOKTG_TENGPLayout.MOYSRBAN.getId()); // 催し連番
    String szTengpcd = data.optString(TOKTG_TENGPLayout.TENGPCD.getId()); // 店グループ

    this.createSqlTOKTG_TENGP(userId, data, SqlType.MRG);

    // ************ 子テーブル処理 ***********
    JSONArray dataArrayDel = new JSONArray();
    // 子テーブルは、一度削除してから追加なので、キー項目に注意
    if (isChange) {
      String[] keys =
          new String[] {TOKTG_TENLayout.MOYSKBN.getId(), TOKTG_TENLayout.MOYSSTDT.getId(), TOKTG_TENLayout.MOYSRBAN.getId(), TOKTG_TENLayout.TENGPCD.getId(), TOKTG_TENLayout.KYOSEIFLG.getId()};
      String[] vals = new String[] {szMoyskbn, szMoysstdt, szMoysrban, szTengpcd, data.optString(TOKTG_TENGPLayout.KYOSEIFLG.getId())};
      dataArrayDel.add(super.createJSONObject(keys, vals));
    }

    // --- 02.全店特売(アンケート有)_店
    if (dataArrayDel.size() > 0) {
      this.createSqlTOKTG_TEN(userId, dataArrayDel, SqlType.DEL);
    }
    if (dataArrayTENCD.size() > 0) {
      this.createSqlTOKTG_TEN(userId, dataArrayTENCD, SqlType.MRG);
    }

    // 排他チェック実行
    String targetTable = "INATK.TOKTG_TENGP";
    String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and TENGPCD = ? and UPDKBN = 0";
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(szMoyskbn);
    targetParam.add(szMoysstdt);
    targetParam.add(szMoysrban);
    targetParam.add(szTengpcd);
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString(TOKTG_TENGPLayout.UPDDT.getId()))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
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
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }

    return option;
  }

  /**
   * 削除処理実行
   *
   * @param sysdate
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    map.get("SENDBTNID");
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);
    String szMoyskbn = data.optString(TOKTG_TENGPLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOKTG_TENGPLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOKTG_TENGPLayout.MOYSRBAN.getId()); // 催し連番
    String szTengpcd = data.optString(TOKTG_TENGPLayout.TENGPCD.getId()); // 店グループ

    this.createSqlTOKTG_TENGP(userId, data, SqlType.DEL);

    // --- 02.全店特売（アンケート有）_店舗（画面処理では何も行わず夜間バッチで物理削除）


    // 排他チェック実行
    String targetTable = "INATK.TOKTG_TENGP";
    String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and TENGPCD = ? and UPDKBN = 0";
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(szMoyskbn);
    targetParam.add(szMoysstdt);
    targetParam.add(szMoysrban);
    targetParam.add(szTengpcd);
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString(TOKTG_TENGPLayout.UPDDT.getId()))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
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
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    DefineReport.Button.NEW.getObj().equals(sendBtnid);
    DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);


    List<JSONObject> msgList = new ArrayList<JSONObject>();

    dataArray.optJSONObject(0);
    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  /**
   * チェック処理(削除時)
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {
    map.get("SENDBTNID");
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    List<JSONObject> msgList = new ArrayList<JSONObject>();

    dataArray.optJSONObject(0);
    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }


  /**
   * 項目用 DB問い合わせ件数チェック
   *
   * @param outobj チェック対象項目
   * @param obj パラメータ
   * @throws Exception
   */
  public JSONObject checkDbCount(String outobj, JSONObject obj) {
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();
    String val = obj.optString("value"); // 基本となる値
    String whr = " and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?";
    paramData.add(obj.optString("MOYSKBN")); // 催し区分
    paramData.add(obj.optString("MOYSSTDT")); // 催しコード（催し開始日）
    paramData.add(obj.optString("MOYSRBAN")); // 催し連番

    // 店グループ
    // 3.1.1.2．全店特売（アンケート有）_店グループに入力店グループがすでに存在すると、エラー。
    if (outobj.equals(DefineReport.InpText.TENGPCD.getObj())) {
      whr += " and UPDKBN = 0";
      boolean isExist = super.getDbExist("INATK.TOKTG_TENGP", "TENGPCD", val, whr, paramData);
      if (isExist) {
        // E20166 入力店グループがすでに存在します。 0 E
        return MessageUtility.getDbMessageIdObj("E20166", new String[] {});
      }
    }

    // 店舗
    // 3.1.1.5．店コードがすでに当催しのある店グループ（当店グループ以外）に属する場合、エラー。
    // 3.1.1.5.1．前の画面の「新規」よりの場合の検索条件：
    // 全店特売（アンケート有）_店舗.催しコード = 【画面】.「催しコード」 AND
    // 全店特売（アンケート有）_店舗.店グループ <> 【画面】.「店グループ」 AND
    // 全店特売（アンケート有）_店舗.店コード = 【画面】.「店コード」AND
    // 全店特売（アンケート有）_店舗.強制フラグ = 0：通常
    // 3.1.1.5.2．前の画面の「強制グループ」よりの場合の検索条件：
    // 全店特売（アンケート有）_店舗.催しコード = 【画面】.「催しコード」 AND
    // 全店特売（アンケート有）_店舗.店グループ <> 【画面】.「店グループ」 AND
    // 全店特売（アンケート有）_店舗.店コード = 【画面】.「店コード」AND
    // 全店特売（アンケート有）_店舗.強制フラグ = 1：強制
    if (outobj.equals(DefineReport.InpText.TENCD.getObj())) {
      whr += " and TENGPCD <> ? and KYOSEIFLG = ? ";
      paramData.add(obj.optString("TENGPCD")); // 店グループ
      paramData.add(StringUtils.defaultIfEmpty(obj.optString("KYOSEIFLG"), DefineReport.Values.OFF.getVal())); // 強制フラグ
      boolean isExist = super.getDbExist("INATK.TOKTG_TEN", "TENCD", val, whr, paramData);
      if (isExist) {
        // E20168 本店舗は当催しの他店グループに登録されています。 0 E
        return MessageUtility.getDbMessageIdObj("E20168", new String[] {});
      }
    }

    JSONObject data = new JSONObject();
    data.put("RES", "true");
    return data;
  }

  /**
   * 全店特売(アンケート有)_店グループINSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKTG_TENGP(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    // 更新列設定
    TOKTG_TENGPLayout[] notTarget = new TOKTG_TENGPLayout[] {TOKTG_TENGPLayout.UPDKBN, TOKTG_TENGPLayout.SENDFLG, TOKTG_TENGPLayout.OPERATOR, TOKTG_TENGPLayout.ADDDT, TOKTG_TENGPLayout.UPDDT};
    String sendflg = DefineReport.Values.SENDFLG_UN.getVal();
    String updkbn = DefineReport.ValUpdkbn.NML.getVal();
    if (SqlType.DEL.getVal() == sql.getVal()) {
      updkbn = DefineReport.ValUpdkbn.DEL.getVal();
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.TOKTG_TENGP ( ");

    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,TENGPCD"); // F4 : 店グループ
    sbSQL.append(" ,TENGPKN"); // F5 : 店グループ名称
    sbSQL.append(" ,KYOSEIFLG"); // F6 : 強制グループフラグ
    sbSQL.append(" ,QASYUKBN"); // F7 : アンケート種類
    sbSQL.append(" ,QACREDT_K"); // F8 : アンケート作成日_強制
    sbSQL.append(" ,QARCREDT_K"); // F9 : アンケート再作成日_強制
    sbSQL.append(" ,UPDKBN"); // F10: 更新区分
    sbSQL.append(" ,SENDFLG"); // F11: 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F12: オペレータ
    sbSQL.append(" ,UPDDT"); // F14: 更新日

    sbSQL.append(")values(");
    for (TOKTG_TENGPLayout itm : TOKTG_TENGPLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm)) {
        continue;
      } // 対象外
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      if (data.containsKey(itm.getId())) {
        String value = StringUtils.strip(data.optString(itm.getId()));
        if (StringUtils.isNotEmpty(value)) {
          prmData.add(value);
          sbSQL.append("cast(? as " + itm.getTyp() + ") ");
        } else {
          sbSQL.append("cast(null as " + itm.getTyp() + ") ");
        }
      } else {
        sbSQL.append("null ");
      }
    }
    sbSQL.append(" ," + updkbn); // F10: 更新区分
    sbSQL.append(" ," + sendflg); // F11: 送信フラグ
    sbSQL.append(" ,'" + userId + "'"); // F12: オペレータ
    sbSQL.append(" ,current_timestamp"); // F14: 更新日
    sbSQL.append(" )");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有)_店グループ");
    return result;
  }

  /**
   * 関連テーブルINSERT/UPDATE SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKTG_TEN(String userId, JSONArray dataArray, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();


    // 更新列設定
    TOKTG_TENLayout[] notTarget = new TOKTG_TENLayout[] {TOKTG_TENLayout.SENDFLG, TOKTG_TENLayout.OPERATOR, TOKTG_TENLayout.ADDDT, TOKTG_TENLayout.UPDDT};


    String values = "", names = "", rows = "", cols = "";
    String sendflg = DefineReport.Values.SENDFLG_UN.getVal();
    // 列別名設定
    for (TOKTG_TENLayout itm : TOKTG_TENLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm)) {
        continue;
      } // パラメータ不要
      names += "," + itm.getId();
      cols += ", cast(" + itm.getId() + " as " + itm.getTyp() + ") as " + itm.getCol();
    }
    names = StringUtils.removeStart(names, ",");
    cols = StringUtils.removeStart(cols, ",");

    // 値設定
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (TOKTG_TENLayout itm : TOKTG_TENLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm)) {
          continue;
        } // パラメータ不要
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (StringUtils.isEmpty(val)) {
          if (itm.getCol().equals("TENCD")) {
            values += ", 0";
          } else {
            values += ", null";
          }
        } else {
          prmData.add(val);
          values += ", cast( ? as " + itm.getTyp() + ") ";
        }
      }
      values += "," + sendflg + ""; // F8 : 送信フラグ
      values += ",'" + userId + "'"; // F9 : オペレータ
      values += ",current_timestamp"; // F10: 登録日
      values += ",current_timestamp"; // F11: 更新日
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");



    if (SqlType.DEL.getVal() == sql.getVal()) {
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.TOKTG_TEN ( ");
    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,TENCD"); // F4 : 店コード
    sbSQL.append(" ,KYOSEIFLG"); // F5 : 強制グループフラグ
    sbSQL.append(" ,TENGPCD"); // F6 : 店グループ
    sbSQL.append(" ,LDTENKBN"); // F7 : リーダー店区分
    sbSQL.append(" ,SENDFLG"); // F8 : 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F9 : オペレータ
    sbSQL.append(" ,ADDDT"); // F10: 登録日
    sbSQL.append(" ,UPDDT"); // F11: 更新日
    sbSQL.append(")values");
    sbSQL.append("" + rows + "");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有)_店舗");
    return result;
  }


  /** 全店特売(アンケート有)_店グループレイアウト() */
  public enum TOKTG_TENGPLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SIGNED", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "SIGNED", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SIGNED", "催し連番"),
    /** 店グループ */
    TENGPCD(4, "TENGPCD", "SIGNED", "店グループ"),
    /** 店グループ名称 */
    TENGPKN(5, "TENGPKN", "CHAR", "店グループ名称"),
    /** 強制グループフラグ */
    KYOSEIFLG(6, "KYOSEIFLG", "SIGNED", "強制グループフラグ"),
    /** アンケート種類 */
    QASYUKBN(7, "QASYUKBN", "SIGNED", "アンケート種類"),
    /** アンケート作成日_強制 */
    QACREDT_K(8, "QACREDT_K", "SIGNED", "アンケート作成日_強制"),
    /** アンケート再作成日_強制 */
    QARCREDT_K(9, "QARCREDT_K", "SIGNED", "アンケート再作成日_強制"),
    /** 更新区分 */
    UPDKBN(10, "UPDKBN", "SIGNED", "更新区分"),
    /** 送信フラグ */
    SENDFLG(11, "SENDFLG", "SIGNED", "送信フラグ"),
    /** オペレータ */
    OPERATOR(12, "OPERATOR", "CHAR", "オペレータ"),
    /** 登録日 */
    ADDDT(13, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(14, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKTG_TENGPLayout(Integer no, String col, String typ, String txt) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.txt = txt;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** 全店特売(アンケート有)_店舗レイアウト() */
  public enum TOKTG_TENLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SIGNED", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "SIGNED", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SIGNED", "催し連番"),
    /** 店コード */
    TENCD(4, "TENCD", "SIGNED", "店コード"),
    /** 強制グループフラグ */
    KYOSEIFLG(5, "KYOSEIFLG", "SIGNED", "強制グループフラグ"),
    /** 店グループ */
    TENGPCD(6, "TENGPCD", "SIGNED", "店グループ"),
    /** リーダー店区分 */
    LDTENKBN(7, "LDTENKBN", "SIGNED", "リーダー店区分"),
    /** 送信フラグ */
    SENDFLG(8, "SENDFLG", "SIGNED", "送信フラグ"),
    /** オペレータ */
    OPERATOR(9, "OPERATOR", "CHAR", "オペレータ"),
    /** 登録日 */
    ADDDT(10, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(11, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKTG_TENLayout(Integer no, String col, String typ, String txt) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.txt = txt;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    public String getTxt() {
      return txt;
    }

    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

}
