package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.Defines;
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
public class ReportJU032Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportJU032Dao(String JNDIname) {
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

    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    // User userInfo = getUserInfo();
    // if(userInfo==null){
    // return "";
    // }
    //
    // String szSircd = getMap().get("SIRCD"); // 選択メーカーコード
    // String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン
    //
    // // パラメータ確認
    // // 必須チェック
    // if ( szSircd == null) {
    // System.out.println(super.getConditionLog());
    // return "";
    // }
    //
    // // タイトル情報(任意)設定
    // List<String> titleList = new ArrayList<String>();


    StringBuffer sbSQL = new StringBuffer();

    // // オプション情報（タイトル）設定
    // JSONObject option = new JSONObject();
    // option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    // setOption(option);
    //
    // System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
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

  // 対象店関連
  String tenht = "1"; // 1:同一発注数量 2:ランク別発注数量 3:店別発注数量
  String kanrino = "";
  Set<Integer> tencds = new TreeSet<Integer>();
  HashMap<String, String> tenRank = new HashMap<String, String>();

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(店別アンケート付き送り付け_催し)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_TENHT")); // 更新情報(数量パターン)

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 店舗アンケート付き送り付けINSERT/UPDATE処理
    createSqlQju(data, dataArrayG, userInfo);

    // 排他チェック実行
    if (!StringUtils.isEmpty(data.optString("F1"))) {

      String moyskbn = "";
      String moysstdt = "";
      String moysrban = "";

      if (data.optString("F1").length() >= 8) {
        moyskbn = data.optString("F1").substring(0, 1);
        moysstdt = data.optString("F1").substring(1, 7);
        moysrban = data.optString("F1").substring(7);
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        targetWhere = "MOYSKBN=null AND ";
      } else {
        targetWhere = "MOYSKBN=? AND ";
        targetParam.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        targetWhere += "MOYSSTDT=null AND ";
      } else {
        targetWhere += "MOYSSTDT=? AND ";
        targetParam.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        targetWhere += "MOYSRBAN=null AND ";
      } else {
        targetWhere += "MOYSRBAN=? AND ";
        targetParam.add(moysrban);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        targetWhere += "KANRINO=null AND ";
      } else {
        targetWhere += "KANRINO=? AND ";
        targetParam.add(kanrino);
      }

      targetWhere += " UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ";

      targetTable = "INATK.TOKQJU_SHN";

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, "")) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
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
        option.put(MsgKey.S.getKey(), MessageUtility.getDbMessageIdObj("I00002", new String[] {"管理番号" + kanrino + "で"}));
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
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(店別アンケート付き送り付け_催し)
    JSONArray dataArrayG = JSONArray.fromObject(map.get("DATA_TENHT")); // 更新情報(数量パターン)
    String shoriDt = map.get("SHORIDT"); // 処理日付

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    MessageUtility mu = new MessageUtility();
    JSONArray dbDatas = new JSONArray();

    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String rankNoAdd = "";
    String sryPtnNo = "";

    String[] taisyoTen = new String[] {};

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    if (dataArrayG.size() == 0 || dataArrayG.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 催しコード存在チェック
    JSONObject data = dataArray.getJSONObject(0);

    if (!StringUtils.isEmpty(data.optString("F1")) && data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    } else {
      msg.add(mu.getDbMessageObj("E20005", new String[] {}));
      return msg;
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "T1.MOYSKBN=null AND ";
    } else {
      sqlWhere += "T1.MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "T1.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "T1.MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "T1.MOYSRBAN=null AND ";
    } else {
      sqlWhere += "T1.MOYSRBAN=? AND ";
      paramData.add(moysrban);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("T2.NNSTDT "); // 納入開始日
    sbSQL.append(",T2.NNEDDT "); // 納入終了日
    sbSQL.append("FROM ");
    sbSQL.append("INATK.TOKQJU_MOY T1 LEFT JOIN INATK.TOKMOYCD T2 ON T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された催しコード
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // 登録のない催しコード
      msg.add(mu.getDbMessageObj("E20005", new String[] {}));
      return msg;
    }

    // 発注日範囲チェック
    if (!StringUtils.isEmpty(data.optString("F13")) && !StringUtils.isEmpty(data.optString("F12")) && data.optInt("F13") <= data.optInt("F12")) {
      // 納入日 <= 発注日はエラー
      msg.add(mu.getDbMessageObj("E20264", new String[] {}));
      return msg;
    }

    // 納入日の範囲チェック
    if (dbDatas.getJSONObject(0).optInt("NNSTDT") > data.optInt("F13") || dbDatas.getJSONObject(0).optInt("NNEDDT") < data.optInt("F13")) {
      // 納入日 <= 発注日はエラー
      msg.add(mu.getDbMessageObj("E20274", new String[] {}));
      return msg;
    }

    // 処理日付 < 発注日 < 納入日以外エラー
    if (!StringUtils.isEmpty(shoriDt)) {
      if (!(Integer.valueOf(shoriDt) < data.optInt("F12"))) {
        // 発注日>処理日付の条件で入力してください。
        msg.add(mu.getDbMessageObj("E20127", new String[] {}));
        return msg;
      } else if (!(data.optInt("F12") < data.optInt("F13"))) {
        // 発注日 ＜ 納入日の条件で入力してください。
        msg.add(mu.getDbMessageObj("E20264", new String[] {}));
        return msg;
      }
    }

    // 同一数量、ランク別発注数量、店別数量発注のいずれか一つのみ入力可能(必須)
    for (int i = 0; i < dataArrayG.size(); i++) {
      JSONObject dataG = new JSONObject();
      dataG = dataArrayG.getJSONObject(i);
      if (!dataG.isEmpty() && !StringUtils.isEmpty(dataG.optString("F2"))) {
        tenht = "3";
        break;
      }
    }

    if ((!StringUtils.isEmpty(data.optString("F14")) || !StringUtils.isEmpty(data.optString("F15"))) && (!StringUtils.isEmpty(data.optString("F16")) || !StringUtils.isEmpty(data.optString("F17")))) {
      // 同一数量発注入力の場合、ランク別発注数量入力登録できません。
      msg.add(mu.getDbMessageObj("E20461", new String[] {}));
      return msg;
    }

    if ((!StringUtils.isEmpty(data.optString("F14")) || !StringUtils.isEmpty(data.optString("F15")))) {
      if (tenht.equals("3")) {
        // 同一数量発注入力の場合、店別数量発注入力登録できません。
        msg.add(mu.getDbMessageObj("E20462", new String[] {}));
        return msg;
      }
    }

    if ((!StringUtils.isEmpty(data.optString("F16")) || !StringUtils.isEmpty(data.optString("F17")))) {
      if (tenht.equals("3")) {
        // ランク別発注数量入力の場合、店別数量発注入力登録できません。
        msg.add(mu.getDbMessageObj("E20463", new String[] {}));
        return msg;
      }
      tenht = "2";
    }

    if (StringUtils.isEmpty(data.optString("F14")) && StringUtils.isEmpty(data.optString("F15")) && StringUtils.isEmpty(data.optString("F16")) && StringUtils.isEmpty(data.optString("F17"))
        && !tenht.equals("3")) {
      // 同一発注数量、ランク別発注数量、店別発注数量のいづれか一つは入力してください。
      msg.add(mu.getDbMessageObj("E20569", new String[] {}));
      return msg;
    }

    if (tenht.equals("1")) {
      if ((!StringUtils.isEmpty(data.optString("F14")) && StringUtils.isEmpty(data.optString("F15")))) {
        msg.add(mu.getDbMessageObj("EX1103", new String[] {"発注数"}));
        return msg;
      }
      if ((StringUtils.isEmpty(data.optString("F14")) && !StringUtils.isEmpty(data.optString("F15")))) {
        msg.add(mu.getDbMessageObj("EX1103", new String[] {"ランク"}));
        return msg;
      }
    }
    if (tenht.equals("2")) {
      if ((!StringUtils.isEmpty(data.optString("F16")) && StringUtils.isEmpty(data.optString("F17")))) {
        msg.add(mu.getDbMessageObj("EX1103", new String[] {"パターン"}));
        return msg;
      }
      if ((StringUtils.isEmpty(data.optString("F16")) && !StringUtils.isEmpty(data.optString("F17")))) {
        msg.add(mu.getDbMessageObj("EX1103", new String[] {"ランク"}));
        return msg;
      }
    }

    // 変数を初期化
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();

    // 商品存在チェック
    if (StringUtils.isEmpty(data.optString("F2"))) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(data.optString("F2"));
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTSHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");


    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      // マスタに登録のない商品
      msg.add(mu.getDbMessageObj("E40012", new String[] {}));
      return msg;
    }

    // 変数を初期化
    dbDatas = new JSONArray();
    // 催し区分9で使用されている商品は登録不可
    dbDatas = getMoysCdChk(data);

    if (dbDatas.size() != 0) {
      // 催し区分9で使用されている商品は登録不可
      msg.add(mu.getDbMessageObj("E20162", new String[] {"催し区分=9で"}));
      return msg;
    }

    // 対象店取得
    if (tenht.equals("3")) {
      for (int i = 0; i < dataArrayG.size(); i++) {
        JSONObject dataG = new JSONObject();
        dataG = dataArrayG.getJSONObject(i);

        if (!dataG.isEmpty() && !StringUtils.isEmpty(dataG.optString("F1"))) {

          int tencd = dataG.optInt("F1");

          if (!tencds.contains(tencd)) {
            tencds.add(tencd);
          }
        }
      }

      // 同一発注数量 or ランク別発注数量に入力があった場合ランクマスタより対象店を取得
    } else {

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      rankNoAdd = data.optString("F14");
      if (StringUtils.isEmpty(rankNoAdd)) {
        rankNoAdd = data.optString("F16");
        sryPtnNo = data.optString("F17");
      }

      sqlFrom = "INATK.TOKRANK ";

      // 商品コードより部門コードを取得
      if (StringUtils.isEmpty(data.optString("F2"))) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        if (data.optString("F2").length() >= 2) {
          sqlWhere += "BMNCD=? AND ";
          paramData.add(data.optString("F2").substring(0, 2));
        } else {
          sqlWhere += "BMNCD=null AND ";
        }
      }

      if (Integer.valueOf(rankNoAdd) >= 900) {

        // 催し区分
        if (StringUtils.isEmpty(moyskbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moyskbn);
        }

        // 催し開始日
        if (StringUtils.isEmpty(moysstdt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysstdt);
        }

        // 催し連番
        if (StringUtils.isEmpty(moysrban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysrban);
        }

        sqlFrom = "INATK.TOKRANKEX ";
      }

      // ランクNo.
      sqlWhere += "RANKNO=? AND ";
      paramData.add(rankNoAdd);

      // 一覧表情報
      sbSQL = new StringBuffer();
      sbSQL.append("SELECT ");
      sbSQL.append("TENRANK_ARR ");
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // 存在しない対象店ランク№
        if (Integer.valueOf(rankNoAdd) >= 900) {
          msg.add(mu.getDbMessageObj("E20466", new String[] {}));
        } else {
          msg.add(mu.getDbMessageObj("E20057", new String[] {}));
        }
        return msg;
      }

      // 対象店の取得
      taisyoTen = dbDatas.getJSONObject(0).getString("TENRANK_ARR").split("");

      for (int i = 0; i < taisyoTen.length; i++) {
        if (!StringUtils.isEmpty(StringUtils.trim(taisyoTen[i]))) {

          // 店コード取得用
          if (!tencds.contains(i + 1)) {
            tencds.add(i + 1);
          }

          // 店ランク取得用
          if (!tenRank.containsKey(i + 1)) {
            tenRank.put(String.valueOf(i + 1), taisyoTen[i]);
          }
        }
      }

      if (!StringUtils.isEmpty(sryPtnNo)) {

        // 変数を初期化
        sbSQL = new StringBuffer();
        iL = new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<String>();

        sqlFrom = "INATK.TOKSRPTN ";

        // 商品コードより部門コードを取得
        if (StringUtils.isEmpty(data.optString("F2"))) {
          sqlWhere += "BMNCD=null AND ";
        } else {
          if (data.optString("F2").length() >= 2) {
            sqlWhere += "BMNCD=? AND ";
            paramData.add(data.optString("F2").substring(0, 2));
          } else {
            sqlWhere += "BMNCD=null AND ";
          }
        }

        if (Integer.valueOf(sryPtnNo) >= 900) {

          // 催し区分
          if (StringUtils.isEmpty(moyskbn)) {
            sqlWhere += "MOYSKBN=null AND ";
          } else {
            sqlWhere += "MOYSKBN=? AND ";
            paramData.add(moyskbn);
          }

          // 催し開始日
          if (StringUtils.isEmpty(moysstdt)) {
            sqlWhere += "MOYSSTDT=null AND ";
          } else {
            sqlWhere += "MOYSSTDT=? AND ";
            paramData.add(moysstdt);
          }

          // 催し連番
          if (StringUtils.isEmpty(moysrban)) {
            sqlWhere += "MOYSRBAN=null AND ";
          } else {
            sqlWhere += "MOYSRBAN=? AND ";
            paramData.add(moysrban);
          }

          sqlFrom = "INATK.TOKSRPTNEX ";
        }

        // ランクNo.
        sqlWhere += "SRYPTNNO=? AND ";
        paramData.add(sryPtnNo);

        // 一覧表情報
        sbSQL = new StringBuffer();
        sbSQL.append("SELECT ");
        sbSQL.append("SRYPTNNO ");
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere);
        sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() == 0) {
          // 存在しない数量パターン
          msg.add(mu.getDbMessageObj("E20131", new String[] {}));
          return msg;
        }
      }
    }

    return msg;
  }

  public JSONArray getMoysCdChk(JSONObject data) {

    // 変数を初期化
    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> paramData = new ArrayList<String>();
    ItemList iL = new ItemList();
    String sqlValues = "";

    // 催し区分が9なら事前打出し、8ならアンケート付送り付け
    String table = "INATK.TOKJU_SHN";
    if (data.optString("F1").substring(0, 1).equals("9")) {
      table = "INATK.TOKQJU_SHN";
    }

    // 商品存在チェック
    if (StringUtils.isEmpty(data.optString("F2"))) {
      sqlValues += "null, ";
    } else {
      sqlValues += "CAST(? AS CHARACTER(14)), ";
      paramData.add(data.optString("F2"));
    }

    // 納入日存在チェック
    if (StringUtils.isEmpty(data.optString("F13"))) {
      sqlValues += "null, ";
    } else {
      sqlValues += "CAST(? AS signed), ";
      paramData.add(data.optString("F13"));
    }

    // 発注日存在チェック
    if (StringUtils.isEmpty(data.optString("F12"))) {
      sqlValues += "null, ";
    } else {
      sqlValues += "CAST(? AS signed), ";
      paramData.add(data.optString("F12"));
    }

    // 商品区分存在チェック
    if (StringUtils.isEmpty(data.optString("F3"))) {
      sqlValues += "null, ";
    } else {
      sqlValues += "CAST(? AS signed), ";
      paramData.add(data.optString("F3"));
    }

    // 別伝区分存在チェック
    if (StringUtils.isEmpty(data.optString("F6"))) {
      sqlValues += "0, ";
    } else {
      sqlValues += "CAST(? AS signed), ";
      paramData.add(data.optString("F6"));
    }

    // 入数存在チェック
    if (StringUtils.isEmpty(data.optString("F8"))) {
      sqlValues += "null, ";
    } else {
      sqlValues += "CAST(? AS signed), ";
      paramData.add(data.optString("F8"));
    }

    // 原価存在チェック
    if (StringUtils.isEmpty(data.optString("F9"))) {
      sqlValues += "null, ";
    } else {
      sqlValues += "CAST(? AS DECIMAL(8,2)), ";
      paramData.add(data.optString("F9"));
    }

    // 売価存在チェック
    if (StringUtils.isEmpty(data.optString("F10"))) {
      sqlValues += "null ";
    } else {
      sqlValues += "CAST(? AS signed) ";
      paramData.add(data.optString("F10"));
    }

    sbSQL.append("SELECT ");
    sbSQL.append("T2.SHNCD ");
    sbSQL.append("FROM ");
    sbSQL.append("( ");
    sbSQL.append("SELECT ");
    sbSQL.append("T1.SHNCD ");
    sbSQL.append(",T2.BINKBN ");
    sbSQL.append(",T1.NNDT ");
    sbSQL.append(",T1.HTDT ");
    sbSQL.append(",T1.SHNKBN ");
    sbSQL.append(",T1.BDENKBN ");
    sbSQL.append(",T2.TEIKANKBN ");
    sbSQL.append(",T1.IRISU ");
    sbSQL.append(",T1.GENKAAM ");
    sbSQL.append(",T1.BAIKAAM  ");
    sbSQL.append("FROM ");
    sbSQL.append("(values ROW(  ");
    sbSQL.append(sqlValues);
    sbSQL.append(")) AS T1(  ");
    sbSQL.append("SHNCD ");
    sbSQL.append(",NNDT ");
    sbSQL.append(",HTDT ");
    sbSQL.append(",SHNKBN ");
    sbSQL.append(",BDENKBN ");
    sbSQL.append(",IRISU ");
    sbSQL.append(",GENKAAM ");
    sbSQL.append(",BAIKAAM ");
    sbSQL.append(") LEFT JOIN INAMS.MSTSHN T2 ON T1.SHNCD=T2.SHNCD ");
    sbSQL.append(") T1 ");
    sbSQL.append(",( ");
    sbSQL.append("SELECT DISTINCT ");
    sbSQL.append("T1.SHNCD,T2.BINKBN,T1.NNDT,T1.HTDT,T1.SHNKBN,T1.BDENKBN,T2.TEIKANKBN,T1.IRISU,T1.GENKAAM,T1.BAIKAAM ");
    sbSQL.append("FROM ");
    sbSQL.append(table + " T1 LEFT JOIN INAMS.MSTSHN T2 ON T1.SHNCD=T2.SHNCD ");
    sbSQL.append("WHERE ");
    sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
    sbSQL.append(") T2 ");
    sbSQL.append("WHERE ");
    sbSQL.append("T1.SHNCD=T2.SHNCD AND ");
    sbSQL.append("T1.BINKBN=T2.BINKBN AND ");
    sbSQL.append("T1.NNDT=T2.NNDT AND ");
    sbSQL.append("T1.SHNKBN=T2.SHNKBN AND ");
    sbSQL.append("T1.BDENKBN=T2.BDENKBN AND ");
    sbSQL.append("T1.TEIKANKBN=T2.TEIKANKBN AND ");
    sbSQL.append("T1.IRISU=T2.IRISU AND ");
    sbSQL.append("T1.GENKAAM=T2.GENKAAM AND ");
    sbSQL.append("T1.BAIKAAM=T2.BAIKAAM ");

    return iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
  }

  /**
   * 店舗アンケート付き送り付けINSERT/UPDATE処理
   *
   * @param data
   * @param dataArrayG
   * @param userInfo
   */
  public String createSqlQju(JSONObject data, JSONArray dataArrayG, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    JSONObject getData = new JSONObject();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    String sqlFrom = "";
    Object[] valueData = new Object[] {};
    String values = "";
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    ArrayList<String> prmData = new ArrayList<String>();
    HashMap<String, String> tenSuryoMap = new HashMap<String, String>();

    // 入力値格納用変数
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String tenHtSu_Arr = "";
    String moyCd_Arr = "";
    String kanriNo_Arr = "";
    String dblCnt_Arr = "";
    int lastTenCd = 1;

    new HashMap<String, String>();
    new HashMap<String, String>();
    new HashMap<String, String>();

    if (data.optString("F1").length() >= 8) {
      moyskbn = data.optString("F1").substring(0, 1);
      moysstdt = data.optString("F1").substring(1, 7);
      moysrban = data.optString("F1").substring(7);
    }

    // 管理番号の取得
    sbSQL = new StringBuffer();
    iL = new ItemList();
    dbDatas = new JSONArray();
    sqlWhere = "";
    paramData = new ArrayList<String>();
    getData = new JSONObject();

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "MOYSKBN=null AND ";
    } else {
      sqlWhere += "MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "MOYSSTDT=null AND ";
    } else {
      sqlWhere += "MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "MOYSRBAN=null ";
    } else {
      sqlWhere += "MOYSRBAN=? ";
      paramData.add(moysrban);
    }

    // 一覧表情報
    sbSQL.append("SELECT ");
    sbSQL.append("MAX(SUMI_KANRINO) AS SUMI_KANRINO ");
    sbSQL.append("FROM INATK.SYSMOYCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() > 0) {
      getData = dbDatas.getJSONObject(0);
      if (!StringUtils.isEmpty(getData.optString("SUMI_KANRINO"))) {
        kanrino = String.valueOf(getData.optInt("SUMI_KANRINO") + 1);
      } else {
        kanrino = "1";
      }
    } else {
      kanrino = "1";
    }

    if (!tenht.equals("3")) {

      int tenHtsu = data.optInt("F15");
      Iterator<Integer> ten = tencds.iterator();

      for (int i = 0; i < tencds.size(); i++) {

        int tencd = ten.next();

        // ランク別発注の場合店ランクを取得
        if (tenht.equals("2")) {

          // 変数を初期化
          sbSQL = new StringBuffer();
          iL = new ItemList();
          dbDatas = new JSONArray();
          sqlWhere = "";
          paramData = new ArrayList<String>();

          sqlFrom = "INATK.TOKSRYRANK ";

          // 商品コードより部門コードを取得
          if (StringUtils.isEmpty(data.optString("F2"))) {
            sqlWhere += "BMNCD=null AND ";
          } else {
            if (data.optString("F2").length() >= 2) {
              sqlWhere += "BMNCD=? AND ";
              paramData.add(data.optString("F2").substring(0, 2));
            } else {
              sqlWhere += "BMNCD=null AND ";
            }
          }

          if (StringUtils.isEmpty(data.optString("F17")) && data.optInt("F17") >= 900) {

            // 催し区分
            if (StringUtils.isEmpty(moyskbn)) {
              sqlWhere += "MOYSKBN=null AND ";
            } else {
              sqlWhere += "MOYSKBN=? AND ";
              paramData.add(moyskbn);
            }

            // 催し開始日
            if (StringUtils.isEmpty(moysstdt)) {
              sqlWhere += "MOYSSTDT=null AND ";
            } else {
              sqlWhere += "MOYSSTDT=? AND ";
              paramData.add(moysstdt);
            }

            // 催し連番
            if (StringUtils.isEmpty(moysrban)) {
              sqlWhere += "MOYSRBAN=null AND ";
            } else {
              sqlWhere += "MOYSRBAN=? AND ";
              paramData.add(moysrban);
            }

            sqlFrom = "INATK.TOKSRYRANKEX ";
          }

          // ランクNo.
          sqlWhere += "SRYPTNNO=? AND ";
          paramData.add(data.optString("F17"));

          sqlWhere += "TENRANK=? ";
          paramData.add(tenRank.get(String.valueOf(tencd)));

          // 一覧表情報
          sbSQL = new StringBuffer();
          sbSQL.append("SELECT ");
          sbSQL.append("SURYO ");
          sbSQL.append("FROM ");
          sbSQL.append(sqlFrom);
          sbSQL.append("WHERE ");
          sbSQL.append(sqlWhere);

          dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

          if (dbDatas.size() != 0) {
            tenHtsu = dbDatas.getJSONObject(0).optInt("SURYO");
          }
        }

        if ((tencd - lastTenCd) != 0) {
          tenHtSu_Arr += String.format("%" + (tencd - lastTenCd) * 5 + "s", "");
        }

        String suryo = String.valueOf(tenHtsu);

        if (!StringUtils.isEmpty(suryo) && Integer.valueOf(suryo) != 0) {
          tenHtSu_Arr += String.format("%05d", Integer.valueOf(suryo));
        } else {
          tenHtSu_Arr += String.format("%05d", 0);
          suryo = "0";
        }

        if (tenSuryoMap.containsKey(tencd)) {
          tenSuryoMap.replace(String.valueOf(tencd), suryo);
        } else {
          tenSuryoMap.put(String.valueOf(tencd), suryo);
        }

        lastTenCd = (tencd + 1);
      }
    } else {

      for (int i = 0; i < dataArrayG.size(); i++) {
        JSONObject dataG = dataArrayG.getJSONObject(i);

        int tencd = dataG.optInt("F1");

        if ((tencd - lastTenCd) != 0) {
          tenHtSu_Arr += String.format("%" + (tencd - lastTenCd) * 5 + "s", "");
        }

        String suryo = "";
        if (dataG.size() > 1) {
          suryo = dataG.getString("F2");
        }

        if (!StringUtils.isEmpty(suryo)) {
          tenHtSu_Arr += String.format("%05d", Integer.valueOf(suryo));
        } else {
          tenHtSu_Arr += String.format("%5s", "");
          suryo = "";
        }

        if (tenSuryoMap.containsKey(tencd)) {
          tenSuryoMap.replace(String.valueOf(tencd), suryo);
        } else {
          tenSuryoMap.put(String.valueOf(tencd), suryo);
        }

        lastTenCd = (tencd + 1);
      }
    }

    tenHtSu_Arr = new ReportJU012Dao(JNDIname).spaceArr(tenHtSu_Arr, 5);

    int maxField = 14; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // 催しコードを追加
        values += " ?, ?, ?, ?";
        prmData.add(moyskbn);
        prmData.add(moysstdt);
        prmData.add(moysrban);
        prmData.add(kanrino);
      }

      if (!ArrayUtils.contains(new String[] {"F1", "F11"}, key)) {
        String val = data.optString(key);
        if (key.equals("F14")) {
          values += ", ?";
          prmData.add(tenht);

          if (tenht.equals("1")) {
            values += ", ?, ?, null";
            prmData.add(data.optString("F14"));
            prmData.add(data.optString("F15"));
          } else if (tenht.equals("2")) {
            values += ", ?, null, ?";
            prmData.add(data.optString("F16"));
            prmData.add(data.optString("F17"));
          } else {
            values += ", null, null, null";
          }

          values += ", ?";
          prmData.add(tenHtSu_Arr);
        } else if (key.equals("F6") && StringUtils.isEmpty(val)) {
          values += ", ?";
          prmData.add("0");
        } else if (StringUtils.isEmpty(val)) {
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

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKQJU_SHN ( ");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",KANRINO"); // 管理番号
    sbSQL.append(",SHNCD"); // 商品コード
    sbSQL.append(",SHNKBN"); // 商品区分
    sbSQL.append(",TSEIKBN"); // 訂正区分
    sbSQL.append(",JUKBN"); // 事前区分
    sbSQL.append(",BDENKBN"); // 別伝区分
    sbSQL.append(",WAPPNKBN"); // ワッペン区分
    sbSQL.append(",IRISU"); // 入数
    sbSQL.append(",GENKAAM"); // 原価
    sbSQL.append(",BAIKAAM"); // 売価
    sbSQL.append(",HTDT"); // 発注日
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",JUTENKAIKBN"); // 展開方法
    sbSQL.append(",RANKNO_ADD"); // 対象ランク
    sbSQL.append(",HTSU"); // 発注数
    sbSQL.append(",SURYOPTN"); // 数量パターン
    sbSQL.append(",TENHTSU_ARR"); // 店発注数配列
    sbSQL.append(",UPDKBN"); // 更新区分：
    sbSQL.append(",SENDFLG"); // 送信区分：
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES ( " + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分：
    sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " "); // 送信区分：
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("店舗アンケート付き送り付け_商品");

    // 事前打出し_商品納入日用配列作成
    String moyscd = moyskbn + moysstdt + String.format("%3s", Integer.valueOf(moysrban));

    HashMap<String, String> moyCdMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F2"), data.optString("F13"), "1");
    HashMap<String, String> kanriNoMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F2"), data.optString("F13"), "2");
    HashMap<String, String> cblCntMap = new ReportJU012Dao(JNDIname).getArrMap(data.optString("F2"), data.optString("F13"), "3");

    for (HashMap.Entry<String, String> getKeyVal : tenSuryoMap.entrySet()) {
      String key = getKeyVal.getKey();
      String val = getKeyVal.getValue();

      String getMoysCd = "";
      String getKanriNo = "";
      int flg = 0;

      // 数量ゼロは対象外
      if (StringUtils.isEmpty(val)) {
        if (moyCdMap.containsKey(key)) {
          getMoysCd = moyCdMap.get(key);
        }

        if (kanriNoMap.containsKey(key)) {
          getKanriNo = kanriNoMap.get(key);
        }

        // 同一のものだった場合
        if (moyscd.equals(getMoysCd) && String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {

          if (moyCdMap.containsKey(key)) {
            moyCdMap.remove(key);
          }

          if (kanriNoMap.containsKey(key)) {
            kanriNoMap.remove(key);
          }

          if (cblCntMap.containsKey(key)) {

            flg = Integer.valueOf(cblCntMap.get(key));
            if (flg > 0) {
              cblCntMap.replace(key, String.valueOf(flg - 1));
            } else {
              cblCntMap.remove(key);
            }
          }
        }
      } else {
        if (moyCdMap.containsKey(key)) {
          getMoysCd = moyCdMap.get(key);
          moyCdMap.replace(key, moyscd);
        } else {
          moyCdMap.put(key, moyscd);
        }

        if (kanriNoMap.containsKey(key)) {
          getKanriNo = kanriNoMap.get(key);
          kanriNoMap.replace(key, String.valueOf(Integer.valueOf(kanrino)));
        } else {
          kanriNoMap.put(key, String.valueOf(Integer.valueOf(kanrino)));
        }

        // 同一のものじゃなかった場合
        if (!moyscd.equals(getMoysCd) || !String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {
          if (cblCntMap.containsKey(key)) {
            flg = Integer.valueOf(cblCntMap.get(key)) + 1;
            cblCntMap.replace(key, String.valueOf(flg));
          } else {
            cblCntMap.put(key, "1");
          }
        }
      }
    }

    moyCd_Arr = new ReportJU012Dao(JNDIname).createArr(moyCdMap, "1");
    kanriNo_Arr = new ReportJU012Dao(JNDIname).createArr(kanriNoMap, "2");
    dblCnt_Arr = new ReportJU012Dao(JNDIname).createArr(cblCntMap, "3");

    // 事前打出し_商品納入日更新
    values = " ";
    prmData = new ArrayList<String>();
    valueData = new Object[] {};

    // 商品コード
    if (StringUtils.isEmpty(data.optString("F2"))) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(data.optString("F2"));
    }

    // 納入日
    if (StringUtils.isEmpty(data.optString("F13"))) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(data.optString("F13"));
    }

    // 催し配列
    if (StringUtils.isEmpty(moyCd_Arr)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(moyCd_Arr);
    }

    // 管理番号配列
    if (StringUtils.isEmpty(kanriNo_Arr)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(kanriNo_Arr);
    }

    // 重複件数配列
    if (StringUtils.isEmpty(dblCnt_Arr)) {
      values += "null ";
    } else {
      values += "? ";
      prmData.add(dblCnt_Arr);
    }

    valueData = ArrayUtils.add(valueData, values);

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.TOKJU_SHNNNDT ( ");
    sbSQL.append(" SHNCD"); // 商品コード
    sbSQL.append(",NNDT"); // 納入日
    sbSQL.append(",MOYCD_ARR"); // 催し配列
    sbSQL.append(",KANRINO_ARR"); // 管理番号配列
    sbSQL.append(",DBLCNT_ARR"); // 重複件数配列
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES ( " + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("事前打出し_商品納入日");

    // 催しコード内部管理更新
    values = " ";
    prmData = new ArrayList<String>();
    valueData = new Object[] {};

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      values += "null,";
    } else {
      values += "?,";
      prmData.add(moysrban);
    }

    // 管理番号
    if (StringUtils.isEmpty(kanrino)) {
      values += "null";
    } else {
      values += "?";
      prmData.add(kanrino);
    }

    valueData = ArrayUtils.add(valueData, values);

    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INATK.SYSMOYCD ( ");
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",SUMI_KANRINO"); // 管理番号
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(") VALUES ( " + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催しコード内部管理更新");

    return sbSQL.toString();
  }
}
