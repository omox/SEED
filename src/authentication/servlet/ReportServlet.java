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
/*
 * 作成日: 2007/10/26
 *
 */
package authentication.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import authentication.bean.Report;
import authentication.bean.User;
import authentication.defines.Consts;
import authentication.defines.Defines;
import authentication.defines.Form;
import authentication.util.ReportHelper;


/**
 * レポート画面表示制御クラス
 * レポート画面に表示するレポートを制御するクラス
 */
public class ReportServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 4864413765074297857L;
	private ServletConfig config = null;

	/**
	 * 初期化
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
	}

	/**
	 * 破棄
	 */
	public void destroy() {
		super.destroy();
	}

	/**
	 * リクエスト時処理
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * リクエスト時処理
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/**
		 * 処理準備
		 */
		// セッションの取得
		HttpSession session = request.getSession();
		// ページ遷移先
		String strPage = null;

		try {

			/**
			 * レポート表示処理
			 */

			// ACTION 情報ありの場合、事前設定内容を継続する
			if (session.getAttribute(Form.ACTION)==null){

				// レポート分類番号
				setSession(session, Consts.STR_SES_REPORT_SIDE, request.getParameter(Form.REPORT_SIDE));
				// レポート番号
				setSession(session, Consts.STR_SES_REPORT_NO, request.getParameter(Form.REPORT_NO));
				// レポート分類の配列格納番号
				setSession(session, Consts.STR_SES_REPORT_SIDE_ARRAY, request.getParameter(Form.REPORT_SIDE_ARRAY));
				// レポートの配列格納番号
				setSession(session, Consts.STR_SES_REPORT_NO_ARRAY, request.getParameter(Form.REPORT_NO_ARRAY));

				// 親画面からの引き継ぎ情報
				String sendParam = request.getParameter(Consts.STR_SES_REPORT_SEND_PARAM);
				if (sendParam == null) {
					sendParam = "";
				}
				session.setAttribute(Consts.STR_SES_REPORT_SEND_PARAM,	sendParam);
				String sendMode = request.getParameter(Consts.STR_SES_REPORT_SEND_MODE);
				if (sendMode == null) {
					sendMode = "";
				}
				session.setAttribute(Consts.STR_SES_REPORT_SEND_MODE,	sendMode);

			} else {
				// ACTION 情報のクリア
				session.removeAttribute(Form.ACTION);
			}

			// ユーザ情報の取得
			User loginusr = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
			if (loginusr != null) {
				session.setAttribute(Defines.ID_REQUEST_USER_ID, loginusr.getId());
			}

			// 該当レポート情報取得
			ReportHelper reportHelp = new ReportHelper();
			Report rep = reportHelp.getReportObject(request);
			if (rep != null) {
				session.setAttribute(Defines.ID_REQUEST_CUSTOM_USER,	rep.getUsers_custom());
				session.setAttribute(Defines.ID_REQUEST_CUSTOM_GROUP,	rep.getGroup_custom());
				session.setAttribute(Defines.ID_REQUEST_CUSTOM_POS,		rep.getPosmst_custom());
				session.setAttribute(Defines.ID_REQUEST_CUSTOM_SIDE,	rep.getSide_custom());
				session.setAttribute(Defines.ID_REQUEST_CUSTOM_REPORT,	rep.getReport_custom());
				session.setAttribute(Defines.ID_REQUEST_JSP_REPORT,		rep.getReport_jsp());
				session.setAttribute(Defines.ID_REQUEST_TITLE_REPORT,	rep.getReport_name());

				session.setAttribute(Consts.STR_SES_REPORT_NO,	String.valueOf( rep.getReport_no() ));
				session.setAttribute(Consts.STR_SES_REPORT_YOBI1,	String.valueOf( rep.getYobi1_()));
				session.setAttribute(Consts.STR_SES_REPORT_YOBI2,	String.valueOf( rep.getYobi2_()));
				session.setAttribute(Consts.STR_SES_REPORT_YOBI3,	String.valueOf( rep.getYobi3_()));
				session.setAttribute(Consts.STR_SES_REPORT_YOBI4,	String.valueOf( rep.getYobi4_()));
				session.setAttribute(Consts.STR_SES_REPORT_YOBI5,	String.valueOf( rep.getYobi5_()));
			}

			// ページ遷移先の設定
			strPage = config.getInitParameter("Report");
		} catch (Exception e) {

			//例外処理
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
	 * 指定の値をセッションに格納する
	 * null の場合は、セッションから読込
	 * @param session	セッション変数
	 * @param keyName	格納名
	 * @param keyValue	格納値
	 * @return String
	 */
	private String setSession(HttpSession session, String keyName, String keyValue) {

		// 戻り値
		String rtnValue = keyValue;

		// セッション格納値の判定
		if (rtnValue == null) {
			// nullの場合
			if (session.getAttribute(keyName) == null ) {
				// セッションがnullの場合
				rtnValue = "0";

			} else if (session.getAttribute(keyName) instanceof String) {
				// 文字列型の場合
				rtnValue = (String) session.getAttribute(keyName);

			} else if (session.getAttribute(keyName) instanceof Long) {
				// Long 型の場合
				Long value = (Long) session.getAttribute(keyName);
				rtnValue = value.toString();

			} else {
				// その他はクリア
				rtnValue = "0";
			}
		}

		// セッションに設定
		session.setAttribute(keyName, rtnValue);

		return rtnValue;

	}

}
