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
public class Reportx192Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx192Dao(String JNDIname) {
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
    JSONArray msg = this.checkDel(map);

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

    String szTengpcd = getMap().get("TENGPCD"); // 店グループ
    String szBumon = getMap().get("BUMON"); // 部門
    String szGpkbn = getMap().get("GPKBN"); // グループ区分
    String szAreakbn = getMap().get("AREAKBN"); // エリア区分
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (szTengpcd == null
    // || szBumon == null
    // || szGpkbn == null
    // || szAreakbn == null
    ) {
      System.out.println(super.getConditionLog());
      return "";
    }
    ArrayList<String> paramData = new ArrayList<String>();
    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();


    if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid)) {
      sbSQL.append("select");
      sbSQL.append(" STGP.GPKBN"); // F1 ：グループ区分
      sbSQL.append(", right('0'||STGP.BMNCD,2)"); // F2 ：部門
      // sbSQL.append(", BMNCD"); // F2 ：部門
      sbSQL.append(", STGP.TENGPCD"); // F3 ：店グループ
      sbSQL.append(", STGP.TENGPAN"); // F4 ：店舗グループ名（漢字）
      sbSQL.append(", STGP.TENGPKN"); // F5 ：店舗グループ名（カナ）
      sbSQL.append(", STGP.PCARD_OPFLG"); // F6 ：プライスカード出力有無
      sbSQL.append(", STGP.AREAKBN"); // F7 ：エリア区分
      sbSQL.append(", STGP.OPERATOR"); // F8 ：オペレータ
      sbSQL.append(", DATE_FORMAT(STGP.ADDDT, '%y/%m/%d')"); // F9 ：登録日
      sbSQL.append(", DATE_FORMAT(STGP.UPDDT, '%y/%m/%d')"); // F10 ：更新日
      sbSQL.append(", case"); // F11 ：アイテム数
      sbSQL.append(" when STGP.GPKBN = 2 then COALESCE(BKCTL.ITMES, 0)");
      sbSQL.append(" else null end as ITEMS");
      sbSQL.append(" from INAMS.MSTSHNTENGP STGP");
      sbSQL.append(" left join (select TENGPCD, COUNT(SHNCD) as ITMES from INAMS.MSTBAIKACTL group by TENGPCD) BKCTL on STGP.TENGPCD = BKCTL.TENGPCD");
      sbSQL.append(" where STGP.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());
      sbSQL.append(" and STGP.GPKBN = ? ");
      paramData.add(szGpkbn);
      sbSQL.append(" and STGP.BMNCD = ? ");
      paramData.add(szBumon);
      sbSQL.append(" and STGP.AREAKBN = ? ");
      paramData.add(szAreakbn);
      sbSQL.append(" and STGP.TENGPCD = ? ");
      paramData.add(szTengpcd);
      setParamData(paramData);

    }

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

  boolean isTest = true;

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * 商品店グループINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlSHNTENGP(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_TENPO")); // 更新情報(商品店グループ店舗)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 7; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);

        if (StringUtils.isEmpty(val)) {
          values += " null,";
        } else {
          values += " ?,";
          prmData.add(val);
        }
      }

      if (k == maxField) {

        values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", 0";
        values += ", '" + userId + "'";
        values += ", CURRENT_TIMESTAMP";
        values += ", CURRENT_TIMESTAMP";

        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }

      // 商品店グループの登録・更新
      sbSQL = new StringBuffer();
      sbSQL.append(" INSERT INTO INAMS.MSTSHNTENGP  (");
      sbSQL.append("GPKBN"); // グループ区分
      sbSQL.append(", BMNCD"); // 部門
      sbSQL.append(", AREAKBN"); // エリア区分
      sbSQL.append(", TENGPCD"); // 店グループ（エリア）
      sbSQL.append(", TENGPAN"); // 店舗グループ名（カナ）
      sbSQL.append(", TENGPKN"); // 店舗グループ名（漢字）
      sbSQL.append(", PCARD_OPFLG"); // プライスカード出力有無
      sbSQL.append(", UPDKBN"); // 更新区分：
      sbSQL.append(", SENDFLG"); // 送信フラグ
      sbSQL.append(", OPERATOR "); // オペレーター：
      sbSQL.append(", ADDDT");// 登録日:
      sbSQL.append(", UPDDT "); // 更新日：
      sbSQL.append(") ");
      sbSQL.append("VALUES ");
      sbSQL.append(StringUtils.removeStart(StringUtils.join(valueData, ","), ",")  + "AS NEW " );
      if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) ) {
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("GPKBN = NEW.GPKBN ");
        sbSQL.append(", BMNCD = NEW.BMNCD ");
        sbSQL.append(", AREAKBN = NEW.AREAKBN ");
        sbSQL.append(", TENGPCD = NEW.TENGPCD ");
        sbSQL.append(", TENGPAN = NEW.TENGPAN ");
        sbSQL.append(", TENGPKN = NEW.TENGPKN ");
        sbSQL.append(", PCARD_OPFLG = NEW.PCARD_OPFLG ");
        sbSQL.append(", UPDKBN = NEW.UPDKBN ");
        sbSQL.append(", SENDFLG = NEW.SENDFLG ");
        sbSQL.append(", OPERATOR = NEW.OPERATOR ");
        sbSQL.append(", UPDDT = NEW.UPDDT ");
      }

      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());
      }

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("商品店グループマスタ");

      // クリア
      prmData = new ArrayList<String>();
      valueData = new Object[] {};
      values = "";

    // 商品店テーブル店舗マスタの削除処理
    sbSQL = new StringBuffer();
    prmData.add(data.optString("F1"));
    prmData.add(data.optString("F2"));
    prmData.add(data.optString("F4"));
    sbSQL.append(" delete from INAMS.MSTSHNTENGPTEN where GPKBN = ? and BMNCD = ? and TENGPCD = ?");

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("商品店グループ店舗マスタ");

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";


    maxField = 1; // Fxxの最大値
    int len = dataArrayT.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataCld = dataArrayT.getJSONObject(i);
      if (!dataCld.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {""}, key)) {
            String val = dataCld.optString(key);

            if (StringUtils.isEmpty(val)) {
              values += "(?,?,?,NULL";
            } else {
              prmData.add(dataCld.optString(key));
              prmData.add(dataCld.optString("F2"));
              prmData.add(dataCld.optString("F3"));
              prmData.add(dataCld.optString("F4"));
              values += "(?,?,?,?";
            }
            values += (", 0"); // 送信フラグ
            values += (", '" + userId + "' "); // オペレーター
            if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
              values += (", CURRENT_TIMESTAMP "); // 登録日 新規登録時には登録日の更新も行う。
            }
            values += (", CURRENT_TIMESTAMP "); // 更新日
            values += ")";
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, values);
            values = "";

          }
        }
      }


      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 商品店グループ店舗マスタの登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" REPLACE into INAMS.MSTSHNTENGPTEN (");
        sbSQL.append(" GPKBN"); // グループ区分
        sbSQL.append(", BMNCD"); // 部門
        sbSQL.append(", TENGPCD"); // 店グループ
        sbSQL.append(", TENCD"); // 店コード
        sbSQL.append(", SENDFLG"); // 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター：
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", ADDDT "); // 登録日：
        }
        sbSQL.append(", UPDDT "); // 更新日：
        sbSQL.append(") ");
        sbSQL.append("VALUES (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));


        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("商品店グループ店舗マスタ");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }

    }
    return sbSQL.toString();
  }

  /**
   * 商品店グループ店舗マスタDERETE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  public String createDeleteSql(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    userInfo.getId();

    String gpkbn = ""; // グループ区分
    String bmncd = ""; // 部門コード
    String tengpcd = ""; // 店グループ
    StringBuffer sbSQL;

    sbSQL = new StringBuffer();
    sbSQL.append("delete from INAMS.MSTSHNTENGPTEN where GPKBN = ? and BMNCD = ? and TENGPCD = ?");

    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      gpkbn = data.optString("F1");
      bmncd = data.optString("F2");
      tengpcd = data.optString("F3");
      // tencd = data.optString("F4");

      prmData = new ArrayList<String>();
      prmData.add(gpkbn);
      prmData.add(bmncd);
      prmData.add(tengpcd);
      // prmData.add(tencd);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("商品店グループ店舗マスタ");
    }
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

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONArray.fromObject(map.get("DATA_TENPO"));
    JSONArray.fromObject(map.get("DATA_TENPO_DEL"));
    JSONObject option = new JSONObject();

    userInfo.getId();

    // パラメータ確認
    // 必須チェック

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // SQL発行：商品店グループ
    if (dataArray.size() > 0) {
      this.createSqlSHNTENGP(data, map, userInfo);

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
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
    JSONArray.fromObject(map.get("DATA"));

    JSONObject msgObj = new JSONObject();
    new JSONArray();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    String szTengpcd = map.get("TENGPCD"); // 店グループ
    String szBumon = map.get("BUMON"); // 部門
    String szGpkbn = map.get("GPKBN"); // グループ区分
    String szAreakbn = map.get("AREAKBN"); // エリア区分

    if (szTengpcd.length() == 0 || szAreakbn.length() == 0 || StringUtils.equals(DefineReport.Values.NONE.getVal(), szBumon) || StringUtils.equals(DefineReport.Values.NONE.getVal(), szGpkbn)) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    new ItemList();
    new JSONArray();
    StringBuffer sbSQL;
    ArrayList<String> prmData;

    // 商品店グループマスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    // sbSQL.append("update INAMS.MSTSHNTENGP set UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal()+" where
    // GPKBN = ? and BMNCD = ? and AREAKBN = ? and TENGPCD = ? ");
    sbSQL.append(" UPDATE INAMS.MSTSHNTENGP SET ");
    sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", SENDFLG = 0");
    sbSQL.append(", OPERATOR='" + userId + "'");
    sbSQL.append(", UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append(" WHERE GPKBN = ?");
    sbSQL.append(" and BMNCD = ?");
    sbSQL.append(" and AREAKBN = ?");
    sbSQL.append(" and TENGPCD = ?");

    prmData.add(szGpkbn);
    prmData.add(szBumon);
    prmData.add(szAreakbn);
    prmData.add(szTengpcd);
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("商品店グループマスタ");

    // 商品店グループ店舗マスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    sbSQL.append("delete from INAMS.MSTSHNTENGPTEN where GPKBN = ? and BMNCD = ? and TENGPCD = ?");
    prmData.add(szGpkbn);
    prmData.add(szBumon);
    prmData.add(szTengpcd);
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("商品店グループ店舗マスタ");

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
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
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
    map.get(DefineReport.ID_PARAM_OBJ);

    JSONArray msg = new JSONArray();

    // 入力値を取得
    // JSONObject data = dataArray.getJSONObject(0);

    MessageUtility mu = new MessageUtility();
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // 入力値を取得
    JSONObject data = dataArray.getJSONObject(0);
    // 関連情報取得
    ItemList iL = new ItemList();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 新規チェック
    if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid) || StringUtils.equals(DefineReport.Button.SEL_COPY.getObj(), sendBtnid)) {

      paramData = new ArrayList<String>();
      paramData.add(data.getString("F1"));
      paramData.add(data.getString("F2"));
      paramData.add(data.getString("F3"));
      paramData.add(data.getString("F4"));
      sqlcommand = "select COUNT(*) as VALUE from INAMS.MSTSHNTENGP where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and GPKBN = ? and BMNCD = ? and AREAKBN = ? and TENGPCD = ?";

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        JSONObject o = mu.getDbMessageObj("E00004", new String[] {});
        msg.add(o);
        return msg;
      }

    }

    /*
     * if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
     * msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal())); return msg; }
     */


    // 入力制限：税区分が1、0の場合、税率区分の入力必須
    /*
     * if(StringUtils.equals(data.optString("F5"), "0") || StringUtils.equals(data.optString("F5"),
     * "1")){ if(StringUtils.equals(data.getString("F8"),DefineReport.Values.NONE.getVal())){
     * msg.add(MessageUtility.getMsg("税率区分の設定を行ってください。")); return msg; } }
     */
    // 各データ行チェック
    // if(!outobj.equals(DefineReport.Button.DELETE.getObj())) {
    // FieldType fieldType = FieldType.GRID;
    // for (int i = 0; i < dataArray.size(); i++) {
    // JSONObject data = dataArray.getJSONObject(i);
    // String[] addParam = {Integer.toString(i+1)};
    // // 比較対象店舗必須チェック
    // if(!InputChecker.isNotNull(data.optString("F2"))){
    // msg.add(MessageUtility.getCheckNullMessage(DefineReport.Select.M_TENPO.getTxt(), fieldType,
    // addParam));
    // }
    // }
    // }
    return msg;
  }

  /**
   * チェック処理(削除時)
   *
   * @throws Exception
   */
  @SuppressWarnings("static-access")
  public JSONArray checkDel(HashMap<String, String> map) {
    JSONArray.fromObject(map.get("DATA"));

    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();

    String szTengpcd = map.get("TENGPCD"); // 店グループ
    String szGpkbn = map.get("GPKBN"); // グループ区分
    String szAreakbn = map.get("AREAKBN"); // エリア区分
    String szBmncd = map.get("BUMON"); // 部門

    String szTableName1 = ""; // テーブル名称
    String szTableName2 = ""; // テーブル名称

    // 基本INSERT/UPDATE文
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    StringBuffer sbSQL;
    ArrayList<String> prmData = new ArrayList<String>();

    if (StringUtils.equals("1", szGpkbn)) {
      szTableName1 = "INAMS.MSTSIRGPSHN";
      szTableName2 = "INAMS.MSTSIRGPSHN_Y";

    } else if (StringUtils.equals("2", szGpkbn)) {
      szTableName1 = "INAMS.MSTBAIKACTL";
      szTableName2 = "INAMS.MSTBAIKACTL_Y";

    } else if (StringUtils.equals("3", szGpkbn)) {
      szTableName1 = "INAMS.MSTSHINAGP";
      szTableName2 = "INAMS.MSTSHINAGP_Y";
    }

    // 存在チェック:通常テーブル
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    // sbSQL.append("select T1.SHNCD from "+szTableName1+" T1 inner join INAMS.MSTSHN SHN on SHN.SHNCD =
    // T1.SHNCD and NVL(SHN.UPDKBN, 0) <> 1 where T1.TENGPCD = ? and T1.AREAKBN = ? and SHN.BMNCD = ?
    // fetch first 1 rows only");
    sbSQL.append("select T1.SHNCD from (select T1.SHNCD, SHN.BMNCD from " + szTableName1
        + " T1 inner join INAMS.MSTSHN SHN on SHN.SHNCD = T1.SHNCD and COALESCE(SHN.UPDKBN, 0) <> 1 where T1.TENGPCD = ? and T1.AREAKBN = ? and COALESCE(SHN.UPDKBN, 0) <> 1 ) T1 where BMNCD = ? limit 1 ");

    prmData.add(szTengpcd);
    prmData.add(szAreakbn);
    prmData.add(szBmncd);


    dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
    if (dbDatas.size() > 0) {
      msg.add(mu.getDbMessageObj("E00006", new String[] {}));
      return msg;
    }

    // 存在チェック:予約テーブル
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    // sbSQL.append("select T1.SHNCD from "+szTableName2+" T1 inner join INAMS.MSTSHN SHN on SHN.SHNCD =
    // T1.SHNCD and NVL(SHN.UPDKBN, 0) <> 1 where T1.TENGPCD = ? and T1.AREAKBN = ? and SHN.BMNCD = ?
    // fetch first 1 rows only");
    sbSQL.append("select T1.SHNCD from (select T1.SHNCD, SHN.BMNCD from " + szTableName2
        + " T1 inner join INAMS.MSTSHN SHN on SHN.SHNCD = T1.SHNCD and COALESCE(SHN.UPDKBN, 0) <> 1 where T1.TENGPCD = ? and T1.AREAKBN = ? and COALESCE(SHN.UPDKBN, 0) <> 1 ) T1 where BMNCD = ? limit 1 ");
    prmData.add(szTengpcd);
    prmData.add(szAreakbn);
    prmData.add(szBmncd);

    dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
    if (dbDatas.size() > 0) {
      msg.add(mu.getDbMessageObj("E00006", new String[] {}));
      return msg;
    }
    return msg;
  }
}
