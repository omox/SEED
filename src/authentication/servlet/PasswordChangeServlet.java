/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package authentication.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;

import authentication.bean.User;
import authentication.connection.DBConnection;
import authentication.defines.Consts;
import authentication.defines.Form;
import authentication.defines.Message;
import authentication.defines.SQL;
import authentication.util.Tagescape;
import authentication.validation.PasswordChangeValidation;

public class PasswordChangeServlet  extends HttpServlet  {
	/**
	 *
	 */
	private static final long serialVersionUID = 5747577826054129750L;
	private ServletConfig config = null;

	/**
	 * 初期化 <br />
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
	}

	/**
	 * 破棄 <br />
	 */
	public void destroy() {
		super.destroy();
	}

	/**
	 * リクエスト時処理 <br />
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}
	/**
	 * リクエスト時処理 <br />
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String strPage = null;
		try {
			if (request.getParameter(Form.MTN_INPAREA) != null) {
				/**
				 * 入力エリア処理
				 */
				// イベント取得

				String event = request.getParameter(Form.MTN_INPAREA).toString();
				Tagescape esc = new Tagescape();
				PasswordChangeValidation vld = new PasswordChangeValidation();
				if (event.equals(Form.MTN_SAVBTN)) {
					// 入力された値の取得

					String old_pass = esc.htmlEscape(request.getParameter(Form.MTN_OLD_PASS).toString().trim());
					String new_pass = esc.htmlEscape(request.getParameter(Form.MTN_NEW_PASS).toString().trim());
					User loginusr = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
					String er_flg = getPassword(old_pass,loginusr);
					if (vld.entry(old_pass, new_pass,loginusr,er_flg) == false) {
						/* エラーメッセージの設定 */
						request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
						request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
					}else{
						String msg= null;
						msg = setPassChange(old_pass,new_pass,loginusr,msg);

						request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_SUP);
						request.setAttribute(Form.COMMON_MSG, msg);
					}
				}
			}

			strPage = config.getInitParameter("PasswordChange");
		} catch (Exception e) {
			e.printStackTrace();
			// エラーメッセージの設定
			request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			request.setAttribute(Form.COMMON_MSG, e.getMessage());
			// ページ遷移先の設定
			strPage = this.getServletContext().getInitParameter("Page_Error");
		}
		/**
		 * ページ遷移
		 */
		getServletContext().getRequestDispatcher(strPage).forward(request, response);

	}
	/**
	 * パスワードチェック
	 *
	 */
	protected String getPassword(String old_pass,User loginusr) throws SQLException {
		String sql = SQL.USER_SEL_001;
		Object[] param = { loginusr.getId(), old_pass };
		Map mu = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapHandler();
			mu = (Map) qr.query(sql, param, rsh);

		}catch (Exception en ){
			System.out.println("DBUtils NullPointerException:PasswordChangeServlet==SYS_USERS");
		}
		if (mu==null){
			return "false";
		}else{
			return "true";
		}

	}


	/**
	 * ユーザーパラーメタ変更設定
	 *
	 */
	protected String setPassChange(String old_pass,String new_pass, User loginusr,String msg) throws SQLException {
		Connection con = null; // コネクション
		String save = null;//保存･削除メッセージ
		try {
			/* コネクションの取得 */

			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			con = qr.getDataSource().getConnection();
			con.setAutoCommit(false);

			/**
			 * UPDATE
			 */
			int cnt = 0;
			save = Consts.SQL_MESSAGE_CHENGE;
			cnt = qr.update(con, SQL.PASSCHANGE_UPDATE_001, new Object[] {new_pass,loginusr.getId(),new Integer(loginusr.getCD_user())});
			System.out.println("UPDATE pkey db[SYS_USERS]:" + " COUNT:" + cnt);


			DbUtils.commitAndCloseQuietly(con);
			msg = save + Message.SUP_SAVE_END;
		} catch (SQLException e) {
			try {
				DbUtils.rollback(con);
			} catch (SQLException e2) {
			}
			DbUtils.closeQuietly(con);
			msg = save + Message.ERR_SAVE_END;
		}
		return msg;

	}

}
