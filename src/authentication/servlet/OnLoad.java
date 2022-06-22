/*
 * 作成日: 2007/10/26
 *
 */
package authentication.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import authentication.connection.DBConnection;

/**
 * アプリケーション起動時処理
 * アプリケーション起動時に、システム全体で使用する設定を行う
 */
public class OnLoad extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -6208092741720928643L;

	/**
	 * 初期化
	 */
	public void init(ServletConfig config) throws ServletException {

		try {
			/* JNDI、データソースの設定 */
			DBConnection.init(config.getInitParameter("JNDI"));
		} catch (Exception e) {
			throw new ServletException(e);
		}
		super.init(config);
	}

	/**
	 * 破棄
	 */
	public void destroy() {
		super.destroy();
	}
}
