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
import common.Defines;
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.Reportx002Dao.CSVSHNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx092Dao extends ItemDao {

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
  public Reportx092Dao(String JNDIname) {
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
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map, userInfo, sysdate);

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
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    // JSONArray msg = this.check(map, userInfo, sysdate);
    JSONArray msg = this.checkDel(map, userInfo, sysdate);


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
   * 割引総額売価取得
   *
   * @throws Exception
   */
  public String createSqlSelMSTSHN(JSONObject obj) {

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT ");
    sbSQL.append(" T1.SHNKN AS F1");
    sbSQL.append(" ,LPAD(T2.DAICD,2,'0') AS F2");
    sbSQL.append(" ,T2.DAIBRUIKN AS F3");
    sbSQL.append(" ,LPAD(T3.CHUCD,2,'0') AS F4");
    sbSQL.append(" ,T3.CHUBRUIKN AS F5");
    sbSQL.append(" ,LPAD(T4.SHOCD,2,'0') AS F6");
    sbSQL.append(" ,T4.SHOBRUIKN AS F7");
    sbSQL.append(" ,T1.URICD AS F8");
    sbSQL.append(" ,T1.KIKKN AS F9");
    sbSQL.append(" ,T1.ODS_NATSUSU AS F10");
    sbSQL.append(" ,T1.RG_IRISU AS F11");
    sbSQL.append(" FROM INAMS.MSTSHN T1 ");
    sbSQL.append(" LEFT JOIN INAMS.MSTDAIBRUI T2 ON ");
    sbSQL.append(" T1.BMNCD = T2.BMNCD AND ");
    sbSQL.append(" T1.DAICD = T2.DAICD AND ");
    sbSQL.append(" T2.UPDKBN <> 1 ");
    sbSQL.append(" LEFT JOIN INAMS.MSTCHUBRUI T3 ON ");
    sbSQL.append(" T1.BMNCD = T3.BMNCD AND  ");
    sbSQL.append(" T1.DAICD = T3.DAICD AND ");
    sbSQL.append(" T1.CHUCD = T3.CHUCD AND ");
    sbSQL.append(" T3.UPDKBN <> 1 ");
    sbSQL.append(" LEFT JOIN INAMS.MSTSHOBRUI T4 ON ");
    sbSQL.append(" T1.BMNCD = T4.BMNCD AND ");
    sbSQL.append(" T1.DAICD = T4.DAICD AND ");
    sbSQL.append(" T1.CHUCD = T4.CHUCD AND ");
    sbSQL.append(" T1.SHOCD = T4.SHOCD AND ");
    sbSQL.append(" T4.UPDKBN <> 1 ");
    sbSQL.append(" WHERE  ");
    sbSQL.append(" T1.UPDKBN <> 1 AND ");
    sbSQL.append(" T1.SHNCD = ? ");

    return sbSQL.toString();
  }

  /**
   * 割引総額売価取得
   *
   * @throws Exception
   */
  public String createSqlGenryo(JSONObject obj) {

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT ");
    sbSQL.append(" T1.SHNKN AS F1");
    sbSQL.append(" ,T2.SIRKN AS F2");
    sbSQL.append(" FROM INAMS.MSTSHN T1 ");
    sbSQL.append(" LEFT JOIN INAMS.MSTSIR T2 ");
    sbSQL.append(" ON T1.SSIRCD = T2.SIRCD");
    sbSQL.append(" WHERE ");
    sbSQL.append(" T1.SHNCD = ? ");

    return sbSQL.toString();
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szCallcd = getMap().get("TXT_CALLCD"); // 呼出コード
    String szBmncd = getMap().get("TXT_BMNCD"); // 部門コード
    String szShncd = getMap().get("TXT_SHNCD"); // 商品コード
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    paramData.add(szBmncd);
    paramData.add(szCallcd);
    paramData.add(szShncd);
    // パラメータ確認
    // 必須チェック
    if (szCallcd == null || sendBtnid == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    if (DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)) {
      sbSQL.append("SELECT ");
      sbSQL.append("T1.BMNCD AS F1 "); // F1 : 部門
      sbSQL.append(",T1.CALLCD AS F2 "); // F2 : 呼出コード
      sbSQL.append(",RIGHT ('0000000' || RTRIM(T1.SHNCD), 8) AS F3 ");
      sbSQL.append(",T2.SHNKN AS F4 "); // F4 : 商品名漢字
      sbSQL.append(",T1.SHNKNUP AS F5 ");// F5 : 商品名上段
      sbSQL.append(",T1.SHNKNDN AS F6 ");// F6 : 商品名下段
      sbSQL.append(",LPAD(T2.DAICD,2,'0') AS F7 ");// F7 : 大コード
      sbSQL.append(",T3.DAIBRUIKN AS F8 ");// F8 : 大分類名（漢字）
      sbSQL.append(",LPAD(T2.CHUCD,2,'0') AS F9 ");// F9 : 中コード
      sbSQL.append(",T4.CHUBRUIKN AS F10 ");// F10 : 中分類名（漢字）
      sbSQL.append(",LPAD(T2.SHOCD,2,'0') AS F11 ");// F11 : 小コード
      sbSQL.append(",T5.SHOBRUIKN AS F12 ");// F12 : 小分類名（漢字）
      sbSQL.append(",T2.RG_IRISU AS F13 ");// F13 : 入数
      sbSQL.append(",T1.UTRAY AS F14 ");// F14 : 使用トレイ
      sbSQL.append(",T1.KAKOKBN AS F15 ");// F15 : 生鮮・加工食品区分
      sbSQL.append(",T1.KONPOU AS F16 ");// F16: 梱包
      sbSQL.append(",T1.TEIKANKBN AS F17 ");// F17 : 定貫・不定貫区分
      sbSQL.append(",T1.FUTAI AS F18 ");// F18: 風袋
      sbSQL.append(",T2.URICD AS F19 ");// F19 : 販売コード
      sbSQL.append(",T1.JURYOUP AS F20 ");// F20: 下限重量
      sbSQL.append(",T2.KIKKN AS F21 ");// F21 : 規格漢字名
      sbSQL.append(",T1.JURYODN AS F22 ");// F22: 上限重量
      sbSQL.append(",T1.NAIKN AS F23 ");// F23 : 内容量
      sbSQL.append(",T2.ODS_NATSUSU AS F24 ");// F24 : ODS_賞味期限_夏
      sbSQL.append(",T1.UPDKBN AS F25 ");// F25: 更新区分
      sbSQL.append(",T1.SENDFLG AS F26 ");// F41: 送信フラグ
      sbSQL.append(",DATE_FORMAT(T1.ADDDT,'%y/%m/%d') AS F42 ");// F42: 登録日
      sbSQL.append(",DATE_FORMAT(T1.UPDDT,'%y/%m/%d') AS F43 ");// F43: 更新日
      sbSQL.append(",T1.OPERATOR AS F44 ");// F44: オペレータ
      sbSQL.append(",DATE_FORMAT(T1.UPDDT,'%Y%m%d%H%i%s%f') AS HDN_UPDDT "); // 排他チェック用：更新日
      sbSQL.append("FROM INAMS.MSTNETSUKE T1 ");
      sbSQL.append(" LEFT JOIN INAMS.MSTSHN T2 ON ");
      sbSQL.append(" T1.SHNCD = T2.SHNCD AND ");
      sbSQL.append(" T2.UPDKBN <> 1 ");
      sbSQL.append(" LEFT JOIN INAMS.MSTDAIBRUI T3 ON ");
      sbSQL.append(" T2.BMNCD = T3.BMNCD AND ");
      sbSQL.append(" T2.DAICD = T3.DAICD AND ");
      sbSQL.append(" T3.UPDKBN <> 1 ");
      sbSQL.append(" LEFT JOIN INAMS.MSTCHUBRUI T4 ON ");
      sbSQL.append(" T2.BMNCD = T4.BMNCD AND ");
      sbSQL.append(" T2.DAICD = T4.DAICD AND ");
      sbSQL.append(" T2.CHUCD = T4.CHUCD AND ");
      sbSQL.append(" T4.UPDKBN <> 1 ");
      sbSQL.append(" LEFT JOIN INAMS.MSTSHOBRUI T5 ON ");
      sbSQL.append(" T2.BMNCD = T5.BMNCD AND ");
      sbSQL.append(" T2.DAICD = T5.DAICD AND ");
      sbSQL.append(" T2.CHUCD = T5.CHUCD AND ");
      sbSQL.append(" T2.SHOCD = T5.SHOCD AND ");
      sbSQL.append(" T5.UPDKBN <> 1 ");
      sbSQL.append(" WHERE T1.BMNCD = ?");
      sbSQL.append(" AND T1.CALLCD = ?");
      sbSQL.append(" AND T1.SHNCD = ?");
      // DB検索用パラメータ設定
      setParamData(paramData);

      // オプション情報（タイトル）設定
      JSONObject option = new JSONObject();
      option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
      setOption(option);
    }

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }


  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public String getTOKMOYCDData(JSONObject obj) {

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("RIGHT ('0000000' || RTRIM(T1.SHNCD), 8) AS F1 ");
    sbSQL.append(",T2.SHNKN AS F2 ");
    sbSQL.append(",T1.NAIKN AS F3 ");
    sbSQL.append(",T1.GENKA AS F4 ");
    sbSQL.append(",T1.BUDOMARI AS F5 ");
    sbSQL.append(",T1.GENKAKEI AS F6 ");
    sbSQL.append(",T3.SIRKN AS F7 ");
    sbSQL.append(",0 AS F8 ");
    sbSQL.append(",1 AS F9 ");
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTUGENRYO T1 ");
    sbSQL.append("LEFT JOIN ");
    sbSQL.append("INAMS.MSTSHN T2 ");
    sbSQL.append("ON T1.SHNCD = T2.SHNCD ");
    sbSQL.append("LEFT JOIN ");
    sbSQL.append("INAMS.MSTSIR T3 ");
    sbSQL.append("ON T3.SIRCD = T2.SSIRCD ");
    sbSQL.append("WHERE ");
    sbSQL.append("T1.BMNCD = ? ");
    sbSQL.append("AND T1.CALLCD = ? ");
    sbSQL.append("AND T1.UPDKBN = 0 ");
    sbSQL.append("ORDER BY T1.SHNCD ");


    return sbSQL.toString();
  }


  boolean isTest = true;

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<String>();

    // 期間系
    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.KIKAN.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
    getWhere().add(cells);

    // 店舗系
    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.KIGYO.getTxt());
    cells.add(DefineReport.Select.TENPO.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.TENPO.getObj()));
    getWhere().add(cells);

    // 分類系
    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.BUMON.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.BUMON.getObj()));
    getWhere().add(cells);

    // 共通箇所設定
    createCmnOutput(jad);

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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();


    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 値付器マスタ、使用原料マスタINSERT/UPDATE処理
    this.createSqlNETSUKE(data, map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    if (dataArray.size() > 0) {
      targetTable = "INAMS.MSTNETSUKE";
      targetWhere = "IFNULL(UPDKBN, 0) <> " + DefineReport.ValUpdkbn.DEL.getVal() + " AND BMNCD = ? AND CALLCD = ? AND SHNCD = ?  ";
      targetParam.add(data.optString("F1"));
      targetParam.add(data.optString("F2"));
      targetParam.add(data.optString("F3"));
      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F14"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
    }

    // 更新情報
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
   * 値付器マスタ、使用原料マスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlNETSUKE(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("GENRYO_DATA")); // 更新情報(予約発注_納品日)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    ArrayList<String> whereData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    System.out.print("map : " + map + "\n");

    map.get("SENDBTNID");
    map.get("BMNCD");
    map.get("CALLCD");

    int maxField = 13; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (k == 1) {
        values += String.valueOf(0 + 1);

      }

      if (!ArrayUtils.contains(new String[] {""}, key)) {
        String val = data.optString(key);
        if (StringUtils.isEmpty(val)) {
          values += ", NULL";
        } else {
          values += ", ?";
          prmData.add(val);
          whereData.add(val);
        }
      }

      if (k == maxField) {
        valueData = ArrayUtils.add(valueData, values.substring(2));
        values = "";
      }
    }

    // 値付器マスタの登録・更新
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INAMS.MSTNETSUKE ( ");
    sbSQL.append("BMNCD ");// F1 : 部門
    sbSQL.append(",CALLCD ");// F2 : 呼出コード
    sbSQL.append(",SHNCD ");// F3 : 商品コード
    sbSQL.append(",SHNKNUP ");// F4 : 商品名上段
    sbSQL.append(",SHNKNDN ");// F5 : 商品名下段
    sbSQL.append(",KAKOKBN ");// F6 : 生鮮・加工食品区分
    sbSQL.append(",TEIKANKBN");// F7 : 定貫・不定貫区分
    sbSQL.append(",NAIKN ");// F8 : 内容量
    sbSQL.append(",UTRAY ");// F9 : 使用トレイ
    sbSQL.append(",KONPOU ");// F10: 包装形態
    sbSQL.append(",FUTAI ");// F11: 風袋
    sbSQL.append(",JURYOUP ");// F12: 下限重量
    sbSQL.append(",JURYODN ");// F13: 上限重量
    sbSQL.append(",UPDKBN ");// F14: 更新区分
    sbSQL.append(",SENDFLG ");// F15:送信フラグ
    sbSQL.append(",OPERATOR ");// F16:オペレーター：
    sbSQL.append(",ADDDT ");// F17:登録日：
    sbSQL.append(",UPDDT ");// F18:更新日：
    sbSQL.append(" ) VALUES (" + StringUtils.join(valueData, ",") + " ");
    sbSQL.append(",0 ");
    sbSQL.append(",0 ");
    sbSQL.append(",'" + userId + "' ");
    sbSQL.append(",(SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ");
    sbSQL.append("ELSE ADDDT END AS ADDDT FROM INAMS.MSTNETSUKE ");
    sbSQL.append("WHERE BMNCD = " + whereData.get(0) + " ");
    sbSQL.append("AND SHNCD = " + whereData.get(2) + " ");
    sbSQL.append("AND CALLCD = " + whereData.get(1) + " LIMIT 1 ) AS T1 )");
    sbSQL.append(",CURRENT_TIMESTAMP )");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("値付器マスタ");


    // 使用原料マスタの登録・更新
    if (!dataArrayT.isEmpty()) {
      // クリア
      prmData = new ArrayList<String>();
      valueData = new Object[] {};
      whereData = new ArrayList<String>();
      values = "";
      maxField = 7; // Fxxの最大値


      for (int j = 0; j < dataArrayT.size(); j++) {
        if (j == 0) {
          values += "( ";
        } else {
          values += ",( ";
        }

        for (int i = 1; i <= maxField; i++) {
          String key = "F" + i;
          String val = dataArrayT.optJSONObject(j).optString(key);
          if (i == 3) {
            values += ",(SELECT * FROM (SELECT BMNCD FROM INAMS.MSTSHN WHERE SHNCD = " + dataArrayT.optJSONObject(j).optString("F1") + " LIMIT 1) AS T1 )";
          } else if (StringUtils.isEmpty(val)) {
            if (i == 1) {
              values += "NULL ";
            } else {
              values += ",NULL ";
            }
          } else if (!StringUtils.isEmpty(val)) {
            if (i == 1) {
              values += "? ";
            } else {
              values += ",? ";
            }
            prmData.add(val);
          }
        }
        values += ",0 ";
        // 削除処理
        if (dataArrayT.optJSONObject(j).optString("F8").equals("1")) {
          values += ",1 ";
        } else {
          values += ",0 ";
        }
        values += ",'" + userId + "' ";
        values += ",( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values += "FROM INAMS.MSTUGENRYO  ";
        values += "WHERE CALLCD = " + dataArrayT.optJSONObject(j).optString("F2") + " ";
        values += "AND SHNCD = " + dataArrayT.optJSONObject(j).optString("F1") + " LIMIT 1 ) T2 ) ";
        values += ",CURRENT_TIMESTAMP ";
        values += ") ";

        if (j == dataArrayT.size() - 1) {
          valueData = ArrayUtils.add(valueData, values);
          values = "";
        }

      }

      sbSQL = new StringBuffer();
      sbSQL.append("REPLACE INTO INAMS.MSTUGENRYO ( ");
      sbSQL.append(" SHNCD ");// F1 : 商品コード
      sbSQL.append(",CALLCD ");// F2 : 呼出コード
      sbSQL.append(",BMNCD ");// F3 : 部門コード
      sbSQL.append(",NAIKN ");// F4 : 内容量
      sbSQL.append(",GENKA ");// F5 : 原料原価
      sbSQL.append(",BUDOMARI ");// F6 : 歩留り
      sbSQL.append(",GENKAKEI ");// F7 : 原価小計
      sbSQL.append(",SENDFLG ");// F8:送信フラグ
      sbSQL.append(",UPDKBN ");// F8:更新区分
      sbSQL.append(",OPERATOR ");// F9:オペレーター：
      sbSQL.append(",ADDDT ");// F10:登録日：
      sbSQL.append(",UPDDT ");// F11:更新日：
      sbSQL.append(") VALUES " + StringUtils.join(valueData, ",") + " ");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("使用原料マスタ");

      // クリア
      prmData = new ArrayList<String>();
      valueData = new Object[] {};
      values = "";

    }

    return sbSQL.toString();
  }

  /**
   * 使用原料マスタ DELETE処理
   *
   * @param dataArray
   * @param map
   * @param userInfo
   */
  public String createDeleteSqlGENRYO(HashMap<String, String> map) {
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String bmoncd = map.get("BMNCD"); // 部門コード
    String callcd = map.get("CALLCD"); // 呼出コード

    StringBuffer sbSQL;

    sbSQL = new StringBuffer();;
    sbSQL.append("DELETE FROM INAMS.MSTUGENRYO WHERE BMNCD = ? AND CALLCD = ?");
    prmData.add(bmoncd);
    prmData.add(callcd);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("使用原料マスタ");

    return sbSQL.toString();
  }


  /** マスタレイアウト */
  public interface MSTLayout {
    public Integer getNo();

    public String getCol();

    public String getTyp();

    public String getId();

    public DataType getDataType();

    public boolean isText();
  }

  /** 値付機マスタレイアウト */
  public enum MSTNETSUKELayout implements MSTLayout {
    /** 部門 */
    BMNCD(1, "BMNCD", "SMALLINT"),
    /** 呼出コード */
    CALLCD(2, "CALLCD", "INTEGER"),
    /** 商品コード */
    SHNCD(3, "SHNCD", "INTEGER"),
    /** 商品名上段 */
    BRUICD(4, "SHNKNUP", "VARCHAR(50)"),
    /** 商品名下段 */
    KAKOBIINJI(5, "SHNKNDN", "VARCHAR(50)"),
    /** 生鮮・加工食品区分 */
    KAKOJIINJI(6, "KAKOKBN", "SMALLINT"),
    /** 定貫・不定貫区分 */
    KAKOJISEL(7, "TEIKANKBN", "SMALLINT"),
    /** 内容量 */
    SHOHIBIINJI(8, "NAIKN", "VARCHAR(46)"),
    /** 使用トレイ */
    SHOHIJIINJI(9, "UTRAY", "VARCHAR(30)"),
    /** 包装形態 */
    OMOTEBARI(10, "KONPOU", "VARCHAR(20)"),
    /** 風袋 */
    URABARIHAKKO(11, "FUTAI", "SMALLINT"),
    /** 下限重量 */
    URABARI(12, "JURYOUP", "SMALLINT"),
    /** 上限重量 */
    AICATCHLABEL(13, "JURYODN", "SMALLINT");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private MSTNETSUKELayout(Integer no, String col, String typ) {
      this.no = no;
      this.col = col;
      this.typ = typ;
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

    /** @return tbl 列型 */
    @Override
    public String getTyp() {
      return typ;
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
      if (typ.indexOf("DATE") != -1) {
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

  private enum dbNumericTypeInfo {
    /** SMALLINT */
    SMALLINT(5, -32768, 32768),
    /** INT */
    INT(10, -2147483648, 2147483648l),
    /** INTEGER */
    INTEGER(10, -2147483648, 2147483648l);

    private final int digit;

    /** 初期化 */
    private dbNumericTypeInfo(int digit, long min, long max) {
      this.digit = digit;
    }

    /** @return digit */
    public int getDigit() {
      return digit;
    }
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

    String bmncd = map.get("BMNCD"); // 部門コード
    String callcd = map.get("CALLCD"); // 呼出コード

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    if (dataArray.size() == 0) {
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
      return msgObj;
    }

    // 基本INSERT/UPDATE文
    StringBuffer sbSQL = new StringBuffer();;
    ArrayList<String> prmData = new ArrayList<String>();
    prmData.add(bmncd);
    prmData.add(callcd);
    // 削除処理：更新区分に"1"（削除）を登録
    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE INAMS.MSTNETSUKE ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + " ");
    sbSQL.append(",SENDFLG = 0 ");
    sbSQL.append(",OPERATOR = '" + userId + "' ");
    sbSQL.append(",UPDDT = current_timestamp ");
    sbSQL.append("WHERE BMNCD = ? ");
    sbSQL.append("AND CALLCD = ? ");

    int count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("値付器マスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
    } else {
      msgObj.put(MsgKey.E.getKey(), getMessage());
    }

    // 基本INSERT/UPDATE文
    sbSQL = new StringBuffer();
    prmData = new ArrayList<String>();
    prmData.add(callcd);
    // 削除処理
    sbSQL = new StringBuffer();
    sbSQL.append("DELETE FROM INAMS.MSTUGENRYO ");
    sbSQL.append("WHERE CALLCD = ?  ");
    count = super.executeSQL(sbSQL.toString(), prmData);
    if (StringUtils.isEmpty(getMessage())) {
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println("使用原料マスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] {Integer.toString(count)}));
      msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
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
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    MessageUtility mu = new MessageUtility();
    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

    List<JSONObject> msgList = this.checkData(isNew, isChange, map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public List<JSONObject> checkData(boolean isNew, boolean isChange, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）
  ) {
    JSONArray msg = new JSONArray();
    String ErrTblNm = "MSTNETSUKE";
    JSONObject data = dataArray.optJSONObject(0);
    JSONArray dataArray2 = JSONArray.fromObject(map.get("GENRYO_DATA")); // 対象情報

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.値付器マスタ
    for (MSTNETSUKELayout colinf : MSTNETSUKELayout.values()) {
      String val = StringUtils.trim(data.optString(colinf.getId()));
      if (StringUtils.isNotEmpty(val)) {
        DataType dtype = null;
        int[] digit = null;
        try {
          DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
          dtype = inpsetting.getType();
          digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
        } catch (IllegalArgumentException e) {
          dtype = colinf.getDataType();
          digit = colinf.getDigit();
        }
        // ①データ型による文字種チェック
        if (!InputChecker.checkDataType(dtype, val)) {
          JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
          o.put(CSVSHNLayout.ERRCD.getCol(), o.optString(MessageUtility.CD));
          o.put(CSVSHNLayout.ERRTBLNM.getCol(), ErrTblNm);
          o.put(CSVSHNLayout.ERRFLD.getCol(), colinf.getCol());
          o.put(CSVSHNLayout.ERRVL.getCol(), o.optString(MessageUtility.MSG));
          msg.add(o);
          return msg;
        }
        // ②データ桁チェック
        if (!InputChecker.checkDataLen(dtype, val, digit)) {
          JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
          o.put(CSVSHNLayout.ERRCD.getCol(), o.optString(MessageUtility.CD));
          o.put(CSVSHNLayout.ERRTBLNM.getCol(), ErrTblNm);
          o.put(CSVSHNLayout.ERRFLD.getCol(), colinf.getCol());
          o.put(CSVSHNLayout.ERRVL.getCol(), o.optString(MessageUtility.MSG));
          msg.add(o);
          return msg;
        }
      }
    }

    if (this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), data.optString(MSTNETSUKELayout.SHNCD.getId()))) {
      JSONObject o = mu.getDbMessageObj("E20160", new String[] {});
      o.put(CSVSHNLayout.ERRCD.getCol(), o.optString(MessageUtility.CD));
      o.put(CSVSHNLayout.ERRTBLNM.getCol(), ErrTblNm);
      o.put(CSVSHNLayout.ERRFLD.getCol(), MSTNETSUKELayout.SHNCD.getCol());
      o.put(CSVSHNLayout.ERRVL.getCol(), o.optString(MessageUtility.MSG));
      msg.add(o);
      return msg;
    }

    if (dataArray2.size() > 0) {
      for (int i = 0; i < dataArray2.size(); i++) {
        this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), dataArray2.optJSONObject(i).optString("F1"));
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

    JSONArray.fromObject(map.get("DATA"));
    map.get("TENGPCD");

    new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    new JSONArray();
    new MessageUtility();

    // チェック処理
    // 対象件数チェック
    /*
     * if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
     * msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal())); return msg; }
     */

    return msg;
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
    // 商品コード
    tbl = "INAMS.MSTSHN";
    col = "SHNCD";

    paramData.add(value);
    sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);

    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
      return false;
    }
    return true;
  }
}
