package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportYH002Dao extends ItemDao {

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
  public ReportYH002Dao(String JNDIname) {
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

  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

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
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return option;
  }

  // 削除処理
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    JSONObject option = new JSONObject();
    JSONArray msgList = this.checkDel(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.deleteData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(配送グループ)
    JSONArray dataArrayNHN = JSONArray.fromObject(map.get("DATA_NHN")); // 更新情報(配送店グループ)
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    String sqlcommand = "";
    ArrayList<String> paramData = new ArrayList<String>();

    new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    MessageUtility mu = new MessageUtility();
    new JSONArray();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    String kkkcd = data.optString("F1");
    String shncd = data.optString("F2");

    // 標準-商品コード
    // ①商品マスタに無い場合エラー
    if (!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), shncd)) {
      JSONObject o = mu.getDbMessageObj("E11046", new String[] {});
      msg.add(o);
      return msg;
    }

    // 重複チェック：商品コード
    if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
      paramData = new ArrayList<String>();
      paramData.add(kkkcd);
      paramData.add(shncd);
      sqlcommand = "select COUNT(SHNCD) as value from INATK.HATYH_SHN where COALESCE(UPDKBN, 0) <> 1 and KKKCD = ? and SHNCD = ? ";

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        JSONObject o = mu.getDbMessageObj("E00004", new String[] {});
        msg.add(o);
        return msg;
      }
    }

    for (int i = 0; i < dataArrayNHN.size(); i++) {
      data = dataArray.getJSONObject(0);

      // 納入日取得
      ArrayList<JSONObject> nndt = new ArrayList<JSONObject>();
      HashSet<String> nndt_ = new HashSet<String>();
      String val = data.optString("F1");
      if (StringUtils.isNotEmpty(val)) {
        nndt.add(data); // レコード
        nndt_.add(val); // 納入日(HashSetにより重複なし状態にする。)
      }

      // 重複チェック
      if (nndt.size() != nndt_.size()) {
        JSONObject o = MessageUtility.getDbMessageIdObj("E20023", new String[] {});
        msg.add(o);
        return msg;
      }

    }
    return msg;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

    // 削除データ検索用コード
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    String tengpcd = map.get("TENGPCD"); // 配送店グループコード

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    MessageUtility mu = new MessageUtility();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    if (tengpcd.isEmpty() || tengpcd == null) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // エリア別配送パターンテーブルにレコードが存在する場合エラー
    sbSQL.append("SELECT ");
    sbSQL.append("	 HSGPCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("	INAMS.MSTAREAHSPTN ");
    sbSQL.append("WHERE ");
    sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND "); // 入力された配送グループで検索
    sbSQL.append("	TENGPCD	= '" + tengpcd + "' "); // 入力された配送店グループで検索

    dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

    if (dbDatas.size() >= 1) {
      // エリア別配送パターンに存在している
      msg.add(mu.getDbMessageObj("E11036", new String[] {}));
      return msg;
    }

    return msg;
  }

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // 排他チェック用
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 予約発注_商品、予約発注_納品日INSERT/UPDATE処理
    this.createSqlSHN(data, map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    if (dataArray.size() > 0) {
      targetTable = "INATK.HATYH_SHN";
      targetWhere = "COALESCE(UPDKBN, 0) <> " + DefineReport.ValUpdkbn.DEL.getVal() + " and KKKCD = ? and SHNCD = ? ";
      targetParam.add(data.optString("F1"));
      targetParam.add(data.optString("F2"));
      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F11"))) {
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
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

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


    // 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
    createDelSqlHs(data, map, userInfo);

    // 排他チェック実行
    targetTable = "INAMS.MSTHSGP";
    targetWhere = " HSGPCD = ?";
    targetParam.add(data.optString("F1"));

    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F11"))) {
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
   * 配送グループマスタ、配送店グループマスタDELETE(倫理削除)処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createDelSqlHs(JSONObject data, HashMap<String, String> map, User userInfo) {

    // 削除データ検索用コード
    String tengpcd = map.get("TENGPCD"); // 配送店グループコード

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 配送店グループマスタの件数チェック
    sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("	 HSGPCD ");
    sbSQL.append("FROM ");
    sbSQL.append("	INAMS.MSTHSTENGP ");
    sbSQL.append("WHERE ");
    sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND ");
    sbSQL.append("	UPDKBN	= " + DefineReport.ValUpdkbn.NML.getVal() + " ");

    dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("	INAMS.MSTHSGP ");
    sbSQL.append("SET ");
    // 1件のみの場合は対象の配送グループを論理削除
    if (dbDatas.size() == 1) {
      sbSQL.append("	 UPDKBN		= " + DefineReport.ValUpdkbn.DEL.getVal() + ", ");
    }
    sbSQL.append("	 OPERATOR	= '" + userId + "' ");
    sbSQL.append("	,UPDDT		= CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append("	HSGPCD = '" + data.optString("F1") + "' ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("配送グループマスタ");

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("	INAMS.MSTHSTENGP ");
    sbSQL.append("SET ");
    sbSQL.append("	 UPDKBN		= " + DefineReport.ValUpdkbn.DEL.getVal() + " ");
    sbSQL.append("	,OPERATOR	= '" + userId + "' ");
    sbSQL.append("	,UPDDT		= CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND ");
    sbSQL.append("	TENGPCD	= '" + tengpcd + "'");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("配送店グループマスタ");

    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("	INAMS.MSTHSGPTEN ");
    sbSQL.append("SET ");
    sbSQL.append("	 UPDKBN		= " + DefineReport.ValUpdkbn.DEL.getVal() + " ");
    sbSQL.append("	,OPERATOR	= '" + userId + "' ");
    sbSQL.append("	,UPDDT		= CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND ");
    sbSQL.append("	TENGPCD	= '" + tengpcd + "'");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("配送グループ店マスタ");

    return sbSQL.toString();
  }

  /**
   * 予約発注_商品、予約発注_納品日INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlSHN(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_NHN")); // 更新情報(予約発注_納品日)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    String yoteisu = data.optString("F9"); // 予定数
    String gendosu = data.optString("F10"); // 限度数

    int maxField = 17; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        // values += String.valueOf(0 + 1);

      }

      if (!ArrayUtils.contains(new String[] {"F11", "F12", "F13", "F14", "F15", "F16"}, key)) {
        String val = data.optString(key);
        if (StringUtils.isEmpty(val)) {

          if (StringUtils.equals(key, "F17")) {
            // デフォルト値設定:入力不可フラグ
            values += ", '0'";

          } else {
            values += ", null";
          }
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

    // 予約発注_商品の登録・更新
    sbSQL.append(" INSERT INTO INATK.HATYH_SHN (");
    sbSQL.append(" KKKCD"); // 企画コード
    sbSQL.append(", SHNCD"); // 商品コード
    sbSQL.append(", CATALGNO"); // カタログ番号
    sbSQL.append(", HTDT"); // 発注日
    sbSQL.append(", UKESTDT"); // 受付開始日
    sbSQL.append(", UKEEDDT"); // 受付終了日
    sbSQL.append(", TENISTDT"); // 店舗入力開始日
    sbSQL.append(", TENIEDDT"); // 店舗入力終了日
    sbSQL.append(", YOTEISU"); // 予定数
    sbSQL.append(", GENDOSU"); // 限度数
    sbSQL.append(", NGFLG"); // 入力不可フラグ
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
    sbSQL.append(" KKKCD=VALUES(KKKCD) ");
    sbSQL.append(",SHNCD=VALUES(SHNCD) ");
    sbSQL.append(",CATALGNO=VALUES(CATALGNO) ");
    sbSQL.append(",HTDT=VALUES(HTDT) ");
    sbSQL.append(",UKESTDT=VALUES(UKESTDT) ");
    sbSQL.append(",UKEEDDT=VALUES(UKEEDDT) ");
    sbSQL.append(",TENISTDT=VALUES(TENISTDT) ");
    sbSQL.append(",YOTEISU=VALUES(YOTEISU) ");
    sbSQL.append(",GENDOSU=VALUES(GENDOSU) ");
    sbSQL.append(",NGFLG=VALUES(NGFLG) ");
    sbSQL.append(",UPDKBN=VALUES(UPDKBN) ");
    sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
    sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
    // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
    sbSQL.append(",UPDDT=VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("予約発注_商品");

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    maxField = 9; // Fxxの最大値
    int len = dataArrayT.size();

    if (len > 0) {
      // 予約発注_納入日を物理削除する
      this.createDeleteSqNNDT(data);
    }

    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);
          System.out.println(k);
          if (k == 1) {
            // values += String.valueOf(0 + 1);
            // 配送グループコードを追加
          }

          if (!ArrayUtils.contains(new String[] {"F6", "F7", "F8", "F9"}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.equals("F3", key)) {
              // 納入日
              val = common.CmnDate.dateFormat(common.CmnDate.convYYMMDD(val));

            } else if (StringUtils.equals("F4", key)) {
              // 予定数
              if (StringUtils.equals(yoteisu, "0") && StringUtils.isEmpty(val)) {
                // 予約発注_商品.予定数が0の時、空の値に0を設定する。
                val = "0";
              }

            } else if (StringUtils.equals("F5", key)) {
              // 限度数
              if (StringUtils.equals(gendosu, "0") && StringUtils.isEmpty(val)) {
                // 予約発注_商品.予定数が0の時、空の値に0を設定する。
                val = "0";
              }
            }

            if (k == 1) {
              if (StringUtils.isEmpty(val)) {
                values += " null";
              } else {
                values += " ?";
                prmData.add(val);
              }
            } else {
              if (StringUtils.isEmpty(val)) {
                values += ", null";
              } else {
                values += ", ?";
                prmData.add(val);
              }
            }
          }
          if (k == 9) {
            values += ", 0";
            values += ", '" + userId + "'";
            values += ", CURRENT_TIMESTAMP";
            values += ", CURRENT_TIMESTAMP";
            values += "";
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {

        // 予約発注_納品日の登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.HATYH_NNDT (");
        sbSQL.append(" KKKCD"); // 企画コード
        sbSQL.append(", SHNCD"); // 商品コード
        sbSQL.append(", NNDT"); // 納入日
        sbSQL.append(", YOTEISU"); // 予定数
        sbSQL.append(", GENDOSU"); // 限度数
        sbSQL.append(", SENDFLG"); // 送信フラグ
        sbSQL.append(", OPERATOR "); // オペレーター
        sbSQL.append(", ADDDT "); // 登録日
        sbSQL.append(", UPDDT "); // 更新日
        sbSQL.append(") values (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append(" KKKCD=VALUES(KKKCD) ");
        sbSQL.append(",SHNCD=VALUES(SHNCD) ");
        sbSQL.append(",NNDT=VALUES(NNDT) ");
        sbSQL.append(",YOTEISU=VALUES(YOTEISU) ");
        sbSQL.append(",GENDOSU=VALUES(GENDOSU) ");
        sbSQL.append(",SENDFLG=VALUES(SENDFLG) ");
        sbSQL.append(",OPERATOR=VALUES(OPERATOR) ");
        // sbSQL.append(",ADDDT=VALUES(ADDDT) ");
        sbSQL.append(",UPDDT=VALUES(UPDDT) ");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("予約発注_納品日");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * 予約発注_納入日 DELETE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  public String createDeleteSqNNDT(JSONObject data) {
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String kkkcd = data.optString("F1"); // 商品コード
    String shncd = data.optString("F2");

    StringBuffer sbSQL;

    sbSQL = new StringBuffer();
    sbSQL.append("delete from INATK.HATYH_NNDT where KKKCD = ? and SHNCD = ?");
    prmData.add(kkkcd);
    prmData.add(shncd);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("予約発注_納入日");

    return sbSQL.toString();
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szKkkcd = getMap().get("KKKCD"); // 企画No
    String szShncd = getMap().get("SHNCD"); // 商品No
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (szKkkcd == null || sendBtnid == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)) {
      sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
      sbSQL.append(" select");
      sbSQL.append(" CAST(T1.KKKCD AS CHAR) || ' - ' || T1.KKKKM");
      sbSQL.append(", T1.KKKCD");
      sbSQL.append(", T1.NNSTDT_DSP || W1.JWEEK || '～' || T1.NNEDDT_DSP || W2.JWEEK as TEXT");
      sbSQL.append(", right (T1.NNSTDT, 6)");
      sbSQL.append(", right (T1.NNEDDT, 6)");
      sbSQL.append(", right ('0000' || SHN.CATALGNO, 4)");
      // sbSQL.append(", SHN.CATALGNO");
      sbSQL.append(", SHN.SHNCD");
      sbSQL.append(", right (SHN.HTDT, 6)");
      sbSQL.append(", right (SHN.UKESTDT, 6)");
      sbSQL.append(", right (SHN.UKEEDDT, 6)");
      sbSQL.append(", right (SHN.TENISTDT, 6)");
      sbSQL.append(", right (SHN.TENIEDDT, 6)");
      sbSQL.append(", SHN.YOTEISU");
      sbSQL.append(", SHN.GENDOSU");
      sbSQL.append(", DATE_FORMAT(SHN.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT ");
      sbSQL.append(", SHN.OPERATOR");
      sbSQL.append(", DATE_FORMAT(SHN.ADDDT, '%y/%m/%d') as ADDDT");
      sbSQL.append(", DATE_FORMAT(SHN.UPDDT, '%y/%m/%d') as UPDDT");
      sbSQL.append(", SHN.NGFLG");
      sbSQL.append(" from (select");
      sbSQL.append(" T2.*, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || T2.NNSTDT, 6), '%Y%m%d'), '%y/%m/%d') as NNSTDT_DSP");
      sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || T2.NNSTDT, 6), '%Y%m%d')) as STARTDTW");
      sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || T2.NNEDDT, 6), '%Y%m%d'), '%y/%m/%d') as NNEDDT_DSP");
      sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || T2.NNEDDT, 6), '%Y%m%d')) as ENDDTW");
      sbSQL.append(" from INATK.HATYH_KKK T2");
      sbSQL.append(" where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and KKKCD = " + szKkkcd + ") T1");
      sbSQL.append(" left outer join WEEK W1 on T1.STARTDTW = W1.CWEEK");
      sbSQL.append(" left outer join WEEK W2 on T1.ENDDTW = W2.CWEEK");
      sbSQL.append(" left outer join INATK.HATYH_SHN SHN on SHN.KKKCD = T1.KKKCD");
      sbSQL.append(" where SHN.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and SHN.SHNCD = '" + szShncd + "'");

    } else if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {

      sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
      sbSQL.append(" select");
      sbSQL.append(" CAST(T1.KKKCD AS CHAR) || ' - ' || T1.KKKKM");
      sbSQL.append(", T1.KKKCD");
      sbSQL.append(", T1.NNSTDT_DSP || W1.JWEEK || '～' || T1.NNEDDT_DSP || W2.JWEEK as TEXT");
      sbSQL.append(", right (T1.NNSTDT, 6)");
      sbSQL.append(", right (T1.NNEDDT, 6)");
      sbSQL.append(" from (select");
      sbSQL.append(" T2.*, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || T2.NNSTDT, 6), '%Y%m%d'), '%y/%m/%d') as NNSTDT_DSP");
      sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || T2.NNSTDT, 6), '%Y%m%d')) as STARTDTW");
      sbSQL.append(", DATE_FORMAT(DATE_FORMAT('20' || right ('0' || T2.NNEDDT, 6), '%Y%m%d'), '%y/%m/%d') as NNEDDT_DSP");
      sbSQL.append(", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || T2.NNEDDT, 6), '%Y%m%d')) as ENDDTW");
      sbSQL.append(" from INATK.HATYH_KKK T2");
      sbSQL.append(" where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and KKKCD = " + szKkkcd + ") T1");
      sbSQL.append(" left outer join WEEK W1 on T1.STARTDTW = W1.CWEEK");
      sbSQL.append(" left outer join WEEK W2 on T1.ENDDTW = W2.CWEEK");
      sbSQL.append(" left outer join INATK.HATYH_SHN SHN on SHN.KKKCD = T1.KKKCD");

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
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public boolean checkMstExist(String outobj, String value) {
    // 関連情報取得
    ItemList iL = new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";

    String tbl = "";
    String col = "";
    String rep = "";
    // 商品コード
    if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
      tbl = "INAMS.MSTSHN";
      col = "SHNCD";
    }


    if (tbl.length() > 0 && col.length() > 0) {
      if (paramData.size() > 0 && rep.length() > 0) {
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep);
      } else {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);
      }

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
