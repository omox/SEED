package dao;

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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTR005Dao extends ItemDao {

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
  public ReportTR005Dao(String JNDIname) {
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(正規定量_商品)
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_HTSTR")); // 更新情報(正規定量_店別数量)
    boolean chgFlg = Boolean.valueOf(map.get("CHGFLG"));

    // 格納用変数
    JSONArray msg = new JSONArray();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    if (!chgFlg && (dataArrayT.size() == 0 || dataArrayT.getJSONObject(0).isEmpty())) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
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

    // 格納用変数
    JSONArray msg = new JSONArray();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    return msg;
  }

  public List<JSONObject> checkData(MessageUtility mu, JSONArray dataArray, // 正規
      JSONArray dataArrayJ, // 次週
      JSONObject dataOther // その他情報
  ) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    ArrayList<String> paramData = new ArrayList<String>();

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.正規
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);

      if (jo.size() == 0) {
        continue;
      }

      String reqNo = String.valueOf(jo.optString(HATSTRLayout.GYONO.getId())) + "行目：";

      for (HATSTRLayout colinf : HATSTRLayout.values()) {

        // 行番号ならチェックはなし
        if (colinf.getId().equals(HATSTRLayout.GYONO.getId())) {
          continue;
        }

        String val = StringUtils.trim(jo.optString(colinf.getId()));
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
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + colinf.getText() + "は"});
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + colinf.getText() + "は"});
            msg.add(o);
            return msg;
          }

          // 週No.の入力は不可
          if (colinf.getId().equals(HATSTRLayout.SHUNO.getId()) && !StringUtils.isEmpty(val)) {
            JSONObject o = mu.getDbMessageObj("EX1091", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }

    }

    // 2.次週
    for (int i = 0; i < dataArrayJ.size(); i++) {
      JSONObject jo = dataArrayJ.optJSONObject(i);

      if (jo.size() == 0) {
        continue;
      }

      String reqNo = String.valueOf(jo.optString(HATSTRLayout.GYONO.getId())) + "行目：";

      for (HATSTRLayout colinf : HATSTRLayout.values()) {

        // 行番号ならチェックはなし
        if (colinf.getId().equals(HATSTRLayout.GYONO.getId())) {
          continue;
        }

        String val = StringUtils.trim(jo.optString(colinf.getId()));
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
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + colinf.getText() + "は"});
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + colinf.getText() + "は"});
            msg.add(o);
            return msg;
          }
        } else {
          // 週No.の入力は必須
          if (colinf.getId().equals(HATSTRLayout.SHUNO.getId())) {
            JSONObject o = mu.getDbMessageObj("EX1092", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 新規(正) 1.1 必須入力項目チェックを行う。
    // 変更(正) 1.1 必須入力項目チェックを行う。
    HATSTRLayout[] targetCol = null;
    targetCol = new HATSTRLayout[] {HATSTRLayout.TENCD, HATSTRLayout.SHNCD};
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);

      if (jo.size() == 0) {
        continue;
      }

      String reqNo = String.valueOf(jo.optString(HATSTRLayout.GYONO.getId())) + "行目：";

      for (HATSTRLayout colinf : targetCol) {

        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    targetCol = null;
    targetCol = new HATSTRLayout[] {HATSTRLayout.TENCD, HATSTRLayout.SHNCD, HATSTRLayout.SHUNO};
    for (int i = 0; i < dataArrayJ.size(); i++) {
      JSONObject jo = dataArrayJ.optJSONObject(i);

      if (jo.size() == 0) {
        continue;
      }

      String reqNo = String.valueOf(jo.optString(HATSTRLayout.GYONO.getId())) + "行目：";

      for (HATSTRLayout colinf : targetCol) {

        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // 正規：店マスタ、正規定量_商品存在チェック
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);

      if (jo.size() == 0) {
        continue;
      }

      String reqNo = String.valueOf(jo.optString(HATSTRLayout.GYONO.getId())) + "行目：";

      // 正規定量_商品テーブルに存在するか
      if (!checkHatstrShn(jo)) {
        JSONObject o = mu.getDbMessageObj("E20062", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 店マスタに存在するか
      if (!checkMstTen(jo)) {
        JSONObject o = mu.getDbMessageObj("E11096", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // 正規：店マスタ、正規定量_商品存在チェック・週№チェック
    for (int i = 0; i < dataArrayJ.size(); i++) {
      JSONObject jo = dataArrayJ.optJSONObject(i);

      if (jo.size() == 0) {
        continue;
      }

      String reqNo = String.valueOf(jo.optString(HATSTRLayout.GYONO.getId())) + "行目：";

      // 正規定量_商品テーブルに存在するか
      if (!checkHatstrShn(jo)) {
        JSONObject o = mu.getDbMessageObj("E20062", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 店マスタに存在するか
      if (!checkMstTen(jo)) {
        JSONObject o = mu.getDbMessageObj("E11096", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 週Noに存在するか
      if (!checkAddSHUNO(jo)) {
        JSONObject o = mu.getDbMessageObj("E11302", new String[] {reqNo + HATSTRLayout.SHUNO.getText()});
        msg.add(o);
        return msg;
      }


      // 週№チェック
      sbSQL = new StringBuffer();
      sbSQL.append("WITH SHORI AS (  ");
      sbSQL.append("SELECT ");
      sbSQL.append("DAYOFWEEK(DATE_FORMAT(VALUE, '%Y%m%d')) AS DT ");
      sbSQL.append(",DATE_FORMAT(DATE_FORMAT(VALUE, '%Y%m%d'), '%Y%m%d') AS N ");
      sbSQL.append(",DATE_FORMAT(DATE_FORMAT(VALUE, '%Y%m%d') + INTERVAL 7 DAY, '%Y%m%d') AS N1 ");
      sbSQL.append("FROM(  ");
      sbSQL.append("SELECT ");
      sbSQL.append("SHORIDT as VALUE  ");
      sbSQL.append("FROM ");
      sbSQL.append("INAAD.SYSSHORIDT  ");
      sbSQL.append("WHERE ");
      sbSQL.append("COALESCE(UPDKBN, 0) <> 1  ");
      sbSQL.append("ORDER BY ");
      sbSQL.append("ID desc LIMIT 1 ) as T1 ) ");
      sbSQL.append("SELECT ");
      sbSQL.append("SHUNO.SHUNO ");
      sbSQL.append(",SHORI.DT ");
      sbSQL.append("FROM ");
      sbSQL.append("INAAD.SYSSHUNO AS SHUNO ");
      sbSQL.append(",SHORI  ");
      sbSQL.append("WHERE ");
      sbSQL.append("(SHORI.DT IN (2,3) AND SHORI.N BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT) OR ");
      sbSQL.append("(SHORI.DT NOT IN (2,3) AND SHORI.N1 BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT) ");

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() > 0) {
        if (jo.optInt(HATSTRLayout.SHUNO.getId()) <= dbDatas.getJSONObject(0).optInt("SHUNO")) {
          if (dbDatas.getJSONObject(0).optInt("DT") == 2 || dbDatas.getJSONObject(0).optInt("DT") == 3) {
            msg.add(mu.getDbMessageObj("EX1093", new String[] {reqNo}));
          } else {
            msg.add(mu.getDbMessageObj("EX1094", new String[] {reqNo}));
          }
          return msg;
        }
      }

    }
    return msg;
  }

  // 存在チェック
  public boolean checkHatstrShn(JSONObject jo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String shnCd = jo.optString(HATSTRLayout.SHNCD.getId());
    String binKbn = "1";

    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(shnCd);
    }

    if (StringUtils.isEmpty(binKbn)) {
      sqlWhere += "BINKBN=null AND ";
    } else {
      sqlWhere += "BINKBN=? AND ";
      paramData.add(binKbn);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHNCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_SHN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された商品コードで検索
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      return false;
    }
    return true;
  }

  // 存在チェック
  public boolean checkMstTen(JSONObject jo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String tenCd = jo.optString(HATSTRLayout.TENCD.getId());

    if (StringUtils.isEmpty(tenCd)) {
      sqlWhere += "TENCD=null AND ";
    } else {
      sqlWhere += "TENCD=? AND ";
      paramData.add(tenCd);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("TENCD "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAMS.MSTTEN ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された店コードで検索
    sbSQL.append("MISEUNYOKBN<>9 AND ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      return false;
    }
    return true;
  }

  // 存在チェック：週No
  public boolean checkAddSHUNO(JSONObject jo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String shuNo = jo.optString(HATSTRLayout.SHUNO.getId());

    if (StringUtils.isEmpty(shuNo)) {
      sqlWhere += "SHUNO=null ";
    } else {
      sqlWhere += "SHUNO=?";
      paramData.add(shuNo);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("SHUNO "); // レコード件数
    sbSQL.append("FROM ");
    sbSQL.append("INAAD.SYSSHUNO ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere); // 入力された週Noで検索

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() == 0) {
      return false;
    }
    return true;
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
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報(正規定量_商品)

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
    createSqlHs(data, map, userInfo);

    // 排他チェック実行
    targetTable = "INATK.HATSTR_SHN";
    targetWhere = " SHNCD=? AND BINKBN=? AND UPDKBN=?";
    targetParam.add(data.optString("F1"));
    targetParam.add(data.optString("F3"));
    targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F14"))) {
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
    createDelSqlHs(data, userInfo);

    // 排他チェック実行
    targetTable = "INATK.HATSTR_SHN";
    targetWhere = " SHNCD=? AND BINKBN=? AND UPDKBN=?";
    targetParam.add(data.optString("F1"));
    targetParam.add(data.optString("F3"));
    targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F14"))) {
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
   * 正規定量_商品、正規定量_店別数量DELETE(倫理削除)処理
   *
   * @param data
   * @param userInfo
   */
  public String createDelSqlHs(JSONObject data, User userInfo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    // 商品コード
    if (StringUtils.isEmpty(data.optString("F1"))) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(data.optString("F1"));
    }

    // 便区分
    if (StringUtils.isEmpty(data.optString("F3"))) {
      sqlWhere += "BINKBN=null";
    } else {
      sqlWhere += "BINKBN=?";
      paramData.add(data.optString("F3"));
    }

    // 正規定量_商品論理削除
    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INATK.HATSTR_SHN ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal() + ",");
    sbSQL.append("SENDFLG=0,");// 2022/04/19 追加
    sbSQL.append("OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("正規定量_商品");

    // 正規定量_店別数量論理削除
    sbSQL = new StringBuffer();
    sbSQL.append("UPDATE ");
    sbSQL.append("INATK.HATSTR_TEN ");
    sbSQL.append("SET ");
    sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal() + ",");
    sbSQL.append("SENDFLG=0,");// 2022/04/19 追加
    sbSQL.append("OPERATOR='" + userId + "'");
    sbSQL.append(",UPDDT=CURRENT_TIMESTAMP ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(paramData);
    lblList.add("正規定量_店別数量");

    return sbSQL.toString();
  }

  /**
   * 正規定量_商品、正規定量_店別数量INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlHs(JSONObject data, HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();
    JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA_HTSTR")); // 更新情報(正規定量_店別数量)

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 10; // Fxxの最大値
    for (int k = 1; k <= maxField; k++) {
      String key = "F" + String.valueOf(k);

      if (!ArrayUtils.contains(new String[] {"F2"}, key)) {
        String val = data.optString(key);
        if (StringUtils.isEmpty(val)) {
          values += "null ,";
        } else {
          values += "? ,";
          prmData.add(val);
        }
      }

      if (k == maxField) {
        values += DefineReport.ValUpdkbn.NML.getVal();
        values += ",0";
        values += ", '" + userId + "' ";
        values += ",CURRENT_TIMESTAMP ";
        values += ",CURRENT_TIMESTAMP ";
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
      }
    }

    // 正規定量_商品の登録・更新
    sbSQL.append("INSERT INTO INATK.HATSTR_SHN ( ");
    sbSQL.append("SHNCD"); // 商品コード
    sbSQL.append(",BINKBN"); // 便区分
    sbSQL.append(",TSKBN_MON"); // 訂正区分_月
    sbSQL.append(",TSKBN_TUE"); // 訂正区分_火
    sbSQL.append(",TSKBN_WED"); // 訂正区分_水
    sbSQL.append(",TSKBN_THU"); // 訂正区分_木
    sbSQL.append(",TSKBN_FRI"); // 訂正区分_金
    sbSQL.append(",TSKBN_SAT"); // 訂正区分_土
    sbSQL.append(",TSKBN_SUN"); // 訂正区分_日
    sbSQL.append(",UPDKBN");
    sbSQL.append(",SENDFLG");
    sbSQL.append(",OPERATOR");
    sbSQL.append(",ADDDT");
    sbSQL.append(",UPDDT");
    sbSQL.append(") VALUES ");
    sbSQL.append(StringUtils.join(valueData, ","));
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("SHNCD = VALUES(SHNCD)"); // 商品コード
    sbSQL.append(",BINKBN = VALUES(BINKBN)"); // 便区分
    sbSQL.append(",TSKBN_MON = VALUES(TSKBN_MON)"); // 訂正区分_月
    sbSQL.append(",TSKBN_TUE = VALUES(TSKBN_TUE)"); // 訂正区分_火
    sbSQL.append(",TSKBN_WED = VALUES(TSKBN_WED)"); // 訂正区分_水
    sbSQL.append(",TSKBN_THU = VALUES(TSKBN_THU)"); // 訂正区分_木
    sbSQL.append(",TSKBN_FRI = VALUES(TSKBN_FRI)"); // 訂正区分_金
    sbSQL.append(",TSKBN_SAT = VALUES(TSKBN_SAT)"); // 訂正区分_土
    sbSQL.append(",TSKBN_SUN = VALUES(TSKBN_SUN)"); // 訂正区分_日
    sbSQL.append(",UPDKBN = VALUES(UPDKBN)");
    sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
    sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
    sbSQL.append(",UPDDT = VALUES(UPDDT)");

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("正規定量_商品");

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    maxField = 9; // Fxxの最大値
    int len = dataArrayT.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayT.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (k == 1) {

            // 配送グループコードを追加
            values += "?, ?";
            prmData.add(data.optString("F1"));
            prmData.add(data.optString("F3"));
          }

          if (!ArrayUtils.contains(new String[] {"F2"}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += ", null";
            } else {
              values += ", ?";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += ", " + DefineReport.ValUpdkbn.NML.getVal();
            values += ",0";
            values += ", '" + userId + "' ";
            values += ",CURRENT_TIMESTAMP ";
            values += ",CURRENT_TIMESTAMP ";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.HATSTR_TEN ( ");
        sbSQL.append("SHNCD"); // 商品コード
        sbSQL.append(",BINKBN"); // 便区分
        sbSQL.append(",TENCD"); // 店コード
        sbSQL.append(",SURYO_MON"); // 店別数量_月
        sbSQL.append(",SURYO_TUE"); // 店別数量_火
        sbSQL.append(",SURYO_WED"); // 店別数量_水
        sbSQL.append(",SURYO_THU"); // 店別数量_木
        sbSQL.append(",SURYO_FRI"); // 店別数量_金
        sbSQL.append(",SURYO_SAT"); // 店別数量_土
        sbSQL.append(",SURYO_SUN"); // 店別数量_日
        sbSQL.append(",UPDKBN");
        sbSQL.append(",SENDFLG");
        sbSQL.append(",OPERATOR");
        sbSQL.append(",ADDDT");
        sbSQL.append(",UPDDT");
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("SHNCD = VALUES(SHNCD)"); // 商品コード
        sbSQL.append(",BINKBN = VALUES(BINKBN)"); // 便区分
        sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード
        sbSQL.append(",SURYO_MON = VALUES(SURYO_MON)"); // 店別数量_月
        sbSQL.append(",SURYO_TUE = VALUES(SURYO_TUE)"); // 店別数量_火
        sbSQL.append(",SURYO_WED = VALUES(SURYO_WED)"); // 店別数量_水
        sbSQL.append(",SURYO_THU = VALUES(SURYO_THU)"); // 店別数量_木
        sbSQL.append(",SURYO_FRI = VALUES(SURYO_FRI)"); // 店別数量_金
        sbSQL.append(",SURYO_SAT = VALUES(SURYO_SAT)"); // 店別数量_土
        sbSQL.append(",SURYO_SUN = VALUES(SURYO_SUN)"); // 店別数量_日
        sbSQL.append(",UPDKBN = VALUES(UPDKBN)");
        sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
        sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
        sbSQL.append(",UPDDT = VALUES(UPDDT)");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("正規定量_店別数量");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * 正規定量_店別数量(次週を含む)INSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public JSONObject createSqlHatstrCsv(JSONArray dataArray, JSONArray dataArrayJ, User userInfo) {

    JSONObject result = new JSONObject();

    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    // クリア
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    int maxField = 11; // Fxxの最大値
    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArray.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {"F4"}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += "null ,";
            } else {
              values += "? ,";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += DefineReport.ValUpdkbn.NML.getVal();
            values += ",0 ";
            values += ", '" + userId + "' ";
            values += ", CURRENT_TIMESTAMP ";
            values += ", CURRENT_TIMESTAMP ";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.HATSTR_TEN ( ");
        sbSQL.append("SHNCD"); // 商品コード
        sbSQL.append(",BINKBN"); // 便区分
        sbSQL.append(",TENCD"); // 店コード
        sbSQL.append(",SURYO_MON"); // 店別数量_月
        sbSQL.append(",SURYO_TUE"); // 店別数量_火
        sbSQL.append(",SURYO_WED"); // 店別数量_水
        sbSQL.append(",SURYO_THU"); // 店別数量_木
        sbSQL.append(",SURYO_FRI"); // 店別数量_金
        sbSQL.append(",SURYO_SAT"); // 店別数量_土
        sbSQL.append(",SURYO_SUN"); // 店別数量_日
        sbSQL.append(",UPDKBN");
        sbSQL.append(",SENDFLG");
        sbSQL.append(",OPERATOR");
        sbSQL.append(",ADDDT");
        sbSQL.append(",UPDDT");
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("SHNCD = VALUES(SHNCD)"); // 商品コード
        sbSQL.append(",BINKBN = VALUES(BINKBN)"); // 便区分
        sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード
        sbSQL.append(",SURYO_MON = VALUES(SURYO_MON)"); // 店別数量_月
        sbSQL.append(",SURYO_TUE = VALUES(SURYO_TUE)"); // 店別数量_火
        sbSQL.append(",SURYO_WED = VALUES(SURYO_WED)"); // 店別数量_水
        sbSQL.append(",SURYO_THU = VALUES(SURYO_THU)"); // 店別数量_木
        sbSQL.append(",SURYO_FRI = VALUES(SURYO_FRI)"); // 店別数量_金
        sbSQL.append(",SURYO_SAT = VALUES(SURYO_SAT)"); // 店別数量_土
        sbSQL.append(",SURYO_SUN = VALUES(SURYO_SUN)"); // 店別数量_日
        sbSQL.append(",UPDKBN = VALUES(UPDKBN)");
        sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
        sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
        sbSQL.append(",UPDDT = VALUES(UPDDT)");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("正規定量_店別数量");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    len = dataArrayJ.size();
    for (int i = 0; i < len; i++) {
      JSONObject dataT = dataArrayJ.getJSONObject(i);
      if (!dataT.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          if (!ArrayUtils.contains(new String[] {}, key)) {
            String val = dataT.optString(key);
            if (StringUtils.isEmpty(val)) {
              values += "null ,";
            } else {
              values += "? ,";
              prmData.add(val);
            }
          }

          if (k == maxField) {
            values += "0 ";
            values += ", '" + userId + "' ";
            values += ", CURRENT_TIMESTAMP ";
            values += ", CURRENT_TIMESTAMP ";
            valueData = ArrayUtils.add(valueData, "(" + values + ")");
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        sbSQL.append("INSERT INTO INATK.HATJTR_TEN ( ");
        sbSQL.append("SHNCD"); // 商品コード
        sbSQL.append(",BINKBN"); // 便区分
        sbSQL.append(",TENCD"); // 店コード
        sbSQL.append(",SHUNO"); // 週№
        sbSQL.append(",SURYO_MON"); // 店別数量_月
        sbSQL.append(",SURYO_TUE"); // 店別数量_火
        sbSQL.append(",SURYO_WED"); // 店別数量_水
        sbSQL.append(",SURYO_THU"); // 店別数量_木
        sbSQL.append(",SURYO_FRI"); // 店別数量_金
        sbSQL.append(",SURYO_SAT"); // 店別数量_土
        sbSQL.append(",SURYO_SUN"); // 店別数量_日
        sbSQL.append(",SENDFLG");
        sbSQL.append(",OPERATOR");
        sbSQL.append(",ADDDT");
        sbSQL.append(",UPDDT");
        sbSQL.append(") VALUES ");
        sbSQL.append(StringUtils.join(valueData, ","));
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
        sbSQL.append("SHNCD = VALUES(SHNCD)"); // 商品コード
        sbSQL.append(",BINKBN = VALUES(BINKBN)"); // 便区分
        sbSQL.append(",TENCD = VALUES(TENCD)"); // 店コード
        sbSQL.append(",SHUNO = VALUES(SHUNO)"); // 週№
        sbSQL.append(",SURYO_MON = VALUES(SURYO_MON)"); // 店別数量_月
        sbSQL.append(",SURYO_TUE = VALUES(SURYO_TUE)"); // 店別数量_火
        sbSQL.append(",SURYO_WED = VALUES(SURYO_WED)"); // 店別数量_水
        sbSQL.append(",SURYO_THU = VALUES(SURYO_THU)"); // 店別数量_木
        sbSQL.append(",SURYO_FRI = VALUES(SURYO_FRI)"); // 店別数量_金
        sbSQL.append(",SURYO_SAT = VALUES(SURYO_SAT)"); // 店別数量_土
        sbSQL.append(",SURYO_SUN = VALUES(SURYO_SUN)"); // 店別数量_日
        sbSQL.append(",SENDFLG = VALUES(SENDFLG)");
        sbSQL.append(",OPERATOR = VALUES(OPERATOR)");
        sbSQL.append(",UPDDT = VALUES(UPDDT)");

        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
        }

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("次週定量_店別数量");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return result;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szShncd = getMap().get("SHNCD"); // 商品コード
    String szBinkbn = getMap().get("BINKBN"); // 便区分

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";

    if (StringUtils.isEmpty(szShncd)) {
      sqlWhere += "SHN.SHNCD=null AND ";
    } else {
      sqlWhere += "SHN.SHNCD=? AND ";
      paramData.add(szShncd);
    }

    if (StringUtils.isEmpty(szBinkbn)) {
      sqlWhere += "SHN.BINKBN=null AND ";
    } else {
      sqlWhere += "SHN.BINKBN=? AND ";
      paramData.add(szBinkbn);
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("SELECT ");
    sbSQL.append("SHN.SHNCD "); // F1 : 商品コード
    sbSQL.append(",MST.SHNKN "); // F2 : 商品名
    sbSQL.append(",SHN.BINKBN "); // F3 : 便区分
    sbSQL.append(",replace(SHN.TSKBN_MON, '0', '')"); // F4 : 訂正区分＿月
    sbSQL.append(",replace(SHN.TSKBN_TUE, '0', '')"); // F5 : 訂正区分＿火
    sbSQL.append(",replace(SHN.TSKBN_WED, '0', '')"); // F6 : 訂正区分＿水
    sbSQL.append(",replace(SHN.TSKBN_THU, '0', '')"); // F7 : 訂正区分＿木
    sbSQL.append(",replace(SHN.TSKBN_FRI, '0', '')"); // F8 : 訂正区分＿金
    sbSQL.append(",replace(SHN.TSKBN_SAT, '0', '')"); // F9 : 訂正区分＿土
    sbSQL.append(",replace(SHN.TSKBN_SUN, '0', '')"); // F10 : 訂正区分＿日
    sbSQL.append(",SHN.OPERATOR "); // F11 : オペレーター
    sbSQL.append(",DATE_FORMAT(SHN.ADDDT,'%y/%m/%d') AS ADDDT "); // F12 : 登録日
    sbSQL.append(",DATE_FORMAT(SHN.UPDDT,'%y/%m/%d') AS UPDDT "); // F13 : 更新日
    sbSQL.append(",DATE_FORMAT(SHN.UPDDT,'%Y%m%d%H%i%s%f') as HDN_UPDDT "); // F14 : 更新日時
    sbSQL.append("FROM ");
    sbSQL.append("INATK.HATSTR_SHN SHN LEFT JOIN INAMS.MSTSHN MST ON SHN.SHNCD=MST.SHNCD ");
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);
    sbSQL.append("SHN.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + getClass().getSimpleName() + "[sql]*/ " + sbSQL.toString());
    }
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }

  /** 正規定量_店別数量(次週も含む) */
  public enum HATSTRLayout implements MSTLayout {

    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 便区分 */
    BINKBN(2, "BINKBN", "SMALLINT", "便区分"),
    /** 店 */
    TENCD(3, "TENCD", "SMALLINT", "店コード"),
    /** 週No. */
    SHUNO(4, "SHUNO", "SMALLINT", "週No"),
    /** 数量_月 */
    SURYO_MON(5, "SURYO_MON", "INTEGER", "数量_月"),
    /** 数量_火 */
    SURYO_TUE(6, "SURYO_TUE", "INTEGER", "数量_火"),
    /** 数量_水 */
    SURYO_WED(7, "SURYO_WED", "INTEGER", "数量_水"),
    /** 数量_木 */
    SURYO_THU(8, "SURYO_THU", "INTEGER", "数量_木"),
    /** 数量_金 */
    SURYO_FRI(9, "SURYO_FRI", "INTEGER", "数量_金"),
    /** 数量_土 */
    SURYO_SAT(10, "SURYO_SAT", "INTEGER", "数量_土"),
    /** 数量_日 */
    SURYO_SUN(11, "SURYO_SUN", "INTEGER", "数量_日"),
    /** 行番号 */
    GYONO(12, "GYONO", "INTEGER", "行番号");

    private final Integer no;
    private final String col;
    private final String typ;
    private final String text;

    /** 初期化 */
    private HATSTRLayout(Integer no, String col, String typ, String text) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.text = text;
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

    /** @return tbl 列名(論理名称) */
    public String getText() {
      return text;
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
}
