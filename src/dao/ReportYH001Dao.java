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
public class ReportYH001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportYH001Dao(String JNDIname) {
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

    String szKkkcd = getMap().get("KKKCD"); // 店グループ
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (szKkkcd == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();

    if (!StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
      sbSQL.append("select");
      sbSQL.append(" KKK.KKKCD"); // F1 : 企画No
      sbSQL.append(", KKK.KKKKM"); // F2 : 企画名
      sbSQL.append(", right (KKK.NNSTDT, 6)"); // F3 : 納入開始日
      sbSQL.append(", right (KKK.NNEDDT, 6)"); // F4 : 納入終了日
      sbSQL.append(", KKK.OPERATOR"); // F5 : オペレータ
      sbSQL.append(", DATE_FORMAT(KKK.ADDDT, '%y/%m/%d')"); // F6 : 登録日
      sbSQL.append(", DATE_FORMAT(KKK.UPDDT, '%y/%m/%d')"); // F7 : 更新日
      sbSQL.append(", DATE_FORMAT(UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F8 : 排他チェック用：更新日(非表示)
      sbSQL.append(", YHS.MAX_HTDT"); // F9 : 発注日(企画内最大値)
      sbSQL.append(", YHN.MIN_NNDT"); // F10 : 発注日(企画内最小値)
      sbSQL.append(", YHN.MAX_NNDT"); // F11 : 発注日(企画内最大値)
      sbSQL.append(" from INATK.HATYH_KKK KKK");
      sbSQL.append(" left join (select KKKCD, MAX(HTDT) as MAX_HTDT from INATK.HATYH_SHN group by KKKCD) YHS on YHS.KKKCD = KKK.KKKCD");
      sbSQL.append(" left join (select KKKCD, MIN(NNDT) as MIN_NNDT, MAX(NNDT) as MAX_NNDT from INATK.HATYH_NNDT group by KKKCD) YHN on YHN.KKKCD = KKK.KKKCD");
      sbSQL.append(" where KKK.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and KKK.KKKCD = " + szKkkcd);
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

  /** 予約発注企画更新のKEY保持用変数 */
  String kkk_seq = ""; //

  /**
   * 商品店グループINSERT/UPDATE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  public String createSqlHatyuKKK(JSONObject data, HashMap<String, String> map, User userInfo) {

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    StringBuffer sbSQL;
    new ItemList();
    new JSONArray();

    if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
      kkk_seq = this.getKKK_SEQ();
    }

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

        // 新規登録はシーケンスを使用する。
        if (StringUtils.equals("F1", key)) {
          if (StringUtils.isEmpty(val)) {
            if (StringUtils.isNotEmpty(kkk_seq)) {
              val = kkk_seq;
            }
          }
        }

        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (k == 1) {
            values += ", RIGHT(?,4)";
            prmData.add(val);
          } else {
            values += ", ?";
            prmData.add(val);
          }
        }
      }

      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, values);
        values = "";
      }
    }

    // 予約発注_企画の登録・更新
    sbSQL = new StringBuffer();
    sbSQL.append(" INSERT INTO INATK.HATYH_KKK (");
    sbSQL.append("  KKKCD"); // F1 : 企画コード
    sbSQL.append(", KKKKM"); // F2 : 企画名称
    sbSQL.append(", NNSTDT"); // F3 : 納入開始日
    sbSQL.append(", NNEDDT"); // F4 : 納入終了日
    sbSQL.append(", UPDKBN"); // F5 : 更新区分
    sbSQL.append(", SENDFLG"); // F6 : 送信フラグ
    sbSQL.append(", OPERATOR "); // F7 : オペレーター
    sbSQL.append(", ADDDT "); // F8 : 登録日
    sbSQL.append(", UPDDT "); // F9 : 更新日
    sbSQL.append(") VALUES (");
    sbSQL.append(StringUtils.join(valueData, ",").substring(1));
    sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // F5 : 更新区分
    sbSQL.append(", 0 "); // F6 : 送信フラグ
    sbSQL.append(", '" + userId + "' "); // F7 : オペレーター
    sbSQL.append(", CURRENT_TIMESTAMP "); // F8 : 登録日
    sbSQL.append(", CURRENT_TIMESTAMP "); // F9 : 更新日
    sbSQL.append(")");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" KKKCD=VALUES(KKKCD) ");
    sbSQL.append(",KKKKM=VALUES(KKKKM) ");
    sbSQL.append(",NNSTDT=VALUES(NNSTDT) ");
    sbSQL.append(",NNEDDT=VALUES(NNEDDT) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("予約発注_企画");

    return sbSQL.toString();
  }

  /**
   * 予約発注商品INSERT/UPDATE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  public String createDelSqlYhs(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // 更新情報

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();

    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject data = dataArray.getJSONObject(i);

      // 予約発注_商品の登録・更新
      sbSQL = new StringBuffer();
      prmData = new ArrayList<String>();
      sbSQL.append("update INATK.HATYH_SHN");
      sbSQL.append(" set");
      sbSQL.append(" UPDKBN = ?");
      sbSQL.append(", SENDFLG = 0 ");
      sbSQL.append(", OPERATOR='" + userId + "'");
      sbSQL.append(", UPDDT=CURRENT_TIMESTAMP ");
      sbSQL.append(" where KKKCD = ?");
      sbSQL.append(" and SHNCD = ?");
      prmData.add(data.optString("F3"));
      prmData.add(data.optString("F1"));
      prmData.add(data.optString("F2"));

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("予約発注_商品");
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
    JSONArray dataArrayShn = JSONArray.fromObject(map.get("DATA_SHN")); // 対象情報
    JSONObject option = new JSONObject();

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    // 排他チェック用
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // SQL発行：予約発注_企画
    this.createSqlHatyuKKK(data, map, userInfo);

    // SQL発行：予約発注_商品
    if (dataArrayShn.size() > 0) {
      this.createDelSqlYhs(dataArrayShn, map, userInfo);

    }

    // 排他チェック実行
    if (StringUtils.isNotEmpty(data.optString("F1"))) {
      targetTable = "INATK.HATYH_KKK";
      targetWhere = " KKKCD = ?";
      targetParam.add(data.optString("F1"));

      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))) {
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
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          // option.put(MsgKey.S.getKey(), MessageUtility.getMessage("入力No"+kkk_seq+"で登録しました。"));
          // option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), kkk_seq));
          // option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
          JSONObject sMsg = new JSONObject();

          if (StringUtils.isNotEmpty(kkk_seq)) {
            sMsg = MessageUtility.getDbMessageIdObj("I00002", new String[] {"入力No" + kkk_seq + "で"});
          } else {
            option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
          }
          option.put(MsgKey.S.getKey(), sMsg);
        } else {
          option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
        }
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
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報

    JSONObject msgObj = new JSONObject();
    new JSONArray();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 更新情報
    String values = "";
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      values += data.optString("F1");
      values += data.optString("F2");

    }
    values = StringUtils.removeStart(values, ",");

    if (values.length() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    String szKkkcd = ""; // 企画コード
    String szShncd = ""; // 商品コード

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;
    ArrayList<String> prmData;

    // 商品店グループマスタ
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();

    sbSQL.append("update INATK.HATYH_SHN set");
    sbSQL.append(" UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
    sbSQL.append(", SENDFLG = 0 ");
    sbSQL.append(", OPERATOR = '" + userId + "' ");
    sbSQL.append(", UPDDT = CURRENT_TIMESTAMP ");
    sbSQL.append(" where KKKCD = ? and SHNCD = ?");

    prmData.add(szKkkcd);
    prmData.add(szShncd);
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("予約発注商品");

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
    MessageUtility mu = new MessageUtility();

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray.fromObject(map.get("DATA_SHN"));


    JSONArray msg = new JSONArray();
    new ItemList();
    JSONArray dbDatas = new JSONArray();
    new StringBuffer();
    new ArrayList<String>();

    dataArray.getJSONObject(0);

    if (dbDatas.size() > 0) {
      msg.add(mu.getDbMessageObj("E00006", new String[] {}));

    }
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

    map.get("TENGPCD");
    map.get("GPKBN");
    map.get("AREAKBN");


    new ItemList();
    new JSONArray();
    new ArrayList<String>();


    return msg;
  }

  /**
   * SEQ情報取得処理(企画No)
   *
   * @throws Exception
   */
  public String getKKK_SEQ() {
    // 関連情報取得
    ItemList iL = new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ005') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }
}
