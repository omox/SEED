/*
 * 作成日: 2006/10/26
 *
 */
package authentication.connection;

import javax.sql.DataSource;
import java.sql.Connection;
import javax.naming.InitialContext;

/**
 * データベース接続処理クラス <br />
 */
public class DBConnection {
	/**
	 * クラス変数の宣言
	 */
	private static DataSource ds_; // データソース

	/**
	 * 初期化
	 * jndiを設定し、DBとのJDBC接続の設定を行う。
	 *
	 * @param strJndi  JNDI名
	 * @return 処理結果
	 * @throws Exception 例外
	 */
	public static boolean init(String strJndi) throws Exception {

		try {

			/**
			 * データソースの取得
			 */
			InitialContext ic = new InitialContext(); // JNDI名称の取得
			ds_ = (DataSource) ic.lookup(strJndi); // データソースの取得

			/**
			 * 例外処理
			 */
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}

		return true;
	}

	/**
	 * コネクション取得 <br />
	 *
	 * @return コネクション <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	public static Connection getConnection() throws Exception {

		Connection con = null;
		try {
			/**
			 * コネクションの取得
			 */
			con = ds_.getConnection();
			/**
			 * 例外処理
			 */
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
		return con;
	}

	public static DataSource getDataSource() {
		return ds_;
	}
}
