/**
 *
 */
package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import dao.DBConnection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Omoto_Yuki
 *
 */
public class ItemList {

  /**
   * 商品入力の登録機能
   *
   * @param json
   * @return boolean
   * @throws Exception
   */
  public boolean setItemQuery(JSONArray json, String jdbcName) throws Exception {

    // 検索
    Connection con = null; // コネクション
    String sqlCommand = "";
    String szCD_USER = "";
    String szNM_CTG = "";

    try {
      // コネクションの取得
      con = DBConnection.getConnection(jdbcName);
      con.setAutoCommit(false);

      // T_CTG_HEADの登録
      sqlCommand = DefineReport.ID_SQL_KEY_SET_HEAD;

      // 実行SQL設定
      PreparedStatement stmt = con.prepareStatement(sqlCommand);

      JSONObject obj = (JSONObject) json.get(0);
      szCD_USER = obj.getString("CD_USER");
      szNM_CTG = obj.getString("NM_CTG");

      // パラメータ設定
      stmt.setString(1, szCD_USER);
      stmt.setString(2, szNM_CTG);

      // SQL実行
      stmt.executeUpdate();

      // T_CTG_ITEMの削除
      sqlCommand = DefineReport.ID_SQL_KEY_DELETE_ITEM;

      // 実行SQL設定
      stmt = con.prepareStatement(sqlCommand);

      // パラメータ設定
      stmt.setString(1, szCD_USER);
      stmt.setString(2, szNM_CTG);

      // SQL実行
      stmt.executeUpdate();

      // T_CTG_ITEMの登録
      sqlCommand = DefineReport.ID_SQL_KEY_SET_ITEM;

      // 実行SQL設定
      stmt = con.prepareStatement(sqlCommand);

      for (int i = 1; i < json.size(); i++) {
        obj = (JSONObject) json.get(i);
        if (!"".equals(obj.getString("CD_ITEM"))) {

          // パラメータ設定
          stmt.setString(1, obj.getString("CD_ITEM"));
          stmt.setString(2, Integer.toString(i));
          stmt.setString(3, szCD_USER);
          stmt.setString(4, szNM_CTG);

          // SQL実行
          stmt.executeUpdate();
        }
      }


      con.commit();
      con.close();

    } catch (Exception e) {
      /* 接続解除 */
      if (con != null) {
        try {
          con.rollback();
          con.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }

    return true;
  }

  /**
   * 商品入力Fileの削除処理
   *
   * @param json
   * @return boolean
   * @throws Exception
   */
  public boolean deleteItemQuery(JSONArray json, String jdbcName) throws Exception {

    // 検索
    Connection con = null; // コネクション
    String sqlCommand = "";
    String szCD_CTG = "";

    try {
      // コネクションの取得
      con = DBConnection.getConnection(jdbcName);
      con.setAutoCommit(false);

      // T_CTG_ITEMの削除
      sqlCommand = DefineReport.ID_SQL_KEY_DELETE_ITEM_SP;

      // 実行SQL設定
      PreparedStatement stmt = con.prepareStatement(sqlCommand);

      JSONObject obj = (JSONObject) json.get(0);
      szCD_CTG = obj.getString("CD_CTG");

      // パラメータ設定
      stmt.setString(1, szCD_CTG);

      // SQL実行
      stmt.executeUpdate();

      // T_CTG_HEADの削除
      sqlCommand = DefineReport.ID_SQL_KEY_DELETE_HEAD_SP;

      // 実行SQL設定
      stmt = con.prepareStatement(sqlCommand);

      // パラメータ設定
      stmt.setString(1, szCD_CTG);

      // SQL実行
      stmt.executeUpdate();

      con.commit();
      con.close();

    } catch (Exception e) {
      /* 接続解除 */
      if (con != null) {
        try {
          con.rollback();
          con.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }

    return true;
  }

  /**
   * 更新処理
   *
   * @param sqlCommand
   * @param paramData
   * @param jdbcName
   * @return
   * @throws Exception
   */
  public boolean executeItem(String sqlCommand, ArrayList<String> paramData, String jdbcName) throws Exception {

    // 検索
    Connection con = null; // コネクション

    try {
      // コネクションの取得
      con = DBConnection.getConnection(jdbcName);
      con.setAutoCommit(false);

      // 実行SQL設定
      PreparedStatement stmt = con.prepareStatement(sqlCommand);

      // パラメータ設定
      for (int i = 0; i < paramData.size(); i++) {
        stmt.setString((i + 1), paramData.get(i));
      }
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* [sql] */ " + sqlCommand + ";/* [prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
      }

      // SQL実行
      stmt.executeUpdate();

      con.commit();
      con.close();

    } catch (Exception e) {
      /* 接続解除 */
      if (con != null) {
        try {
          con.rollback();
          con.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }

    return true;
  }

  /**
   * 検索＆ArrayList生成
   *
   * @param sqlCommand 実行SQL
   * @param paramData SQLパラメータ
   * @param jdbcName 接続JNDI名
   * @return ArrayList
   */
  public ArrayList<List<String>> selectArray(String sqlCommand, ArrayList<String> paramData, String jdbcName) {
    ArrayList<List<String>> rows = new ArrayList<List<String>>();

    // 検索
    Connection con = null; // コネクション
    try {
      // コネクションの取得
      con = DBConnection.getConnection(jdbcName);

      // 実行SQL設定
      PreparedStatement stmt = con.prepareStatement(sqlCommand);

      // パラメータ判断
      if (paramData != null) {
        for (int i = 0; i < paramData.size(); i++) {
          stmt.setString((i + 1), paramData.get(i));
        }
      }
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* [sql] */ " + sqlCommand + ";/* [prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
      }
      // SQL実行
      ResultSet rs = stmt.executeQuery();

      // カラム数
      ResultSetMetaData rsmd = rs.getMetaData();
      int sizeColumn = rsmd.getColumnCount();

      // タイトル名称取得
      List<String> titles = new ArrayList<String>();
      for (int i = 1; i <= sizeColumn; i++) {
        titles.add(rsmd.getColumnName(i));
      }
      rows.add(titles);

      // 結果の取得
      while (rs.next()) {

        // 情報保存
        List<String> cols = new ArrayList<String>();

        for (int i = 1; i <= sizeColumn; i++) {
          if (null == rs.getString(i)) {
            cols.add("");
          } else {
            cols.add(rs.getString(i));
          }
        }
        // 情報保存（レコード）
        rows.add(cols);
      }

      con.commit();
      con.close();

    } catch (Exception e) {
      /* 接続解除 */
      if (con != null) {
        try {
          con.rollback();
          con.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }

    return rows;
  }

  /**
   * 検索＆JSON生成
   *
   * @param sqlCommand 実行SQL
   * @param paramData SQLパラメータ
   * @param jdbcName 接続JNDI名
   * @return JSON文字列
   */
  public static JSONArray selectJSONArray(String sqlCommand, ArrayList<String> paramData, String jdbcName) {
    JSONArray json = new JSONArray();

    // 検索
    Connection con = null; // コネクション
    try {
      // コネクションの取得
      con = DBConnection.getConnection(jdbcName);

      // 実行SQL設定
      PreparedStatement stmt = con.prepareStatement(sqlCommand);

      // パラメータ判断
      if (paramData != null) {
        for (int i = 0; i < paramData.size(); i++) {
          stmt.setString((i + 1), paramData.get(i));
        }
      }
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println("/* [sql] */ " + sqlCommand + ";/* [prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
      }
      // SQL実行
      ResultSet rs = stmt.executeQuery();

      // カラム数
      ResultSetMetaData rsmd = rs.getMetaData();
      int sizeColumn = rsmd.getColumnCount();

      // 結果の取得
      while (rs.next()) {
        JSONObject obj = new JSONObject();

        for (int i = 1; i <= sizeColumn; i++) {
          obj.put(rsmd.getColumnName(i), rs.getString(i));
        }

        // 行データ格納
        json.add(obj);
      }

      con.commit();
      con.close();

    } catch (Exception e) {
      /* 接続解除 */
      if (con != null) {
        try {
          con.rollback();
          con.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }

    return json;
  }
}
