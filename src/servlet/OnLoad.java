/*
 * 作成日: 2006/08/24
 *
 */
package servlet;

import common.Defines;
import dao.DBConnection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * アプリケーション起動時処理 <br />
 * アプリケーション起動時に、システム全体で使用する設定を行う。 <br />
 */
public class OnLoad extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -8641601266912491268L;

	/**
	 * 初期化 <br />
	 */
	public void init(ServletConfig config) throws ServletException {


		try {
			/* JNDI、データソースの設定 */
			DBConnection.init(Defines.STR_JNDI_DS);

		} catch (Exception e) {
			throw new ServletException(e);
		}
		super.init(config);
	}

	/**
	 * 破棄 <br />
	 */
	public void destroy() {
		super.destroy();
	}
}
