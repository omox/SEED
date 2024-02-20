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
public class Reportx161Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx161Dao(String JNDIname) {
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

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szSircd = getMap().get("SIRCD"); // 選択メーカーコード
    getMap().get("SENDBTNID");

    // パラメータ確認
    // 必須チェック
    if (szSircd == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();


    StringBuffer sbSQL = new StringBuffer();

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
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
    new ArrayList<String>();

    // 共通箇所設定
    createCmnOutput(jad);

  }

  boolean isTest = false;

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * 複数仕入先マスタ_店舗INSERT/UPDATE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  @SuppressWarnings("static-access")
  public String createSqlFsirT(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String updateRows = ""; // 更新データ

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String sircd = "";
    String tencd = "";
    String adddt = ""; // 登録日(削除時仕様)

    StringBuffer sbSQL;
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      String values = "";

      if (isTest) {

        // 登録設定
        /*
         * 新規登録の場合 ：timestamp 変更登録の場合 ：ADDDT(検索値) PK重複の論理削除されたデータがある場合 ：timestamp
         */
        sircd = data.optString("F1");
        tencd = data.optString("F2");
        sbSQL = new StringBuffer();
        sbSQL.append("select");
        sbSQL.append(" case");
        sbSQL.append(" when UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + " then 'current timestamp'");
        sbSQL.append(" when UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " then DATE_FORMAT(DATE_FORMAT(ADDDT, '%Y%m%d'),%y-%m-%d) end ");
        sbSQL.append(" from INAMS.MSTFUKUSUSIR_T");
        sbSQL.append(" where SIRCD = " + sircd + " and TENCD = " + tencd);
        dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
        if (dbDatas.size() > 0) {
          adddt = dbDatas.getJSONObject(0).getString("1") + ",";
        } else {
          adddt = "current timestamp,";
        }

        values += StringUtils.defaultIfEmpty("'" + data.optString("F1") + "'", "null") + ",";
        values += StringUtils.defaultIfEmpty("'" + data.optString("F2") + "'", "null") + ",";

        values += StringUtils.defaultIfEmpty("'" + data.optString("F3") + "'", "null").replace("'0'", "null") + ",";

        values += DefineReport.ValUpdkbn.NML.getVal() + ",";
        values += "null,";
        values += "'" + userId + "',";
        values += adddt;
        values += "current timestamp";

      } else {
        for (int j = 0; j < data.size(); j++) {
          values += ", ?";
        }
        values = StringUtils.removeStart(values, ",");

        for (String prm : values.split(",", 0)) {
          prmData.add(prm);
        }
      }
      updateRows += ",(" + values + ")";
    }
    updateRows = StringUtils.removeStart(updateRows, ",");

    sbSQL = new StringBuffer();
    sbSQL.append(" REPLACE INTO INAMS.MSTFUKUSUSIR_T (");
    sbSQL.append("  cast(T1.SIRCD as INTEGER) as SIRCD"); // F1 : 仕入先コード
    sbSQL.append(" ,cast(T1.TENCD as SMALLINT) as TENCD"); // F2 : 店コード
    sbSQL.append(" ,cast(T1.SEQNO as SMALLINT) as SEQNO"); // F3 : 入力順番
    sbSQL.append(" ,cast(T1.UPDKBN as SMALLINT) as UPDKBN"); // F4 : 更新区分
    sbSQL.append(" ,cast(T1.SENDFLG as SMALLINT) as SENDFLG"); // F5 : 送信フラグ
    sbSQL.append(" ,cast(T1.OPERATOR as VARCHAR(20)) as OPERATOR"); // F6 : オペレータ
    sbSQL.append(" ,cast(T1.ADDDT as DATE) as ADDDT"); // F7 : 登録日
    sbSQL.append(" ,cast(T1.UPDDT as DATE) as UPDDT"); // F8 : 更新日
    sbSQL.append(" from (values " + updateRows + ") as T1(");
    sbSQL.append("  SIRCD");
    sbSQL.append(" ,TENCD");
    sbSQL.append(" ,SEQNO");
    sbSQL.append(" ,UPDKBN");
    sbSQL.append(" ,SENDFLG");
    sbSQL.append(" ,OPERATOR");
    sbSQL.append(" ,ADDDT");
    sbSQL.append(" ,UPDDT");
    sbSQL.append(" ))as RE on (T.SIRCD = RE.SIRCD and T.TENCD = RE.TENCD)");
    sbSQL.append(" when matched then update set");
    sbSQL.append("  SIRCD=RE.SIRCD");
    sbSQL.append(" ,TENCD=RE.TENCD");
    sbSQL.append(" ,SEQNO=RE.SEQNO");
    sbSQL.append(" ,UPDKBN=RE.UPDKBN");
    sbSQL.append(" ,SENDFLG=RE.SENDFLG");
    sbSQL.append(" ,OPERATOR=RE.OPERATOR");
    sbSQL.append(" ,ADDDT=RE.ADDDT");
    sbSQL.append(" ,UPDDT=RE.UPDDT");
    sbSQL.append(" when not matched then insert values (");
    sbSQL.append("  RE.SIRCD");
    sbSQL.append(" ,RE.TENCD");
    sbSQL.append(" ,RE.SEQNO");
    sbSQL.append(" ,RE.UPDKBN");
    sbSQL.append(" ,RE.SENDFLG");
    sbSQL.append(" ,RE.OPERATOR");
    sbSQL.append(" ,RE.ADDDT");
    sbSQL.append(" ,RE.UPDDT");
    sbSQL.append(")");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("複数仕入先マスタ_店舗");

    return sbSQL.toString();
  }

  /**
   * 複数仕入先_実仕入先マスタINSERT/UPDATE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  @SuppressWarnings("static-access")
  public String createSqlFsirR(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String updateRows = ""; // 更新データ

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String adddt = ""; // 登録日
    String rsircd = ""; // 仕入先コード
    String seqno = ""; // 入力順番

    StringBuffer sbSQL;
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      String values = "";

      if (isTest) {

        // 登録設定
        /*
         * 新規登録の場合 ：timestamp 変更登録の場合 ：ADDDT(検索値) PK重複の論理削除されたデータがある場合 ：timestamp
         */
        rsircd = data.optString("F1");
        seqno = data.optString("F2");
        sbSQL = new StringBuffer();
        sbSQL.append("select");
        sbSQL.append(" case");
        sbSQL.append(" when UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + " then 'current timestamp'");
        sbSQL.append(" when UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " then DATE_FORMAT(DATE_FORMAT(ADDDT, '%Y%m%d'),%y-%m-%d) end");
        sbSQL.append(" from INAMS.MSTFUKUSUSIR_R");
        sbSQL.append(" where RSIRCD = " + rsircd + " and SEQNO = " + seqno);
        dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
        if (dbDatas.size() > 0) {
          adddt = dbDatas.getJSONObject(0).getString("1") + ",";
        } else {
          adddt = "current timestamp,";
        }

        values += StringUtils.defaultIfEmpty(data.optString("F1"), "null") + ",";
        values += StringUtils.defaultIfEmpty("'" + data.optString("F2") + "'", "null") + ",";
        values += StringUtils.defaultIfEmpty("'" + data.optString("F3") + "'", "null") + ",";
        values += DefineReport.ValUpdkbn.NML.getVal() + ",";
        values += "null,";
        values += "'" + userId + "',";
        values += adddt;
        values += "current timestamp";

      } else {
        for (int j = 0; j < data.size(); j++) {
          values += ", ?";
        }
        values = StringUtils.removeStart(values, ",");

        for (String prm : values.split(",", 0)) {
          prmData.add(prm);
        }
      }
      updateRows += ",(" + values + ")";
    }
    updateRows = StringUtils.removeStart(updateRows, ",");

    sbSQL = new StringBuffer();
    // sbSQL.append(" merge into INAMS.MSTFUKUSUSIR_R as T using (select");
    sbSQL.append(" REPLACE INTO INAMS.MSTFUKUSUSIR_R ( ");
    sbSQL.append("  cast(T1.SIRCD as INTEGER) as SIRCD"); // F1 : 仕入先コード
    sbSQL.append(" ,cast(T1.SEQNO as SMALLINT) as SEQNO"); // F2 : 入力順番
    sbSQL.append(" ,cast(T1.RSIRCD as INTEGER) as RSIRCD"); // F3 : 実仕入先コード
    sbSQL.append(" ,cast(T1.UPDKBN as SMALLINT) as UPDKBN"); // F4 : 更新区分
    sbSQL.append(" ,cast(T1.SENDFLG as SMALLINT) as SENDFLG"); // F5 : 送信フラグ
    sbSQL.append(" ,cast(T1.OPERATOR as VARCHAR(20)) as OPERATOR"); // F6 : オペレータ
    sbSQL.append(" ,cast(T1.ADDDT as DATE) as ADDDT"); // F7 : 登録日
    sbSQL.append(" ,cast(T1.UPDDT as DATE) as UPDDT"); // F8 : 更新日
    sbSQL.append(" from (values " + updateRows + ") as T1(");
    sbSQL.append("  SIRCD");
    sbSQL.append(" ,SEQNO");
    sbSQL.append(" ,RSIRCD");
    sbSQL.append(" ,UPDKBN");
    sbSQL.append(" ,SENDFLG");
    sbSQL.append(" ,OPERATOR");
    sbSQL.append(" ,ADDDT");
    sbSQL.append(" ,UPDDT");
    sbSQL.append(" ))as RE on (T.SIRCD = RE.SIRCD and T.SEQNO = RE.SEQNO)");
    sbSQL.append(" when matched then update set");
    sbSQL.append("  SIRCD=RE.SIRCD");
    sbSQL.append(" ,SEQNO=RE.SEQNO");
    sbSQL.append(" ,RSIRCD=RE.RSIRCD");
    sbSQL.append(" ,UPDKBN=RE.UPDKBN");
    sbSQL.append(" ,SENDFLG=RE.SENDFLG");
    sbSQL.append(" ,OPERATOR=RE.OPERATOR");
    sbSQL.append(" ,ADDDT=RE.ADDDT");
    sbSQL.append(" ,UPDDT=RE.UPDDT");
    sbSQL.append(" when not matched then insert values (");
    sbSQL.append("  RE.SIRCD");
    sbSQL.append(" ,RE.SEQNO");
    sbSQL.append(" ,RE.RSIRCD");
    sbSQL.append(" ,RE.UPDKBN");
    sbSQL.append(" ,RE.SENDFLG");
    sbSQL.append(" ,RE.OPERATOR");
    sbSQL.append(" ,RE.ADDDT");
    sbSQL.append(" ,RE.UPDDT");
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("実仕入先マスタ");

    return sbSQL.toString();
  }

  /**
   * 複数仕入先マスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlFUKUSUSIR(HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayFUKUSUSIR_R = JSONArray.fromObject(map.get("DATA_ZITSIR")); // 更新情報(複数仕入先_実仕入先マスタ)
    JSONArray dataArrayFUKUSUSIR_T = JSONArray.fromObject(map.get("DATA_FSIRT")); // 更新情報(複数仕入先_店舗)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 3; // Fxxの最大値
    int len = dataArrayFUKUSUSIR_R.size();

    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayFUKUSUSIR_R.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (k == 1) {
            values += String.valueOf(0 + 1);

          }

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += ", null";
            } else {
              values += ", ?";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 複数仕入先_実仕入先マスタの登録・更新
        sbSQL = new StringBuffer();

        sbSQL.append(" REPLACE INTO INAMS.MSTFUKUSUSIR_R ( ");
        sbSQL.append(" SIRCD"); // 仕入先コード
        sbSQL.append(", SEQNO"); // 入力順番
        sbSQL.append(", RSIRCD"); // 実仕入先コード
        sbSQL.append(", UPDKBN"); // 更新区分
        sbSQL.append(", SENDFLG"); // 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター
        // sbSQL.append(", current timestamp AS ADDDT "); // 登録日
        // sbSQL.append(", current timestamp AS UPDDT "); // 更新日
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(",ADDDT ");// 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(",UPDDT ");
        sbSQL.append(")");
        sbSQL.append("VALUES (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal()); // 更新区分
        sbSQL.append(", 0"); // 送信フラグ
        sbSQL.append(", '" + userId + "' "); // オペレーター
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + " */ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("複数仕入先_実仕入先マスタ");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    maxField = 3; // Fxxの最大値
    len = dataArrayFUKUSUSIR_T.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayFUKUSUSIR_T.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (k == 1) {
            values += String.valueOf(0 + 1);

          }

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += ", null";
            } else {
              values += ", ?";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 複数仕入先_店舗の登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" REPLACE INTO INAMS.MSTFUKUSUSIR_T ( ");
        sbSQL.append(" SIRCD"); // 仕入先コード
        sbSQL.append(", TENCD"); // 店コード
        sbSQL.append(", SEQNO"); // 入力順番
        sbSQL.append(", UPDKBN"); // 更新区分
        sbSQL.append(", SENDFLG"); // 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター
        // sbSQL.append(", current timestamp AS ADDDT "); // 登録日
        // sbSQL.append(", current timestamp AS UPDDT "); // 更新日
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(",ADDDT ");// 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(",UPDDT ");
        sbSQL.append(")");
        sbSQL.append("VALUES (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal()); // 更新区分
        sbSQL.append(", 0"); // 送信フラグ
        sbSQL.append(", '" + userId + "' "); // オペレーター
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + " */ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("複数仕入先_店舗");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }
    return sbSQL.toString();
  }

  /**
   * 複数仕入先マスタDELETE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  public String createDeleteSql(HashMap<String, String> map, User userInfo) {
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    userInfo.getId();

    String sircd = map.get("SIRCD"); // 仕入先コード

    StringBuffer sbSQL;

    // 複数仕入先マスタ_店舗
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    sbSQL.append("delete from INAMS.MSTFUKUSUSIR_T where SIRCD = ?");
    prmData.add(sircd);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + " */ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("複数仕入先マスタ_店舗");

    // 実仕入先マスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    sbSQL.append("delete from INAMS.MSTFUKUSUSIR_R where SIRCD = ?");
    prmData.add(sircd);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + " */ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("実仕入先マスタ");

    return sbSQL.toString();
  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {

    JSONArray.fromObject(map.get("DATA_FSIRT"));
    JSONArray.fromObject(map.get("DATA_ZITSIR"));
    JSONObject option = new JSONObject();

    userInfo.getId();
    map.get("SENDBTNID");
    map.get("YOBINFO");
    String sircd = map.get("SIRCD"); // 仕入先コード

    // パラメータ確認
    // 必須チェック
    if (sircd == null) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 複数仕入先Delete処理
    this.createDeleteSql(map, userInfo);

    // 複数仕入先INSERT/UPDATE処理
    this.createSqlFUKUSUSIR(map, userInfo);

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
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {

    JSONObject msgObj = new JSONObject();
    new JSONArray();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String szSircd = map.get("SIRCD");

    if (szSircd.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    ArrayList<String> prmData;


    // 複数仕入先_実仕入先マスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    sbSQL.append(" UPDATE INAMS.MSTFUKUSUSIR_R SET ");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=current timestamp ");
    sbSQL.append(" WHERE SIRCD = ? ");

    prmData.add(szSircd);
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("複数仕入先_実仕入先マスタ");

    // 複数仕入先_店舗
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    sbSQL.append(" UPDATE INAMS.MSTFUKUSUSIR_T SET ");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=current timestamp ");
    sbSQL.append(" WHERE SIRCD = ? ");

    prmData.add(szSircd);
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("複数仕入先_店舗");


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
        msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
    }
    return msgObj;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray dataArrayFST = JSONArray.fromObject(map.get("DATA_FSIRT")); // 対象情報
    JSONArray dataArrayFSR = JSONArray.fromObject(map.get("DATA_ZITSIR")); // 対象情報
    map.get(DefineReport.ID_PARAM_OBJ);

    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sircd = map.get("SIRCD"); // 仕入先コード
    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<String>();



    // 入力値を取得
    JSONObject data = dataArray.getJSONObject(0);
    // 関連情報取得
    ItemList iL = new ItemList();

    // チェック処理
    // 対象件数チェック
    if (sircd == null) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }
    if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
      // 複数仕入先_実仕入先マスタ
      for (int i = 0; i < dataArrayFSR.size(); i++) {
        data = dataArrayFSR.getJSONObject(i);
        if (data.isEmpty()) {
          continue;
        }

        paramData = new ArrayList<String>();
        paramData.add(data.getString("F1"));
        paramData.add(data.getString("F2"));
        sqlcommand = "select count(*) as VALUE from INAMS.MSTFUKUSUSIR_R where COALESCE(UPDKBN, 0) <> 1 and SIRCD = ? and SEQNO = ?";

        @SuppressWarnings("static-access")
        JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
        if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
          JSONObject o = mu.getDbMessageObj("E00004", "仕入先コード");
          msg.add(o);
          return msg;
        }
      }

      // 複数仕入先_店舗
      for (int i = 0; i < dataArrayFST.size(); i++) {
        data = dataArrayFST.getJSONObject(i);
        if (data.isEmpty()) {
          continue;
        }

        paramData = new ArrayList<String>();
        paramData.add(data.getString("F1"));
        paramData.add(data.getString("F2"));
        sqlcommand = "select count(*) as VALUE from INAMS.MSTFUKUSUSIR_T where COALESCE(UPDKBN, 0) <> 1 and SIRCD = ? and TENCD = ?";

        @SuppressWarnings("static-access")
        JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
        if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
          JSONObject o = mu.getDbMessageObj("E00004", "仕入先コード");
          msg.add(o);
          return msg;
        }
      }
    }
    return msg;
  }
}
