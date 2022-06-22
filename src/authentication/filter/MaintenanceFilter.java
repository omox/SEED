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
 * セッションより権限を取得し、メンテナンス画面を閲覧する権限があるかをチェックする
 * 権限がない、又は無効な場合は、メニュー画面へ強制的に遷移させ、
 * それ以外の場合は処理を継続させる。
 */
public class MaintenanceFilter implements Filter {
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

		String mside = httpRequest.getParameter(Form.MTN_SIDE) != null ? (String) httpRequest.getParameter(Form.MTN_SIDE) : null;
		String kmside = null;

		/* セッションチェック */
		if (mside == null) {
			/**
			 * 別ページ遷移設定 : Menuへ
			 *
			 * レポートパラメータ不備
			 */
			httpRequest.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			httpRequest.setAttribute(Form.COMMON_MSG, "無効なURLです。");
			httpRequest.getRequestDispatcher(config.getInitParameter("Menu")).forward(request, response);
		}

		/**
		 * 初画面表示時セッションクリア
		 */
		if (kmside != null) {
		} else if (httpRequest.getParameter(Form.MTN_INPAREA) == null
				&& httpRequest.getParameter(Form.MTN_SELAREA) == null) {

			/* メンテナンス画面のListクリア */
			session.removeAttribute(Consts.STR_SES_GRD);
			session.removeAttribute(Consts.STR_SES_GRD2);
			session.removeAttribute(Consts.STR_SES_GRD3);
			session.removeAttribute(Consts.STR_SES_GRD4);
			session.removeAttribute(Consts.STR_SES_GRD5);
			session.removeAttribute(Consts.STR_SES_GRD6);
			session.removeAttribute(Consts.STR_SES_TITLE);

		}
		/**
		 * マスターメンテナンス権限チェック
		 */
		ArrayList mstr = (ArrayList) session.getAttribute(Consts.STR_SES_MSTSIDE);
		boolean chkauth = false;
		if (mstr != null) {
			for (int i = 0; i < mstr.size(); i++) {
				Side m = (Side) mstr.get(i);
				if (kmside != null) {
					chkauth = true;
					break;
				} else if (m.getSide() == Long.parseLong(mside)) {
					chkauth = true;
					break;
				}
			}
		}
		if (!chkauth) {
			/**
			 * 別ページ遷移設定 : Menuへ
			 * 閲覧権限なし
			 */
			/* エラーメッセージの設定 */
			httpRequest.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			httpRequest.setAttribute(Form.COMMON_MSG, Message.ERR_AUTH_INVALID);
			// セッション初期化
			httpRequest.getRequestDispatcher(config.getInitParameter("Menu"))
					.forward(request, response);
		}
		/**
		 * 表示処理へ
		 */
		chain.doFilter(httpRequest, httpResponse);
		return;

	}
}