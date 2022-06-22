/*
 * 作成日: 2007/10/26
 *
 */
package authentication.dbaccess;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;

import authentication.connection.DBConnection;

/**
 * SYS_LOGテーブルにアクセスする
 */
public class DBinfo {

	/**
	 * SQL
	 */

	// ログ書き込み用
	public static final String ID_SQL_INSERT_SYSLOGS =
		"insert into KEYSYS.SYS_LOGS(CD_USER,DT_ACTION,CD_ACTION,REMARK) values(?,current_timestamp,?,?)";

	/**
	 * ログ書き込み
	 */
	public void setLogMenu(String id_user) {
		Connection con = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			con = qr.getDataSource().getConnection();
			qr.update(con, ID_SQL_INSERT_SYSLOGS, new Object[] {
					id_user,
					"login",
					"BluePrintSystem"
				}
			);
			con.commit();
			con.close();

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
					con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}
}