package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.InputChecker;
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
public class Reportx142Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx142Dao(String JNDIname) {
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

  private String createCommand() {

    StringBuffer sbSQL = new StringBuffer();
    return sbSQL.toString();
  }

  boolean isTest = false;

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

    CmnDate.dbDateFormat(sysdate);
    JSONObject option = new JSONObject();
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    ArrayList<String> valData = new ArrayList<String>();
    String values = "";
    String userId = userInfo.getId();

    String obj = map.get(DefineReport.ID_PARAM_OBJ);
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報
    JSONArray dataArray2 = JSONArray.fromObject(map.get("DATA_TEN")); // 対象店舗
    JSONArray dataArray3 = JSONArray.fromObject(map.get("DATA_KIKAN")); // 対象期間
    JSONObject msgObj = new JSONObject();
    JSONArray msg = new JSONArray();

    System.out.println("dataArray : " + dataArray);
    System.out.println("dataArray2 : " + dataArray2);
    System.out.println("dataArray3 : " + dataArray3);


    if (DefineReport.Button.UPD_TAB1.getObj().equals(obj)) {

      for (int j = 0; j < dataArray2.size(); j++) {
        for (int i = 1; i <= 3; i++) {
          String col = "F" + i;
          String val = dataArray.optJSONObject(0).optString(col);
          if (i < 3) {
            prmData.add(val);
            valData.add(val);
          } else {
            val = dataArray2.optJSONObject(j).optString("F3");
            prmData.add(val);
            valData.add(val);
          }
        }

        if (j == 0) {
          values += "( ? ,? ,? ";
        } else {
          values += ",( ? ,? ,? ";
        }
        values += ", " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + " ";
        values += ", '" + userId + "' ";
        values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values += "FROM INAMS.MSTTENKYU WHERE TENCD = " + valData.get(2) + " ";
        values += "AND TENKYUDT = " + valData.get(1) + " LIMIT 1 ) T2 ) ";
        values += ",CURRENT_TIMESTAMP )";
        valData.clear();
      }

    } else if (DefineReport.Button.UPD_TAB2.getObj().equals(obj)) {

      for (int j = 0; j < dataArray3.size(); j++) {
        String f4 = dataArray.optJSONObject(0).optString("F4");
        String f6 = dataArray.optJSONObject(0).optString("F6");
        String val = dataArray3.optJSONObject(j).optString("F5");
        prmData.add(f4);
        valData.add(f4);
        prmData.add(val);
        valData.add(val);
        prmData.add(f6);
        valData.add(f6);

        if (j == 0) {
          values += "( ? ,? ,? ";
        } else {
          values += ",( ? ,? ,? ";
        }
        values += ", " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", " + DefineReport.Values.SENDFLG_UN.getVal() + " ";
        values += ", '" + userId + "' ";
        values += ", ( SELECT * FROM ( SELECT CASE WHEN COUNT(*) = 0 OR IFNULL(UPDKBN,0) = 1 THEN CURRENT_TIMESTAMP ELSE ADDDT END AS ADDDT ";
        values += "FROM INAMS.MSTTENKYU WHERE TENCD = " + valData.get(2) + " ";
        values += "AND TENKYUDT = " + valData.get(1) + " LIMIT 1 ) T2 ) ";
        values += ",CURRENT_TIMESTAMP )";
        valData.clear();
      }
    }

    values = StringUtils.removeStart(values, ",");
    // 基本INSERT/UPDATE文
    StringBuffer sbSQL;

    // 更新SQL
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO INAMS.MSTTENKYU (");
    sbSQL.append("TENKYUFLG"); // F1 : 店休日
    sbSQL.append(",TENKYUDT"); // F2 : 店コード
    sbSQL.append(",TENCD"); // F3 : 店休フラグ
    sbSQL.append(",UPDKBN"); // F4 : 更新区分：
    sbSQL.append(",SENDFLG"); // F5 : 送信フラグ：
    sbSQL.append(",OPERATOR "); // F6 : オペレーター：
    sbSQL.append(",ADDDT "); // F7 : 登録日： current timestamp or
    sbSQL.append(",UPDDT "); // F8 : 更新日： current timestamp
    sbSQL.append(") VALUES");
    sbSQL.append(" " + values + " ");


    String rownum = ""; // エラー行数
    String targetTable = " INAMS.MSTTENKYU ";
    String targetWhere = "";
    ArrayList<String> targetParam = new ArrayList<String>();
    JSONObject data = new JSONObject();
    JSONObject data2 = new JSONObject();
    if (DefineReport.Button.UPD_TAB1.getObj().equals(obj)) {
      dataArray = JSONArray.fromObject(map.get("DATA_TEN")); // 対象情報
      dataArray2 = JSONArray.fromObject(map.get("DATA")); // 対象情報
      targetWhere = " IFNULL(UPDKBN, 0) <> " + DefineReport.ValUpdkbn.DEL.getVal() + " and TENKYUDT = ? and TENCD = ?";
      for (int i = 0; i < dataArray.size(); i++) {
        data = dataArray.getJSONObject(i);
        data2 = dataArray2.getJSONObject(0);
        if (data.isEmpty()) {
          continue;
        }

        targetParam = new ArrayList<String>();
        targetParam.add(data2.optString("F1"));
        targetParam.add(data.optString("F3"));

        if (!(StringUtils.isEmpty(data.optString("F3")) && StringUtils.isEmpty(data2.optString("F1")))) {
          if (!super.checkExclusion(targetTable, targetWhere, targetParam, "")) {
            rownum = data.optString("IDX");
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[] {rownum}));
            option.put(MsgKey.E.getKey(), msg);
            return option;
          }
        }
      }
    } else if (DefineReport.Button.UPD_TAB2.getObj().equals(obj)) {

      dataArray = JSONArray.fromObject(map.get("DATA_KIKAN")); // 対象情報
      dataArray2 = JSONArray.fromObject(map.get("DATA")); // 対象情報
      targetWhere = " IFNULL(UPDKBN, 0) <> " + DefineReport.ValUpdkbn.DEL.getVal() + " and TENCD = ? and TENKYUDT = ? ";
      for (int i = 0; i < dataArray.size(); i++) {
        data = dataArray.getJSONObject(i);
        data2 = dataArray2.getJSONObject(0);
        if (data.isEmpty()) {
          continue;
        }

        targetParam = new ArrayList<String>();
        targetParam.add(data2.optString("F6"));
        targetParam.add(data.optString("F5"));

        if (!(StringUtils.isEmpty(data.optString("F5")) && StringUtils.isEmpty(data2.optString("F6")))) {
          if (!super.checkExclusion(targetTable, targetWhere, targetParam, "")) {
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, ""));
            option.put(MsgKey.E.getKey(), msg);
            return option;
          }
        }
      }
    }

    if (msg.size() > 0) {
      // 重複チェック_エラー時
      msgObj.put(MsgKey.E.getKey(), msg);
    } else {
      // 更新処理実行
      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(sbSQL);
      int count = super.executeSQL(sbSQL.toString(), prmData);
      if (StringUtils.isEmpty(getMessage())) {
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println("店休マスタを " + MessageUtility.getMessage(Msg.S00003.getVal(), new String[] {Integer.toString(dataArray.size()), Integer.toString(count)}));
        msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      } else {
        msgObj.put(MsgKey.E.getKey(), getMessage());
      }
    }
    return msgObj;
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


  /** 店舗休日(日付複数店舗)マスタレイアウト */
  public enum TENKYUTENPOLayout implements MSTLayout {
    /** 日付 */
    TENKYUDT(2, "TENKYUDT", "DATE"),
    /** 店舗コード */
    TENCD(3, "TENCD", "SMALLINT");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TENKYUTENPOLayout(Integer no, String col, String typ) {
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
  /** 店舗休日(店舗期間)マスタレイアウト */
  public enum TENKYUKIKANLayout implements MSTLayout {
    /** 店コード */
    TENCD(4, "TENCD", "SMALLINT"),
    /** 日付 */
    TENKYUDT(5, "TENKYUDT", "DATE");


    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private TENKYUKIKANLayout(Integer no, String col, String typ) {
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
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    map.get("SENDBTNID");

    MessageUtility mu = new MessageUtility();
    // ①正 .新規
    boolean isNew = false;

    List<JSONObject> msgList = this.checkData(isNew, map, userInfo, sysdate, mu, dataArray);

    JSONArray msgArray = new JSONArray();

    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public List<JSONObject> checkData(boolean isNew, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray // 対象情報（主要な更新情報）
  ) {
    JSONArray msg = new JSONArray();
    JSONObject data = dataArray.optJSONObject(0);

    JSONArray dataArray2 = JSONArray.fromObject(map.get("DATA_TEN"));
    JSONArray dataArray3 = JSONArray.fromObject(map.get("DATA_KIKAN"));
    String obj = map.get(DefineReport.ID_PARAM_OBJ);
    if (DefineReport.Button.UPD_TAB1.getObj().equals(obj)) {
      // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
      // 1.店舗休日マスタ(複数店舗)
      for (TENKYUTENPOLayout colinf : TENKYUTENPOLayout.values()) {
        if (!colinf.getId().equals("F3")) {
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
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
              msg.add(o);
              return msg;
            }
          }
        } else {
          for (int j = 0; j < dataArray2.size(); j++) {
            String val = StringUtils.trim(dataArray2.optJSONObject(j).optString(colinf.getId()));
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
                msg.add(o);
                return msg;
              }
              // ②データ桁チェック
              if (!InputChecker.checkDataLen(dtype, val, digit)) {
                JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
                msg.add(o);
                return msg;
              }
            }
          }
        }
      }

      TENKYUTENPOLayout[] targetCol = null;

      targetCol = new TENKYUTENPOLayout[] {TENKYUTENPOLayout.TENKYUDT, TENKYUTENPOLayout.TENCD};

      for (TENKYUTENPOLayout colinf : targetCol) {
        if (!colinf.getId().equals("F3")) {
          if (StringUtils.isEmpty(data.optString(colinf.getId()))) {
            JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
            msg.add(o);
            return msg;
          }
        } else {
          for (int j = 0; j < dataArray2.size(); j++) {
            if (StringUtils.isEmpty(dataArray2.optJSONObject(j).optString(colinf.getId()))) {
              JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
              msg.add(o);
              return msg;
            }
          }
        }
      }
    } else if (DefineReport.Button.UPD_TAB2.getObj().equals(obj)) {
      // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
      // 1.店舗休日マスタ(期間)
      for (TENKYUKIKANLayout colinf : TENKYUKIKANLayout.values()) {
        if (!colinf.getId().equals("F5")) {
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
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
              msg.add(o);
              return msg;
            }
          }
        } else {
          for (int j = 0; j < dataArray3.size(); j++) {
            String val = StringUtils.trim(dataArray3.optJSONObject(j).optString(colinf.getId()));
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
                msg.add(o);
                return msg;
              }
              // ②データ桁チェック
              if (!InputChecker.checkDataLen(dtype, val, digit)) {
                JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
                msg.add(o);
                return msg;
              }
            }
          }
        }
      }
      TENKYUKIKANLayout[] targetCol = null;

      targetCol = new TENKYUKIKANLayout[] {TENKYUKIKANLayout.TENKYUDT, TENKYUKIKANLayout.TENCD};

      for (TENKYUKIKANLayout colinf : targetCol) {
        if (!colinf.getId().equals("F5")) {
          if (StringUtils.isEmpty(data.optString(colinf.getId()))) {
            JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
            msg.add(o);
            return msg;
          }
        } else {
          for (int j = 0; j < dataArray3.size(); j++) {
            if (StringUtils.isEmpty(dataArray3.optJSONObject(j).optString(colinf.getId()))) {
              JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
              msg.add(o);
              return msg;
            }
          }
        }
      }
    }
    return msg;
  }
}
