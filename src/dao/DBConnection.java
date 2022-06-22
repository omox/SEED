/*
 * 作成日: 2006/08/04
 *
 */
package dao;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * データベース接続処理クラス <br />
 */
public class DBConnection {
	/**
	 * クラス変数の宣言
	 */
	private static DataSource ds_;	// データソース

	/**
	 * 初期化 <br />
	 * jndiを設定し、DBとのJDBC接続の設定を行う。 <br />
	 *
	 * @param strJndi JNDI名 <br />
	 * @return 処理結果 <br />
	 * @throws Exception 例外 <br />
	 */
	public static boolean init(String strJndi) throws Exception {

		try {

			/**
			 * データソースの取得
			 */
			InitialContext ic = new InitialContext();	// JNDI名称の取得
			ds_ = (DataSource) ic.lookup(strJndi);		// データソースの取得
		} catch (Exception e) {
			throw e;
		}

		return true;
	}

	/**
	 * コネクション取得 <br />
	 *
	 * @return コネクション <br />
	 * @throws Exception 例外 <br />
	 */
	public static Connection getConnection(String jdbcName) throws Exception {
		Connection con = null;
		try {
			/**
			 * コネクションの取得
			 */
			con = ds_.getConnection();

		} catch (Exception e) {
			throw e;
		}
		return con;
	}
}
