/**
 *
 */
package dao;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.iq80.snappy.CorruptionException;
import org.iq80.snappy.Snappy;
import authentication.bean.User;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ItemDao implements ItemInterface {

  /** JNDI */
  protected String JNDIname;

  /** パラメータ関係 */
  private HashMap<String, String> map;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getMap()
   */
  @Override
  public HashMap<String, String> getMap() {
    return map;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setMap(java.util.HashMap)
   */
  @Override
  public void setMap(HashMap<String, String> map) {
    this.map = map;
  }

  /** json 情報 */
  private String json;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getJson()
   */
  @Override
  public String getJson() {
    return json;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setJson(java.lang.String)
   */
  @Override
  public void setJson(String json) {
    this.json = json;
  }

  /** 検索条件（Excel出力用） */
  private ArrayList<List<String>> where;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getWhere()
   */
  @Override
  public ArrayList<List<String>> getWhere() {
    return where;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setWhere(java.util.ArrayList)
   */
  @Override
  public void setWhere(ArrayList<List<String>> where) {
    this.where = where;
  }

  /** メタ情報（Excel出力用） */
  private ArrayList<Integer> meta;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getMeta()
   */
  @Override
  public ArrayList<Integer> getMeta() {
    return meta;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setMeta(java.util.ArrayList)
   */
  @Override
  public void setMeta(ArrayList<Integer> meta) {
    this.meta = meta;
  }

  /** 開始レコード */
  private int start;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getStart()
   */
  @Override
  public int getStart() {
    return start;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setStart(int)
   */
  @Override
  public void setStart(int start) {
    this.start = start;
  }

  /** 取得レコード数 */
  private int limit;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getLimit()
   */
  @Override
  public int getLimit() {
    return limit;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setLimit(int)
   */
  @Override
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /** DB検索用パラメータ配列 */
  private ArrayList<String> paramData = new ArrayList<String>();

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getParamData()
   */
  @Override
  public ArrayList<String> getParamData() {
    return paramData;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setParamData(java.util.ArrayList)
   */
  @Override
  public void setParamData(ArrayList<String> paramData) {
    this.paramData = paramData;
  }

  /** DB検索結果（0レコード＝タイトル） */
  private ArrayList<byte[]> table;

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#getTable()
   */
  @Override
  public ArrayList<byte[]> getTable() {
    return table;
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#setTable(java.util.ArrayList)
   */
  @Override
  public void setTable(ArrayList<byte[]> table) {
    this.table = table;
  }

  /**
   * 新しいjdbcItemDaoのインスタンスを生成します。
   *
   * @param source
   */
  public ItemDao(String JNDIname) {

    this.JNDIname = JNDIname;

    // 保存用 List (レコード情報)作成
    table = new ArrayList<byte[]>();

    // 配列準備
    paramData = new ArrayList<String>();

    // メタ情報
    meta = new ArrayList<Integer>();
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#selectBy()
   */
  @Override
  public boolean selectBy() {
    return selectBySQL("");
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#selectForDL()
   */
  @Override
  public boolean selectForDL() {
    return selectBySQL("");
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#selectBySQL(java.lang.String)
   */
  @Override
  public boolean selectBySQL(String command) {
    return selectBySQL(command, true);
  }

  /*
   * (non-Javadoc)
   *
   * @see dao.ItemInterface#selectBySQL(java.lang.String)
   */
  public boolean selectBySQL(String command, boolean setDbTitle) {

    // コネクションの取得
    Connection con = null;
    try {
      con = DBConnection.getConnection(this.JNDIname);
    } catch (Exception e1) {
      e1.printStackTrace();
      return false;
    }

    ResultSet rs = null;
    PreparedStatement statement = null;
    long startTime, stop, diff;

    if ("".equals(command)) {
      return false;
    }
    try {

      // 実行SQL設定
      statement = con.prepareStatement(command);

      // パラメータ判断
      // setParamData(new ArrayList<String>());
      for (int i = 0; i < getParamData().size(); i++) {
        statement.setString((i + 1), getParamData().get(i));
      }

      startTime = System.currentTimeMillis();

      // SQL実行
      rs = statement.executeQuery();

      stop = System.currentTimeMillis();
      diff = stop - startTime;
      // 現在日時情報で初期化されたインスタンスの取得
      LocalDateTime nowDateTime = LocalDateTime.now();
      DateTimeFormatter java8Format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
      // 日時情報を指定フォーマットの文字列で取得
      String java8Disp = nowDateTime.format(java8Format);
      // 日付時刻情報 システム識別 レポート情報 ユーザー情報 実行時間 SQL パラメータ
      System.out.println("/* " + java8Disp + "\t[MDM]\t" + getMap().get("report") + "\t" + userInfo.getId() + "\t" + diff + "*/\t" + command + ";\t/*" + getParamData().toString() + " */");

      this.setTable(new ArrayList<byte[]>());

      // カラム数
      ResultSetMetaData rsmd = rs.getMetaData();
      int sizeColumn = rsmd.getColumnCount();

      // タイトル名称取得
      List<String> titles = new ArrayList<String>();
      for (int i = 1; i <= sizeColumn; i++) {
        titles.add(rsmd.getColumnName(i));
      }
      if (setDbTitle) {
        getTable().add(Snappy.compress(StringUtils.join(titles.toArray(new String[titles.size()]), "\t").getBytes("UTF-8")));
      }

      // メタ情報（名称）
      for (int i = 1; i <= sizeColumn; i++) {
        meta.add(rsmd.getColumnType(i));// 列の SQL 型
      }

      // 結果の取得
      DecimalFormat df = new DecimalFormat();
      df.setMaximumFractionDigits(1);
      df.setMinimumFractionDigits(1);

      while (rs.next()) {

        // 情報保存
        List<String> cols = new ArrayList<String>();

        for (int i = 1; i <= sizeColumn; i++) {

          // タイプ別取得
          switch (rsmd.getColumnType(i)) {
            case Types.DECIMAL:
              if (null == rs.getString(i)) {
                cols.add("");
              } else if (rsmd.getScale(i) == 0 && rs.getDouble(i) <= 2147483647 && rs.getDouble(i) >= -2147483647) {
                cols.add(Integer.toString((int) rs.getDouble(i)));
              } else {
                cols.add(String.valueOf(rs.getDouble(i)));
              }
              break;
            case Types.INTEGER:
              if (null == rs.getString(i)) {
                cols.add("");
              } else {
                cols.add(String.valueOf(rs.getInt(i)));
              }
              break;
            default:
              if (null == rs.getString(i)) {
                cols.add("");
              } else {
                cols.add(rs.getString(i));
              }
          }

        }

        // 情報保存（レコード）
        getTable().add(Snappy.compress(StringUtils.join(cols.toArray(new String[cols.size()]), "\t").getBytes("UTF-8")));
      }

    } catch (SQLException e) {
      rollback(con);
      e.printStackTrace();
      if (DefineReport.ID_SQLSTATE_COLUMN_GREATER.equals(e.getSQLState()) || DefineReport.ID_SQLSTATE_APPLICATION_HEPE.equals(e.getSQLState())
          || DefineReport.ID_SQLSTATE_BUFFER_GREATER.equals(e.getSQLState()) || DefineReport.ID_SQLSTATE_COLUMN_OVER.equals(e.getSQLState())) {
        // 横軸（列）が多すぎる場合
        setMessage(DefineReport.ID_MSG_COLUMN_GREATER + "(" + e.getSQLState() + ")");
      } else if (DefineReport.ID_SQLSTATE_CONNECTION_RESET.equals(e.getSQLState())) {
        // 通信切断
        setMessage(DefineReport.ID_MSG_CONNECTION_REST + "(" + e.getSQLState() + ")");
      } else {
        // その他SQLエラー
        setMessage(DefineReport.ID_MSG_SQL_EXCEPTION + e.getMessage());
      }

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      close(rs);
      close(statement);
      close(con);
    }
    return false;
  }

  /**
   * 更新処理
   *
   * @param sqlCommand
   * @param paramData
   * @param jdbcName
   * @return 実行件数
   * @throws Exception
   */
  public Integer executeSQL(String command, ArrayList<String> paramData) {
    int count = 0;


    // コネクションの取得
    Connection con = null;
    try {
      con = DBConnection.getConnection(this.JNDIname);
    } catch (Exception e1) {
      e1.printStackTrace();
      return count;
    }

    PreparedStatement statement = null;
    long startTime, stop, diff;

    if ("".equals(command)) {
      return count;
    }

    try {
      con.setAutoCommit(false);

      // 実行SQL設定
      statement = con.prepareStatement(command);

      // パラメータ設定
      for (int i = 0; i < paramData.size(); i++) {
        statement.setString((i + 1), paramData.get(i));
      }
      startTime = System.currentTimeMillis();

      // SQL実行
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* [sql] */ " + command + ";/* [prm] " + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
      }
      // SQL実行
      count = statement.executeUpdate();

      stop = System.currentTimeMillis();
      diff = stop - startTime;
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("TIME:" + diff + " ms" + " COUNT:" + count);
      }

      con.commit();

    } catch (SQLException e) {
      count = 0;
      rollback(con);
      e.printStackTrace();
      if (DefineReport.ID_SQLSTATE_CONNECTION_RESET.equals(e.getSQLState())) {
        // 通信切断
        setMessage(DefineReport.ID_MSG_CONNECTION_REST + "(" + e.getSQLState() + ")");
      } else {
        // その他SQLエラー
        setMessage(DefineReport.ID_MSG_SQL_EXCEPTION + e.getMessage());
      }

    } catch (Exception e) {
      count = 0;
      e.printStackTrace();

    } finally {
      close(statement);
      close(con);
    }
    return count;
  }

  /**
   * 更新処理
   *
   * @param sqlCommands
   * @param paramDatas
   * @param jdbcName
   * @return 実行件数
   * @throws Exception
   */
  public ArrayList<Integer> executeSQLs(ArrayList<String> commands, ArrayList<ArrayList<String>> paramDatas) throws Exception {
    ArrayList<Integer> countList = new ArrayList<Integer>();

    // コネクションの取得
    Connection con = null;
    try {
      con = DBConnection.getConnection(this.JNDIname);
    } catch (Exception e1) {
      e1.printStackTrace();
      return countList;
    }

    PreparedStatement statement = null;
    long startTime, stop, diff;

    try {
      con.setAutoCommit(false);

      for (int index = 0; index < commands.size(); index++) {
        String command = commands.get(index);
        ArrayList<String> paramData = paramDatas.get(index);
        // 実行SQL設定
        statement = con.prepareStatement(command);

        // パラメータ設定
        for (int i = 0; i < paramData.size(); i++) {
          statement.setString((i + 1), paramData.get(i));
        }
        startTime = System.currentTimeMillis();

        // SQL実行
        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("/* [sql] */" + command + "; /* [prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + "[statement] */");
        }
        // SQL実行
        int count = statement.executeUpdate();
        // SQL実行後にstatementをcloseする。
        close(statement);
        countList.add(count);

        stop = System.currentTimeMillis();
        diff = stop - startTime;
        if (DefineReport.ID_DEBUG_MODE) {
          System.out.println("TIME:" + diff + " ms" + " COUNT:" + count);
        }
      }

      con.commit();
    } catch (SQLException e) {
      countList = new ArrayList<Integer>();
      rollback(con);
      e.printStackTrace();
      if (DefineReport.ID_SQLSTATE_CONNECTION_RESET.equals(e.getSQLState())) {
        // 通信切断
        setMessage(DefineReport.ID_MSG_CONNECTION_REST + "(" + e.getSQLState() + ")");
      } else {
        // その他SQLエラー
        setMessage(DefineReport.ID_MSG_SQL_EXCEPTION + e.getMessage());
      }

    } catch (Exception e) {
      countList = new ArrayList<Integer>();
      e.printStackTrace();

    } finally {
      close(statement);
      close(con);
    }
    return countList;
  }

  /**
   * Web商談 更新処理
   *
   * @param sqlCommand SQLコマンド
   * @param paramData パラメータ（文字列）
   * @param con データベース・コネクション
   * @return 実行結果（行数）
   * @throws Exception
   */
  public static int updateBySQL(String sqlCommand, ArrayList<String> paramData, Connection con) throws Exception {
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* [sql] */" + sqlCommand + ";/* [prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
    }

    // 実行SQL設定
    PreparedStatement stmt = con.prepareStatement(sqlCommand);

    // パラメータ設定
    for (int i = 0; i < paramData.size(); i++) {
      stmt.setString((i + 1), paramData.get(i));
    }

    // SQL実行
    return stmt.executeUpdate();
  }

  /**
   * Web商談 INSERT処理
   *
   * @param sqlCommand
   * @param paramData
   * @param jdbcName
   * @return 自動生成されたID
   * @throws Exception
   */
  public Integer executeSQLReturnId(String command, ArrayList<String> paramData, String keyCol, int initId, String selectTable) {
    int count = 0;
    int id = 0;


    // コネクションの取得
    Connection con = null;
    try {
      con = DBConnection.getConnection(this.JNDIname);
    } catch (Exception e1) {
      e1.printStackTrace();
      return count;
    }

    PreparedStatement statement = null;
    long startTime, stop, diff;

    if ("".equals(command)) {
      return count;
    }

    try {
      con.setAutoCommit(false);

      // 実行SQL設定
      statement = con.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);

      // パラメータ設定
      for (int i = 0; i < paramData.size(); i++) {
        statement.setString((i + 1), paramData.get(i));
      }
      startTime = System.currentTimeMillis();

      // SQL実行
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* [sql] */" + command + "; /* [prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
      }
      // SQL実行
      count = statement.executeUpdate();

      stop = System.currentTimeMillis();
      diff = stop - startTime;
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("TIME:" + diff + " ms" + " COUNT:" + count);
      }

      con.commit();

      if (count > 0) {
        // try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        // if (generatedKeys.next()) {
        // // get auto-increment id
        // id = generatedKeys.getInt(1);
        // }
        // }
        id = initId;
        if (initId == 0) {
          Statement getLastIdStm = con.createStatement();
          ResultSet rs = getLastIdStm.executeQuery("SELECT MAX(" + keyCol + ") as " + keyCol + " FROM " + selectTable);
          if (rs.next()) {
            id = rs.getInt(keyCol);
          }
        }
      }

    } catch (SQLException e) {
      rollback(con);
      e.printStackTrace();
      if (DefineReport.ID_SQLSTATE_CONNECTION_RESET.equals(e.getSQLState())) {
        // 通信切断
        setMessage(DefineReport.ID_MSG_CONNECTION_REST + "(" + e.getSQLState() + ")");
      } else {
        // その他SQLエラー
        setMessage(DefineReport.ID_MSG_SQL_EXCEPTION + e.getMessage());
      }

    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      close(statement);
      close(con);
    }
    return id;
  }

  /**
   * コミットします。
   *
   * @param conn
   */
  protected void commit(Connection conn) {
    if (conn != null) {
      try {
        conn.commit();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * ロールバックします。
   *
   * @param conn
   */
  protected void rollback(Connection conn) {
    if (conn != null) {
      try {
        conn.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    }
  }

  /**
   * コネクションをクローズします。
   *
   * @param conn
   */
  protected void close(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * ステートメントをクローズします。
   *
   * @param statement
   */
  protected void close(PreparedStatement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 結果セットをクローズします。
   *
   * @param rs
   */
  protected void close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String getSelectCommand() {
    return null;
  }

  /**
   * 検索結果情報から指定列の情報を文字列（カンマ区切り）として取得
   *
   * @Override
   */
  @Override
  public String getReader(int columnNumber) {

    StringBuffer sb = new StringBuffer();

    // 検索結果情報
    ArrayList<byte[]> al = this.getTable();

    Iterator<byte[]> itr = al.iterator();
    itr.next();// タイトル部スキップ

    // カラムタイプ取得
    ArrayList<Integer> mt = this.getMeta();
    Iterator<Integer> itrMt = mt.iterator();
    int indexCol = 0;
    Integer typeCol = 0;
    while (itrMt.hasNext()) {
      indexCol++;
      if (indexCol == columnNumber) {
        typeCol = itrMt.next();
        break;
      } else {
        itrMt.next();
      }
    }

    // 検索結果読み取り
    while (itr.hasNext()) {
      if (sb.length() > 0) {
        sb.append(",");
      }

      indexCol = 0;
      // セル（列）情報リスト
      String[] columnsList;
      byte[] bytes = itr.next();
      try {
        columnsList = StringUtils.splitPreserveAllTokens(new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");
        for (String col : columnsList) {
          indexCol++;
          // 指定列のみ取得
          if (indexCol == columnNumber) {
            // カラムタイプ判定
            switch (typeCol) {
              case Types.CHAR:
              case Types.VARCHAR:
                sb.append("'" + col + "'");
                break;
              default:
                sb.append(col);
                break;
            }
            break; // while
          } else {
            // カラムのスキップ
            // itrCols.next();
          }
        }
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (CorruptionException e) {
        e.printStackTrace();
      }
    }
    return sb.toString();
  }

  /** ログインユーザー情報 */
  private User userInfo;

  /**
   * ログインユーザー情報を取得します。
   *
   * @return ログインユーザー情報
   */
  @Override
  public User getUserInfo() {
    return userInfo;
  }

  /**
   * ログインユーザー情報を設定します。
   *
   * @param userInfo ログインユーザー情報
   */
  @Override
  public void setUserInfo(User userInfo) {
    this.userInfo = userInfo;
  }

  /** オプション情報 */
  private JSONObject option;

  /**
   * オプション情報を取得します。
   */
  @Override
  public JSONObject getOption() {
    return option;
  }

  /**
   * オプション情報を設定します。
   *
   * @param option
   */
  @Override
  public void setOption(JSONObject option) {
    this.option = option;
  }

  /**
   * ユーザー情報のログ文を取得
   *
   * @return 検索条件の内容
   */
  protected String getUserInfoLog(User userInfo) {
    return "実行ユーザー情報：" + userInfo.getName();
  }

  /**
   * 検索条件の出力ログ文を取得(map)
   *
   * @return 検索条件の内容
   */
  protected String getConditionLog() {
    Object[] items = null;
    Map<String, String> m = new TreeMap<String, String>(getMap());
    for (String key : m.keySet()) {
      items = ArrayUtils.add(items, key + ":" + m.get(key));
    }
    return StringUtils.join(items, ",");
  }

  /**
   * 画面取得カンマ区切り検索条件をDB検索条件の形式に変換する。
   *
   * @param text 画面取得カンマ区切り検索条件
   * @return 検索条件の内容
   */
  protected static String convCommaString(String text) {
    if (StringUtils.isEmpty(text)) {
      return "";
    }
    JSONArray dataArray = JSONArray.fromObject(StringUtils.split(text, ','));
    String convData = "";
    for (int i = 0; i < dataArray.size(); i++) {
      convData += "'" + dataArray.get(i).toString() + "',";
    }
    return StringUtils.removeEnd(convData, ",");
  }

  /**
   * 数値文字列を引数形式でフォーマットする。
   *
   * @param val 数値文字列
   * @param format フォーマット
   * @return 変換文字列
   */
  protected String convFormat(String val, String format) {
    double dValue = NumberUtils.toDouble(val);
    DecimalFormat dFormat = new DecimalFormat(format);
    return dFormat.format(dValue);
  }

  /**
   * 検索条件表示用情報の共通箇所設定
   *
   * @param jad 検索条件
   */
  protected void createCmnOutput(JsonArrayData jad) {
    // タイトル名称
    List<String> cells = new ArrayList<String>();
    cells.add(jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(0, cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(1, cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);
  }

  /**
   * 必須分類条件時の条件有効無効判断<br>
   * 集計単位と分類の選択値によって、条件が有効か無効かを返す。<br>
   *
   * @param type 判断する条件値(=集計単位の各要素の値)
   * @param szSyukei 集計単位選択値
   * @param szBunrui 分類条件選択値
   * @return true:有効/false:無効
   */
  protected boolean isUsefulBunrui(DefineReport.Option type, String szSyukei, String szBunrui) {
    // 選択値が空の場合は条件無効
    if (StringUtils.isEmpty(szBunrui)) {
      return false;
    }
    if (DefineReport.Values.NONE.getVal().equals(szBunrui)) {
      return false;
    }
    return this.isUsefulBunrui(type, szSyukei);
  }

  /**
   * 必須分類条件時の条件有効無効判断<br>
   * 集計単位と分類の選択値によって、条件が有効か無効かを返す。<br>
   *
   * @param type 判断する条件値(=集計単位の各要素の値)
   * @param szSyukei 集計単位選択値
   * @return true:有効/false:無効
   */
  protected boolean isUsefulBunrui(DefineReport.Option type, String szSyukei) {
    return NumberUtils.toInt(type.getVal()) <= NumberUtils.toInt(szSyukei);
  }

  /**
   * 同一項目の＝条件句を返却する<br>
   * <br>
   *
   * @param collection 列名リスト
   * @param prefix1 テーブル別名1
   * @param prefix2 テーブル別名2
   * @return =条件句
   */
  protected String convEqualText(Object[] collection, String prefix1, String prefix2) {
    String text = "";
    for (Object val : collection) {
      text += prefix1 + val.toString() + " = " + prefix2 + val.toString() + " and ";
    }
    return StringUtils.removeEnd(text, " and ");
  }

  /**
   * 同一項目の＝条件句を返却する<br>
   * <br>
   *
   * @param collection 列名リスト
   * @param prefix1 テーブル別名1
   * @param prefix2 テーブル別名2
   * @return =条件句
   */
  protected String convEqualText2(Object[] collection, String prefix1, String prefix2) {
    String text = "";
    for (Object val : collection) {
      text += prefix1 + val.toString() + " = nvl(" + prefix2 + val.toString() + "," + prefix1 + val.toString() + ") and ";
    }
    return StringUtils.removeEnd(text, " and ");
  }

  /**
   * 同一項目のnvl句を返却する<br>
   * <br>
   *
   * @param collection 列名リスト
   * @param prefix1 テーブル別名1
   * @param prefix2 テーブル別名2
   * @return =条件句
   */
  protected String convNVLText(Object[] collection, String prefix1, String prefix2) {
    String text = "";
    for (Object val : collection) {
      text += "nvl(" + prefix1 + val.toString() + " , " + prefix2 + val.toString() + ") ,";
    }
    return StringUtils.removeEnd(text, " ,");
  }

  /**
   * 同一項目のnvl句を返却する<br>
   * <br>
   *
   * @param collection 列名リスト
   * @param prefix1 テーブル別名1
   * @param prefix2 テーブル別名2
   * @return =条件句
   */
  protected String convNVLText(Object[] collection, Object[] prefix) {
    String text = "";
    for (Object val : collection) {
      text += "nvl(" + StringUtils.join(prefix, val + ",") + val + ") ,";
    }
    return StringUtils.removeEnd(text, " ,");
  }

  /**
   * 同一項目のnvl句を返却する<br>
   * <br>
   *
   * @param collection 列名リスト
   * @param prefix1 テーブル別名1
   * @param prefix2 テーブル別名2
   * @return =条件句
   */
  protected String convNVLCol(Object[] collection, Object[] prefix) {
    String text = "";
    for (Object val : collection) {
      text += "nvl(" + StringUtils.join(prefix, val + ",") + val + ") as " + val.toString() + ",";
    }
    return text;
  }

  /**
   * feche条件句を返却する<br>
   * <br>
   *
   * @param rowNum 最大行数
   * @return =条件句
   */
  protected String getFechSql() {
    return getFechSql(DefineReport.MAX_ROWNUM);
  }

  /**
   * feche条件句を返却する<br>
   * <br>
   *
   * @param rowNum 最大行数
   * @return =条件句
   */
  protected String getFechSql(String rowNum) {
    return " LIMIT " + rowNum + " ";
  }

  /** message 情報 */
  private String message = "";

  /**
   * メッセージの取得
   */
  @Override
  public String getMessage() {
    return this.message;
  }

  /**
   * メッセージの設定
   *
   * @param message メッセージ文字列
   */
  @Override
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * 排他判断<br>
   * 更新日時が変わっていないことを判断する<br>
   *
   * @param targetTable 対象テーブル
   * @param targetWhere テーブルのキー条件
   * @param targetParam テーブルのキー
   * @param inp_upddt 画面入力開始時の更新日時
   * @return true:一致/false:不一致(排他発生)
   */
  public boolean checkExclusion(String targetTable, String targetWhere, ArrayList<String> targetParam, String inp_upddt) {
    if (targetTable.length() > 0 && targetWhere.length() > 0) {
      ItemList iL = new ItemList();
      String sqlcommand = "select count(*) as VALUE from " + targetTable + " as T1 where " + targetWhere;
      // 新規の場合
      if (StringUtils.isEmpty(inp_upddt)) {
        // 関連情報取得
        @SuppressWarnings("static-access")
        JSONArray array = iL.selectJSONArray(sqlcommand, targetParam, Defines.STR_JNDI_DS);

        if (array.size() == 0 || NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
          // 新規行の場合
          return true;
        } else if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) != 0) {
          // 更新日時が空でも、レコードが存在する場合。
          if (checkExistUpddt(targetTable)) {
            sqlcommand = "select UPDDT as VALUE from " + targetTable + " where " + targetWhere;
            @SuppressWarnings("static-access")
            JSONArray array2 = iL.selectJSONArray(sqlcommand, targetParam, Defines.STR_JNDI_DS);
            if (array2.size() > 0 && !array2.getJSONObject(0).has("VALUE")) {
              // [更新日]項目が未設定のデータの場合は排他扱いにしない。
              return true;
            }
          }
        }
      } else {
        targetParam.add(inp_upddt);
        sqlcommand = sqlcommand + " and DATE_FORMAT(UPDDT, '%Y%m%d%H%i%s%f') = ? ";
        @SuppressWarnings("static-access")
        JSONArray array = iL.selectJSONArray(sqlcommand, targetParam, Defines.STR_JNDI_DS);
        if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * 排他判断<br>
   * 更新日時が変わっていないことを判断する<br>
   *
   * @param array SQL実行結果
   * @param inp_upddt 画面入力開始時の更新日時
   * @return true:一致/false:不一致(排他発生)
   */
  public boolean checkExclusion(JSONArray array, String inp_upddt) {
    // 新規の場合
    if (StringUtils.isEmpty(inp_upddt)) {
      if (array.size() == 0 || NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0) {
        return true;
      }
    } else {
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }


  /**
   * 排他判断<br>
   * 対象のテーブルに"更新日"の項目が存在するか判断する<br>
   *
   * @param targetTable 対象テーブル
   */
  public boolean checkExistUpddt(String targetTable) {
    // 新規の場合
    ItemList iL = new ItemList();
    String sqlcommand = "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = ? and TABLE_NAME = ? ";
    ArrayList<String> targetParam = new ArrayList<String>();
    String[] tableInfo = targetTable.split("\\.");

    if (StringUtils.isEmpty(targetTable) || tableInfo.length < 2) {
      return false;
    }
    targetParam.add(tableInfo[0]);
    targetParam.add(tableInfo[1]);
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlcommand, targetParam, Defines.STR_JNDI_DS);
    if (array.size() > 0) {
      for (int i = 0; i < array.size(); i++) {
        String colNmae = array.getJSONObject(i).optString("NAME");
        if (StringUtils.equals("UPDDT", colNmae)) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * DB存在チェック<br>
   * 条件のデータが存在するかチェック<br>
   *
   * @param tbl 対象テーブル
   * @param col テーブルのキー
   * @param val 基本となる値
   * @param whr 追加条件
   * @param paramData パラメータ
   * @return true:一致/false:不一致(排他発生)
   */
  public boolean getDbExist(String tbl, String col, String val, String whr, ArrayList<String> paramData) {
    if (tbl.length() > 0 && col.length() > 0) {
      ItemList iL = new ItemList();
      String sqlcommand = "";
      sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", val) + whr;

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }


  /**
   * DB2の数値型最大値<br>
   */
  protected enum dbNumericTypeInfo {
    /** SMALLINT */
    SMALLINT(5, -32768, 32768),
    /** INT */
    INT(10, -2147483648, 2147483648l),
    /** INTEGER */
    INTEGER(10, -2147483648, 2147483648l);

    private final int digit;
    private final long max;
    private final long min;

    /** 初期化 */
    private dbNumericTypeInfo(int digit, long min, long max) {
      this.digit = digit;
      this.min = min;
      this.max = max;
    }

    /** @return digit */
    public int getDigit() {
      return digit;
    }

    /** @return min */
    public long getMin() {
      return min;
    }

    /** @return max */
    public long getMax() {
      return max;
    }
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


  /** FTPファイル情報 */
  public interface FtpFileInfoInterface {
    /** @return bnm ファイル名 */
    public String getFnm();

    /** @return len レコード長 */
    public Integer getLen();
  }

  /** File出力項目などで参照しているテーブル */
  public enum RefTable {
    /** 商品マスタ */
    MSTSHN("MSTSHN", "商品マスタ"),
    /** ソースコード管理マスタ */
    MSTSRCCD("MSTSRCCD", "ソースコード管理マスタ"),
    /** 仕入グループ商品 */
    MSTSIRGPSHN("MSTSIRGPSHN", "仕入グループ商品"),
    /** 売価コントロールマスタ */
    MSTBAIKACTL("MSTBAIKACTL", "売価コントロールマスタ"),
    /** 品揃グループマスタ */
    MSTSHINAGP("MSTSHINAGP", "品揃グループマスタ"),
    /** 平均パック単価マスタ */
    MSTAVGPTANKA("MSTAVGPTANKA", "平均パック単価マスタ"),
    /** 添加物マスタ */
    MSTTENKABUTSU("MSTTENKABUTSU", "添加物マスタ"),
    /** グループ分類 */
    MSTGRP("MSTGRP", "グループ分類マスタ"),
    /** 自動発注管理マスタ */
    MSTAHS("MSTAHS", "自動発注管理マスタ"),
    /** 店別異部門管理 */
    MSTSHNTENBMN("MSTSHNTENBMN", "店別異部門管理"),
    /** CSV取込トラン_商品マスタ */
    CSVSHN("CSVSHN", "CSV取込トラン_商品マスタ"),
    /** 提案商品マスタ */
    PIMTIT("PIMTIT", "提案商品マスタ"),
    /** 提案ソースコード管理マスタ */
    PIMTISRCCD("PIMTISRCCD", "提案ソースコード管理マスタ"),
    /** 提案仕入グループ商品 */
    PIMTISIRGPSHN("PIMTISIRGPSHN", "提案仕入グループ商品"),
    /** 提案売価コントロールマスタ */
    PIMTIBAIKACTL("PIMTIBAIKACTL", "提案売価コントロールマスタ"),
    /** 提案品揃グループマスタ */
    PIMTISHINAGP("PIMTISHINAGP", "提案品揃グループマスタ"),
    /** 提案添加物マスタ */
    PIMTITENKABUTSU("PIMTITENKABUTSU", "提案添加物マスタ"),

    /** CSV */
    CSV("CSV", "CSVファイル"),
    /** その他 */
    OTHER("OTHER", "その他");

    private final String id;
    private final String txt;

    /** 初期化 */
    private RefTable(String id, String txt) {
      this.id = id;
      this.txt = txt;
    }

    /** @return id 物理名称 */
    public String getId() {
      return id;
    }

    /** @return txt 論理名称 */
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（SQLタイプ）<br>
   */
  public enum SqlType {
    /** INSERT */
    INS(1, "INSERT"),
    /** UPDATE */
    UPD(2, "UPDATE"),
    /** DELETE */
    DEL(3, "DELETE"),
    /** MERGE */
    MRG(4, "MERGE");

    private final Integer val;
    private final String txt;

    /** 初期化 */
    private SqlType(Integer val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    public Integer getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 空白値チェック
   *
   * @param value:値
   * @param zeroEmpty:ゼロを空白として扱うか否か
   * @return true-空白/false-空白じゃない
   */
  public boolean isEmptyVal(String value, boolean zeroEmpty) {
    if (StringUtils.isEmpty(value)) {
      return true;
    }
    if (zeroEmpty && NumberUtils.isNumber(value) && NumberUtils.toDouble(value) == 0d) {
      return true;
    }
    return false;
  }

  /**
   * 簡易JSONObject作成<br>
   * 配列をJSONObjectに詰める
   *
   * @param keys:キー配列
   * @param values:値配列
   * @return JSONObject
   */
  public JSONObject createJSONObject(String[] keys, String[] values) {
    JSONObject obj = new JSONObject();
    for (int i = 0; i < keys.length; i++) {
      obj.put(keys[i], values[i]);
    }
    return obj;
  }

  /** FTP転送ファイルの改行コード桁数 */
  public final static int LEN_NEW_LINE_CODE = 2;

  /**
   * FTP転送ファイルの共通ヘッダー情報(識別H1)取得用SQL作成
   *
   * @param fnm:ファイル名
   * @param ope:オペレータ
   * @param len:レコード長
   * @param setRno:RNO列として1をセットするか否か
   * @return sql
   * @throws Exception
   */
  public String createCmnSqlFTPH1(String fnm, String ope, Integer len, boolean setRno) {
    StringBuffer sbSQL = new StringBuffer();
    int dlen = len - LEN_NEW_LINE_CODE;
    sbSQL.append(" select ");
    if (setRno) {
      sbSQL.append(" 0 as RNO,");
    }
    sbSQL.append(" rpad('H1'||left(rpad('" + fnm + "', 8, ' '),8)||left(rpad('" + ope + "', 20, ' '),20)||to_char(current timestamp,'YYYYMMDDHH24MISS'), " + dlen + ", ' ')  as REC");
    sbSQL.append(" from sysibm.sysdummy1");
    return sbSQL.toString();
  }


  /**
   * 全店特売(アンケート有/無)判断
   *
   * @param szMoyskbn:催し区分
   * @param szMoysrban:催し連番
   * @return true:アンケート有/false:アンケート無
   * @throws Exception
   */
  public boolean isTOKTG(String szMoyskbn, String szMoysrban) {
    return DefineReport.ValKbn10002.VAL1.getVal().equals(szMoyskbn) && NumberUtils.toInt(szMoysrban, -1) >= 50;

  }

}
