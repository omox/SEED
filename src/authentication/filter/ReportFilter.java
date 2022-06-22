/*
 * 作成日: 2007/10/26
 *
 */
package authentication.filter;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import authentication.bean.*;
import authentication.defines.*;

/**
 * セッションより権限を取得し、レポート画面を閲覧する権限があるかをチェックする
 * 権限がない、又は無効な場合は、メニュー画面へ強制的に遷移させ、
 * それ以外の場合は処理を継続させる。
 */
public class ReportFilter implements Filter {
	private FilterConfig config = null;

	/**
	 * 初期化
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	/**
	 * 破棄
	 */
	public void destroy() {
	}

	/**
	 * リクエスト時フィルター処理
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpSession session = httpRequest.getSession(false);

		// レポート分類番号
		String repside = httpRequest.getParameter(Form.REPORT_SIDE) != null ? (String) httpRequest.getParameter(Form.REPORT_SIDE) : null;
		if (repside == null) {
			repside = (String)session.getAttribute(Consts.STR_SES_REPORT_SIDE);
		}

		// レポート番号
		String repno = httpRequest.getParameter(Form.REPORT_NO) != null ? (String) httpRequest.getParameter(Form.REPORT_NO) : null;
		if (repno == null) {
			repno = (String)session.getAttribute(Consts.STR_SES_REPORT_NO);
		}

		if (repside == null && repno == null) {
			/**
			 * 別ページ遷移設定 : Menuへ
			 * レポートパラメータ不備
			 */
			httpRequest.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			httpRequest.setAttribute(Form.COMMON_MSG, "無効なURLです。");
			httpRequest.getRequestDispatcher(config.getInitParameter("Menu")).forward(request, response);
		}

		/**
		 * レポート権限チェック
		 */
		ArrayList menu = (ArrayList) session.getAttribute(Consts.STR_SES_REPSIDE);
		ArrayList report = null;
		boolean chkauth = false;

		if (menu != null) {
			for (int i = 0; i < menu.size(); i++) {
				Side m = (Side) menu.get(i);
				if (m.getSide() == Long.parseLong(repside)) {
					report = m.getReport();
					chkauth = true;
					break;
				}
			}
		}

		// レポート画面がsubmitした際のFormが違い、パラメータが足りないので判定できない。一時凍結 20080116
		if (!chkauth) {
			/**
			 * 別ページ遷移設定 : Menuへ
			 * 閲覧権限なし
			 */
			/* エラーメッセージの設定 */
			httpRequest.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			httpRequest.setAttribute(Form.COMMON_MSG, Message.ERR_AUTH_INVALID);
			httpRequest.getRequestDispatcher(config.getInitParameter("Menu")).forward(request, response);
		}

		/**
		 * レポートNo未指定の場合の自動No設定
		 */
		if (repno == null) {
			Report r = (Report) report.get(0);
			session.setAttribute(Consts.STR_SES_REPORT_NO, new Long(r.getReport_no()));
		}

		/**
		 * レポート表示処理へ
		 */
		chain.doFilter(httpRequest, httpResponse);
		return;
	}

}