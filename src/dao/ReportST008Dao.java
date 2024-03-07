package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportST008Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportST008Dao(String JNDIname) {
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

    String szBmncd = getMap().get("BMNCD"); // 部門
    String szMoyscd = getMap().get("MOYSCD"); // 催しコード
    String szRinji = getMap().get("RINJI"); // 臨時
    String szRankno = getMap().get("RANKNO"); // ランクNo.
    getMap().get("MOYSKBN");
    getMap().get("MOYSSTDT");
    getMap().get("MOYSRBAN");

    String tentenArr = getMap().get("TENTENARR"); // 販売実績_点数配列
    getMap().get("SENDBTNID");
    String colZitten = "NULL"; // 参照列名：参考販売実績
    String joinTable = ""; // 結合用テーブル名
    String sortParam = "'1'";

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 基本情報取得
    JSONArray array = getTOKMOYCDData(getMap());

    // パラメータ確認
    if (StringUtils.isEmpty(szBmncd)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    if (StringUtils.equals("0", szRinji) && !StringUtils.isEmpty(szMoyscd)) {
      System.out.println(super.getConditionLog());
      return "";
    }
    if (StringUtils.equals("1", szRinji) && StringUtils.isEmpty(szMoyscd)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    String sortBtn = getMap().get("SORTBTN");
    String sortOrder = getMap().get("SORTORDER") == null ? "" : getMap().get("SORTORDER");
    String sortBtnId = "TENNO"; // 押下ボタン
    if (!sortBtn.equals("-")) {
      for (int i = 0; i < sortBtn.split("-").length; i++) {
        if (i == 0) {
          sortBtnId = sortBtn.split("-")[i];
        } else if (i == 1) {
          // sortBtn.split("-")[i];
        }
      }
    }

    if (!StringUtils.isEmpty(szRankno) && Integer.parseInt(szRankno) >= 900) {
      // 引継いだランクNo.＞＝900の場合、臨時チェック状態を有りする。
      szRinji = "1";
    } else if (!StringUtils.isEmpty(szRankno) && Integer.parseInt(szRankno) < 900) {
      // 引継いだランクNo.＜900の場合、臨時チェック状態を無にしする。
      szRinji = "0";
    }

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();


    sbSQL.append(" with RECURSIVE T1(IDX) as (select");
    sbSQL.append(" 1 from (SELECT 1 AS DUMMY) DUMMY");
    sbSQL.append(" union all select");
    sbSQL.append(" IDX + 1 from T1");
    sbSQL.append(" where IDX < 400)");
    sbSQL.append(", WK as (select");

    if (StringUtils.equals("1", szRinji)) {
      sbSQL.append(" TENRANK_ARR as ARR");
      sbSQL.append(", 1 as LEN");
      sbSQL.append(" from INATK.TOKRANKEX");
      sbSQL.append(" where MOYSKBN =" + StringUtils.substring(szMoyscd, 0, 1));
      sbSQL.append(" and MOYSSTDT =" + StringUtils.substring(szMoyscd, 1, 7));
      sbSQL.append(" and MOYSRBAN =" + StringUtils.substring(szMoyscd, 7, 10));
      if (StringUtils.equals(null, szBmncd) || StringUtils.isEmpty(szBmncd)) {
        sbSQL.append(" and BMNCD = null");
      } else {
        sbSQL.append(" and BMNCD = " + szBmncd);
      }
      if (StringUtils.equals(null, szRankno) || StringUtils.isEmpty(szRankno)) {
        sbSQL.append(" and RANKNO = null");
      } else {
        sbSQL.append(" and RANKNO =  " + szRankno);
      }
      sbSQL.append(" and UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());

    } else if (StringUtils.equals("0", szRinji)) {
      sbSQL.append(" TENRANK_ARR as ARR");
      sbSQL.append(", 1 as LEN");
      sbSQL.append(" from INATK.TOKRANK");
      if (StringUtils.equals(null, szBmncd) || StringUtils.isEmpty(szBmncd)) {
        sbSQL.append(" where BMNCD = null");
      } else {
        sbSQL.append(" where BMNCD = " + szBmncd);
      }
      if (StringUtils.equals(null, szRankno) || StringUtils.isEmpty(szRankno)) {
        sbSQL.append(" and RANKNO = null");
      } else {
        sbSQL.append(" and RANKNO =  " + szRankno);
      }
      sbSQL.append(" and UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());
    }
    sbSQL.append(")");

    if (StringUtils.isNotEmpty(tentenArr)) {
      // 実績点数配列より点数を展開する。
      colZitten = "case when trim(T5.SURYO) = '' then null else int(trim(T5.SURYO)) end";
      sortParam = "case when TRIM(T5.SURYO) = '' then '2' else '1' end";
      joinTable = " left join ARRWK_TENSU as T5 on T5.IDX = T1.IDX ";
      sbSQL.append(", WK_TENSU as (select");
      sbSQL.append(" '" + tentenArr + "' as ARR");
      sbSQL.append(", 9 as LEN");
      sbSQL.append(" from (SELECT 1 AS DUMMY) DUMMY)");
      sbSQL.append(", ARRWK_TENSU(IDX, SURYO, S, ARR, LEN) as (select");
      sbSQL.append(" 1");
      sbSQL.append(", SUBSTR(ARR, 1, LEN)");
      sbSQL.append(", 1 + LEN");
      sbSQL.append(", ARR");
      sbSQL.append(", LEN");
      sbSQL.append(" from WK_TENSU");
      sbSQL.append(" union all select");
      sbSQL.append(" IDX + 1");
      sbSQL.append(", SUBSTR(ARR, S, LEN)");
      sbSQL.append(", S + LEN");
      sbSQL.append(", ARR, LEN");
      sbSQL.append(" from ARRWK_TENSU");
      sbSQL.append(" where S <= LENGTH(ARR)");
      sbSQL.append(")");
    }
    sbSQL.append(DefineReport.ID_SQL_ARR_CMN);
    sbSQL.append(" select");
    sbSQL.append(" right ('000' || T1.IDX, 3) as TENCD"); // F1 :店番
    sbSQL.append(", case when COALESCE(T2.MISEUNYOKBN, 9) = 9 then '' else T2.TENKN end as TENKN"); // F2 :店舗名称
    sbSQL.append(", TRIM(T4.RNK) as 'RANK'"); // F3 :ランク
    sbSQL.append(", " + colZitten + " as SANKOUHBJ"); // F4 :参考販売実績
    sbSQL.append(", case when COALESCE(T2.MISEUNYOKBN, 9) <> 9 then T3.AREACD end as AREACD"); // F5 :エリア
    sbSQL.append(", case when COALESCE(T2.MISEUNYOKBN, 9) = 9 then 0 else 1 end as EDITFLG"); // F6 :入力フラグ
    sbSQL.append(", case when TRIM(T4.RNK) = '' then 2 else 1 end as SORTKBN"); // F7 :ソート区分
    sbSQL.append(", " + sortParam + " as SORTKBN2");
    sbSQL.append(" from T1");
    sbSQL.append(" left join ARRWK T4 on T4.IDX = T1.IDX ");
    sbSQL.append(" left join (select TEN.* from INAMS.MSTTEN TEN");
    sbSQL.append("  inner join INAMS.MSTTENBMN TBN");
    sbSQL.append("  on TBN.TENCD = TEN.TENCD");
    if (StringUtils.equals(null, szBmncd) || StringUtils.isEmpty(szBmncd)) {
      sbSQL.append(" and TBN.BMNCD = null");
    } else {
      sbSQL.append(" and TBN.BMNCD = " + szBmncd);
    }
    sbSQL.append("  and COALESCE(TEN.UPDKBN, 0) <> 1");
    sbSQL.append("  and COALESCE(TBN.UPDKBN, 0) <> 1");
    sbSQL.append(") T2 on T1.IDX = T2.TENCD and COALESCE(UPDKBN, 0) <> 1");
    sbSQL.append(" left join (select * from INAMS.MSTTENBMN where BMNCD = 1) T3 on T3.TENCD = T2.TENCD");
    sbSQL.append(joinTable);
    if (StringUtils.equals("TENNO", sortBtnId)) {
      sbSQL.append(" order by T1.IDX " + sortOrder);

    } else if (StringUtils.equals("RANKNO", sortBtnId)) {
      sbSQL.append(" order by (case when TRIM(T4.RNK) = '' then 2 else 1 end), T4.RNK " + sortOrder + ", T2.TENCD");

    } else if (StringUtils.equals("SANKOUHBJ", sortBtnId)) {
      sbSQL.append(" order by (case when SANKOUHBJ is null then 2 else 1 end), SANKOUHBJ " + sortOrder + ", TENCD");

    } else {
      sbSQL.append(" order by T1.IDX");
    }

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array);
    setOption(option);

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

    // 共通箇所設定
    createCmnOutput(jad);

  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

    ArrayList<String> paramData = new ArrayList<String>();
    JSONArray array = new JSONArray();

    String szBmncd = map.containsKey("BMNCD_INI") && !StringUtils.isEmpty(map.get("BMNCD_INI")) ? map.get("BMNCD_INI") : map.get("BMNCD"); // 部門
    String szMoyscd = map.containsKey("MOYSCD_INI") && !StringUtils.isEmpty(map.get("MOYSCD_INI")) ? map.get("MOYSCD_INI") : map.get("MOYSCD"); // 催しコード
    String szRinji = map.containsKey("RINJI_INI") && !StringUtils.isEmpty(map.get("RINJI_INI")) ? map.get("RINJI_INI") : map.get("RINJI"); // 臨時
    String szRankno = map.containsKey("RANKNO_INI") && !StringUtils.isEmpty(map.get("RANKNO_INI")) ? map.get("RANKNO_INI") : map.get("RANKNO"); // ランクNo.
    String szRankkn = map.containsKey("RANKKN_INI") && !StringUtils.isEmpty(map.get("RANKKN_INI")) ? map.get("RANKKN_INI") : ""; // ランク名称
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {

      String values = "", names = "", rows = "";

      // 部門
      if (StringUtils.isEmpty(szBmncd)) {
        values += ", null";
      } else {
        paramData.add(szBmncd);
        values += ", cast(? as char(" + MessageUtility.getDefByteLen(szBmncd) + "))";
      }
      names += ", F1";

      // ランク№
      if (StringUtils.isEmpty(szRankno)) {
        values += ", null";
      } else {
        paramData.add(szRankno);
        values += ", cast(? as char(" + MessageUtility.getDefByteLen(szRankno) + "))";
      }
      names += ", F2";

      // ランク名称
      if (StringUtils.isEmpty(szRankkn)) {
        values += ", null";
      } else {
        paramData.add(szRankkn);
        values += ", cast(? as char(" + MessageUtility.getDefByteLen(szRankkn) + "))";
      }
      names += ", F3";

      // 臨時
      if (StringUtils.isEmpty(szRinji)) {
        values += ", null";
      } else {
        if (StringUtils.equals("1", szRinji)) {
          paramData.add(DefineReport.Values.ON.getVal());
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(DefineReport.Values.ON.getVal()) + "))";
        } else {
          values += ", null";
        }
      }
      names += ", F4";

      // 催し
      if (StringUtils.isEmpty(szMoyscd)) {
        values += ", null";
      } else {
        paramData.add(szMoyscd);
        values += ", cast(? as char(" + MessageUtility.getDefByteLen(szMoyscd) + "))";
      }
      names += ", F5";

      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
      rows = StringUtils.removeStart(rows, ",");
      names = StringUtils.removeStart(names, ",");
      sbSQL.append(" select * from (VALUES ROW " + rows + ") as T1(" + names + ")");

    } else {

      // 臨時チェックありの場合
      if (StringUtils.equals("1", szRinji)) {
        sbSQL.append(" select");
        sbSQL.append(" BMNCD as F1"); // F1 : 部門
        sbSQL.append(", RANKNO as F2"); // F2 : ランクNo.
        if (StringUtils.isEmpty(szRankkn)) {
          sbSQL.append(", RANKKN as F3"); // F3 : ランク名称
        } else {
          sbSQL.append(", '" + szRankkn + "' as F3"); // F3 : ランク名称
        }
        sbSQL.append(", " + DefineReport.Values.ON.getVal() + " as F4"); // F4 : 臨時
        sbSQL.append(", right('0'||MOYSKBN, 1) || right('000000'||MOYSSTDT, 6) || right('000'||MOYSRBAN, 3) as F5"); // F5 : 催しコード
        sbSQL.append(", DATE_FORMAT(ADDDT,'%y/%m/%d') as F6 "); // F6 : 登録日
        sbSQL.append(", DATE_FORMAT(UPDDT,'%y/%m/%d') as F7 "); // F7 : 更新日
        sbSQL.append(", OPERATOR as F8"); // F8 : オペレータ
        sbSQL.append(" from INATK.TOKRANKEX");
        sbSQL.append(" where BMNCD =" + szBmncd);
        sbSQL.append(" and MOYSKBN =" + StringUtils.substring(szMoyscd, 0, 1));
        sbSQL.append(" and MOYSSTDT =" + StringUtils.substring(szMoyscd, 1, 7));
        sbSQL.append(" and MOYSRBAN =" + StringUtils.substring(szMoyscd, 7, 10));
        if (StringUtils.equals(null, szRankno) || StringUtils.isEmpty(szRankno)) {
          sbSQL.append(" and RANKNO = null");
        } else {
          sbSQL.append(" and RANKNO =" + szRankno);
        }
        sbSQL.append(" and COALESCE(UPDKBN, 0) <> 1");

      } else {
        // 臨時チェックなしの場合
        sbSQL.append(" select");
        sbSQL.append(" BMNCD as F1"); // F1 : 部門
        sbSQL.append(", RANKNO as F2"); // F2 : ランクNo.
        if (StringUtils.isEmpty(szRankkn)) {
          sbSQL.append(", RANKKN as F3"); // F3 : ランク名称
        } else {
          sbSQL.append(", '" + szRankkn + "' as F3"); // F3 : ランク名称
        }
        sbSQL.append(", " + DefineReport.Values.OFF.getVal() + " as F4"); // F4 : 臨時
        sbSQL.append(", NULL as F5"); // F5 : 催しコード
        sbSQL.append(", DATE_FORMAT(ADDDT,'%y/%m/%d') as F6 "); // F6 : 登録日
        sbSQL.append(", DATE_FORMAT(UPDDT,'%y/%m/%d') as F7 "); // F7 : 更新日
        sbSQL.append(", OPERATOR as F8"); // F8 : オペレータ
        sbSQL.append(" from INATK.TOKRANK");
        sbSQL.append(" where BMNCD =" + szBmncd);
        if (StringUtils.equals(null, szRankno) || StringUtils.isEmpty(szRankno)) {
          sbSQL.append(" and RANKNO = null");
        } else {
          sbSQL.append(" and RANKNO =" + szRankno);
        }
        sbSQL.append(" and COALESCE(UPDKBN, 0) <> 1");
      }

    }

    ItemList iL = new ItemList();
    array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }


  /**
   * 更新処理
   *
   * @param request
   * @param session
   * @param map
   * @param userInfo
   * @return
   */
  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {

    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
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

    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 削除処理
    try {
      msgObj = this.deleteData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
    }
    return msgObj;
  }

  /**
   * 更新処理実行
   *
   * @param map
   * @param userInfo
   * @return
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    JSONObject option = new JSONObject();
    new JSONArray();

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray dataArrayTeninfo = JSONArray.fromObject(map.get("DATA_TENINFO")); // 更新情報

    // 必須チェック
    if (dataArray.isEmpty() && dataArrayTeninfo.isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    if (StringUtils.equals("0", dataArray.getJSONObject(0).getString("F4"))) {
      // SQL発行：ランクマスタ
      this.createSqlTokrank(data, map, userInfo);

    } else {
      // 臨時チェックありの場合
      // SQL発行：臨時ランクマスタ
      this.createSqlTokrankex(data, map, userInfo);

    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      // 更新処理実行
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(super.getMessage())) {
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
      option.put(MsgKey.E.getKey(), super.getMessage());
    }
    return option;
  }

  boolean isTest = true;

  /**
   * ランクマスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  /*
   * public String createSqlTokrank(JSONArray dataArray, JSONArray dataArrayTeninfo, HashMap<String,
   * String> map, User userInfo, String sysdate){
   *
   * String dbsysdate = CmnDate.dbDateFormat(sysdate); JSONObject option = new JSONObject(); // 更新情報
   * ArrayList<String> prmData = new ArrayList<String>(); String values = "";
   *
   * String obj = map.get(DefineReport.ID_PARAM_OBJ); JSONObject msgObj = new JSONObject(); JSONArray
   * msg = new JSONArray();
   *
   * // パラメータ確認 int kryoColNum = 9; // テーブル列数 // ログインユーザー情報取得 int userId = userInfo.getCD_user(); //
   * ログインユーザー values = ""; // 更新情報 for (int i = 1; i <= kryoColNum; i++) { String col = "F" + i;
   * String val = dataArray.optJSONObject(0).optString(col); if (i==4) { // F4 : 店ランク配列： val = "";
   * for(int j=0; j < dataArrayTeninfo.size(); j++){ if
   * (StringUtils.isEmpty(dataArrayTeninfo.optJSONObject(j).optString("F3"))) { val += " "; } else {
   * val += dataArrayTeninfo.optJSONObject(j).optString("F3"); } } } else if (i==5) { // F5 : 更新区分：
   * val = "0"; } else if (i==6) { // F6 : 送信フラグ： val = "0"; } else if (i==7) { // F7 : オペレーター： val =
   * ""+userId; } else if (i==8) { // F8 : 登録日： val = dbsysdate; } else if (i==9) { // F9 : 更新日： val =
   * dbsysdate; } if (isTest) { if (i == 1) { values += "( '" + val + "'"; // F1 : 部門： } else if (i ==
   * 9) { values += ", '" + val + "')"; // F9 : 更新日： } else { values += ", '" + val + "'"; // F2 :
   * ランクNo.：, F3 : ランク名称： } } else { prmData.add(val); values += ", ?"; } } // 基本INSERT/UPDATE文
   * StringBuffer sbSQL; // 更新SQL sbSQL = new StringBuffer();
   * sbSQL.append("merge into INATK.TOKRANK as T using (select"); sbSQL.append("  BMNCD"); // F1 : 部門：
   * sbSQL.append(", RANKNO"); // F2 : ランクNo.： sbSQL.append(", RANKKN"); // F3 : ランク名称：
   * sbSQL.append(", TENRANK_ARR"); // F4 : 店ランク配列： sbSQL.append(", UPDKBN"); // F5 : 更新区分
   * sbSQL.append(", SENDFLG"); // F6 : 送信フラグ： sbSQL.append(", OPERATOR"); // F7 : オペレータ
   * sbSQL.append(", ADDDT"); // F8 : 登録日 sbSQL.append(", UPDDT"); // F9 : 更新日
   * sbSQL.append(" from (values "+values+" ) as T1("); sbSQL.append("  BMNCD"); // F1 : 部門：
   * sbSQL.append(", RANKNO"); // F2 : ランクNo.： sbSQL.append(", RANKKN"); // F3 : ランク名称：
   * sbSQL.append(", TENRANK_ARR"); // F4 : 店ランク配列： sbSQL.append(", UPDKBN"); // F5 : 更新区分
   * sbSQL.append(", SENDFLG"); // F6 : 送信フラグ： sbSQL.append(", OPERATOR"); // F7 : オペレータ
   * sbSQL.append(", ADDDT"); // F8 : 登録日 sbSQL.append(", UPDDT"); // F9 : 更新日
   * sbSQL.append("))as RE on (T.BMNCD = RE.BMNCD and T.RANKNO = RE.RANKNO)");
   * sbSQL.append(" when matched then update set"); sbSQL.append("  BMNCD = RE.BMNCD"); // F1 : 部門：
   * sbSQL.append(", RANKNO = RE.RANKNO"); // F2 : ランクNo.： sbSQL.append(", RANKKN = RE.RANKKN"); // F3
   * : ランク名称： sbSQL.append(", TENRANK_ARR = RE.TENRANK_ARR"); // F4 : 店ランク配列：
   * sbSQL.append(", UPDKBN = RE.UPDKBN"); // F5 : 更新区分： sbSQL.append(", SENDFLG = RE.SENDFLG"); // F6
   * : 送信フラグ： sbSQL.append(", OPERATOR = RE.OPERATOR"); // F7 : オペレータ
   * sbSQL.append(", UPDDT = RE.UPDDT"); // F9 : 更新日 sbSQL.append(" when not matched then insert(");
   * sbSQL.append("  BMNCD"); // F1 : 部門： sbSQL.append(", RANKNO"); // F2 : ランクNo.：
   * sbSQL.append(", RANKKN"); // F3 : ランク名称： sbSQL.append(", TENRANK_ARR"); // F4 : 店ランク配列：
   * sbSQL.append(", UPDKBN"); // F5 : 更新区分： sbSQL.append(", SENDFLG"); // F6 : 送信フラグ：
   * sbSQL.append(", OPERATOR"); // F7 : オペレータ sbSQL.append(", ADDDT"); // F8 : 登録日
   * sbSQL.append(", UPDDT"); // F9 : 更新日 sbSQL.append(") values ("); sbSQL.append("  RE.BMNCD"); //
   * F1 : 部門： sbSQL.append(", RE.RANKNO"); // F2 : ランクNo.： sbSQL.append(", RE.RANKKN"); // F3 : ランク名称：
   * sbSQL.append(", RE.TENRANK_ARR"); // F4 : 店ランク配列： sbSQL.append(", RE.UPDKBN"); // F5 : 更新区分：
   * sbSQL.append(", RE.SENDFLG"); // F6 : 送信フラグ： sbSQL.append(", RE.OPERATOR"); // F7 : オペレータ
   * sbSQL.append(", RE.ADDDT"); // F8 : 登録日 sbSQL.append(", RE.UPDDT"); // F9 : 更新日
   * sbSQL.append(")");
   *
   * System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());
   *
   * sqlList.add(sbSQL.toString()); prmList.add(prmData); lblList.add("ランクマスタ");
   *
   * // // クリア // prmData = new ArrayList<String>(); // valueData = new Object[]{}; // values = "";
   *
   * return sbSQL.toString(); }
   */

  /**
   * ランクマスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTokrank(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayTeninfo = JSONArray.fromObject(map.get("DATA_TENINFO")); // 更新情報


    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    map.get("DISPTYPE");

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 4; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // values += String.valueOf(0 + 1);

      }
      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);

        if (StringUtils.equals("F4", key)) {
          val = "";
          val = this.createArr(dataArrayTeninfo, map);

          /*
           * for(int j=0; j < dataArrayTeninfo.size(); j++){ if
           * (StringUtils.isEmpty(dataArrayTeninfo.optJSONObject(j).optString("F3"))) { val += " "; } else {
           * val += dataArrayTeninfo.optJSONObject(j).optString("F3"); } }
           */
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          values += ", ?";
          prmData.add(val);
        }
      }
      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, values);
        values = "";
      }
    }

    // ランクマスタの登録・更新
    sbSQL = new StringBuffer();
    sbSQL.append(" INSERT INTO INATK.TOKRANK (");
    sbSQL.append("  BMNCD"); // F1 : 部門
    sbSQL.append(" ,RANKNO"); // F2 : ランク№
    sbSQL.append(" ,RANKKN"); // F3 : ランク名称
    sbSQL.append(" ,TENRANK_ARR"); // F4 : 店ランク配列
    sbSQL.append(", SENDFLG"); // 更新区分
    sbSQL.append(", UPDKBN"); // 更新区分
    sbSQL.append(", OPERATOR "); // オペレーター
    sbSQL.append(", ADDDT "); // 登録日
    sbSQL.append(", UPDDT "); // 更新日
    sbSQL.append(") values (");
    sbSQL.append(StringUtils.join(valueData, ",").substring(1));
    sbSQL.append(", 0 "); // 更新区分
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分
    sbSQL.append(", '" + userId + "' "); // オペレーター
    sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日
    sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
    sbSQL.append(")");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" BMNCD=VALUES(BMNCD) ");
    sbSQL.append(",RANKNO=VALUES(RANKNO) ");
    sbSQL.append(",RANKKN=VALUES(RANKKN) ");
    sbSQL.append(",TENRANK_ARR=VALUES(TENRANK_ARR) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    sbSQL.append(",ADDDT=VALUES(ADDDT) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("ランクマスタ");

    return sbSQL.toString();
  }

  public String createArr(JSONArray dataArray, HashMap<String, String> map) {
    String Arr = "";
    int maxRow = 400;

    for (int j = 0; j < maxRow; j++) {
      String tencd = ("000" + (j + 1));
      tencd = StringUtils.substring(tencd, tencd.length() - 3, tencd.length());
      // String tencd = StringUtils.substring(("000" + (j + 1)), 0, 3);
      String param = " ";

      for (int i = 0; i < dataArray.size(); i++) {
        JSONObject data = dataArray.getJSONObject(i);
        if (StringUtils.equals(tencd, data.optString("F1"))) {
          if (StringUtils.isNotEmpty(data.optString("F3").trim())) {
            param = data.optString("F3");
            break;
          }
        }
      }
      Arr += param;
    }
    return Arr;
  }

  /**
   * 臨時ランクマスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  /*
   * public String createSqlTokrankex(JSONArray dataArray, JSONArray dataArrayTeninfo, HashMap<String,
   * String> map, User userInfo, String sysdate){ String dbsysdate = CmnDate.dbDateFormat(sysdate);
   * JSONObject option = new JSONObject(); // 更新情報 ArrayList<String> prmData = new
   * ArrayList<String>(); String values = "";
   *
   * String obj = map.get(DefineReport.ID_PARAM_OBJ); JSONObject msgObj = new JSONObject(); JSONArray
   * msg = new JSONArray();
   *
   * // パラメータ確認 int kryoColNum = 12; // テーブル列数 // ログインユーザー情報取得 int userId = userInfo.getCD_user(); //
   * ログインユーザー
   *
   * // 更新情報 for (int i = 1; i <= kryoColNum; i++) { String col = "F" + i; String val =
   * dataArray.optJSONObject(0).optString(col); if (i==2) { // F2 : 催し区分： val =
   * StringUtils.substring(dataArray.optJSONObject(0).optString("F5"), 0, 1); } else if (i==3) { // F3
   * : 催し開始日： val = StringUtils.substring(dataArray.optJSONObject(0).optString("F5"), 1, 7); } else if
   * (i==4) { // F4 : 催し連番： val = StringUtils.substring(dataArray.optJSONObject(0).optString("F5"), 7,
   * 10); } else if (i==5) { // F5 : ランクNo.： val = dataArray.optJSONObject(0).optString("F2"); } else
   * if (i==6) { // F6 : ランク名称： val = dataArray.optJSONObject(0).optString("F3"); } else if (i==7) {
   * // F7 : 店ランク配列： val = ""; for(int j=0; j < dataArrayTeninfo.size(); j++){ if
   * (StringUtils.isEmpty(dataArrayTeninfo.optJSONObject(j).optString("F3"))) { val += " "; } else {
   * val += dataArrayTeninfo.optJSONObject(j).optString("F3"); } } } else if (i==8) { // F8 : 更新区分：
   * val = "0"; } else if (i==9) { // F9 : 送信フラグ： val = "0"; } else if (i==10) { // F10 : オペレーター： val
   * = ""+userId; } else if (i==11) { // F11 : 登録日： val = dbsysdate; } else if (i==12) { // F12 : 更新日：
   * val = dbsysdate; } if (isTest) { if (i == 1) { values += "( '" + val + "'"; // F1 : 部門： } else if
   * (i == 12) { values += ", '" + val + "')"; // F12 : 更新日： } else { values += ", '" + val + "'"; } }
   * else { prmData.add(val); values += ", ?"; } } // 基本INSERT/UPDATE文 StringBuffer sbSQL; // 更新SQL
   * sbSQL = new StringBuffer(); sbSQL.append("merge into INATK.TOKRANKEX as T using (select");
   * sbSQL.append("  BMNCD"); // F1 : 部門： sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
   * sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日： sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
   * sbSQL.append(", RANKNO"); // F5 : ランクNo.： sbSQL.append(", RANKKN"); // F6 : ランク名称：
   * sbSQL.append(", TENRANK_ARR"); // F7 : 店ランク配列： sbSQL.append(", UPDKBN"); // F8 : 更新区分
   * sbSQL.append(", SENDFLG"); // F9 : 送信フラグ： sbSQL.append(", OPERATOR"); // F10 : オペレータ
   * sbSQL.append(", ADDDT"); // F11 : 登録日 sbSQL.append(", UPDDT"); // F12 : 更新日
   * sbSQL.append(" from (values "+values+" ) as T1("); sbSQL.append("  BMNCD"); // F1 : 部門：
   * sbSQL.append(", MOYSKBN"); // F2 : 催し区分： sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日：
   * sbSQL.append(", MOYSRBAN"); // F4 : 催し連番： sbSQL.append(", RANKNO"); // F5 : ランクNo.：
   * sbSQL.append(", RANKKN"); // F6 : ランク名称： sbSQL.append(", TENRANK_ARR"); // F7 : 店ランク配列：
   * sbSQL.append(", UPDKBN"); // F8 : 更新区分 sbSQL.append(", SENDFLG"); // F9 : 送信フラグ：
   * sbSQL.append(", OPERATOR"); // F10 : オペレータ sbSQL.append(", ADDDT"); // F11 : 登録日
   * sbSQL.append(", UPDDT"); // F12 : 更新日 sbSQL.
   * append("))as RE on (T.BMNCD = RE.BMNCD and T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.RANKNO = RE.RANKNO)"
   * ); sbSQL.append(" when matched then update set"); sbSQL.append("  BMNCD = RE.BMNCD"); // F1 : 部門：
   * sbSQL.append(", MOYSKBN = RE.MOYSKBN"); // F2 : 催し区分： sbSQL.append(", MOYSSTDT = RE.MOYSSTDT");
   * // F3 : 催し開始日： sbSQL.append(", MOYSRBAN = RE.MOYSRBAN"); // F4 : 催し連番：
   * sbSQL.append(", RANKNO = RE.RANKNO"); // F5 : ランクNo.： sbSQL.append(", RANKKN = RE.RANKKN"); // F6
   * : ランク名称： sbSQL.append(", TENRANK_ARR = RE.TENRANK_ARR"); // F7 : 店ランク配列：
   * sbSQL.append(", UPDKBN = RE.UPDKBN"); // F8 : 更新区分： sbSQL.append(", SENDFLG = RE.SENDFLG"); // F9
   * : 送信フラグ： sbSQL.append(", OPERATOR = RE.OPERATOR"); // F10 : オペレータ
   * sbSQL.append(", UPDDT = RE.UPDDT"); // F12 : 更新日 sbSQL.append(" when not matched then insert(");
   * sbSQL.append("  BMNCD"); // F1 : 部門： sbSQL.append(", MOYSKBN"); // F2 : 催し区分：
   * sbSQL.append(", MOYSSTDT"); // F3 : 催し開始日： sbSQL.append(", MOYSRBAN"); // F4 : 催し連番：
   * sbSQL.append(", RANKNO"); // F5 : ランクNo.： sbSQL.append(", RANKKN"); // F6 : ランク名称：
   * sbSQL.append(", TENRANK_ARR"); // F7 : 店ランク配列： sbSQL.append(", UPDKBN"); // F8 : 更新区分：
   * sbSQL.append(", SENDFLG"); // F9 : 送信フラグ： sbSQL.append(", OPERATOR"); // F10 : オペレータ
   * sbSQL.append(", ADDDT"); // F11 : 登録日 sbSQL.append(", UPDDT"); // F12 : 更新日
   * sbSQL.append(") values ("); sbSQL.append("  RE.BMNCD"); // F1 : 部門： sbSQL.append(", RE.MOYSKBN");
   * // F2 : 催し区分： sbSQL.append(", RE.MOYSSTDT"); // F3 : 催し開始日： sbSQL.append(", RE.MOYSRBAN"); // F4
   * : 催し連番： sbSQL.append(", RE.RANKNO"); // F5 : ランクNo.： sbSQL.append(", RE.RANKKN"); // F6 : ランク名称：
   * sbSQL.append(", RE.TENRANK_ARR"); // F7 : 店ランク配列： sbSQL.append(", RE.UPDKBN"); // F8 : 更新区分：
   * sbSQL.append(", RE.SENDFLG"); // F9 : 送信フラグ： sbSQL.append(", RE.OPERATOR"); // F10 : オペレータ
   * sbSQL.append(", RE.ADDDT"); // F11 : 登録日 sbSQL.append(", RE.UPDDT"); // F12 : 更新日
   * sbSQL.append(")");
   *
   * System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());
   *
   * sqlList.add(sbSQL.toString()); prmList.add(prmData); lblList.add("臨時ランクマスタ");
   *
   * // // クリア // prmData = new ArrayList<String>(); // valueData = new Object[]{}; // values = "";
   *
   * return sbSQL.toString(); }
   */

  /**
   * 臨時ランクマスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlTokrankex(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayTeninfo = JSONArray.fromObject(map.get("DATA_TENINFO")); // 更新情報


    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    map.get("DISPTYPE");

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 7; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // values += String.valueOf(0 + 1);

      }
      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);

        if (StringUtils.equals("F7", key)) {
          val = "";
          val = this.createArr(dataArrayTeninfo, map);
          /*
           * for(int j=0; j < dataArrayTeninfo.size(); j++){
           *
           * if (StringUtils.isEmpty(dataArrayTeninfo.optJSONObject(j).optString("F3"))) { val += " "; } else
           * { val += dataArrayTeninfo.optJSONObject(j).optString("F3"); } }
           */
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          values += ", ?";
          prmData.add(val);
        }
      }
      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, values);
        values = "";
      }
    }

    // 臨時ランクマスタの登録・更新
    sbSQL = new StringBuffer();
    sbSQL.append(" INSERT INTO INATK.TOKRANKEX (");
    sbSQL.append("  BMNCD"); // F1 : 部門
    sbSQL.append(" ,MOYSKBN"); // F2 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F3 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F4 : 催し連番
    sbSQL.append(" ,RANKNO"); // F5 : ランク№
    sbSQL.append(" ,RANKKN"); // F6 : ランク名称
    sbSQL.append(" ,TENRANK_ARR"); // F7 : 店ランク配列
    sbSQL.append(", UPDKBN"); // 更新区分
    sbSQL.append(", SENDFLG"); // 送信フラグ
    sbSQL.append(", OPERATOR "); // オペレーター
    sbSQL.append(", ADDDT "); // 登録日
    sbSQL.append(", UPDDT "); // 更新日
    sbSQL.append(") values (");
    sbSQL.append(StringUtils.join(valueData, ",").substring(1));
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分
    sbSQL.append(", 0 "); // 送信フラグ
    sbSQL.append(", '" + userId + "' "); // オペレーター
    sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日
    sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
    sbSQL.append(")");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" BMNCD=VALUES(BMNCD) ");
    sbSQL.append(",MOYSKBN=VALUES(MOYSKBN) ");
    sbSQL.append(",MOYSSTDT=VALUES(MOYSSTDT) ");
    sbSQL.append(",MOYSRBAN=VALUES(MOYSRBAN) ");
    sbSQL.append(",RANKNO=VALUES(RANKNO) ");
    sbSQL.append(",RANKKN=VALUES(RANKKN) ");
    sbSQL.append(",TENRANK_ARR=VALUES(TENRANK_ARR) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("臨時ランクマスタ");

    return sbSQL.toString();
  }

  /**
   * 削除処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    new ArrayList<String>();
    new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    if (StringUtils.equals("0", dataArray.getJSONObject(0).getString("F4"))) {
      // SQL発行：ランクマスタ
      createDelSqlTokrank(data, map, userInfo);
    } else {
      // 臨時チェックありの場合
      // SQL発行：臨時ランクマスタ
      createDelSqlTokrankex(data, map, userInfo);

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
   * ランクマスタDELETE(論理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlTokrank(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    sqlWhere += " where BMNCD=?";
    sqlWhere += " and RANKNO=?";
    paramData.add(data.optString("F1")); // 部門
    paramData.add(data.optString("F2")); // ランクNo.

    // ランクマスタの論理削除
    sbSQL.append("update INATK.TOKRANK");
    sbSQL.append(" set");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", SENDFLG=0");
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("ランクマスタ");

    return sbSQL.toString();
  }

  /**
   * 臨時ランクマスタDELETE(論理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlTokrankex(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    sqlWhere += " where BMNCD=?";
    sqlWhere += " and MOYSKBN=?";
    sqlWhere += " and MOYSSTDT=?";
    sqlWhere += " and MOYSRBAN=?";
    sqlWhere += " and RANKNO=?";
    paramData.add(data.optString("F1")); // 部門
    paramData.add(StringUtils.substring(data.optString("F5"), 0, 1)); // 催し区分
    paramData.add(StringUtils.substring(data.optString("F5"), 1, 7)); // 催し開始日
    paramData.add(StringUtils.substring(data.optString("F5"), 7, 10)); // 催し連番
    paramData.add(data.optString("F2")); // ランクNo.

    // 臨時ランクマスタの論理削除
    sbSQL.append("update INATK.TOKRANKEX");
    sbSQL.append(" set");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", SENDFLG=0");
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("臨時ランクマスタ");

    return sbSQL.toString();
  }

  /**
   * チェック処理
   *
   * @param map
   * @return
   */
  @SuppressWarnings("static-access")
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray.fromObject(map.get("DATA_TENINFO"));

    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";
    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 入力値を取得
    JSONObject data = dataArray.getJSONObject(0);
    ItemList iL = new ItemList();

    // 新規登録重複チェック
    if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
      if (dataArray.size() > 0) {
        paramData = new ArrayList<String>();
        if (StringUtils.equals("0", data.getString("F4"))) {
          paramData.add(data.getString("F1"));
          paramData.add(data.getString("F2"));
          sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKRANK where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and BMNCD = ? and RANKNO = ?";

          JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
          if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
            JSONObject o = mu.getDbMessageObj("E00004", "ランクマスタ");
            msg.add(o);
            return msg;
          }

        } else {
          paramData = new ArrayList<String>();
          paramData.add(data.getString("F1"));
          paramData.add(data.getString("F2"));
          paramData.add(data.getString("F3"));
          paramData.add(data.getString("F4"));
          paramData.add(data.getString("F5"));
          sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKRANKEX where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal()
              + " and BMNCD = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and RANKNO = ?";

          JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
          if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
            JSONObject o = mu.getDbMessageObj("E00004", "臨時ランクマスタ");
            msg.add(o);
            return msg;
          }
        }
      }
    }

    return msg;
  }
}
